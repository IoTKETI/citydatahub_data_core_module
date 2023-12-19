#!/bin/bash

echo "Initialize Schema!!"

cp /usr/local/spark/conf/hive-site.xml ${HIVE_CONF_DIR}
cp /usr/local/spark/conf/core-site.xml ${HADOOP_HOME}/etc/hadoop/
cp /usr/local/spark/conf/hdfs-site.xml ${HADOOP_HOME}/etc/hadoop/
cp /usr/local/spark/conf/yarn-site.xml ${HADOOP_HOME}/etc/hadoop/
cp /usr/local/spark/jars/postgresql-42.2.19.jar ${HIVE_HOME}/lib/
cp /usr/local/spark/jars/postgresql-42.2.19.jar ${HIVE_HOME}/jdbc/

${HIVE_HOME}/bin/schematool -initSchema -dbType postgres

${THRIFT_HOME}/bin/thrift-server.sh start

sleep 2

tail -f ${SPARK_HOME}/logs/*.out