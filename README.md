[![Java Version](http://img.shields.io/badge/Java-1.8-blue.svg)](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
[![Download](https://api.bintray.com/packages/julianghionoiu/maven/dev-sourcecode-record/images/download.svg)](https://bintray.com/julianghionoiu/maven/dev-sourcecode-record/_latestVersion)
[![Codeship Status for julianghionoiu/dev-sourcecode-record](https://img.shields.io/codeship/0d0facf0-757b-0135-e8f7-4a0a8123458a/master.svg)](https://codeship.com/projects/244257)
[![Coverage Status](https://coveralls.io/repos/github/julianghionoiu/dev-sourcecode-record/badge.svg?branch=master)](https://coveralls.io/github/julianghionoiu/dev-sourcecode-record?branch=master)

## Compiling

```
./gradlew build shadowJar
```

## Running

```
java -jar build/libs/record-dir-and-upload-all.jar <dir> <output>
```