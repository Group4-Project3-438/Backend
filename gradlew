#!/bin/sh

APP_HOME=$(cd "${0%/*}" && pwd -P)
CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

if [ -n "$JAVA_HOME" ]; then
  JAVACMD="$JAVA_HOME/bin/java"
else
  JAVACMD="java"
fi

if [ ! -x "$JAVACMD" ]; then
  echo "ERROR: Java not found"
  exit 1
fi

if [ ! -f "$CLASSPATH" ]; then
  echo "ERROR: Missing gradle-wrapper.jar"
  exit 1
fi

exec "$JAVACMD" -Xmx64m -Xms64m -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"