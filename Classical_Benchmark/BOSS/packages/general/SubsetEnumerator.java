package general;

import java.util.Arrays;

public class SubsetEnumerator
{
	 
	final private int n;
	final private int s;
	private int currentSubset;
	
	 
	public SubsetEnumerator( int n, int s ){
		this.n = n;
		this.s = s;
	}

	 
	public static void main(String[] args)
	{
		int n=6;
		System.out.println(" Testing the subset enumerator object \n The total number of elements is: "+n);
 		for(int s=1; s<=n; s++)
		{
			System.out.println("Current subset size is: "+s);
			SubsetEnumerator subsetEnumerator = new SubsetEnumerator(n, s);
			int subset = subsetEnumerator.getFirstSubset();   
 			while(subset < (1<<n)){
 				System.out.println( "the current subset is "+Arrays.toString(Combinations.convertCombinationFromBitToByteFormat( subset, n, s)));
 				subset = subsetEnumerator.getNextSubset();
			}
		}
	}
	
	 
	public int getFirstSubset()
	{
		currentSubset=0;
		for(int i=0; i<s; i++)
			currentSubset += (1<<i);
		return( currentSubset );
	}
	
	 
	public int getNextSubset()
	{
		currentSubset = Combinations.getPreviousCombinationInBitFormat2(n, s, currentSubset);
		return(currentSubset);
	}
}