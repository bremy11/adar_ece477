#!/bin/bash

javac -cp json-simple-1.1.1.jar boilerServer.java
java -cp .:"json-simple-1.1.1.jar:mysql-connector-java-5.1.22-bin.jar" boilerServer
