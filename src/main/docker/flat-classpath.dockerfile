 # Stage 0, "builder", extract fat jar
FROM amd64/openjdk:11-alpine as builder
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} /target/app.jar
RUN mkdir -p /target/dependency && (cd /target/dependency; jar -xf ../*.jar)

# Stage 1, "boot-app"
FROM amd64/openjdk:11-alpine
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
COPY --from=builder /target/dependency/BOOT-INF/lib /app/lib
COPY --from=builder /target/dependency/BOOT-INF/classes /app
COPY --from=builder /target/dependency/META-INF /app
ENTRYPOINT ["java", "-cp" , "app:app/lib/*", "io.github.bhuwanupadhyay.example.SpringBoot"]