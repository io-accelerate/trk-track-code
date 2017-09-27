[![Java Version](http://img.shields.io/badge/Java-1.8-blue.svg)](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
[![Download](https://api.bintray.com/packages/julianghionoiu/maven/dev-sourcecode-record/images/download.svg)](https://bintray.com/julianghionoiu/maven/dev-sourcecode-record/_latestVersion)
[![Codeship Status for julianghionoiu/dev-sourcecode-record](https://img.shields.io/codeship/0d0facf0-757b-0135-e8f7-4a0a8123458a/master.svg)](https://codeship.com/projects/244257)
[![Coverage Status](https://img.shields.io/codecov/c/github/julianghionoiu/dev-sourcecode-record.svg)](https://codecov.io/gh/julianghionoiu/dev-sourcecode-record)

## Compiling

```
./gradlew build shadowJar -i
```

## Running

Recording a folder
```
java -jar build/libs/dev-sourcecode-record-0.0.2-SNAPSHOT-all.jar record \
    --source xyz/a_source --output snapshot.srcs
```

List the contents of a SRCS file
```
java -jar build/libs/dev-sourcecode-record-0.0.2-SNAPSHOT-all.jar list \
    --input snapshot.srcs
```

Export one individual snapshot
```
java -jar build/libs/dev-sourcecode-record-0.0.2-SNAPSHOT-all.jar export \
    --input snapshot.srcs --time 0 --output ./xyz/frames
```

Export entire file to Git
```
java -jar build/libs/dev-sourcecode-record-0.0.2-SNAPSHOT-all.jar convert-to-git \
    --input snapshot.srcs --output ./xyz/to_git
```