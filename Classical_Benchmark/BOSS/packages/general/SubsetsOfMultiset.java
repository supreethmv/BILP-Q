package general;

import java.util.Arrays;
import java.util.Random;

public class SubsetsOfMultiset
{
	final private ElementOfMultiset[] multiset; 
 	
	final private int sizeOfSubsets;  
	
	private int totalNumOfElementsInMultiset;  
	
	private ElementOfMultiset[] multisetWithIncrementalElements;  
	
	private boolean currentSubsetIsFirstSubset;  
	private ElementOfMultiset[] currentSubset; 
 	
	private int numOfUniqueElementsInCurrentSubset; 
 
	
	private ElementOfMultiset[] lastSubset; 
 
	
	private int[] numOfInstancesOutsideSubset;  
	
	private final boolean keepTrackOfNumOfInstancesOutsideSubset;  
	public SubsetsOfMultiset( ElementOfMultiset[] multiset, int sizeOfSubsets, boolean keepTrackOfNumOfInstancesOutsideSubset )
	{
 		this.multiset = multiset;
		this.sizeOfSubsets = sizeOfSubsets;
		this.keepTrackOfNumOfInstancesOutsideSubset = keepTrackOfNumOfInstancesOutsideSubset;
		resetParameters();
	}
	
	 
	public void resetParameters()
	{
		currentSubsetIsFirstSubset = true;
		
		multisetWithIncrementalElements = new ElementOfMultiset[ multiset.length ];
		for(int i=0; i<multiset.length; i++){
			multisetWithIncrementalElements[i] = new ElementOfMultiset( i+1, multiset[i].repetition );
		}
		setLastSubset();
		currentSubset = new ElementOfMultiset[ multiset.length ];
		
 		totalNumOfElementsInMultiset = 0;
		for( int i=0; i<multiset.length; i++){
			totalNumOfElementsInMultiset += multiset[i].repetition;
		}
	}
 
