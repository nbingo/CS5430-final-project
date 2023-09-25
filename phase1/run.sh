#!/usr/bin/env bash

if [ -f "phase1_impl.jar" ]
then
  export CLASSPATH=./bin/:phase1.jar
  rmiregistry &
  sleep 2
  rmiregistrypid=$!

  java -enableassertions -jar "phase1_impl.jar"
  echo $?

  kill $rmiregistrypid
else
  echo "phase1_impl.jar could not be found, build project using ./build.sh before running with ./run.sh."
fi


# lsof -i :1099
