# DAQ scan report for device 9a02571e8f03
Started %% 2019-05-28 16:05:53+00:00

Report generation error: 'device_info' is undefined
Failing data model:
{'device_description': 'N/A', 'modules': {'nmap': {'enabled': True}, 'bacext': {'enabled': True}, 'brute': {'enabled': True}, 'switch': {'enabled': False}}, 'process': {'reviewer': '*** Reviewer Name ***', 'approver': '*** Approver Name ***', 'operator': '*** Operator Name ***'}, 'run_info': {'run_id': '5ced5c61', 'mac_addr': '9a:02:57:1e:8f:03', 'daq_version': '0.9.7', 'started': '2019-05-28T16:05:53.797Z'}}

## Report summary

|Result|Test|Notes|
|---|---|---|
|skip|base.switch.ping||
|pass|base.target.ping|target |
|skip|network.brute||
|pass|protocol.bacnet.version||
|pass|security.ports.nmap||

## Module ping

```
Baseline ping test report
%% 57 packets captured.
RESULT skip base.switch.ping
RESULT pass base.target.ping target %% 10.20.81.40
```

## Module nmap

```
No invalid ports found.
RESULT pass security.ports.nmap
```

## Module brute

```
Target port 10000 not open.
RESULT skip network.brute
```

## Module bacext

```
RESULT pass protocol.bacnet.version
```

## Report complete

