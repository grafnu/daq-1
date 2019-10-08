"""Collecting the state of CPN components"""

import copy
import logging
import os
import os.path
import yaml

LOGGER = logging.getLogger('cpn')

KEY_NODES = 'cpn_nodes'
KEY_ATTRIBUTES = 'attributes'


class CPNStateCollector:
    """Processing and storing CPN components states"""
    def __init__(self):
        self._cpn_state = {}
        self._nodes_state = self._cpn_state.setdefault(KEY_NODES, {})

        cpn_file_name = os.getenv('CPN_CONFIG_FILE')
        if not cpn_file_name:
            LOGGER.warning("CPN Config file is not specified")
        else:
            LOGGER.info(f"Loading CPN config file: {cpn_file_name}")
            try:
                with open(cpn_file_name) as cpn_file:
                    cpn_data = yaml.safe_load(cpn_file)
                    cpn_nodes = cpn_data.get('cpn_nodes', {})

                    for node, attr_map in cpn_nodes.items():
                        self._nodes_state.setdefault(node, {})[KEY_ATTRIBUTES] = copy.copy(attr_map)
            except OSError as e:
                LOGGER.warning(e)

    def get_cpn_state(self):
        """Get CPN state"""
        ret_map = {}

        for cpn_node, node_state in self._nodes_state.items():
            ret_node_map = ret_map.setdefault(cpn_node, {})
            ret_node_map['attributes'] = copy.copy(node_state.get(KEY_ATTRIBUTES, {}))

        return ret_map
