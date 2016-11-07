# Codenvy image build
#
# Build:
#   cd ..\..\ <-- Root of the codenvy source repository
#   docker build -f dockerfiles/codenvy/Dockerfile -t codenvy/codenvy:<version> .
#
FROM codenvy/jdk
ENV DOCKER_BUCKET get.docker.com
ENV DOCKER_VERSION 1.6.0

RUN set -x \
  && curl -sL "https://${DOCKER_BUCKET}/builds/Linux/x86_64/docker-$DOCKER_VERSION" \
  > /usr/bin/docker; chmod +x /usr/bin/docker
ADD assembly/onpremises-ide-packaging-tomcat-codenvy-allinone/target/onpremises-ide-packaging-tomcat-codenvy-allinone-*.zip /
RUN unzip -q onpremises-ide-packaging-tomcat-codenvy-allinone-*.zip -d /opt/codenvy-tomcat && rm onpremises-ide-packaging-tomcat-codenvy-allinone-*.zip
CMD /opt/codenvy-tomcat/bin/catalina.sh ${JPDA} run
