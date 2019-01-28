#!/bin/bash

if [ `whoami` != 'root' ]; then
    echo Need to run as root.
    exit -1
fi

TEST_RESULTS=test_results.out
rm -f $TEST_RESULTS

echo Writing test results to $TEST_RESULTS

cmdrun="cmd/run"

# Test various configurations of mud files.

echo Mud profile tests | tee -a $TEST_RESULTS
cp misc/system_muddy.conf local/system.conf

device_traffic="tcpdump -en -r inst/run-port-01/scans/monitor.pcap port 47808"
device_bcast="$device_traffic and ether broadcast"
device_ucast="$device_traffic and ether dst 9a:02:57:1e:8f:02"
device_xcast="$device_traffic and ether host 9a:02:57:1e:8f:03"
cntrlr_traffic="tcpdump -en -r inst/run-port-02/scans/monitor.pcap port 47808"
cntrlr_bcast="$cntrlr_traffic and ether broadcast"
cntrlr_ucast="$cntrlr_traffic and ether dst 9a:02:57:1e:8f:01"
cntrlr_xcast="$cntrlr_traffic and ether host 9a:02:57:1e:8f:03"

function test_mud {
    type=$1
    $cmdrun -s device_specs=misc/device_specs_bacnet_$type.json
    bcast=$($device_bcast | wc -l)
    ucast=$($device_ucast | wc -l)
    xcast=$($device_xcast | wc -l)
    echo device $type $(($bcast > 2)) $(($ucast > 2)) $(($xcast > 0)) | tee -a $TEST_RESULTS
    bcast=$($cntrlr_bcast | wc -l)
    ucast=$($cntrlr_ucast | wc -l)
    xcast=$($cntrlr_xcast | wc -l)
    echo cntrlr $type $(($bcast > 2)) $(($ucast > 2)) $(($xcast > 0)) | tee -a $TEST_RESULTS
}

test_mud base

echo Done with tests | tee -a $TEST_RESULTS
