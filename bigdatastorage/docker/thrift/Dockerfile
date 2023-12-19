FROM centos:7

USER root

RUN yum -y install epel-release
RUN yum -y update

RUN yum -y install \
    java-1.8.0-openjdk \
    wget \
    net-tools \
    openssh-server \
    openssh-clients \
    openssh-askpass \
    supervisor \
    dos2unix

RUN ssh-keygen -q -t rsa -N '' -f /etc/ssh/ssh_host_rsa_key
RUN ssh-keygen -q -t dsa -N '' -f /etc/ssh/ssh_host_ecdsa_key
RUN ssh-keygen -q -t ed25519 -N '' -f /etc/ssh/ssh_host_ed25519_key

# Versions
ENV SPARK_VERSION 3.0.1
ENV HADOOP_PROFILE 2.7
ENV HIVE_VERSION 2.3.7
ENV HADOOP_VER 3.0.0
ENV THRIFT_SERVER_VERSION 1.0
ENV GEOHIKER_VERSION 1.2.56

# Set Environment Variable
ENV JAVA_HOME /usr/lib/jvm/jre-1.8.0-openjdk
ENV THRIFT_HOME /usr/local/thrift-server
ENV GEOHIKER_HOME /usr/local/thrift-server
ENV SPARK_HOME /usr/local/spark
ENV PYTHONPATH ${SPARK_HOME}/python/:${PYTHONPATH}

ENV HIVE_HOME /usr/local/hive
ENV HIVE_CONF_DIR /usr/local/hive/conf

ENV HADOOP_PREFIX /usr/local/hadoop
ENV HADOOP_HOME /usr/local/hadoop
ENV HADOOP_MAPRED_HOME ${HADOOP_HOME}
ENV HADOOP_COMMON_HOME ${HADOOP_HOME}
ENV HADOOP_HDFS_HOME ${HADOOP_HOME}
ENV YARN_HOME ${HADOOP_HOME}

ENV HADOOP_COMMON_LIB_NATIVE_DIR ${HADOOP_HOME}/lib/native
ENV HADOOP_OPT "-Djava.library.path=${HADOOP_PREFIX}/lib/native"

# DownLoad Spark
WORKDIR /usr/local

ADD https://archive.apache.org/dist/spark/spark-${SPARK_VERSION}/spark-${SPARK_VERSION}-bin-hadoop${HADOOP_PROFILE}.tgz .
RUN tar xfz spark-${SPARK_VERSION}-bin-hadoop${HADOOP_PROFILE}.tgz
RUN ln -s spark-${SPARK_VERSION}-bin-hadoop${HADOOP_PROFILE} spark

# Download additional libraries for spark
WORKDIR ${SPARK_HOME}/jars

## Download jst2geojson
ADD https://repo1.maven.org/maven2/org/wololo/jts2geojson/0.16.1/jts2geojson-0.16.1.jar .

## Download postgresql-jdbc-connector
ADD https://jdbc.postgresql.org/download/postgresql-42.2.19.jar .

# Spark configuration
WORKDIR ${SPARK_HOME}/conf

## Set spark logging properties
RUN cp log4j.properties.template log4j.properties

# DownLoad Hive
WORKDIR /usr/local

ADD https://archive.apache.org/dist/hive/hive-${HIVE_VERSION}/apache-hive-${HIVE_VERSION}-bin.tar.gz .
RUN tar xfz apache-hive-${HIVE_VERSION}-bin.tar.gz
RUN ln -s apache-hive-${HIVE_VERSION}-bin hive

# DownLoad Hadoop
WORKDIR /usr/local

ADD https://archive.apache.org/dist/hadoop/common/hadoop-${HADOOP_VER}/hadoop-${HADOOP_VER}.tar.gz .
RUN tar xfz hadoop-${HADOOP_VER}.tar.gz
RUN ln -s hadoop-${HADOOP_VER} hadoop

# Deploy Thrift Server
WORKDIR /usr/local

ADD thrift-server-${THRIFT_SERVER_VERSION}.tar.gz .
RUN ln -s thrift-server-${THRIFT_SERVER_VERSION} thrift-server

RUN chown -R root:root thrift-server
RUN chmod -R 744 thrift-server

# Deploy thrift scripts
WORKDIR ${THRIFT_HOME}/bin
WORKDIR ${THRIFT_HOME}/sbin

## datacore-start-thriftserver.sh
RUN dos2unix datacore-start-thriftserver.sh
RUN chmod +x datacore-start-thriftserver.sh

## datacore-stop-thriftserver.sh
RUN dos2unix datacore-stop-thriftserver.sh
RUN chmod +x datacore-stop-thriftserver.sh

# Set PATH
ENV PATH ${PATH}:${JAVA_HOME}/bin:${SPARK_HOME}/bin:${HIVE_HOME}/bin:${HADOOP_HOME}/bin:${HADOOP_HOME}/sbin

WORKDIR /
ADD run-thrift.sh .
RUN chmod a+x run-thrift.sh

ADD supervisord.conf .
ADD ivy.settings /usr/local/lib/.

RUN sh ${THRIFT_HOME}/bin/install-dependencies.sh

ENTRYPOINT ["supervisord", "-c", "supervisord.conf"]
