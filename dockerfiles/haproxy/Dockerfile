# Copyright (c) 2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# build:
#   docker build -t codenvy/haproxy:<version> .
#
# use:
#    docker run codenvy/haproxy:<version>

FROM alpine:3.4

ENV HAPROXY_MAJOR 1.5
ENV HAPROXY_VERSION 1.5.18
ENV HAPROXY_MD5 21d35f114583ef731bc96af05b46c75a

# see http://sources.debian.net/src/haproxy/1.5.8-1/debian/rules/ for some helpful navigation of the possible "make" arguments
RUN set -x \
    && apk add --no-cache --virtual .build-deps \
	curl \
	gcc \
	libc-dev \
	linux-headers \
	make \
	openssl-dev \
	pcre-dev \
	zlib-dev \
    && curl -SL "http://www.haproxy.org/download/${HAPROXY_MAJOR}/src/haproxy-${HAPROXY_VERSION}.tar.gz" -o haproxy.tar.gz \
    && echo "${HAPROXY_MD5}  haproxy.tar.gz" | md5sum -c \
    && mkdir -p /usr/src \
    && tar -xzf haproxy.tar.gz -C /usr/src \
    && mv "/usr/src/haproxy-$HAPROXY_VERSION" /usr/src/haproxy \
    && rm haproxy.tar.gz \
    && make -C /usr/src/haproxy \
	TARGET=linux2628 \
	USE_PCRE=1 PCREDIR= \
	USE_OPENSSL=1 \
	USE_ZLIB=1 \
	all \
	install-bin \
    && mkdir -p /usr/local/etc/haproxy \
    && cp -R /usr/src/haproxy/examples/errorfiles /usr/local/etc/haproxy/errors \
    && rm -rf /usr/src/haproxy \
    && runDeps="$( \
	scanelf --needed --nobanner --recursive /usr/local \
	    | awk '{ gsub(/,/, "\nso:", $2); print "so:" $2 }' \
	    | sort -u \
	    | xargs -r apk info --installed \
	    | sort -u \
    )" \
    && apk add --virtual .haproxy-rundeps $runDeps \
    && apk del .build-deps
RUN apk add --update rsyslog
RUN sed -i 's/#$ModLoad imudp/$ModLoad imudp/g' /etc/rsyslog.conf && \
    sed -i 's/#$UDPServerRun 514/$UDPServerRun 514/g' /etc/rsyslog.conf
COPY entrypoint.sh /
RUN chmod +x entrypoint.sh && mkdir -p /etc/rsyslog.d /service/rsyslog
ADD haproxy.conf /etc/rsyslog.d/haproxy.conf
ADD rsyslog.conf /etc/rsyslog.conf
EXPOSE 80 443
ENTRYPOINT ["/entrypoint.sh"]
