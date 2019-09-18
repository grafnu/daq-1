from collections import namedtuple

EVENT_TYPE_PORT_STATE = "Port State"
EVENT_TYPE_PORT_LEARN = "Port Learn"
EVENT_TYPE_CONFIG_CHANGE = "Config Change"

all_events = {}


def save_all_events(f):
    def wrapped(*args, **kwargs):
        res = f(*args, **kwargs)
        print(all_events)
        return res
    return wrapped


@save_all_events
def process_port_state(event):
    all_events\
        .setdefault(event.dpid, {})\
        .setdefault(EVENT_TYPE_PORT_STATE, {})\
        .setdefault(event.port, [])\
        .append({"ts":event.timestamp, "acitve": event.active})


@save_all_events
def process_port_learn(event):
    all_events\
        .setdefault(event.dpid, {})\
        .setdefault(EVENT_TYPE_PORT_LEARN, {})\
        .setdefault(event.port, [])\
        .append({"ts":event.timestamp, "mac":event.mac})


@save_all_events
def process_config_change(event):
    all_events\
        .setdefault(event.dpid, {})\
        .setdefault(EVENT_TYPE_CONFIG_CHANGE, [])\
        .append({"ts":event.timestamp, "type": event.restart_type})


class PortStateEvent(namedtuple("PortStateEvent", "dpid timestamp port active")):
    pass


class PortLearnEvent(namedtuple("PortLearnEvent", "dpid timestamp port mac")):
    pass


class ConfigChangeEvent(namedtuple("ConfigChangeEvent", "dpid timestamp restart_type")):
    pass
