/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webserver;

import java.io.*;
import java.net.*;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 *
 * @author Denisse
 */
public class HttpWorker extends Object {

    private static int nextWorkerID = 0;

    private File docRoot;
    private ObjetoCola idleWorkers;
    private int workerID;
    private ObjetoCola handoffBox;

    private Thread internalThread;
    private volatile boolean noStopRequested;

    public HttpWorker(File docRoot, int workerPriority, ObjetoCola idleWorkers) {
        this.docRoot = docRoot;
        this.idleWorkers = idleWorkers;
        workerID = getNextWorkerID();
        handoffBox = new ObjetoCola(1);

        noStopRequested = true;

        Runnable r = new Runnable() {
            public void run() {
                try {
                    runWork();
                } catch (Exception x) {
                    x.printStackTrace();
                }
            }
        };

        internalThread = new Thread(r);
        internalThread.setPriority(workerPriority);
        internalThread.start();

    }

    public static synchronized int getNextWorkerID() {
        int id = nextWorkerID;
        nextWorkerID++;
        return id;
    }

    public void processRequest(Socket s) {
        handoffBox.add(s);
    }

    private void runWork() {
        Socket s = null;
        InputStream in = null;
        OutputStream out = null;

        while (noStopRequested) {
            try {
                idleWorkers.add(this);
                s = (Socket) handoffBox.remove();

                in = s.getInputStream();
                out = s.getOutputStream();
                generateResponse(in, out);
                out.flush();

            } catch (IOException iox) {
                System.err.println("I/O error while procesing request, " + " ignoring and adding back to idle "
                        + "queue - workerID=" + workerID);
            } catch (Exception x) {
                Thread.currentThread().interrupt();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException iox) {

                    } finally {
                        in = null;
                    }
                }

                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException iox) {

                    } finally {
                        out = null;
                    }
                }

                if (s != null) {
                    try {
                        s.close();
                    } catch (IOException iox) {

                    } finally {
                        s = null;
                    }
                }
            }
        }
    }

    private void generateResponse(InputStream in, OutputStream out) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String requestLine = reader.readLine();
        if ((requestLine == null) || (requestLine.length() < 1)) {
            throw new IOException("could no read request");
        }

        System.out.println("workerID " + workerID + " , requestLine " + requestLine);

        StringTokenizer st = new StringTokenizer(requestLine);
        String filename = null;

        String cadena = "";
        int accountant = 0;
        while ((cadena != null || cadena != "")&& accountant <4 ) {
            cadena = reader.readLine();
            if (cadena == null || cadena == "" ) {
                break;
            } else {
                System.out.println("workerID " + workerID + " , "+cadena);
            }
            accountant++;
        }

        try {
            st.nextToken();
            filename = st.nextToken();
        } catch (NoSuchElementException x) {
            throw new IOException("Could nor parse  request line");
        }

        File requestedFile = generateFile(filename);
        BufferedOutputStream buffout = new BufferedOutputStream(out);

        if (requestedFile.exists()) {
            System.out.println("workerID " + workerID + " 200 OK: " + filename);

            int fileLen = (int) requestedFile.length();
            BufferedInputStream fileIn = new BufferedInputStream(new FileInputStream(requestedFile));

            String contentType = URLConnection.guessContentTypeFromStream(fileIn);

            byte[] headerBytes = createHeaderBytes("HTTP/1.0 200 OK", fileLen, contentType);

            buffout.write(headerBytes);

            byte[] buf = new byte[2048];
            int blockLen = 0;

            while ((blockLen = fileIn.read(buf)) != -1) {
                buffout.write(buf, 0, blockLen);
            }
            fileIn.close();
        } else {
            System.out.println("workerID " + workerID + " 404 Not Found: " + filename);
            byte[] headerBytes = createHeaderBytes("HTTP/1.0 404 Not Found", -1, null);
            buffout.write(headerBytes);
        }
        buffout.flush();

    }

    private File generateFile(String filename) {
        File requestedFile = docRoot;
        StringTokenizer st = new StringTokenizer(filename, "/");
        while ((st.hasMoreTokens())) {
            String tok = st.nextToken();

            if (tok.equals("..")) {
                continue;
            }

            requestedFile = new File(requestedFile, tok);
        }

        if (requestedFile.exists() && requestedFile.isDirectory()) {
            requestedFile = new File(requestedFile, "index.html");
        }

        return requestedFile;
    }

    private byte[] createHeaderBytes(String resp, int contentLen, String contentType) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(baos));

        writer.write(resp + "\r\n");

        if (contentLen != -1) {
            writer.write("Content-Length: " + contentLen + "\r\n");
        }

        if (contentType != null) {
            writer.write("Content-Type: " + contentType + "\r\n");
        }
        writer.write("\r\n");
        writer.flush();
        byte[] data = baos.toByteArray();
        writer.close();
        return data;
    }

    public void stopRequest() {
        noStopRequested = false;
        internalThread.interrupt();

    }

    public boolean isAlive() {
        return internalThread.isAlive();
    }
}
