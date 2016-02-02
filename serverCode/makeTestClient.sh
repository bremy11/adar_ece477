#!/bin/bash

javac -cp .:json-simple-1.1.1.jar serverTestClient.java
java -cp .:json-simple-1.1.1.jar serverTestClient
