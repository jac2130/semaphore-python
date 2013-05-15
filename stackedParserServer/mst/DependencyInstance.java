package mst;

import gnu.trove.*;
import java.io.*;
import java.util.*;

public class DependencyInstance implements Serializable {

    public FeatureVector fv;
    public String actParseTree;

    // The various data types. Here's an example from Portuguese:
    //
    // 3  eles ele   pron       pron-pers M|3P|NOM 4    SUBJ   _     _
    // ID FORM LEMMA COURSE-POS FINE-POS  FEATURES HEAD DEPREL PHEAD PDEPREL
    //
    // We ignore PHEAD and PDEPREL for now. 

    // FORM: the forms - usually words, like "thought"
    public String[] forms;

    // LEMMA: the lemmas, or stems, e.g. "think"
    public String[] lemmas;

    // COURSE-POS: the course part-of-speech tags, e.g."V"
    public String[] cpostags;

    // FINE-POS: the fine-grained part-of-speech tags, e.g."VBD"
    public String[] postags;

    // FEATURES: some features associated with the elements separated by "|", e.g. "PAST|3P"
    public String[][] feats;
    
    // afm 03-07-08 --- true if there are stacked learning features
    public boolean stacked=false;
    
    // afm 03-07-08 --- heads predicted by the base classifier in a stacked learning framework
    public int[] heads_pred;

    // afm 03-07-08 --- dependency relations predicted by the base classifier in a stacked learning framework
    public String[] deprels_pred;

    // HEAD: the IDs of the heads for each element
    public int[] heads;

    // DEPREL: the dependency relations, e.g. "SUBJ"
    public String[] deprels;

    // RELATIONAL FEATURE: relational features that hold between items
    public RelationalFeature[] relFeats;

    public DependencyInstance() {}

    public DependencyInstance(DependencyInstance source) {
	this.fv = source.fv;
	this.actParseTree = source.actParseTree;
    }
    
    public DependencyInstance(String[] forms, FeatureVector fv) {
	this.forms = forms;
	this.fv = fv;
    }
    
    public DependencyInstance(String[] forms, String[] postags, FeatureVector fv) {
	this(forms, fv);
	this.postags = postags;
    }
    
    public DependencyInstance(String[] forms, String[] postags, 
			      String[] labs, FeatureVector fv) {
	this(forms, postags, fv);
	this.deprels = labs;
    }

    public DependencyInstance(String[] forms, String[] postags, 
			      String[] labs, int[] heads) {
	this.stacked = false; // afm 03-07-2008
    this.forms = forms;
	this.postags = postags;
	this.deprels = labs;
	this.heads = heads;
    }

    public DependencyInstance(String[] forms, String[] lemmas, String[] cpostags, 
			      String[] postags, String[][] feats, String[] labs, int[] heads) {
	this(forms, postags, labs, heads);
	this.lemmas = lemmas;
	this.cpostags = cpostags;
	this.feats = feats;
    }

    public DependencyInstance(String[] forms, String[] lemmas, String[] cpostags, 
			      String[] postags, String[][] feats, String[] labs, int[] heads,
			      RelationalFeature[] relFeats) {
	this(forms, lemmas, cpostags, postags, feats, labs, heads);
	this.relFeats = relFeats;
    }
    
    public DependencyInstance(String[] forms, String[] lemmas, String[] cpostags, 
		      String[] postags, String[][] feats, String[] labs, int[] heads,
		      RelationalFeature[] relFeats, String[] deprels_pred, int[] heads_pred, boolean stacked) {
    	this(forms, lemmas, cpostags, postags, feats, labs, heads, relFeats);
    	this.stacked = stacked;
    	this.deprels_pred = deprels_pred;
    	this.heads_pred = heads_pred;
}

    public void setFeatureVector (FeatureVector fv) {
	this.fv = fv;
    }


    public int length () {
	return forms.length;
    }

    public String toString () {
	StringBuffer sb = new StringBuffer();
	sb.append(Arrays.toString(forms)).append("\n");
	return sb.toString();
    }


    public void setPreviousLevelsInfo(String[] depRelPred, int[] headPred)
    {
    	this.deprels_pred = depRelPred;
    	this.heads_pred = headPred;
    }
    
    private void writeObject (ObjectOutputStream out) throws IOException {
	out.writeObject(forms);
	out.writeObject(lemmas);
	out.writeObject(cpostags);
	out.writeObject(postags);
	out.writeObject(heads);
	out.writeObject(deprels);
	out.writeObject(actParseTree);
	out.writeObject(feats);
	out.writeObject(relFeats);
	out.writeObject(deprels_pred);
	out.writeObject(heads_pred);
	out.writeObject(new Boolean(stacked));
    }


    private void readObject (ObjectInputStream in) throws IOException, ClassNotFoundException {
	forms = (String[])in.readObject();
	lemmas = (String[])in.readObject();
	cpostags = (String[])in.readObject();
	postags = (String[])in.readObject();
	heads = (int[])in.readObject();
	deprels = (String[])in.readObject();
	actParseTree = (String)in.readObject();
	feats = (String[][])in.readObject();
	relFeats = (RelationalFeature[])in.readObject();
	deprels_pred = (String[])in.readObject();
	heads_pred = (int[])in.readObject();
	stacked = (Boolean)in.readObject();
    }

}
