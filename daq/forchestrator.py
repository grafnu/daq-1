"""Orchestrator component for controlling a Faucet SDN"""

import logging
import http.server
import socketserver
import sys
import threading
import time

import configurator
import faucet_event_client
import http_server
import faucet_events

LOGGER = logging.getLogger('forch')


class Forchestrator:
    """Main class encompassing faucet orchestrator components for dynamically
    controlling faucet ACLs at runtime"""

    def __init__(self, config):
        self._config = config
        self._faucet_events = None
        self._server = None

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
            LOGGER.debug('Faucet event %s', event)
            if not event:
                return True

            timestamp = event.get("timestamp", time.time())

            (dpid, port, active) = self._faucet_events.as_port_state(event)
            if dpid and port:
                LOGGER.info('Port state %s %s %s', dpid, port, active)
                port_state_event = faucet_events.PortStateEvent(dpid, timestamp, port, active)
                faucet_events.process_port_state(port_state_event)

            (dpid, port, target_mac) = self._faucet_events.as_port_learn(event)
            if dpid and port:
                LOGGER.info('Port learn %s %s %s', dpid, port, target_mac)
                port_learn_event = faucet_events.process_port_learn(dpid, timestamp, port, target_mac)
                faucet_events.process_port_learn(port_learn_event)

            (dpid, restart_type) = self._faucet_events.as_config_change(event)
            if dpid is not None:
                LOGGER.info('DP restart %d %s', dpid, restart_type)
                config_change_event = faucet_events.ConfigChangeEvent(dpid, timestamp, restart_type)
                faucet_events.process_config_change(config_change_event)

        return False

    def get_overview(self, params):
        return {
            'hello': 'world',
            'params': params
        }


if __name__ == '__main__':
    logging.basicConfig(level=logging.INFO)
    CONFIG = configurator.Configurator().parse_args(sys.argv)
    FORCH = Forchestrator(CONFIG)
    FORCH.initialize()
    HTTP = http_server.HttpServer(CONFIG)
    HTTP.map_request('overview', FORCH.get_overview)
    HTTP.start_server()
    FORCH.main_loop()
