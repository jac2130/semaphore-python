package mst;


import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;

import mst.Alphabet;
import mst.io.*;
import mst.mallet.LabelClassifier;

import gnu.trove.*;

import edu.umass.cs.mallet.base.classify.Classifier;

public class DependencyParser {

	public ParserOptions options;

	private DependencyPipe pipe;
	private DependencyDecoder decoder;
	protected Parameters params;
	private Classifier classifier;

	public DependencyParser(DependencyPipe pipe, ParserOptions options) {
		this.pipe=pipe;
		this.options = options;

		// Set up arrays
		params = new Parameters(pipe.dataAlphabet.size());
		decoder = options.secondOrder ? 
				new DependencyDecoder2O(pipe) : new DependencyDecoder(pipe);
	}
	

	// afm 03-06-08 --- Count the real number of instances to be considered
	public int countActualInstances(int ignore[]) 
	{
		int i;
		int numInstances = ignore.length;
		int numActualInstances = 0;
		for (i = 0; i < numInstances; i++)
		{
			if (ignore[i] == 0) // This sentence is not to be ignored
				numActualInstances++;
		}
		return numActualInstances;
	}

	public void augment(int[] instanceLengths, String trainfile, File train_forest, int numParts) 
	throws IOException {

		//System.out.print("About to train. ");
		//System.out.print("Num Feats: " + pipe.dataAlphabet.size());

		int i, j;
		int[] ignore = new int[instanceLengths.length];

		//String trainpartfile;
		//createPartitions(instanceLengths, trainfile, numParts);
		//for(i = 0; i < numParts; i++)
		//{
		//	trainpartfile = trainfile + "." + i;
		//}

		int numInstances = instanceLengths.length;
		int numInstancesPerPart = numInstances / numParts; // The last partition becomes bigger
		pipe.initOutputFile(options.outfile); // Initialize the output file once

		for(j = 0; j < numParts; j++)
		{
			System.out.println("Training classifier for partition " + j);    

			// Make partition
			for (i = 0; i < numInstances; i++)
			{
				if (i >= j * numInstancesPerPart &&
						i < (j+1) * numInstancesPerPart)
					ignore[i] = 1; // Mark to ignore this instance in training
				else
					ignore[i] = 0;
			}

			// Train on one split
			params = new Parameters(pipe.dataAlphabet.size());
			train(instanceLengths, ignore,trainfile, train_forest);		
			
			// Test on the other split
			System.out.println("Making predictions for partition " + j);    

			for (i = 0; i < numInstances; i++)
				ignore[i] = 1 - ignore[i]; // Toggle ignore
			outputParses(ignore);		
		}

		pipe.close(); // Close the output file once
	}

	
	
