FROM bladex/alpine-java:openjdk8-openj9_cn_slim

MAINTAINER vingsoft@qq.com

RUN mkdir -p /blade/plugin/dataQuality

WORKDIR /blade/plugin/dataQuality

EXPOSE 8109

ADD ./target/data-quality.jar ./app.jar

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]

CMD ["--spring.profiles.active=test"]
