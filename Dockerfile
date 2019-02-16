FROM openjdk:alpine
ARG JARFILE
COPY $JARFILE /usr/src/app/app.jar
WORKDIR /usr/src/app
CMD java -XX:+PrintFlagsFinal $JAVA_OPTIONS -jar app.jar