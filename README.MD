# 로컬에 db띄우는 방법
- 도커 데스크탑 설치 후
1. docker pull postgres:latest
2. docker run --name ecs-postgres  -e POSTGRES_USER=ecs -e POSTGRES_PASSWORD=ecs -e POSTGRES_DB=ecs -p 5432:5432 -d postgres:latest
3. spring.datasource.url=jdbc:postgresql://localhost:5432/ecs
spring.datasource.username=ecs
spring.datasource.password=ecs
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true