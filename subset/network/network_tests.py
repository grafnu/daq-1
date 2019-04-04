import subprocess
import time
import sys

name_of_tests = ['network.min_send', 'network.application.min_send', 'dhcp.long', 'ntp.update']

arguments = sys.argv

test_request = str(arguments[1])

test_id = -1

for x in range(0, len(name_of_tests)):
    if test_request == name_of_tests[x]:
        test_id = x

print('test_id=' + str(test_id))

cap_pcap_file = str(arguments[2])
#capture.pcap

device_address = '127.0.0.1'

if test_id == 0:
    device_address = str(arguments[3])

report_filename = 'report.txt'

min_packet_length = 40
packets_in_report = 10

tcpdump_display_all_packets = 'tcpdump -n src host ' + device_address + ' -r ' + cap_pcap_file
tcpdump_display_udp_bacnet_packets = 'tcpdump -n udp dst portrange 47808-47809 ' + cap_pcap_file
tcpdump_display_arp_packets = 'tcpdump -v arp -r ' + cap_pcap_file
tcpdump_display_ntp_packets = 'tcpdump dst port 123 -r ' + cap_pcap_file

tests = [tcpdump_display_all_packets, tcpdump_display_udp_bacnet_packets, tcpdump_display_arp_packets, tcpdump_display_ntp_packets]

def shell_command_fb(command, wait_time, terminate_flag):
    process = subprocess.Popen(command, universal_newlines=True, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    text = process.stdout.read()
    retcode = process.wait()
    time.sleep(wait_time)
    if terminate_flag:
        process.terminate()
    if len(text) > 0:
        return text

pointer_list_line_end = []

def find_char_pointer(value, find_text, pointer_list):
    pointer = 0
    while pointer != -1:
        pointer = value.find(find_text, pointer)
        if pointer == -1:
            break
        else:
            pointer_list.append(pointer)
        pointer += 1

packet_request_list = []

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
    i = 0
    while i < max :
        file_open.write(packet_request_list[i] + '\n')
        i += 1
    file_open.write('packets_sent=' + str(packets_received)  + '\n')
    file_open.write(name_of_tests[id] + '=true\n')

shell_result = shell_command_fb(tests[test_id], 0, False)
file_open = open(report_filename, 'w')

if not shell_result is None:
    if len(shell_result) > min_packet_length:
        find_char_pointer(shell_result, '\n', pointer_list_line_end)
        cut_packets_to_list(packet_request_list,pointer_list_line_end)
        packets_received = len(packet_request_list)
        validate_test(test_id)
else:
    file_open.write(name_of_tests[test_id] + '=false\n')

file_open.close()

