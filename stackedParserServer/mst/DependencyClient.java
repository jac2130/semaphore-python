package mst;


import java.io.*;
import java.net.*;

/*
 * args[0]=input
 * args[1]=output
 * args[1]=parser server port
 */
public class DependencyClient {
    public static void main(String[] args) throws IOException
    {
        if(args.length!=3)
        {
        	System.err.println("Usage: DependencyClient <input-pos-tagged-file> <output-conll-formatted-file> <port>");
        	System.exit(1);
        }
        int port = new Integer(args[2].trim());
    	Socket kkSocket = null;
        PrintWriter out = null;
        BufferedReader in = null;

        try {
            kkSocket = new Socket("localhost", port);
            out = new PrintWriter(kkSocket.getOutputStream(),true);
            in = new BufferedReader(new InputStreamReader(kkSocket.getInputStream()));
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: DepServer.");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: Server.");
            System.exit(1);
        }
        
        BufferedReader bR = new BufferedReader(new FileReader(args[0]));
        String userInput=null;
    	while ((userInput = bR.readLine()) != null) {
    		out.println(userInput);
    	}
    	bR.close();
    	
    	String allParses="";
        String fromServer="";
        while ((fromServer = in.readLine()) != null) {
            allParses+=fromServer.trim()+"\n";
        }
        out.close();
        in.close();
        kkSocket.close();
        System.err.println("Got all output from server. Writing in output file");
        BufferedWriter bWriter = new BufferedWriter(new FileWriter(args[1]));
        bWriter.write(allParses);
        bWriter.close();
    }
}
