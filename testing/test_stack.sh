#!/bin/bash

source testing/test_preamble.sh

out_base=/tmp/daq-test_stack

echo Stacking Tests >> $TEST_RESULTS

bin/setup_stack

ovs-vsctl show | tee -a $TEST_RESULTS

echo Waiting for stack to settle | tee -a $TEST_RESULTS
sleep 30

function test_pair {
    src=$1
    dst=$2
    
    out_file=$out_base-$src-$dst
    host=daq-faux-$src
    cmd="ping -c 10 192.168.0.$dst"
    echo $host: $cmd
    echo -n $host: $cmd\ > $out_file
    docker exec -ti $host $cmd | fgrep time= | wc -l >> $out_file &
}

rm -f $out_base-*
test_pair 1 2
test_pair 1 3
test_pair 2 1
test_pair 2 3
test_pair 3 1
test_pair 3 2

wait

cat $out_base-* | tee -a $TEST_RESULTS

echo Done with stack test. | tee -a $TEST_RESULTS
