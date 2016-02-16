/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package webserver;

/**
 *
 * @author Denisse
 */
public class mainCliente {
    public static void main(String [] array) //</font>
	{
		Cliente instancia = new Cliente(); 
		instancia.conexionGET("http://localhost:80");
                //instancia.conexionPOST("http://localhost:80");
	}
}
