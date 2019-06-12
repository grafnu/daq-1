# Used for testing of device and type customization mappings in test_aux.sh
cp /config/device/snake.txt /tmp/
cp /config/type/lizard.txt /tmp/

# Sleep for 5 minutes to let long-lease dhcp discovery happen.
sleep_time=$((5*60))
echo Sleeping for ${sleep_time}s to wait for DHCP.
sleep $sleep_time
