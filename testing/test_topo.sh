#!/bin/bash

source testing/test_preamble.sh

echo Topology Tests >> $TEST_RESULTS

# Create system.conf and startup file for arbitrary number of faux virtual devices.
function generate_system {
  echo source misc/system.conf > local/system.conf

  faux_num=$1

  topostartup=inst/startup_topo.cmd
  rm -f $topostartup
  echo startup_cmds=$topostartup >> local/system.conf

  echo sec_port=$((faux_num+1)) >> local/system.conf

  # Create required number of faux devices
  for iface in $(seq 1 $faux_num); do
      iface_names=${iface_names},faux-$iface
      echo autostart cmd/faux $iface discover >> $topostartup
  done
  echo intf_names=${iface_names#,} >> local/system.conf
  # Specify a different set of tests
  echo host_tests=misc/topo_tests.conf >> local/system.conf
}

echo DAQ topologies test | tee -a $TEST_RESULTS

minimal_device_traffic="tcpdump -en -r inst/run-port-01/scans/monitor.pcap port 47808"
minimal_device_bcast="$minimal_device_traffic and ether broadcast"
minimal_device_ucast="$minimal_device_traffic and ether dst 9a:02:57:1e:8f:02"
minimal_device_xcast="$minimal_device_traffic and ether host 9a:02:57:1e:8f:03"
minimal_cntrlr_traffic="tcpdump -en -r inst/run-port-02/scans/monitor.pcap port 47808"
minimal_cntrlr_bcast="$minimal_cntrlr_traffic and ether broadcast"
minimal_cntrlr_ucast="$minimal_cntrlr_traffic and ether dst 9a:02:57:1e:8f:01"
minimal_cntrlr_xcast="$minimal_cntrlr_traffic and ether host 9a:02:57:1e:8f:03"

function run_topo {
    type=$1
    devices=$2

    generate_system $devices
    cmd/run -s site_description=$type device_specs=misc/device_specs_topo_$type.json
}


    
    # For reference, faux devices MAC addresses are in the form 9a:02:57:1e:8f:XX
#    bcast=$(eval echo \$$type\_device_bcast | wc -l)
#    ucast=$(eval echo \$$type\_device_ucast | wc -l)
#    xcast=$(eval echo \$$type\_device_xcast | wc -l)
#    echo device $type $(($bcast > 2)) $(($ucast > 2)) $(($xcast > 0)) | tee -a $TEST_RESULTS
#    bcast=$(eval echo \$$type\_cntrlr_bcast | wc -l)
#    ucast=$(eval echo \$$type\_cntrlr_ucast | wc -l)
#    xcast=$(eval echo \$$type\_cntrlr_xcast | wc -l)
#    echo cntrlr $type $(($bcast > 2)) $(($ucast > 2)) $(($xcast > 0)) | tee -a $TEST_RESULTS
#}

# Run tests. The first option is the name of the test, the second one is the number of devices
#test_topo one 1
run_topo minimal 3
#test_topo minimal_commissioning 4
#test_topo complete 6
#test_topo headend 11
#test_topo two_groups 11

echo Done with tests | tee -a $TEST_RESULTS