	public static void main(String[] args)
	{
 		ElementOfMultiset[] multiset = new ElementOfMultiset[ 3 ];
		multiset[0] = new ElementOfMultiset( 4, 3 );
		multiset[1] = new ElementOfMultiset( 5, 2 );
		multiset[2] = new ElementOfMultiset( 6, 1 );
		
 		for(int size=1; size<=3; size++)
		{
			System.out.println("Current size is "+size);
			SubsetsOfMultiset subsetsOfMultiset = new SubsetsOfMultiset( multiset, size, true );
			ElementOfMultiset[] subset = subsetsOfMultiset.getNextSubset();
			while(subset != null){
 				System.out.println( "the current subset is "+General.convertMultisetToString( subset )+
						" , and numOfInstancesOutsideSubset = "+Arrays.toString( subsetsOfMultiset.getNumOfInstancesOutsideSubset() ) );
				subset = subsetsOfMultiset.getNextSubset();
			}
		}
	}
	
	 
	public ElementOfMultiset[] getNextSubset()
	{
		if( currentSubsetIsFirstSubset )
		{
			setCurrentSubsetToFirstSubset();
			currentSubsetIsFirstSubset = false;
			return( prepareResult() );
		}
		else  
		{
			int totalNumberOfElementsSeenSoFar = 0;
			int indexInLastSubset = lastSubset.length-1;
			for(int indexInCurrentSubset = numOfUniqueElementsInCurrentSubset-1; indexInCurrentSubset>=0; indexInCurrentSubset--)
			{	
				if( currentSubset[ indexInCurrentSubset ].element != lastSubset[ indexInLastSubset ].element )
				{
 					if( currentSubset[ indexInCurrentSubset ].repetition > 1 )
					{
						currentSubset[ indexInCurrentSubset ].repetition--;
						currentSubset[ indexInCurrentSubset+1 ].element = currentSubset[ indexInCurrentSubset ].element+1;
						currentSubset[ indexInCurrentSubset+1 ].repetition = 1;
						numOfUniqueElementsInCurrentSubset++;
						fillRemainingAgents( totalNumberOfElementsSeenSoFar, indexInCurrentSubset+1 );
					}else{
						currentSubset[ indexInCurrentSubset ].element++;
						fillRemainingAgents( totalNumberOfElementsSeenSoFar, indexInCurrentSubset );
					}
					return( prepareResult() );
				}else{
					if( currentSubset[ indexInCurrentSubset ].repetition < lastSubset[ indexInLastSubset ].repetition )
					{
						totalNumberOfElementsSeenSoFar  += currentSubset[indexInCurrentSubset].repetition;
						numOfUniqueElementsInCurrentSubset--;
						indexInCurrentSubset--;
						
 						if( currentSubset[ indexInCurrentSubset ].repetition > 1 )
						{
							currentSubset[ indexInCurrentSubset ].repetition--;
							currentSubset[ indexInCurrentSubset+1 ].element = currentSubset[ indexInCurrentSubset ].element+1;
							currentSubset[ indexInCurrentSubset+1 ].repetition = 1;
							numOfUniqueElementsInCurrentSubset++;
							fillRemainingAgents( totalNumberOfElementsSeenSoFar, indexInCurrentSubset+1 );
						}else{
							currentSubset[ indexInCurrentSubset ].element++;
							fillRemainingAgents( totalNumberOfElementsSeenSoFar, indexInCurrentSubset );
						}
						return( prepareResult() );
					}else{
						totalNumberOfElementsSeenSoFar  += currentSubset[indexInCurrentSubset].repetition;
						indexInLastSubset--;
						numOfUniqueElementsInCurrentSubset--;
					}
				}
			}
			return(null);
		}
	}
	
	 
	private void fillRemainingAgents( int totalNumOfAgentsToBeAdded, int indexAtWhichToStartFilling )
	{
		if( totalNumOfAgentsToBeAdded == 0 ){
			return;
		}
		int firstUniqueAgentToBeAdded = currentSubset[ indexAtWhichToStartFilling ].element;

 		int max = multisetWithIncrementalElements[ firstUniqueAgentToBeAdded-1 ].repetition - currentSubset[ indexAtWhichToStartFilling ].repetition;
		if( max > 0 ){
			if( totalNumOfAgentsToBeAdded <= max ){
				currentSubset[ indexAtWhichToStartFilling ].repetition += totalNumOfAgentsToBeAdded;
				return;
			}else{
				currentSubset[ indexAtWhichToStartFilling ].repetition += max;
				totalNumOfAgentsToBeAdded -= max;
			}
		}
 		int k=1;
		do{
			numOfUniqueElementsInCurrentSubset++;
			if( totalNumOfAgentsToBeAdded <= multisetWithIncrementalElements[ firstUniqueAgentToBeAdded+k-1 ].repetition ){
				currentSubset[ k + indexAtWhichToStartFilling ] = new ElementOfMultiset( firstUniqueAgentToBeAdded + k, totalNumOfAgentsToBeAdded);
				break;
			}else{
				currentSubset[ k + indexAtWhichToStartFilling ] = new ElementOfMultiset( firstUniqueAgentToBeAdded + k, multisetWithIncrementalElements[ firstUniqueAgentToBeAdded+k-1 ].repetition);
				totalNumOfAgentsToBeAdded -= multisetWithIncrementalElements[ firstUniqueAgentToBeAdded+k-1 ].repetition;
				k++;
			}
		}
		while(true);
	}
	
	 
	private ElementOfMultiset[] prepareResult()
	{
 		ElementOfMultiset[] subsetWithOriginalElements = new ElementOfMultiset[ numOfUniqueElementsInCurrentSubset ];
		if( keepTrackOfNumOfInstancesOutsideSubset ){
			numOfInstancesOutsideSubset = new int[ numOfUniqueElementsInCurrentSubset ];
		}
		
		for(int i=0; i < numOfUniqueElementsInCurrentSubset; i++)
		{
			ElementOfMultiset originalElement = multiset[ currentSubset[i].element - 1 ]; 
			subsetWithOriginalElements[i] = new ElementOfMultiset( originalElement.element, currentSubset[i].repetition );
		
			if( keepTrackOfNumOfInstancesOutsideSubset ){
				numOfInstancesOutsideSubset[i] = originalElement.repetition - currentSubset[i].repetition;
			}
		}
		return( subsetWithOriginalElements );
	}
	
	 
	private void setCurrentSubsetToFirstSubset()
	{
 		currentSubset = new ElementOfMultiset[ multisetWithIncrementalElements.length ];
		for(int i=0; i<currentSubset.length; i++){
			currentSubset[i] = new ElementOfMultiset( 0 , 0 );
		}
 		if( keepTrackOfNumOfInstancesOutsideSubset )
			numOfInstancesOutsideSubset = new int[ multisetWithIncrementalElements.length ];

		int totalNumOfAgentsToBeAdded = sizeOfSubsets;
		int i=0;
		for(int j=0; j<multisetWithIncrementalElements.length; j++)
		{
			if( totalNumOfAgentsToBeAdded <= multisetWithIncrementalElements[j].repetition ){
				currentSubset[i] = new ElementOfMultiset( multisetWithIncrementalElements[j].element, totalNumOfAgentsToBeAdded);
 				if( keepTrackOfNumOfInstancesOutsideSubset ){
					numOfInstancesOutsideSubset[i] = multisetWithIncrementalElements[j].repetition - totalNumOfAgentsToBeAdded;
				}
				break;
			}else{
				currentSubset[i] = new ElementOfMultiset( multisetWithIncrementalElements[j].element, multisetWithIncrementalElements[j].repetition);
				totalNumOfAgentsToBeAdded -= multisetWithIncrementalElements[j].repetition;
				i++;
 				if( keepTrackOfNumOfInstancesOutsideSubset ){
					numOfInstancesOutsideSubset[i] = 0;
				}
			}
		}
		numOfUniqueElementsInCurrentSubset = i+1;
	}
	
	 
	private void setLastSubset()
	{
 		ElementOfMultiset[] temp = new ElementOfMultiset[ multisetWithIncrementalElements.length ];
		for(int i=0; i<temp.length; i++)
			temp[i] = new ElementOfMultiset( 0 , 0 );
		
		int totalNumOfAgentsToBeAdded = sizeOfSubsets;
		int i=temp.length-1;
		for(int j=multisetWithIncrementalElements.length-1; j>=0; j--)
		{
			if( totalNumOfAgentsToBeAdded <= multisetWithIncrementalElements[j].repetition ){
				temp[i] = new ElementOfMultiset( multisetWithIncrementalElements[j].element, totalNumOfAgentsToBeAdded);
				break;
			}else{
				temp[i] = new ElementOfMultiset( multisetWithIncrementalElements[j].element, multisetWithIncrementalElements[j].repetition);
				totalNumOfAgentsToBeAdded -= multisetWithIncrementalElements[j].repetition;
				i--;
			}
		}
 		lastSubset = new ElementOfMultiset[ multisetWithIncrementalElements.length - i ];
		for(int j=lastSubset.length-1; j>=0; j--)
			lastSubset[j] = temp[ temp.length - lastSubset.length + j ];
	}
	
	 
	public int[] getNumOfInstancesOutsideSubset()
	{
		if( keepTrackOfNumOfInstancesOutsideSubset )
			return( numOfInstancesOutsideSubset );
		else{
			System.err.println("the method getNumOfInstancesOutsideSubset was called while the" +
					"parameter keepTrackOfNumOfInstancesOutsideSubset was set to false");
			return(null);
		}
	}
	
	 
	public ElementOfMultiset[] getRandomSubset()
	{
 		if(( sizeOfSubsets <= 0 )||( sizeOfSubsets >= totalNumOfElementsInMultiset )){
			System.err.println("ERROR in method: getRandomSubset, the size of the desired subset must be between 1 and totalNumOfElements-1");
			return(null);
		}
		
		Random randomIndex = new Random();
		Random randomRepetition = new Random();
		
 		int sizeThatWeWillConsider;
		if( sizeOfSubsets <= totalNumOfElementsInMultiset/2 )
			sizeThatWeWillConsider = sizeOfSubsets;
		else
			sizeThatWeWillConsider = totalNumOfElementsInMultiset - sizeOfSubsets;
			
 		int[] indicesOfElementsThatCanBeAdded = new int[ multiset.length ];
		
 		int[] numOfRepetitionsInSubset = new int[ multiset.length ];
		
 		int maxIndex = multiset.length;
		
 		for(int i=multiset.length-1; i>=0; i--)
		{
			indicesOfElementsThatCanBeAdded[i] = i;
			numOfRepetitionsInSubset[i] = 0;
		}
 		int sizeOfSubsetSoFar=0;
		int numOfUniqueElementsInSubset=0;
		do{
 			int i = randomIndex.nextInt( maxIndex );
			int j = indicesOfElementsThatCanBeAdded[ i ];
			
 			int k = 1;  

 			if( numOfRepetitionsInSubset[j] == 0 )
				numOfUniqueElementsInSubset++;
			
 			numOfRepetitionsInSubset[j] += k;
			
 			sizeOfSubsetSoFar += k;
			
 			if( numOfRepetitionsInSubset[j] == multiset[j].repetition ){
				indicesOfElementsThatCanBeAdded[ i ] = indicesOfElementsThatCanBeAdded[ maxIndex-1 ];
				maxIndex--;
			}
		}
		while( sizeOfSubsetSoFar != sizeThatWeWillConsider );

		 
		if( sizeOfSubsets <= totalNumOfElementsInMultiset/2 )
		{
 			if( keepTrackOfNumOfInstancesOutsideSubset )
				numOfInstancesOutsideSubset = new int[ numOfUniqueElementsInSubset ];

			ElementOfMultiset[] subset = new ElementOfMultiset[ numOfUniqueElementsInSubset ];
			int j=0;
			for(int i=0; i < multiset.length; i++)
			{
				if( numOfRepetitionsInSubset[i] > 0 )
				{
					subset[j] = new ElementOfMultiset( multiset[i].element, numOfRepetitionsInSubset[i]);

 					if( keepTrackOfNumOfInstancesOutsideSubset )
						numOfInstancesOutsideSubset[j] = multiset[i].repetition - numOfRepetitionsInSubset[i];
					
					j++;
					if( j == subset.length ){
						break;
					}
				}
			}
			return( subset );
		}else{
 			ElementOfMultiset[] temp_subset = new ElementOfMultiset[ multiset.length ];
			int[] temp_numOfInstancesOutsideSubset = new int[ multiset.length ];
			int j=0;
			for(int i=0; i < multiset.length; i++)
			{
				if( numOfRepetitionsInSubset[i] < multiset[i].repetition ){
					temp_subset[j] = new ElementOfMultiset( multiset[i].element, multiset[i].repetition - numOfRepetitionsInSubset[i]);
					
 					if( keepTrackOfNumOfInstancesOutsideSubset )
						temp_numOfInstancesOutsideSubset[j] = numOfRepetitionsInSubset[i];

					j++;
				}
			}
 			ElementOfMultiset[] subset = new ElementOfMultiset[ j ];
			numOfInstancesOutsideSubset = new int[ j ];
			for(int i=0; i<j; i++){
				subset[i] = temp_subset[i];
				numOfInstancesOutsideSubset[i] = temp_numOfInstancesOutsideSubset[i];
			}
			return( subset );
		}
	}
}