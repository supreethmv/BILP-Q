package general;
import java.math.BigInteger;
public class Combinations
{
	private static long[][] pascalMatrix = null;
	public static long[][] initPascalMatrix( int numOfLines, int numOfColumns )
	{
		if(( pascalMatrix==null )||( numOfLines > pascalMatrix.length ))
		{
			if( pascalMatrix == null )  
			{
				pascalMatrix = new long[numOfLines][numOfColumns];
				for(int i=0; i<numOfLines;   i++) { pascalMatrix[i][0]=1;  }
				for(int j=1; j<numOfColumns; j++) {	pascalMatrix[0][j]=j+1;}
				for(int i=1; i<numOfLines; i++)
					for(int j=1; j<numOfColumns; j++)
						pascalMatrix[i][j]=pascalMatrix[i-1][j]+pascalMatrix[i][j-1];
			}
			else 
 			{
				long[][] prev_pascalMatrix = pascalMatrix;
				int prev_numOfLines   = prev_pascalMatrix.length;
				int prev_numOfColumns = prev_pascalMatrix[0].length; 
				pascalMatrix = new long[numOfLines][numOfColumns];
				for(int i=0; i<numOfLines  ; i++) { pascalMatrix[i][0]=1;  }
				for(int j=1; j<numOfColumns; j++) { pascalMatrix[0][j]=j+1;}
				for(int i=1; i<prev_numOfLines; i++)
				{
                                    for(int j=1; j<prev_numOfColumns; j++)
						pascalMatrix[i][j]=prev_pascalMatrix[i][j];
                                    for(int j=prev_numOfColumns; j<numOfColumns; j++)
						pascalMatrix[i][j]=pascalMatrix[i-1][j]+pascalMatrix[i][j-1];;
				}
                                for(int i=prev_numOfLines; i<numOfLines; i++)
					for(int j=1; j<numOfColumns; j++)
						pascalMatrix[i][j]=pascalMatrix[i-1][j]+pascalMatrix[i][j-1];
                                prev_pascalMatrix=null;
			}
		}
		return( pascalMatrix );
	}
	public static long binomialCoefficient(int x, int y)
	{
		if( x==y ) return( 1 ); 
                initPascalMatrix( x, x );
                return( pascalMatrix[x-y-1][y] );
	}
    public static int getSizeOfCombinationInBitFormat( int combinationInBitFormat, int numOfAgents )
    {
    	return( Integer.bitCount( combinationInBitFormat ) );
    }	
    public static long getNumOfCS( int numOfAgents )
    {
    	long numOfCS =0;
    	for(int size=1; size<=numOfAgents; size++)
    		numOfCS += Combinations.getNumOfCSOfCertainSize(numOfAgents,size);
    	return( numOfCS );
    }
    public static BigInteger getNumOfCS_bitIntegerVersion( int numOfAgents )
    {
    	BigInteger numOfCS = BigInteger.valueOf(0);	
    	for(int size=1; size<=numOfAgents; size++)
    		numOfCS = numOfCS.add(Combinations.getNumOfCSOfCertainSize_bigIntegerVersion(numOfAgents,size));
    	return( numOfCS );
    }
    public static long getNumOfCoalitionsInSearchSpace( int numOfAgents )
    {
    	long numOfCoalitionsInSearchSpace =0;
    	for(int size=1; size<=numOfAgents; size++)
    		numOfCoalitionsInSearchSpace += size * Combinations.getNumOfCSOfCertainSize(numOfAgents,size);
    	return( numOfCoalitionsInSearchSpace );
    }
    public static BigInteger getNumOfCoalitionsInSearchSpace_bitIntegerVersion( int numOfAgents )
    {
    	BigInteger numOfCoalitionsInSearchSpace = BigInteger.valueOf(0);	
    	for(int size=1; size<=numOfAgents; size++) {
    		numOfCoalitionsInSearchSpace = numOfCoalitionsInSearchSpace.add(Combinations.getNumOfCSOfCertainSize_bigIntegerVersion(numOfAgents,size));
    		numOfCoalitionsInSearchSpace = numOfCoalitionsInSearchSpace.multiply( BigInteger.valueOf(size) );
    	}
    	return( numOfCoalitionsInSearchSpace );
    }
	public static long getNumOfCSOfCertainSize( int numOfAgents, int size )
	{
	    if( (size==1)||(size==numOfAgents) )
	    	return(1);
	    else
	    	return(size* getNumOfCSOfCertainSize((int)(numOfAgents-1),size) + getNumOfCSOfCertainSize((int)(numOfAgents-1),(int)(size-1)));
	}	
        public static BigInteger getNumOfCSOfCertainSize_bigIntegerVersion( int numOfAgents, int size )
	{
	    if( (size==1)||(size==numOfAgents) )
	    	return( BigInteger.valueOf(1) );
	    else
	    {
	    	BigInteger solution = getNumOfCSOfCertainSize_bigIntegerVersion( (int)(numOfAgents-1), size ).multiply( BigInteger.valueOf(size) );
	    	solution = solution.add( getNumOfCSOfCertainSize_bigIntegerVersion( (int)(numOfAgents-1), (int)(size-1) ) );
	    	return( solution );
	    }
	}
	public static int convertCombinationFromByteToBitFormat( int[] combinationInByteFormat )
	{
   		return( convertCombinationFromByteToBitFormat( combinationInByteFormat, combinationInByteFormat.length ) );
	}	
	public static int convertCombinationFromByteToBitFormat( int[] combinationInByteFormat, int combinationSize )
	{
   		int combinationInBitFormat = 0;
   		for(int i=0; i<combinationSize; i++)
                    combinationInBitFormat += 1 << (combinationInByteFormat[i]-1);
   		return( combinationInBitFormat );
	}
	public static int[] convertCombinationFromBitToByteFormat( int combinationInBitFormat, int numOfAgents, int combinationSize )
	{
		int[] combinationInByteFormat = new int[ combinationSize ];
		int j=0;
		for(int i=0; i<numOfAgents; i++){
			if ((combinationInBitFormat & (1<<i)) != 0){  
				combinationInByteFormat[j]= (int)(i+1);
				j++;
			}
		}
		return( combinationInByteFormat );
	}
	public static int[] convertCombinationFromBitToByteFormat( int combinationInBitFormat, int numOfAgents )
	{
            int combinationSize = getSizeOfCombinationInBitFormat( combinationInBitFormat, numOfAgents);
		return( convertCombinationFromBitToByteFormat(combinationInBitFormat, numOfAgents, combinationSize) );
	}
	public static int[][] convertSetOfCombinationsFromBitToByteFormat( int[] setOfCombinationsInBitFormat,int numOfAgents)
	{
            int[] sizeOfEachCombination = new int[ setOfCombinationsInBitFormat.length ];    	
        for(int i=setOfCombinationsInBitFormat.length-1; i>=0; i--)
        	sizeOfEachCombination[i] = (int)getSizeOfCombinationInBitFormat( setOfCombinationsInBitFormat[i] , numOfAgents);
        return( convertSetOfCombinationsFromBitToByteFormat(setOfCombinationsInBitFormat,numOfAgents,sizeOfEachCombination));
        }
	public static int[][] convertSetOfCombinationsFromBitToByteFormat(
			int[] setOfCombinationsInBitFormat, int numOfAgents, int[] sizeOfEachCombination )
	{
        int[][] setOfCombinationsInByteFormat = new int[ setOfCombinationsInBitFormat.length ][];
    	for(int i=0; i<setOfCombinationsInBitFormat.length; i++)
    		setOfCombinationsInByteFormat[i] = convertCombinationFromBitToByteFormat( setOfCombinationsInBitFormat[i], numOfAgents); 
    	return(setOfCombinationsInByteFormat);		
	}
	public static int[] convertSetOfCombinationsFromByteToBitFormat( int[][] setOfCombinationsInByteFormat )
	{
    	int[] setOfCombinationsInBitFormat = new int[ setOfCombinationsInByteFormat.length ];
    	for(int i=0; i<setOfCombinationsInByteFormat.length; i++)
    		setOfCombinationsInBitFormat[i] = convertCombinationFromByteToBitFormat( setOfCombinationsInByteFormat[i] );
    	return( setOfCombinationsInBitFormat );
    }
	public static int[][] getCombinationsOfGivenSize(int numOfAgents, int size )
	{
		int[][] list = new int[ (int)binomialCoefficient(numOfAgents, size) ][size];		
		int index = list.length - 1;
 		for(int i=1;i<=size;i++)
			list[index][i-1]=i;
 		int maxPossibleValueForFirstAgent = (int)(numOfAgents-size+1);
		while( index>0 )
		{
			for(int i=size-1; i>=0; i--)
			{
 				if( list[index][i] < maxPossibleValueForFirstAgent+i )
				{					
					index--;
					for(int j=0; j<i; j++)
						list[index][j]=list[index+1][j];
					list[index][i]=(int)(list[index+1][i]+1);
					for(int j=i+1; j<size; j++)
						list[index][j]=(int)(list[index][j-1]+1);
					break;
				}
			}
		}
		return(list);
	}
	public static int[] getCombinationsOfGivenSizeInBitFormat(int numOfAgents, int size)
	{
 		final int[] onesBeforeIndex = new int[ numOfAgents+1 ];
		for(int k=numOfAgents; k>0; k--)
			onesBeforeIndex[k] = (1<<k) - 1;
		int[] list=new int[ (int)binomialCoefficient(numOfAgents, size) ];
		int index = list.length - 1;
 		list[index]=0;
		for(int i=1;i<=size;i++)
			list[index] += (1<<(i-1));  
 		int maxPossibleValueForFirstAgent = (int)(numOfAgents-size+1);
		while( index>0 )  
		{
 			int i=size-1;  
			for(int k=numOfAgents; k>0; k--)
			{
				if ((list[index] & (1<<(k-1))) != 0)  
				{
					if( k < maxPossibleValueForFirstAgent+i ) 
 					{					
						index--;
						list[index] = (list[index+1] & onesBeforeIndex[k-1]);
						list[index] += (1<<k); 
						for(int j=1; j<size-i; j++)
							list[index] += (1<<(k+j)); 
						i--;
						break;
					}
					i--;
				}
			}
		}
		return(list);
	}
	public static void getPreviousCombination( final int numOfAgents, final int size, int[] combination)
	{
		final int maxPossibleValueForFirstAgent = (int)(numOfAgents-size+1);
		for(int i=size-1; i>=0; i--) {
			if( combination[i] < maxPossibleValueForFirstAgent+i ) {
					combination[i]++;				
				for(int j=i+1; j<size; j++) {
					combination[j]=combination[j-1]+1;
				}
				break;
			}
		}			
	}
	public static int[][] getCombinationsOfGivenSize_multisetVersion_oldVersion( int[] multiset )
	{
 		int[][] subsets;
		int indexInSubsets=0;
		int[] underlyingSet = General.getUnderlyingSet( multiset );
		int sizeOfUnderlyingSet = underlyingSet.length;
 		int[] multiplicity = new int[ sizeOfUnderlyingSet ];
		for(int i=0; i<sizeOfUnderlyingSet; i++)
			multiplicity[i] = General.getMultiplicity( underlyingSet[i], multiset );
 		int[] sortedMultiplicity = General.sortArray( multiplicity, false );
		int upperBoundOnNumOfSubsets = 0;
		for(int curSize=1; curSize<=sizeOfUnderlyingSet; curSize++)
		{
			int x = 1;
			for(int i=0; i<curSize; i++)
				x *= sortedMultiplicity[i];
			upperBoundOnNumOfSubsets += x * Combinations.binomialCoefficient( sizeOfUnderlyingSet, curSize );
		}		
 		subsets = new int[ upperBoundOnNumOfSubsets ][];
 		for(int sizeOfCombination=1; sizeOfCombination<=sizeOfUnderlyingSet; sizeOfCombination++)
		{
			int[] curCombination = new int[sizeOfCombination];
			long numOfCombinationsOfCurSize = Combinations.binomialCoefficient( sizeOfUnderlyingSet, sizeOfCombination );
			for(int i=0; i<numOfCombinationsOfCurSize; i++)
			{
				if( i==0 )
					curCombination = Combinations.getCombinationAtGivenIndex( sizeOfCombination, 0, sizeOfUnderlyingSet );
				else
					Combinations.getNextCombination( sizeOfUnderlyingSet, sizeOfCombination, curCombination );
                                indexInSubsets += getSubsetsThatMatchCombination( curCombination, sizeOfCombination, subsets, indexInSubsets, underlyingSet, multiplicity );
			}
		}		
 		int[][] temp = new int[ indexInSubsets ][];
		for(int i=0; i<indexInSubsets; i++)
			temp[i] = subsets[i];
		subsets = temp;
		return( subsets );
	}
	private static int getSubsetsThatMatchCombination( int[] combination, int sizeOfCombination,
			int[][] subsets, int indexInSubsets, int[] underlyingSet, int[] multiplicity )
	{
		int[] numOfRepetitions = new int[sizeOfCombination];
 		int numOfPossibilitiesOfNumOfRepetitions = 1;
		for(int i=0; i<combination.length; i++)
			numOfPossibilitiesOfNumOfRepetitions *= multiplicity[ combination[i]-1 ];
 		for(int i=0; i<numOfPossibilitiesOfNumOfRepetitions; i++)
		{
			if( i==0 )
			{
 				for(int j=0; j<sizeOfCombination; j++)
					numOfRepetitions[j] = 1;
			}else
			{
 				for(int j=sizeOfCombination-1; j>=0; j--) {
					if( numOfRepetitions[j] < multiplicity[ combination[j]-1 ] ){
						numOfRepetitions[j] += 1;				
						for(int k=j+1; k<sizeOfCombination; k++)
							numOfRepetitions[k]=1;
						break;
					}
				}
			}
 			int sizeOfCurSubset = 0;
			for(int j=0; j<sizeOfCombination; j++)
				sizeOfCurSubset += numOfRepetitions[j];
			subsets[ indexInSubsets ] = new int[ sizeOfCurSubset ];
 			int m=0;
			for(int j=0; j<sizeOfCombination; j++){
				for(int k=0; k<numOfRepetitions[j]; k++){
					subsets[ indexInSubsets ][ m ] = underlyingSet[ combination[j]-1 ];
					m++;			
				}
			}
 			indexInSubsets++;
		}
 		return(numOfPossibilitiesOfNumOfRepetitions);
	}
	public static int getPreviousCombinationInBitFormat( final int numOfAgents, final int size, int combination)
	{
 		int i=size-1; 
		int maxPossibleValueForFirstAgent = (int)(numOfAgents-size+1);
		for(int k=numOfAgents; k>0; k--)
		{
			if ((combination & (1<<(k-1))) != 0) 
 			{
				if( k < maxPossibleValueForFirstAgent+i ) 
 				{
					combination &= (1<<(k-1)) - 1; 
					combination += (1<<k); 
					for(int j=1; j<size-i; j++) 
 						combination += (1<<(k+j)); 
					i--;
					break;
				}
				i--;
			}
		}
		return( combination );		
	}
	public static void getNextCombination( int numOfAgents, int size, int[] combination)
	{
		final int maxPossibleValueForFirstAgent = (int)(numOfAgents-size+1);
		for(int i=size-1; i>0; i--) {
			if( combination[i] != combination[i-1]+1 ) {
				combination[i]--;				
				for(int j=i+1; j<size; j++) {
					combination[j]=(int)(maxPossibleValueForFirstAgent+j);
				}
				return;
			}
		}
 		combination[0]--;
		for(int j=1; j<size; j++) {
			combination[j]=(int)(maxPossibleValueForFirstAgent+j);
		}		
	}
	public static int getPreviousCombinationInBitFormat2( int numOfAgents, int size, int combination)
	{
		int t = (combination | (combination - 1)) + 1;  
		return( t | ((((t & -t) / (combination & -combination)) >> 1) - 1) );
		 	}
	public static int getNextCombinationInBitFormat( int numOfAgents, int size, int combination)
	{
		int k2=0;
 		int i=size-1; 
		final int maxPossibleValueForFirstAgent = (int)(numOfAgents-size+1);
		for(int k=numOfAgents; k>0; k--)
		{
			if((combination & (1<<(k-1))) != 0)  
			{
				if((combination & (1<<(k-2))) == 0)  
				{
					combination &= (1<<(k-1)) - 1;  
					combination += (1<<(k-2));  			
					for(int j=i+1; j<size; j++)   
						combination += (1<<(maxPossibleValueForFirstAgent+j - 1));
					return( combination );
				}
				i--;
				if(i==0)
				{
 					k2 = k-1;
					while( (combination & (1<<(k2-1))) == 0 ) { 
 						k2--;
					}
					break;
				}
			}
		}
 		combination = (1<<(k2-2));  
		for(int j=1; j<size; j++)
 			combination += (1 << (int)( (maxPossibleValueForFirstAgent+j) - 1));
		return( combination );
	}
	public static int[] getCombinationAtGivenIndex(int size, int index, int numOfAgents)
	{
 		index++;
		initPascalMatrix( numOfAgents+1, numOfAgents+1 );
		int[] M=new int[size];
		boolean done=false;		
		 int j=1; int s1=size;
		do
		{
 			  int x=1;  while( pascalMatrix[s1-1][x-1] < index )  x++;
			  M[j-1]=(int)( (numOfAgents-s1+1)-x+1 );
			  if( pascalMatrix[s1-1][x-1]==index )
			{
 				for(int k=j; k<=size-1; k++) M[k]=(int)( M[k-1]+1 );
				done=true;
			}
			else  
			{
				j++;  index -=pascalMatrix[s1-1][x-2];  s1--;
			}
		}
		while( done==false );
		return(M);
	}
	public static int getIndexOfCombination( final int[] combination, final int size, final int numOfAgents )
	{
		long indexOfCombination=0;
		if( size==1 )
			indexOfCombination = numOfAgents-combination[0]+1;
		else
		{
			boolean sequence=true;
			for(int i1=size-1; i1>=1; i1--) {
				if( combination[i1]-combination[i1-1]>1 ) {
					indexOfCombination = pascalMatrix[ size-i1-1 ][ (numOfAgents-size+i1)-combination[i1]+1 ];
					for(int i2=i1-1; i2>=0; i2--) {					
						indexOfCombination += pascalMatrix[ size-i2-1 ][ (numOfAgents-size+i2)-combination[i2] ];
					}
					sequence=false;
					break;
				}
			}
			if( sequence )
				indexOfCombination=pascalMatrix[ size-1 ][ numOfAgents-size-combination[0]+1 ];
		}
		return( ((int)indexOfCombination) - 1 );
	}
}