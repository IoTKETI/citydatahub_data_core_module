# 부록. Docker를 이용해서 Thrift 서버 설치

복제한 프로젝트 내 bigdatastorage 폴더 하위의 docker 폴더는 `hadoop, postgres, thrift` 각 각의 서비스별로 폴더로 구분하여 구성되어 있습니다. docker를 이용해서 Thrift 서버를 설치하는 경우에는 docker 폴더 하위의 docker-compose.yml 파일을 사용해주시기 바랍니다.

docker 및 docker-compose가 설치되어있지 않은 경우, 아래의 명령을 통해 설치를 진행해주시기 바랍니다.
 
  <br/>

  - Docker 및 docker-compose 설치
    ```bash
    yum install -y docker
    
    sudo curl -L "https://github.com/docker/compose/releases/download/1.27.4/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    
    sudo chmod +x /usr/local/bin/docker-compose
    ```

  <br/>

  - Docker 서비스 시작
    
    ```bash
    systemctl start docker
    ```
  
  <br/>

  - Docker network 생성

    ```bash
    docker network create -d bridge local-docker-bridge
    ```
  <br/>

  - Postgresql과 Hadoop의 volume을 마운트 할 로컬 디렉토리 변경

    - `/usr/local/lib/citydatahub_data_core_module/bigdatastorage/docker` 하위에 있는 `.env 파일` 수정을 통해 Postgresql과 Hadoop의 volume을 마운트 할 로컬 디렉토리의 경로를 변경할 수 있습니다.

        ```bash
        # /usr/local/lib/citydatahub_data_core_module/bigdatastorage/docker/.env
        # 기본 설정 경로

        POSTGRESQL_LOCAL_PATH=/usr/local/lib/postgresql/data
        HADOOP_LOCAL_PATH=/hdfs-data
        
        ```
  <br/>

  - `/usr/local/lib/citydatahub_data_core_module/bigdatastorage/docker` 하위에 있는 docker-compose.yml 파일 실행

    ```bash
    cd /usr/local/lib/citydatahub_data_core_module/bigdatastorage/docker

    docker-compose up -d --build
    ```

  <br/>

  - Thrift 서버 컨테이너 접근 및 Thrift 서버 실행 
    ```bash
    # hadoop, postgres, thrift 컨테이너가 띄워져있는지 확인
    docker ps
    
    # thrift 서버 컨테이너에 bash shell로 접근
    docker exec -it thrift /bin/bash

    # Thrift 서버 실행
    $THRIFT_HOME/bin/thrift-server.sh start
    ```
  
  <br/>

  - (참고) Thrift Server를 처음 실행할 때, 실행이 완료될 때까지 시간이 소요될 수 있습니다.
  실행이 완료되었다는 것을 확인하려면 `netstat -tnlp | grep 10000` 명령어를 사용하여
  10000번 포트가 열렸는지 확인할 수 있습니다.

  <br/>


  - 접속한 Beeline에서 Thrift 서버 접속 및 테스트 (테이블 생성 및 데이터 적재)

    Thrift 서버 접속 계정 정보는 아래와 같습니다.
    아래에서 beeline을 통해 Thrift 서버에 접속 시 입력되는 계정 정보는 HDFS에 데이터를 적재하기 위해 필요한 계정 정보입니다.

    <br/>
    
    username : root <br/>
    password : (없음)

    <br/>

    docker-compose를 이용한 설치는 수동 설치와 달리 Hadoop cluster와 연동된 구성이기 때문에 아래와 같이 beeline 접속해서 테스트 시 root 계정으로 접근해서 테이블 생성 및 데이터 삽입을 해줘야 합니다.

<br/>

  ```bash
  beeline> !connect jdbc:hive2://localhost:10000/default
  Connecting to jdbc:hive2://localhost:10000/default
  Enter username for jdbc:hive2://localhost:10000: 
  Enter password for jdbc:hive2://localhost:10000: 

  # Thrift 서버 접속
  # 테스트 테이블 생성
  0: jdbc:hive2://localhost:10000/default> CREATE TABLE test_table (id INT, name VARCHAR(50));

  # 생성된 test_table 테이블 확인
  0: jdbc:hive2://localhost:10000/default> SHOW TABLES;

  # 생성한 테이블에 데이터 삽입
  0: jdbc:hive2://localhost:10000/default> INSERT INTO test_table VALUES(1, 'lee');

  # 삽입한 데이터 확인
  0: jdbc:hive2://localhost:10000/default> SELECT * FROM test_table;
  ```
  
  <br/>

  - 적재된 데이터는 아래의 과정을 통해 확인해주시기 바랍니다.
    ```bash
    # Hadoop 컨테이너 접속 
    docker exec -it hadoop /bin/bash

    # 데이터 적재 경로
    hdfs dfs -ls /user/hive/warehouse/{생성한 테이블 명} 
    ```

