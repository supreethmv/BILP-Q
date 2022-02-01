package general;

public class IntegerPartition
{
	public int[] partsSortedAscendingly;
	public int[] sortedMultiplicity;
	public int[] sortedUnderlyingSet;
	public int   sortedUnderlyingSetInBitFormat;
	public int   numOfAgents;
	public ElementOfMultiset[] sortedMultiset;
	public int[] tempIntegersThatResultedFromASplit; 
 
	public IntegerPartition( int[] parts )
	{
		numOfAgents=0;
		for(int i=0; i<parts.length; i++)
			numOfAgents += parts[i];
		partsSortedAscendingly = General.sortArray( parts , true );
		sortedUnderlyingSet = General.getUnderlyingSet( partsSortedAscendingly );
		sortedMultiplicity = new int[ sortedUnderlyingSet.length ];
		int indexInMultiplicity = 0;
		sortedMultiplicity[ indexInMultiplicity ]=1;
		for(int i=1; i<partsSortedAscendingly.length; i++){
			if( partsSortedAscendingly[i] == partsSortedAscendingly[i-1] )
				sortedMultiplicity[ indexInMultiplicity ]++;
			else{
				indexInMultiplicity++;
				sortedMultiplicity[ indexInMultiplicity ]=1;
			}					
		}
		sortedUnderlyingSetInBitFormat = Combinations.convertCombinationFromByteToBitFormat( sortedUnderlyingSet );
		
 		sortedMultiset = new ElementOfMultiset[ sortedMultiplicity.length ];
		for(int i=0; i<sortedMultiset.length; i++){
			sortedMultiset[i] = new ElementOfMultiset( sortedUnderlyingSet[i], sortedMultiplicity[i]);
		}
	}

	 
	public int getNumOfDirectedlyConnectedIntegerPartitions( int largestIntegerBeingSplit, int prev_largestIntegerBeingSplit )
	{
 		if( sortedUnderlyingSet[0] == numOfAgents)
			return( (int)Math.floor( numOfAgents / (double)2) );
			
		int counter=0;
		for(int i=0; i< sortedUnderlyingSet.length; i++)
		{
			if(( sortedUnderlyingSet[i] > largestIntegerBeingSplit )||( sortedUnderlyingSet[i] <= prev_largestIntegerBeingSplit )){
				continue;
			}
 			for(int j=1; j<=(int)Math.floor( sortedUnderlyingSet[i]/(double)2 ); j++)
			{
				int smallHalf = (int)j;
				int largeHalf = (int)(sortedUnderlyingSet[i] - j);
				if( largeHalf > numOfAgents-smallHalf-largeHalf ){
					continue;
				}
				counter++;
			}
		}
		return(counter);
	}

	 
	public IntegerPartition[] getListOfDirectedlyConnectedIntegerPartitions( int largestIntegerBeingSplit, int prev_largestIntegerBeingSplit )
	{
 		int counter = getNumOfDirectedlyConnectedIntegerPartitions( largestIntegerBeingSplit, prev_largestIntegerBeingSplit );
		if( counter == 0 ){
			return( null );
		}
		IntegerPartition[] listOfDirectlyConnectedIntegerPartitions = new IntegerPartition[ counter ];

 		if( sortedUnderlyingSet[0] == numOfAgents )
		{
			int index=0;
			for(int i=1; i<= (int)Math.floor( numOfAgents/(double)2); i++)
			{
				int[] newSortedParts = new int[2];
				newSortedParts[0] = i;
				newSortedParts[1] = numOfAgents-i;				
				listOfDirectlyConnectedIntegerPartitions[index] = new IntegerPartition( newSortedParts );
				listOfDirectlyConnectedIntegerPartitions[index].tempIntegersThatResultedFromASplit = new int[]{ i, numOfAgents-i };
				index++;
			}
		}
		else{
 			int index=0;
			for(int i=0; i< sortedUnderlyingSet.length; i++)
			{
				final int curPart = sortedUnderlyingSet[i];
				if(( curPart > largestIntegerBeingSplit )||( curPart <= prev_largestIntegerBeingSplit )){
					continue;
				}
 				for(int j=1; j<=(int)Math.floor( curPart/(double)2 ); j++)
				{
					int smallHalf = (int)j;
					int largeHalf = (int)(curPart - j);
					if( largeHalf > numOfAgents-smallHalf-largeHalf ){
						continue;
					}
					int[] newSortedParts = new int[ partsSortedAscendingly.length + 1 ];
					int i1=0;
					int i2=0;
					while( partsSortedAscendingly[i1] < smallHalf ){
						newSortedParts[i2] = partsSortedAscendingly[i1];
						i1++;
						i2++;
					}
					newSortedParts[i2] = smallHalf;
					i2++;
					while( partsSortedAscendingly[i1] < largeHalf ){
						newSortedParts[i2] = partsSortedAscendingly[i1];
						i1++;
						i2++;
					}
					newSortedParts[i2] = largeHalf;
					i2++;
					boolean curPartHasBeenSeen = false;
					while( i1 < partsSortedAscendingly.length ){
						if(( partsSortedAscendingly[i1] == curPart )&&( curPartHasBeenSeen == false )){
							curPartHasBeenSeen = true;
							i1++;
						}else{
							newSortedParts[i2] = partsSortedAscendingly[i1];
							i1++;
							i2++;
						}
					}
					listOfDirectlyConnectedIntegerPartitions[index] = new IntegerPartition( newSortedParts );
					listOfDirectlyConnectedIntegerPartitions[index].tempIntegersThatResultedFromASplit = new int[]{ smallHalf, largeHalf };
					index++;
				}
			}
		}
		return( listOfDirectlyConnectedIntegerPartitions );
	}
	
	 
	public static int getNumOfIntegerPartitions( int n )
	{
		int numOfIntegerPartitions = 0;
		for(int level=1; level<=n; level++)
		{
			numOfIntegerPartitions +=  IntegerPartition.getNumOfIntegerPartitionsInLevel( n, level );
		}
		return( numOfIntegerPartitions );
	}
	
	 
	public static int getNumOfIntegerPartitionsInLevel( int n, int level)
	{
		return( getNumOfIntegerPartitionsInLevel_additionalParameter( n, level, (int)(n-level+1) ));
	}
	private static int getNumOfIntegerPartitionsInLevel_additionalParameter( int n, int level, int M )
	{
		if(( level==1 )||( level==n ))
		{
			return(1);
		}		
		int sum=0;
		for(int M1=(int)Math.ceil( n/(double)level ); M1<=Math.min(n-level+1,M); M1++)
		{
			sum += getNumOfIntegerPartitionsInLevel_additionalParameter( (int)(n-M1), (int)(level-1), M1 );
		}
		return(sum);
	}
 
