# Lumber Website

## Installation

    $ git clone https://github.com/lumberdev/lumber-site.git

    $ npm install

or

\$ yarn install

## Usage

Run the development setup (localhost:3000/):

    $ shadow-cljs watch app

Optional (but useful for mobile device testing):

    $ npm install -g browser-sync

    $ browser-sync start --server 'resources/public' --files 'resources/public'

Build clojurescript for production:

    $ yarn release

Build clojurescript for [GH pages](https://lumberdev.github.io/lumber-site/):

    $ yarn release-dev

Remember to push `docs` to master after!

Run the project production server:

    $ clj -m lumber.serve

## Deployment

Install the latest version of Now CLI:

    npm i -g now

To create a test deployment

    now --docker

If the deployment works properly point lumberdev.nyc to it

    now alias set https://lumber-site-<code-here>.now.sh/ lumberdev.nyc

where you'll get the code from the previous step.

## License

Copyright © 2019 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
