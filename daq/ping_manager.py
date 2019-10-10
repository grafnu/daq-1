"""Manages ping task"""

import asyncio
import threading
from asyncio.subprocess import PIPE
from collections import namedtuple


PingResult = namedtuple('PingResult', ['host_name', 'proc_code', 'stdout', 'stderr'])


class PingManager:
    """Manages a thread that periodically pings the hosts"""
    def __init__(self, hosts: dict, interval: int = 60, count: int = 10):
        self._hosts = hosts
        self._count = count
        self._interval = interval
        self._loop = asyncio.new_event_loop()
        asyncio.get_child_watcher().attach_loop(self._loop)

    async def _ping_host(self, host_name, ip):
        """Ping a single host"""
        cmd = f"ping -c {self._count} {ip}"

        proc = await asyncio.create_subprocess_shell(cmd, stdout=PIPE, stderr=PIPE)
        proc_code = await proc.wait()
        stdout, stderr = await proc.communicate()

        return PingResult(host_name, proc_code, stdout.decode(), stderr.decode())

    async def _ping_hosts(self):
        """Ping hosts and return ping result of the hosts"""
        ret_map = {}
        ping_results = await asyncio.gather(*(self._ping_host(h, ip) for h, ip in self._hosts.items()))

        for ping_result in ping_results:
            host_map = ret_map.setdefault(ping_result.host_name, {})
            host_map['proc_code'] = ping_result.proc_code
            host_map['stdout'] = ping_result.stdout
            host_map['stderr'] = ping_result.stderr

        return ret_map

    def _periodic_ping_hosts(self, handler):
        """Periodically ping hosts"""
        ticker = threading.Event()
        while not ticker.wait(self._interval):
            ret_map = self._loop.run_until_complete(self._ping_hosts())
            handler(ret_map)

    def start_loop(self, handler):
        """Start ping loop"""
        t = threading.Thread(target=self._periodic_ping_hosts, args=(handler,))
        t.start()
