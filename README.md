# Lumber Website

## Installation

    $ git clone https://github.com/lumberdev/lumber-site.git

    $ npm install
    
or 
    
    $ yarn install

## Usage

Run the development setup (localhost:3000/):

    $ shadow-cljs watch app
    
Optional (but useful for mobile device testing):

    $ npm install -g browser-sync

    $ browser-sync start --server 'resources/public' --files 'resources/public'
    
Build clojurescript for production:

    $ shadow-cljs release app

Run the project production server:

    $ clj -m lumber.serve

### Bugs

## License

Copyright © 2019 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
