FROM maven
RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY . .
RUN mvn package
CMD ["java", "-jar", "target/rabbitmq-consumer-1.0-SNAPSHOT-jar-with-dependencies.jar"]