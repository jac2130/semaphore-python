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

import mst.DependencyInstance;

/**
 * A writer to create files in CONLL format.
 *
 * <p>
 * Created: Sat Nov 10 15:25:10 2001
 * </p>
 *
 * @author Jason Baldridge
 * @version $Id: CONLLWriter.java 94 2007-01-17 17:05:12Z jasonbaldridge $
 * @see mstparser.io.DependencyWriter
 */
public class CONLLWriter extends DependencyWriter {

    public CONLLWriter (boolean labeled) {
    	this.labeled = labeled;
    }

    public void write(DependencyInstance instance) throws IOException {
	
	for (int i=0; i<instance.length(); i++) {
	    writer.write(Integer.toString(i+1));                writer.write('\t');
	    writer.write(instance.forms[i]);                    writer.write('\t');
	    writer.write(instance.forms[i]);                    writer.write('\t'); // Shouldn't it be "lemmas" here??
	    //writer.write(instance.cpostags[i]);                 writer.write('\t');
	    writer.write(instance.postags[i]);                  writer.write('\t');
	    writer.write(instance.postags[i]);                  writer.write('\t');
	    writer.write("-");                                  writer.write('\t');
	    // afm 03-07-08
	    if (instance.stacked)
	    {
		    writer.write(Integer.toString(instance.heads_pred[i]));  writer.write('\t');
		    writer.write(instance.deprels_pred[i]);                  writer.write('\t');	    	
	    }
	    writer.write(Integer.toString(instance.heads[i]));  writer.write('\t');
	    writer.write(instance.deprels[i]);                  writer.write('\t');
	    writer.write("-\t-");
	    writer.newLine();
	}
	writer.newLine();

    }
}
