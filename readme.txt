A chat server/client GUI implementation, using java and udp sockets.

The chat server registers to the directory service (send its ip and port).
The chat client sends a request to the directory service to obtain the chat server's info.
The client can create channels.
The client can join channels.
The client has sound notifications for various events.
Sorry, for using all the usual chat sounds!

Every message send through udp needs to be acknowledged, since udp doesnt provide any guarantee.