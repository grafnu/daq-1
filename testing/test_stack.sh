#!/bin/bash

source testing/test_preamble.sh

# Runs lint checks and some similar things
echo Lint checks | tee -a $TEST_RESULTS
bin/check_style
echo check_style exit code $? | tee -a $TEST_RESULTS

out_dir=out/daq-test_stack
rm -rf $out_dir

t1sw1p28_pcap=$out_dir/t1sw1-eth28.pcap
t1sw2p28_pcap=$out_dir/t1sw2-eth28.pcap
t2sw1p1_pcap=$out_dir/t2sw1-eth1.pcap
t2sw1p47_pcap=$out_dir/t2sw1-eth47.pcap
t2sw1p48_pcap=$out_dir/t2sw1-eth48.pcap
t2sw2p1_pcap=$out_dir/t2sw2-eth1.pcap
nodes_dir=$out_dir/nodes

mkdir -p $out_dir $nodes_dir

ping_count=10
cap_length=$((ping_count + 20))

echo Generator tests | tee -a $TEST_RESULTS
rm -rf out/topology
bin/generate_topology raw_topo=topology/not-normal/nz-kiwi-ctr1 topo_dir=out/topology/normalized
#diff -r out/topology/normalized topology/nz-kiwi-ctr1/ | tee -a $TEST_RESULTS

sites=$(cd topology; ls -d *)
mkdir -p out/topology/generated
for site in $sites; do
    if [ ! -f topology/$site/site_config.json ]; then
        continue;
    fi
    bin/generate_topology site_config=topology/$site/site_config.json topo_dir=out/topology/generated/$site
done
#diff -r out/topology/generated topology/ | tee -a $TEST_RESULTS

function test_pair {
    src=$1
    dst=$2

    host=daq-faux-$src
    out_file=$nodes_dir/$host-$dst
    cmd="ping -c $ping_count 192.168.0.$dst"
    echo $host: $cmd
    echo -n $host: $cmd\ > $out_file
    docker exec $host $cmd | fgrep time= | fgrep -v DUP | wc -l >> $out_file 2>/dev/null &
}

# Compare two numbers and output { -1, 0, 1 }
function comp {
    echo $((($1 - $2 > 0) - ($1 - $2 < 0)))
}

