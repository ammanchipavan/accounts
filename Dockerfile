#start with a base image containing Java runtime
FROM openjdk:11-slim as build

#Information who maintains the image
MAINTAINER pavanammanchi.com

#Add the application's jar to the container
COPY target/accounts-0.0.1-SNAPSHOT.jar accounts-0.0.1-SNAPSHOT.jar

#execute the application
ENTRYPOINT ["java", "-jar", "accounts-0.0.1-SNAPSHOT.jar"]