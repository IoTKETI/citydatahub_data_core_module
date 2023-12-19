# 2.5 Thrift 서버 실행 전 준비

- /usr/local/lib 하위에 ivy.settings 파일 생성 

  ```bash
  cd /usr/local/lib
  vi ivy.settings
  ```

- 생성한 ivy.settings 파일에 아래의 내용 추가

  ```xml
  <ivysettings>
  <settings defaultBranch="${ivy.deliver.branch}" defaultResolver="default-chain" />
  <property name ="osgeo"  value="https://repo.osgeo.org/repository/release/"/>
  <property name ="jboss" value="https://repository.jboss.org/nexus/congent/repositories/thirdparty-releases/"/>
  <property name="nexus-dtonic" value="https://nexus.dtonic.io/repository/maven-public"/>
  <resolvers>
  <ibiblio name="nexus" m2compatible="true" root="${nexus=dtonioc}"/>
  </resolvers>
  <resolvers>
  <ibiblio name="central" m2compatible="true"/>
  <ibiblio name="nexus" m2compatible="true" root="${nexus-dtonic}"/>
  <ibiblio name="jboss" m2compatible="true" root="${jboss}"/>
  <ibiblio name="os-geo" m2compatible="true" root="${osgeo}"/>
  <chain name="default-chain">
  <resolver ref="nexus" />
  <!--resolver ref="central" /-->
  <!--resolver ref="jboss" /-->
  <!--resolver ref="os-geo" /-->
  </chain>
  </resolvers>
  </ivysettings>
  ```

<br/>

- Thrift 서버를 최초로 실행하는 경우, 우선적으로 필요한 디펜던시들을 다운로드하는 명령을 실행합니다.
  ```bash
  $THRIFT_HOME/bin/install-dependencies.sh
  ```


# 2.6 Thrift 서버 실행

  ```bash
  $THRIFT_HOME/bin/thrift-server.sh start
  ```
- (참고) Thrift Server를 처음 실행할 때, 실행이 완료될 때까지 시간이 소요될 수 있습니다. 
  실행이 완료되었다는 것을 확인하려면 `netstat -tnlp | grep 10000` 명령어를 사용하여 
  10000번 포트가 열렸는지 확인할 수 있습니다.
  
<br/>

## 2.5.1 Beeline을 통해 Thrift 서버 접속 및 테스트

`$THRIFT_HOME/bin/thrift-server.sh start` 명령을 통해 Thrift 서버를 시작한 후에 완전히 서버가 올라가기까지 시간이 소요되기 때문에 약간의 시간을 두고 Beeline 접속을 해주시기 바랍니다.

<br/>

- Beeline 접속
  ```bash
  $SPARK_HOME/bin/beeline
  ```

<br/>

- 접속한 Beeline에서 Thrift 서버 접속 및 테스트 (테이블 생성 및 데이터 적재)

  아래에서 beeline을 통해 Thrift 서버에 접속 시 입력되는 계정 정보는 HDFS에 데이터를 적재하기 위해 필요한 계정 정보입니다.

  username과 password에 아무 값도 입력되지 않을 경우, anonymous로 데이터가 적재됩니다.
  
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