/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webserver;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import static java.lang.System.in;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import javax.net.ssl.HttpsURLConnection;

/**
 *
 * @author Denisse
 */
public class Cliente {

    public String conexionGET(String request) {

        String response = "";

        BufferedReader rd = null;

        try {

            URL url = new URL(request);
            URLConnection conn = url.openConnection();
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String line;

            while ((line = rd.readLine()) != null) {

                //Process line...
                response += line;

            }

        } catch (IOException e) {
            System.out.println("HTTP/1.0 404 Not Found");
        }

        System.out.println(response);
        return response;

    }

    public String conexionPOST(String request) {

        String response = "";
        BufferedReader rd = null;

        try {

            URL url = new URL(request);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            String datos = "Hey hola soy un nuevo archivo";
            //Escribir los parametros en el mensaje
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", "" + Integer.toString(datos.getBytes().length));
            BufferedOutputStream buffout = new BufferedOutputStream(conn.getOutputStream());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(baos));
            writer.write(datos);
            writer.write("\r\n");
            writer.flush();
            buffout.write(baos.toByteArray());
            buffout.flush();
            buffout.close();

            //Recibir respuesta
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String line;

            while ((line = rd.readLine()) != null) {

                //Process line...
                response += line;

            }

        } catch (Exception e) {

        }
        System.out.println(response);
        return response;

    }

}
