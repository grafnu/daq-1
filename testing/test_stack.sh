#!/bin/bash

source testing/test_preamble.sh

echo Stacking Tests >> $TEST_RESULTS

bin/setup_stack

ovs-vsctl show | tee -a $TEST_RESULTS

echo Waiting for stack to settle | tee -a $TEST_RESULTS
sleep 30

function test_pair {
    src=$1
    dst=$2
    
    results=$(docker exec -ti daq-faux-$src ping -c 10 192.168.0.$dst | fgrep time= | wc -l)
    echo Ping from $src $dst returned $results | tee -a $TEST_RESULTS
}

test_pair 1 2
test_pair 1 3
test_pair 2 1
test_pair 2 3
test_pair 3 1
test_pair 3 2
