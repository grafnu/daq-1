from collections import namedtuple
from datetime import datetime


def save_states(f):
    def wrapped(self, *args, **kwargs):
        res = f(self, *args, **kwargs)
        print(self.system_states)
        return res

    return wrapped


class FaucetStatesCollector:

    TABLE_ENTRY_SWITCH = "dpid"
    TABLE_ENTRY_PORTS = "ports"
    TABLE_ENTRY_PORT_STATE_CHANGED_COUNT = "change_count"
    TABLE_ENTRY_PORT_STATUS = "status"
    TABLE_ENTRY_PORT_LAST_CHANGED_TS = "last_changed_timestamp"
    TABLE_ENTRY_PORT_LEARNED_MACS = "learned_macs"
    TABLE_ENTRY_PORT_MAC_LEARNING_TS = "timestamp"
    TABLE_ENTRY_CONFIG_CHANGE_COUNT = "change_count"
    TABLE_ENTRY_LAST_RESTART_TYPE = "last_restart"
    TABLE_ENTRY_LAST_RESTART_TS = "last_restart_timestamp"

    def __init__(self):
        self.system_states = {FaucetStatesCollector.TABLE_ENTRY_SWITCH: {}}
        self.switch_states = self.system_states[FaucetStatesCollector.TABLE_ENTRY_SWITCH]

    def get_switches():
        return self.system_states

    @save_states
    def process_port_state(self, dpid, port, status, timestamp):
        port_table = self.switch_states\
            .setdefault(dpid, {})\
            .setdefault(FaucetStatesCollector.TABLE_ENTRY_PORTS, {})\
            .setdefault(port, {})

        port_table[FaucetStatesCollector.TABLE_ENTRY_PORT_STATUS] = status
        port_table[FaucetStatesCollector.TABLE_ENTRY_PORT_LAST_CHANGED_TS] = datetime.fromtimestamp(timestamp).strftime("%Y-%m-%d %H:%M%S")

        port_table[FaucetStatesCollector.TABLE_ENTRY_PORT_STATE_CHANGED_COUNT] = \
            port_table.setdefault(FaucetStatesCollector.TABLE_ENTRY_PORT_STATE_CHANGED_COUNT, 0) + 1

    @save_states
    def process_port_learn(self, dpid, port, mac, timestamp):
        mac_table = self.switch_states\
            .setdefault(dpid, {})\
            .setdefault(FaucetStatesCollector.TABLE_ENTRY_PORTS, {})\
            .setdefault(port, {})\
            .setdefault(FaucetStatesCollector.TABLE_ENTRY_PORT_LEARNED_MACS, {})\
            .setdefault(mac, {})

        mac_table[FaucetStatesCollector.TABLE_ENTRY_PORT_MAC_LEARNING_TS] = datetime.fromtimestamp(timestamp).strftime("%Y-%m-%d %H:%M%S")

    @save_states
    def process_config_change(self, dpid, restart_type, timestamp):
        config_change_table = self.switch_states\
            .setdefault(dpid, {})

        config_change_table[FaucetStatesCollector.TABLE_ENTRY_LAST_RESTART_TYPE] = restart_type
        config_change_table[FaucetStatesCollector.TABLE_ENTRY_LAST_RESTART_TS] = datetime.fromtimestamp(timestamp).strftime("%Y-%m-%d %H:%M:%S")
        config_change_table[FaucetStatesCollector.TABLE_ENTRY_CONFIG_CHANGE_COUNT] =\
            config_change_table.setdefault(FaucetStatesCollector.TABLE_ENTRY_CONFIG_CHANGE_COUNT, 0) + 1
