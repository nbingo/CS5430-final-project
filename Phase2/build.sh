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
echo "Building with entry point Phase${phase}App."

if [ ! -d "./bin/" ] 
then
  mkdir bin/
fi

javac -d "./bin/" -cp "./bin/:./phase2.jar" ./src/types/requests/implementation/*.java
javac -d "./bin/" -cp "./bin/:./phase2.jar" ./src/types/responses/implementation/*.java
javac -d "./bin/" -cp "./bin/:./phase2.jar" ./src/types/acl/implementation/*.java
javac -d "./bin/" -cp "./bin/:./phase2.jar" ./src/phase0/implementation/*.java
javac -d "./bin/" -cp "./bin/:./phase2.jar" ./src/phase1/server/implementation/*.java
javac -d "./bin/" -cp "./bin/:./phase2.jar" ./src/phase2/server/implementation/*.java
javac -d "./bin/" -cp "./bin/:./phase2.jar" ./src/network/implementation/*.java
javac -d "./bin/" -cp "./bin/:./phase2.jar" ./src/phase1/stub/implementation/*.java
javac -d "./bin/" -cp "./bin/:./phase2.jar" ./src/phase2/stub/implementation/*.java
javac -d "./bin/" -cp "./bin/:./phase2.jar" ./src/phase1/app/*.java
javac -d "./bin/" -cp "./bin/:./phase2.jar" ./src/phase2/app/*.java

cp phase2.jar bin/
cd bin
touch "MANIFEST.MF"
if [ $phase == "2" ]
then 
  printf "Main-Class: phase2.app.Phase2App\nClass-Path: phase2.jar\n" > "./MANIFEST.MF";
  jar_out="phase2_impl.jar"
else
  printf "Main-Class: phase1.app.Phase1App\nClass-Path: phase2.jar\n" > "./MANIFEST.MF";
  jar_out="phase1_impl.jar";
fi
jar -cfm $jar_out "./MANIFEST.MF" \
  ./types/requests/implementation/*.class \
  ./types/responses/implementation/*.class \
  ./types/acl/implementation/*.class \
  ./phase0/implementation/*.class \
  ./phase1/server/implementation/*.class \
  ./phase2/server/implementation/*.class \
  ./network/implementation/*.class \
  ./phase1/stub/implementation/*.class \
  ./phase2/stub/implementation/*.class \
  ./phase1/app/*.class \
  ./phase2/app/*.class
cp $jar_out ..
cd ..
