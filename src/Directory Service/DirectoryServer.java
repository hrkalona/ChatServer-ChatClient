/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.*;

/**
 *
 * @author hrkalona2
 */
public class DirectoryServer {
    
 

    public static void main(String[] args) throws IOException {
        new DirectoryServerThread().start();
    }

    
}
