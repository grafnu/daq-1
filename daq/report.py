"""Device report handler"""

import datetime
import logging
import os
import pytz
import shutil
import time

LOGGER = logging.getLogger('report')

class ReportGenerator():
    """Generate a report for device qualification"""

    _NAME_FORMAT = "report_%s_%s.txt"

    def __init__(self, path_base, target_mac):
        self._target_mac = target_mac
        report_when = datetime.datetime.now(pytz.utc).replace(microsecond=0)
        report_filename = self._NAME_FORMAT % (self._target_mac.replace(':', ''),
                                               report_when.isoformat().replace(':', ''))
        report_path = os.path.join(path_base, report_filename)
        LOGGER.info('Creating report as %s', report_path)
        self.path = report_path
        if not os.path.isdir(path_base):
            os.makedirs(path_base)
        self._file = open(report_path, "w")
        self.write('DAQ scan report for device %s' % self._target_mac)
        self.write('Started %s' % report_when)

    def write(self, msg):
        """Write a message to a report file"""
        self._file.write(msg + '\n')
        self._file.flush()

    def copy(self, input_path):
        """Copy an input file to the report"""
        with open(input_path, 'r') as input_stream:
            shutil.copyfileobj(input_stream, self._file)
        self._file.flush()

    def finalize(self):
        LOGGER.info('Finalizing report %s', self.path)
        self.write('Report complete.')
        self._file.close()
        self._file = None
