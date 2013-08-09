# Statuses

_Statuses_ is an experimental, extremely simple-minded microblogging
infrastructure for internal use, basically created to have a small yet
meaningful Clojure app to play with. Don't expect too much, certainly
not something ready for production unless you're mainly interested in
fiddling around with stuff.

## How to for developers

* Make sure you have a reasonably recent Java runtime. _Statuses_ has
  been tested with JDK 7, but should work with JDK 6 as well (and
  possibly even JDK 5).
* Grab Leiningen (we've just upgraded to the Leiningen 2 preview and
  suggest you use this) and install according to its instructions
* Clone this repository
* Use `lein run` to start the application, then access it at
  [http://localhost:8080](http://localhost:8080) - some sample data is
  auto-generated if the "database" is not found
* Use `lein test` to run the test suite
* If you want to created a deployable artifact, use `lein uberjar` to
  create a stand-alone .jar file that you can run with `java -jar
  <name>`


If you want to run the application from clojure REPL e.g. in IntelliJ IDEA, you can go like this:

* open a clojure console/ REPL prompt in the "statuses" root folder
* you should see `user=>`
* type `(load-file "src/statuses/server.clj")` <ENTER>
* type `(ns statuses.server)` <ENTER>
* type `(-main nil)` <ENTER>

This should start the server.


## Dependencies

_Statuses_ uses

* Compojure for URI routing
* Hiccup for HTML and XML templating
* clj-time for time-related stuff
* clojure.data.json for JSON support

Note that all these dependencies are fetched automatically when you
use `lein run` for the first time, and are put into the uberjar for
deployment.

## Design notes

### Persistence

Given its very limited initial requirements at @innoQ (and to support
its usability as a self-contained example), the most significant
design decision within _Statuses_ is that no database is
used. Instead, all of the status updates are kept in memory and are
persisted every minute. This works surprisingly well in terms of code
simplicity, but will obviously scale only within limits. But if you
assume 15k are needed for every 100 status updates, even a million of
them would fit in 150MB of JSON (and probably something similar in
memory, haven't checked yet).

If scaling problems arise, it's likely going to be because writing the
DB to disk takes too long (though even that might be doubtful given
today's disk speeds). Should that become a problem, a solution might
be to combine the in-memory DB with an event-driven model, where the
full memory dump is only written very rarely, but every status update
is persisted immediately (essentially a transaction log). The full
state could thus be restored from the events stored, adding yet
another buzzword and turning this an event-sourcing model. But seeing
how far one can get without actually using a "real" database is part
of the experiment.

So there are currently no plans to change the db model, even though
refactoring the persistence into a Clojure `protocol` might be
reasonable to allow others to implement different backends.

### Modules

Currently, the structure of the whole program is still very simplistic
and should probably be refactored soon. At the moment, code is split
across the following namespaces and matching files:

* `statuses.server`: the server main entry point
* `statuses.backend.core`: the in-memory data structure and related functions
* `statuses.backend.json`: JSON utility functions for converting the
  in-memory DB to and from JSON
* `statuses.backend.persistence`: functions to write and read the DB
  to/from disk
* `statuses.backend.time`: utility functions for handling the
  conversion of (localized) time stamps
* `statuses.views.common`: layouts (using Twitter Bootstrap) for the UI
* `statuses.views.main`: the actual UI elements for displaying HTML
  (and JSON)
* `statuses.views.atom`: function to provide an Atom 1.0 feed of
  status updates


## Author information and license

Copyright 2012 innoQ Deutschland GmbH. Published under the Apache 2.0 license.
