[![Java Version](http://img.shields.io/badge/Java-1.8-blue.svg)](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)

Library designed for recording the source code history of a programming sessions.
The file generated is a SRCS file enabled for streaming. (Fragmented Snapshots)

## To use as a library

### Add as Maven dependency

Add a dependency to `io.accelerate:code-track` in `compile` scope. See `Central` shield for latest release number.
```xml
<dependency>
    <groupId>io.accelerate</groupId>
    <artifactId>code-track</artifactId>
    <version>X.Y.Z</version>
</dependency>
```

### Usage

The following example records a folder for 1 hour with 1 snapshot every minute. 
There will be one key snapshot (full dir copy) to 9 patch snapshots.

```java
        CopyFromDirectorySourceCodeProvider sourceCodeProvider = new CopyFromDirectorySourceCodeProvider(
                Paths.get("./sourceCodeDir"));
        String destinationPath = Paths.get("./sourcecode.srcs");
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
                .withRecordingListener(sourceCodeMetricsCollector)
                .build();
        
        //Issue performance updates
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                System.out.println("Recorded "+sourceCodeMetricsCollector.getTotalSnapshots() + " snapshots"+
                    ", last snapshot processed in "+ sourceCodeRecordingListener.getLastSnapshotProcessingTimeNano() + " nanos");
            }
        }, 0, 60000);
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

Useful git commands
```
# Show the git tags in reverse creation order
git tag --sort=-creatordate 
```



## The SRCS file format

The SRCS file format is divided into two parts.

1. Header

    The header contains:

    | Field       | Bytes  | Type   | Description |
    | ---         | ---    | ---    |  ---        |
    | Magic bytes | 6      | String | Contains string `SRCSTM`. |
    | Timestamp   | 8      | Long   | UNIX timestamp when the snapshot first recorded. |

    All long integer data will be stored in Little Endian format.

2. Segments

    After the header, the body will contains several segments that contains
    snapshot of the working directory. The content of the segments are

    | Field       | Bytes  | Type   | Description |
    | ---         | ---    | ---    | ---         |
    | Magic bytes | 6      | String | This contains either `SRCKEY` or `SRCPTC` magic bytes. The first one means that the segment is Key Snapshot while the latter means Patch Snapshot. Key Snapshot is snapshot that contains the whole files in the working directory at the time snapshot was taken. While Patch Snapshot contains only the difference between the files in the directory and the files taken the previous snapshots. There can be more than one Patch Snapshot between two Key Snapshots. |
    | Timestamp   | 8      | Long   | This timestamp stores the number of seconds since the first snapshot was taken. Naturally the timestamp of the first snapshot is zero. |
    | Size        | 8      | Long   | This contains the size of the payload stored in the end of the segment. |
    | Tag         | 64     | String | This contains the tag name of the current snapshot. When the file is being exported to git repository, the tag will be used to make git tag of the snapshot's git commit. |
    | Checksum    | 20     | Bina   ry | This contains MD5 hash of the payload data for consistency checking. |
    | Payload     | <Size> | Binary | For Key Snapshot, the payload contains Zip data containing the snapshot. As for Patch Snapshot, the payload contains patch in Diff format. The diff data is compressed using Gzip. |

## Development

### Build and run as command-line app

This will create a runnable fat jar:
```
./gradlew build shadowJar -i
```

You can use the app to record changes in a folder
```
java -jar build/libs/dev-sourcecode-record-0.0.25-all.jar record \
    --source xyz/a_source --output snapshot.srcs
```

### Install to mavenLocal

If you want to build the SNAPSHOT version locally you can install to the local Maven cache
```
./gradlew -x test clean install
```

### Running tests

#### JUnit tests

```bash
./gradlew clean test -i
```

### Release

Configure the version inside the "gradle.properties" file

Create publishing bundle into Maven Local
```bash
./gradlew publishToMavenLocal
```

Check Maven Local contains release version:
```
CURRENT_VERSION=$(cat gradle.properties | grep version | cut -d "=" -f2)

ls -l $HOME/.m2/repository/io/accelerate/code-track/${CURRENT_VERSION}
```

Publish to Maven Central Staging repo

### Publish to Maven Central - the manual way

At this point publishing to Maven Central from Gradle is only possible manually.
Things might have changed, check this page:
https://central.sonatype.org/publish/publish-portal-gradle/

Generate the Maven Central bundle:
```
./generateMavenCentralBundle.sh
```

Upload the bundle to Maven Central by clicking the "Publish Component" button.
https://central.sonatype.com/publishing

### To build artifacts in Github

Commit all changes then:
```bash
export RELEASE_TAG="v$(cat gradle.properties | cut -d= -f2)"
git tag -a "${RELEASE_TAG}" -m "${RELEASE_TAG}"
git push --tags
git push
```