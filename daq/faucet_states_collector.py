"""Processing faucet events"""
from datetime import datetime


def save_states(func):
    """Decorator to dump the current states after the states map is modified"""
    def wrapped(self, *args, **kwargs):
        res = func(self, *args, **kwargs)
        print(self.system_states)
        return res

    return wrapped


class FaucetStatesCollector:
    """Processing faucet events and store states in the map"""

    MAP_ENTRY_SWITCH = "dpids"
    MAP_ENTRY_PORTS = "ports"
    MAP_ENTRY_PORT_STATE_CHANGED_COUNT = "change_count"
    MAP_ENTRY_PORT_STATUS = "status"
    MAP_ENTRY_PORT_LAST_CHANGED_TS = "last_changed_timestamp"
    MAP_ENTRY_PORT_LEARNED_MACS = "learned_macs"
    MAP_ENTRY_PORT_MAC_LEARNING_TS = "timestamp"
    MAP_ENTRY_CONFIG_CHANGE_COUNT = "change_count"
    MAP_ENTRY_LAST_RESTART_TYPE = "last_restart"
    MAP_ENTRY_LAST_RESTART_TS = "last_restart_timestamp"
    TOPOLOGY_ENTRY = "topology"
    TOPOLOGY_ROOT = "stack_root"
    TOPOLOGY_GRAPH = "graph_obj"
    TOPOLOGY_CHANGE_COUNT = "change_count"

    def __init__(self):
        self.system_states = {FaucetStatesCollector.MAP_ENTRY_SWITCH: {},\
                FaucetStatesCollector.TOPOLOGY_ENTRY: {}}
        self.switch_states = self.system_states[FaucetStatesCollector.MAP_ENTRY_SWITCH]
        self.topo_state = self.system_states[FaucetStatesCollector.TOPOLOGY_ENTRY]

    def get_switches(self):
        """get switches state"""
        return self.switch_states

    def get_system(self):
        """get the system states"""
        return self.system_states

    def get_topology(self):
        """get the topology state"""
        return self.topo_state

    @save_states
    def process_port_state(self, timestamp, dpid, port, status):
        """process port state event"""
        port_table = self.switch_states\
            .setdefault(dpid, {})\
            .setdefault(FaucetStatesCollector.MAP_ENTRY_PORTS, {})\
            .setdefault(port, {})

        port_table[FaucetStatesCollector.MAP_ENTRY_PORT_STATUS] = status
        port_table[FaucetStatesCollector.MAP_ENTRY_PORT_LAST_CHANGED_TS] = \
            datetime.fromtimestamp(timestamp).isoformat()

        port_table[FaucetStatesCollector.MAP_ENTRY_PORT_STATE_CHANGED_COUNT] = \
            port_table.setdefault(
                FaucetStatesCollector.MAP_ENTRY_PORT_STATE_CHANGED_COUNT, 0) + 1

    @save_states
    def process_port_learn(self, timestamp, dpid, port, mac):
        """process port learn event"""
        mac_table = self.switch_states\
            .setdefault(dpid, {})\
            .setdefault(FaucetStatesCollector.MAP_ENTRY_PORTS, {})\
            .setdefault(port, {})\
            .setdefault(FaucetStatesCollector.MAP_ENTRY_PORT_LEARNED_MACS, {})\
            .setdefault(mac, {})

        mac_table[FaucetStatesCollector.MAP_ENTRY_PORT_MAC_LEARNING_TS] = \
            datetime.fromtimestamp(timestamp).isoformat()

    @save_states
    def process_stack_topo_change(self, timestamp, stack_root, graph):
        """Process stack topology change event"""
        topo_change_obj = self.topo_state

        topo_change_obj[FaucetStatesCollector.TOPOLOGY_ROOT] = stack_root
        topo_change_obj[FaucetStatesCollector.TOPOLOGY_GRAPH] = graph
        topo_change_obj[FaucetStatesCollector.TOPOLOGY_CHANGE_COUNT] =\
            topo_change_obj.setdefault(FaucetStatesCollector.TOPOLOGY_CHANGE_COUNT, 0) + 1

    @save_states
    def process_config_change(self, timestamp, dpid, restart_type):
        """process config change event"""
        config_change_table = self.switch_states\
            .setdefault(dpid, {})

        config_change_table[FaucetStatesCollector.MAP_ENTRY_LAST_RESTART_TYPE] = restart_type
        config_change_table[FaucetStatesCollector.MAP_ENTRY_LAST_RESTART_TS] = \
            datetime.fromtimestamp(timestamp).isoformat()
        config_change_table[FaucetStatesCollector.MAP_ENTRY_CONFIG_CHANGE_COUNT] = \
            config_change_table.setdefault(
                FaucetStatesCollector.MAP_ENTRY_CONFIG_CHANGE_COUNT, 0) + 1
