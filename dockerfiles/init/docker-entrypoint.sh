#!/bin/sh

# Make sure service is running
cp -rf /files/manifests /copy
cp -rf /files/modules /copy
cp -rf /files/docs /copy
# do not copy codenvy.env if exist
if [ ! -f  /copy/codenvy.env ]; then
    cp /files/codenvy.env /copy
fi
