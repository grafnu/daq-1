# Device 9a02571e8f01, 2019-07-15 09:23:01+00:00 to 2019-07-15 09:30:23+00:00

|  Role  |      Name              | Status |
|--------|------------------------|--------|
|Operator| *** Operator Name *** |        |
|Approver| *** Approver Name *** |        |

| Test iteration   |                        |
|------------------|------------------------|
| Test report date | 2019-07-15T09:23:01.382Z |
| DAQ version      | 1.0.0 |
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
|fail|connection.mac_oui||
|fail|network.brute||
|skip|protocol.bacnet.pic||
|skip|protocol.bacnet.version||
|pass|security.ports.nmap||
|skip|security.tls.v3||
|skip|security.x509||

## Module ping

```
Baseline ping test report
%% 74 packets captured.
RESULT skip base.switch.ping
RESULT pass base.target.ping target %% 10.20.20.164
```

## Module nmap

```
Allowing 10000 open tcp snet-sensor-mgmt
No invalid ports found.
RESULT pass security.ports.nmap
```

## Module brute

```
Username:manager
Password:friend
Login success!
RESULT fail network.brute
```

## Module switch

```
LOCAL_IP not configured, assuming no network switch.
```

## Module macoui

```
Mac OUI Test
RESULT fail connection.mac_oui
```

## Module bacext

```
RESULT skip protocol.bacnet.version
Bacnet device not found.
RESULT skip protocol.bacnet.pic
Bacnet device not found... Pics check cannot be performed.
```

## Module tls

```
IOException unable to connect to server.
RESULT skip security.tls.v3
RESULT skip security.x509
```

## Report complete

