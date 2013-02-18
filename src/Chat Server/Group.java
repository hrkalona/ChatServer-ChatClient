
import java.io.Serializable;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author hrkalona2
 */
public class Group implements Serializable {
  private String group_name;
  private String password;
  private int group_port;
  private int max_users;
  
    public Group(String group_name, String password, int group_port, int max_users) {
        
        this.group_name = group_name;
        this.group_port = group_port;
        this.password = password;
        this.max_users = max_users;
        
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
    
    public String getPassword() {
        
        return password;
        
    } 
    
}
