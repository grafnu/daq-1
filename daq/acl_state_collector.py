"""Process ACL states"""

from __future__ import absolute_import

import logger
from utils import dict_proto

from proto.acl_counting_pb2 import RuleCounts

LOGGER = logger.get_logger('aclstate')


class AclStateCollector:
    """Processing ACL states for ACL counting"""

    def __init__(self):
        self._switch_configs = {}

    def get_port_rule_counts(self, switch, port, rule_samples):
        """Return the ACL count for a port"""

        acl_config, error_map = self._verify_port_acl_config(switch, port)

        if not acl_config:
            return dict_proto(error_map, RuleCounts)

        rule_counts = self._get_port_rule_counts(switch, port, acl_config, rule_samples)
        return dict_proto(rule_counts, RuleCounts)

    # pylint: disable=protected-access
    def _get_port_rule_counts(self, switch, port, acl_config, rule_samples):
        rule_counts_map = {'rules': {}, 'errors': []}
        rules_map = rule_counts_map['rules']
        errors = rule_counts_map['errors']

        LOGGER.info('Processing port %d rules %d', port, len(acl_config.rules))
        for rule_config in acl_config.rules:
            cookie_num = rule_config.get('cookie')
            if not cookie_num:
                LOGGER.error(
                    'Cookie is not generated for ACL rule: %s, %s',
                    acl_config._id, rule_config.get('description'))
                continue

            rule_description = rule_config.get('description')
            if not rule_description:
                LOGGER.warning('Rule with cookie %s does not have a description', cookie_num)
                continue

            has_sample = None
            sample_cookies = set()
            for sample in rule_samples:
                if sample.labels.get('dp_name') != switch:
                    continue
                if int(sample.labels.get('in_port')) != port:
                    continue

                sample_cookies.add(cookie_num)
                if str(sample.labels.get('cookie')) != str(cookie_num):
                    continue

                has_sample = int(sample.value)

            rule_map = rules_map.setdefault(rule_description, {})
            rule_map['packet_count'] = has_sample

            LOGGER.info('Cookie %d, samples %d, count %s, cookies %s',
                        cookie_num, len(rule_samples), has_sample, sample_cookies)

            #if has_sample is not None:
            #    error = (f'No ACL metric sample available: '
            #             f'{switch}, {port}, {acl_config._id}, {rule_description}')
            #    errors.append(error)
            #    LOGGER.error(error)

        return rule_counts_map

    def _verify_port_acl_config(self, switch, port):
        error_map = {'errors': []}
        error_list = error_map['errors']

        switch_config = self._switch_configs.get(switch)
        if not switch_config:
            error = f'Switch not defined in Faucet dps config: {switch}'
            LOGGER.error(error)
            error_list.append(error)
            return None, error_map

        port_config = switch_config.ports.get(port)
        if not port_config:
            error = f'Port not defined in Faucet dps config: {switch}, {port}'
            LOGGER.error(error)
            error_list.append(error)
            return None, error_map

        acls_config = port_config.acls_in
        if not acls_config:
            error = f'No ACLs applied to port: {switch}, {port}'
            LOGGER.error(error)
            error_list.append(error)
            return None, error_map

        if len(acls_config) != 1:
            error = f'More than one ACLs were applied to port: {switch}, {port}'
            LOGGER.error(error)
            error_list.append(error)
            return None, error_map

        return acls_config[0], None

    def update_switch_configs(self, switch_configs):
        """Update cache of switch configs"""
        self._switch_configs = switch_configs
