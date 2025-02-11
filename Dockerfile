FROM eclipse-temurin:21-jre-alpine

ENV TZ=Europe/Oslo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

VOLUME /tmp
COPY /target/dataset-catalog.jar app.jar

RUN sh -c 'touch /app.jar'
CMD java -jar $JAVA_OPTS app.jar
