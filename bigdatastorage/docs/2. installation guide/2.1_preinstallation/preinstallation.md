# 2.1 설치 전 준비 (수동 및 docker 사용한 설치 공통)

- JDK 설치 (java-1.8.0-openjdk) 및 $JAVA_HOME 환경변수 등록 (Java 8 버전이 Spark 버전과 호환)

  ```bash
  # JDK 설치
  yum install java-1.8.0-openjdk-devel.x86_64

  # .bashrc 파일 수정
  vi ~/.bashrc

  # .bashrc 파일에 JAVA_HOME 환경변수 추가
  export JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.362.b08-1.el7_9.x86_64

  # 변경된 .bashrc 파일 적용
  source ~/.bashrc

  # JAVA_HOME 환경변수가 제대로 설정되었는지 확인
  echo $JAVA_HOME
  ```

- /usr/local/lib 폴더 하위에 프로젝트를 복제

  ```bash
  cd /usr/local/lib
  git clone https://github.com/IoTKETI/citydatahub_data_core_module.git
  ```

- 복제된 프로젝트 폴더 내 bigdatastorage 폴더로 이동 후 하위의 gradlew 파일 권한 수정 및 build 및 makeTar 명령을 통한 \*.tar.gz 파일 생성

  ```bash
  chmod +x gradlew
  ./gradlew build
  ./gradlew makeTar
  ```

## 프로젝트 빌드 후 결과 파일 확인

- 위의 작업이 완료된 후에 아래 폴더 경로에서 `tar.gz` 파일을 확인하실 수 있습니다.

  ```
  build/dist
  docker/thrift/thrift-server
  ```

  <br/>

`여기까지의 내용이 수동 및 Docker를 사용한 Thrift 서버 설치 시 공통으로 진행해야 되는 내용이었습니다.`

만약 docker를 사용해서 설치를 하시는 경우에는`run on docker`를 참고해서 진행해주시고, 수동 설치를 진행하시는 경우에는 아래 내용을 포함해서 2.2 Environment Variable ~ 2.5. Run Thrift Server를 순차적으로 이어서 진행해주시기 바랍니다.

<br/>

수동 설치 매뉴얼의 경우에는 Thrift 서버를 구동하기 위한 매뉴얼로, HDFS에 실제 데이터를 적재하기 위해서는 `2.4. Spark Setting` 페이지의 `2.4.3 Hadoop 클러스터 (Hadoop & Hive과 연동))`부분에 대한 설정은 반드시 해주셔야 합니다. 

<br/>

만약 `2.4.3 Hadoop 클러스터 (Hadoop & Hive과 연동))`에 대한 별도의 설정을 하지 않을 경우, `/root/spark-warehouse/` 경로의 하위에 생성한 테이블명의 폴더가 생성되며, 각 폴더의 하위에 적재된 데이터에 대한 파일이 생성/적재됩니다.

<br/>

## 설치 시 필요한 net-tools와 wget 설치

```bash
yum install -y net-tools
yum install -y wget
```

<br/>

## Hadoop, Hive, Spark 설치

```bash
# BigDataStorageHandler가 동작하는 환경은 아래와 같습니다.

- Spark: 3.0.1
- HADOOP_PROFILE: 2.7(Spark 다운로드를 위한 요구)
- Hive: 2.3.7
- Hadoop: 3.0.0
```

아래 명령어를 통해 spark, hive, hadoop을 설치해주시기 바랍니다.

```bash
# spark
curl -s https://archive.apache.org/dist/spark/spark-3.0.1/spark-3.0.1-bin-hadoop2.7.tgz | tar -xvz -C /usr/local/
# hive
curl -s https://archive.apache.org/dist/hive/hive-2.3.7/apache-hive-2.3.7-bin.tar.gz | tar -xvz -C /usr/local/
# hadoop
curl -s https://archive.apache.org/dist/hadoop/common/hadoop-3.0.0/hadoop-3.0.0.tar.gz | tar -xvz -C /usr/local/
```

위에서 설치한 spark, hive, hadoop 경로에 대한 symbolic link 설정

```bash
# spark
cd /usr/local && ln -s spark-3.0.1-bin-hadoop2.7 spark
# hive
cd /usr/local && ln -s apache-hive-2.3.7-bin hive
# hadoop
cd /usr/local && ln -s hadoop-3.0.0 hadoop
```
