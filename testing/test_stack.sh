#!/bin/bash

source testing/test_preamble.sh

out_dir=/tmp/daq-test_stack
rm -rf $out_dir

pcap_file=$out_dir/t2sw1-eth6.pcap
nodes_dir=$out_dir/nodes

mkdir -p $out_dir $nodes_dir

setup_delay=60

echo Stacking Tests >> $TEST_RESULTS

bin/setup_stack || exit 1

echo Configured bridges:
bridges=$(ovs-vsctl list-br | sort)
for bridge in $bridges; do
    echo
    echo OVS bridge $bridge | tee -a $TEST_RESULTS
    ovs-ofctl show $bridge | sed -e 's/ addr:.*//' | tee -a $TEST_RESULTS
done

echo
echo Waiting $setup_delay sec for stack to settle | tee -a $TEST_RESULTS
sleep $setup_delay

function test_pair {
    src=$1
    dst=$2

    host=daq-faux-$src
    out_file=$nodes_dir/$host-$dst
    cmd="ping -c 10 192.168.0.$dst"
    echo $host: $cmd
    echo -n $host: $cmd\ > $out_file
    docker exec -ti $host $cmd | fgrep time= | wc -l >> $out_file &
}

echo Capturing pcap to $pcap_file for 20 seconds...
timeout 20 tcpdump -eni t2sw1-eth6 -w $pcap_file &
sleep 1

test_pair 1 2
test_pair 1 3
test_pair 2 1
test_pair 2 3
test_pair 3 1
test_pair 3 2

echo Waiting for pair tests to complete...
wait

bcount=$(tcpdump -en -r $pcap_file | wc -l)
echo pcap count is $bcount
echo pcap sane $((bcount > 10)) $((bcount < 1000)) | tee -a $TEST_RESULTS

cat $nodes_dir/* | tee -a $TEST_RESULTS

echo Faucet logs
more inst/faucet/*/faucet.log
echo nz-kiwi-ctl1
docker logs nz-kiwi-ctl1 | tail
echo nz-kiwi-ctl2
docker logs nz-kiwi-ctl2 | tail

echo Done with stack test. | tee -a $TEST_RESULTS

echo Cleanup bridges...
sudo ovs-vsctl del-br corp
sudo ovs-vsctl del-br t1sw1
sudo ovs-vsctl del-br t1sw2
sudo ovs-vsctl del-br t2sw1
sudo ovs-vsctl del-br t2sw2

echo Done with cleanup. Goodby.
