/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package webserver;

import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Denisse
 */
public class ObjetoCola {
    private Object cola[];
    private int capacidad;
    private int size;
    private int head;
    private int tail;
    
    public ObjetoCola(int cap){
        if(cap>0)
            capacidad = cap;
        else
            capacidad = 1;
        cola = new Object[capacidad];
        head =0;
        tail =0;
        size =0;
    }
    public int getCapacidad(){
        return capacidad;
    }
    
    public synchronized int getSize(){
        return size;
    }
    public synchronized boolean estaVacio(){
        if(size==0)
            return true;
        else
            return false;
    }
    
    public synchronized boolean estaLleno(){
        if(capacidad==size)
            return true;
        else
            return false;
    }
    
     public synchronized void add(Object objeto){
         waitWhileFull();
         cola[head] = objeto;
         head++;
         head = head%capacidad;
         size++;
         notifyAll();
     }
    
      
      public synchronized Object remove(){
          waitWhileEmpty();
          
          Object objeto = cola[tail];
          cola[tail] = null;
          tail++;
          tail = tail% capacidad;
          size--;
          notifyAll();
          return objeto;
      }
      

   
      public synchronized void waitWhileEmpty(){
          while(!estaLleno()){
              try {
                  wait();
              } catch (InterruptedException ex) {
                 
              }
          }
      }
      
      public synchronized void waitWhileFull(){
          while(estaLleno()){
              try {
                  wait();
              } catch (InterruptedException ex) {
                 
              }
          }
      }
      
      
      
}
