FROM eclipse-temurin:17-jre-alpine AS runtime

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8080 8081

ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]