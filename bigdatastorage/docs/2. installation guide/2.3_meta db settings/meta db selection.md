# 2.3 Metastore DB 설정

- Spark Thrift는 Hive의 Metastore DB를 사용하며, 관련 셋팅이 필요합니다.
- 기존에 사용하고 있는 PostgreSQL DB가 있는 경우, 아래 `2.3.2 PostgreSQL을 Metastore DB로 설정`에서 `hive 사용자 및 meta DB 생성` 내용부터 참고를 해주시고, /usr/local/hive/conf 폴더 하위에 hive-site.xml 파일의 내용에서는 기존 DB에 설정되어있는 UserName, Password, ConnectionDriverName 및 ConnectionURL에 대한 value를 수정해주시기 바랍니다.
- 기존에 사용하고 있는 RDBMS가 없는 경우, 아래 `2.3.1 Metastore로 사용 할 데이터베이스 선택`부터 순차적으로 진행해주시기 바랍니다.

<br/>

## 2.3.1 Metastore로 사용 할 데이터베이스 선택

- 기본적으로 Apache Spark은 Derby라는 기본 Metastore DB를 제공합니다.
- 만약 기본적으로 제공되는 Derby 외 MySQL이나 PostgreSQL와 같은 RDBMS를 사용하는 경우에는 아래의 추가 과정을 통해 설정해주시기 바랍니다.
- Ambari 또는 Cloudera Manager(HDP/CDH)에 의해 사용되는 DB 또한 Metastore로써 사용될 수 있습니다.

<br/>

Thrift 서버는 단 하나의 metastore를 가질 수 있으며, 본 수동 설치 가이드에서는 기본적으로 PostgreSQL를 metastore DB로 사용함을 전제로 하여 작성하였습니다.

<br/>

## 2.3.2 PostgreSQL을 Metastore DB로 설정

<br/>

- PostgreSQL 설치 (본 매뉴얼은 PostgreSQL 13을 기준으로 작성하였습니다)

  ```bash
  # PostgreSQL 설치 정보를 담고 있는 RPM 설치
  yum install -y https://download.postgresql.org/pub/repos/yum/reporpms/EL-7-x86_64/pgdg-redhat-repo-latest.noarch.rpm

  # PostgreSQL13 설치
  yum install -y postgresql13-server
  ```

<br/>

- PostgreSQL 서비스 시작

  ```bash
  systemctl enable postgresql-13
  systemctl start postgresql-13
  systemctl status postgresql-13
  ```

<br/>
  
- hive 사용자 및 meta DB 생성

  ```bash
  # postgres 계정으로 전환
  $su - postgres

  $psql

  # 기존 사용자 리스트 출력
  postgres=#\du

  # hive 사용자를 필요 권한 및 비밀번호 설정과 함께 생성
  postgres=#CREATE ROLE hive Superuser CreateDB LOGIN PASSWORD 'hive123!';

  # hive 사용자가 소유자인 hive 데이터베이스 생성
  postgres=#CREATE DATABASE hive OWNER hive;

  # 생성된 hive 데이터베이스 확인
  postgres=#\l
  ```

<br/>

- /var/lib/pgsql/13/data/pg_hba.conf 파일 내 아래 항목을 수정

  ```bash
  ...
  local   all             all                     trust
  local   all             all      127.0.0.1/32   trust
  ...
  ```

<br/>

- postgresql-13 서비스 재시작 및

  ```bash
  # 서비스 재시작
  systemctl restart postgresql-13
  ```

<br/>

- JDBC 다운로드
  ```bash
  wget https://jdbc.postgresql.org/download/postgresql-42.2.19.jar --no-check-certificate
  ```

<br/>

- 다운받은 JDBC \*.jar 파일을 $HIVE_HOME/lib, $HIVE_HOME/jdbc, $SPARK_HOME/jars 폴더 하위에 복사
  ```bash
  cp postgresql-42.2.19.jar $HIVE_HOME/lib
  cp postgresql-42.2.19.jar $HIVE_HOME/jdbc
  cp postgresql-42.2.19.jar $SPARK_HOME/jars
  ```

<br/>
  
- `/usr/local/hive/conf` 및 `/usr/local/spark/conf` 폴더 하위에 hive-site.xml 파일 생성한 뒤 아래 내용 추가 <br/> (PostgreSQL의 설정 정보에서 Port는 5432(default)로 작성되었습니다)

  ```bash
  <configuration>
    <property>
      <name>javax.jdo.option.ConnectionUserName</name>
      <value>hive</value>
      <description>Username to use against metastore database</description>
    </property>

    <property>
      <name>javax.jdo.option.ConnectionPassword</name>
      <value>hive123!</value>
      <description>password to use against metastore database</description>
    </property>

    <property>
      <name>javax.jdo.option.ConnectionDriverName</name>
      <value>org.postgresql.Driver</value>
      <description> Class name of JDBC Driver Metastore DB</description>
    </property>

    <property>
      <name>javax.jdo.option.ConnectionURL</name>
      <value>jdbc:postgresql://localhost:5432/hive</value>
      <description>
        JDBC connect string for a JDBC metastore.
        To use SSL to encrypt/authenticate the connection, provide database-specific SSL flag in the connection URL.
        For example, jdbc:postgresql://myhost/db?ssl=true for postgres database.
      </description>
    </property>
  </configuration>
  ```

<br/>
  
- Hive metastore 초기화

  ```bash
  cd $HIVE_HOME/bin

  # Postgres
  ./schematool -initSchema -dbType postgres
  ```
