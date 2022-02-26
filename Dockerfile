FROM openjdk
RUN mkdir /app
WORKDIR /app
COPY ./build/libs/* .

EXPOSE 8080

CMD java -jar ./incubator-0.0.1-all.jar
