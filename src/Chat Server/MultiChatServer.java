import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class MultiChatServer extends Thread implements DirectoryService {
  private int server_port;
  private DatagramSocket socket = null;
  private boolean running;
  private ArrayList<MultiChatServerThread> groups;
  private ArrayList<Group> group_settings;
  private Calendar calendar;
  private String DIRECTORY_SERVER_IP;
  private int DIRECTORY_SERVER_PORT;
  public static final int MAX_GROUPS = 200;
  
    public MultiChatServer() {
        server_port = 5503;
        
        try {
            socket = new DatagramSocket(server_port);
        } 
        catch (SocketException ex) {}
        
        groups = new ArrayList<MultiChatServerThread>();
        loadGroups();
        
        for(int i = 0; i < group_settings.size(); i++) {
            MultiChatServerThread thread = new MultiChatServerThread(group_settings.get(i).getGroupName(), group_settings.get(i).getPassword(), group_settings.get(i).getGroupPort(), group_settings.get(i).getMaxUsers());
            groups.add(thread);
            thread.start(); 
            calendar = new GregorianCalendar();
            System.out.println("<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + ">" + " [ Main Server ] Loading group.");
            calendar = new GregorianCalendar();
            try {
                System.out.println("<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + ">" + " [ Main Server ] Group name: " + group_settings.get(i).getGroupName() + ", IP: " + InetAddress.getLocalHost().getHostAddress() + ", PORT: " + group_settings.get(i).getGroupPort() + " , MAX USERS: " + group_settings.get(i).getMaxUsers() + ", PASSWORD: [" + group_settings.get(i).getPassword() + "]");
            } 
            catch (UnknownHostException ex) {}
        }
        
        try {
            BufferedReader br = new BufferedReader(new FileReader("./directory_server.ini"));
            String temp = br.readLine();
            
            if(temp.substring(0, 3).equalsIgnoreCase("IP=")) {
                DIRECTORY_SERVER_IP = temp.substring(3, temp.length());
            }
            else if(temp.substring(0, 5).equalsIgnoreCase("PORT=")) {
                DIRECTORY_SERVER_PORT = Integer.parseInt(temp.substring(5, temp.length()));
            }
            temp = br.readLine();
            if(temp.substring(0, 3).equalsIgnoreCase("IP=")) {
                DIRECTORY_SERVER_IP = temp.substring(3, temp.length());
            }
            else if(temp.substring(0, 5).equalsIgnoreCase("PORT=")) {
                DIRECTORY_SERVER_PORT = Integer.parseInt(temp.substring(5, temp.length()));
            }
            else {
                throw new Exception();
            }
        } 
        catch (FileNotFoundException ex) {
            DIRECTORY_SERVER_IP = "127.0.0.1";
            DIRECTORY_SERVER_PORT = 0;
        }
        catch (IOException ex) {
            DIRECTORY_SERVER_IP = "127.0.0.1";
            DIRECTORY_SERVER_PORT = 0;
        }
        catch (Exception ex) {
            DIRECTORY_SERVER_IP = "127.0.0.1";
            DIRECTORY_SERVER_PORT = 0;
        }
        
        try {
            removeRecord("ChatServerUDP", InetAddress.getLocalHost().getHostAddress(), server_port);
            addRecord("ChatServerUDP", InetAddress.getLocalHost().getHostAddress(), server_port, "0");
        } 
        catch (UnknownHostException ex) {
            calendar = new GregorianCalendar();
            System.out.println("\n<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + ">" + " [ Main Server ] A problem has beed occured while trying to update the record on the directory server.");
        }
        
        running = true;
        
    }
    
    @Override
    public void run() {
        
        if(socket == null) {
            calendar = new GregorianCalendar();
            System.out.println("\n<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + ">" + " [ Main Server ] A problem has beed occured while trying to start the chat server. Terminating.");
            System.exit(0);
        }
        
        
        try {
            calendar = new GregorianCalendar();
            System.out.println("\n<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + ">" + " [ Main Server ] The chat server is running.");
            calendar = new GregorianCalendar();
            System.out.println("<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + ">" + " [ Main Server ] IP: " + InetAddress.getLocalHost().getHostAddress() + ", PORT: " + server_port);
            calendar = new GregorianCalendar();
            System.out.println("<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + ">" + " [ Main Server ] To terminate, type exit.");
        } 
        catch (UnknownHostException ex) {}
        
        while (running) {
            
            try {
                byte[] buf = new byte[1 + 17 + 17 + 2];
 
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                
                socket.setSoTimeout(5000); //5 sec
                socket.receive(packet);
                
                calendar = new GregorianCalendar();
                System.out.print("\n" + "<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + ">" + " [ Main Server ] New packet recieved from, " + packet.getAddress().getHostAddress());
                
                switch (new Byte(buf[0]).intValue()) {
                    case 0x07:
                        System.out.println(", create group request.");
                        buf = createGroup(buf);
                        break;
                    case 0x09:
                        System.out.println(", delete group request.");
                        buf = deleteGroup(buf);
                        break;
                    case 0x0B:
                        System.out.println(", get groups list request.");
                        buf = getGroups();
                        break;
                    default:
                        System.out.println(", unknown request.");
                        buf[0] = new Integer(0x00).byteValue();
                        break;
                }
 
                                                
                packet = new DatagramPacket(buf, buf.length, packet.getAddress(), packet.getPort());
                socket.send(packet);
            } 
            catch(SocketTimeoutException ex) {}
            catch (IOException e) {
                calendar = new GregorianCalendar();
                System.out.println("<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + ">" + " [ Main Server ] An exception has occured while running the server.");
                terminate();
            }
            
        }
        
        calendar = new GregorianCalendar();
        System.out.println("<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + ">" + " [ Main Server ] Terminating.");
        socket.close();
               
    }
    
    private byte[] createGroup(byte[] buf) {
        
        int i;
        for(i = 0; buf[i + 1] != 0x00; i++) {}
        
        byte[] group_name_bytes = new byte[i];
        
        for(i = 0; i < group_name_bytes.length; i++) {
            group_name_bytes[i] = buf[i + 1];   
        }
        String group_name = new String(group_name_bytes); 
        
        boolean name_flag = false;
        
        
        for(i = 0; i < groups.size(); i++) {
            if(groups.get(i).getGroupName().equalsIgnoreCase(group_name)) {
                name_flag = true;
            }
        }
        
        byte[] buf2 = new byte[2];
        
        buf2[1] = new Integer(0x00).byteValue(); //ok
        
        if(!name_flag) {
            if(groups.size()  < MAX_GROUPS) {
                
               for(i = 0; buf[i + 18] != 0x00; i++) {}
        
               byte[] password_bytes = new byte[i];
        
               for(i = 0; i < password_bytes.length; i++) {
                   password_bytes[i] = buf[i + 18];   
               }
               String password = new String(password_bytes); 
               
               int max_users = ((buf[35] & 0xFF) << 8) + (buf[36] & 0xFF);
                 
                int PORT = 0;
                try {
                    ServerSocket test = new ServerSocket(0);
                    PORT = test.getLocalPort();
                    test.close();
                } 
                catch (IOException ex) {}
                try {
                    calendar = new GregorianCalendar();
                    System.out.println("<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + ">" + " [ Main Server ] A new group has beed created.");
                    calendar = new GregorianCalendar();
                    System.out.println("<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + ">" + " [ Main Server ] Group name: " + group_name + ", IP: " + InetAddress.getLocalHost().getHostAddress() + ", PORT: " + PORT + " ,MAX USERS: " + max_users + ", PASSWORD: [" + password + "]");
                } 
                catch (UnknownHostException ex) {}
                
                group_settings.add(new Group(group_name, password, PORT, max_users));
                saveGroups();
                
                MultiChatServerThread thread = new MultiChatServerThread(group_name, password, PORT, max_users);
                groups.add(thread);
                thread.start();                       
            }
            else {
                buf2[1] = new Integer(0x02).byteValue(); //no space
            }
        }
        else {
            buf2[1] = new Integer(0x01).byteValue(); // name taken
        }
        
        buf2[0] = new Integer(0x08).byteValue();
                                  
        return buf2;
        
    }
    
    private byte[] deleteGroup(byte[] buf) {
        
        int i;
        for(i = 0; buf[i + 1] != 0x00; i++) {}
        
        byte[] group_name_bytes = new byte[i];
        
        for(i = 0; i < group_name_bytes.length; i++) {
            group_name_bytes[i] = buf[i + 1];   
        }
        String group_name = new String(group_name_bytes);
        
        byte[] buf2 = new byte[2];
        
        buf2[1] = new Integer(0x01).byteValue(); //not found
        
        for(i = 0; i < groups.size(); i++) {
            if(groups.get(i).getGroupName().equalsIgnoreCase(group_name)) {
                group_settings.remove(i);
                saveGroups();
                
                groups.remove(i);
                buf2[1] = new Integer(0x00).byteValue(); //ok
                calendar = new GregorianCalendar();
                System.out.println("<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + ">" + " [ Main Server ] The group, " + group_name + " was deleted, because it was empty for too long.");
            }
        }
        
        buf2[0] = new Integer(0x0A).byteValue();
        
        
        return buf2;
        
    }
    
    private byte[] getGroups() {
        
        byte[] buf = new byte[3 + (17 + 2 + 2 + 2 + 1) * groups.size()];
        
        buf[0] = new Integer(0x0C).byteValue();
        
        buf[1] = (byte)(groups.size() >>> 8);
        buf[2] = (byte)(groups.size()); 
        
        for(int k = 0; k < groups.size(); k++) {
            
            byte[] group_name_bytes = groups.get(k).getGroupName().getBytes();
        
            int i, j;
            for(i = 3 + k * 24, j = 0; j < group_name_bytes.length; i++, j++) {
                buf[i] = group_name_bytes[j];
            }
            buf[i] = new Integer(0x00).byteValue();
        
            buf[20 + k * 24] = (byte)(groups.get(k).getGroupPort() >>> 8);
            buf[21 + k * 24] = (byte)(groups.get(k).getGroupPort());
        
            buf[22 + k * 24] = (byte)(groups.get(k).getUsers().size() >>> 8);
            buf[23 + k * 24] = (byte)(groups.get(k).getUsers().size());
        
            buf[24 + k * 24] = (byte)(groups.get(k).getMaxUsers() >>> 8);
            buf[25 + k * 24] = (byte)(groups.get(k).getMaxUsers());
        
            buf[26 + k * 24] = new Integer(groups.get(k).isPasswordProtected()).byteValue();
            
        }
        
        return buf;
        
    }
    
    public void terminate() {
        
        
        for(int i = 0; i < groups.size(); i++) {
            groups.get(i).terminate();
        }
        
        running = false;
        
    }
    
    private void loadGroups() {

        ObjectInputStream file = null;
        try {
           file = new ObjectInputStream(new FileInputStream("groups.dat"));
           group_settings = (ArrayList<Group>) file.readObject();
        }
        catch(IOException ex) {
            group_settings = new ArrayList<Group>();
        }
        catch(ClassNotFoundException ex) {
            group_settings = new ArrayList<Group>();
        }

        try {
            file.close();
        }
        catch(Exception ex) {}

    }
    
    private void saveGroups() {

        ObjectOutputStream file = null;

        try {
            file = new ObjectOutputStream(new FileOutputStream("groups.dat"));
            file.writeObject(group_settings);
            file.flush();
        }
        catch(IOException ex) {}

        try {
            file.close();
        }
        catch(Exception ex) {}

    }
    
    @Override
    public void addRecord(String name, String IP, int PORT, String properties) {
        
        DatagramSocket socket2 = null;
        try {
            socket2 = new DatagramSocket();
        } 
        catch (SocketException ex) {
            calendar = new GregorianCalendar();
            System.out.println("\n<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + ">" + " [ Main Server ] A problem has beed occured while trying to add the record on the directory server.");
            return;
        }
              
 
        byte[] buf = new byte[91];
        buf[0] = new Integer(0x01).byteValue();
        
        byte[] name_bytes = name.getBytes();
        
        int i, j;
        for(i = 1, j = 0; j < name_bytes.length && i < 64; i++, j++) {
            buf[i] = name_bytes[j];
        }
        buf[i] = new Integer(0x00).byteValue();
        
        byte[] ip_bytes = IP.getBytes();
        
        for(i = 65, j = 0; j < ip_bytes.length && i < 81; i++, j++) {
            buf[i] = ip_bytes[j];
        }
        buf[i] = new Integer(0x00).byteValue();
         
        buf[81] = (byte)(PORT >>> 8);
        buf[82] = (byte)(PORT);
                            
        byte[] properties_bytes = properties.getBytes();
        
        for(i = 83, j = 0; j < properties_bytes.length && i < 90; i++, j++) {
            buf[i] = properties_bytes[j];
        }
        buf[i] = new Integer(0x00).byteValue();
        
        
        DatagramPacket packet = null;
        try {
            packet = new DatagramPacket(buf, buf.length, getAddress(DIRECTORY_SERVER_IP), DIRECTORY_SERVER_PORT);
        } 
        catch (UnknownHostException ex) {
            calendar = new GregorianCalendar();
            System.out.println("\n<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + ">" + " [ Main Server ] A problem has beed occured while trying to add the record on the directory server.");
            return;
        }
        
        try {
            socket2.send(packet);
        } 
        catch (IOException ex) {
            calendar = new GregorianCalendar();
            System.out.println("\n<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + ">" + " [ Main Server ] A problem has beed occured while trying to add the record on the directory server.");
            return;
        }
     
        packet = new DatagramPacket(buf, buf.length);
        try {
            socket2.setSoTimeout(4000);
            socket2.receive(packet);
        }
        catch(SocketTimeoutException ex) {
            calendar = new GregorianCalendar();
            System.out.println("\n<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + ">" + " [ Main Server ] A problem has beed occured while trying to add the record on the directory server.");
            return;
        }
        catch (IOException ex) {
            calendar = new GregorianCalendar();
            System.out.println("\n<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + ">" + " [ Main Server ] A problem has beed occured while trying to add the record on the directory server.");
            return;
        }
        
        if(new Byte(buf[0]).intValue() == 0x02) {
            if(new Byte(buf[1]).intValue() == 0x01) {
                calendar = new GregorianCalendar();
                System.out.println("\n<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + ">" + " [ Main Server ] The new record was added successfully.");
            }
            else if(new Byte(buf[1]).intValue() == 0x02) {
                calendar = new GregorianCalendar();
                System.out.println("\n<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + ">" + " [ Main Server ] There was not enough space to add the new record on the directory server.");
            }
        }
        else {
            calendar = new GregorianCalendar();
            System.out.println("\n<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + ">" + " [ Main Server ] A problem has beed occured while trying to add the record on the directory server.");
        }
    
        socket2.close();
        
    }
    
    @Override
    public void removeRecord(String name, String IP, int PORT) {
        
        DatagramSocket socket2 = null;
        try {
            socket2 = new DatagramSocket();
        } 
        catch (SocketException ex) {
            calendar = new GregorianCalendar();
            System.out.println("\n<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + ">" + " [ Main Server ] A problem has beed occured while trying to delete the record on the directory server.");
            return;
        }
              
 
        byte[] buf = new byte[83];
        buf[0] = new Integer(0x03).byteValue();
        
        byte[] name_bytes = name.getBytes();
        
        int i, j;
        for(i = 1, j = 0; j < name_bytes.length && i < 64; i++, j++) {
            buf[i] = name_bytes[j];
        }
        buf[i] = new Integer(0x00).byteValue();
        
        byte[] ip_bytes = IP.getBytes();
        
        for(i = 65, j = 0; j < ip_bytes.length && i < 81; i++, j++) {
            buf[i] = ip_bytes[j];
        }
        buf[i] = new Integer(0x00).byteValue();
        
        buf[81] = (byte)(PORT >>> 8);
        buf[82] = (byte)(PORT);
              
        DatagramPacket packet = null;
        try {
            packet = new DatagramPacket(buf, buf.length, getAddress(DIRECTORY_SERVER_IP), DIRECTORY_SERVER_PORT);
        } 
        catch (UnknownHostException ex) {
            calendar = new GregorianCalendar();
            System.out.println("\n<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + ">" + " [ Main Server ] A problem has beed occured while trying to delete the record on the directory server.");
            return;
        }
        
        try {
            socket2.send(packet);
        } 
        catch (IOException ex) {
            calendar = new GregorianCalendar();
            System.out.println("\n<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + ">" + " [ Main Server ] A problem has beed occured while trying to delete the record on the directory server.");
            return;
        }
     
        packet = new DatagramPacket(buf, buf.length);
        
        try {
            socket2.setSoTimeout(4000);
            socket2.receive(packet);
        }
        catch(SocketTimeoutException ex) {
            calendar = new GregorianCalendar();
            System.out.println("\n<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + ">" + " [ Main Server ] A problem has beed occured while trying to delete the record on the directory server.");
            return;
        }
        catch (IOException ex) {
            calendar = new GregorianCalendar();
            System.out.println("\n<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + ">" + " [ Main Server ] A problem has beed occured while trying to delete the record on the directory server.");
            return;
        }
        
        if(new Byte(buf[0]).intValue() == 0x04) {
            if(new Byte(buf[1]).intValue() == 0x01) {
                calendar = new GregorianCalendar();
                System.out.println("\n<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + ">" + " [ Main Server ] The old record was removed successfully.");
            }
            else if(new Byte(buf[1]).intValue() == 0x02) {
                calendar = new GregorianCalendar();
                System.out.println("\n<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + ">" + " [ Main Server ] The record was not found on the directory server.");
            }
        }
        else {
            calendar = new GregorianCalendar();
            System.out.println("\n<" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + ">" + " [ Main Server ] A problem has beed occured while trying to delete the record on the directory server.");
        }
 
        socket2.close();
        
    }

    @Override
    public void searchRecord(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    private InetAddress getAddress(String IP) throws UnknownHostException {

        InetAddress address = null;

        address = InetAddress.getByName(IP);
  
        return address;

    }

    public static void main(String[] args) throws IOException {     
      
        MultiChatServer server = new MultiChatServer();
        server.start();
        
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String temp;
        
        do {
            temp = br.readLine();
        } while(!temp.equals("exit"));
        
        server.terminate();

    }
    
}
