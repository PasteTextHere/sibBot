#base image
FROM openjdk:15-slim
#env
ENV LOG_LEVEL INFO
#copy jar from target to image
ADD build/libs/sibBot-1.0.jar /usr/src/sibBot-1.0.jar
#set work dir to jar-file path
WORKDIR /usr/src
#port expose
EXPOSE 8080
#start jar inside container
ENTRYPOINT java -XX:+UseContainerSupport -Xss512k -XX:MetaspaceSize=100m -jar sibBot-1.0.jar
