FROM clojure:openjdk-14-tools-deps

LABEL "creators"="lumberdev.nyc" \
      "dev"="Dimitar Marinov" \
      "ops"="Irina Yaroslavova Stefanova"

COPY . /opt/lumber-site-src

WORKDIR /opt/lumber-site-src

RUN apt-get update \
    && apt-get install -y nodejs \
    && apt-get install -y npm \
    && npm config set unsafe-perm true \
    && npm install \
    && npm install -g shadow-cljs \
    && shadow-cljs release app \
    && apt-get remove -y nodejs \
    && apt-get remove -y npm

EXPOSE 80

CMD clj -m lumber.serve
