#!/bin/bash

source testing/test_preamble.sh

# Runs lint checks and some similar things
echo Lint checks | tee -a $TEST_RESULTS
bin/check_style
echo check_style exit code $? | tee -a $TEST_RESULTS

out_dir=out/daq-test_stack
rm -rf $out_dir

t1sw1p6_pcap=$out_dir/t1sw1-eth6.pcap
t1sw1p28_pcap=$out_dir/t1sw1-eth28.pcap
t1sw2p28_pcap=$out_dir/t1sw2-eth28.pcap
t2sw1p1_pcap=$out_dir/t2sw1-eth1.pcap
t2sw1p50_pcap=$out_dir/t2sw1-eth50.pcap
t2sw1p52_pcap=$out_dir/t2sw1-eth52.pcap
t2sw2p1_pcap=$out_dir/t2sw2-eth1.pcap
nodes_dir=$out_dir/nodes

mkdir -p $out_dir $nodes_dir

ping_count=10
cap_length=$((ping_count + 20))
faucet_log=inst/faucet/daq-faucet-1/faucet.log

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
    cmd="ping -c $ping_count 192.168.1.$dst"
    echo $host: $cmd
    echo -n $host: $cmd\ > $out_file
    docker exec $host $cmd | fgrep time= | fgrep -v DUP | wc -l >> $out_file 2>/dev/null &
}

# Compare two numbers and output { -1, 0, 1 }
function comp {
    echo $((($1 - $2 > 0) - ($1 - $2 < 0)))
}

