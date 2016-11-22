
# Initializes an empty directory with the templates needed to configure and run Codenvy.
#
# Build:
#   cd ..\..\ <-- Root of the codenvy source repository
#   docker build -f dockerfiles/init/Dockerfile -t codenvy/init:<version> .
#
# Use:
#    docker run -v $(pwd):/copy codenvy/init:<version>
FROM alpine:latest
RUN mkdir -p /files \
    && mkdir -p /files/modules \
    && mkdir -p /files/manifests \
    && mkdir -p /files/docs

COPY manifests /files/manifests
COPY modules /files/modules
COPY docs /files/docs
COPY dockerfiles/init/docker-entrypoint.sh /
RUN chmod +x docker-entrypoint.sh
ENTRYPOINT ["/docker-entrypoint.sh"]
