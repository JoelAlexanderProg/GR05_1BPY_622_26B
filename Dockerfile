# Imagen de la aplicación web EncuentraEPN, para linux/amd64 y linux/arm64.
# Etapa 1: compila el proyecto con Maven y genera el WAR. El WAR es el mismo en todas las plataformas,
# así que esta etapa corre en la plataforma de la máquina que construye ($BUILDPLATFORM), sin emulación.
FROM --platform=$BUILDPLATFORM eclipse-temurin:21-jdk AS construccion
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY src src
RUN --mount=type=cache,target=/root/.m2 chmod +x mvnw && ./mvnw -B -q package -DskipTests

# Etapa 2: Tomcat 10.1 con la aplicación publicada en la raíz (http://localhost:8080/).
FROM tomcat:10.1-jdk21-temurin
# Hora de Ecuador: la aplicación compara fechas con la hora actual (por ejemplo, al proponer una entrega)
ENV TZ=America/Guayaquil
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=construccion /app/target/GR05_1BPY_622_26B.war /usr/local/tomcat/webapps/ROOT.war
# La base de datos H2 se guarda en ~/gr05_1bpy (en el contenedor, /root/gr05_1bpy); se puede montar un volumen ahí.
# Con -e DATOS_DEMO=true y la base de datos vacía, arranca con los datos de ejemplo de la iteración 5.
EXPOSE 8080
CMD ["catalina.sh", "run"]
