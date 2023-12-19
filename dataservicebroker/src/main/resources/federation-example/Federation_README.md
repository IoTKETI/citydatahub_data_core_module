## Federation 설정 시 참고를 위해 본 디렉토리에 yml 설정 파일을 제공
- 본 예시에서는 DataRegistry 1대와 ServiceBroker 3대 간 Federation 동작 설정 예시를 제공
  - DataManager와 IngestInterface의 정보는 제공하지 않음
- DataRegistry와 ServiceBroker는 각각의 DB를 구성해야 함 
- yml 파일에는 각 serviceBroker 간 차이가 되는 정보만 남겨두었으니 참고하면 됨

## 설정파일
 - application.yml : federation 공통 설정 파일
 - application-dataRegistry.yml : dataRegistry 기동 시 사용할 설정 파일
 - application-servicebroker1.yml : broker1 번 동작 설정 파일
 - application-servicebroker2.yml : broker2 번 동작 설정 파일
 - application-servicebroker3.yml : broker3 번 동작 설정 파일

## VM argument
만약 위 yml 파일 기반으로 Run을 하고자한다면 vm argument 를 지정해줘야 함
 - DataRegistry
 - -Dspring.profiles.active=local -Dspring.config.location=classpath:federation-example/application.yml,classpath:federation-example/application-dataRegistry.yml
 - ServiceBroker1
 - -Dspring.profiles.active=local -Dspring.config.location=classpath:federation-example/application.yml,classpath:federation-example/application-servicebroker1.yml
 - ServiceBroker2
 - -Dspring.profiles.active=local -Dspring.config.location=classpath:federation-example/application.yml,classpath:federation-example/application-servicebroker2.yml
 - ServiceBroker3
- -Dspring.profiles.active=local -Dspring.config.location=classpath:federation-example/application.yml,classpath:federation-example/application-servicebroker3.yml