function test_stack {
    echo Starting stack test... | tee -a $TEST_RESULTS

    echo Restarting faucet to force cold start...
    docker restart daq-faucet-1

    ip link  | fgrep t1sw | fgrep M-DOWN | sed -E 's/.*:(.*):.*/\1/' | tee -a $TEST_RESULTS

    echo Waiting for network stability...
    sleep 15

    echo Capturing pcaps for $cap_length seconds...
    timeout $cap_length tcpdump -Q out -eni t1sw1-eth28 -w $t1sw1p28_pcap &
    timeout $cap_length tcpdump -Q out -eni t1sw2-eth28 -w $t1sw2p28_pcap &
    timeout $cap_length tcpdump -Q out -eni faux-1 -w $t2sw1p1_pcap &
    timeout $cap_length tcpdump -eni t2sw1-eth47 -w $t2sw1p47_pcap &
    timeout $cap_length tcpdump -eni t2sw1-eth48 -w $t2sw1p48_pcap &
    timeout $cap_length tcpdump -Q out -eni faux-2 -w $t2sw2p1_pcap &
    sleep 5

    echo Simple tests...
    docker exec daq-faux-1 sh -c "arp -d 192.168.0.2; ping -c 1 192.168.0.2"
    docker exec daq-faux-1 sh -c "arp -d 192.168.0.3; ping -c 1 192.168.0.3"
    docker exec daq-faux-2 sh -c "arp -d 192.168.0.1; ping -c 1 192.168.0.1"
    docker exec daq-faux-2 sh -c "arp -d 192.168.0.3; ping -c 1 192.168.0.3"
    docker exec daq-faux-3 sh -c "arp -d 192.168.0.1; ping -c 1 192.168.0.1"
    docker exec daq-faux-3 sh -c "arp -d 192.168.0.2; ping -c 1 192.168.0.2"

    test_pair 1 2
    test_pair 1 3
    test_pair 2 1
    test_pair 2 3
    test_pair 3 1
    test_pair 3 2

    echo Starting TCP probes...
    docker exec daq-faux-1 nc -w 1 192.168.0.2 23 2>&1 | tee -a $TEST_RESULTS
    docker exec daq-faux-1 nc -w 1 192.168.0.2 443 2>&1 | tee -a $TEST_RESULTS

    echo Waiting for pair tests to complete...
    start_time=$(date +%s)
    wait
    end_time=$(date +%s)
    echo Waited $((end_time - start_time))s.

    bcount47=$(tcpdump -en -r $t2sw1p47_pcap | wc -l) 2>/dev/null
    bcount48=$(tcpdump -en -r $t2sw1p48_pcap | wc -l) 2>/dev/null
    bcount_total=$((bcount47 + bcount48))
    echo pcap count is $bcount47 $bcount48 $bcount_total
    echo pcap sane $((bcount_total > 100)) $((bcount_total < 220)) | tee -a $TEST_RESULTS
    echo pcap t2sw1p47
    tcpdump -en -c 20 -r $t2sw1p47_pcap
    echo pcap t2sw1p48
    tcpdump -en -c 20 -r $t2sw1p48_pcap
    echo pcap end

    bcount1e=$(tcpdump -en -r $t1sw1p28_pcap ether broadcast| wc -l) 2>/dev/null
    bcount2e=$(tcpdump -en -r $t1sw2p28_pcap ether broadcast| wc -l) 2>/dev/null
    bcount1h=$(tcpdump -en -r $t2sw1p1_pcap ether broadcast | wc -l) 2>/dev/null
    bcount2h=$(tcpdump -en -r $t2sw2p1_pcap ether broadcast | wc -l) 2>/dev/null
    echo pcap bcast $(comp $bcount1e 4) $(comp $bcount2e 0) \
         $(comp $bcount1h 4) $(comp $bcount2h 4) | tee -a $TEST_RESULTS

    telnet47=$(tcpdump -en -r $t2sw1p47_pcap vlan and port 23 | wc -l) 2>/dev/null
    https47=$(tcpdump -en -r $t2sw1p47_pcap vlan and port 443 | wc -l) 2>/dev/null
    telnet48=$(tcpdump -en -r $t2sw1p48_pcap vlan and port 23 | wc -l) 2>/dev/null
    https48=$(tcpdump -en -r $t2sw1p48_pcap vlan and port 443 | wc -l) 2>/dev/null
    echo telnet $((telnet47 + telnet48)) https $((https47 + https48)) | tee -a $TEST_RESULTS

    cat $nodes_dir/* | tee -a $TEST_RESULTS

    echo Done with stack test. | tee -a $TEST_RESULTS
}

function test_dot1x {
    bin/setup_dot1x
    echo Checking positive auth
    docker exec daq-faux-1 wpa_supplicant -B -t -c wpasupplicant.conf -i faux-eth0 -D wired
    sleep 15
    #docker exec daq-faux-1 ping -q -c 10 192.168.12.2 2>&1 | awk -F, '/packet loss/{print $1,$2;}' | tee -a $TEST_RESULTS
    docker exec daq-faux-1 kill -9 $(docker exec daq-faux-1 ps ax | grep wpa_supplicant | awk '{print $1}')
    echo Checking failed auth
    docker exec daq-faux-1 wpa_supplicant -B -t -c wpasupplicant.conf.wng -i faux-eth0 -D wired
    sleep 15
    #docker exec daq-faux-1 ping -q -c 10 192.168.12.2 2>&1 | awk -F, '/packet loss/{print $1,$2;}' | tee -a $TEST_RESULTS
}

echo Base Stack Setup | tee -a $TEST_RESULTS
bin/net_clean
bin/setup_stack local || exit 1

# Test that the 'local' mode of faucet is working properly.
echo 'print("supercalifragilisticexpialidocious")' > faucet/faucet/python_test.py
docker exec daq-faucet-1 python -m faucet.python_test 2>&1 | tee -a $TEST_RESULTS
rm faucet/faucet/python_test.py

echo Forch Tests | tee -a $TEST_RESULTS
cmd/forch 1 &
sleep 5
curl http://localhost:9019/overview > $out_dir/forch_overview.json
cat $out_dir/forch_overview.json
jq .hello $out_dir/forch_overview.json | tee -a $TEST_RESULTS
sudo kill `ps ax | fgrep forch | awk '{print $1}'`

echo Stacking Tests | tee -a $TEST_RESULTS
test_stack
ip link set t1sw1-eth9 down
test_stack
ip link set t1sw2-eth10 down
test_stack
ip link set t1sw1-eth12 down
test_stack
ip link set t1sw1-eth10 down
ip link set t1sw2-eth10 up
ip link set t1sw1-eth12 up
test_stack

#echo Dot1x setup >> $TEST_RESULTS
#bin/net_clean
#test_dot1x

echo Done with cleanup. Goodby.
