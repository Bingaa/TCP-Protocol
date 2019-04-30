import java.io.*;
import java.lang.reflect.Array;
import java.net.*;
import java.util.Timer;

public class Sender {
	
	public static void main(String[] args) throws IOException {
		if(args.length != 5){ 
			System.err.println("Missing Arguments! Sender program takes in the following parameters: ");
			System.err.println("<host address of Receiver>"); 
			System.err.println("<UDP port number used by the receiver to receive data from the sender>"); 
			System.err.println("<UDP port number used by the sender to receive ACKs from the receiver>"); 
			System.err.println("<name of the file to be transferred>"); 
			System.err.println("<timeout> integer number for timeout (in microseconds)"); 
			System.exit(0);
		}
		InetAddress receiverIPAddress = null;
		int receiverPortNumber = 0;
		int senderPortNumber = 0;
		File file = null; 
		int timeout = 0;
		try { 
			receiverIPAddress = InetAddress.getByName(args[0]);
			receiverPortNumber = Integer.parseInt(args[1]);
			senderPortNumber = Integer.parseInt(args[2]);
			file = new File(args[3]); 
			
			timeout = Integer.parseInt(args[4]); 
		} catch (Exception e) { 
			System.err.println("Could not parse parameters!");
			System.exit(0);
		}
		
		
		FileInputStream fileStream = new FileInputStream(file);
		Timer timer = new Timer();
		try{ 
			DatagramSocket ackSocket = new DatagramSocket(senderPortNumber);
			ackSocket.setSoTimeout(timeout);
			System.out.println("Sender Host Address: " + ackSocket.getLocalAddress());
			System.out.println("Port Number to Recieve ACKS: " + ackSocket.getLocalPort());
			
			int seqNum = 0; 
			byte[] send = new byte[125];
			int receivedSeqNum; 
			DatagramPacket ack = new DatagramPacket(new byte[125], 125);
			boolean ackReceived, timeoutIncorrectSeqNum; 
			//while there are still bytes to send
			while(fileStream.available() != 0){ 
				//datagram is constructed such that the first byte represents the sequence number and the remaining 124 bytes are for data
				send[0] = (byte) seqNum; 
				fileStream.read(send, 1,124);
				DatagramPacket data = new DatagramPacket( send, send.length, receiverIPAddress, receiverPortNumber);
				ackReceived = false; 
				
				//send datagram and wait for ack			
				while(!ackReceived){ 
					try{ 
						ackSocket.send(data);
						ackSocket.receive(ack);
						ackReceived = true; 
					} catch (SocketTimeoutException e2){ 
						System.out.println("Timeout Occurred!");
					}
				}
				receivedSeqNum = ack.getData()[0] & 0xFF;
				
				timeoutIncorrectSeqNum = false;
				while(receivedSeqNum != seqNum){ 
					try{ 
						if(timeoutIncorrectSeqNum){ 
							ackSocket.send(data);
						}
						ackSocket.receive(ack);
						receivedSeqNum = ack.getData()[0] & 0xFF;
						ackReceived = true;
					}catch (SocketTimeoutException e2){ 
						System.out.println("Timeout Occurred!");
						timeoutIncorrectSeqNum = true; 
					}
					
					
				}
				
				seqNum = ((seqNum + 1) & 0xFF);
			}
			
			byte[] eot = new byte[1];
			DatagramPacket data = new DatagramPacket( eot, eot.length, receiverIPAddress, receiverPortNumber);
			ackReceived = false; 
			while(!ackReceived){ 
				try{ 
					ackSocket.send( data );	
					ackSocket.receive(ack);
					ackReceived = true; 
				} catch (SocketTimeoutException e2){ 
					System.out.println("Timeout Occurred!");
				}
			}
			
		} catch (Exception e){ 
			System.err.println("Error: " + e);
			System.exit(0);
		}
		
		System.exit(0);
		
	}
}