	public static int getNumOfIntegerPartitionsInLevel( int n, int level, int smallestPart )
	{
		return( getNumOfIntegerPartitionsInLevel_additionalParameter( n, level, smallestPart, (int)(n-level+1) ));
	}
	private static int getNumOfIntegerPartitionsInLevel_additionalParameter( int n,int level,int smallestPart,int M )
	{
		if( smallestPart==1 ) {
			return( getNumOfIntegerPartitionsInLevel( n, level ) );
		}
		else {			
			if(( n<smallestPart )||( level==n )){
				return(0);
			}
			if( level==1 ) {
				return(1);
			}		
			int sum=0;
			for(int M1=(int)Math.ceil( n/(double)level ); M1<=Math.min(n-level+1,M); M1++)
			{
				sum += getNumOfIntegerPartitionsInLevel_additionalParameter((int)(n-M1),(int)(level-1),smallestPart,M1);
			}
			return(sum);
		}
	}


	 
    public static int[][] getIntegerPartitionsOrderedLexicographically( int n, boolean orderIntegerPartitionsAscendingly )
    {
    	int index=0;
    	
     	int[][] listOfIntegerPartitions = new int[ IntegerPartition.getNumOfIntegerPartitions(n) ][];
    	
     	int[] indexOfNewPartition = new int[n];
    	for(int i=0; i<n; i++)
    		indexOfNewPartition[i]=0;
    	
    	 
    	int[] x = new int[n+1];
    	
     	for(int i=1; i<=n; i++) x[i]=1;

     	x[1]=n; int m=1; int h=1;

     	listOfIntegerPartitions[ index ] = new int[x.length];
    	for(int i=0; i<x.length; i++)
    		listOfIntegerPartitions[ index ][i] = x[i];
    	index++;
    	
     	while( x[1]!=1 )
    	{
    		if( x[h]==2 )
    		{
    			m=m+1; x[h]=1; h=h-1;     			
    		}
    		else
    		{
    			int r=x[h]-1; int t=m-h+1; x[h]=r;
    			while( t>=r ){ h=h+1; x[h]=r; t=t-r; }
    			if( t==0 ){ m=h; }
    			else
    			{ 
    				m=h+1;
    				if( t>1 ){ h=h+1; x[h]=t; }
    			}
    		}
     		listOfIntegerPartitions[ index ] = new int[x.length];
    		for(int i=0; i<x.length; i++)
    			listOfIntegerPartitions[ index ][i] = x[i];
        	index++;   	
    	}
    	return( listOfIntegerPartitions );
    }
	
	 
    public static int[][][] getIntegerPartitions( int n, boolean orderIntegerPartitionsAscendingly )
    {
     	int[][][] integerPartitions = allocateMemoryForIntegerPartitions(n);
    	
     	int[] indexOfNewPartition = new int[n];
    	for(int i=0; i<n; i++)
    		indexOfNewPartition[i]=0;
    	
    	 
    	int[] x = new int[n+1];
    	
     	for(int i=1; i<=n; i++) x[i]=1;

     	x[1]=n; int m=1; int h=1; fill_x_in_partitions( x, integerPartitions, m, orderIntegerPartitionsAscendingly, indexOfNewPartition);
    	
     	while( x[1]!=1 )
    	{
    		if( x[h]==2 )
    		{
    			m=m+1; x[h]=1; h=h-1;     			
    		}
    		else
    		{
    			int r=x[h]-1; int t=m-h+1; x[h]=r;
    			while( t>=r ){ h=h+1; x[h]=r; t=t-r; }
    			if( t==0 ){ m=h; }
    			else
    			{ 
    				m=h+1;
    				if( t>1 ){ h=h+1; x[h]=t; }
    			}
    		}
     		fill_x_in_partitions( x, integerPartitions, m, orderIntegerPartitionsAscendingly, indexOfNewPartition );   	
    	}
    	return( integerPartitions );
    }

    
    
