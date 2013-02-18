
import java.io.Serializable;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author hrkalona2
 */
public class Record implements Serializable {
  protected String name;
  protected String IP;
  protected int PORT;
  protected String properties;
  
    public Record(String name, String IP, int PORT, String properties) {
      
        this.name = name;
        this.IP = IP;
        this.PORT = PORT;
        this.properties = properties;
      
    }
  
    public String getName() {
      
        return name;
      
    }
  
    public String getIP() {
      
        return IP;
      
    }
  
    public int getPORT() {
      
        return PORT;
      
    }
    
    public String getProperties() {
        
        return properties;
        
    }
    
}
