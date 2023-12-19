FROM centos:7

USER root

RUN yum -y install epel-release
RUN yum -y update

RUN yum -y install \
    java-1.8.0-openjdk \
    net-tools \
    wget \
    openssh-server \
    openssh-clients \
    openssh-askpass \
    supervisor \
    dos2unix && \
    yum -y clean all

RUN ssh-keygen -q -t rsa -N '' -f /etc/ssh/ssh_host_rsa_key
RUN ssh-keygen -q -t dsa -N '' -f /etc/ssh/ssh_host_ecdsa_key
RUN ssh-keygen -q -t ed25519 -N '' -f /etc/ssh/ssh_host_ed25519_key

# Versions
ENV HBASE_VERSION 2.3.7
ENV HADOOP_VER 3.0.0
ENV THRIFT_SERVER_VERSION 1.0

# Set environment variables
ENV JAVA_HOME /usr/lib/jvm/jre-1.8.0-openjdk
ENV HADOOP_PREFIX /usr/local/hadoop
ENV HADOOP_HOME /usr/local/hadoop
ENV HADOOP_MAPRED_HOME ${HADOOP_HOME}
ENV HADOOP_COMMON_HOME ${HADOOP_HOME}
ENV HADOOP_HDFS_HOME ${HADOOP_HOME}
ENV YARN_HOME ${HADOOP_HOME}

ENV HDFS_NAMENODE_USER root
ENV HDFS_DATANODE_USER root
ENV HDFS_SECONDARYNAMENODE_USER root
ENV YARN_RESOURCEMANAGER_USER root
ENV YARN_NODEMANAGER_USER root
ENV YARN_PROXYSERVER_USER root

ENV HADOOP_COMMON_LIB_NATIVE_DIR ${HADOOP_HOME}/lib/native
ENV HADOOP_OPT "-Djava.library.path=${HADOOP_PREFIX}/lib/native"
ENV HADOOP_OPTS "-Djava.library.path=${HADOOP_HOME}/lib"

# DownLoad Hadoop
WORKDIR /usr/local

ADD https://archive.apache.org/dist/hadoop/common/hadoop-${HADOOP_VER}/hadoop-${HADOOP_VER}.tar.gz .
RUN tar xfz hadoop-${HADOOP_VER}.tar.gz
RUN ln -s hadoop-${HADOOP_VER} hadoop

# Set PATH
ENV PATH ${PATH}:${HADOOP_HOME}/bin:${HADOOP_HOME}/sbin

# Generate ssh key
WORKDIR /root/.ssh
RUN ssh-keygen -q -t rsa -N '' -f id_rsa
RUN cat id_rsa.pub >> authorized_keys
RUN chmod 644 authorized_keys

WORKDIR /
ADD supervisord.conf .

ADD start-hadoop.sh .
RUN dos2unix start-hadoop.sh
RUN chmod a+x start-hadoop.sh

ENTRYPOINT ["supervisord", "-c", "supervisord.conf"]
