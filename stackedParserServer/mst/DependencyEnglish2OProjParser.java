package mst;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class DependencyEnglish2OProjParser 
{
	
	/*
	 * arg[0]=modelfile
	 * arg[1]=tempdir
	 * arg[2]=port
	 */
	public static void main(String[] args) throws FileNotFoundException, Exception
	{
				
		if(args.length!=3)
		{
			System.out.println("Usage: DependencyEnglish2OProjParser <model-file> <tmpdir> <port>");
			System.exit(1);
		}
		String modelFile=args[0].trim();
		int port=new Integer(args[2].trim());
		System.setProperty("java.io.tmpdir", args[1]);
		ArrayList<String> argsList = new ArrayList<String>();
		argsList.add("test");
		argsList.add("separate-lab");
		argsList.add("model-name:"+modelFile);
		argsList.add("decode-type:proj");
		argsList.add("order:2");
		argsList.add("format:CONLL");
		String[] argsArray = new String[argsList.size()];
		argsList.toArray(argsArray);
		ParserOptions options = new ParserOptions(argsArray);
		System.err.println("Default temp directory:"+System.getProperty("java.io.tmpdir"));
		DependencyPipe pipe = options.secondOrder ? 
				new DependencyPipe2O (options) : new DependencyPipe (options);
		DependencyParser dp = new DependencyParser(pipe, options);
		System.err.print("\tLoading model for Dependency Parser...\n");
		dp.loadModel(options.modelName);
		System.err.println("done.");
		pipe.printModelStats(dp.params);
		pipe.closeAlphabets();
		
		
		//INITIALIZE PARSER
		ServerSocket parseServer = null;
		BufferedReader br;
		OutputStream outStream=null;
		PrintWriter outputWriter;
		Socket clientSocket = null;
		try {
			parseServer = new ServerSocket(port);
		}
		catch (IOException e) {
			System.err.println(e);
		}
		// Create a socket object from the ServerSocket to listen and accept
		// connections.
		// Open input and output streams

		while (true) {
			System.err.println("Waiting for Connection on Port: "+port);
			try {
				clientSocket = parseServer.accept();
				System.err.println("Connection Accepted From: "+clientSocket.getInetAddress());
				br = new BufferedReader(new InputStreamReader(new DataInputStream(clientSocket.getInputStream())));
				outputWriter = new PrintWriter(new PrintStream(clientSocket.getOutputStream()));
				outStream = new PrintStream(clientSocket.getOutputStream());		
				String inputLine;
				String doc="";
				while ((inputLine = br.readLine()) != null)
				{	
					if(inputLine.trim().equals("*"))
				        break;
					doc=doc+Util.getCoNLLFormat(inputLine);
				} 
                //WRITE INPUT TO TEMP FILE
                File tempFile = File.createTempFile("serverInputFile", ".txt");
                // Delete temp file when program exits.
                tempFile.deleteOnExit();
		        // Write to temp file
		        BufferedWriter out = new BufferedWriter(new FileWriter(tempFile));
		        out.write(doc);
		        out.close();
		        File parseFile = File.createTempFile("parsedServerFile",".txt");
		        options.testfile=tempFile.getAbsolutePath();
		        options.outfile=parseFile.getAbsolutePath();
		        dp.outputParses(null);
		        String output = getLines(parseFile.getAbsolutePath());
		        tempFile.delete();
		        parseFile.delete();
			outputWriter.print(output);
			outputWriter.flush();
			outputWriter.close();
			}catch (IOException e) {
				e.printStackTrace();
			}
		}	

	}
	
	public static String getLines(String file)
	{
		String output="";
		try
		{
			BufferedReader bReader = new BufferedReader(new FileReader(file));
			String line=null;
			while((line=bReader.readLine())!=null)
				if(line.equals(""))
					output=output.trim()+"\n";
				else
					output+=line+"\t";
			bReader.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return output;
	}
	
}