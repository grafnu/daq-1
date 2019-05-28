#!/bin/bash

source testing/test_preamble.sh

# bacext testing
echo Running bacext test... | tee -a $TEST_RESULTS

cp misc/host_tests.conf local/local_tests.conf
cat <<EOF >>local/local_tests.conf
include subset/bacnet/build.conf
EOF

cp misc/system_base.conf local/system.conf
cat <<EOF >>local/system.conf
startup_faux_opts=bacnet
host_tests=local/local_tests.conf
EOF
cat <<EOF > local/site/module_config.json
{
  "modules": {
    "bacext": {
      "enabled": true
    }
  }
}
EOF
cmd/run -s -b
cat inst/run-port-01/nodes/bacext01/tmp/report.txt | tee -a $TEST_RESULTS

