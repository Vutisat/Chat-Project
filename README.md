Chat-Project
============

A java chat room server program.

The chat server will maintain mappings of peer screen names to IP addresses and port numbers. 
The IP addresses and port numbers are used by peers to listen for connections from other peers. 
When peers start, they connect to the server and supply their screen name the and the IP address 
and port number of the ServerSocket they will be using to listen for other clients to connect with them. 
The peer will then periodically contact the server and request that the server send the screen name, 
IP address, and port number for every other peer that is currently in the system.