<br/>

## Thrift 서버와 외부 Hadoop 클러스터와의 연동

<br/>

Thrift 서버는 Spark 어플리케이션이기 때문에 socket을 통해 hadoop slave(data node, node manager)와 직접적으로 통신을 합니다. 이를 위해서 Thrift 컨테이너의 네트워크는 반드시 host로 구성되어야 합니다.

만약 Thrift 컨테이너의 네트워크를 바꾸고자 한다면, 아래의 내용에 따라 설정을 변경해줘야 합니다.

<br/>

### 1. Hadoop 환경설정 파일 대체

`docker/conf` 폴더에는 각 각의 서비스들에 대한 환경설정 파일들이 있습니다. Thrift 서버가 외부 Hadoop에 접근하기 위해서 `docker/conf/hadoop`에 있는 환경설정 파일들을 외부 Hadoop의 설정파일로 대체해줘야 합니다. 

<br/>

### 2. Metastore DB 설정

빅데이터 저장소의 Standalone 모드는 기존 bridge network 상의 docker로 올라가 있는 Postgres metastore DB와 동작을 하고 있습니다. 
Thrift 컨테이너가 외부 Hadoop과 통신을 하기 위해서는 자체 네트워크 모드를 bridge network에서 host network로 변경을 해줘야 하며, PostgreSQL 컨테이너를 대체하는 또 다른 접근에 대한 수정이 필요합니다. 
docker-compose 파일에서는 PostgreSQL의 5432 포트에 접근하도록 포트가 바인딩 되어있는데, `docker/conf/hive/hive-site.xml` 파일에서 Thrift 서버가 다시 기존의 PostgreSQL을 metastore DB로 사용하도록 수정을 해줘야 합니다. 

<br/>

- 수정 전
```
<property>
    <name>javax.jdo.option.ConnectionURL</name>
    <value>jdbc:postgresql://meta_db:5432/hive</value>
</property>
```

<br/>

- 수정 후
```
<property>
    <name>javax.jdo.option.ConnectionURL</name>
    <value>jdbc:postgresql://<DOCKER_HOST_IP>:54321/hive</value>
</property>
```

위의 환경 설정을 수정한 후에 추가된 설정 정보를 적용하기 위해 Thrift 서버를 재시작 해주시기 바랍니다.

<br/>

## HDFS 데이터 수동 백업

<br/>

HDFS에 적재된 데이터는 컨테이너가 재기동 후에도 유지되어야 하기 때문에 아래 과정을 통해 HDFS의 데이터를 수동으로 백업합니다. 

아래 과정은 Hadoop 컨테이너 내에 접속하여 시행하도록 합니다. 

<br/>

### 1. HDFS 내 데이터 백업

Hadoop Docker 컨테이너 내 /hdfs-data 디렉토리는 로컬 시스템의 hdfs_local_data 디렉토리와 volume mount되어 있습니다. 데이터를 백업하기 위해서는 아래 명령을 통해 docker 컨테이너 내 /hdfs-data 디렉토리에 HDFS 내 데이터 파일을 복사하도록 합니다.

```zsh
hdfs dfs -get /user/hive/warehouse/* /hdfs-data 
```

### 2. Hadoop 컨테이너 재기동 후

컨테이너가 재기동된 후에는 /user/hive/warehouse 디렉토리가 삭제된 상태이기 때문에 우선 /user/hive/warehouse 디렉토리를 생성한 후에 hdfs-data 디렉토리(Hadoop container) 내에 백업된 데이터를 HDFS에 업로드하도록 합니다.

```zsh
hdfs dfs -mkdir -p /user/hive/warehouse   
hdfs dfs -put hdfs-data/* /user/hive/warehouse  
```
