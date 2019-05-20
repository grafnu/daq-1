# DAQ scan report for device 9a02571e8f02
Started %% 2019-05-20 11:42:59+00:00

|  Role  |      Name              |
|--------|------------------------|
|Operator| *** Operator Name *** |
|Reviewer| *** Reviewer Name *** |
|Approver| *** Approver Name *** |
|--------|------------------------|
| Test report date | 2019-05-20T11:42:59.061Z |
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
|fail|security.ports.nmap||

## Module ping

```
Baseline ping test report
%% 81 packets captured.
RESULT skip base.switch.ping
RESULT pass base.target.ping target %% 10.20.10.39
```

## Module nmap

```
Open port 10000 open tcp snet-sensor-mgmt
RESULT fail security.ports.nmap
```

## Module macoui

```
Brute Test 
connection.mac_oui PASSED 
9A0257 Daq Faux Containainer 
```

## Report complete

