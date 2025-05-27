#!/bin/bash

# Set JVM memory settings
export JAVA_OPTS="-Xms1g -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+DisableExplicitGC -XX:+UseStringDeduplication -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp/heap_dump.hprof"

echo "Starting PathService with memory settings: $JAVA_OPTS"

# Run the application with Gradle
./gradlew :PathService:bootRun
