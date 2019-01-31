"""Faucet network topology generator"""

import copy
import logging
import os
import sys
import yaml

from daq import DAQ

LOGGER = logging.getLogger('generator')

class TopologyGenerator():
    """Topology generator for Faucet top-level configs"""

    _YAML_POSTFIX = '.yaml'

    def __init__(self, daq_config):
        self.config = daq_config.config
        daq_config.configure_logging()
        self._setup = None
        self._site = None

    def process(self):
        """Process a configured generator"""
        if 'raw_topo' in self.config:
            self._normalize_raw_topo()
        elif 'site_config' in self.config:
            self._generate_topology()
        else:
            LOGGER.error('No valid generator options found')
            return -1
        return 0

    def _normalize_raw_topo(self):
        source_dir = self.config.get('raw_topo')
        assert source_dir, 'raw_topo not defined'
        topo_dir = self.config.get('topo_dir', 'inst')
        if not os.path.exists(topo_dir):
            os.makedirs(topo_dir)
        for filename in os.listdir(source_dir):
            if not filename.endswith(self._YAML_POSTFIX):
                LOGGER.info('Skipping non-yaml file %s', filename)
                continue
            in_path = os.path.join(source_dir, filename)
            loaded_yaml = self._load_config(in_path)
            self._write_yaml(topo_dir, filename, loaded_yaml)

    def _generate_topology(self):
        setup_config = 'topology/setup.json'
        self._setup = self._load_config(setup_config)
        site_config = self.config.get('site_config')
        assert site_config, 'site_config not defined'
        self._site = self._load_config(site_config)
        for domain in self._get_all_domains():
            target = self._make_target(domain)
            self._write_config(target, 'faucet.yaml', self._make_faucet())
            self._write_config(target, 'gauge.yaml', self._make_gauge(domain))
            self._write_config(target, 'dps.yaml', self._make_dps(domain))
            self._write_config(target, 'vlans.yaml', self._make_vlans())

    def _load_config(self, path):
        LOGGER.info('Loading %s', path)
        with open(path) as stream:
            return yaml.safe_load(stream)

    def _write_config(self, target, filename, data):
        topo_base = self.config.get('topo_dir')
        topo_dir = os.path.join(topo_base, self._get_ctl_name(target))
        self._write_yaml(topo_dir, filename, data)

    def _write_yaml(self, topo_dir, filename, data):
        if not os.path.exists(topo_dir):
            os.makedirs(topo_dir)
        out_path = os.path.join(topo_dir, filename)
        LOGGER.info('Writing output file %s', out_path)
        with open(out_path, 'w') as stream:
            yaml.safe_dump(data, stream=stream)

    def _get_all_domains(self):
        return self._site['tier1']['domains'].keys()

    def _make_target(self, domain):
        domain_dp = self._site['tier1']['domains'][domain]
        return {
            'floor': domain_dp['floor'],
            'idf': domain_dp['idf'],
            'domain': domain
        }

    def _make_faucet(self):
        return {
            'include': ['vlans.yaml', 'dps.yaml'],
            'version': 2
        }

    def _make_gauge(self, domain):
        db_type = self._setup['gauge']['db_type']
        return {
            'dbs': {
                db_type: self._setup['db_types'][db_type]
            },
            'faucet_configs': [self._setup['faucet_yaml']],
            'watchers': {
                'flow_table': self._make_gauge_watcher(domain, 'flow_table'),
                'port_stats': self._make_gauge_watcher(domain, 'port_stats')
            }
        }

    def _make_gauge_watcher(self, domain, watcher_type):
        return {
            'db': self._setup['gauge']['db_type'],
            'interval': self._setup['gauge']['interval'],
            'type': watcher_type,
            'dps': self._get_all_dp_names(domain)
        }

    def _get_all_dp_names(self, domain):
        t1_dp_names = list(self._make_t1_dps(domain).keys())
        t2_dp_names = list(self._make_t2_dps(domain).keys())
        return t1_dp_names + t2_dp_names

    def _get_t1_dp_name(self, domain):
        t1_conf = self._site['tier1']['domains'][domain]
        return self._get_dp_name('t1', t1_conf['floor'], t1_conf['idf'], domain)

    def _get_t2_dp_name(self, t2_conf):
        return self._get_dp_name('t2', t2_conf['floor'], t2_conf['idf'], t2_conf['domain'])

    def _make_t1_dps(self, domain):
        t1_defaults = self._site['tier1']['defaults']
        t1_conf = self._site['tier1']['domains'][domain]
        dp_name = self._get_t1_dp_name(domain)
        return {
            dp_name: {
                'dp_id': t1_conf['dp_id'],
                'hardware': t1_defaults['hardware'],
                'lldp_beacon': self._get_switch_lldp_beacon(),
                'name': dp_name,
                'interfaces': self._make_t1_dp_interfaces(t1_conf, domain),
                'stack': {
                    'priority': 1
                }
            }
        }

    def _make_t1_dp_interfaces(self, t1_conf, domain):
        interfaces = {}
        corp_port = self._site['tier1']['defaults']['corp_port']
        interfaces.update({corp_port: self._make_corp_interface(t1_conf['uplink'])})
        interfaces.update(self._make_t1_stack_interfaces(domain))
        interfaces.update(self._make_t1_device_interfaces())
        return interfaces

    def _make_corp_interface(self, uplink):
        interface = {}
        interface.update(self._setup['corp_iface'])
        interface['name'] = uplink
        interface['tagged_vlans'] = [self._site['vlan_id']]
        return interface

    def _make_t1_device_interfaces(self):
        device_port = self._site['tier1']['defaults']['device_port']
        return {
            device_port: self._make_device_interface()
        }

    def _make_device_interface(self):
        return {
            'description': self._setup['device_description'],
            'native_vlan': self._site['vlan_id']
        }

    def _make_t1_stack_interfaces(self, domain):
        interfaces = {}
        tier1_ports = self._site['tier2']['tier1_ports']
        for tier1_port in tier1_ports:
            tier2_spec = tier1_ports[tier1_port]
            if tier2_spec['domain'] is domain:
                interfaces.update({
                    tier1_port:self._make_t1_stack_interface(tier2_spec)
                })
            else:
                interfaces.update({
                    tier1_port:self._make_t2_external_interface()
                })

        return interfaces

    def _make_t1_stack_interface(self, tier2_spec):
        port = tier2_spec['uplink_port']
        dp_name = self._get_t2_dp_name(tier2_spec)
        return {
            'lldp_beacon': self._get_port_lldp_beacon(),
            'stack': {
                'dp': dp_name,
                'port': port
            }
        }

    def _make_t2_external_interface(self):
        return {
            'lldp_beacon': self._get_port_lldp_beacon(),
            'loop_protect_external': self._setup['loop_protect_external'],
            'tagged_vlans': [self._site['vlan_id']]
        }

    def _make_t2_dps(self, domain):
        dps = {}
        t1_ports = self._site['tier2']['tier1_ports']
        for t1_port in t1_ports:
            t2_conf = t1_ports[t1_port]
            if t2_conf['domain'] == domain:
                dp_name = self._get_dp_name('t2', t2_conf['floor'], t2_conf['idf'], domain)
                dps.update({dp_name: self._make_t2_dp(t2_conf, t1_port)})
        return dps

    def _make_t2_dp(self, t2_conf, t1_port):
        t2_defaults = self._site['tier2']['defaults']
        dp_name = self._get_t2_dp_name(t2_conf)
        return {
            'dp_id': t2_conf['dp_id'],
            'hardware': t2_defaults['hardware'],
            'lldp_beacon': self._get_switch_lldp_beacon(),
            'interface_ranges': self._make_t2_interface_ranges(),
            'interfaces': self._make_t2_interfaces(t2_conf, t1_port),
            'name': dp_name,
        }

    def _make_t2_interface_ranges(self):
        interface_ranges = self._site['tier2']['defaults']['device_ports']
        return {interface_ranges: self._make_device_interface()}

    def _make_t2_interfaces(self, t2_conf, t1_port):
        return {
            t2_conf['uplink_port']: self._make_t2_stack_interface(t2_conf, t1_port),
            t2_conf['alternate_port']: self._make_t2_external_interface()
        }

    def _make_t2_stack_interface(self, t2_conf, t1_port):
        t1_dp_name = self._get_t1_dp_name(t2_conf['domain'])
        return {
            'lldp_beacon': self._get_port_lldp_beacon(),
            'stack': {
                'dp': t1_dp_name,
                'port': t1_port
            }
        }

    def _get_switch_lldp_beacon(self):
        return copy.deepcopy(self._setup['switch_lldp_beacon'])

    def _get_port_lldp_beacon(self):
        return copy.deepcopy(self._setup['port_lldp_beacon'])

    def _get_dp_name(self, dp_type, floor, idf, domain):
        site_name = self._site['site_name']
        return self._setup['dp_name_format'] % (site_name, dp_type, floor, idf, domain)

    def _get_ctl_name(self, target):
        site_name = self._site['site_name']
        format_tuple = (site_name, target['floor'], target['idf'], target['domain'])
        return self._setup['ctl_name_format'] % format_tuple

    def _make_vlans(self):
        return {
            'version': 2,
            'vlans': {
                self._setup['vlan']['name']: {
                    'description': self._setup['vlan']['description'],
                    'vid': self._site['vlan_id']
                }
            }
        }

    def _make_dps(self, domain):
        return {
            'version': 2,
            'dps': {**self._make_t1_dps(domain), **self._make_t2_dps(domain)}
        }



if __name__ == '__main__':
    sys.exit(TopologyGenerator(DAQ(sys.argv)).process())
