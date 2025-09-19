FROM bladex/alpine-java:8_server-jre_cn_unlimited

MAINTAINER vingsoft

RUN mkdir -p /blade/plugin/rocketmq

WORKDIR /blade/plugin/rocketmq

EXPOSE 6789

ADD ./target/blade-rocketmq.jar ./app.jar

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]

CMD ["--spring.profiles.active=test"]
