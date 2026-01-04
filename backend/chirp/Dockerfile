FROM gcr.io/distroless/java21-debian12

WORKDIR /app

COPY app/build/libs/app-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENV JAVA_TOOL_OPTIONS="-Djava.net.preferIPv4Stack=true \
                       -XX:+UseContainerSupport \
                       -XX:MaxRAMPercentage=75.0 \
                       -XX:InitialRAMPercentage=50.0 \
                       -XX:MaxMetaspaceSize=256m \
                       -XX:+HeapDumpOnOutOfMemoryError \
                       -XX:HeapDumpPath=/tmp \
                       -Dfile.encoding=UTF-8"

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
