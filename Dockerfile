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

FROM pwittchen/ubuntu-java8
ENV artifact workflow_designer_server-jar-with-dependencies.jar 
WORKDIR /app
COPY --from=build /app/target/${artifact} /app/${artifact}
ADD krb5.conf /etc
ADD hdfs.keytab /
RUN apt-get update && apt-get install -y krb5-user cron
ADD crontab /etc/cron.d/kerberos-cron
RUN chmod 0644 /etc/cron.d/kerberos-cron
RUN crontab /etc/cron.d/kerberos-cron
RUN touch /var/log/cron.log
EXPOSE 8680
#IP must be an ip of a docker containter running cloudera get by sdocker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' [container-id]
ENTRYPOINT ["sh", "-c", "/etc/init.d/cron start && echo 172.17.0.3  quickstart.cloudera >> /etc/hosts && /usr/bin/kinit -kt /hdfs.keytab hdfs@CLOUDERA && exec java -Xmx1G -jar ${artifact}"]
