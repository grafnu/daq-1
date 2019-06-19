# DAQ scan report for device 3c5ab41e8f0a
Started %% 2019-06-19 13:31:44+00:00

Report generation error: 'device_info' is undefined
Failing data model:
{'modules': {'nmap': {'enabled': True}, 'macoui': {'enabled': True}, 'password': {'enabled': True}, 'switch': {'enabled': False}, 'brute': {'enabled': True}, 'bacext': {'enabled': True}, 'tls': {'enabled': True}}, 'process': {'approver': '*** Approver Name ***', 'operator': '*** Operator Name ***'}, 'run_info': {'run_id': '5d0a3940', 'mac_addr': '3c:5a:b4:1e:8f:0a', 'daq_version': '0.9.7', 'started': '2019-06-19T13:31:44.122Z'}}

## Report summary

|Result|Test|Notes|
|---|---|---|
|skip|base.switch.ping||
|pass|base.target.ping|target |
|pass|connection.mac_oui||
|skip|network.brute||
|fail|protocol.bacnet.version||
|pass|security.passwords||
|fail|security.ports.nmap||
|skip|security.tls.v3||
|skip|security.x509||

## Module ping

```
Baseline ping test report
%% 65 packets captured.
RESULT skip base.switch.ping
RESULT pass base.target.ping target %% 10.20.21.244
```

## Module nmap

```
Open port 22 open tcp ssh
RESULT fail security.ports.nmap
```

## Module brute

```
Target port 10000 not open.
RESULT skip network.brute
```

## Module macoui

```
Mac OUI Test
RESULT pass connection.mac_oui
```

## Module bacext

```
RESULT fail protocol.bacnet.version
```

## Module tls

```
IOException unable to connect to server.
RESULT skip security.tls.v3
RESULT skip security.x509
```

## Module password

```
Brute Test
MAC Address : 38d135010289
Manufacturer : EasyIO Sdn. Bhd.
RESULT pass security.passwords
```

## Report complete

