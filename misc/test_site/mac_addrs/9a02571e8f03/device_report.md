Report generation error: 'device_info' is undefined
Failing data model:
{'modules': {'nmap': {'enabled': True}, 'macoui': {'enabled': True}, 'switch': {'enabled': False}, 'brute': {'enabled': True}, 'bacext': {'enabled': True}, 'tls': {'enabled': True}}, 'process': {'approver': '*** Approver Name ***', 'operator': '*** Operator Name ***'}, 'run_info': {'run_id': '5d007e76', 'mac_addr': '9a:02:57:1e:8f:03', 'daq_version': '0.9.7', 'started': '2019-06-12T04:24:22.611Z'}, 'clean_mac': '9a02571e8f03', 'start_time': datetime.datetime(2019, 6, 12, 4, 24, 22, tzinfo=<UTC>), 'end_time': datetime.datetime(2019, 6, 12, 4, 26, 18, tzinfo=<UTC>)}

## Report summary

|Result|Test|Notes|
|---|---|---|
|skip|base.switch.ping||
|pass|base.target.ping|target |

## Module ping

```
Baseline ping test report
%% 249 packets captured.
RESULT skip base.switch.ping
RESULT pass base.target.ping target %% 10.20.34.40
```

## Report complete

