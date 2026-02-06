# ========= BUILD STAGE =========
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copia arquivos de configuração do Gradle
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle .
COPY settings.gradle .

# Dá permissão de execução ao gradlew
RUN chmod +x gradlew

# Cache de dependências
RUN ./gradlew dependencies --no-daemon

# Build
COPY src ./src
RUN ./gradlew bootJar --no-daemon -x test

# ========= RUNTIME STAGE =========
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Usuário não-root
RUN addgroup -S app && adduser -S app -G app
USER app

# Copia o jar gerado
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
