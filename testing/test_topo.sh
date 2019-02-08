#!/bin/bash

source testing/test_preamble.sh

echo Topology Tests >> $TEST_RESULTS

# Create system.conf and startup file for arbitrary number of faux virtual devices.
function generate {
  echo source misc/system.conf > local/system.conf

  type=$1
  faux_num=$2

  echo Running $type $faux_num | tee -a $TEST_RESULTS

  # Clean out in case there's an error
  rm -rf inst/run-port-*  inst/runtime_conf/

  topostartup=inst/startup_topo.cmd
  rm -f $topostartup
  echo startup_cmds=$topostartup >> local/system.conf

  echo sec_port=$((faux_num+1)) >> local/system.conf

  # Create required number of faux devices
  for iface in $(seq 1 $faux_num); do
      iface_names=${iface_names},faux-$iface
      echo autostart cmd/faux $iface discover telnet >> $topostartup
  done
  echo intf_names=${iface_names#,} >> local/system.conf

  # Specify a different set of tests
  echo host_tests=misc/topo_tests.conf >> local/system.conf

  echo site_description=\"$type with $devices devices\" >> local/system.conf
  echo device_specs=misc/device_specs_topo_$type.json >> local/system.conf
  echo test_config=inst/runtime_conf/ >> local/system.conf
}

MAC_BASE=9a:02:57:1e:8f

function check_bacnet {
    at_dev=$(printf %02d $1)
    ex_dev=$(printf %02d $2)

    at_mac=$MAC_BASE:$(printf %02x $at_dev)
    ex_mac=$MAC_BASE:$(printf %02x $ex_dev)

    conf_dir=inst/runtime_conf/port-$(printf %02d $from_dev)
    mkdir -p $conf_dir
    cmd_file=$conf_dir/ping_runtime.sh

    iface=$(hostname)-eth0
    tcp_base="tcpdump -en -r eth0.pcap port 47808"
    out_file=/tmp/bacnet_result.txt

    cat >> $cmd_file <<EOF
    test -f eth0.pcap || timeout 20 tcpdump -eni $iface -w eth0.pcap || true
    echo -n ucast_to $ex_dev >> $out_file
    $tcp_base and ether dst $ex_mac | wc -l >> $out_file

    echo -n ucast_to $ex_dev >> $out_file
    $tcp_base and not ether src $at_mac and not ether dst $at_mac | wc -l >> $out_file

    echo -n ucast_to $ex_dev >> /tmp/nc_result.txt >> $out_file
    $tcp_base and ether broadcast and ether src $at_mac | wc -l >> $out_file
EOF
}

function check_tcp {
    from_dev=$1
    to_dev=$2
    port=$3

    to_host=daq-faux-$to_dev

    conf_dir=inst/runtime_conf/port-$(printf %02d $from_dev)
    mkdir -p $conf_dir
    echo "timeout 10 nc $to_host $port 2>&1 || echo Fail $to_host:$port >> /tmp/nc_result.txt" >> $conf_dir/ping_runtime.sh
}

function run_test {
    cmd/run -s
    more inst/run-port-*/nodes/ping*/tmp/nc_result.txt | tee -a $TEST_RESULTS
    more inst/run-port-*/nodes/ping*/tmp/bacnet_result.txt | tee -a $TEST_RESULTS
}

generate open 3
check_tcp 1 2 23
check_bacnet 1 2
check_bacnet 2 3
check_bacnet 3 1
run_test

generate minimal 3
check_tcp 1 2 23
check_bacnet 1 2
check_bacnet 2 3
check_bacnet 3 1
run_test

echo Done with tests | tee -a $TEST_RESULTS
