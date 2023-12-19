# 2.4 Spark 설정

## 2.4.1 Thrift Server 배포

<br/>

- 프로젝트 빌드 후 생성된 \*.tar.gz 파일을 /usr/local 하위 경로에 압축 해제

  ```bash
  tar -xvzf /usr/local/lib/citydatahub_data_core_module/bigdatastorage/build/dist/thrift-server-1.0.tar.gz -C /usr/local
  ```

<br/>

- jts2geojson 설치

  ```bash
  wget -P $SPARK_HOME/jars/ "https://repo1.maven.org/maven2/org/wololo/jts2geojson/0.16.1/jts2geojson-0.16.1.jar"
  ```

<br/>

- Thrift 서버 실행 준비

  ```bash
  yum -y update
  yum install -y epel-release
  yum -y install supervisor

  cp /usr/local/lib/citydatahub_data_core_module/bigdatastorage/docker/thrift/run-thrift.sh /
  cd /
  chmod a+x run-thrift.sh

  # 스크립트 파일 내 line separator 이슈를 고려하여 각 스크립트 파일 convert 
  yum -y install dos2unix
  dos2unix $THRIFT_HOME/bin/thrift-server.sh
  dos2unix $THRIFT_HOME/bin/install-dependencies.sh
  dos2unix $GEOHIKER_HOME/sbin/datacore-start-thriftserver.sh
  dos2unix $GEOHIKER_HOME/sbin/datacore-stop-thriftserver.sh

  # 실행되는 스크립트 파일 권한 수정 (실행권한 추가)
  chmod +x $THRIFT_HOME/bin/thrift-server.sh
  chmod +x $THRIFT_HOME/bin/install-dependencies.sh
  chmod +x $GEOHIKER_HOME/sbin/datacore-start-thriftserver.sh
  chmod +x $GEOHIKER_HOME/sbin/datacore-stop-thriftserver.sh
  ```

<br/>

## 2.4.3 Hadoop 클러스터 (Hadoop & Hive과 연동)

HDFS에 데이터를 적재하고, YARN에서 query를 실행하기 위해 hadoop 클러스터가 설치된 경로의 config 폴더 하위에 있는 아래 명기되어있는 파일들을 `$SPARK_HOME/conf` 경로에 위치하도록 해야 합니다.

  ```
  core-site.xml
  hdfs-site.xml
  yarn-site.xml
  ```