    public static int[][][] getRestrictedIntegerPartitions( int m,int l1,boolean ascending )
    {

        int[][][] integerPartitions = allocateMemoryForIntegerPartitions( (int)m, (int)l1 );
    	


        int[] indexOfNewPartition = new int[m];
    	for(int i=0; i<integerPartitions.length; i++)
    		indexOfNewPartition[i]=0;
    	

        int original_m = m;
   	
    	integerPartitions[0][0][0] = (int)m;
    	for(int level=2; level<=integerPartitions.length; level++)
    	{
    		m = original_m; 
                
    		int n=level; 
                
    		int l2 = m; 
                
    		int z=0;
    		
    		
                

    		int i,j; int[] x=new int[n+1]; int[] y=new int[n+1];
    		j=z*n*(n-1); m=m-n*l1-j/2; l2=l2-l1;
    		if(( m>=0 )&( m<=n*l2-j ))
    		{
    			for(i=1; i<=n; i++)
    			{
    				x[i]=l1+z*(n-i); y[i]=l1+z*(n-i);
    			}
    			i=1; l2=l2-z*(n-1);

    			boolean carryOn=true;
    			while( carryOn )
    			{
    				carryOn=false;
    				while( m>l2 )
    				{
    					m=m-l2; x[i]=y[i]+l2;
    					i=i+1;
    				}
    				x[i]=y[i]+m; fill_x_in_partitions( x, integerPartitions, n, ascending, indexOfNewPartition );
    				if(( i<n )&( m>1 ))
    				{
    					m=1; x[i]=x[i]-1;
    					i=i+1; x[i]=y[i]+1;
    					fill_x_in_partitions( x, integerPartitions, n, ascending, indexOfNewPartition );
    				}
    				for( j=i-1; j>=1; j-- )
    				{
    					l2=x[j]-y[j]-1; m=m+1;
    					if( m<=(n-j)*l2 )
    					{
    						x[j]=y[j]+l2;
    						carryOn=true; break;
    					}
    					m=m+l2; x[i]=y[i]; i=j;    				
    				}
    			}    		
    		}
    	}
    	return( integerPartitions );
    }
    
    
    
