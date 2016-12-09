---
title: Security
excerpt: ""
layout: docs
overview: true
permalink: /docs/security/
---
# Switching to HTTPS  
By default Codenvy runs over HTTP as this is simplest to install. You can switch to HTTPS at any time.

## Create A Certificate Chain
Do not use self-signed certs as these typically cause problems. If you need a signed certificate for a POC you can use a free certificate authority like [startssl.com](startssl.com).

## Create A PEM File
Create a new file called `cert.pem` and paste the following in order:
1. Contents of the domain cert
2. Contents of the intermediate cert
3. Private key

Copy this file to `/etc/haproxy` and set the ownership and permissions:
```shell  
sudo chmod 0600 /etc/haproxy/cert.pem

sudo chown root:root /etc/haproxy/cert.pem\
```
## Edit Codenvy Properties
Use your favourite editor to edit the `/etc/puppet/manifests/nodes/codenvy/codenvy.pp` file. Search for `$host_protocol` and make the following change:
```shell  
$host_protocol = "https"
$path_to_haproxy_ssl_certificate = /etc/haproxy/cert.pem
```
In the same file search for `$machine_ws_agent_run_command`. Comment out the existing line and add a new one below it:
```shell  
$machine_ws_agent_run_command = "wget -q https://www.startssl.com/certs/ca.crt -O /tmp/ca.crt && sudo /opt/jdk1.8.0_45/bin/keytool -import -trustcacerts -keystore /opt/jdk1.8.0_45/jre/lib/security/cacerts -storepass changeit -noprompt -alias codenvy_dev.ca -file /tmp/ca.crt 2> /dev/null; sleep 5 && mkdir -p ~/che && rm -rf ~/che/* && mkdir -p ~/che/ws-agent && tar -xzf /mnt/che/ws-agent.tar.gz -C ~/che/ws-agent && export JPDA_ADDRESS=\"4403\" && ~/che/ws-agent/bin/catalina.sh run"
```
Save and close the `codenvy.pp` file.

## Add Certificate to Java Trust Store
If you're using a 3rd party signed certificate, Java will automatically respect it.

If you are using a self-signed certificate, you need to add the certificate to Java by running these two commands on the Codenvy node:
```shell  
# Replace "www.startssl.com" with your certificate provider
wget -q https://www.startssl.com/certs/ca.crt -O /tmp/ca.crt

sudo /usr/local/jdk1.8.0_45/bin/keytool -import -trustcacerts -keystore /usr/local/jdk1.8.0_45/jre/lib/security/cacerts -storepass changeit -noprompt -alias codenvy_dev.ca -file /tmp/ca.crt
```
## Check Ports
Ensure that ports 443 and 444 are open.

## Restart Codenvy
```shell  
sudo service codenvy restart\
```
## Check Codenvy
Check your install by accessing the dashboard from an incognito browser window. If you are able to log in and the user dashboard appears correctly then you're done.

