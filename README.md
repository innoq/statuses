# statuses

**statuses** is an experimental, extremely simple-minded microblogging
infrastructure for internal use, basically created to have a small yet
meaningful [Clojure] application to play with.

Don't expect too much, certainly not something ready for production unless
you're mainly interested in fiddling around with stuff.

## Building from Source

**statuses** uses [Leiningen] as build system. In the instructions below, `lein`
is invoked from the root of the source tree.

### Prerequisites

You will need [Git], [JDK 8 update 162 or later][JDK] and [Leiningen 2.7.1 or
later][Leiningen]. All other dependencies are fetched by [Leiningen].

### Check out sources

```
git clone https://github.com/innoq/statuses.git
```

### Build the application

```
lein uberjar
```

### Run the application locally

```
lein run
```

## License

**statuses** is released under version 2.0 of the [Apache License].


[Apache License]: https://www.apache.org/licenses/LICENSE-2.0.html
[Clojure]: https://clojure.org
[Git]: https://help.github.com/set-up-git-redirect
[JDK]: http://www.oracle.com/technetwork/java/javase/downloads
[Leiningen]: https://leiningen.org

