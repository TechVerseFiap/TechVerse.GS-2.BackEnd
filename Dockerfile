# Etapa 1: compila o projeto com Maven
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q package -DskipTests

# Etapa 2: imagem leve só para rodar a API
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/cidadao-facil-api.jar app.jar

# Limite de memória para o plano free do Render (512 MB)
ENV JAVA_OPTS="-Xmx350m -Xss512k -XX:+UseSerialGC"

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
