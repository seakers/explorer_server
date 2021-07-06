#!/bin/bash

cd  architecture_lib
mvn install:install-file -Dfile=./system-architecture-problems-1.0.jar -DgroupId=seakers -DartifactId=system-architecture-problems -Dversion=1.0 -Dpackaging=jar