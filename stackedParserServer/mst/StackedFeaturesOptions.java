package mst;


/**
 * @author Andre Martins afm@cs.cmu.edu 03/19/2008
 * Class to check the feature options for stacked parsing
 *
 */
public class StackedFeaturesOptions
{
	public boolean usePredEdge = true;
	public boolean usePrevSibl = true;
	public boolean useNextSibl = true;
	public boolean useLabels = true;
	public boolean useGrandparents = true;
	public boolean useValency = false;
	public boolean useAllChildren = true; 
	public boolean usePredHead = true; 
	
	
	public void display()
	{
		System.out.println("Stacked features options:");
		System.out.println("  usePredEdge = " + usePredEdge);
		System.out.println("  usePrevSibl = " + usePrevSibl);
		System.out.println("  useNextSibl = " + useNextSibl);
		System.out.println("  useLabels = " + useLabels);
		System.out.println("  useGrandparents = " + useGrandparents);
		System.out.println("  useValency = " + useValency);	
		System.out.println("  useAllChildren = " + useAllChildren);	
		System.out.println("  usePredHead = " + usePredHead);	
	}

}