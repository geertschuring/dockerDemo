FROM openjdk:8
COPY build/distributions/DemoService.zip /opt/DemoService.zip
WORKDIR /opt
RUN unzip DemoService.zip
CMD ["/opt/DemoService/bin/DemoService"]
