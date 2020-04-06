FROM clojure:openjdk-14-tools-deps

LABEL "creators"="lumber.dev" \
      "dev"="Dimitar Marinov" \
      "ops"="Irina Yaroslavova Stefanova"

RUN apt-get update \
    && apt-get install -y nodejs \
    && apt-get install -y npm \
    && npm config set unsafe-perm true \
    && npm install -g shadow-cljs

WORKDIR /opt/lumber-site-src

# The following will BUST the CACHE code changes

# copy just deps.edn for clojure deps without busting cache with other files
COPY ./deps.edn /opt/lumber-site-src
# download clojure deps (avoid downloading in CMD step)
RUN clj -Stree

# copy package.json for JS deps
COPY ./package.json /opt/lumber-site-src
# download JS deps (avoid shadow-cljs downloading them)
RUN npm install

# COPY all source code
COPY . /opt/lumber-site-src
RUN shadow-cljs release app

EXPOSE 8080

CMD clj -m lumber.serve
