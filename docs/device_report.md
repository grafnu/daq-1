# Device 9a:02:57:1e:8f:01, *** Make *** *** Model ***

## Test Roles

|  Role  |      Name              | Status |
|--------|------------------------|--------|
|Operator| *** Operator Name *** |        |
|Approver| *** Approver Name *** |        |

## Test Iteration

| Test             |                        |
|------------------|------------------------|
| Test report start date | 2020-02-18 17:01:08+00:00 |
| Test report end date   | 2020-02-18 17:13:17+00:00 |
| DAQ version      | 1.0.1 |
| Attempt number   | 1 |

## Device Identification

| Device            | Entry              |
|-------------------|--------------------|
| Name              | *** Name *** |
| GUID              | *** GUID *** |
| MAC addr          | 9a:02:57:1e:8f:01 |
| Hostname          | *** Network Hostname *** |
| Type              | *** Type *** |
| Make              | *** Make *** |
| Model             | *** Model *** |
| Serial Number     | *** Serial *** |
| Firmware Version  | *** Firmware Version *** |

## Device Description

![Image of device](*** Device Image URL ***)

*** Device Description ***


### Device documentation

[Device datasheets](*** Device Datasheets URL ***)
[Device manuals](*** Device Manuals URL ***)

## Report summary

Overall device result FAIL

|Category|Result|
|---|---|
|Security|PASS|
|Other|1/2|
|Connectivity|n/a|

|Expectation|pass|fail|skip|info|gone|
|---|---|---|---|---|---|
|Required|1|0|1|0|0|
|Recommended|1|0|0|0|0|
|Other|8|9|13|4|2|

|Result|Test|Category|Expectation|Notes|
|---|---|---|---|---|
|skip|base.switch.ping|Other|Other||
|pass|base.target.ping|Connectivity|Required|target|
|skip|cloud.udmi.pointset|Other|Other|No device id.|
|info|communication.type.broadcast|Other|Other|Broadcast packets received.|
|pass|connection.dhcp_long|Other|Other|ARP packets received.|
|fail|connection.mac_oui|Other|Other||
|pass|connection.min_send|Other|Other|ARP packets received. Packets received.|
|skip|connection.port_duplex|Other|Other||
|skip|connection.port_link|Other|Other||
|skip|connection.port_speed|Other|Other||
|skip|network.brute|Security|Required||
|fail|network.ntp.support|Other|Other||
|skip|poe.negotiation|Other|Other||
|skip|poe.power|Other|Other||
|skip|poe.support|Other|Other||
|fail|protocol.app_min_send|Other|Other||
|skip|protocol.bacnet.pic|Other|Other|Bacnet device not found... Pics check cannot be performed.|
|skip|protocol.bacnet.version|Other|Other|Bacnet device not found.|
|skip|security.firmware|Other|Other|Could not retrieve a firmware version with nmap.|
|pass|security.ports.nmap|Security|Recommended||
|skip|security.tls.v3|Other|Other||
|skip|security.x509|Other|Other||
|gone|unknown.fake.llama|Other|Other||
|gone|unknown.fake.monkey|Other|Other||


## Module ping

```
Baseline ping test report
%% 84 packets captured.
RESULT skip base.switch.ping
RESULT pass base.target.ping target %% 10.20.11.164
```

## Module nmap

```
No invalid ports found.
RESULT pass security.ports.nmap
```

## Module brute

```
Target port 10000 not open.
RESULT skip network.brute
```

## Module discover

```
--------------------
security.firmware
--------------------
Automatic bacnet firmware scan using nmap
--------------------
PORT      STATE  SERVICE
47808/udp closed bacnet
MAC Address: 9A:02:57:1E:8F:01 (Unknown)
Firmware test complete
--------------------
RESULT skip security.firmware Could not retrieve a firmware version with nmap.
```

## Module network

