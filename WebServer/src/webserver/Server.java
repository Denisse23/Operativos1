/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package webserver;

import java.net.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Denisse
 */
public class Server extends Object {
    private ObjetoCola idleWorkers;
    private HttpWorker[] workerList;
    private ServerSocket ss;
    
    private Thread internalThread;
    private volatile boolean noStopRequested;
    
    public Server(File docRoot, int port, int numberOfWorkers, int maxPriority) throws IOException{
       
            ss = new ServerSocket(port, 10);
            
            if((docRoot == null) || !docRoot.exists() || !docRoot.isDirectory()){
            }else{
                numberOfWorkers = Math.max(1, numberOfWorkers);
                int serverPriority = Math.max(Thread.MIN_PRIORITY +2, Math.min(maxPriority, Thread.MAX_PRIORITY -1));
                int workerPriority = serverPriority -1;
                
                idleWorkers = new ObjetoCola(numberOfWorkers);
                workerList = new HttpWorker[numberOfWorkers];
                
                for(int i=0;i<numberOfWorkers;i++){
                    workerList[i] =  new HttpWorker(docRoot, workerPriority, idleWorkers);
                }
                
                 noStopRequested = true;
                 
                 Runnable r = new Runnable(){
                     public void run(){
                         try{
                             runWork();
                         }catch(Exception x){
                             x.printStackTrace();
                         }
                     }
                 };
                 
                 internalThread = new Thread(r);
                 internalThread.setPriority(serverPriority);
                 internalThread.start();
            }
            
        
    }
    
    private void runWork(){
        System.out.println("Server listo para recibir peticiones");
        
        while(noStopRequested){
            try{
                Socket s = ss.accept();
                
                if(idleWorkers.estaVacio()){
                    System.out.println("Server ocupado, peticion denegada");
                    
                   BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                 
                    
                    writer.write("HTTP/1.0 503 Service "+"Unavailable\r\n\r\n");
                    
                    writer.flush();
                    writer.close();
                    writer = null;
                }else{
                    
                    HttpWorker worker = (HttpWorker)idleWorkers.remove();
                    worker.processRequest(s);
                }
            }catch(IOException iox){
                if(noStopRequested){
                    iox.printStackTrace();
                }
            }catch(Exception x){
                    Thread.currentThread().interrupt();
            }
        }
    }
    
    public void stopRequest(){
        noStopRequested = false;
        internalThread.interrupt();
        
        for(int i=0;i<workerList.length;i++){
            workerList[i].stopRequest();
        }
        
        if(ss!=null){
            try{
                ss.close();
            }catch(IOException x){
            }
            ss=null;
        }
    }
    
    
    public boolean isAlive(){
        return internalThread.isAlive();
    }
    
    private static void usageAndExit(String msg, int exitCode){
        System.err.println(msg);
        System.err.println("Usage: java HttpServer <port> "+"<numberWorkers> <documentRoot>");
        System.err.println("    <port> - port to listen on "+"for HTTP request");
        System.err.println("    <numberWorkers> - numberOf "+"worker threads to create");
        System.err.println("    <documentRoot> - base "+"directory for Html files");
        System.exit(exitCode);
    }
    
     
}
