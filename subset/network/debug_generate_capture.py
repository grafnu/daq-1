import subprocess
import time
import sys

arguments = sys.argv

capture_time = int(arguments[1])
eth_interface = arguments[2]

cap_pcap_file = 'capture.pcap'
header_cmd = 'tcpdump '

tcpdump_capture_unlimited_byte_packets = header_cmd + '-i ' + eth_interface  + ' -s0 -w ' + cap_pcap_file

def shell_command_without_result(command, wait_time, terminate_flag):
    process = subprocess.Popen(command, universal_newlines=True, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    time.sleep(wait_time)
    if terminate_flag:
        process.terminate()

shell_command_without_result(tcpdump_capture_unlimited_byte_packets, capture_time, True)
