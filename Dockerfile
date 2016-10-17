FROM codenvy/jdk
ADD assembly/onpremises-ide-packaging-tomcat-codenvy-allinone/target/onpremises-ide-packaging-tomcat-codenvy-allinone-*.zip /
RUN unzip -q onpremises-ide-packaging-tomcat-codenvy-allinone-*.zip -d /opt/codenvy-tomcat && rm onpremises-ide-packaging-tomcat-codenvy-allinone-*.zip
CMD /opt/codenvy-tomcat/bin/catalina.sh ${JPDA} run