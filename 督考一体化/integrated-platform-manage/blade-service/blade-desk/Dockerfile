FROM bladex/alpine-java:openjdk8-openj9_cn_slim

MAINTAINER vingsoft@qq.com

RUN mkdir -p /blade/desk

WORKDIR /blade/desk

EXPOSE 8105

ADD ./target/blade-desk.jar ./app.jar

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]

CMD ["--spring.profiles.active=test"]
