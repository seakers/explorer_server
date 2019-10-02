### ----------------------------------------
### Gradle Building
### ----------------------------------------
FROM gradle:jdk12 as builder
COPY --chown=gradle:gradle ./src /home/gradle/src
COPY --chown=gradle:gradle build.gradle settings.gradle /home/gradle/
WORKDIR /home/gradle
COPY --chown=gradle:gradle ./. /home/gradle
# RUN gradle build
# RUN ls build/distributions











### ----------------------------------------
### Maven Base Image
### ----------------------------------------
FROM maven:3.6.2-jdk-12






### ----------------------------------------
### Copy Statements
### ----------------------------------------






### ----------------------------------------
### Run Command
### ----------------------------------------
CMD /bin/bash