```
--------------------
connection.dhcp_long
--------------------
Device sends ARP request on DHCP lease expiry.
--------------------
%% 17:02:24.764612 ARP, Request who-has daq-faux-1 tell 10.0.0.1, length 28
%% 17:02:24.764890 ARP, Request who-has 10.0.0.1 tell daq-faux-1, length 28
%% 17:02:24.764921 ARP, Reply daq-faux-1 is-at 9a:02:57:1e:8f:01 (oui Unknown), length 28
%% 17:02:24.764927 ARP, Reply 10.0.0.1 is-at 6e:c2:e0:c6:0f:73 (oui Unknown), length 28
%% 17:03:09.565854 ARP, Request who-has daq-faux-1 tell 10.0.0.1, length 28
%% 17:03:09.566036 ARP, Request who-has 10.0.0.1 tell daq-faux-1, length 28
%% 17:03:09.566083 ARP, Reply 10.0.0.1 is-at 6e:c2:e0:c6:0f:73 (oui Unknown), length 28
%% 17:03:09.566169 ARP, Reply daq-faux-1 is-at 9a:02:57:1e:8f:01 (oui Unknown), length 28
%% 17:06:53.820434 ARP, Request who-has 10.0.0.1 tell daq-faux-1, length 28
%% 17:06:53.820551 ARP, Reply 10.0.0.1 is-at 6e:c2:e0:c6:0f:73 (oui Unknown), length 28
%% packets_count=13
RESULT pass connection.dhcp_long ARP packets received.

--------------------
connection.min_send
--------------------
Device sends data at a frequency of less than 5 minutes.
--------------------
%% 17:02:24.764890 ARP, Request who-has 10.0.0.1 tell 10.20.11.164, length 28
%% 17:02:24.764921 ARP, Reply 10.20.11.164 is-at 9a:02:57:1e:8f:01, length 28
%% 17:02:28.601444 IP 10.20.11.164.33601 > 10.255.255.255.41794: UDP, length 32
%% 17:02:48.614797 IP 10.20.11.164.32984 > 10.255.255.255.41794: UDP, length 32
%% 17:03:04.519204 IP 10.20.11.164.68 > 10.0.0.1.67: BOOTP/DHCP, Request from 9a:02:57:1e:8f:01, length 300
%% 17:03:08.630924 IP 10.20.11.164.34079 > 10.255.255.255.41794: UDP, length 32
%% 17:03:09.566036 ARP, Request who-has 10.0.0.1 tell 10.20.11.164, length 28
%% 17:03:09.566169 ARP, Reply 10.20.11.164 is-at 9a:02:57:1e:8f:01, length 28
%% 17:03:28.649459 IP 10.20.11.164.43135 > 10.255.255.255.41794: UDP, length 32
%% 17:03:48.664036 IP 10.20.11.164.54183 > 10.255.255.255.41794: UDP, length 32
%% packets_count=13
RESULT pass connection.min_send ARP packets received. Packets received.

--------------------
communication.type.broadcast
--------------------
Device sends unicast or broadcast packets.
--------------------
RESULT info communication.type.broadcast Broadcast packets received. 
--------------------
protocol.app_min_send
--------------------
Device sends application packets at a frequency of less than 5 minutes.
--------------------
RESULT fail protocol.app_min_send 
--------------------
network.ntp.support
--------------------
Device sends NTP request packets.
--------------------
RESULT fail network.ntp.support 
--------------------
connection.dhcp_long
--------------------
Device sends ARP request on DHCP lease expiry.
--------------------
%% 17:02:24.764612 ARP, Request who-has daq-faux-1 tell 10.0.0.1, length 28
%% 17:02:24.764890 ARP, Request who-has 10.0.0.1 tell daq-faux-1, length 28
%% 17:02:24.764921 ARP, Reply daq-faux-1 is-at 9a:02:57:1e:8f:01 (oui Unknown), length 28
%% 17:02:24.764927 ARP, Reply 10.0.0.1 is-at 6e:c2:e0:c6:0f:73 (oui Unknown), length 28
%% 17:03:09.565854 ARP, Request who-has daq-faux-1 tell 10.0.0.1, length 28
%% 17:03:09.566036 ARP, Request who-has 10.0.0.1 tell daq-faux-1, length 28
%% 17:03:09.566083 ARP, Reply 10.0.0.1 is-at 6e:c2:e0:c6:0f:73 (oui Unknown), length 28
%% 17:03:09.566169 ARP, Reply daq-faux-1 is-at 9a:02:57:1e:8f:01 (oui Unknown), length 28
%% 17:06:53.820434 ARP, Request who-has 10.0.0.1 tell daq-faux-1, length 28
%% 17:06:53.820551 ARP, Reply 10.0.0.1 is-at 6e:c2:e0:c6:0f:73 (oui Unknown), length 28
%% packets_count=13
RESULT pass connection.dhcp_long ARP packets received.

--------------------
connection.min_send
--------------------
Device sends data at a frequency of less than 5 minutes.
--------------------
%% 17:02:24.764890 ARP, Request who-has 10.0.0.1 tell 10.20.11.164, length 28
%% 17:02:24.764921 ARP, Reply 10.20.11.164 is-at 9a:02:57:1e:8f:01, length 28
%% 17:02:28.601444 IP 10.20.11.164.33601 > 10.255.255.255.41794: UDP, length 32
%% 17:02:48.614797 IP 10.20.11.164.32984 > 10.255.255.255.41794: UDP, length 32
%% 17:03:04.519204 IP 10.20.11.164.68 > 10.0.0.1.67: BOOTP/DHCP, Request from 9a:02:57:1e:8f:01, length 300
%% 17:03:08.630924 IP 10.20.11.164.34079 > 10.255.255.255.41794: UDP, length 32
%% 17:03:09.566036 ARP, Request who-has 10.0.0.1 tell 10.20.11.164, length 28
%% 17:03:09.566169 ARP, Reply 10.20.11.164 is-at 9a:02:57:1e:8f:01, length 28
%% 17:03:28.649459 IP 10.20.11.164.43135 > 10.255.255.255.41794: UDP, length 32
%% 17:03:48.664036 IP 10.20.11.164.54183 > 10.255.255.255.41794: UDP, length 32
%% packets_count=13
RESULT pass connection.min_send ARP packets received. Packets received.

--------------------
communication.type.broadcast
--------------------
Device sends unicast or broadcast packets.
--------------------
RESULT info communication.type.broadcast Broadcast packets received. 
--------------------
protocol.app_min_send
--------------------
Device sends application packets at a frequency of less than 5 minutes.
--------------------
RESULT fail protocol.app_min_send 
--------------------
network.ntp.support
--------------------
Device sends NTP request packets.
--------------------
RESULT fail network.ntp.support 
```

