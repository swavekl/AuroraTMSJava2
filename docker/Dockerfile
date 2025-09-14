FROM alpine:latest

FROM amazoncorretto:17.0.14-al2

RUN mkdir /usr/local/auroratms

COPY server-2.0.0.jar /usr/local/auroratms/auroratms.jar
COPY vault.properties /usr/local/auroratms
COPY ca.pem /usr/local/auroratms

RUN keytool -import -trustcacerts -alias auroratms-ca -file /usr/local/auroratms/ca.pem -cacerts -storepass changeit -noprompt

RUN yum -y install tar

CMD ["java", "--add-modules", "java.se", "--add-exports", "java.base/jdk.internal.ref=ALL-UNNAMED", "--add-opens", "java.base/java.lang=ALL-UNNAMED", "--add-opens", "java.base/sun.nio.ch=ALL-UNNAMED", "--add-opens", "java.base/java.net=ALL-UNNAMED", "--add-opens", "java.management/sun.management=ALL-UNNAMED", "--add-opens", "jdk.management/com.sun.management.internal=ALL-UNNAMED", "-jar", "/usr/local/auroratms/auroratms.jar"]

