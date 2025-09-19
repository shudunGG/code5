FROM bladex/alpine-java:openjdk8-openj9_cn_slim

MAINTAINER vingsoft@qq.com

RUN mkdir -p /blade/swagger

WORKDIR /blade/swagger

EXPOSE 18000

ADD ./target/blade-swagger.jar ./app.jar

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]

CMD ["--spring.profiles.active=test"]
