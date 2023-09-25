#!/usr/bin/env bash

if [ ! -d "./bin/" ] 
then
  mkdir bin/
fi

javac -d "./bin/" -cp "./bin/:./phase1.jar" ./src/types/requests/implementation/*.java
javac -d "./bin/" -cp "./bin/:./phase1.jar" ./src/types/responses/implementation/*.java
javac -d "./bin/" -cp "./bin/:./phase1.jar" ./src/phase0/implementation/*.java
javac -d "./bin/" -cp "./bin/:./phase1.jar" ./src/phase1/server/implementation/*.java
javac -d "./bin/" -cp "./bin/:./phase1.jar" ./src/network/implementation/*.java
javac -d "./bin/" -cp "./bin/:./phase1.jar" ./src/phase1/stub/implementation/*.java
javac -d "./bin/" -cp "./bin/:./phase1.jar" ./src/phase1/app/*.java

cp phase1.jar bin/
cd bin
touch "MANIFEST.MF"
printf "Main-Class: phase1.app.Phase1App\nClass-Path: phase1.jar\n" > "./MANIFEST.MF"
jar -cfm "phase1_impl.jar" "./MANIFEST.MF" \
  ./types/requests/implementation/*.class \
  ./types/responses/implementation/*.class \
  ./phase0/implementation/*.class \
  ./phase1/server/implementation/*.class \
  ./network/implementation/*.class \
  ./phase1/stub/implementation/*.class \
  ./phase1/app/*.class
cp phase1_impl.jar ..
cd ..
