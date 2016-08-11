#!/bin/bash

# tells bash that it should exit the script if any statement returns value > 0
set -e

CURRENT_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
LOG_FILE=migration.log
rm -f $LOG_FILE
echo "Migration scripts are situated in directory '$CURRENT_DIR'" >> $LOG_FILE

#### fix mongoDB
echo "=========== fix mongoDB ======" >> $LOG_FILE

mongo -u$mongo_admin_user_name -p$mongo_admin_pass --authenticationDatabase admin "${CURRENT_DIR}/update_mongo.js" &>> $LOG_FILE
