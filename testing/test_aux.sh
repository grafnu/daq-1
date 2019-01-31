#!/bin/bash

if [ `whoami` != 'root' ]; then
    echo Need to run as root.
    exit -1
fi

TEST_RESULTS=${TEST_RESULTS:-/tmp/test_results.out}
echo Results in $TEST_RESULTS
echo Aux Tests > $TEST_RESULTS

echo Generator tests | tee -a $TEST_RESULTS
rm -rf topology/out
normalize_base=topology/uk-lon-wts/uk-lon-wts-ctl0g-1-1/
bin/generate_topology raw_topo=$normalize_base topo_dir=topology/out/normalized
diff -r $normalize_base topology/out/normalized | tee -a $TEST_RESULTS
bin/generate_topology site_config=topology/uk-lon-wts/site_config.json topo_dir=topology/out/generated
diff -r topology/uk-lon-wts/ topology/out/generated | tee -a $TEST_RESULTS

echo mudacl tests | tee -a $TEST_RESULTS
mudacl/bin/test.sh
echo Mudacl exit code $? | tee -a $TEST_RESULTS
validator/bin/test.sh
echo Validator exit code $? | tee -a $TEST_RESULTS

# Runs lint checks and some similar things
echo Lint checks | tee -a $TEST_RESULTS
cmd/inbuild skip
echo cmd/inbuild exit code $? | tee -a $TEST_RESULTS

echo Extended tests | tee -a $TEST_RESULTS
cp misc/system_multi.conf local/system.conf
DAQ_FAUX1_OPTS=brute DAQ_FAUX2_OPTS=nobrute cmd/run -s
tail -qn 1 inst/run-port-*/nodes/brute*/tmp/report.txt | tee -a $TEST_RESULTS
sort inst/result.log | tee -a $TEST_RESULTS
more cat inst/run-port-*/nodes/brute*/activate.log

echo Done with tests | tee -a $TEST_RESULTS
