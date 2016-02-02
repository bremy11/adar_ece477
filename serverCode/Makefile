

all: JsonEncodeDemo EventServer serverTestClient



JsonEncodeDemo: JsonEncodeDemo.java
javac -cp json-simple-1.1.1.jar JsonEncodeDemo.java

runDemo: JsonEncodeDemo
java -cp .:json-simple-1.1.1.jar JsonEncodeDemo

EventServer: EventServer.java
javac -cp json-simple-1.1.1.jar EventServer.java

runServer: JsonEncodeDemo
java -cp .:"json-simple-1.1.1.jar;mysql-connector-java-5.1.22-bin.jar" EventServer


serverTestClient: serverTestClient.java
javac -cp .:json-simple-1.1.1.jar serverTestClient.java
java -cp .:json-simple-1.1.1.jar serverTestClient

boilerServer: boilerServer.java
javac -cp json-simple-1.1.1.jar boilerServer.java
java -cp .:"json-simple-1.1.1.jar:mysql-connector-java-5.1.22-bin.jar" boilerServer
