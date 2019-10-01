"""Processing faucet events"""

import json
from datetime import datetime
import logging
from threading import Lock
import copy

LOGGER = logging.getLogger('forch')

def dump_states(func):
    """Decorator to dump the current states after the states map is modified"""

    def set_default(obj):
        if isinstance(obj, set):
            return list(obj)
        return obj

    def wrapped(self, *args, **kwargs):
        res = func(self, *args, **kwargs)
        with self.lock:
            LOGGER.debug(json.dumps(self.system_states, default=set_default))
        return res

    return wrapped


KEY_SWITCH = "dpids"
KEY_DP_ID = "dp_id"
KEY_PORTS = "ports"
KEY_PORT_STATUS_COUNT = "change_count"
KEY_PORT_STATUS_TS = "timestamp"
KEY_PORT_STATUS_UP = "status_up"
KEY_LEARNED_MACS = "learned_macs"
KEY_MAC_LEARNING_SWITCH = "switches"
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
TOPO_API_HEALTH = "is_healthy"
TOPO_API_NOT_HEALTH = "is_wounded"
TOPO_API_DP_MAP = "switch_map"
TOPO_API_LINK_MAP = "physical_stack_links"
TOPO_API_LACP = "lacp_lag_status"
TOPO_API_ROOT = "active_root"