function test_stack {
    desc=$1
    echo Starting $desc stack test... | tee -a $TEST_RESULTS

    ip link  | fgrep t1sw | fgrep M-DOWN | sed -E 's/.*:(.*):.*/\1/' | tee -a $TEST_RESULTS

    # Some versions of OVS are somewhat unstable, so restart for consistency.
    echo $desc Restarting ovs...
    /etc/init.d/openvswitch-switch restart

    rm -f $faucet_log
    echo $desc Waiting for network stability...
    sleep 15
    
    echo $desc Capturing pcaps for $cap_length seconds...
    rm -f $out_dir/*.pcap
    timeout $cap_length tcpdump -eni t1sw1-eth6 -w $t1sw1p6_pcap &
    timeout $cap_length tcpdump -Q out -eni t1sw1-eth28 -w $t1sw1p28_pcap &
    timeout $cap_length tcpdump -Q out -eni t1sw2-eth28 -w $t1sw2p28_pcap &
    timeout $cap_length tcpdump -Q out -eni faux-1 -w $t2sw1p1_pcap &
    timeout $cap_length tcpdump -eni t2sw1-eth50 -w $t2sw1p50_pcap &
    timeout $cap_length tcpdump -eni t2sw1-eth52 -w $t2sw1p52_pcap &
    timeout $cap_length tcpdump -Q out -eni faux-2 -w $t2sw2p1_pcap &
    sleep 5

    echo $desc Simple tests...
    for from in 0 1 2 3; do
        for to in 0 1 2 3; do
            if [ $from != $to ]; then
                docker exec daq-faux-$from sh -c "arp -d 192.168.1.$to; ping -c 1 192.168.1.$to"
            fi
        done
    done

    echo $desc Pair tests...
    for from in 0 1 2 3; do
        for to in 0 1 2 3; do
            if [ $from != $to ]; then
                test_pair $from $to
            fi
        done
    done

    echo $desc Starting TCP probes...
    docker exec daq-faux-0 nc -w 1 192.168.1.1 23 2>&1 | tee -a $TEST_RESULTS
    docker exec daq-faux-0 nc -w 1 192.168.1.1 443 2>&1 | tee -a $TEST_RESULTS

    echo $desc Waiting for pair tests to complete...
    start_time=$(date +%s)
    wait
    end_time=$(date +%s)
    echo $desc Waited $((end_time - start_time))s.

    bcount6=$(tcpdump -en -r $t1sw1p6_pcap | wc -l) 2>/dev/null
    bcount50=$(tcpdump -en -r $t2sw1p50_pcap | wc -l) 2>/dev/null
    bcount52=$(tcpdump -en -r $t2sw1p52_pcap | wc -l) 2>/dev/null
    bcount_total=$((bcount50 + bcount52))
    echo $desc pcap count is $bcount6 $bcount50 $bcount52 $bcount_total
    echo pcap sane $((bcount6 < 100)) \
         $((bcount_total > 100)) $((bcount_total < 220)) | tee -a $TEST_RESULTS
    echo $desc pcap t2sw1p50
    tcpdump -en -c 20 -r $t2sw1p50_pcap
    echo $desc pcap t2sw1p52
    tcpdump -en -c 20 -r $t2sw1p52_pcap
    echo $desc pcap end

    bcount1e=$(tcpdump -en -r $t1sw1p28_pcap ether broadcast| wc -l) 2>/dev/null
    bcount2e=$(tcpdump -en -r $t1sw2p28_pcap ether broadcast| wc -l) 2>/dev/null
    bcount1h=$(tcpdump -en -r $t2sw1p1_pcap ether broadcast | wc -l) 2>/dev/null
    bcount2h=$(tcpdump -en -r $t2sw2p1_pcap ether broadcast | wc -l) 2>/dev/null
    echo pcap bcast $(comp $bcount1e 4) $(comp $bcount2e 0) \
         $(comp $bcount1h 4) $(comp $bcount2h 4) | tee -a $TEST_RESULTS

    telnet50=$(tcpdump -en -r $t2sw1p50_pcap vlan and port 23 | wc -l) 2>/dev/null
    https50=$(tcpdump -en -r $t2sw1p50_pcap vlan and port 443 | wc -l) 2>/dev/null
    telnet52=$(tcpdump -en -r $t2sw1p52_pcap vlan and port 23 | wc -l) 2>/dev/null
    https52=$(tcpdump -en -r $t2sw1p52_pcap vlan and port 443 | wc -l) 2>/dev/null
    echo telnet $((telnet50 + telnet52)) https $((https50 + https52)) | tee -a $TEST_RESULTS

    cat $nodes_dir/* | tee -a $TEST_RESULTS

    echo Done with $desc stack test. | tee -a $TEST_RESULTS
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

function test_forch {
    cmd/forch 1 2>&1 &

    # Need to wait long enough for polling mechanisms to kick in.
    sleep 20

    for api in system_state dataplane_state switch_state cpn_state process_state; do
        curl http://localhost:9019/$api > $out_dir/$api.json
        echo forch results from $api
        cat $out_dir/$api.json
        echo
    done

    echo system_state | tee -a $TEST_RESULTS
    api_result=$out_dir/system_state.json
    jq .site_name $api_result | tee -a $TEST_RESULTS
    jq .state_summary_change_count $api_result | tee -a $TEST_RESULTS

    echo dataplane_state | tee -a $TEST_RESULTS
    api_result=$out_dir/dataplane_state.json
    jq '.egress_state' $api_result | tee -a $TEST_RESULTS
    jq '.switches."nz-kiwi-t1sw1".state' $api_result | tee -a $TEST_RESULTS
    jq '.stack_links."nz-kiwi-t1sw1:6@nz-kiwi-t1sw2:6".state' $api_result | tee -a $TEST_RESULTS

    echo switch_state | tee -a $TEST_RESULTS
    api_result=$out_dir/switch_state.json
    jq '.switches."nz-kiwi-t2sw1".root_path[1].switch' $api_result | tee -a $TEST_RESULTS
    jq '.switches."nz-kiwi-t2sw1".root_path[1].in' $api_result | tee -a $TEST_RESULTS
    jq '.switches."nz-kiwi-t2sw1".root_path[1].out' $api_result | tee -a $TEST_RESULTS
    jq '.switches."nz-kiwi-t1sw1".attributes.dp_id' $api_result | tee -a $TEST_RESULTS

    echo cpn_state | tee -a $TEST_RESULTS
    api_result=$out_dir/cpn_state.json
    for node in nz-kiwi-t1sw1 nz-kiwi-t2sw2; do
        jq ".cpn_nodes.\"$node\".attributes.cpn_ip" $api_result | tee -a $TEST_RESULTS
        jq ".cpn_nodes.\"$node\".attributes.role" $api_result | tee -a $TEST_RESULTS
        jq ".cpn_nodes.\"$node\".attributes.vendor" $api_result | tee -a $TEST_RESULTS
        jq ".cpn_nodes.\"$node\".attributes.model" $api_result | tee -a $TEST_RESULTS
        jq ".cpn_nodes.\"$node\".state" $api_result | tee -a $TEST_RESULTS
    done

    echo process_state | tee -a $TEST_RESULTS
    api_result=$out_dir/process_state.json
    jq .bosun.state $api_result | tee -a $TEST_RESULTS

    sudo kill `ps ax | fgrep forch | awk '{print $1}'`
}

echo Base Stack Setup | tee -a $TEST_RESULTS
bin/net_clean
bin/setup_stack local || exit 1

# Test that the 'local' mode of faucet is working properly.
echo 'print("supercalifragilisticexpialidocious")' > faucet/faucet/python_test.py
docker exec daq-faucet-1 python -m faucet.python_test 2>&1 | tee -a $TEST_RESULTS
rm faucet/faucet/python_test.py

echo Forch Tests | tee -a $TEST_RESULTS
test_forch

echo Stacking Tests | tee -a $TEST_RESULTS
test_stack stack-solid
ip link set t1sw1-eth9 down
test_stack stack-linkd
ip link set t1sw2-eth10 down
test_stack stack-twod
ip link set t1sw1-eth6 down
ip link set t1sw1-eth11 down
test_stack stack-broken
ip link set t1sw1-eth10 down
ip link set t1sw2-eth10 up
ip link set t1sw1-eth6 up
test_stack stack-restored

#echo Dot1x setup >> $TEST_RESULTS
#bin/net_clean
#test_dot1x

echo Done with cleanup. Goodby.
