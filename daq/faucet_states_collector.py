"""Processing faucet events"""

import json
from datetime import datetime


def dump_states(func):
    """Decorator to dump the current states after the states map is modified"""

    def set_default(obj):
        if isinstance(obj, set):
            return list(obj)
        return obj

    def wrapped(self, *args, **kwargs):
        res = func(self, *args, **kwargs)
        _output = json.dumps(self.system_states, default=set_default)
        #print(_output)
        return res

    return wrapped


KEY_SWITCH = "dpids"
KEY_PORTS = "ports"
KEY_PORT_STATUS_COUNT = "change_count"
KEY_PORT_STATUS_TS = "timestamp"
KEY_PORT_STATUS_UP = "status_up"
KEY_LEARNED_MACS = "learned_macs"
KEY_MAC_LEARNING_PORT = "port"
KEY_MAC_LEARNING_IP = "ip_address"
KEY_MAC_LEARNING_TS = "timestamp"
KEY_CONFIG_CHANGE_COUNT = "config_change_count"
KEY_CONFIG_CHANGE_TYPE = "config_change_type"
KEY_CONFIG_CHANGE_TS = "config_change_timestamp"
TOPOLOGY_ENTRY = "topology"
TOPOLOGY_ROOT = "stack_root"
TOPOLOGY_GRAPH = "graph_obj"
TOPOLOGY_CHANGE_COUNT = "change_count"


class FaucetStatesCollector:
    """Processing faucet events and store states in the map"""

    def __init__(self):
        self.system_states = {KEY_SWITCH: {}, TOPOLOGY_ENTRY: {}}
        self.switch_states = self.system_states[KEY_SWITCH]
        self.topo_state = self.system_states[TOPOLOGY_ENTRY]

    def get_system(self):
        """get the system states"""
        return self.system_states

    def get_topology(self):
        """get the topology state"""
        return self.topo_state

    def get_switches(self):
        """get a set of all switches"""
        switch_data = {}
        for switch_name in self.switch_states:
            switch_data[switch_name] = self.get_switch(switch_name)
        return switch_data

    def get_switch(self, switch_name):
        """get switches state"""
        switch_map = {}

        # filling switch attributes
        attributes_map = switch_map.setdefault("attributes", {})
        attributes_map["name"] = switch_name
        attributes_map["dp_id"] = None
        attributes_map["description"] = None

        # filling switch dynamics
        switch_states = self.switch_states.get(str(switch_name), {})
        switch_map["config_change_count"] = switch_states.get(KEY_CONFIG_CHANGE_COUNT, "")
        switch_map["config_change_type"] = switch_states.get(KEY_CONFIG_CHANGE_TYPE, "")
        switch_map["config_change_timestamp"] = switch_states.get(KEY_CONFIG_CHANGE_TS, "")

        switch_port_map = switch_map.setdefault("ports", {})

        # filling port information
        for port_id, port_states in switch_states.get(KEY_PORTS, {}).items():
            port_map = switch_port_map.setdefault(port_id, {})
            # port attributes
            switch_port_attributes_map = port_map.setdefault("attributes", {})
            switch_port_attributes_map["description"] = None
            switch_port_attributes_map["stack_peer_switch"] = None
            switch_port_attributes_map["stack_peer_port"] = None

            # port dynamics
            port_map["status_up"] = port_states.get(KEY_PORT_STATUS_UP, "")
            port_map["port_type"] = None
            port_map["status_timestamp"] = port_states.get(KEY_PORT_STATUS_TS, "")
            port_map["status_count"] = port_states.get(KEY_PORT_STATUS_COUNT, "")
            port_map["packet_count"] = None

        # filling learned macs
        switch_learned_mac_map = switch_map.setdefault("learned_macs", {})
        system_learned_mac_states = self.system_states.get(KEY_LEARNED_MACS, {})
        for mac in switch_states.get(KEY_LEARNED_MACS, set()):
            mac_map = switch_learned_mac_map.setdefault(mac, {})
            mac_states = system_learned_mac_states.get(mac, {})
            mac_map["port"] = mac_states.get(KEY_MAC_LEARNING_PORT, "")
            mac_map["timestamp"] = mac_states.get(KEY_MAC_LEARNING_TS, "")
            mac_map["ip_address"] = mac_states.get(KEY_MAC_LEARNING_IP, "")

        return switch_map

    @dump_states
    def process_port_state(self, timestamp, name, port, status):
        """process port state event"""
        port_table = self.switch_states\
            .setdefault(name, {})\
            .setdefault(KEY_PORTS, {})\
            .setdefault(port, {})

        port_table[KEY_PORT_STATUS_UP] = status
        port_table[KEY_PORT_STATUS_TS] = datetime.fromtimestamp(timestamp).isoformat()

        port_table[KEY_PORT_STATUS_COUNT] = port_table.setdefault(KEY_PORT_STATUS_COUNT, 0) + 1

    @dump_states
    # pylint: disable=too-many-arguments
    def process_port_learn(self, timestamp, name, port, mac, src_ip):
        """process port learn event"""
        # update global mac table
        global_mac_table = self.system_states\
            .setdefault(KEY_LEARNED_MACS, {})\
            .setdefault(mac, {})

        global_mac_table[KEY_MAC_LEARNING_IP] = src_ip
        global_mac_table[KEY_MAC_LEARNING_PORT] = port
        global_mac_table[KEY_MAC_LEARNING_TS] = datetime.fromtimestamp(timestamp).isoformat()

        # update per switch mac table
        self.switch_states\
            .setdefault(name, {})\
            .setdefault(KEY_LEARNED_MACS, set())\
            .add(mac)

    @dump_states
    def process_config_change(self, timestamp, dp_name, restart_type, dp_id):
        """process config change event"""

        # No dp_id (or 0) indicates that this is system-wide, not for a given switch.
        if not dp_id:
            return

        dp_state = self.switch_states.setdefault(dp_name, {})

        dp_state[KEY_CONFIG_CHANGE_TYPE] = restart_type
        dp_state[KEY_CONFIG_CHANGE_TS] = datetime.fromtimestamp(timestamp).isoformat()
        dp_state[KEY_CONFIG_CHANGE_COUNT] = dp_state.setdefault(KEY_CONFIG_CHANGE_COUNT, 0) + 1

    @dump_states
    def process_stack_topo_change(self, timestamp, stack_root, graph):
        """Process stack topology change event"""
        topo_state = self.topo_state

        topo_state[TOPOLOGY_ROOT] = stack_root
        topo_state[TOPOLOGY_GRAPH] = graph
        topo_state[TOPOLOGY_CHANGE_COUNT] = topo_state.setdefault(TOPOLOGY_CHANGE_COUNT, 0) + 1
