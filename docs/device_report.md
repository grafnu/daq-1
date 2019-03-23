# DAQ scan report for device 9a02571e8f01
Started 2019-03-23 00:16:34+00:00

Operator: <operator_name>
Reviewer: <reviewer_name>
Approver: <approver_name>

Test report date:     <test_timestamp>
Test report revision: <revision_number>

## Device identification

Device name:         <device_name>
Device GUID:         <device_guid>
Device MAC address:  <mac_address>
Device hostname:     <hostname>
Device type:         <device_type>
Device manufacturer: <manufacturer>
Device model:        <model>
Serial number:       <serial_number>
Firmware version:    <firmware_version>

## Device description

Free text including description of device and links to more information (datasheets, manuals, installation notes, etc.)

## Test priorities

| Test Name | Priority |
|-----------|----------|
| category1.test1 | REQUIRED |
| category1.test2 | RECOMMENDED |
| category2.test1 | REQUIRED |

## Report summary

skip base.switch.ping
pass base.target.ping target
pass security.ports.nmap

## Module ping

Baseline ping test report
# 133 packets captured.
RESULT skip base.switch.ping
RESULT pass base.target.ping target # 10.20.44.38

## Module nmap

No open ports found.
RESULT pass security.ports.nmap

## Module brute

Target port 10000 not open.

## Module switch

LOCAL_IP not configured, assuming no network switch.

## Report complete

