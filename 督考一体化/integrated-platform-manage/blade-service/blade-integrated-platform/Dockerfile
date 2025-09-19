#FROM bladex/alpine-java:openjdk8-openj9_cn_slim
#FROM 60.168.132.72:3401/library/alpine-java:openjdk8-openj9_cn_slim
FROM crpi-55h529x432k7glob.cn-qingdao.personal.cr.aliyuncs.com/vingsoft/alpine-java:openjdk8-openj9_cn_slim

MAINTAINER vingsoft@qq.com

RUN mkdir -p /blade/integrated-platform

WORKDIR /blade/integrated-platform

EXPOSE 6001

ADD ./target/blade-integrated-platform.jar ./app.jar

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]

CMD ["--spring.profiles.active=test"]
