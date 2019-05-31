# DAQ scan report for device 9a02571e8f03
Started %% 2019-05-31 14:25:41+00:00

Report generation error: 'device_info' is undefined
Failing data model:
{'run_info': {'run_id': '5cf13965', 'started': '2019-05-31T14:25:41.165Z', 'mac_addr': '9a:02:57:1e:8f:03', 'daq_version': '0.9.7'}, 'device_description': 'N/A', 'modules': {'brute': {'enabled': True}, 'nmap': {'enabled': True}, 'switch': {'enabled': False}, 'bacext': {'enabled': True}}, 'process': {'operator': '*** Operator Name ***', 'approver': '*** Approver Name ***', 'reviewer': '*** Reviewer Name ***'}}

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
%% 73 packets captured.
RESULT skip base.switch.ping
RESULT pass base.target.ping target %% 10.20.12.40
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

