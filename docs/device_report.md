# DAQ scan report for device 9a02571e8f01
Started %% 2019-06-06 14:00:35+00:00

|  Role  |      Name              | Status |
|--------|------------------------|--------|
|Operator| *** Operator Name *** |        |
|Approver| *** Approver Name *** |        |

| Test iteration   |                        |
|------------------|------------------------|
| Test report date | 2019-06-06T14:00:34.975Z |
| DAQ version      | 0.9.7 |
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

|Result|Test|Notes|
|---|---|---|
|pass|base.target.ping|target |
|pass|connection.port_duplex||
|pass|connection.port_link||
|pass|connection.port_speed||
|fail|poe.negotiation||
|fail|poe.power||
|fail|poe.support||
|fail|connection.mac_oui||
|fail|network.brute||
|fail|protocol.bacnet.version||
|pass|security.ports.nmap||
|skip|security.tls.v3||
|skip|security.x509||

## Module ping

```
Baseline ping test report
%% 82 packets captured.
RESULT skip base.switch.ping
RESULT pass base.target.ping target %% 10.20.6.38
```

## Module nmap

```
Open port 443 open tcp https
RESULT fail security.ports.nmap
```

## Module brute

```
Target port 10000 not open.
RESULT skip network.brute
```

## Module switch

```
show interface:
Link is UP
administrative state is UP
current duplex full
current speed 100
current polarity mdi
configured duplex auto
configured speed auto
configured polarity auto
<UP,BROADCAST,RUNNING,MULTICAST>
input packets 18768
bytes 1729761
dropped 0
multicast packets 624
output packets 61753
multicast packets 30610
input average rate : 30 seconds 19.82 Kbps, 5 minutes 3.44 Kbps
output average rate: 30 seconds 22.70 Kbps, 5 minutes 4.80 Kbps
input peak rate 192.11 Kbps at 2019/05/02 08:15:58
Time since last state change: 0 days 19:18:35

show platform:
enabled:                1
loopback:               0
link:                   1
speed:                100  
max speed:               1000
duplex:                 1
linkscan:               2
autonegotiate:          1
master:                 2
tx pause:               0  
rx pause:                   0
untagged vlan:       4090
vlan filter:            1
stp state:              4
learn:                  5
discard:                0
jam:                    0
max frame size:      1482
MC Disable SA:         no
MC Disable TTL:        no
MC egress untag:        0
MC egress vid:          1
MC TTL threshold:      -1

show power-inline:
Interface:
Admin:
Pri:
Oper:
Power:(mW
Device:
Class:
Max:  (mW)

RESULT pass connection.port_link
RESULT pass connection.port_speed
RESULT pass connection.port_duplex
RESULT fail poe.power
RESULT fail poe.negotiation
RESULT fail poe.support
```

## Module macoui

```
Mac OUI Test
RESULT fail connection.mac_oui
```

## Module bacext

```
RESULT fail protocol.bacnet.version
```

## Module tls

```
IOException unable to connect to server.
RESULT skip security.tls.v3
RESULT skip security.x509
```

## Report complete

