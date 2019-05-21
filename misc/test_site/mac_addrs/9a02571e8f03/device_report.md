# DAQ scan report for device 9a02571e8f03
Started %% 2019-05-21 13:44:18+00:00

Report generation error: 'device_info' is undefined
Failing data model:
{'modules': {'nmap': {'enabled': True}, 'macoui': {'enabled': True}, 'switch': {'enabled': False}, 'brute': {'enabled': True}}, 'device_description': 'N/A', 'process': {'approver': '*** Approver Name ***', 'operator': '*** Operator Name ***', 'reviewer': '*** Reviewer Name ***'}, 'run_info': {'run_id': '5ce400b2', 'mac_addr': '9a:02:57:1e:8f:03', 'daq_version': '0.9.7', 'started': '2019-05-21T13:44:18.783Z'}}

## Report summary

|Result|Test|Notes|
|---|---|---|
|skip|base.switch.ping||
|pass|base.target.ping|target |
|pass|security.ports.nmap||

## Module ping

```
Baseline ping test report
%% 81 packets captured.
RESULT skip base.switch.ping
RESULT pass base.target.ping target %% 10.20.17.40
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

