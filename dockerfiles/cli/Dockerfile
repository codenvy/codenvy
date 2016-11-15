
# Contains the Codenvy CLI.
#
# Build:
#   docker build -t codenvy/cli .
# 
# Use:
#    docker run -v $(pwd):/codenvy codenvy/cli [command]
FROM alpine:3.4
RUN mkdir -p /codenvy \
    && mkdir -p /version \
    && mkdir -p /cli \
    && apk add --no-cache ca-certificates curl openssl \
    && apk add --update bash \
    && rm -rf /var/cache/apk/*

ENV DOCKER_BUCKET get.docker.com
ENV DOCKER_VERSION 1.11.2
ENV DOCKER_SHA256 8c2e0c35e3cda11706f54b2d46c2521a6e9026a7b13c7d4b8ae1f3a706fc55e1
#ENV DOCKER_COMPOSE_BUCKET github.com
#ENV DOCKER_COMPOSE_VERSION 1.8.1

RUN set -x \
	&& curl -fSL "https://${DOCKER_BUCKET}/builds/Linux/x86_64/docker-${DOCKER_VERSION}.tgz" -o docker.tgz \
	&& echo "${DOCKER_SHA256} *docker.tgz" | sha256sum -c - \
	&& tar -xzvf docker.tgz \
	&& mv docker/* /usr/local/bin/ \
	&& rmdir docker \
	&& rm docker.tgz \
	&& docker -v

#RUN set -x \
#  && curl -L https://${DOCKER_COMPOSE_BUCKET}/docker/compose/releases/download/1.8.1/run.sh > /usr/local/bin/docker-compose; chmod +x /usr/local/bin/docker-compose

COPY codenvy.sh /cli/
COPY cli.sh /cli/
COPY entrypoint.sh /
COPY version /version/

RUN chmod +x entrypoint.sh \
    && chmod +x /cli/codenvy.sh

ENTRYPOINT ["/cli/codenvy.sh"]



