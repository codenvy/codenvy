
# Contains a manifest of Docker images required to run Codenvy for a particular version.
# Also contains an upgrade summary showing which upgrade paths a particular version is allowed.
# This image is only tagged as "latest".  It is a single image that provides starting point of info.
#
# Build:
#   cd ..\..\ <-- Root of the codenvy source repository
#   docker build -f dockerfiles/version/Dockerfile -t codenvy/version .
# 
# Use:
#    docker run -v $(pwd):/copy codenvy/version
FROM alpine:3.4
RUN mkdir -p /files 

COPY version /files/
COPY dockerfiles/version/docker-entrypoint.sh /

RUN chmod +x docker-entrypoint.sh 
ENTRYPOINT ["/docker-entrypoint.sh"]
