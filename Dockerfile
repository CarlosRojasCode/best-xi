# Receta para construir y arrancar el backend en un servidor (Render).
# Tiene dos fases: en la primera se compila el proyecto y en la segunda
# solo se guarda el .jar resultante, para que la imagen final pese menos.

# ---- Fase 1: compilar ----
FROM eclipse-temurin:21-jdk AS compilar
WORKDIR /app

# Primero solo Maven y el pom.xml: así Docker reutiliza las dependencias
# descargadas si solo cambia el código.
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN sh mvnw -q dependency:go-offline

COPY src src
RUN sh mvnw -q -DskipTests package

# ---- Fase 2: ejecutar ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=compilar /app/target/*.jar app.jar

EXPOSE 8080
# El plan gratuito de Render tiene 512 MB: Java usa como máximo el 75 %.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
