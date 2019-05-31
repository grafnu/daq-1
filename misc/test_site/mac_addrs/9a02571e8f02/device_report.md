# DAQ scan report for device 9a02571e8f02
Started %% 2019-05-31 14:25:55+00:00

|  Role  |      Name              |
|--------|------------------------|
|Operator| *** Operator Name *** |
|Reviewer| *** Reviewer Name *** |
|Approver| *** Approver Name *** |
|--------|------------------------|
| Test report date | 2019-05-31T14:25:55.469Z |
| DAQ version      | 0.9.7 |

## Device Identification

| Device        | Entry              |
|---------------|--------------------|
| Name          | *** Name *** |
| GUID          | *** GUID *** |
| MAC addr      | 9a:02:57:1e:8f:02 |
| Hostname      | *** Network Hostname *** |
| Type          | *** Type *** |
| Make          | *** Make *** |
| Model         | *** Model *** |
| Serial Number | *** Serial *** |
| Version       | *** Version *** |

## Device Description

N/A

## Report summary

|Result|Test|Notes|
|---|---|---|
|skip|base.switch.ping||
|pass|base.target.ping|target |
|pass|network.brute||
|fail|protocol.bacnet.version||
|fail|security.ports.nmap||

## Module ping

```
Baseline ping test report
%% 82 packets captured.
RESULT skip base.switch.ping
RESULT pass base.target.ping target %% 10.20.1.39
```

## Module nmap

```
Open port 10000 open tcp snet-sensor-mgmt
RESULT fail security.ports.nmap
```

## Module brute

```
Connection closed by foreign host.
Failed after retries.
RESULT pass network.brute
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

