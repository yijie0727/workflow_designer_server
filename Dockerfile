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

FROM openjdk:8-jre
ENV artifact workflow_designer_server-jar-with-dependencies.jar 
WORKDIR /app
COPY --from=build /app/target/${artifact} /app/${artifact}
EXPOSE 8680
ENTRYPOINT ["sh", "-c"]
CMD ["java -jar ${artifact}"]