class FaucetStatesCollector:
    """Processing faucet events and store states in the map"""
    def __init__(self):
        self.system_states = {KEY_SWITCH: {}, TOPOLOGY_ENTRY: {}, KEY_LEARNED_MACS: {}}
        self.switch_states = self.system_states[KEY_SWITCH]
        self.topo_state = self.system_states[TOPOLOGY_ENTRY]
        self.lock = Lock()
        self.learned_macs = self.system_states[KEY_LEARNED_MACS]

    def get_system(self):
        """get the system states"""
        return self.system_states

    def get_topology(self):
        """get the topology state"""
        dplane_map = {}
        dplane_map[TOPO_API_DP_MAP] = self.get_switch_map()
        dplane_map[TOPO_API_LINK_MAP] = self.get_stack_topo()
        dplane_map[TOPO_API_LACP] = None
        dplane_map[TOPO_API_ROOT] = None
        return dplane_map

    def get_switches(self):
        """get a set of all switches"""
        switch_data = {}
        for switch_name in self.switch_states:
            switch_data[switch_name] = self.get_switch(switch_name)
        return switch_data

    def get_switch_map(self):
        """returns switch map for topology overview"""
        switch_map = {}
        topo_obj = self.topo_state
        with self.lock:
            for switch in topo_obj.get(TOPOLOGY_GRAPH, {}).get("nodes", []):
                _id = switch.get("id")
                if _id:
                    switch_map[_id] = {}
                    switch_map[_id]["status"] = None
        return switch_map

    def get_switch(self, switch_name):
        """get switches state"""
        switch_map = {}
        with self.lock:
            # filling switch attributes
            attributes_map = switch_map.setdefault("attributes", {})
            attributes_map["name"] = switch_name
            attributes_map["dp_id"] = \
                    self.switch_states.get(str(switch_name), {}).get(KEY_DP_ID, "")
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
            for mac in switch_states.get(KEY_LEARNED_MACS, set()):
                mac_map = switch_learned_mac_map.setdefault(mac, {})
                mac_states = self.learned_macs.get(mac, {})
                mac_map["ip_address"] = mac_states.get(KEY_MAC_LEARNING_IP, "")
                learned_switch = mac_states.get(KEY_MAC_LEARNING_SWITCH, {}).get(switch_name, {})
                mac_map["port"] = learned_switch.get(KEY_MAC_LEARNING_PORT, "")
                mac_map["timestamp"] = learned_switch.get(KEY_MAC_LEARNING_TS, "")

        return switch_map

    def get_stack_topo(self):
        """Returns formatted topology object"""
        topo_map = {}
        topo_obj = self.topo_state
        with self.lock:
            for link in topo_obj.get(TOPOLOGY_GRAPH, {}).get("links", []):
                link_obj = {}
                port_map = link.get("port_map")
                if port_map:
                    link_obj["switch_a"] = port_map["dp_a"]
                    link_obj["port_a"] = port_map["port_a"]
                    link_obj["switch_b"] = port_map["dp_z"]
                    link_obj["port_b"] = port_map["port_z"]
                    link_obj["status"] = None
                topo_map[link["key"]] = link_obj
        return topo_map

    def get_active_host_route(self, src_mac, dst_mac):
        """Given two MAC addresses in the core network, find the active route between them"""
        res = {'path': []}

        if src_mac not in self.learned_macs or dst_mac not in self.learned_macs:
            return res

        src_learned_switches = self.learned_macs[src_mac].get(KEY_MAC_LEARNING_SWITCH, {})
        dst_learned_switches = self.learned_macs[dst_mac].get(KEY_MAC_LEARNING_SWITCH, {})

        next_hops = self.get_graph(src_mac, dst_mac)

        if not next_hops:
            return res

        src_switch, src_port = self.get_access_switch(src_mac)

        next_hop = {'switch': src_switch, 'ingress': src_port, 'egress': None}

        while next_hop['switch'] in next_hops:
            next_hop['egress'] = dst_learned_switches[next_hop['switch']][KEY_MAC_LEARNING_PORT]
            res['path'].append(copy.copy(next_hop))
            next_hop['switch'] = next_hops[next_hop['switch']]
            next_hop['ingress'] = src_learned_switches[next_hop['switch']][KEY_MAC_LEARNING_PORT]

        next_hop['egress'] = dst_learned_switches[next_hop['switch']][KEY_MAC_LEARNING_PORT]
        res['path'].append(copy.copy(next_hop))

        return res

    @dump_states
    def process_port_state(self, timestamp, name, port, status):
        """process port state event"""
        with self.lock:
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
        with self.lock:
            # update global mac table
            global_mac_table = self.learned_macs.setdefault(mac, {})

            global_mac_table[KEY_MAC_LEARNING_IP] = src_ip

            global_mac_switch_table = global_mac_table.setdefault(KEY_MAC_LEARNING_SWITCH, {})
            learning_switch = global_mac_switch_table.setdefault(name, {})
            learning_switch[KEY_MAC_LEARNING_PORT] = port
            learning_switch[KEY_MAC_LEARNING_TS] = datetime.fromtimestamp(timestamp).isoformat()

            # update per switch mac table
            self.switch_states\
                .setdefault(name, {})\
                .setdefault(KEY_LEARNED_MACS, set())\
                .add(mac)

    @dump_states
    def process_config_change(self, timestamp, dp_name, restart_type, dp_id):
        """process config change event"""
        with self.lock:
            # No dp_id (or 0) indicates that this is system-wide, not for a given switch.
            if not dp_id:
                return

            dp_state = self.switch_states.setdefault(dp_name, {})
            dp_state = self.switch_states.setdefault(dp_name, {})

            dp_state[KEY_DP_ID] = dp_id
            dp_state[KEY_CONFIG_CHANGE_TYPE] = restart_type
            dp_state[KEY_CONFIG_CHANGE_TS] = datetime.fromtimestamp(timestamp).isoformat()
            dp_state[KEY_CONFIG_CHANGE_COUNT] = dp_state.setdefault(KEY_CONFIG_CHANGE_COUNT, 0) + 1

    @dump_states
    def process_stack_topo_change(self, timestamp, stack_root, graph):
        """Process stack topology change event"""
        topo_state = self.topo_state
        with self.lock:
            topo_state[TOPOLOGY_ROOT] = stack_root
            topo_state[TOPOLOGY_GRAPH] = graph
            topo_state[TOPOLOGY_CHANGE_COUNT] = topo_state.setdefault(TOPOLOGY_CHANGE_COUNT, 0) + 1

    @staticmethod
    def get_endpoints_from_link(link_map):
        """Get the the pair of switch and port for a link"""
        from_sw = link_map["port_map"]["dp_a"]
        from_port = int(link_map["port_map"]["port_a"][5:])
        to_sw = link_map["port_map"]["dp_z"]
        to_port = int(link_map["port_map"]["port_z"][5:])

        return from_sw, from_port, to_sw, to_port

    # pylint: disable=too-many-arguments
    def add_link(self, src_mac, dst_mac, sw_1, port_1, sw_2, port_2, graph):
        """Insert link into graph if link is used by the src and dst"""
        src_learned_switches = self.learned_macs[src_mac][KEY_MAC_LEARNING_SWITCH]
        dst_learned_switches = self.learned_macs[dst_mac][KEY_MAC_LEARNING_SWITCH]
        src_learned_port = src_learned_switches.get(sw_1, {}).get(KEY_MAC_LEARNING_PORT, "")
        dst_learned_port = dst_learned_switches.get(sw_2, {}).get(KEY_MAC_LEARNING_PORT, "")

        if src_learned_port == port_1 and dst_learned_port == port_2:
            graph[sw_2] = sw_1

    def get_access_switch(self, mac):
        """Get access switch and port for a given MAC"""
        access_switch_port = {}
        learned_switches = self.learned_macs.get(mac, {}).get(KEY_MAC_LEARNING_SWITCH)

        for switch, port_map in learned_switches.items():
            access_switch_port[switch] = port_map[KEY_MAC_LEARNING_PORT]

        if not access_switch_port:
            return None

        for link_map in self.topo_state.get(TOPOLOGY_GRAPH).get("links", []):
            if not link_map:
                continue

            sw_1, port_1, sw_2, port_2 = FaucetStatesCollector.get_endpoints_from_link(link_map)
            if access_switch_port.get(sw_1, "") == port_1:
                access_switch_port.pop(sw_1)
            if access_switch_port.get(sw_2, "") == port_2:
                access_switch_port.pop(sw_2)

        return access_switch_port.popitem()

    def get_graph(self, src_mac, dst_mac):
        """Get a graph consists of links only used by src and dst MAC"""
        graph = {}
        for link_map in self.topo_state.get(TOPOLOGY_GRAPH, {}).get("links", []):
            if not link_map:
                continue
            sw_1, p_1, sw_2, p_2 = FaucetStatesCollector.get_endpoints_from_link(link_map)
            self.add_link(src_mac, dst_mac, sw_1, p_1, sw_2, p_2, graph)
            self.add_link(src_mac, dst_mac, sw_2, p_2, sw_1, p_1, graph)

        return graph
