# TCP-Protocol
Java implementation of a TCP connection to transfer files by making a UDP connection reliable

## How to Run 
1) Go to project repository 
2) javac *
3) java Sender <host address of receiver> <port number to send data> <port number for acks> <file to be transferred> <timeout in microseconds>
4) java Receiver 
5) Fill in UI with same parameters used to run sender and specify file name 
6) Watch file get transferred to Receiver's directory 

This program can be run across a local area network by running ipconfig in command line to get local area IP. Then Sender and Receiver can be run on different devices to complete a file transfer. 
    

