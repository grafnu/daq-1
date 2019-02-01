#!/bin/bash

source testing/test_preamble.sh

echo Base Many >> $TEST_RESULTS

echo source misc/system.conf > local/system.conf

manystartup=inst/startup_many.cmd
rm -f $manystartup
echo startup_cmds=$manystartup >> local/system.conf

echo sec_port=10 >> local/system.conf

ifaces=
for iface in 1 2 3 4 5 6 7 8 9; do
    ifaces=${ifaces},faux-$iface
    echo autostart cmd/faux $iface >> $manystartup
done
echo intf_names=${ifaces#,} >> local/system.conf

echo DAQ stress test | tee $TEST_RESULTS
cmd/run run_limit=40
cat inst/result.log
results=$(fgrep [] inst/result.log | wc -l)
echo Found $results successful runs.
echo Enough results $(($results >= 40)) | tee -a $TEST_RESULTS

echo Done with tests | tee -a $TEST_RESULTS
