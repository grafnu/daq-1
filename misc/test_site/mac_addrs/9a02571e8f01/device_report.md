# DAQ scan report for device 9a02571e8f01
Started %% 2019-05-31 14:25:26+00:00

|  Role  |      Name              |
|--------|------------------------|
|Operator| *** Operator Name *** |
|Reviewer| *** Reviewer Name *** |
|Approver| *** Approver Name *** |
|--------|------------------------|
| Test report date | 2019-05-31T14:25:26.926Z |
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
|fail|protocol.bacnet.version||
|pass|security.ports.nmap||

## Module ping

```
Baseline ping test report
%% 82 packets captured.
RESULT skip base.switch.ping
RESULT pass base.target.ping target %% 10.20.92.38
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

## Module bacext

```
RESULT fail protocol.bacnet.version
```

## Report complete
