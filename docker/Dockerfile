FROM alpine:latest

FROM amazoncorretto:8u352-al2
# JAVA_HOME=/usr/lib/jvm/java-1.8.0-amazon-corretto

RUN mkdir /usr/local/auroratms
COPY server-0.0.1-SNAPSHOT.jar /usr/local/auroratms/auroratms.jar

COPY vault.properties /usr/local/auroratms
COPY ca.pem /usr/local/auroratms

RUN keytool -import -trustcacerts -alias auroratms-ca -file /usr/local/auroratms/ca.pem -keystore /usr/lib/jvm/java-1.8.0-amazon-corretto/jre/lib/security/cacerts -storepass changeit -noprompt

RUN yum -y install tar

CMD ["java","-jar","/usr/local/auroratms/auroratms.jar"]

