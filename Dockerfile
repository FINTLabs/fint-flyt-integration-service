FROM gradle:9.4.1-jdk25 AS build
WORKDIR /workspace
COPY . .
RUN chmod +x gradlew && ./gradlew --no-daemon bootJar

FROM gcr.io/distroless/java25:nonroot
ENV TZ="Europe/Oslo"
ENV JAVA_TOOL_OPTIONS=-XX:+ExitOnOutOfMemoryError
WORKDIR /app
COPY --from=build /workspace/build/libs/*.jar ./app.jar
EXPOSE 8080
CMD ["app.jar"]
