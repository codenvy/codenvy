#!/bin/bash

set -e  # tells bash that it should exit the script if any statement returns value > 0

dollar_symbol='$'

CURRENT_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
LOG_FILE="$CODENVY_IM_BASE/migration.log"
rm -f "$LOG_FILE"


# Fix locations in PostgreSQL
echo >> "$LOG_FILE"
echo "Fix locations in PostgreSQL..." >> "$LOG_FILE"

sudo su - postgres -c "psql dbcodenvy << EOF
UPDATE environment
SET location=regexp_replace(location, '${host_protocol}://${host_url}/api', '')
EOF" >> "$LOG_FILE"
