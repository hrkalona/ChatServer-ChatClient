/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author hrkalona2
 */
public interface DirectoryService {
    
    public void addRecord(String name, String IP, int PORT, String properties);
    
    public void removeRecord(String name, String IP, int PORT);
    
    public void searchRecord(String name);
    
}
