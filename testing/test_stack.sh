#!/bin/bash

source testing/test_preamble.sh

export HOSTNAME=127.0.0.1

local=
if [ "$1" == local ]; then
    local=y
    shift
fi

# Runs lint checks and some similar things
if [ -z "$local" ]; then
    echo Lint checks | tee -a $TEST_RESULTS
    bin/check_style
    echo check_style exit code $? | tee -a $TEST_RESULTS
fi

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

cap_base=10
ping_count=10
num_pairs=12
cap_length=$((cap_base + ping_count + num_pairs * 2))
faucet_log=inst/faucet/daq-faucet-1/faucet.log

function test_pair {
    src=$1
    dst=$2

    host=daq-faux-$src
    out_file=$nodes_dir/$host-$dst
    cmd="ping -c $ping_count 192.168.1.$dst"
    echo $host: $cmd
    echo -n $host: $cmd\ > $out_file
    docker exec $host $cmd | fgrep time= | wc -l >> $out_file 2>/dev/null &
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
    sleep 30

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

    echo $desc Waiting for port capture to complete...
    sleep $cap_length

    bcount6=$(tcpdump -en -r $t1sw1p6_pcap | wc -l) 2>/dev/null
    bcount50=$(tcpdump -en -r $t2sw1p50_pcap | wc -l) 2>/dev/null
    bcount52=$(tcpdump -en -r $t2sw1p52_pcap | wc -l) 2>/dev/null
    bcount_total=$((bcount50 + bcount52))
    echo $desc pcap count is $bcount6 $bcount50 $bcount52 $bcount_total
    echo pcap sane $((bcount6 < 100)) \
         $((bcount_total > 100)) $((bcount_total < 220)) | tee -a $TEST_RESULTS
    for link in t1sw1p28 t1sw2p28 t2sw1p50 t2sw1p52; do
        eval pcap=\$${link}_pcap
        echo $desc pcap $link icmp from $pcap
        tcpdump -en -r $pcap vlan and icmp
    done
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

function setup_forch {
    # Wait for basic Faucet to startup.
    sleep 10

    cmd/forch local 1 2>&1 &

    # Need to wait long enough for polling mechanisms to kick in.
    sleep 20
}

function fetch_forch {
    name=$1
    args=$2
    sub=$3

    api=$name$args
    fname=$fout_dir/$name$sub.json

    curl http://localhost:9019/$api > $fname
    echo http://localhost:9019/$api > $fname.txt
    jq . $fname >> $fname.txt
    echo forch $name$sub results from $api
    cat $fname
    echo
}

function test_forch {
    fout_dir=$out_dir/forch$1
    mkdir -p $fout_dir

    echo Running forch$1 tests | tee -a $TEST_RESULTS

    # Make sure mac addresses are still learned...
    docker exec daq-faux-1 ping -q -c 3 192.168.1.2

    sleep 30.3231 &
    fetch_forch system_state
    fetch_forch dataplane_state
    fetch_forch switch_state '?switch=nz-kiwi-t2sw1&port=1' 1
    fetch_forch switch_state '?switch=nz-kiwi-t1sw2&port=10' 2
    fetch_forch cpn_state
    fetch_forch process_state
    fetch_forch list_hosts '' 1
    fetch_forch list_hosts ?eth_src=9a:02:57:1e:8f:01 2
    fetch_forch host_path '?eth_src=9a:02:57:1e:8f:01&eth_dst=9a:02:57:1e:8f:02' 1
    fetch_forch host_path '?eth_src=9a:02:57:1e:8f:01&to_egress=true' 2

    echo system_state | tee -a $TEST_RESULTS
    api_result=$fout_dir/system_state.json
    jq .site_name $api_result | tee -a $TEST_RESULTS
    jq .state_summary_change_count $api_result | tee -a $TEST_RESULTS
    jq .peer_controller_url $api_result | tee -a $TEST_RESULTS

    echo dataplane_state | tee -a $TEST_RESULTS
    api_result=$fout_dir/dataplane_state.json
    jq '.egress_state' $api_result | tee -a $TEST_RESULTS
    jq '.egress_state_change_count' $api_result | tee -a $TEST_RESULTS
    jq '.switches."nz-kiwi-t1sw2".switch_state' $api_result | tee -a $TEST_RESULTS
    jq '.stack_links."nz-kiwi-t1sw1:6@nz-kiwi-t1sw2:6".link_state' $api_result | tee -a $TEST_RESULTS

    echo switch_state | tee -a $TEST_RESULTS
    api_result=$fout_dir/switch_state1.json
    jq '.switches_state_change_count' $api_result | tee -a $TEST_RESULTS
    jq '.switches."nz-kiwi-t2sw1".root_path[1].switch' $api_result | tee -a $TEST_RESULTS
    jq '.switches."nz-kiwi-t2sw1".root_path[1].in' $api_result | tee -a $TEST_RESULTS
    jq '.switches."nz-kiwi-t2sw1".root_path[1].out' $api_result | tee -a $TEST_RESULTS
    jq '.switches."nz-kiwi-t2sw1".attributes.dp_id' $api_result | tee -a $TEST_RESULTS
    api_result=$fout_dir/switch_state2.json
    jq '.switches_state_detail' $api_result | tee -a $TEST_RESULTS
    jq '.switches."nz-kiwi-t1sw2".switch_state' $api_result | tee -a $TEST_RESULTS

    echo cpn_state | tee -a $TEST_RESULTS
    api_result=$fout_dir/cpn_state.json
    jq '.cpn_state_change_count' $api_result | tee -a $TEST_RESULTS
    for node in nz-kiwi-t1sw1 nz-kiwi-t2sw2; do
        jq ".cpn_nodes.\"$node\".attributes.cpn_ip" $api_result | tee -a $TEST_RESULTS
        jq ".cpn_nodes.\"$node\".state" $api_result | tee -a $TEST_RESULTS
    done

    echo process_state | tee -a $TEST_RESULTS
    api_result=$fout_dir/process_state.json
    jq .processes_state_change_count $api_result | tee -a $TEST_RESULTS
    jq .faucet.state $api_result | tee -a $TEST_RESULTS
    jq .sleep.state $api_result | tee -a $TEST_RESULTS
    jq .sleep.cmd_line $api_result | tee -a $TEST_RESULTS

    echo list_hosts | tee -a $TEST_RESULTS
    api_result=$fout_dir/list_hosts1.json
    jq '.eth_srcs."9a:02:57:1e:8f:01".url' $api_result | tee -a $TEST_RESULTS
    api_result=$fout_dir/list_hosts2.json
    jq '.eth_dsts."9a:02:57:1e:8f:02".url' $api_result | tee -a $TEST_RESULTS

    echo host_path | tee -a $TEST_RESULTS
    api_result=$fout_dir/host_path1.json
    jq .dst_ip $api_result | tee -a $TEST_RESULTS
    jq .path[1].switch $api_result | tee -a $TEST_RESULTS
    jq .path[1].out $api_result | tee -a $TEST_RESULTS
    api_result=$fout_dir/host_path2.json
    jq .src_ip $api_result | tee -a $TEST_RESULTS
    jq .path[1].switch $api_result | tee -a $TEST_RESULTS
    jq .path[1].egress $api_result | tee -a $TEST_RESULTS
}

if [ -z "$local" ]; then
    echo Base Stack Setup | tee -a $TEST_RESULTS
    bin/net_clean
    bin/setup_stack local || exit 1
else
    echo Restarting Faucet | tee -a $TEST_RESULTS
    docker restart daq-faucet-1
    docker exec daq-faux-1 ping -c 3 192.168.1.2
fi

setup_forch
controllers=`sudo ovs-vsctl get-controller t1sw2`

# Test that the 'local' mode of faucet is working properly.
echo 'print("supercalifragilisticexpialidocious")' > faucet/faucet/python_test.py
docker exec daq-faucet-1 python -m faucet.python_test 2>&1 | tee -a $TEST_RESULTS
rm faucet/faucet/python_test.py

echo Stacking Tests | tee -a $TEST_RESULTS
test_stack stack-solid
test_forch -pre

echo Bring t2sw3 down | tee -a $TEST_RESULTS
sudo ovs-vsctl del-controller t2sw3
ip link set t1sw1-eth9 down
test_stack stack-linkd

ip link set t1sw2-eth10 down
test_stack stack-twod
test_forch -twod

echo Bring t2sw3 up | tee -a $TEST_RESULTS
sudo ovs-vsctl set-controller t2sw3 $controllers
ip addr add 240.0.0.1/24 dev lo
ip link set t1sw1-eth6 down
ip link set t1sw1-eth11 down
test_stack stack-broken
test_forch -broke

ip addr del 240.0.0.1/24 dev lo
ip link set t1sw1-eth10 down
ip link set t1sw2-eth10 up
ip link set t1sw1-eth6 up
test_stack stack-restored
test_forch -post

echo Killing forch...
sudo kill `ps ax | fgrep forch | awk '{print $1}'`

#echo Dot1x setup >> $TEST_RESULTS
#bin/net_clean
#test_dot1x

echo Done with cleanup. Goodby.
