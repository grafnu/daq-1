"""Orchestrator component for controlling a Faucet SDN"""

import logging
import sys
import time
import threading

import configurator
import faucet_event_client
import http_server
from faucet_states_collector import FaucetStatesCollector

LOGGER = logging.getLogger('forch')


class Forchestrator:
    """Main class encompassing faucet orchestrator components for dynamically
    controlling faucet ACLs at runtime"""

    def __init__(self, config):
        self._config = config
        self._faucet_events = None
        self._server = None
        self._faucet_states_collector = FaucetStatesCollector()

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
            LOGGER.error("Exception:", str(e))
            raise

    def _handle_faucet_events(self):
        while self._faucet_events:
            event = self._faucet_events.next_event()
            if not event:
                return True

            LOGGER.debug('Faucet event %s', event)
            timestamp = event.get("timestamp", time.time())

            (dpid, port, active) = self._faucet_events.as_port_state(event)
            if dpid and port:
                LOGGER.debug('Port state %s %s %s', dpid, port, active)
                self._faucet_states_collector.process_port_state(timestamp, dpid, port, active)

            (dpid, port, target_mac, src_ip) = self._faucet_events.as_port_learn(event)
            if dpid and port:
                LOGGER.debug('Port learn %s %s %s', dpid, port, target_mac)
                self._faucet_states_collector.process_port_learn(
                    timestamp, dpid, port, target_mac, src_ip)

            (dpid, restart_type) = self._faucet_events.as_config_change(event)
            if dpid is not None:
                LOGGER.debug('DP restart %d %s', dpid, restart_type)
                self._faucet_states_collector.process_config_change(timestamp, dpid, restart_type)

            (stack_root, graph) = self._faucet_events.as_stack_topo_change(event)
            if stack_root is not None:
                LOGGER.debug('stack topology change root:%s graph:%s', stack_root, graph)
                self._faucet_states_collector\
                        .process_stack_topo_change(timestamp, stack_root, graph)


        return False

    def get_overview(self, path, params):
        """Get an overview of the system"""
        return {
            'hello': 'world',
            'params': params
        }

    def get_switches(self, path, params):
        """Get the state of the switches"""
        return self._faucet_states_collector.get_switches(params['switch_name'])

    def get_topology(self, path, params):
        """Get the network topology overview"""
        return self._faucet_states_collector.get_topology()


if __name__ == '__main__':
    logging.basicConfig(level=logging.INFO)
    CONFIG = configurator.Configurator().parse_args(sys.argv)
    FORCH = Forchestrator(CONFIG)
    FORCH.initialize()
    HTTP = http_server.HttpServer(CONFIG)
    HTTP.map_request('overview', FORCH.get_overview)
    HTTP.map_request('topology', FORCH.get_topology)
    HTTP.map_request('switches', FORCH.get_switches)
    HTTP.map_request('', HTTP.static_file(''))
    HTTP.start_server()
    FORCH.main_loop()
    LOGGER.warning('Exiting program')
    HTTP.stop_server()

