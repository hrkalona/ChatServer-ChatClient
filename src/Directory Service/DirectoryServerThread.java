/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hrkalona2
 */
public class DirectoryServerThread extends Thread  {
  protected DatagramSocket socket = null;
  protected ArrayList<Record> records = null;
  protected static final int MAX_SIZE = 4096;
  protected boolean running;
  protected int server_PORT;
          
    public DirectoryServerThread() throws IOException {
        this("ServerThread");
    }
 
    public DirectoryServerThread(String name) {
        
        super(name);
        
        server_PORT = 4445;
        try {
            socket = new DatagramSocket(server_PORT);
        } 
        catch (SocketException ex) {
            System.out.println("The port is already in use. Terminating.");
            return;
        }
        
        running = true;
        
        loadDirectory();
 
    }
 
    public void run() {
        
        try {
            System.out.println("Directory Service.\n");
            System.out.println("IP: " + InetAddress.getLocalHost().getHostAddress() + "\nPORT: " + server_PORT + "\n");
        } 
        catch (UnknownHostException ex) {}
 
        while (running) {
            try {
                byte[] buf = new byte[91];
 
                // receive request
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                
                System.out.print("\nNew packet recieved from, " + packet.getAddress().getHostAddress());
                
                switch (new Byte(buf[0]).intValue()) {
                    case 0x01:
                        System.out.println(", add request.\n");
                        buf = addRecord(buf);
                        break;
                    case 0x03:
                        System.out.println(", delete request.\n");
                        buf = deleteRecord(buf);
                        break;
                    case 0x05:
                        System.out.println(", search request.\n");
                        buf = searchRecord(buf);
                        break;
                    default:
                        System.out.println(", unknown request.\n");
                        buf[0] = new Integer(0x00).byteValue();
                        break;
                }
                 
                InetAddress address = packet.getAddress();
                int port = packet.getPort();
                packet = new DatagramPacket(buf, buf.length, address, port);
                socket.send(packet);
            } 
            catch (IOException e) {
                System.out.println("An exception has occured while running the server.\nTerminating.");
                running = false;
            }
        }
        socket.close();
    }
 
    
    
    protected byte[] addRecord(byte[] buf) {
                
        int temp;
        
        if(records.size() < MAX_SIZE) {
            int i;
            for(i = 0; buf[i + 1] != 0x00; i++) {}
        
            byte[] name_bytes = new byte[i];
        
            for(i = 0; i < name_bytes.length; i++) {
                name_bytes[i] = buf[i + 1];   
            }
            String name = new String(name_bytes); 
        
            for(i = 0; buf[i + 65] != 0x00; i++) {}
        
            byte[] ip_bytes = new byte[i];
        
            for(i = 0; i < ip_bytes.length; i++) {
                ip_bytes[i] = buf[i + 65];
            }
            String IP = new String(ip_bytes);                         
            
            int PORT = ((buf[81] & 0xFF) << 8) + (buf[82] & 0xFF);
        
            for(i = 0; buf[i + 83] != 0x00; i++) {}
        
            byte[] properties_bytes = new byte[i];
        
            for(i = 0; i < properties_bytes.length; i++) {
                properties_bytes[i] = buf[i + 83];
            }
            String properties = new String(properties_bytes);
        
            records.add(new Record(name, IP, PORT, properties));
                       
            saveDirectory();
            
            System.out.println("New record added,\n      Name: " + name + "\n        IP: " + IP + "\n      PORT: " + PORT + "\nProperties: " + properties);
            
            temp = 0x01;
            
        }
        else {
            temp = 0x02;
            System.out.println("Not enough space to add the record.");
        }
        
        //RESPONSE
        
        buf = new byte[2];
        
        buf[0] = new Integer(0x02).byteValue();
        buf[1] = new Integer(temp).byteValue();
        
        return buf;
        
    }
    
