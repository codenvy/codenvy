---
title: Email
excerpt: "Configure external SMTP server to use for sending emails initiated by Codenvy."
layout: docs
overview: true
permalink: /docs/smtp/
---
#### Failed Codenvy Emails
If you perform a fresh installation of Codenvy and you do not receive notification emails from Codenvy, such as the verification email for a new user, then the likely cause is that your ISP is blocking outbound SMTP email on the default port.  Consider sending email through a relay host.  


# Essential Files  


| File   | Purpose   
| --- | --- 
| `/etc/postfix`   | Directory for configuring Postfix SMTP server. Used as a default SMTP server and installed with Codenvy if another is not configured.   
| `/etc/puppet/modules/all_in_one/templates/email-connection.properties.erb`   |    
| Configuration for external SMTP server.   | `/etc/postfix/sasl_passwd`   
| Configure user name and password if you want Codenvy emails to be sent through another relay host.   | `/etc/postfix/main.cf`   


# Configuration  
There are a number of events that will trigger Codenvy sending an email to an administrator or a user.  For example, new users created from the self-service forms will be sent a verification email.  Users invited to join your Codenvy workspace will be sent an invitation email.

Codenvy will use the SMTP configuration provided in the `email-connection.properties.erb` file. If we do not detect an SMTP server, we use an embedded Postfix. You can configure Postfix at `/etc/postfix/main.cf`.

Otherwise you can configure an external SMTP server by modifying the Codenvy system properties. This is an example for gmail:
```ruby  
mail.host=smtp.gmail.com
mail.port=465
mail.transport.protocol=smtp
mail.smtp.auth=true
 
mail.smtp.auth.username=YOUR_GMAIL_LOGIN@gmail.com
mail.smtp.auth.password=YOUR_GMAIL_PASSWORD
mail.smtp.socketFactory.port=465
mail.smtp.socketFactory.class=javax.net.ssl.SSLSocketFactory
mail.smtp.socketFactory.fallback=false\
```
As with any Codenvy property change, tell Puppet to reconfigure the system:
```shell  
puppet agent -t\
```

# Relay Servers  
There are tutorials online for configuring Postfix with different providers:
1. [Gmail](https://charlesauer.net/tutorials/centos/postfix-as-gmail-relay-centos.php)
2. [Mandrillapp](https://mandrill.zendesk.com/hc/en-us/articles/205582187-How-to-Use-Postfix-to-Send-Email-with-Mandrill)
3. [Sendgrid](https://sendgrid.com/docs/Integrate/Mail_Servers/postfix.html)
# Postfix Mandrillapp Example  
You can bypass the Codenvy configuration of Postfix and modify it directly. For example, Comcast ISP prevents outbound email over the standard port 25.  This is how to configure Codenvy's email to send through Mandrillapp over secure port 587. Start by inserting your user name and API key:
```text  
[smtp.mandrillapp.com]:587 USERNAME:APIKEY\
```
Convert this to a postmap file:
```shell  
chmod 600 /etc/postfix/sasl_passwd
postmap /etc/postfix/sasl_passwd\
```
Update the Postfix configuration:
```julia  
relayhost = [smtp.mandrillapp.com]:587
smtp_sasl_auth_enable = yes
smtp_sasl_password_maps = hash:/etc/postfix/sasl_passwd
smtp_sasl_security_options = noanonymous
smtp_tls_CAfile = /etc/pki/tls/certs/ca-bundle.crt
smtp_use_tls = yes\
```
Restart Postfix:
```shell  
service postfix restart
service postfix status\
```
