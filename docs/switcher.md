# Control Plane Switch Access.

This setup defines how to access the the control plane from a container test.

## Required Config
In addition to the `ext_ofip` and `ext_addr` config values, setting
`ext_loip` will enable switch control-plan access. It should be set
to a pattern for the test container IP, e.g. `192.0.3.@/16`, where
the `@` will be automaticaly replaced with test port set number.
Doing this causes the `LOCAL_IP` and `SWITCH_IP` env variables to be set in
test containers. See `misc/test_ping` for an example of how to use them.

If `ext_ctrl` is _not_ defined, then the system will spin up a special
`daq-switch` Docker container to masqurade as a switch (well, respond
to a ping, at least); otherwise, it should enable access to an external
actual physical switch.

## Test Run
<pre>
~/daq$ <b>cat local/system.conf</b>
source misc/system_multi.conf

ext_ofip=192.0.2.10/16
ext_addr=192.0.2.138
ext_loip=192.0.3.@/16
~/daq$ <b>cmd/run -s</b>
Running as root...
Loading config from local/system.conf
Release version 0.9.0
cleanup='echo cleanup'
ext_addr=192.0.2.138
ext_loip=192.0.3.@/16
ext_ofip=192.0.2.10/16
intf_names=faux-1,faux-2,faux-3
&hellip;
INFO:mininet:*** Starting 2 switches
INFO:mininet:pri
INFO:mininet:sec
INFO:mininet:...
INFO:network:Attaching switch interface ctrl-pri on port 1000
INFO:runner:Waiting for system to settle...
INFO:runner:Entering main event loop.
&hellip;
INFO:runner:Done with runner.
INFO:daq:DAQ runner returned 0
Cleanup docker kill daq-faux-1
daq-faux-1
Cleanup docker kill daq-faux-2
daq-faux-2
Cleanup docker kill daq-faux-3
daq-faux-3
Done with run, exit 0
~/daq$ <b>fgrep 192.0.2.138 inst/run-port-01/nodes/ping01/activate.log</b>
PING 192.0.2.138 (192.0.2.138) 56(84) bytes of data.
64 bytes from 192.0.2.138: icmp_seq=1 ttl=64 time=2054 ms
64 bytes from 192.0.2.138: icmp_seq=2 ttl=64 time=1025 ms
64 bytes from 192.0.2.138: icmp_seq=3 ttl=64 time=1.74 ms
--- 192.0.2.138 ping statistics ---
</pre>
