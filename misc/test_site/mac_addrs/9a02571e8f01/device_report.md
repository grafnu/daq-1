# DAQ scan report for device 9a02571e8f01
Started %% 2019-05-29 16:45:27+00:00

|  Role  |      Name              |
|--------|------------------------|
|Operator| *** Operator Name *** |
|Reviewer| *** Reviewer Name *** |
|Approver| *** Approver Name *** |
|--------|------------------------|
| Test report date | 2019-05-29T16:45:27.975Z |
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

## Module ping

```
Baseline ping test report
%% 61 packets captured.
RESULT skip base.switch.ping
RESULT pass base.target.ping target %% 10.20.85.38
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

## Report complete

