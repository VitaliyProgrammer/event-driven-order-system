# syntax=docker/dockerfile:1
FROM eclipse-temurin:21-jdk AS build
ARG MODULE
WORKDIR /workspace
COPY . .
RUN --mount=type=cache,target=/root/.m2,sharing=locked \
    ./mvnw -q -pl ${MODULE} -am package -Dmaven.test.skip=true && \
    cp ${MODULE}/target/${MODULE}-*.jar /workspace/app.jar

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --uid 1001 app
USER app
COPY --from=build /workspace/app.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
