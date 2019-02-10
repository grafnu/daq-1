#!/bin/bash

source testing/test_preamble.sh

echo Topology Tests >> $TEST_RESULTS

bacnet_file=/tmp/bacnet_result.txt
socket_file=/tmp/socket_result.txt

# Create system.conf and startup file for arbitrary number of faux virtual devices.
function generate {
  echo source misc/system.conf > local/system.conf

  type=$1
  faux_num=$2

  echo Running $type $faux_num | tee -a $TEST_RESULTS

  # Clean out in case there's an error
  rm -rf inst/run-port-*
  rm -rf inst/runtime_conf

  topostartup=inst/startup_topo.cmd
  rm -f $topostartup
  echo startup_cmds=$topostartup >> local/system.conf

  echo sec_port=$((faux_num+1)) >> local/system.conf

  # Create required number of faux devices
  iface_names=
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
  echo monitor_scan_sec=0 >> local/system.conf
}

MAC_BASE=9a:02:57:1e:8f

function check_bacnet {
    at_dev=$(printf %02d $1)
    ex_dev=$(printf %02d $2)
    shift
    shift
    expected="$*"

    at_mac=$MAC_BASE:$(printf %02x $at_dev)
    ex_mac=$MAC_BASE:$(printf %02x $ex_dev)

    conf_dir=inst/runtime_conf/port-$at_dev
    mkdir -p $conf_dir
    cmd_file=$conf_dir/ping_runtime.sh

    tcp_base="tcpdump -en -r /tmp/eth0.pcap port 47808"

    if [ ! -f $conf_dir/.test_bacnet ]; then
        touch $conf_dir/.test_bacnet
        cat >> $cmd_file <<EOF
sleep 30 # For startup race-conditions.
timeout 30 tcpdump -eni \$HOSTNAME-eth0 -w /tmp/eth0.pcap || true
function testit {
    echo \$((\$($tcp_base and \$@ | wc -l ) > 0))
}
EOF
    fi

    cat >> $cmd_file <<EOF
ether_dst=\$(testit ether dst $ex_mac)
ether_not=\$(testit not ether src $at_mac and not ether dst $at_mac)
bcast_src=\$(testit ether broadcast and ether src $at_mac)
bcast_oth=\$(testit ether broadcast and not ether src $at_mac)
result="\$ether_dst \$ether_not \$bcast_src \$bcast_oth"
echo check_bacnet $at_dev $ex_dev \$result | tee -a $bacnet_file
[ -z "$expected" -o "$expected" == "\$result" ] || (echo \$result != $expected && false)
EOF
}

function check_socket {
    from_dev=$1
    to_dev=$2
    shift
    shift
    expected="$*"

    to_host=daq-faux-$(printf %d $to_dev)

    conf_dir=inst/runtime_conf/port-$(printf %02d $from_dev)
    mkdir -p $conf_dir
    cmd_file=$conf_dir/ping_runtime.sh

    if [ ! -f $conf_dir/.test_socket ]; then
        touch $conf_dir/.test_socket
        cat >> $cmd_file <<EOF
function tcp_port {
    (timeout 10 nc $to_host \$1 2>/dev/null || echo Failed-$to_dev) | xargs
}
EOF
    fi

    cat >> $cmd_file <<EOF
telnet=\$(tcp_port 23)
https=\$(tcp_port 443)
result="\$telnet \$https"
echo check_socket $from_dev $to_dev \$result >> $socket_file
[ -z "$expected" -o "$expected" == "\$result" ] || (echo \$result != $expected && false)
EOF
}

function run_test {
    # Hack for race condition to make sure all actual is testing before this one completes.
    for conf in $(find inst/runtime_conf -name ping_runtime.sh); do
        echo sleep 60 >> $conf
    done
    cmd/run -s
    fgrep :ping: inst/result.log | tee -a $TEST_RESULTS
    cat inst/run-port-*/nodes/ping*${socket_file} | tee -a $TEST_RESULTS
    cat inst/run-port-*/nodes/ping*${bacnet_file} | tee -a $TEST_RESULTS
    more inst/run-port-*/nodes/ping*/activate.log
}

generate open 3
check_socket 01 02 daq-faux-2 Failed-02
check_bacnet 01 02 1 1 1 1
check_bacnet 02 03 1 1 1 1
check_bacnet 03 01 1 1 1 1
run_test

generate minimal 3
check_socket 01 02 Failed-02 Failed-02
check_bacnet 01 02 1 1 1 1
check_bacnet 02 03 0 1 1 1
check_bacnet 03 01 1 1 1 1
run_test

generate commissioning 4
check_bacnet 01 02 1 1 1 1
check_bacnet 01 04 0 1 1 1
check_bacnet 02 03 0 1 1 1
check_bacnet 02 04 0 1 1 1
check_bacnet 03 01 1 1 1 1
run_test

echo Done with tests | tee -a $TEST_RESULTS
