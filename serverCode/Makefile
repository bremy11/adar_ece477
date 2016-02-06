

all: boilerServer cClient serverTestClient

serverTestClient: serverTestClient.java
	./makeTestClient.sh

cClient: testCClient.c
	gcc -o cClient testCClient.c

boilerServer: boilerServer.java
	./makeServer.sh

clean: 
	rm cClient
	rm *.class
