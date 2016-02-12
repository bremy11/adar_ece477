

all: dijkstr boilerServer cClient serverTestClient

dijkstr:
	javac ./dijkstra/*.java
	jar cf ./dijkstra.jar ./dijkstra/*.class

serverTestClient: serverTestClient.java
	./makeTestClient.sh

cClient: testCClient.c
	gcc -o cClient testCClient.c

boilerServer: boilerServer.java
	./makeServer.sh

clean: 
	rm -f dijkstra.jar
	rm -f ./dijkstra/*.class
	rm -f cClient
	rm -f *.class
