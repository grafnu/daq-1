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
tcpdump_display_eapol_packets = 'tcpdump port 1812 or port 1813 or port 3799 ' + cap_pcap_file
tcpdump_display_umb_packets = 'tcpdump -n ether broadcast and ether multicast ' + cap_pcap_file

tests = {
    'connection.min_send' : tcpdump_display_all_packets,
    'protocol.app_min_send' : tcpdump_display_udp_bacnet_packets, 
    'connection.dhcp_long' : tcpdump_display_arp_packets, 
    'network.ntp.update' : tcpdump_display_ntp_packets,
    'security.network.802_1x' : tcpdump_display_eapol_packets,
    'communication.type' : tcpdump_display_umb_packets
}

def write_report(string_to_append):
    with open(report_filename, 'a+') as file_open:
        file_open.write(string_to_append)

def shell_command_with_result(command, wait_time, terminate_flag):
    process = subprocess.Popen(command, universal_newlines=True, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    text = process.stdout.read()
    retcode = process.wait()
    time.sleep(wait_time)
    if terminate_flag:
        process.terminate()
    if len(text) > 0:
        return text

def validate_test():
    max = 0
    if packets_received > packets_in_report :
        max = packets_in_report
    else:
        max = packets_received
    for i in range(0, max):
        write_report(packet_request_list[i] + '\n')
    write_report('packets_sent=' + str(packets_received)  + '\n')
    write_report("RESULT pass %s\n" % test_request)

shell_result = shell_command_with_result(tests[test_request], 0, False)

if not shell_result is None:
    if len(shell_result) > min_packet_length:
        packet_request_list = shell_result.split("\n")
        packets_received = len(packet_request_list)
        validate_test()
else:
    write_report("RESULT fail %s\n" % test_request)