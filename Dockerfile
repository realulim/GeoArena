FROM openjdk:8-jdk-alpine
RUN /bin/sh -c "apk add --no-cache bash bash-completion tzdata"
RUN /bin/sh -c "sed -i.bak 's/\/bin\/ash/\/bin\/bash/g' /etc/passwd"
ARG JAR_FILE
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","app.jar"]
