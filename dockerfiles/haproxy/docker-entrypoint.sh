#!/bin/sh

# Make sure service is running
exec rsyslogd -n &

# Touch the log file so we can tail on it
touch /var/log/haproxy/haproxy.log

# Throw the log to output
tail -f /var/log/haproxy/haproxy.log &

# Start haproxy
exec haproxy -f /usr/local/etc/haproxy/haproxy.cfg
