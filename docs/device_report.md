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
|skip|base.switch.ping||	
|pass|base.target.ping|target |
|skip|connection.port_duplex||
|skip|connection.port_link||
|skip|connection.port_speed||
|skip|poe.negotiation||
|skip|poe.power||
|skip|poe.support||
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
RESULT fail protocol.bacnet.version
```

## Module tls

```
IOException unable to connect to server.
RESULT skip security.tls.v3
RESULT skip security.x509
```

## Report complete

