# DAQ scan report for device 9a02571e8f01
Started %% 2019-05-21 13:44:33+00:00

|  Role  |      Name              |
|--------|------------------------|
|Operator| *** Operator Name *** |
|Reviewer| *** Reviewer Name *** |
|Approver| *** Approver Name *** |
|--------|------------------------|
| Test report date | 2019-05-21T13:44:33.311Z |
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
|pass|security.ports.nmap||

## Module ping

```
Baseline ping test report
%% 60 packets captured.
RESULT skip base.switch.ping
RESULT pass base.target.ping target %% 10.20.78.38
```

## Module nmap

```
Allowing 10000 open tcp snet-sensor-mgmt
No invalid ports found.
RESULT pass security.ports.nmap
```

## Module macoui

```
Brute Test 
connection.mac_oui PASSED 
9A0257 Daq Faux Containainer 
```

## Report complete