	public void train(int[] instanceLengths, int[] ignore, String trainfile, File train_forest) 
	throws IOException {
		
		int i = 0;
		for(i = 0; i < options.numIters; i++) {

			System.out.print(" Iteration "+i);
			System.out.print("[");

			long start = System.currentTimeMillis();
			
			trainingIter(instanceLengths,ignore,trainfile,train_forest,i+1);

			long end = System.currentTimeMillis();
			//System.out.println("Training iter took: " + (end-start));
			System.out.println("|Time:"+(end-start)+"]");			
		}
		params.averageParams(i*countActualInstances(ignore));
		//	 afm 06-04-08
		if (options.separateLab)
		{		
			LabelClassifier oc = new LabelClassifier(options, instanceLengths,ignore,trainfile,train_forest,this, pipe);
			try
			{
				classifier = oc.trainClassifier(100);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	// Note: Change this to pass -1 for indices in instanceLengths[] that you
	// don't want to use on training (need to be careful because i is being used
	// in the for loop; need new index)
	private void trainingIter(int[] instanceLengths, int ignore[], String trainfile, 
			File train_forest, int iter) throws IOException {

		int numUpd = 0;
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(train_forest));
		boolean evaluateI = true;

		int numInstances = instanceLengths.length;

		// afm -- Count the real number of instances to be considered
		int numActualInstances = countActualInstances(ignore);

		int j = 0;
		for(int i = 0; i < numInstances; i++) {
			if((i+1) % 500 == 0) {
				System.out.print((i+1)+",");
				//System.out.println("  "+(i+1)+" instances");
			}
			
			int length = instanceLengths[i];


			// Get production crap.
			FeatureVector[][][] fvs = new FeatureVector[length][length][2];
			double[][][] probs = new double[length][length][2];
			FeatureVector[][][][] nt_fvs = new FeatureVector[length][pipe.types.length][2][2];
			double[][][][] nt_probs = new double[length][pipe.types.length][2][2];
			FeatureVector[][][] fvs_trips = new FeatureVector[length][length][length];
			double[][][] probs_trips = new double[length][length][length];
			FeatureVector[][][] fvs_sibs = new FeatureVector[length][length][2];
			double[][][] probs_sibs = new double[length][length][2];

			DependencyInstance inst;

			if(options.secondOrder) {
				inst = ((DependencyPipe2O)pipe).readInstance(in,length,fvs,probs,
						fvs_trips,probs_trips,
						fvs_sibs,probs_sibs,
						nt_fvs,nt_probs,params);
			}

			else
				inst = pipe.readInstance(in,length,fvs,probs,nt_fvs,nt_probs,params);

			// afm 03-06-08
			if (ignore[i] != 0) // This sentence is to be ignored
				continue;

			double upd = (double)(options.numIters*numActualInstances - (numActualInstances*(iter-1)+(j+1)) + 1);
			int K = options.trainK;
			Object[][] d = null;
			if(options.decodeType.equals("proj")) {
				if(options.secondOrder)
					d = ((DependencyDecoder2O)decoder).decodeProjective(inst,fvs,probs,
							fvs_trips,probs_trips,
							fvs_sibs,probs_sibs,
							nt_fvs,nt_probs,K);
				else
					d = decoder.decodeProjective(inst,fvs,probs,nt_fvs,nt_probs,K);
			}
			if(options.decodeType.equals("non-proj")) {
				if(options.secondOrder)
					d = ((DependencyDecoder2O)decoder).decodeNonProjective(inst,fvs,probs,
							fvs_trips,probs_trips,
							fvs_sibs,probs_sibs,
							nt_fvs,nt_probs,K);
				else
					d = decoder.decodeNonProjective(inst,fvs,probs,nt_fvs,nt_probs,K);
			}
			params.updateParamsMIRA(inst,d,upd);
			j++;
		}

		//System.out.println("");	
		//System.out.println("  "+numInstances+" instances");

		System.out.print(numActualInstances);

		in.close();

	}

	///////////////////////////////////////////////////////
	// Saving and loading models
	///////////////////////////////////////////////////////
	public void saveModel(String file) throws IOException 
	{
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
		out.writeObject(params.parameters);
		out.writeObject(pipe.dataAlphabet);
		out.writeObject(pipe.typeAlphabet);

		// afm 06-04-08
		if (options.separateLab)
		{
			out.writeObject(classifier);
		}

		out.close();


	}

	public void loadModel(String file) throws Exception 
	{
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
		params.parameters = (double[])in.readObject();
		pipe.dataAlphabet = (Alphabet)in.readObject();
		pipe.typeAlphabet = (Alphabet)in.readObject();

		// afm 06-04-08
		if (options.separateLab)
		{
			classifier = (Classifier)in.readObject();
		}

		in.close();
		pipe.closeAlphabets();


	}


	//////////////////////////////////////////////////////
	// Get Best Parses ///////////////////////////////////
	//////////////////////////////////////////////////////
	public void outputParses (int[] ignore) throws IOException {

		String tFile = options.testfile;
		String file = options.outfile;

		long start = System.currentTimeMillis();

		pipe.initInputFile(tFile);
		//if (ignore == null) // afm 03-07-2008 --- If this is called for each partition, must have initialized output file before 
		if (!options.train || !options.stackedLevel0) // afm 03-07-2008 --- If this is called for each partition, must have initialized output file before 
			pipe.initOutputFile(file);

		System.out.print("Processing Sentence: ");
		DependencyInstance instance = pipe.nextInstance();
		int cnt = 0;
		int i = 0;
		LabelClassifier oc = new LabelClassifier(options);
		while(instance != null) 
		{
			cnt++;
			System.out.print(cnt+" ");
			String[] forms = instance.forms;

			int length = forms.length;

			// afm 03-07-08 --- If this instance is to be ignored, just go for the next one
			if (ignore != null && ignore[i] != 0)
			{
				instance = pipe.nextInstance();
				i++;
				continue;
			}	    

			FeatureVector[][][] fvs = new FeatureVector[forms.length][forms.length][2];
			double[][][] probs = new double[forms.length][forms.length][2];
			FeatureVector[][][][] nt_fvs = new FeatureVector[forms.length][pipe.types.length][2][2];
			double[][][][] nt_probs = new double[forms.length][pipe.types.length][2][2];
			FeatureVector[][][] fvs_trips = new FeatureVector[length][length][length];
			double[][][] probs_trips = new double[length][length][length];
			FeatureVector[][][] fvs_sibs = new FeatureVector[length][length][2];
			double[][][] probs_sibs = new double[length][length][2];
			if(options.secondOrder)
				((DependencyPipe2O)pipe).fillFeatureVectors(instance,fvs,probs,
						fvs_trips,probs_trips,
						fvs_sibs,probs_sibs,
						nt_fvs,nt_probs,params);
			else
				pipe.fillFeatureVectors(instance,fvs,probs,nt_fvs,nt_probs,params);


			int K = options.testK;
			Object[][] d = null;
			
			if(options.decodeType.equals("proj")) 
			{
				if(options.secondOrder)
					d = ((DependencyDecoder2O)decoder).decodeProjective(instance,fvs,probs,
							fvs_trips,probs_trips,
							fvs_sibs,probs_sibs,
							nt_fvs,nt_probs,K);
				else
					d = decoder.decodeProjective(instance,fvs,probs,nt_fvs,nt_probs,K);
			}
			if(options.decodeType.equals("non-proj")) 
			{
				
				if(options.secondOrder)
				{					
					d = ((DependencyDecoder2O)decoder).decodeNonProjective(instance,fvs,probs,
							fvs_trips,probs_trips,
							fvs_sibs,probs_sibs,
							nt_fvs,nt_probs,K);
					
					
				}
				else
					d = decoder.decodeNonProjective(instance,fvs,probs,nt_fvs,nt_probs,K);
			}

			String[] res = ((String)d[0][1]).split(" ");
			String[] pos = instance.cpostags;

			String[] formsNoRoot = new String[forms.length-1];
			String[] posNoRoot = new String[formsNoRoot.length];
			String[] labels = new String[formsNoRoot.length];
			int[] heads = new int[formsNoRoot.length];

			Arrays.toString(forms);
			Arrays.toString(res);
			for(int j = 0; j < formsNoRoot.length; j++) 
			{
				formsNoRoot[j] = forms[j+1];
				posNoRoot[j] = pos[j+1];

				String[] trip = res[j].split("[\\|:]");
				labels[j] = pipe.types[Integer.parseInt(trip[2])];
				heads[j] = Integer.parseInt(trip[0]);
			}		
			
			//	 afm 06-04-08
			if (options.separateLab)
			{
				/*
				 * ask whether instance contains level0 information
				 */
				/*
				 * Note, forms and pos have the root. labels and heads do not
				 */
				if (options.stackedLevel1)
					labels=oc.outputLabels(classifier,instance.forms,instance.postags,labels,heads,instance.deprels_pred, instance.heads_pred, instance);
				else
					labels=oc.outputLabels(classifier,instance.forms,instance.postags,labels,heads, null, null, instance);

			}
			
			// afm 03-07-08
			//if (ignore == null)
			if (options.stackedLevel0 == false)
				pipe.outputInstance(new DependencyInstance(formsNoRoot, posNoRoot, labels, heads));
			else
			{
				int[] headsNoRoot = new int[instance.heads.length-1];
				String[] labelsNoRoot = new String[instance.heads.length-1];
				for(int j = 0; j < headsNoRoot.length; j++) 
				{
					headsNoRoot[j] = instance.heads[j+1];
					labelsNoRoot[j] = instance.deprels[j+1];
				}
				DependencyInstance out_inst = new DependencyInstance(formsNoRoot, posNoRoot, labelsNoRoot, headsNoRoot);
				out_inst.stacked = true;
				out_inst.heads_pred = heads;
				out_inst.deprels_pred = labels;
				pipe.outputInstance(out_inst);
			}    		

			//String line1 = ""; String line2 = ""; String line3 = ""; String line4 = "";
			//for(int j = 1; j < pos.length; j++) {
			//	String[] trip = res[j-1].split("[\\|:]");
			//	line1+= sent[j] + "\t"; line2 += pos[j] + "\t";
			//	line4 += trip[0] + "\t"; line3 += pipe.types[Integer.parseInt(trip[2])] + "\t";
			//}
			//pred.write(line1.trim() + "\n" + line2.trim() + "\n"
			//	       + (pipe.labeled ? line3.trim() + "\n" : "")
			//	       + line4.trim() + "\n\n");

			instance = pipe.nextInstance();
			i++;
		}
		//if (ignore == null) // afm 03-07-2008 --- If this is called for each partition (ignore != null), must close pipe outside the loop 	
		if (!options.train || !options.stackedLevel0) // afm 03-07-2008 --- If this is called for each partition (ignore != null), must close pipe outside the loop 	
			pipe.close();

		long end = System.currentTimeMillis();
		System.out.println("Took: " + (end-start));

	}

	/////////////////////////////////////////////////////
	// RUNNING THE PARSER
	////////////////////////////////////////////////////
	public static void main (String[] args) throws FileNotFoundException, Exception
	{
		System.setProperty("java.io.tmpdir", "./tmp/");
		ParserOptions options = new ParserOptions(args);
		System.out.println("Default temp directory:"+System.getProperty("java.io.tmpdir"));
		
		System.out.println("Separate labeling: " + options.separateLab);	
		
		if (options.train)
		{
			DependencyPipe pipe = options.secondOrder ? 
					new DependencyPipe2O (options) : new DependencyPipe (options);
			int[] instanceLengths = 
				pipe.createInstances(options.trainfile,options.trainforest);
			pipe.closeAlphabets();
			DependencyParser dp = new DependencyParser(pipe, options);
			//pipe.printModelStats(null);
			int numFeats = pipe.dataAlphabet.size();
			int numTypes = pipe.typeAlphabet.size();
			System.out.print("Num Feats: " + numFeats);	
			System.out.println(".\tNum Edge Labels: " + numTypes);
			if (options.stackedLevel0) // Augment training data with output predictions, for stacked learning (afm 03-03-08)
			{
				// Output data augmented with output predictions
				System.out.println("Augmenting training data with output predictions...");	    
				options.testfile = options.trainfile;
				dp.augment(instanceLengths,options.trainfile,options.trainforest,options.augmentNumParts);
				// Now train the base classifier in the whole corpus, nothing being ignored
				System.out.println("Training the base classifier in the whole corpus...");	    
			}
			// afm 03-06-08 --- To allow some instances to be ignored
			int ignore[] = new int[instanceLengths.length];
			for (int i = 0; i < instanceLengths.length; i++)
				ignore[i] = 0;
			dp.params = new Parameters(pipe.dataAlphabet.size());	    	    
			dp.train(instanceLengths,ignore,options.trainfile,options.trainforest);
			System.out.print("Saving model...");
			dp.saveModel(options.modelName);
			System.out.print("done.");
		}
		if (options.test) {
			DependencyPipe pipe = options.secondOrder ? 
					new DependencyPipe2O (options) : new DependencyPipe (options);
			DependencyParser dp = new DependencyParser(pipe, options);
			System.out.print("\tLoading model...");
			dp.loadModel(options.modelName);
			System.out.println("done.");
			pipe.printModelStats(dp.params);
			pipe.closeAlphabets();
			dp.outputParses(null);
		}

		System.out.println();

		if (options.eval) {
			System.out.println("\nEVALUATION PERFORMANCE:");
			DependencyEvaluator.evaluate(options.goldfile, 
					options.outfile, 
					options.format);
		}
	}
}