    private static int[][][] allocateMemoryForIntegerPartitions( int n )
	{

            int[] numOfIntegerPartitionsInLevel = new int[n];
		for(int level=1; level<=n; level++)
			numOfIntegerPartitionsInLevel[level-1] = getNumOfIntegerPartitionsInLevel( n, level );
		
    	int[][][] integerPartitions = new int[n][][];		
		for(int level=1; level<=n; level++)
		{
			integerPartitions[level-1] = new int[ numOfIntegerPartitionsInLevel[level-1] ][];
			for(int i=0; i<numOfIntegerPartitionsInLevel[level-1]; i++)
			{
				integerPartitions[level-1][i] = new int[ level ];    			
			}
		}
		return( integerPartitions );
	}
    
	
    
    private static int[][][] allocateMemoryForIntegerPartitions( int n, int smallestPart )
	{

            int numOfNonEmptyLevels=0;
    	int[] numOfIntegerPartitions = new int[n];
		for(int level=1; level<=n; level++)
		{
			numOfIntegerPartitions[level-1] = getNumOfIntegerPartitionsInLevel( n, level, smallestPart );
			if( numOfIntegerPartitions[level-1]>0 )
			{
				numOfNonEmptyLevels++;
			}
		}
    		
		int[][][] integerPartitions = new int[numOfNonEmptyLevels][][];		
		for(int level=1; level<=numOfNonEmptyLevels; level++)
		{
			integerPartitions[level-1] = new int[ numOfIntegerPartitions[level-1] ][];
			for(int i=0; i<numOfIntegerPartitions[level-1]; i++)
			{
				integerPartitions[level-1][i] = new int[ level ];    			
			}
		}
		return( integerPartitions );
	}
    
	
    
    private static void fill_x_in_partitions( int x[], int[][][] integerPartitions, int m, boolean ascending, int[] indexOfNewPartition )
    {


        if( ascending==false )
    	{
    		for(int i=1; i<=m; i++)
    			integerPartitions[ m-1 ][ indexOfNewPartition[m-1] ][ i-1 ] = (int)x[i];
    		indexOfNewPartition[m-1]++;
    	}
    	else 

        {
    		for(int i=1; i<=m; i++)
    			integerPartitions[ m-1 ][ indexOfNewPartition[m-1] ][ i-1 ] = (int)x[m-i+1];
    		indexOfNewPartition[m-1]++;    		
    	}
    }
    
    
    
    public static int[] sortListOfIntegerPartitionsLexicographically( int[][] listOfIntegerPartitions, boolean eachIntegerPartitionIsOrderedAscendingly )
    {
    	int[] sortedIndices = new int[ listOfIntegerPartitions.length ];
    	
    	return( sortedIndices );
    }
    
    
    
    public boolean contains( ElementOfMultiset[] multiset )
    {
    	if( sortedMultiset.length < multiset.length ) return( false );
    	
    	for( int i=0; i<multiset.length; i++ ){
    		boolean found = false;
    		for( int j=0; j<this.sortedMultiset.length; j++ ){
    			if( this.sortedMultiset[j].element == multiset[i].element ){
    				if( this.sortedMultiset[j].repetition < multiset[i].repetition ){
    					return( false );
    				}
    				found = true;
    				break;
    			}
    		}
    		if( found == false ){
    			return( false );
    		}
    	}
   		return(true);
    }
}