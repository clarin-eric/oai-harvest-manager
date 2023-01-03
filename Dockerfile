FROM registry.gitlab.com/clarin-eric/docker-alpine-supervisor-java-base:openjdk11-1.2.12 as java-base

RUN apk --no-cache add maven=3.6.1-r0

# install OAI Harvester
    
COPY . /tmp
RUN cd /tmp && \
    mvn clean package && \
    mkdir /tmp/oai && \
    cd /tmp/oai && \
    tar -xfz /tmp/oai-harvest-manager/target/oai-harvest-manager-1.2*.tar.gz

### Package stage

FROM java-base

# app workdir
RUN mkdir -p /app/workdir &&\
    mkdir -p /app/oai
WORKDIR /app/oai

COPY --from=build /tmp/oai /app/oai

ENTRYPOINT ["/app/oai/run-harvester.sh"]

# cleanup
RUN rm -rf /var/lib/apt/lists/* && \
    rm -rf /tmp/*
