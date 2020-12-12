# networkProgramming

Functional:
1.Start the peer processes: 
a)read the Common.cfg to correctly set the related variables.
b)read the PeerInfo.cfg to correctly and set the bitfield.
c)peer make TCP connects to all peers that started before it. 
d)When a peer is connected to at least one other peer, it starts to exchange pieces as described in the protocol description section. A peer terminates when it finds out that all the peers, not just itself, have downloaded the complete file. 

2.After connection: 25% 
a)Handshake message: Whenever a connection is established between two peers, each of the peers of the connection sends to the other one the handshake message before sending other messages. 
b)Exchange bitfield message. 
c)Send ‘interested’ or ‘not interested’ message.
e)Set optimistically unchoked neighbor every ‘m’ seconds. 

3.File exchange: 30% 
a)Send ‘request’ message. 
b)Send ‘have’ message.
c)Send ‘not interested’ message. 
d)Send ‘interested’ message. 
e)Send ‘piece’ message. 
f)Receive ‘have’ message and update related bitfield. 

To the best of our knowledge everything should be functional. Except for the following:
1. terminating all threads cleanly upon completion.
2. after all files are collected. program must be exited by user manually. Only after this will the log files populate. Due to the threads not closing cleanly one log file may not be generated.
3. Unable to print out completed file
4. choking timer to compare datarates may be faulty.

Contributions are reflected in the commit messages of github. main branch
https://github.com/rahul-rudra71/networkProgramming
