#!/bin/bash

cd  vassar_lib
mvn install:install-file -Dfile=./vassar-1.0.jar -DgroupId=seakers -DartifactId=vassar -Dversion=1.0 -Dpackaging=jar