# 2.2 환경변수 설정 

- 아래 명령을 통해 vi 편집기로 .bashrc 파일을 수정하도록 합니다. 
  ```bash
  vi ~/.bashrc
  ```

- 기존 ~/.bashrc 파일 내용에 아래의 내용을 추가해주도록 합니다.
  
  ```bash
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
  
  # Set Geohiker Version
  export GEOHIKER_VERSION=1.2.56
  ```
- 작업이 완료된 후에는 아래의 명령을 통해 등록한 환경변수를 적용하도록 합니다.
  ```bash
  source ~/.bashrc
  ```
- (선택) Geohiker 버전을 업데이트 하기 위해서는 `GEOHIKER_VERSION` 환경 변수를 수정해주시기 바랍니다.
  - Default version: 1.2.56 (Verified version as of May 10, 2023)
  ```aidl
  geohikerVersion=1.2.56
  ```
