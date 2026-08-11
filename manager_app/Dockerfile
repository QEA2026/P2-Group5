FROM maven:3.9.11-eclipse-temurin-21 AS build

WORKDIR /build

# Cache Maven dependencies separately from the source code so repeated builds are faster.
COPY manager_app/pom.xml ./pom.xml
RUN mvn --batch-mode --no-transfer-progress -DskipTests dependency:go-offline

COPY manager_app/src ./src
RUN mvn --batch-mode --no-transfer-progress -DskipTests package \
    dependency:copy-dependencies \
    -DincludeScope=runtime \
    -DoutputDirectory=target/dependency

FROM eclipse-temurin:21-jre

WORKDIR /app/manager_app

COPY --from=build /build/target/classes ./classes
COPY --from=build /build/target/dependency ./dependency

EXPOSE 8080

CMD ["java", "-cp", "classes:dependency/*", "com.revature.Main"]
