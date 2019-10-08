"""Collecting the states of the local system"""

from datetime import datetime
import psutil


class LocalStateCollector:
    """Storing local system states"""
    def __init__(self):
        self._state = {'processes': []}
        self._process_state = self._state['processes']

    def get_process_state(self):
        """Get the information of processes in proc_set"""

        target_procs = {'ryu-manager', 'keepalived', 'forch', 'dunsel_watcher', 'python'}
        procs = [p for p in psutil.process_iter() if p.name() in target_procs]
        self._process_state = []

        # fill up process info
        for proc in procs:
            proc_map = {}
            self._process_state.append(proc_map)

            proc_map['name'] = proc.name()
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
