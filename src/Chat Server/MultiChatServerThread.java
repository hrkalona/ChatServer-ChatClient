import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class MultiChatServerThread extends Thread {
    private String group_name;
    private String password;
    private int group_port;
    private DatagramSocket socket = null;
    private ArrayList<UserInfo> users;
    private ArrayList<UserInfo> users_to_be_removed;
    private boolean running;
    private Calendar calendar;
    private int max_users;
    private int group_message_counter;
    public static final int MAX_TIMEOUTS = 5;
   
    public MultiChatServerThread(String group_name, String password, int group_port, int max_users) {

	super("MultiChatServerThread");
        this.group_name = group_name;
        this.group_port = group_port;
        this.password = password;
        this.max_users = max_users;
        group_message_counter = 0;
        
        try {
            socket = new DatagramSocket(group_port);
        } 
        catch (SocketException ex) {}
        
        users = new ArrayList<UserInfo>();
        users_to_be_removed = new ArrayList<UserInfo>();
        
        running = true;
       
    }
    
    public String getGroupName() {
        
        return group_name;
        
    }
    
    public int getGroupPort() {
        
        return group_port;
        
    }
    
    public int getMaxUsers() {
        
        return max_users;
        
    }
    
    public ArrayList<UserInfo> getUsers() {
        
        return users;
        
    }
    
    public int isPasswordProtected() {
        
        return password.equals("") ? 0x00 : 0x01;
                
    }
    
    public void terminate() {
        
        if(!users.isEmpty()) {
            disconnectUsers();  
        }
        
        calendar = new GregorianCalendar();
        System.out.println("<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + ">" + " [ " + group_name + " ] Terminating.");
        
        try {
            socket.close();
        }
        catch(Exception ex) {}
        
        stop();
        
    }
    
    private void reliable_send(DatagramSocket socket, DatagramPacket packet, UserInfo user) throws IOException {
      byte[] buf = new byte[1];
      boolean waiting = true;
      int timeouts = 0;
      
 
        byte[] buf2 = new byte[packet.getData().length + 4]; //header
        buf2[0] = (byte)(group_message_counter >>> 24);
        buf2[1] = (byte)(group_message_counter >>> 16);
        buf2[2] = (byte)(group_message_counter >>> 8);
        buf2[3] = (byte)(group_message_counter);
      
        for(int k = 0; k < packet.getData().length; k++) {
            buf2[k + 4] = packet.getData()[k];
        }
        
        packet.setData(buf2);
          
        do {
            
            try {
                socket.send(packet);
                packet = new DatagramPacket(buf, buf.length);
                socket.setSoTimeout(300);
                socket.receive(packet);
                if(new Byte(buf[0]).intValue() == 0x00) {
                    waiting = false;   
                }
            } 
            catch(SocketTimeoutException ex) {}
            catch(NullPointerException ex) {}
                
            
            timeouts++;
            
        } while(waiting && timeouts < MAX_TIMEOUTS);
        
        if(timeouts == MAX_TIMEOUTS) {
            if(user != null) {
                users_to_be_removed.add(user);
            }
        }
               
    }  
    
    private DatagramPacket receive(DatagramSocket socket, DatagramPacket packet) throws SocketTimeoutException, IOException {
     
        packet.setData(new byte[packet.getData().length + 4]);
        
        socket.setSoTimeout(300000); //5 min
        socket.receive(packet);
               
        int message_counter = ((packet.getData()[0] & 0xFF) << 24) + ((packet.getData()[1] & 0xFF) << 16) + ((packet.getData()[2] & 0xFF) << 8) + (packet.getData()[3] & 0xFF);
        int i;
        for(i = 0; i < users.size(); i++) {
            if(packet.getAddress().getHostAddress().equals(users.get(i).getIP()) && packet.getPort() == users.get(i).getSendingPort()) {
                break;   
            }
        }
        
        if(i < users.size()) { //user already in the group
            if(message_counter > users.get(i).getMessageCounter()) { //new message
                byte[] buf = new byte[1];
                buf[0] = new Integer(0x00).byteValue();
                DatagramPacket packet2 = new DatagramPacket(buf, buf.length, packet.getAddress(), packet.getPort());
                socket.send(packet2);
 
                byte[] buf2 = new byte[packet.getData().length - 4];
                
                for(int j = 0; j < buf2.length; j++) {
                    buf2[j] = packet.getData()[j + 4];
                }
        
                packet.setData(buf2);
                
                users.get(i).increaseMessageCounter();
            }
            else { //old
                byte[] buf = new byte[1];
                buf[0] = new Integer(0x00).byteValue();
                DatagramPacket packet2 = new DatagramPacket(buf, buf.length, packet.getAddress(), packet.getPort());
                socket.send(packet2);
 
                byte[] buf2 = new byte[packet.getData().length - 4];
                
                for(int j = 0; j < buf2.length; j++) {
                    buf2[j] = packet.getData()[j + 4];
                }
                
                buf2[0] =  new Integer(0x30).byteValue(); //No type value  
        
                packet.setData(buf2);            
            }
        }
        else { //first message
            if(message_counter == 0) {
                byte[] buf = new byte[1];
                buf[0] = new Integer(0x00).byteValue();
                DatagramPacket packet2 = new DatagramPacket(buf, buf.length, packet.getAddress(), packet.getPort());
                socket.send(packet2);
 
                byte[] buf2 = new byte[packet.getData().length - 4];
                
                for(int j = 0; j < buf2.length; j++) {
                    buf2[j] = packet.getData()[j + 4];
                }
                
                packet.setData(buf2);
            }
            else { //Bad message
                byte[] buf2 = new byte[packet.getData().length - 4];
                
                for(int j = 0; j < buf2.length; j++) {
                    buf2[j] = packet.getData()[j + 4];
                }
                
                buf2[0] =  new Integer(0xFF).byteValue(); //No type value  
        
                packet.setData(buf2);  
            }          
        }
        

        return packet;        
        
    }

    public void run() {
        
        while (running) {
            try {
                byte[] buf = new byte[1 + 17 + 1501];
 
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                
                packet = receive(socket, packet);
                
                buf = packet.getData();
                
                calendar = new GregorianCalendar();
                System.out.print("\n" + "<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + ">" + " [ " + group_name +" ] New packet received from, " + packet.getAddress().getHostAddress());
                              
                switch (new Byte(buf[0]).intValue()) {
                    case 0x0D:
                        System.out.println(", join group request.");
                        joinGroup(buf, packet.getAddress(), packet.getPort());      
                        sendUserList();
                        break;
                    case 0x0F:
                        System.out.println(", message request.");
                        sendMessage(buf);
                        break;
                    case 0x13:
                        System.out.println(", leave group request.");
                        leaveGroup(buf);
                        sendUserList();
                        break;
                    case 0x30:
                        System.out.println(", the same packet was received more than once.");
                        break;
                    default:
                        System.out.println(", unknown request.");
                        break;
                }
                                        
            }
            catch(Exception ex) {
                if(users.isEmpty()) {
                    byte[] buf2 = new byte[18];
                
                    buf2[0] = new Integer(0x09).byteValue();
                
                    byte[] group_name_bytes = group_name.getBytes();
                    
                    int i, j;
                    for(i = 1, j = 0; j < group_name_bytes.length; i++, j++) {
                        buf2[i] = group_name_bytes[j];
                    }
                    buf2[i] = new Integer(0x00).byteValue();
                    
                    DatagramSocket socket2 = null;
                    DatagramPacket packet = null;
                
                    try {
                        socket2 = new DatagramSocket();
                    } 
                    catch (SocketException ex2) {
                        //System.out.println("\nA problem while handling the socket has been occured.");
                    }
                    
                    try {
                        packet = new DatagramPacket(buf2, buf2.length, InetAddress.getLocalHost(), 5503);
                    } 
                    catch (UnknownHostException ex1) {
                        //System.out.println("\nCould not find the specific host.");
                    }
                    
                    try {
                        socket2.send(packet);
                    } 
                    catch (IOException ex2) {
                        //System.out.println("\nA problem while sending the packet has been occured.");
                    }
                
                    packet = new DatagramPacket(buf2, buf2.length);
                    try {
                        socket2.receive(packet);
                    }
                    catch (IOException ex2) {
                        //System.out.println("\nA problem while recieving the response has been occured.");
                        return;
                    }
        
                    if(new Byte(buf2[0]).intValue() == 0x0A) {
                        if(new Byte(buf2[1]).intValue() == 0x00) {
                            //System.out.println("\nThe group was deleted successfully.");
                            running = false;
                        }
                        else if(new Byte(buf2[1]).intValue() == 0x01) {
                            //System.out.println("\nThe group was not found.");
                        }
                    }
                    else {
                        //System.out.println("\nWrong packet received.");
                    }         
                }
            }
            
            if(!users_to_be_removed.isEmpty()) {
                for(int i = 0; !users_to_be_removed.isEmpty(); i++) {
                    userTimedOut(users_to_be_removed.get(i));
                    sendUserList(); 
                    users_to_be_removed.remove(i);
                }
            }
                
        }
        
        calendar = new GregorianCalendar();
        System.out.println("<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + ">" + " [ " + group_name + " ] Terminating.");
        
        try {
            socket.close();
        }
        catch(Exception ex) {}
        
    }
    
    
    
    private void joinGroup(byte[] buf, InetAddress ip_address, int SENDING_PORT) {
      UserInfo user_added = null;
        
        byte[] buf3 = new byte[1 + 1 + 17 + 4];
        
        if(users.size() < max_users) {
            int i;
            for(i = 0; buf[i + 1] != 0x00; i++) {}
        
            byte[] password_bytes = new byte[i];
        
            for(i = 0; i < password_bytes.length; i++) {
                password_bytes[i] = buf[i + 1];   
            }
            String temp_password = new String(password_bytes);
        
            if(temp_password.equals(password)) {
        
                for(i = 0; buf[i + 18] != 0x00; i++) {}
        
                byte[] user_name_bytes = new byte[i];
        
                for(i = 0; i < user_name_bytes.length; i++) {
                    user_name_bytes[i] = buf[i + 18];   
                }
                String user_name = new String(user_name_bytes); 
        
        
                boolean flag = false;
                
                
                buf3[1] = new Integer(0x00).byteValue(); //ok
        
                for(i = 0; i < users.size(); i++) {
                    if(users.get(i).getName().equalsIgnoreCase(user_name)) {
                        flag = true; 
                        buf3[1] = new Integer(0x01).byteValue(); //user already exists
                        
                        calendar = new GregorianCalendar();
                        System.out.println("<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + ">" + " [ " + group_name +" ] The name already exists in the group.");
                    }
                }
                                           
                if(!flag) {
                    
                    byte[] buf2 = new byte[18];
                    
                    DatagramSocket socket2 = null;
                    DatagramPacket packet = null;
                    
                    buf2[0] = new Integer(0x0E).byteValue();
                    
                    int j;
                    for(i = 1, j = 0; j < user_name_bytes.length && i < 17; i++, j++) {
                        buf2[i] = user_name_bytes[j];
                    }
                    buf2[i] = new Integer(0x00).byteValue();
                    
                    int LISTENING_PORT = ((buf[35] & 0xFF) << 8) + (buf[36] & 0xFF);
                   
                    for(i = 0; i < users.size(); i++) {
                         try {
                            socket2 = new DatagramSocket();
                         } 
                         catch (SocketException ex) {
                             //System.out.println("\nA problem while handling the socket has been occured.");
                         }
                         
                         try {
                             packet = new DatagramPacket(buf2, buf2.length, getAddress(users.get(i).getIP()), users.get(i).getListeningPort());
                         } 
                         catch (UnknownHostException ex) {
                             //System.out.println("\nCould not find the specific host.");
                         }
                         
                         try {
                             reliable_send(socket2, packet, users.get(i));
                         } 
                         catch (IOException ex) {
                             //System.out.println("\nA problem while sending the packet has been occured.");
                         }       
                        
                    }
                    
                    
                    UserInfo user = new UserInfo(ip_address.getHostAddress(), LISTENING_PORT, SENDING_PORT, user_name);
                    users.add(user);
                    user_added = user;
                    
                    calendar = new GregorianCalendar();
                    System.out.println("<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + ">" + " [ " + group_name +" ] " + user_name + " has joined the group (" + ip_address.getHostAddress() + ":" + LISTENING_PORT + ").");
                    
                    byte[] group_name_bytes = group_name.getBytes();
                            
                    for(i = 2, j = 0; j < group_name_bytes.length && i < 18; i++, j++) {
                        buf3[i] = group_name_bytes[j];
                    }
                    buf3[i] = new Integer(0x00).byteValue();
       
                    buf3[19] = (byte)(group_message_counter >>> 24);
                    buf3[20] = (byte)(group_message_counter >>> 16);
                    buf3[21] = (byte)(group_message_counter >>> 8);
                    buf3[22] = (byte)(group_message_counter);
                            
                }
            }
            else {
                buf3[1] = new Integer(0x03).byteValue(); //bad password 
                calendar = new GregorianCalendar();
                System.out.println("<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + ">" + " [ " + group_name +" ] The password was incorrect.");
            }
        }
        else {
            buf3[1] = new Integer(0x02).byteValue(); //no space
            calendar = new GregorianCalendar();
            System.out.println("<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + ">" + " [ " + group_name +" ] No space available in the group.");
        }
        
        
        buf3[0] = new Integer(0x0E).byteValue();
        
        DatagramPacket packet = new DatagramPacket(buf3, buf3.length, ip_address, SENDING_PORT);
        
        try {
            reliable_send(socket, packet, user_added);
        } 
        catch (IOException ex) {
            //System.out.println("\nA problem while sending the packet has been occured.");
        }
        
        group_message_counter++;
        
    }
    
    private void sendMessage(byte[] buf) {
        
        buf[0] = new Integer(0x10).byteValue();
        
        DatagramSocket socket2 = null;
        DatagramPacket packet = null;
        
        for(int i = 0; i < users.size(); i++) {
            try {
                socket2 = new DatagramSocket();
            } 
            catch (SocketException ex) {
                //System.out.println("\nA problem while handling the socket has been occured.");
            }
            
            try {
                packet = new DatagramPacket(buf, buf.length, getAddress(users.get(i).getIP()), users.get(i).getListeningPort());
            } 
            catch (UnknownHostException ex) {
                //System.out.println("\nCould not find the specific host.");
            }
            
            try {
                reliable_send(socket2, packet, users.get(i));
            } 
            catch (IOException ex) {
                //System.out.println("\nA problem while sending the packet has been occured.");
            }         
        }
        
        int i;
        for(i = 0; buf[i + 1] != 0x00; i++) {}
                        
        byte[] name_bytes = new byte[i];
                        
        for(i = 0; i < name_bytes.length; i++) {
            name_bytes[i] = buf[i + 1];   
        }
                        
        String name = new String(name_bytes);
                        
        for(i = 0; buf[i + 18] != 0x00; i++) {}
                        
        byte[] message_bytes = new byte[i];
                        
        for(i = 0; i < message_bytes.length; i++) {
            message_bytes[i] = buf[i + 18];   
        }
                        
        String message = new String(message_bytes);
        
        calendar = new GregorianCalendar();
        System.out.println("<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + ">" + " [ " + group_name + " ] " + name + " : " + message);
        
        group_message_counter++;
        
    }
    
    private void sendUserList() {
        
      
        byte[] buf = new byte[3 + 17 * users.size()];
            
        buf[0] = new Integer(0x12).byteValue();
        
        DatagramSocket socket2 = null;
        DatagramPacket packet = null;
        
        buf[1] = (byte)(users.size() >>> 8);
        buf[2] = (byte)(users.size());
        
        int i;
        for(int k = 0; k < users.size(); k++) {
            byte[] name_bytes = users.get(k).getName().getBytes();
            
            int j;
            for(i = 2 + k * 17 + 1, j = 0; j < name_bytes.length && i < 2 + k * 17 + 16; i++, j++) {
                buf[i] = name_bytes[j];
            }
            buf[i] = new Integer(0x00).byteValue();
            
        }
        
        for(i = 0; i < users.size(); i++) {
            try {
                socket2 = new DatagramSocket();
            } 
            catch (SocketException ex) {
                //System.out.println("\nA problem while handling the socket has been occured.");
            }
            
            try {
                packet = new DatagramPacket(buf, buf.length, getAddress(users.get(i).getIP()), users.get(i).getListeningPort());
            } 
            catch (UnknownHostException ex) {
                //System.out.println("\nCould not find the specific host.");
            }
            
            try {
                reliable_send(socket2, packet, users.get(i));
            } 
            catch (IOException ex) {
                //System.out.println("\nA problem while sending the packet has been occured.");
            }  
        }
        
        group_message_counter++;
        
    }
    
    private void leaveGroup(byte[] buf) {
                
        int i;
        for(i = 0; buf[i + 1] != 0x00; i++) {}
        
        byte[] name_bytes = new byte[i];
        
        for(i = 0; i < name_bytes.length; i++) {
            name_bytes[i] = buf[i + 1];   
        }
        String name = new String(name_bytes);
        
        for(i = 0; i < users.size(); i++) {
            if(users.get(i).getName().equalsIgnoreCase(name)) {
                users.remove(i);
                
                calendar = new GregorianCalendar();
                System.out.println("<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + ">" + " [ " + group_name +" ] " + name + " has left the group (Disconnect by user).");
                 
                byte[] buf2 = new byte[19];
                
                DatagramSocket socket2 = null;
                DatagramPacket packet = null;
                
                buf2[0] = new Integer(0x14).byteValue();
                
                for(int k = 0; k < users.size(); k++) {
                    try {
                        socket2 = new DatagramSocket();
                    } 
                    catch (SocketException ex) {
                        //System.out.println("\nA problem while handling the socket has been occured.");
                    }

                    buf2[1] = new Integer(0x00).byteValue(); // Disconnect by user.
                    
                    int j, l;
                    for(j = 0, l = 2; j < name_bytes.length; j++, l++) {
                        buf2[l] = name_bytes[j];  
                    }
                    buf2[l] = new Integer(0x00).byteValue();
                    
                    try {
                        packet = new DatagramPacket(buf2, buf2.length, getAddress(users.get(k).getIP()), users.get(k).getListeningPort());
                    } 
                    catch (UnknownHostException ex) {
                        //System.out.println("\nCould not find the specific host.");
                    }
                    
                    try {
                        reliable_send(socket2, packet, users.get(k));
                    } 
                    catch (IOException ex) {
                        //System.out.println("\nA problem while sending the packet has been occured.");
                    }       
                }
                break;
            }
        }
        
        group_message_counter++;
              
    }
    
    private void disconnectUsers() {
        
        byte[] buf = new byte[1];
                
        DatagramSocket socket2 = null;
        DatagramPacket packet = null;
                
        buf[0] = new Integer(0x15).byteValue();
                
        for(int k = 0; k < users.size(); k++) {
            try {
                socket2 = new DatagramSocket();
            } 
            catch (SocketException ex) {
                //System.out.println("\nA problem while handling the socket has been occured.");
            }
            
            try {
                packet = new DatagramPacket(buf, buf.length, getAddress(users.get(k).getIP()), users.get(k).getListeningPort());
            } 
            catch (UnknownHostException ex) {
                //System.out.println("\nCould not find the specific host.");
            }
            
            try {
                reliable_send(socket2, packet, users.get(k));
            } 
            catch (IOException ex) {
                //System.out.println("\nA problem while sending the packet has been occured.");
            }
            
        }
        
        for(int k = 0; k < users.size(); k++) {
            users.remove(k);
        }
        
        group_message_counter++;
       
    }
    
    private void userTimedOut(UserInfo user) {
        
        String name = user.getName();
        
        calendar = new GregorianCalendar();
        System.out.println("<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + ">" + " [ " + group_name +" ] " + name + " has left the group (User timed out).");
        
        users.remove(user);
               
        byte[] buf2 = new byte[19];
                
        DatagramSocket socket2 = null;
        DatagramPacket packet = null;
                
        buf2[0] = new Integer(0x14).byteValue();
                
        for(int k = 0; k < users.size(); k++) {
            try {
                socket2 = new DatagramSocket();
            } 
            catch (SocketException ex) {
                //System.out.println("\nA problem while handling the socket has been occured.");
            }
                    
            buf2[1] = new Integer(0x01).byteValue(); // User timed out.
            
            byte[] name_bytes = name.getBytes();
                                    
            int j, l;
            for(j = 0, l = 2; j < name_bytes.length; j++, l++) {
                buf2[l] = name_bytes[j];  
            }
            buf2[l] = new Integer(0x00).byteValue();
            
            try {
                packet = new DatagramPacket(buf2, buf2.length, getAddress(users.get(k).getIP()), users.get(k).getListeningPort());
            } 
            catch (UnknownHostException ex) {
                //System.out.println("\nCould not find the specific host.");
            }
            
            try {
                reliable_send(socket2, packet, users.get(k));
            } 
            catch (IOException ex) {
                //System.out.println("\nA problem while sending the packet has been occured.");
            } 
        }
        
        group_message_counter++;
        
    }
    
    private InetAddress getAddress(String IP) throws UnknownHostException {

        InetAddress address = null;

        address = InetAddress.getByName(IP);
  
        return address;

    }
    
}
