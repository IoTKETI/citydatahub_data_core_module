# 2.2 Hadoop Configuration Setting

- Open the environment variable settings file using the editor with `vi ~/.bash_profile` and add the following:
  ```
  # Set JAVA_HOME
  export JAVA_HOME=/usr/lib/jvm/jre-1.8.0-openjdk
  
  # Set Hive Path
  export HIVE_HOME=/usr/local/hive
  export HIVE_CONF_DIR=/usr/local/hive/conf
  
  # Set Spark Path
  export SPARK_HOME=/usr/local/spark
  export PYTHONPATH=$SPARK_HOME/python/:$PYTHONPATH$
  
  # Set Hadoop-related Path
  export HADOOP_PREFIX=/usr/local/hadoop
  export HADOOP_HOME=/usr/local/hadoop
  export HADOOP_MAPRED_HOME=${HADOOP_HOME}
  export HADOOP_COMMON_HOME=${HADOOP_HOME}
  export HADOOP_HDFS_HOME=${HADOOP_HOME}
  export YARN_HOME=${HADOOP_HOME}
  export HADOOP_COMMON_LIB_NATIVE_DIR=${HADOOP_HOME}/lib/native
  export HADOOP_OPT="-Djava.library.path=$HADOOP_PREFIX/lib/native"
  export HADOOP_CONF_DIR=/home/centos/jdbc-thrift/yarn-conf
  
  # Add bin folder path for each Hadoop component to PATH
  export PATH=$PATH:$JAVA_HOME/bin
  export PATH=$SPARK_HOME/bin:$PATH
  export PATH=$HIVE_HOME/bin:$PATH
  export PATH=$HADOOP_HOME/bin:$HADOOP_HOME/sbin:$PATH
  
  # Set Thrift Home   
  export THRIFT_HOME=/usr/local/thrift-server
  export GEOHIKER_HOME=$THRIFT_HOME
  ```
- Save the modifications and exit the editor
- Enter ```source ~/.bash_profile``` to apply the modified environment variables