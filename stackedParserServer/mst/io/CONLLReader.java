///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2007 University of Texas at Austin and (C) 2005
// University of Pennsylvania and Copyright (C) 2002, 2003 University
// of Massachusetts Amherst, Department of Computer Science.
//
// This software is licensed under the terms of the Common Public
// License, Version 1.0 or (at your option) any subsequent version.
// 
// The license is approved by the Open Source Initiative, and is
// available from their website at http://www.opensource.org.
///////////////////////////////////////////////////////////////////////////////

package mst.io;


import java.io.*;
import java.util.*;

import mst.DependencyInstance;
import mst.RelationalFeature;
import mst.Util;

/**
 * A reader for files in CoNLL format.
 *
 * <p>
 * Created: Sat Nov 10 15:25:10 2001
 * </p>
 *
 * @author Jason Baldridge
 * @version $Id: CONLLReader.java 112 2007-03-23 19:19:28Z jasonbaldridge $
 * @see mstparser.io.DependencyReader
 */
public class CONLLReader extends DependencyReader {

    protected boolean discourseMode = false;
    protected boolean stacked = false; // afm 03-10-08 --- True if input file contains output predictions 
    protected boolean useStemmingIfLemmasAbsent = false; 
    public CONLLReader (boolean discourseMode, boolean stacked, boolean useStemmingIfLemmasAbsent) {
	this.discourseMode = discourseMode; this.stacked = stacked; this.useStemmingIfLemmasAbsent = useStemmingIfLemmasAbsent;
    }

    public DependencyInstance getNext() throws IOException {

	ArrayList<String[]> lineList = new ArrayList<String[]>();

	String line = inputReader.readLine();
	while (line != null && !line.equals("") && !line.startsWith("*")) {
	    lineList.add(line.split("\t"));
	    line = inputReader.readLine();
	    //System.out.println("## "+line);
	}
	int length = lineList.size();

	if(length == 0) {
	    inputReader.close();
	    return null;
	}

	String[] forms = new String[length+1];
	String[] lemmas = new String[length+1];
	String[] cpos = new String[length+1];
	String[] pos = new String[length+1];
	String[][] feats = new String[length+1][];
	String[] deprels = new String[length+1];
	int[] heads = new int[length+1];
	String[] deprels_pred = new String[length+1]; // For stacked learning --- afm 03-10-08
	int[] heads_pred = new int[length+1];	// For stacked learning --- afm 03-10-08
	
	
	forms[0] = "<root>";
	lemmas[0] = "<root-LEMMA>";
	cpos[0] = "<root-CPOS>";
	pos[0] = "<root-POS>";
	deprels[0] = "<no-type>";
	heads[0] = -1;

	for(int i = 0; i < length; i++) {
	    String[] info = lineList.get(i);
	    
	    forms[i+1] = normalize(info[1]);
	    lemmas[i+1] = normalize(info[2]);
	    
	    // For languages that do not have lemma information --- afm 06-12-2008
	    if (useStemmingIfLemmasAbsent)
	    {
	    	if (lemmas[i+1].equals("_"))
	    		lemmas[i+1] = (forms[i+1].length() > 3)? forms[i+1].substring(0,3) : forms[i+1];
	    }
	    
	    cpos[i+1] = info[3];
	    pos[i+1] = info[4];
	    feats[i+1] = info[5].split("\\|");
	    
	    if (stacked == true) // For stacked learning --- afm 03-10-08
	    {
		    deprels_pred[i+1] = labeled ? info[7] : "<no-type>";
		    heads_pred[i+1] = Integer.parseInt(info[6]);
		    deprels[i+1] = labeled ? info[9] : "<no-type>";
		    heads[i+1] = Integer.parseInt(info[8]);
	    }
	    else
	    {
		    deprels[i+1] = labeled ? info[7] : "<no-type>";
		    heads[i+1] = Integer.parseInt(info[6]);
	    }
	}
	
	feats[0] = new String[feats[1].length];
	for (int i = 0; i< feats[1].length; i++)
	    feats[0][i] = "<root-feat>"+i;
	
	// The following stuff is for discourse and can be safely
	// ignored if you are doing sentential parsing. (In theory it
	// could be useful for sentential parsing.)
	if (discourseMode) {
	    String[][] extended_feats = new String[feats[0].length][length+1];
	    for (int i=0; i<extended_feats.length; i++) {
		for (int j=0; j<length+1; j++)
		    extended_feats[i][j] = feats[j][i];
	    }
	
	    feats = extended_feats;
	}

	ArrayList<RelationalFeature> rfeats = new ArrayList<RelationalFeature>();
	
	while (line != null && !line.equals("")) {
	    rfeats.add(new RelationalFeature(length, line, inputReader));
	    line = inputReader.readLine();
	}

	RelationalFeature[] rfeatsList = new RelationalFeature[rfeats.size()];
	rfeats.toArray(rfeatsList);

	// End of discourse stuff.
	DependencyInstance instance;
	if(stacked)
	{
		instance = new DependencyInstance(forms, lemmas, cpos, pos, feats, deprels, heads, rfeatsList, deprels_pred, heads_pred, stacked);
	}
	else
	{
		instance = new DependencyInstance(forms, lemmas, cpos, pos, feats, deprels, heads, rfeatsList);
	}
	
	return instance;

    }


    protected boolean fileContainsLabels (String file) throws IOException {
	BufferedReader in = new BufferedReader(new FileReader(file));
	String line = in.readLine();
	in.close();

	if(line.trim().length() > 0) 
	    return true;
	else
	    return false;
    }

}
