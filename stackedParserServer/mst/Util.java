package mst;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Utility methods that may be generally useful.
 *
 * @author     Jason Baldridge
 * @created    August 27, 2006
 */
public class Util {

    // Assumes input is a String[] containing integers as strings.
    public static int[] stringsToInts(String[] stringreps) {
	int[] nums = new int[stringreps.length];
	for(int i = 0; i < stringreps.length; i++)
	    nums[i] = Integer.parseInt(stringreps[i]);
	return nums;
    }


    public static String join (String[] a, char sep) {
	StringBuffer sb = new StringBuffer();
	sb.append(a[0]);
	for (int i=1; i<a.length; i++)
	    sb.append(sep).append(a[i]);
	return sb.toString();
    }

    public static String join (int[] a, char sep) {
	StringBuffer sb = new StringBuffer();
	sb.append(a[0]);
	for (int i=1; i<a.length; i++)
	    sb.append(sep).append(a[i]);
	return sb.toString();
    }
    
    public static void convertToCoNLL(String inputFile, String outputFile)
    {
    	ArrayList<String> sentences = new ArrayList<String>();
    	try
    	{
    		BufferedReader bReader = new BufferedReader(new FileReader(inputFile));
    		String line;
    		while((line=bReader.readLine())!=null)
    		{
    			line=line.trim();
    			if(line.equals("*"))
    				break;
    			sentences.add(line);
    		}
    		bReader.close();
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	try
		{
			BufferedWriter bWriter = new BufferedWriter(new FileWriter(outputFile));
			for(String posSentence:sentences)
			{
				String conllFormat=getCoNLLFormat(posSentence);
				writeStuff(bWriter,conllFormat);
			}
			bWriter.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}	
    }
    
    public static String getCoNLLFormat(String posSentence)
    {
    	ArrayList<String> words = new ArrayList<String>();
		ArrayList<String> pos = new ArrayList<String>();
		ArrayList<String> parents = new ArrayList<String>();
		ArrayList<String> labels = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(posSentence.trim());
		while(st.hasMoreTokens())
		{
			String token = st.nextToken();
			int lastIndex = token.lastIndexOf('_');
			String word = token.substring(0,lastIndex);
			String POS = token.substring(lastIndex+1);
			words.add(word);
			pos.add(POS);
			parents.add("0");
			labels.add("SUB");
		}
		String output = "";
		int size = words.size();
		for(int i = 0; i < size; i ++)
		{
			String line = "";
			line+=(i+1)+"\t";
			line+=words.get(i).toLowerCase()+"\t";
			line+=words.get(i).toLowerCase()+"\t";
			line+=pos.get(i)+"\t";
			line+=pos.get(i)+"\t";
			line+="_\t";
			line+=parents.get(i)+"\t";
			line+=labels.get(i);
			output+=line+"\n";
		}
		output+="\n";
		return output;
    }
    
    
    public static void writeStuff(BufferedWriter bWriter, String string)
	{
    	try {
			bWriter.write(string);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

    
}
