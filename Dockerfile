FROM alpine/git as clone
ENV HOME=/app
WORKDIR /app
RUN git clone https://github.com/NEUROINFORMATICS-GROUP-FAV-KIV-ZCU/workflow_designer_server
RUN ["apk", "add", "--update", "bash", "libc6-compat"]

FROM maven:3.5-jdk-8-alpine as build
WORKDIR /app
COPY --from=clone /app/workflow_designer_server /app
ADD config/* /app/src/main/resources/
RUN mvn package

FROM java:8
ENV artifact workflow_designer_server-jar-with-dependencies.jar 
WORKDIR /app
COPY --from=build /app/target/${artifact} /app/${artifact}
#Next two lines are specific for integration with cloudera. If not required remove these next two lines and modify entrypoint
ADD krb5.conf /etc
ADD hdfs.keytab /
RUN apt-get update && apt-get -y install  krb5-user
EXPOSE 8680
#IP must be an ip of a docker containter running cloudera get by docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' [container-id]
ENTRYPOINT ["sh", "-c", " echo 172.17.0.2  quickstart.cloudera >> /etc/hosts && /usr/bin/kinit -kt /hdfs.keytab hdfs@CLOUDERA && exec java -Xmx1G -jar ${artifact}"]

