# DAQ scan report for device 9a02571e8f03
Started %% 2019-05-20 11:43:13+00:00

Report generation error: 'device_info' is undefined
Failing data model:
{'modules': {'nmap': {'enabled': True}, 'macoui': {'enabled': True}, 'switch': {'enabled': False}, 'brute': {'enabled': True}}, 'device_description': 'N/A', 'run_info': {'started': '2019-05-20T11:43:13.941Z', 'mac_addr': '9a:02:57:1e:8f:03', 'run_id': '5ce292d1', 'daq_version': '0.9.7'}, 'process': {'operator': '*** Operator Name ***', 'approver': '*** Approver Name ***', 'reviewer': '*** Reviewer Name ***'}}

## Report summary

|Result|Test|Notes|
|---|---|---|
|skip|base.switch.ping||
|pass|base.target.ping|target |
|pass|security.ports.nmap||

## Module ping

```
Baseline ping test report
%% 82 packets captured.
RESULT skip base.switch.ping
RESULT pass base.target.ping target %% 10.20.8.40
```

## Module nmap

```
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

