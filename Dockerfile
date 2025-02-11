FROM maven:3.8.5-jdk-11-slim AS build
# Add a volume pointing to /tmp
VOLUME /tmp

COPY .git /home/app/.git
COPY src /home/app/src
COPY lib /home/app/lib
COPY pom.xml /home/app
RUN mvn -e -f /home/app/pom.xml clean package -DskipTests

RUN chown -R 1001:1001 /home/app

FROM adoptopenjdk/openjdk11:alpine-jre
VOLUME /tmp
COPY --from=build /home/app/target/cio-creditmgmt-writeapi-v2-0.0.1-SNAPSHOT.jar app.jar

RUN mkdir /home/app/
RUN chown -R 1001:1001 /home/app/

RUN mkdir /home/app/logs/
RUN chown -R 1001:1001 /home/app/logs/


# Configure Certificates
COPY trustcerts/* /tmp/trustcerts/
RUN for certs in `ls /tmp/trustcerts/*.crt`; do keytool -importcert -keystore $JAVA_HOME/lib/security/cacerts -alias `echo $certs|awk -F "/" '{print $4}'` -file $certs -noprompt -trustcacerts -storepass changeit; done &&\
rm -r /tmp/trustcerts




ENV SPRING_PROFILES_ACTIVE=non-prod
ENTRYPOINT ["java", "-Djava.security.edg=file:/dev/./urandom","-Dspring.jdbc.getParameterType.ignore=true","-jar","app.jar"]
