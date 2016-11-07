#! /bin/bash
#
# Start postfix
#
postfix -c /etc/postfix start
#
# Get haproxy container IP and add it to hosts to make sure
# all requests internally go through haproxy container
#

haproxy=$(getent hosts haproxy | awk '{ print $1 }')

echo "$haproxy $CODENVY_API_END_POINT" >> /etc/hosts

#
# Check if Postgres is running. There's a 5 min timeout
# for Postgres to start. If it does not Codenvy Tomcat
# never starts, i.e. CMD is never executed
#

if [[ -z "$PGUSER" || -z "$PGPASSWORD" ]]; then

echo "Postgres variables are not set. Exiting"
echo "Environment variables have the following values:"
echo "User: $PGUSER"
echo "Password: $PGPASSWORD"
exit
fi

echo "Waiting for Postgres to boot..."

MAX_TRIES=60 
COUNT=0
    while [ $COUNT -lt $MAX_TRIES ]; do
    psql -h postgres -d template1 -c '\l'
	if [ $? -eq 0 ]; then
    	echo '[INFO]: Postgres is up and running. Starting Codenvy Tomcat...'
    	break
	fi
	    let COUNT=COUNT+1
	    echo '[INFO]: Postgres is unavailable. Trying in 5 seconds...'
	    sleep 5
               if [ "$COUNT" -eq "60" ]; then
                  echo "[ERROR]: Postgres failed to start in 5 minutes. Check Postgres container logs. Codenvy Tomcat start canceled."
                  exit
               fi
         done
exec "$@"
