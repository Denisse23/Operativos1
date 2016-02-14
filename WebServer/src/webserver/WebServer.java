/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package webserver;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author Denisse
 */
public class WebServer {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
       int port =80;
       int numWorkers = 10;
       File docRoot = new File("./mi_web/");
       Server server;
       try{
           server =  new Server(docRoot,port, numWorkers,1);
       }catch(IOException iox){
           iox.printStackTrace();
       }
       
    }
    
}
