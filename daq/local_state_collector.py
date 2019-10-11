"""Collecting the states of the local system"""

from datetime import datetime
import re

import psutil


class LocalStateCollector:
    """Storing local system states"""

    def __init__(self):
        self._state = {'processes': {}}
        self._process_state = self._state['processes']
        self._target_procs = {'faucet':     ('ryu-manager', r'faucet\.faucet'),
                              'gauge':      ('ryu-manager', r'faucet\.gauge'),
                              'keepalived': ('keepalived', r'keepalived'),
                              'forch':      ('python', r'forchestrator\.py'),
                              'bosun':      ('dunsel_watcher', r'bosun')}

    def get_process_state(self, extended=True):
        """Get the information of processes in proc_set"""
        self._process_state = {}
        procs = self._get_target_processes()

        # fill up process info
        for proc_name, proc in procs.items():
            proc_map = {}
            self._process_state[proc_name] = proc_map

            proc_map['cmd_line'] = proc.cmdline()
            proc_map['create_time'] = datetime.fromtimestamp(proc.create_time()).isoformat()
            proc_map['status'] = proc.status()
            proc_map['cpu_times_s'] = {}
            proc_map['cpu_times_s']['user'] = proc.cpu_times().user
            proc_map['cpu_times_s']['system'] = proc.cpu_times().system
            if hasattr(proc.cpu_times(), 'iowait'):
                proc_map['cpu_times_s']['iowait'] = proc.cpu_times().iowait

            proc_map['memory_info_mb'] = {}
            proc_map['memory_info_mb']['rss'] = proc.memory_info().rss / 1e6
            proc_map['memory_info_mb']['vms'] = proc.memory_info().vms / 1e6

        return self._process_state

    def _get_target_processes(self):
        """Get target processes"""
        procs = {}
        for proc in psutil.process_iter():
            for target_name, (target_cmd, target_regex) in self._target_procs.items():
                if proc.name() == target_cmd:
                    cmd_line_str = ''.join(proc.cmdline())
                    if re.search(target_regex, cmd_line_str):
                        procs[target_name] = proc
                        break
        return procs

    def get_process_overview(self):
        """Get process overview (limited details)"""
        return self.get_process_state(extended=False)
