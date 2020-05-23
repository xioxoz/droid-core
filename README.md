Droid code library
=================

The droid core library aims to provide a sub set of the Android Open Source
Project classes that could be useful in classic Java projects. The library
will try to keep the interfaces as close as possible from the Android framework,
but it might be different when it depends on platform specific tools (logs,
binders, etc).

![Java CI with Maven](https://github.com/xioxoz/droid-core/workflows/Java%20CI%20with%20Maven/badge.svg?branch=master)

How to
------

### Build

```
mvn package
```

### Run tests

```
mvn test
```

### Coverage report

After running build and tests:
```
mvn jacoco:report
```
the report will be located in `target/site/jacoco/index.html`.

