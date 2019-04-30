
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Calendar;

import javax.swing.*;

public class Receiver implements ActionListener {
	
	final JButton transfer = new JButton("TRANSFER");
	
	final JTextField ip = new JTextField(8);
	final JTextField ackport = new JTextField(4);
	final JTextField dataport = new JTextField(4);
	final JTextField fileName = new JTextField(15);
	
	final JLabel ip_label = new JLabel("IP of Sender");
	final JLabel senderPort = new JLabel("ACK Port");
	final JLabel receiverPort = new JLabel("Data Port");
	final JLabel packets = new JLabel("Number of Received In-Order Packets: 0");
	final JLabel transmissionTime = new JLabel("Transmission Time: 0 ms ");
	final JCheckBox unreliable = new JCheckBox("Unreliable");
	
	final JPanel connectPanel = new JPanel();
	final JPanel filePanel = new JPanel();
	final JPanel packetPanel = new JPanel();
	
	public Receiver() {

		final JFrame guiFrame = new JFrame();
		guiFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		guiFrame.setTitle("CP372-A2");
		guiFrame.setSize(500, 150);
		guiFrame.setLocationRelativeTo(null);
		
		connectPanel.add(ip_label, BorderLayout.PAGE_START);
		connectPanel.add(ip, BorderLayout.PAGE_START);
		connectPanel.add(senderPort, BorderLayout.PAGE_START);
		connectPanel.add(ackport, BorderLayout.PAGE_START);
		connectPanel.add(receiverPort, BorderLayout.PAGE_START);
		connectPanel.add(dataport, BorderLayout.PAGE_START);
		
		filePanel.add(new JLabel("Name of File:"),BorderLayout.PAGE_START);
		filePanel.add(fileName,BorderLayout.PAGE_START);
		filePanel.add(unreliable,BorderLayout.PAGE_START);
		filePanel.add(transfer,BorderLayout.PAGE_START);
		
		
		transfer.addActionListener(new ActionListener() {
			  public void actionPerformed(ActionEvent e) {
			    Thread queryThread = new Thread() {
			      public void run() {
			    	  Receive(e);
			      }
			    };
			    queryThread.start();
			  }
			});
		
		packetPanel.setLayout(new BorderLayout());
		packetPanel.add(packets,BorderLayout.LINE_START);
		packetPanel.add(transmissionTime, BorderLayout.PAGE_END);
		
		
		guiFrame.add(connectPanel, BorderLayout.NORTH);
		guiFrame.add(filePanel, BorderLayout.CENTER);
		guiFrame.add(packetPanel, BorderLayout.SOUTH);

		guiFrame.setVisible(true);

	}

	public void Receive(ActionEvent e) {
		if(e.getSource() == transfer){ 
			
			DatagramSocket receiveSocket = null;
			int counter = 0; 
			
			try { 
				
				receiveSocket = new DatagramSocket(Integer.parseInt(dataport.getText()));
		        DatagramPacket data = new DatagramPacket(new byte[125], 125);       
		        FileOutputStream  newFile = new FileOutputStream(fileName.getText());
		        int seqNum; 
		        int expectedSeqNum = 0; 
		        byte[] send = new byte[125];
		        int dropPacket = 0;
		        long millisStart = Calendar.getInstance().getTimeInMillis();
		        long millisEnd = Calendar.getInstance().getTimeInMillis();
		        while(true){
		        	
		        	receiveSocket.receive(data);
		        	//TODO: Figure out what we want end of transfer datagram to look like, right now a datagram with length = 1
		        	//      is end of transfer datagram 
		        	if(data.getLength() == 1){ 
		        		byte[] eot = new byte[1];
		    			DatagramPacket ack = new DatagramPacket(eot, eot.length, InetAddress.getByName(ip.getText()), Integer.parseInt(ackport.getText()));
		        		receiveSocket.send(ack);
		        		break;
		        	}
		        	
		        	seqNum = data.getData()[0] & 0xFF; 
		        	
		        	//send ack back to sender
		        	if(!unreliable.isSelected() || dropPacket % 10 != 0){ 
		        		
			        	send[0] = (byte) seqNum;
			        	DatagramPacket ack = new DatagramPacket(send , send.length, InetAddress.getByName(ip.getText()), Integer.parseInt(ackport.getText()));
			        	receiveSocket.send(ack);
			        	
			        	//if sequence number is what we expect then write data to file, otherwise drop packet 
			        	if(expectedSeqNum == seqNum){ 
			        		counter++;     	
				        	newFile.write(data.getData(), 1, data.getLength() - 1 );
				        	System.out.println(seqNum);
				        	expectedSeqNum = ((expectedSeqNum + 1) & 0xFF);
			        	} else { 
			        		System.out.println("Out of order packet arrived: " + Integer.toString(seqNum));
			        		counter = 0; 
			        	}
			        	
		        	}
		        	millisEnd = Calendar.getInstance().getTimeInMillis();
		        	update(counter, millisEnd - millisStart);
		        	dropPacket++;
		        }
				
				newFile.close();
				receiveSocket.close();
				
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}

	}
	
	private void update(final int packetCounter, final long time) {
		  SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
		    	packets.setText("Number of Received In-Order Packets: " + Integer.toString(packetCounter));
		    	transmissionTime.setText("Transmission Time: " + Long.toString(time)+ " ms");
		    }
		  });
		}
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}
	public static void main(String[] args) throws IOException {
	    Receiver r = new Receiver();  
    }



}
