## 🔐 API REST com Spring Security, OAuth2 e JWT

Este projeto é uma API REST construída com **Spring Boot**, com foco em autenticação e autorização seguindo boas práticas utilizadas em aplicações modernas.

A autenticação é feita de forma **stateless**, utilizando **JWT (JSON Web Token)** assinado com **chaves RSA**, simulando o comportamento de um **Authorization Server**. A aplicação atua como **Resource Server**, validando tokens por meio da chave pública.

### 🛠 Tecnologias utilizadas
- Java
- Spring Boot
- Spring Security
- OAuth2 Resource Server
- JWT (RSA)
- JPA / Hibernate
- MySQL
- Docker

### 🔐 Segurança
- Endpoint de login público para emissão de token
- Tokens JWT assinados com chave privada
- Validação de token com chave pública
- Controle de acesso baseado em roles
- Suporte a múltiplas roles por usuário
- Sessão stateless (`SessionCreationPolicy.STATELESS`)


## 🔑 Chaves RSA

Gere as chaves localmente:

```bash
openssl genrsa -out app.key 2048
openssl rsa -in app.key -pubout -out app.pub
```

### 🧾 `application.properties` de exemplo

```properties
spring.application.name=springsecurity

spring.datasource.url=jdbc:mysql://localhost:3306/mydb
spring.datasource.username=admin
spring.datasource.password=1234
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

spring.sql.init.mode=always
