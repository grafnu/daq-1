"""Orchestrator component for controlling a Faucet SDN"""

import logging
import sys

import configurator
import faucet_event_client
import http_server
from faucet_states_collector import FaucetStatesCollector

LOGGER = logging.getLogger('forch')


class Forchestrator:
    """Main class encompassing faucet orchestrator components for dynamically
    controlling faucet ACLs at runtime"""

    _TOPOLOGY_FILE = 'inst/dp_graph.json'

    def __init__(self, config):
        self._config = config
        self._faucet_events = None
        self._server = None
        self._collector = FaucetStatesCollector()

    def initialize(self):
        """Initialize forchestrator instance"""
        LOGGER.info('Attaching event channel...')
        self._faucet_events = faucet_event_client.FaucetEventClient(self._config)
        self._faucet_events.connect()

    def main_loop(self):
        """Main event processing loop"""
        LOGGER.info('Entering main event loop...')
        while self._handle_faucet_events():
            pass

    def _handle_faucet_events(self):
        while self._faucet_events:
            event = self._faucet_events.next_event()
            if not event:
                return True

            timestamp = event.get("time")

            (name, dpid, port, active) = self._faucet_events.as_port_state(event)
            if dpid and port:
                LOGGER.info('Port state %s %s %s', name, port, active)
                self._collector.process_port_state(timestamp, name, port, active)

            (name, dpid, port, target_mac, src_ip) = self._faucet_events.as_port_learn(event)
            if dpid and port:
                LOGGER.info('Port learn %s %s %s', name, port, target_mac)
                self._collector.process_port_learn(timestamp, name, port, target_mac, src_ip)

            (name, dpid, restart_type) = self._faucet_events.as_config_change(event)
            if dpid is not None:
                LOGGER.info('DP restart %s %s', name, restart_type)
                self._collector.process_config_change(timestamp, name, restart_type, dpid)

            (stack_root, graph) = self._faucet_events.as_stack_topo_change(event)
            if stack_root is not None:
                LOGGER.info('stack topology change root:%s', stack_root)
                self._collector.process_stack_topo_change(timestamp, stack_root, graph)

        return False

    def get_overview(self, path, params):
        """Get an overview of the system"""
        return {
            'hello': 'world',
            'params': params
        }

    def get_switch(self, path, params):
        """Get the state of the switches"""
        return self._collector.get_switch(params['switch_name'])

    def get_switches(self, path, params):
        """Get the state of the switches"""
        return self._collector.get_switches()

    def get_topology(self, path, params):
        """Get the network topology overview"""
        return self._collector.get_topology()

    def get_active_host_route(self, path, params):
        """Get active host route"""
        return self._collector.get_active_host_route(params['src'], params['dst'])


if __name__ == '__main__':
    logging.basicConfig(level=logging.INFO)
    CONFIG = configurator.Configurator().parse_args(sys.argv)
    FORCH = Forchestrator(CONFIG)
    FORCH.initialize()
    HTTP = http_server.HttpServer(CONFIG)
    HTTP.map_request('overview', FORCH.get_overview)
    HTTP.map_request('topology', FORCH.get_topology)
    HTTP.map_request('switches', FORCH.get_switches)
    HTTP.map_request('switch', FORCH.get_switch)
    HTTP.map_request('host_route', FORCH.get_active_host_route)
    HTTP.map_request('', HTTP.static_file(''))
    HTTP.start_server()
    FORCH.main_loop()
