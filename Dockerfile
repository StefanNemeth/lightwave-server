FROM hseeberger/scala-sbt

RUN mkdir /lightwave
WORKDIR /lightwave

ADD . /lightwave
RUN sbt test

CMD ["sbt"]