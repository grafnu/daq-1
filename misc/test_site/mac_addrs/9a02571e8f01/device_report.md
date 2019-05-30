# DAQ scan report for device 9a02571e8f01
Started %% 2019-05-30 06:23:59+00:00

|  Role  |      Name              |
|--------|------------------------|
|Operator| *** Operator Name *** |
|Reviewer| *** Reviewer Name *** |
|Approver| *** Approver Name *** |
|--------|------------------------|
| Test report date | 2019-05-30T06:23:59.398Z |
| DAQ version      | 0.9.7 |

## Device Identification

| Device        | Entry              |
|---------------|--------------------|
| Name          |  |
| GUID          |  |
| MAC addr      | 9a:02:57:1e:8f:01 |
| Hostname      |  |
| Type          |  |
| Make          |  |
| Model         |  |
| Serial Number |  |
| Version       |  |

## Device Description

N/A

## Report summary

|Result|Test|Notes|
|---|---|---|
|skip|base.switch.ping||
|pass|base.target.ping|target |
|fail|network.brute||
|pass|security.ports.nmap||
|fail|security.tls.v3||
|fail|security.x509||

## Module ping

```
Baseline ping test report
%% 75 packets captured.
RESULT skip base.switch.ping
RESULT pass base.target.ping target %% 10.20.22.38
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

## Module tls

```
RESULT fail security.tls.v3
RESULT fail security.x509
```

## Report complete

