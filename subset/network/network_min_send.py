import subprocess
import time
import sys

arguments = sys.argv

device_address = str(arguments[1])

report_filename = 'report.txt'
cap_pcap_file = 'capture.pcap'
header_cmd = 'tcpdump '

tcpdump_display_all_packets = header_cmd + '-n src host ' + device_address + ' -r ' + cap_pcap_file

def shell_command_fb(command, wait_time, terminate_flag):
    process = subprocess.Popen(command, universal_newlines=True, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    text = process.stdout.read()
    retcode = process.wait()
    time.sleep(wait_time)
    if terminate_flag:
        process.terminate()
    if len(text) > 0:
        return text

shell_result = shell_command_fb(tcpdump_display_all_packets, 0, False)

file_open = open(report_filename, 'w')

if len(shell_result) > 40:
    file_open.write('network.min_send=true\n')
else:
    file_open.write('network.min_send=false\n')

file_open.close()
