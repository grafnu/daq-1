# Mock switch testing.

This setup tests using a mock-switch as an experimental endpoint
for accessing the control plane from a container test.

## Window #1
<pre>
~/daq$ <b>cat local/system.conf</b>
source misc/system_multi.conf

ext_ofip=192.0.2.10/16
ext_addr=192.0.2.138
~/daq$ <b>cmd/run -k</b>
Loading config from local/system.conf
&hellip;
Release version 0.9.0
cleanup='echo cleanup'
ext_addr=192.0.2.138
ext_ofip=192.0.2.10/16
intf_names=faux-1,faux-2,faux-3
run_mode=local
sec_port=4
&hellip;
INFO:docker:Target port 1 test mudgee running
INFO:docker:Target port 1 test mudgee passed
<b>INFO:docker:Target port 1 test hold running</b>
INFO:docker:Target port 2 test nmap passed
INFO:docker:Target port 2 test mudgee running
</pre>

## Window #2
<pre>
~/daq$ <b>bin/switch_local</b>
Loading config from local/system.conf
Cleaning old setup...
Creating local bridge...
Creating ovs-link interfaces...
Creating local-link interfaces...
Attaching to bridges...
Creating daq-switch, because only ext_addr defined.
daq-switch
Creating docker with veth -swb at 192.0.2.138/16
Bridging ctrl-swa to ctrl-br
Configuring ctrl-swy with 192.0.2.10/16
Adjust IP tables to enable bridge forwaring...

TODO: Add appropriate IP address to test runner container:
  root@hold01:~# ip addr add 192.0.2.11/16 dev hold01-eth0

Done with local switch setup.
~/daq$ <b>docker exec -ti daq-hold01 bash</b>
root@hold01:~# <b>ip addr add 192.0.2.11/16 dev hold01-eth0</b>
root@hold01:~# <b>ping 192.0.2.138</b>
PING 192.0.2.138 (192.0.2.138) 56(84) bytes of data.
64 bytes from 192.0.2.138: icmp_seq=1 ttl=64 time=1019 ms
64 bytes from 192.0.2.138: icmp_seq=2 ttl=64 time=1.53 ms
64 bytes from 192.0.2.138: icmp_seq=3 ttl=64 time=0.150 ms
64 bytes from 192.0.2.138: icmp_seq=4 ttl=64 time=0.180 ms
64 bytes from 192.0.2.138: icmp_seq=5 ttl=64 time=0.151 ms
64 bytes from 192.0.2.138: icmp_seq=6 ttl=64 time=0.151 ms
&hellip;
</pre>
