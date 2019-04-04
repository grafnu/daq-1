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

pointer_list_line_end = []

def find_char_pointer(value, find_text, pointer_list):
    pointer = 0
    while pointer != -1:
        pointer = value.find(find_text,pointer)
        if pointer == -1:
            break
        else:
            #print('pointer:' , pointer)
            pointer_list.append(pointer)
        pointer += 1

packet_request_list = []

def cut_lines_to_list(request_list,pointer_list):
    last_point = 0
    for point in pointer_list:
        request_list.append(shell_result[last_point:point])
        last_point = point + 1
        #print(point)

shell_result = shell_command_fb(tcpdump_display_all_packets, 0, False)

find_char_pointer(shell_result, '\n', pointer_list_line_end)

cut_lines_to_list(packet_request_list,pointer_list_line_end)

packets_received = len(packet_request_list)

file_open = open(report_filename, 'w')

if len(shell_result) > 40:
    file_open.write('packets_received:' + str(packets_received)  + '\n')
    file_open.write('network.min_send=true\n')
else:
    file_open.write('network.min_send=false\n')

file_open.close()