    protected byte[] deleteRecord(byte[] buf) {
        
        int i;
        for(i = 0; buf[i + 1] != 0x00; i++) {}
        
        byte[] name_bytes = new byte[i];
        
        for(i = 0; i < name_bytes.length; i++) {
            name_bytes[i] = buf[i + 1];   
        }
        String name = new String(name_bytes);
        
        for(i = 0; buf[i + 65] != 0x00; i++) {}
        
        byte[] ip_bytes = new byte[i];
        
        for(i = 0; i < ip_bytes.length; i++) {
            ip_bytes[i] = buf[i + 65];
        }
        String IP = new String(ip_bytes);                       
            
        int PORT = ((buf[81] & 0xFF) << 8) + (buf[82] & 0xFF);
             
        int temp = 0x02;
        
        for(i = 0; i < records.size(); i++) {
            if(records.get(i).getName().equals(name) && records.get(i).getIP().equals(IP) && records.get(i).getPORT() == PORT) {
                System.out.println("Removed record,\n      Name: " + name + "\n        IP: " + IP + "\n      PORT: " + PORT + "\nProperties: " + records.get(i).getProperties());
                records.remove(i);
                saveDirectory();              
                temp = 0x01;
                break;
            }
        }
        
        if(temp == 0x02) {
            System.out.println("No such record found.");
        }
        
        //RESPONSE
            
        buf = new byte[2];
        
        buf[0] = new Integer(0x04).byteValue();
        buf[1] = new Integer(temp).byteValue();
        
        return buf;       
        
    }
    
    protected byte[] searchRecord(byte[] buf) {
        
        int i;
        for(i = 0; buf[i + 1] != 0x00; i++) {}
        
        byte[] name_bytes = new byte[i];
        
        for(i = 0; i < name_bytes.length; i++) {
            name_bytes[i] = buf[i + 1];   
        }
        String name = new String(name_bytes);
        
        ArrayList<Record> found_records = new ArrayList<Record>();
        
        for(i = 0; i < records.size() && found_records.size() < 727; i++) {
            if(records.get(i).getName().startsWith(name)) {
                found_records.add(records.get(i));
            }
        }
        
        //RESPONSE
        
        buf = new byte[3 + found_records.size() * 90];
        
        buf[0] = new Integer(0x06).byteValue();
        
        buf[1] = (byte)(found_records.size() >>> 8);
        buf[2] = (byte)(found_records.size());
        
        for(int k = 0; k < found_records.size(); k++) {
            name_bytes = found_records.get(k).getName().getBytes();
        
            int j;
            for(i = 2 + k * 90 + 1, j = 0; j < name_bytes.length && i < 2 + k * 90 + 64; i++, j++) {
                buf[i] = name_bytes[j];
            }
            buf[i] = new Integer(0x00).byteValue();
        
            byte[] ip_bytes = found_records.get(k).getIP().getBytes();
        
            for(i = 2 + k * 90 + 65, j = 0; j < ip_bytes.length && i < 2 + k * 90 + 81; i++, j++) {                
                buf[i] = ip_bytes[j];
            }
            buf[i] = new Integer(0x00).byteValue();
         
            buf[2 + k * 90 + 81] = (byte)(found_records.get(k).getPORT() >>> 8);
            buf[2 + k * 90 + 82] = (byte)(found_records.get(k).getPORT());
                            
            byte[] properties_bytes = found_records.get(k).getProperties().getBytes();
        
            for(i = 2 + k * 90 + 83, j = 0; j < properties_bytes.length && i < 2 + k * 90 + 90; i++, j++) {
                buf[i] = properties_bytes[j];
            }
            buf[i] = new Integer(0x00).byteValue();
                
        }
        
        if(found_records.isEmpty()) {
            System.out.println("No records found, starting with " + name + " as a prefix.");
        }
        else {
            System.out.println(found_records.size() + " records found, starting with " + name + " as a prefix.");
        }
      
        
        return buf;
        
    }
    
    private void loadDirectory() {

        ObjectInputStream file = null;
        try {
           file = new ObjectInputStream(new FileInputStream("directory.dat"));
           records = (ArrayList<Record>) file.readObject();
        }
        catch(IOException ex) {
            records = new ArrayList<Record>();
        }
        catch(ClassNotFoundException ex) {
            records = new ArrayList<Record>();
        }

        try {
            file.close();
        }
        catch(Exception ex) {}

    }
    
     private void saveDirectory() {

        ObjectOutputStream file = null;

        try {
            file = new ObjectOutputStream(new FileOutputStream("directory.dat"));
            file.writeObject(records);
            file.flush();
        }
        catch(IOException ex) {}

        try {
            file.close();
        }
        catch(Exception ex) {}

    }
    
}
