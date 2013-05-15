package mst;

public class RavineToCoNLLConversion
{
	/*
	 * args[0]: input file
	 * args[1]: output file
	 */
	public static void main(String[] args)
	{
		if(args.length!=2)
		{
			System.err.println("Usage: Class name <input file> <output file>");
			System.exit(1);
		}
		Util.convertToCoNLL(args[0].trim(), args[1].trim());
	}
	
	
}