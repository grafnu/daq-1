# DAQ scan report for device 9a02571e8f03
Started %% 2019-05-31 12:47:53+00:00

Report generation error: 'device_info' is undefined
Failing data model:
{'modules': {'nmap': {'enabled': True}, 'switch': {'enabled': False}, 'brute': {'enabled': True}, 'tls': {'enabled': True}}, 'process': {'approver': '*** Approver Name ***', 'operator': '*** Operator Name ***', 'reviewer': '*** Reviewer Name ***'}, 'device_description': 'N/A', 'run_info': {'daq_version': '0.9.7', 'run_id': '5cf12279', 'mac_addr': '9a:02:57:1e:8f:03', 'started': '2019-05-31T12:47:53.073Z'}}

## Report summary

|Result|Test|Notes|
|---|---|---|
|skip|base.switch.ping||
|pass|base.target.ping|target |
|skip|network.brute||
|pass|security.ports.nmap||
|fail|security.tls.v3||
|fail|security.x509||

## Module ping

```
Baseline ping test report
%% 78 packets captured.
RESULT skip base.switch.ping
RESULT pass base.target.ping target %% 10.20.41.40
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

## Module tls

```
RESULT fail security.tls.v3
RESULT fail security.x509
```

## Report complete

