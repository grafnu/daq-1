"""Orchestrator component for controlling a Faucet SDN"""

import logging
import sys
import configurator
import faucet_event_client
import http_server
from faucet_states_collector import FaucetStatesCollector
from local_state_collector import LocalStateCollector

LOGGER = logging.getLogger('forch')


class Forchestrator:
    """Main class encompassing faucet orchestrator components for dynamically
    controlling faucet ACLs at runtime"""

    def __init__(self, config):
        self._config = config
        self._faucet_events = None
        self._server = None
        self._collector = FaucetStatesCollector() # TODO change to _faucet_collector
        self._local_collector = LocalStateCollector()

    def initialize(self):
        """Initialize forchestrator instance"""
        LOGGER.info('Attaching event channel...')
        self._faucet_events = faucet_event_client.FaucetEventClient(self._config)
        self._faucet_events.connect()

    def main_loop(self):
        """Main event processing loop"""
        LOGGER.info('Entering main event loop...')
        try:
            while self._handle_faucet_events():
                pass
        except KeyboardInterrupt:
            LOGGER.info('Keyboard interrupt. Exiting.')
            self._faucet_events.disconnect()
        except Exception as e:
            LOGGER.error("Exception: %s", e)
            raise

    def _handle_faucet_events(self):
        while self._faucet_events:
            event = self._faucet_events.next_event()
            if not event:
                return True
            timestamp = event.get("time")
            LOGGER.debug("Event: %r", event)
            (name, dpid, port, active) = self._faucet_events.as_port_state(event)
            if dpid and port:
                LOGGER.debug('Port state %s %s %s', name, port, active)
                self._collector.process_port_state(timestamp, name, port, active)
            (name, dpid, port, target_mac, src_ip) = self._faucet_events.as_port_learn(event)
            if dpid and port:
                LOGGER.debug('Port learn %s %s %s', name, port, target_mac)
                self._collector.process_port_learn(timestamp, name, port, target_mac, src_ip)
            (name, dpid, restart_type, dps_config) = self._faucet_events.as_config_change(event)
            if dpid is not None:
                LOGGER.debug('DP restart %s %s', name, restart_type)
                self._collector.process_dp_config_change(timestamp, name, restart_type, dpid)
            if dps_config:
                LOGGER.debug('Config change. New config: %s', dps_config)
                self._collector.process_dataplane_config_change(timestamp, dps_config)

            (stack_root, graph) = self._faucet_events.as_stack_topo_change(event)
            if stack_root is not None:
                LOGGER.debug('stack topology change root:%s', stack_root)
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

    def get_active_host_path(self, path, params):
        """Get active host path"""
        return self._collector.get_active_host_path(params['src'], params['dst'])

    def get_process_state(self, path, params):
        """Get certain processes state on the controller machine"""
        return self._local_collector.get_process_state()


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
    HTTP.map_request('host_path', FORCH.get_active_host_path)
    HTTP.map_request('process_state', FORCH.get_process_state)
    HTTP.map_request('', HTTP.static_file(''))
    HTTP.start_server()
    FORCH.main_loop()
    LOGGER.warning('Exiting program')
    HTTP.stop_server()
