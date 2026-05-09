# Etapa 1: Construcción (Build)
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app
# Copiamos primero el pom.xml y bajamos dependencias (optimiza la caché de Docker)
COPY pom.xml .
RUN mvn dependency:go-offline
# Copiamos el código fuente y compilamos
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Ejecución (Run)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Copiamos solo el .jar compilado de la etapa anterior
COPY --from=builder /app/target/facushop-0.0.1-SNAPSHOT.jar app.jar

# Exponemos el puerto de Spring Boot
EXPOSE 8080

# Comando para ejecutar la app
ENTRYPOINT ["java", "-jar", "app.jar"]