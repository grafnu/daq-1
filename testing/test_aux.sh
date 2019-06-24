#!/bin/bash

source testing/test_preamble.sh

echo Aux Tests >> $TEST_RESULTS

echo mudacl tests | tee -a $TEST_RESULTS
mudacl/bin/test.sh
echo Mudacl exit code $? | tee -a $TEST_RESULTS
validator/bin/test_schema
echo Validator exit code $? | tee -a $TEST_RESULTS

# Runs lint checks and some similar things
echo Lint checks | tee -a $TEST_RESULTS
cmd/inbuild skip
echo cmd/inbuild exit code $? | tee -a $TEST_RESULTS
docker logs daq-runner

rm -rf inst/test_site && mkdir -p inst/test_site
cp -a misc/test_site inst/

echo Extended tests | tee -a $TEST_RESULTS
cp misc/system_all.conf local/system.conf
cat <<EOF >> local/system.conf
fail_hook=misc/dump_network.sh
test_config=misc/runtime_configs/long_wait
site_path=inst/test_site
startup_faux_1_opts="brute"
startup_faux_2_opts="nobrute expiredtls pubber"
startup_faux_3_opts="tls macoui bacnet pubber"
EOF

if [ -n "$GCP_SERVICE_ACCOUNT" ]; then
    echo Installing GCP_SERVICE_ACCOUNT to gcp_cred=local/gcp_service_account.json
    echo "$GCP_SERVICE_ACCOUNT" > local/gcp_service_account.json
    echo gcp_cred=local/gcp_service_account.json >> local/system.conf
else
    echo No GCP_SERVICE_ACCOUNT cred defined.
    echo This varaiable should be defined in your online travis config.
fi

mkdir -p inst/faux/daq-faux-2/local
cp misc/test_site/devices/AHU-1/rsa_private.pkcs8 inst/faux/daq-faux-2/local/
cat <<EOF > inst/faux/daq-faux-2/local/pubber.json
{
  "projectId": "bos-daq-testing",
  "cloudRegion": "us-central1",
  "registryId": "registrar_test",
  "extraField": "fail",
  "gatewayId": "AHU-1"
}
EOF

cmd/run -b -s
tail -qn 1 inst/run-port-*/nodes/bacext*/tmp/report.txt | tee -a $TEST_RESULTS
tail -qn 1 inst/run-port-*/nodes/brute*/tmp/report.txt | tee -a $TEST_RESULTS
tail -qn 1 inst/run-port-*/nodes/macoui*/tmp/report.txt | tee -a $TEST_RESULTS
fgrep -h RESULT inst/run-port-*/nodes/tls*/tmp/report.txt | tee -a $TEST_RESULTS
more inst/run-port-*/scans/dhcp_triggers.txt | cat
dhcp_short=$(fgrep pass inst/run-port-01/scans/dhcp_triggers.txt | wc -l)
dhcp_long=$(fgrep long inst/run-port-01/scans/dhcp_triggers.txt | wc -l)
echo dhcp requests $dhcp_short $dhcp_long | tee -a $TEST_RESULTS
sort inst/result.log | tee -a $TEST_RESULTS
more inst/run-port-*/nodes/ping*/activate.log | cat
more inst/run-port-*/nodes/nmap*/activate.log | cat
more inst/run-port-*/nodes/brute*/activate.log | cat
more inst/run-port-*/nodes/macoui*/activate.log | cat
more inst/run-port-*/nodes/tls*/activate.log | cat
ls inst/fail_fail01/ | tee -a $TEST_RESULTS
echo port-01 module_config modules | tee -a $TEST_RESULTS
jq .modules inst/run-port-01/nodes/ping01/tmp/module_config.json | tee -a $TEST_RESULTS
echo port-02 module_config modules | tee -a $TEST_RESULTS
jq .modules inst/run-port-02/nodes/ping02/tmp/module_config.json | tee -a $TEST_RESULTS
cat inst/run-port-02/nodes/ping02/tmp/snake.txt | tee -a $TEST_RESULTS
cat inst/run-port-02/nodes/ping02/tmp/lizard.txt | tee -a $TEST_RESULTS

more inst/run-port-*/nodes/udmi*/activate.log | cat
more inst/run-port-*/nodes/udmi*/validation.log | cat

for num in 1 2 3; do
    echo docker logs daq-faux-$num
    docker logs daq-faux-$num | head -n 100
done
echo done with docker logs

function redact {
    sed -e 's/\s*%%.*//' \
        -e 's/2019-.*T.*Z/XXX/' \
        -e 's/2019-.*00:00/XXX/' \
        -e 's/DAQ version.*//'
}

cat docs/device_report.md | redact > out/redacted_docs.md
cat inst/reports/report_9a02571e8f01_*.md | redact > out/redacted_file.md

echo Redacted docs diff | tee -a $TEST_RESULTS
(diff out/redacted_docs.md out/redacted_file.md && echo No report diff) | tee -a $TEST_RESULTS

# Make sure there's no file pollution from the test run.
git status --porcelain | tee -a $TEST_RESULTS

echo Done with tests | tee -a $TEST_RESULTS
