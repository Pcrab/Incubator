FROM gradle:jdk17 as build-stage
RUN mkdir /app
WORKDIR /app
COPY . .
RUN ./auto.sh

FROM openjdk as prod-stage
RUN mkdir /app
WORKDIR /app
COPY --from=build-stage /app/build/libs/* .
EXPOSE 8080

CMD java -jar ./incubator-0.0.1-all.jar
