#!/bin/bash
./gradlew shadowJar
# scp ./build/libs/incubator-0.0.1-all.jar aliyun:/home/pcrab/jar
# java -jar ./build/libs/incubator-0.0.1-all.jar "./build/libs/secret.json"
