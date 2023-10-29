#!/usr/bin/env bash

phase="2"
while getopts "p:" flag;
do
  case "${flag}" in
    p) phase=${OPTARG};;
  esac
done

if [ ! $phase == "1" ] && [ ! $phase == "2" ]
then
  phase="2";
fi
echo "Running with entry point Phase${phase}App."

if [ -f "phase${phase}_impl.jar" ]
then
  export CLASSPATH=./bin/:phase2.jar
  rmiregistry &
  sleep 2
  rmiregistrypid=$!

  java -enableassertions -jar "phase${phase}_impl.jar"
  echo $?

  kill $rmiregistrypid
elif [ $phase == "2" ]
then
  echo "phase${phase}_impl.jar could not be found, build project by either using ./build.sh or ./build.sh -p ${phase} before running with either ./run.sh or ./run.sh -p ${phase}."
else
  echo "phase${phase}_impl.jar could not be found, build project using ./build.sh -p ${phase} before running with ./run.sh -p ${phase}."
fi

# lsof -i :1099
