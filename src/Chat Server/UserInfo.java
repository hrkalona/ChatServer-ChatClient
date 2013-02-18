
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Administrator
 */
public class UserInfo {
  private String IP;
  private int LISTENING_PORT;
  private int SENDING_PORT;
  private String Name;
  private int message_counter;

    public UserInfo(String IP, int LISTENING_PORT, int SENDING_PORT, String Name) {

        this.IP = IP;
        this.LISTENING_PORT = LISTENING_PORT;
        this.SENDING_PORT = SENDING_PORT;
        this.Name = Name;
        message_counter = 0;
        
    }
    

    public String getIP() {

        return IP;
        
    }

    public int getListeningPort() {

        return LISTENING_PORT;
        
    }
    
    public int getSendingPort() {

        return SENDING_PORT;
        
    }

    public String getName() {

        return Name;
        
    }
    
    public int getMessageCounter() {
        
        return message_counter;
        
    }
    
    public void increaseMessageCounter() {
       
        message_counter++;
        
    }

}
