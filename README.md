[![Java Version](http://img.shields.io/badge/Java-1.8-blue.svg)](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
[![Download](https://api.bintray.com/packages/julianghionoiu/maven/dev-sourcecode-record/images/download.svg)](https://bintray.com/julianghionoiu/maven/dev-sourcecode-record/_latestVersion)
[![Codeship Status for julianghionoiu/dev-sourcecode-record](https://img.shields.io/codeship/0d0facf0-757b-0135-e8f7-4a0a8123458a/master.svg)](https://codeship.com/projects/244257)
[![Coverage Status](https://img.shields.io/codecov/c/github/julianghionoiu/dev-sourcecode-record.svg)](https://codecov.io/gh/julianghionoiu/dev-sourcecode-record)

Library designed for recording the source code history of a programming sessions.
The file generated is a SRCS file enabled for streaming. (Fragmented Snapshots)

## To use as a library

### Add as Maven dependency

Add a dependency to `tdl:dev-sourcecode-record` in `compile` scope. See `bintray` shield for latest release number.
```xml
<dependency>
  <groupId>ro.ghionoiu</groupId>
  <artifactId>dev-sourcecode-record</artifactId>
  <version>X.Y.Z</version>
</dependency>
```

### Usage

The following example records a folder for 1 hour with 1 snapshot every minute. 
There will be one key snapshot (full dir copy) to 9 patch snapshots.

```java
        CopyFromDirectorySourceCodeProvider sourceCodeProvider = new CopyFromDirectorySourceCodeProvider(
                Paths.get("./sourceCodeDir"));
        String destinationPath = "./sourcecode.srcs";
        SourceCodeRecorder sourceCodeRecorder = new SourceCodeRecorder.Builder(sourceCodeProvider, destinationPath)
                .withTimeSource(new SystemMonotonicTimeSource())
                .withSnapshotEvery(1, TimeUnit.MINUTES)
                .withKeySnapshotSpacing(10)
                .build();


        sourceCodeRecorder.start(Duration.of(1, ChronoUnit.HOURS));
        sourceCodeRecorder.close();
```

To monitor the **recording progress** you register a `SourceCodeRecordingListener`. 
The following example displays the metrics to the screen every 1 minute:

```java
        SourceCodeMetricsCollector sourceCodeMetricsCollector = new SourceCodeMetricsCollector();
        SourceCodeRecorder sourceCodeRecorder = new SourceCodeRecorder.Builder(sourceCodeProvider, destinationPath)
                .withRecordingListener(recordingMetricsCollector)
                .build();
        
        //Issue performance updates
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                System.out.println("Recorded "+sourceCodeMetricsCollector.getTotalSnapshots() + " snapshots");
            }
        }, 0, 5000);
```

To **gracefully stop the recording** you must ensure that you call the `stop()` on the recording.
You do this by registering `shutdownHook`:
```java
        final Thread mainThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            sourceCodeRecorder.stop();
            try {
                mainThread.join();
            } catch (InterruptedException e) {
                log.warn("Could not join main thread", e);
            }
        }));
```

## Working with the sourcecode stream (SRCS)

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


## The SRCS file format

TODO 

## Development

### Build and run as command-line app

This will create a runnable fat jar:
```
./gradlew build shadowJar -i
```

You can use the app to record changes in a folder
```
java -jar build/libs/dev-sourcecode-record-0.0.2-SNAPSHOT-all.jar record \
    --source xyz/a_source --output snapshot.srcs
```

### Install to mavenLocal

If you want to build the SNAPSHOT version locally you can install to the local Maven cache
```
./gradlew -x test clean install
```

### Release to jcenter and mavenCentral

The CI server is configured to pushs release branches to Bintray.
You trigger the process by running the `release` command locally. 

The command will increment the release number and create and annotated tag:
```bash
./gradlew release
git push --tags
```