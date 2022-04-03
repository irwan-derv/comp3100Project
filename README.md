# comp3100Project

## Parameters
- `-l`: logging mode - log sent and received messages to console
- `-p <port number>`: specify a port (default 50000)

## Before running the client
In order to run the client in any capacity, it must first be compiled using `javac TCPClient.java`.

### Running the client using demoS1
Simply run `./demoS1.sh -n TCPClient.class` to run through the test suite.

### Running the client with a server
A ds-server server must be started in newline mode with a config supplied before TCPClient is run. To do so you may start ds-server using `./ds-server -c <config-directory> -n`.

While the server is running the client can be started with `java TCPClient`.