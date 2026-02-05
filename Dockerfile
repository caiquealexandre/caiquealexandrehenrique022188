# ========= BUILD STAGE =========
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

# Cache de dependências
COPY pom.xml .
RUN mvn -q -e -DskipTests dependency:go-offline

# Build
COPY src ./src
RUN mvn -q -DskipTests clean package

# ========= RUNTIME STAGE =========
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Usuário não-root
RUN addgroup -S app && adduser -S app -G app
USER app

# Copia o jar gerado
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
