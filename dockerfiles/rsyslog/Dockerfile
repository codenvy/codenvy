FROM alpine:3.4

RUN  apk add --update rsyslog \
  && rm -rf /var/cache/apk/*

EXPOSE 514 514/udp

VOLUME [ "/var/log", "/etc/rsyslog.d" ]

COPY rsyslog.conf /etc/rsyslog.conf

COPY haproxy.conf /etc/rsyslog.d/haproxy.conf

RUN mkdir -p /var/log/haproxy

ENTRYPOINT [ "rsyslogd", "-n" ]
