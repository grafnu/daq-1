import subprocess
import time
import sys

arguments = sys.argv

test_request = str(arguments[1])
cap_pcap_file = str(arguments[2])
device_address = str(arguments[3])

report_filename = 'report.txt'

min_packet_length = 40
packets_in_report = 10

tcpdump_display_all_packets = 'tcpdump -n src host ' + device_address + ' -r ' + cap_pcap_file
tcpdump_display_udp_bacnet_packets = 'tcpdump -n udp dst portrange 47808-47809 ' + cap_pcap_file
tcpdump_display_arp_packets = 'tcpdump -v arp -r ' + cap_pcap_file
tcpdump_display_ntp_packets = 'tcpdump dst port 123 -r ' + cap_pcap_file

tests = {
'network.min_send' : tcpdump_display_all_packets,
'network.application.min_send' : tcpdump_display_udp_bacnet_packets, 
'dhcp.long' : tcpdump_display_arp_packets, 
'ntp.update' : tcpdump_display_ntp_packets
}

pointer_list_line_end = []
packet_request_list = []

def shell_command_with_result(command, wait_time, terminate_flag):
    process = subprocess.Popen(command, universal_newlines=True, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    text = process.stdout.read()
    retcode = process.wait()
    time.sleep(wait_time)
    if terminate_flag:
        process.terminate()
    if len(text) > 0:
        return text

def find_char_pointer(value, find_text, pointer_list):
    pointer = 0
    while True:
        pointer = value.find(find_text, pointer)
        if pointer == -1:
            break
        else:
            pointer_list.append(pointer)
        pointer += 1

def cut_packets_to_list(request_list, pointer_list):
    last_point = 0
    for point in pointer_list:
        request_list.append(shell_result[last_point:point])
        last_point = point + 1

def validate_test(id):
    max = 0
    if packets_received > packets_in_report :
        max = packets_in_report
    else:
        max = packets_received
    for i in range(0, max):
        file_open.write(packet_request_list[i] + '\n')
    file_open.write('packets_sent=' + str(packets_received)  + '\n')
    file_open.write(tests[test_request] + '=true\n')

shell_result = shell_command_with_result(tests[test_request], 0, False)
file_open = open(report_filename, 'w')

if not shell_result is None:
    if len(shell_result) > min_packet_length:
        find_char_pointer(shell_result, '\n', pointer_list_line_end)
        cut_packets_to_list(packet_request_list,pointer_list_line_end)
        packets_received = len(packet_request_list)
        validate_test(test_request)
else:
    file_open.write(tests[test_request] + '=false\n')

file_open.close()