## Module switch

```
LOCAL_IP not configured, assuming no network switch.
RESULT skip connection.port_link
RESULT skip connection.port_speed
RESULT skip connection.port_duplex
RESULT skip poe.power
RESULT skip poe.negotiation
RESULT skip poe.support
```

## Module macoui

```
Mac OUI Test
RESULT fail connection.mac_oui
```

## Module bacext

```
RESULT skip protocol.bacnet.version Bacnet device not found.
RESULT skip protocol.bacnet.pic Bacnet device not found... Pics check cannot be performed.
```

## Module tls

```
IOException unable to connect to server.
RESULT skip security.tls.v3
RESULT skip security.x509
```

## Module udmi

```
RESULT skip cloud.udmi.pointset No device id.
```

## Module network

```
--------------------
connection.dhcp_long
--------------------
Device sends ARP request on DHCP lease expiry.
--------------------
%% 17:02:24.764612 ARP, Request who-has daq-faux-1 tell 10.0.0.1, length 28
%% 17:02:24.764890 ARP, Request who-has 10.0.0.1 tell daq-faux-1, length 28
%% 17:02:24.764921 ARP, Reply daq-faux-1 is-at 9a:02:57:1e:8f:01 (oui Unknown), length 28
%% 17:02:24.764927 ARP, Reply 10.0.0.1 is-at 6e:c2:e0:c6:0f:73 (oui Unknown), length 28
%% 17:03:09.565854 ARP, Request who-has daq-faux-1 tell 10.0.0.1, length 28
%% 17:03:09.566036 ARP, Request who-has 10.0.0.1 tell daq-faux-1, length 28
%% 17:03:09.566083 ARP, Reply 10.0.0.1 is-at 6e:c2:e0:c6:0f:73 (oui Unknown), length 28
%% 17:03:09.566169 ARP, Reply daq-faux-1 is-at 9a:02:57:1e:8f:01 (oui Unknown), length 28
%% 17:06:53.820434 ARP, Request who-has 10.0.0.1 tell daq-faux-1, length 28
%% 17:06:53.820551 ARP, Reply 10.0.0.1 is-at 6e:c2:e0:c6:0f:73 (oui Unknown), length 28
%% packets_count=13
RESULT pass connection.dhcp_long ARP packets received.

--------------------
connection.min_send
--------------------
Device sends data at a frequency of less than 5 minutes.
--------------------
%% 17:02:24.764890 ARP, Request who-has 10.0.0.1 tell 10.20.11.164, length 28
%% 17:02:24.764921 ARP, Reply 10.20.11.164 is-at 9a:02:57:1e:8f:01, length 28
%% 17:02:28.601444 IP 10.20.11.164.33601 > 10.255.255.255.41794: UDP, length 32
%% 17:02:48.614797 IP 10.20.11.164.32984 > 10.255.255.255.41794: UDP, length 32
%% 17:03:04.519204 IP 10.20.11.164.68 > 10.0.0.1.67: BOOTP/DHCP, Request from 9a:02:57:1e:8f:01, length 300
%% 17:03:08.630924 IP 10.20.11.164.34079 > 10.255.255.255.41794: UDP, length 32
%% 17:03:09.566036 ARP, Request who-has 10.0.0.1 tell 10.20.11.164, length 28
%% 17:03:09.566169 ARP, Reply 10.20.11.164 is-at 9a:02:57:1e:8f:01, length 28
%% 17:03:28.649459 IP 10.20.11.164.43135 > 10.255.255.255.41794: UDP, length 32
%% 17:03:48.664036 IP 10.20.11.164.54183 > 10.255.255.255.41794: UDP, length 32
%% packets_count=13
RESULT pass connection.min_send ARP packets received. Packets received.

--------------------
communication.type.broadcast
--------------------
Device sends unicast or broadcast packets.
--------------------
RESULT info communication.type.broadcast Broadcast packets received. 
--------------------
protocol.app_min_send
--------------------
Device sends application packets at a frequency of less than 5 minutes.
--------------------
RESULT fail protocol.app_min_send 
--------------------
network.ntp.support
--------------------
Device sends NTP request packets.
--------------------
RESULT fail network.ntp.support 
--------------------
connection.dhcp_long
--------------------
Device sends ARP request on DHCP lease expiry.
--------------------
%% 17:02:24.764612 ARP, Request who-has daq-faux-1 tell 10.0.0.1, length 28
%% 17:02:24.764890 ARP, Request who-has 10.0.0.1 tell daq-faux-1, length 28
%% 17:02:24.764921 ARP, Reply daq-faux-1 is-at 9a:02:57:1e:8f:01 (oui Unknown), length 28
%% 17:02:24.764927 ARP, Reply 10.0.0.1 is-at 6e:c2:e0:c6:0f:73 (oui Unknown), length 28
%% 17:03:09.565854 ARP, Request who-has daq-faux-1 tell 10.0.0.1, length 28
%% 17:03:09.566036 ARP, Request who-has 10.0.0.1 tell daq-faux-1, length 28
%% 17:03:09.566083 ARP, Reply 10.0.0.1 is-at 6e:c2:e0:c6:0f:73 (oui Unknown), length 28
%% 17:03:09.566169 ARP, Reply daq-faux-1 is-at 9a:02:57:1e:8f:01 (oui Unknown), length 28
%% 17:06:53.820434 ARP, Request who-has 10.0.0.1 tell daq-faux-1, length 28
%% 17:06:53.820551 ARP, Reply 10.0.0.1 is-at 6e:c2:e0:c6:0f:73 (oui Unknown), length 28
%% packets_count=13
RESULT pass connection.dhcp_long ARP packets received.

--------------------
connection.min_send
--------------------
Device sends data at a frequency of less than 5 minutes.
--------------------
%% 17:02:24.764890 ARP, Request who-has 10.0.0.1 tell 10.20.11.164, length 28
%% 17:02:24.764921 ARP, Reply 10.20.11.164 is-at 9a:02:57:1e:8f:01, length 28
%% 17:02:28.601444 IP 10.20.11.164.33601 > 10.255.255.255.41794: UDP, length 32
%% 17:02:48.614797 IP 10.20.11.164.32984 > 10.255.255.255.41794: UDP, length 32
%% 17:03:04.519204 IP 10.20.11.164.68 > 10.0.0.1.67: BOOTP/DHCP, Request from 9a:02:57:1e:8f:01, length 300
%% 17:03:08.630924 IP 10.20.11.164.34079 > 10.255.255.255.41794: UDP, length 32
%% 17:03:09.566036 ARP, Request who-has 10.0.0.1 tell 10.20.11.164, length 28
%% 17:03:09.566169 ARP, Reply 10.20.11.164 is-at 9a:02:57:1e:8f:01, length 28
%% 17:03:28.649459 IP 10.20.11.164.43135 > 10.255.255.255.41794: UDP, length 32
%% 17:03:48.664036 IP 10.20.11.164.54183 > 10.255.255.255.41794: UDP, length 32
%% packets_count=13
RESULT pass connection.min_send ARP packets received. Packets received.

--------------------
communication.type.broadcast
--------------------
Device sends unicast or broadcast packets.
--------------------
RESULT info communication.type.broadcast Broadcast packets received. 
--------------------
protocol.app_min_send
--------------------
Device sends application packets at a frequency of less than 5 minutes.
--------------------
RESULT fail protocol.app_min_send 
--------------------
network.ntp.support
--------------------
Device sends NTP request packets.
--------------------
RESULT fail network.ntp.support 
```

## Report complete

