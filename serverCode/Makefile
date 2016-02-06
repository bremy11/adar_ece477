

all: boilerServer cClient

serverTestClient: serverTestClient.java
./makeTestClient.sh

cClient: gcc -o cClient testCClient.c

server: boilerServer
./makeServer.sh

boilerServer: boilerServer.java
javac -cp json-simple-1.1.1.jar boilerServer.java

clean: 
rm client

