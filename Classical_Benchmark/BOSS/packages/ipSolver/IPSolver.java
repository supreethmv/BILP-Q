package ipSolver;
import java.io.BufferedReader;
import java.io.FileReader;
import general.*;
import inputOutput.*;
import java.util.ArrayList;
import java.util.HashMap;
import mainSolver.*;
public class IPSolver
{
	public void solve( Input input, Output output, Result result )
	{
 		int numOfAgents=input.numOfAgents;
		result.initializeIPResults();
 		searchFirstAndLastLevel( input, result );
		 double[] avgValueForEachSize = new double[numOfAgents];
		MethodsForScanningTheInput.scanTheInputAndComputeAverage( input, output, result, avgValueForEachSize );
		 double[][] maxValueForEachSize = setMaxValueForEachSize( input );
 		Subspace[][] subspaces = initSubspaces(avgValueForEachSize,maxValueForEachSize,input);
		if( input.solverName == SolverNames.ODPIP ){
			result.ipIntegerPartitionGraph = new IntegerPartitionGraph( subspaces, input.numOfAgents, 1 );
		}
		if(( input.solverName == SolverNames.IP )||( input.solverName == SolverNames.ODPinParallelWithIP )){
			result.ipIntegerPartitionGraph = new IntegerPartitionGraph( subspaces, input.numOfAgents, ((int)Math.floor(2*input.numOfAgents/(double)3)));
		}
		result.totalNumOfExpansions = computeTotalNumOfExpansions( result );
		 long numOfRemainingSubspaces = IntegerPartition.getNumOfIntegerPartitions( numOfAgents );
		 numOfRemainingSubspaces -= disableSubspacesThatWereSearchedWhileScanningTheInput( result, input );
 		setUpperAndLowerBoundsOnOptimalValue( input, result );
 		 numOfRemainingSubspaces -= disableSubspacesWithUBLowerThanTheHighestLB( input, output, result );
		output.printDetailsOfSubspaces( input, result );
 		if( numOfRemainingSubspaces==0 ){
			finalize( result, input, output );
			return;
		}
 		result.idpSolver_whenRunning_ODPIP = null;
		if( input.solverName == SolverNames.ODPIP ){
			result.idpSolver_whenRunning_ODPIP = new IDPSolver_whenRunning_ODPIP( input, result );
			result.idpSolver_whenRunning_ODPIP.initValueOfBestPartitionFound( input, maxValueForEachSize );
			result.idpSolver_whenRunning_ODPIP.start();
		}
 		result.odpSolver_runningInParallelWithIP = null;
		if( input.solverName == SolverNames.ODPinParallelWithIP ){
			result.odpSolver_runningInParallelWithIP = new ODPSolver_runningInParallelWithIP( input, result );
			result.odpSolver_runningInParallelWithIP.initValueOfBestPartitionFound( input, maxValueForEachSize );
			result.odpSolver_runningInParallelWithIP.start();
		}
 		double acceptableValue = result.ipUpperBoundOnOptimalValue*input.acceptableRatio/100;
		if( input.ipPruneSubspaces == false ) acceptableValue = Double.MAX_VALUE;
 		Node[] sortedNodes = getListOfSortedNodes( subspaces, input, result );
 		while( ((double)result.ipLowerBoundOnOptimalValue ) < acceptableValue )
		{
 			if( input.solverName == SolverNames.ODPIP ) result.ipIntegerPartitionGraph.updateEdges( input.numOfAgents, result.get_dpMaxSizeThatWasComputedSoFar() );
 			if( input.solverName == SolverNames.ODPIP ) numOfRemainingSubspaces -= disableSubspacesReachableFromBottomNode( input, result );
 			Node nodeToBeSearched = getFirstEnabledNode( sortedNodes );
			if( nodeToBeSearched == null ) break;
                     // System.out.println("Subspace to be searched before shrinking="+General.convertArrayToString(nodeToBeSearched.subspace.integers));
                         Node Shortnode=getnodes(  subspaces,  input,  result,   nodeToBeSearched ); 
                         if(Shortnode!=null)
                        {
                            nodeToBeSearched=Shortnode;
                        }
                        //System.out.println("Subspace to be searched after shrinking="+General.convertArrayToString(nodeToBeSearched.subspace.integers));
                         ElementOfMultiset[] subsetToBePutAtTheBeginning = getRelevantNodes( nodeToBeSearched, input, result, 1);
 			putSubsetAtTheBeginning( nodeToBeSearched, subsetToBePutAtTheBeginning, input );
 			double[] sumOfMax = computeSumOfMax_splitOneInteger( nodeToBeSearched, maxValueForEachSize, input, result );
 			int numOfIntegersToSplit = 0;
			if(( input.solverName == SolverNames.ODPIP )&&( subsetToBePutAtTheBeginning != null )&&( input.odpipSearchesMultipleSubspacesSimultaneiously ))
				numOfIntegersToSplit = nodeToBeSearched.subspace.integers.length - General.getCardinalityOfMultiset(subsetToBePutAtTheBeginning);
			numOfRemainingSubspaces -= nodeToBeSearched.subspace.search( input,output,result,acceptableValue,avgValueForEachSize,sumOfMax,numOfIntegersToSplit);
 			setUpperAndLowerBoundsOnOptimalValue( input, result );
			acceptableValue = result.ipUpperBoundOnOptimalValue*input.acceptableRatio/100;
			if( input.ipPruneSubspaces == false ) acceptableValue = Double.MAX_VALUE;
 			if( result.ipLowerBoundOnOptimalValue < result.get_ipValueOfBestCSFound() ){ 
				result.ipLowerBoundOnOptimalValue = result.get_ipValueOfBestCSFound() ;
				numOfRemainingSubspaces -= disableSubspacesWithUBLowerThanTheHighestLB( input, output, result );
			}			
 			if( numOfRemainingSubspaces==0 )break;
			if( ((input.solverName == SolverNames.ODPIP)||(input.solverName == SolverNames.ODPinParallelWithIP)) && (result.get_dpHasFinished()) ){			
				break;
			}
		}
   		finalize( result, input, output );
	}
        private Node getnodes( Subspace[][] subspaces, Input input, Result result, Node Nodetobesearched )
	{
		Node[][] nodes = result.ipIntegerPartitionGraph.nodes;
		ArrayList<Node> Plist=new ArrayList<Node>();
                ArrayList<Node> Plisttemp=new ArrayList<Node>();
                boolean Flag1=false;
                int lengthnode=Nodetobesearched.subspace.integers.length-1;
 		Node[] sortedNodes = new Node[ IntegerPartition.getNumOfIntegerPartitions( input.numOfAgents ) ];
		int k=0;
		for(int level=2; level<lengthnode; level++){  
                 for(int i=0; i<nodes[level].length; i++){  
                             if(!(nodes[level][i].subspace.enabled)) continue;
                             int[]F={0};
                            getRelevantNodesandcheck( nodes[level][i],  input,  result, 1,  Nodetobesearched,   F );
                             if(F[0]==1) {
                                 return(nodes[level][i]);                                
                            }
                        }
		}
	return(null);	 
	}
        private ElementOfMultiset[] getRelevantNodesandcheck( Node node, Input input, Result result, int numOfIntegersToSplitAtTheEnd, Node Nodetobesearched, int[]F )
	{
		if(( input.solverName == SolverNames.IP )||( input.solverName == SolverNames.ODPinParallelWithIP )||( input.odpipSearchesMultipleSubspacesSimultaneiously == false )){
			node.subspace.relevantNodes = null;
			return(null);
		}
		int numOfIntegersInNode = node.integerPartition.partsSortedAscendingly.length;
 		Node[] reachableNodes = result.ipIntegerPartitionGraph.getReachableNodes( node );
		if( reachableNodes == null ){
			node.subspace.relevantNodes = null;
			return(null);
		}		
 		SubsetsOfMultiset[] subsetIterators = new SubsetsOfMultiset[ numOfIntegersToSplitAtTheEnd ];
		for(int s=0; s<numOfIntegersToSplitAtTheEnd; s++)
			subsetIterators[s] = new SubsetsOfMultiset( node.integerPartition.sortedMultiset, numOfIntegersInNode - (s+1), false);
 		ElementOfMultiset[] bestSubset = null;
		long bestSavings =0;
		int bestNumOfRelevantNodes=0;
		for(int s=0; s<numOfIntegersToSplitAtTheEnd; s++)
		{
 			ElementOfMultiset[] subset = subsetIterators[s].getNextSubset();
			while(subset != null)
			{
				long savings = 0;
				int numOfRelevantSubspaces = 0;
 				for(int i=0; i<reachableNodes.length; i++) {
					if(( reachableNodes[i].integerPartition.contains( subset ) )&&( reachableNodes[i].subspace.enabled )){
						savings += reachableNodes[i].subspace.totalNumOfExpansionsInSubspace;
						numOfRelevantSubspaces++;
					}
				}
 				if( bestSavings < savings ){
					bestSavings = savings;
					bestSubset  = General.copyMultiset( subset );
					bestNumOfRelevantNodes = numOfRelevantSubspaces;
				}
 				subset = subsetIterators[s].getNextSubset();
			}
		}
 		int index=0;
		if( bestNumOfRelevantNodes == 0  ){
			node.subspace.relevantNodes = null;
			return(null);
		}else{
			node.subspace.relevantNodes = new Node[ bestNumOfRelevantNodes ];
			for(int i=0; i<reachableNodes.length; i++) {
				if(( reachableNodes[i].integerPartition.contains( bestSubset ) )&&( reachableNodes[i].subspace.enabled )){
 					node.subspace.relevantNodes[ index ] = reachableNodes[i];
                                        if(Nodetobesearched==reachableNodes[i])
                                        {
                                             F[0]=1;
                                             break;
                                        }
					index++;
				}
			}
			return( bestSubset );
		}
	}
	 private long disableSubspacesReachableFromBottomNode( Input input, Result result )
	{
		Node bottomNode = result.ipIntegerPartitionGraph.nodes[0][0];
 		Node[] reachableNodes = result.ipIntegerPartitionGraph.getReachableNodes( bottomNode );
 		int numOfDisabledNodes=0;
		if( reachableNodes != null )
			for(int i=0; i<reachableNodes.length; i++)
				if( reachableNodes[i].subspace.enabled ){
					reachableNodes[i].subspace.enabled = false;
 					numOfDisabledNodes++;
				}
		return( numOfDisabledNodes );
	}
	 private void finalize( Result result, Input input, Output output )
	{
		if(( input.solverName == SolverNames.ODPIP )||( input.solverName == SolverNames.ODPinParallelWithIP )){
			if( result.get_dpBestCSFound() != null ){
				result.updateIPSolution( result.get_dpBestCSFound(), result.get_dpValueOfBestCSFound());
			}
			if( result.idpSolver_whenRunning_ODPIP != null ){
				result.idpSolver_whenRunning_ODPIP.setStop(true);
			}
			if( result.odpSolver_runningInParallelWithIP != null ){
				result.odpSolver_runningInParallelWithIP.setStop(true);
			}
		}
		result.ipTime = System.currentTimeMillis() - result.ipStartTime;
		output.emptyStringBufferContentsIntoOutputFiles(input);
		output.printFinalResultsOfIPToFiles( input, result );
	}
	 	private void searchFirstAndLastLevel( Input input , Result result )
	{
		int numOfAgents = input.numOfAgents;
		boolean CS1IsFeasible = true;
		boolean CS2IsFeasible = true;
 		int[][] CS1 = new int[numOfAgents][1];
		for(int i=0; i<=numOfAgents-1; i++)	
			CS1[i][0] = (int)(i+1);
 		int[][] CS2 = new int[1][numOfAgents];
		for(int i=0; i<=numOfAgents-1; i++)
			CS2[0][i] = (int)(i+1);
		if( input.feasibleCoalitions != null ){	
 			for(int i=0; i<CS1.length; i++){
				int curCoalitionInBitFormat = Combinations.convertCombinationFromByteToBitFormat( CS1[i] );
				if( input.feasibleCoalitions.contains( curCoalitionInBitFormat ) == false ){
					CS1IsFeasible = false;
					break;
				}
			}		
 			for(int i=0; i<CS2.length; i++){
				int curCoalitionInBitFormat = Combinations.convertCombinationFromByteToBitFormat( CS2[i] );
				if( input.feasibleCoalitions.contains( curCoalitionInBitFormat ) == false ){
					CS2IsFeasible = false;
					break;
				}
			}
		}
 		double valueOfCS1 = input.getCoalitionStructureValue( CS1 );
		double valueOfCS2 = input.getCoalitionStructureValue( CS2 );
 		if(( (CS1IsFeasible)&&(CS2IsFeasible == false) )||( (CS1IsFeasible)&&(CS2IsFeasible)&&(valueOfCS1>=valueOfCS2) )) {
			result.updateIPSolution( CS1, valueOfCS1 );
		}
		if(( (CS2IsFeasible)&&(CS1IsFeasible == false) )||( (CS2IsFeasible)&&(CS1IsFeasible)&&(valueOfCS2>=valueOfCS1) )) {
			result.updateIPSolution( CS2, valueOfCS2 );
		}
	}
                private Subspace[][] initSubspaces(double[] avgValueForEachSize,double[][] maxValueForEachSize,Input input)
	{
     	int[][][] integers=IntegerPartition.getIntegerPartitions(input.numOfAgents,input.orderIntegerPartitionsAscendingly);
     	Subspace[][] subspaces = new Subspace[ integers.length ][];
		for(int level=0; level<input.numOfAgents; level++)
		{
			subspaces[level]=new Subspace[ integers[level].length ];
			for(int i=0; i<integers[level].length; i++){
				subspaces[level][i] = new Subspace( integers[level][i], avgValueForEachSize, maxValueForEachSize, input );
			}
		}
		return( subspaces );
	}
	 private long disableSubspacesThatWereSearchedWhileScanningTheInput( Result result, Input input )	
	{
		Node[][] nodes = result.ipIntegerPartitionGraph.nodes;
		long numOfSubspacesThatThisMethodHasDisabled = 0;
		for(int level=0; level<nodes.length; level++) {
			for(int i=0; i<nodes[level].length; i++)
			{
				Subspace curSubspace = nodes[level][i].subspace;
 				if(( level==0 )||( level==1 )||( level==(input.numOfAgents-1) )) {
					if( curSubspace.enabled ) {
						curSubspace.enabled = false;
						numOfSubspacesThatThisMethodHasDisabled++;
					}
				}
			}
		}
		return( numOfSubspacesThatThisMethodHasDisabled );
	}
         private double[][] setMaxValueForEachSize( Input input )
	{
 		int numOfAgents = input.numOfAgents;
		int[] numOfRequiredMaxValues = new int[numOfAgents];
		long[] numOfCoalitions = new long[numOfAgents];
		double[][] maxValue = new double[numOfAgents][];		
		for(int size=1; size<=numOfAgents; size++)
		{
			numOfRequiredMaxValues[size-1] = (int)Math.floor( numOfAgents/(double)size );
			numOfCoalitions[size-1] = Combinations.binomialCoefficient( numOfAgents, size );
			maxValue[ size-1 ] = new double[ numOfRequiredMaxValues[size-1] ];
			for(int i=0; i<maxValue[size-1].length; i++)
				maxValue[ size-1 ][i] = 0;
		}	
		final boolean constraintsExist;
		if( input.feasibleCoalitions == null )
			constraintsExist = false;
		else
			constraintsExist = true;
		for(int coalitionInBitFormat=(int)Math.pow(2,numOfAgents)-1; coalitionInBitFormat>0; coalitionInBitFormat--)
			if(( constraintsExist==false )||( input.feasibleCoalitions.contains( coalitionInBitFormat ) ))
 			{
				int size = (int)Combinations.getSizeOfCombinationInBitFormat( coalitionInBitFormat, numOfAgents);
				double[] curMaxValue = maxValue[ size-1 ];
				int j=numOfRequiredMaxValues[size-1]-1;
				if( input.getCoalitionValue( coalitionInBitFormat ) > curMaxValue[ j ] )
				{
					while(( j>0 )&&( input.getCoalitionValue( coalitionInBitFormat ) > curMaxValue[ j-1 ] ))
					{
						curMaxValue[ j ] = curMaxValue[ j-1 ];
						j--;
					}
					curMaxValue[ j ] = input.getCoalitionValue( coalitionInBitFormat );
				}
			}
		return(maxValue);
	}
	 	private long disableSubspacesWithUBLowerThanTheHighestLB( Input input,Output output,Result result )
	{
		Node[][] nodes = result.ipIntegerPartitionGraph.nodes;
		if( input.ipPruneSubspaces == false ) return(0);
		long numOfSubspacesThatThisMethodHasDisabled = 0;
		for(int level=0; level<nodes.length; level++)
		{
			for(int i=0; i<nodes[level].length; i++)
			{
				Subspace curSubspace = nodes[level][i].subspace; 
				if( curSubspace.enabled == true )
				{
 					if( curSubspace.UB - result.ipLowerBoundOnOptimalValue <-0.00000000005 )
					{
						curSubspace.enabled = false;
						numOfSubspacesThatThisMethodHasDisabled += 1;
					}
				}
			}
		}		
		output.printCurrentResultsOfIPToStringBuffer_ifPrevResultsAreDifferent( input, result );
		return( numOfSubspacesThatThisMethodHasDisabled );
	}
	 	private Node[] getListOfSortedNodes( Subspace[][] subspaces, Input input, Result result )
	{
            Node[][] nodes = result.ipIntegerPartitionGraph.nodes;
             ArrayList<Node> All_Nodes= new ArrayList<Node>();
            for(int level=0; level<nodes.length; level++){  
			for(int i=0; i<nodes[level].length; i++)  
			{
				Subspace curSubspace = nodes[level][i].subspace;
				curSubspace.priority = curSubspace.UB;
                                All_Nodes.add(nodes[level][i]);  
                        }
            }
		     int Last_level_IDP=(int)(Math.floor((2*input.numOfAgents)/(double)3));
                HashMap<Integer,ArrayList<Node>> Dk = new HashMap<Integer,ArrayList<Node>>();           
                int middlesize=(int)Math.ceil(input.numOfAgents/(double)2);
                for (int IDP_Level=1;IDP_Level<=Last_level_IDP;IDP_Level++ ){

                     ArrayList<Node> temp_space= new ArrayList<Node>(); 
                    for (Node Nodes : All_Nodes){
                                int[] Subspaceasintegers=Nodes.subspace.integers;                              
                                if((Subspaceasintegers[0]== input.numOfAgents-IDP_Level) &&(IDP_Level<middlesize)){
                                  temp_space.add(Nodes);
                                 }
                                else if((isSubsetSum(Subspaceasintegers,Subspaceasintegers.length,middlesize)) &&(IDP_Level==middlesize)){
                                        temp_space.add(Nodes);  
                                    }
                                else if( (isSubsetSum(Subspaceasintegers,Subspaceasintegers.length,IDP_Level)) &&(IDP_Level>middlesize)){
                                        temp_space.add(Nodes);  
                                }
                }
                 Dk.put(IDP_Level,temp_space);
                  for (Node T : temp_space) {
                      All_Nodes.remove(T);
              

                  
                  
                  }              
                 }
             Node[] sortedNodes =  new Node[ IntegerPartition.getNumOfIntegerPartitions( input.numOfAgents ) -1];
              int m=0;
               for (int counter =Last_level_IDP; counter>=1; counter--) {   

                   Node[] Tempnodes = new Node[Dk.get(counter).size()];
                   int k=0;
                   for (Node nodeToBeSearched : Dk.get(counter)) {
                        Tempnodes[k]=nodeToBeSearched; 
                        k=k+1;
                   }
               for(int i=k-1; i>=0; i--)	 
		{
			int indexOfSmallestElement = i;	
 			for(int j=i; j>=0; j--)	        
 			{
 				if((Tempnodes[j].subspace.priority < Tempnodes[indexOfSmallestElement].subspace.priority )
						||( (Tempnodes[j].subspace.priority == Tempnodes[indexOfSmallestElement].subspace.priority)
								&&( Tempnodes[j].subspace.UB <Tempnodes[indexOfSmallestElement].subspace.UB ) ))
					indexOfSmallestElement = j;	
 			}
 			Node temp = Tempnodes[i];
			Tempnodes[i] = Tempnodes[indexOfSmallestElement];
			Tempnodes[indexOfSmallestElement] = temp;
		}
               for(int l=0; l<k; l++){
               sortedNodes[m]=Tempnodes[l];
               m=m+1;
               }
                }
	return( sortedNodes );
	}
     private static boolean isSubsetSum(int set[], int n, int sum)
    {
        boolean subset[][] = new boolean[sum+1][n+1];
         for (int i = 0; i <= n; i++)
          subset[0][i] = true;
         for (int i = 1; i <= sum; i++)
          subset[i][0] = false;
          for (int i = 1; i <= sum; i++)
         {
           for (int j = 1; j <= n; j++)
           {
             subset[i][j] = subset[i][j-1];
             if (i >= set[j-1])
               subset[i][j] = subset[i][j] || 
                                          subset[i - set[j-1]][j-1];
           }
         }
         return subset[sum][n];
    }      
	private Node getFirstEnabledNode( Node[] sortedNodes )
	{
		for(int i=0; i<sortedNodes.length; i++)
		{
			if( sortedNodes[i].subspace.enabled ) return( sortedNodes[i] );
		}
		return(null);
	}
        private void setUpperAndLowerBoundsOnOptimalValue( Input input, Result result )
	{
		result.ipUpperBoundOnOptimalValue = result.get_ipValueOfBestCSFound();
		result.ipLowerBoundOnOptimalValue = result.get_ipValueOfBestCSFound();
		Node[][] nodes = result.ipIntegerPartitionGraph.nodes;
		for(int level=0; level<nodes.length; level++){
			for(int i=0; i<nodes[level].length; i++)
			{
				Subspace curSubspace = nodes[level][i].subspace;
				if( curSubspace.enabled )
				{
					if( result.ipUpperBoundOnOptimalValue < curSubspace.UB )
						result.ipUpperBoundOnOptimalValue = curSubspace.UB;
					if( result.ipLowerBoundOnOptimalValue < curSubspace.LB )
						result.ipLowerBoundOnOptimalValue = curSubspace.LB;
				}
			}
		}
	}
        private double[] computeSumOfMax_splitNoIntegers( int[] integers, double[][] maxValueForEachSize )
	{
            double[] sumOfMax = new double[ integers.length + 1 ];
            sumOfMax[ integers.length ] = maxValueForEachSize[ integers[integers.length-1]-1 ][0];
            int j=0;
		for( int i=integers.length-1; i>0; i-- )
		{
			if( integers[i-1] == integers[i] )
				j++;
			else
				j=0;					
			sumOfMax[ i ] = sumOfMax[ i+1 ] + maxValueForEachSize[ integers[i-1]-1 ][ j ];
		}
		return( sumOfMax );
	}
        private double[] computeSumOfMax_usefInsteadOfv( int[] integers, double[][] maxValueForEachSize, Result result )
	{
 		double[] sumOfMax = new double[ integers.length + 1 ];
 		sumOfMax[ integers.length ] = result.get_max_f( integers[integers.length-1]-1 );
		for( int i=integers.length-1; i>0; i-- )
			sumOfMax[ i ] = sumOfMax[ i+1 ] + result.get_max_f( integers[i-1]-1 );
		return( sumOfMax );
	}
	private double computeUpperBound( ElementOfMultiset[] integers, double[][] maxValueForEachSize )
	{
		double upperBound = 0;
		for(int i=0; i<integers.length; i++)
		{
			int j=0;
			for(int k=0; k<integers[i].repetition; k++){
				upperBound += maxValueForEachSize[ integers[i].element - 1 ][j];
				j++;
			}
		}
		return( upperBound );
	}
	private double[] computeSumOfMax_splitManyIntegers( Node node, double[][] maxValueForEachSize, Input input, ElementOfMultiset[] subsetToBePutAtTheBeginning, Result result )
	{
		if(( input.solverName == SolverNames.IP )||( input.solverName == SolverNames.ODPinParallelWithIP )||( input.odpipSearchesMultipleSubspacesSimultaneiously == false )||( node.subspace.relevantNodes == null ))
			return( computeSumOfMax_splitNoIntegers( node.subspace.integers, maxValueForEachSize ) );
 		int[] integers = node.subspace.integers;
		Node[] relevantNodes = node.subspace.relevantNodes;
		double[] sumOfMax = new double[ integers.length + 1 ];
		int numOfIntegersToBePutAtTheBeginning = 0;
		if( subsetToBePutAtTheBeginning != null )
			for(int i=0; i<subsetToBePutAtTheBeginning.length; i++)
				numOfIntegersToBePutAtTheBeginning += subsetToBePutAtTheBeginning[i].repetition;
 		int[][] tempIntegers = new int[ relevantNodes.length + 1 ][];
		int[][] tempIntegerRoots = new int[ relevantNodes.length + 1 ][];
		tempIntegers[0] = General.copyArray( node.integerPartition.partsSortedAscendingly );
		tempIntegerRoots[0] = General.copyArray( node.tempIntegerRoots );
		for(int i=1; i<tempIntegers.length; i++){
			tempIntegers[i] = General.copyArray( relevantNodes[i-1].integerPartition.partsSortedAscendingly );
			tempIntegerRoots[i] = General.copyArray( relevantNodes[i-1].tempIntegerRoots );
		}
 		double maxUB = node.subspace.UB;
		for(int i=0; i< relevantNodes.length; i++){
			if( maxUB < relevantNodes[i].subspace.UB ){
				maxUB = relevantNodes[i].subspace.UB ;
			}
		}
		sumOfMax[1] = maxUB;
		int index = 1;
 		while( index < integers.length )
		{
			sumOfMax[ index+1 ] = Integer.MIN_VALUE;
			int curInteger = integers[ index-1 ];
			for(int i=0; i<tempIntegers.length; i++)
			{
				ElementOfMultiset[] set = null;
				ElementOfMultiset[] subset = null;
				if( index <= numOfIntegersToBePutAtTheBeginning ){
					subset = new ElementOfMultiset[1];
					subset[0] = new ElementOfMultiset( curInteger, 1);
				}else{
					set = getSetOfIntegersGivenTheirRoot( tempIntegers[i], tempIntegerRoots[i], curInteger );
					subset = getSubsetOfIntegersGivenTheirSum( set, curInteger );
					if( subset == null ){
						subset = new ElementOfMultiset[1];
						subset[0] = new ElementOfMultiset( curInteger, 1);
					}
				}
				if( i==0 ){
					removeSubset( node, subset, tempIntegers, tempIntegerRoots, i );
				}else{
					removeSubset( relevantNodes[i-1], subset, tempIntegers, tempIntegerRoots, i );
				}
				double[] tempSumOfMax = computeSumOfMax_splitNoIntegers( tempIntegers[i], maxValueForEachSize );
				if( sumOfMax[ index+1 ] < tempSumOfMax[1] )
					sumOfMax[ index+1 ] = tempSumOfMax[1] ;
			}
 			double[] sumOfMaxUsingf = computeSumOfMax_usefInsteadOfv( tempIntegers[0], maxValueForEachSize, result);
			if( sumOfMax[ index+1 ] > sumOfMaxUsingf[1] )
				sumOfMax[ index+1 ] = sumOfMaxUsingf[1] ;
			index++;
		}
		return( sumOfMax );
	}
	private double[] computeSumOfMax_splitOneInteger( Node node, double[][] maxValueForEachSize, Input input, Result result )
	{
		if(( input.solverName == SolverNames.IP )||( input.solverName == SolverNames.ODPinParallelWithIP )||( input.odpipSearchesMultipleSubspacesSimultaneiously == false )||( node.subspace.relevantNodes == null ))
			return( computeSumOfMax_splitNoIntegers( node.subspace.integers, maxValueForEachSize ) );
		int[] integers = node.subspace.integers;
		double[] sumOfMax = new double[ integers.length + 1 ];
 		double maxUB = node.subspace.UB;
		for(int i=0; i<node.subspace.relevantNodes.length; i++)
			if( maxUB < node.subspace.relevantNodes[i].subspace.UB )
				maxUB = node.subspace.relevantNodes[i].subspace.UB ;
 		sumOfMax[ integers.length ] = maxValueForEachSize[ integers[integers.length-1]-1 ][0] + (maxUB - node.subspace.UB);
		double max_f = result.get_max_f( integers[integers.length-1]-1 ); 
		if(( max_f != 0 )&&( sumOfMax[ integers.length ] > max_f ))
			sumOfMax[ integers.length ] = max_f ;
		sumOfMax[ integers.length-1 ] = sumOfMax[ integers.length ] + maxValueForEachSize[ integers[integers.length-2]-1 ][0];
		int k=2;
 		int x = integers.length-k;
		int j=0;
		for( int i=x; i>0; i-- )
		{
			if( integers[i-1] == integers[i] )
				j++;
			else
				j=0;					
			sumOfMax[ i ] = sumOfMax[ i+1 ] + maxValueForEachSize[ integers[i-1]-1 ][ j ];
			k++;
		}
		return( sumOfMax );
	}
	private double[] computeSumOfMax_splitOneInteger_improveUsingf( Node node, double[][] maxValueForEachSize, Input input, Result result )
	{
		if(( input.solverName == SolverNames.IP )||( input.solverName == SolverNames.ODPinParallelWithIP )||( input.odpipSearchesMultipleSubspacesSimultaneiously == false )||( node.subspace.relevantNodes == null ))			
			return( computeSumOfMax_splitNoIntegers( node.subspace.integers, maxValueForEachSize ) );
 		int[] integers = node.subspace.integers;
		double[] sumOfMax = new double[ integers.length + 1 ];
 		double[][] arrayOfMax = new double[ integers.length + 1 ][];
		for(int i=1; i<arrayOfMax.length; i++){
			arrayOfMax[i] = new double[ arrayOfMax.length - i ];			
		}
 		double maxUB = node.subspace.UB;
		for(int i=0; i<node.subspace.relevantNodes.length; i++)
			if( maxUB < node.subspace.relevantNodes[i].subspace.UB )
				maxUB = node.subspace.relevantNodes[i].subspace.UB ;
 		sumOfMax[ integers.length ] = maxValueForEachSize[ integers[integers.length-1]-1 ][0] + (maxUB - node.subspace.UB);
		double max_f = result.get_max_f( integers[integers.length-1]-1 );
		if(( max_f != 0 )&&( sumOfMax[ integers.length ] > max_f ))
			sumOfMax[ integers.length ] = max_f ;
		sumOfMax[ integers.length-1 ] = sumOfMax[ integers.length ] + maxValueForEachSize[ integers[integers.length-2]-1 ][0];
 		for(int i=1; i<arrayOfMax.length; i++)
			arrayOfMax[i][ arrayOfMax[i].length-1 ] = maxValueForEachSize[ integers[integers.length-1]-1 ][0] + (maxUB - node.subspace.UB);
		for(int i=1; i<arrayOfMax.length-1; i++)
			arrayOfMax[i][ arrayOfMax[i].length-2 ] = maxValueForEachSize[ integers[integers.length-2]-1 ][0];				
 		int k=2;
		int x = integers.length-k;
		int j=0;
		for( int i=x; i>0; i-- )
		{
			if( integers[i-1] == integers[i] )
				j++;
			else
				j=0;					
			sumOfMax[ i ] = sumOfMax[ i+1 ] + maxValueForEachSize[ integers[i-1]-1 ][ j ];
 			for(int w=1; w<arrayOfMax.length-k; w++)
				arrayOfMax[w][ arrayOfMax[w].length-(k+1) ] = maxValueForEachSize[ integers[i-1]-1 ][ j ];
			k++;
		}
 		if( input.solverName == SolverNames.ODPIP )
			improveUpperBoundUsingf( input.numOfAgents, sumOfMax, arrayOfMax, maxValueForEachSize, integers, result );
		return( sumOfMax );
	}
	private void improveUpperBoundUsingf( int numOfAgents, double[] sumOfMax, double[][] arrayOfMax, double[][] maxValueForEachSize, int[] integers, Result result )
	{
            for(int i=1; i<sumOfMax.length; i++)  
		{
                int start = i;
			double newSumOfMax = 0;
			int[] numOfTimesWeComputedASizeRegularly = new int[ numOfAgents+1 ];
			for(int size = 0; size <= numOfAgents; size++)
				numOfTimesWeComputedASizeRegularly[ size ]=0;
                        do{ 
                            int sumOfIntegers = 0;
				int bestSumOfIntegers = -1;
				double bestSavings = 0;
				int bestEnd = 0;
				boolean exitLoop = false;
                                for(int end = start; end < sumOfMax.length; end++)
				{	
					sumOfIntegers += integers[ end-1 ];
					if( sumOfIntegers > result.get_dpMaxSizeThatWasComputedSoFar() ){
						exitLoop = true;
						break;
					}
                                        double savings = -1 * result.get_max_f( sumOfIntegers-1 );
					for(int j=start; j<=end; j++)
						savings += arrayOfMax[i][ j-i ];
					if( bestSavings < savings ){
						bestSavings = savings ;
						bestSumOfIntegers = sumOfIntegers;
						bestEnd = end;
					}	
				}
                                if( bestSavings < 0.0000000001 ){
					int curSize = integers[start-1];
					newSumOfMax += maxValueForEachSize[ curSize-1 ][ numOfTimesWeComputedASizeRegularly[curSize] ];
					numOfTimesWeComputedASizeRegularly[ curSize ]++;
					start++;
				}else{
					newSumOfMax += result.get_max_f( bestSumOfIntegers-1 );
					start = bestEnd+1;
				}
			}
			while( start < sumOfMax.length );
			if( sumOfMax[i] > newSumOfMax )
				sumOfMax[i] = newSumOfMax ;
		}
	}
	private ElementOfMultiset[] getRelevantNodes( Node node, Input input, Result result, int numOfIntegersToSplitAtTheEnd )
	{
		if(( input.solverName == SolverNames.IP )||( input.solverName == SolverNames.ODPinParallelWithIP )||( input.odpipSearchesMultipleSubspacesSimultaneiously == false )){
			node.subspace.relevantNodes = null;
			return(null);
		}
		int numOfIntegersInNode = node.integerPartition.partsSortedAscendingly.length;
                Node[] reachableNodes = result.ipIntegerPartitionGraph.getReachableNodes( node );
		if( reachableNodes == null ){
			node.subspace.relevantNodes = null;
			return(null);
		}		
                SubsetsOfMultiset[] subsetIterators = new SubsetsOfMultiset[ numOfIntegersToSplitAtTheEnd ];
		for(int s=0; s<numOfIntegersToSplitAtTheEnd; s++)
			subsetIterators[s] = new SubsetsOfMultiset( node.integerPartition.sortedMultiset, numOfIntegersInNode - (s+1), false);
                ElementOfMultiset[] bestSubset = null;
		long bestSavings =0;
		int bestNumOfRelevantNodes=0;
		for(int s=0; s<numOfIntegersToSplitAtTheEnd; s++)
		{
                    ElementOfMultiset[] subset = subsetIterators[s].getNextSubset();
			while(subset != null)
			{
				long savings = 0;
				int numOfRelevantSubspaces = 0;
                                for(int i=0; i<reachableNodes.length; i++) {
					if(( reachableNodes[i].integerPartition.contains( subset ) )&&( reachableNodes[i].subspace.enabled )){
						savings += reachableNodes[i].subspace.totalNumOfExpansionsInSubspace;
						numOfRelevantSubspaces++;
					}
				}
                                if( bestSavings < savings ){
					bestSavings = savings;
					bestSubset  = General.copyMultiset( subset );
					bestNumOfRelevantNodes = numOfRelevantSubspaces;
				}
                                subset = subsetIterators[s].getNextSubset();
			}
		}
                int index=0;
		if( bestNumOfRelevantNodes == 0  ){
			node.subspace.relevantNodes = null;
			return(null);
		}else{
			node.subspace.relevantNodes = new Node[ bestNumOfRelevantNodes ];
			for(int i=0; i<reachableNodes.length; i++) {
				if(( reachableNodes[i].integerPartition.contains( bestSubset ) )&&( reachableNodes[i].subspace.enabled )){
					node.subspace.relevantNodes[ index ] = reachableNodes[i];
					index++;
				}
			}
			return( bestSubset );
		}
	}
	private ElementOfMultiset[] getRelevantNodes( Node node, Input input, Result result )
	{
		if(( input.solverName == SolverNames.IP )||( input.solverName == SolverNames.ODPinParallelWithIP )||( input.odpipSearchesMultipleSubspacesSimultaneiously == false )){
			node.subspace.relevantNodes = null;
			return(null);
		}
                Node[] reachableNodes = result.ipIntegerPartitionGraph.getReachableNodes( node );
		if( reachableNodes == null ){
			node.subspace.relevantNodes = null;
			return(null);
		}
                int numOfEnabledReachableNodes = 0;
		for(int j=0; j<reachableNodes.length; j++)
			if( reachableNodes[j].subspace.enabled )
				numOfEnabledReachableNodes++;
                if( numOfEnabledReachableNodes == 0){
			node.subspace.relevantNodes = null;
			return( null );
		}else{
			node.subspace.relevantNodes = new Node[ numOfEnabledReachableNodes ];
			int index=0;
			for(int j=0; j<reachableNodes.length; j++){
				if( reachableNodes[j].subspace.enabled ){
					node.subspace.relevantNodes[ index ] = reachableNodes[j];
					index++;
				}
			}
		}
		Node[] relevantNodes = node.subspace.relevantNodes;
                ElementOfMultiset[] copyOfMultiset = General.copyMultiset( node.integerPartition.sortedMultiset );
                for(int i=0; i<copyOfMultiset.length; i++)
		{			
                    int curElement = copyOfMultiset[i].element;
			int minNumOfRepetitions = copyOfMultiset[i].repetition;
			for(int j=0; j<relevantNodes.length; j++)
			{
				boolean foundElement = false;
				ElementOfMultiset[] multiset = relevantNodes[j].integerPartition.sortedMultiset;
				for(int k=0; k<multiset.length; k++)
					if( multiset[k].element == curElement ){
						foundElement = true;
						if( minNumOfRepetitions > multiset[k].repetition )
							minNumOfRepetitions = multiset[k].repetition ;
						break;
					}
				if( foundElement == false ){
					minNumOfRepetitions = 0;
					break;
				}
			}
                        copyOfMultiset[i].repetition = minNumOfRepetitions;
		}
                int counter=0;
		for(int i=0; i<copyOfMultiset.length; i++){
			if( copyOfMultiset[i].repetition > 0 ){
				counter++;
			}
		}
		if( counter == 0 ){
			return( null );
		}else{
			ElementOfMultiset[] subset = new ElementOfMultiset[ counter ];
			int index=0;
			for(int i=0; i<copyOfMultiset.length; i++)
				if( copyOfMultiset[i].repetition > 0 ){
					subset[index] = new ElementOfMultiset( copyOfMultiset[i].element, copyOfMultiset[i].repetition);
					index++;
				}
			return( subset );
		}
	}
	private void putSubsetAtTheBeginning( Node node, ElementOfMultiset[] subset, Input input )
	{
		if(( subset == null )||( input.solverName == SolverNames.IP )||( input.solverName == SolverNames.ODPinParallelWithIP )||( input.odpipSearchesMultipleSubspacesSimultaneiously == false )) return;
                ElementOfMultiset[] remainingIntegers_multiset = General.copyMultiset( node.integerPartition.sortedMultiset );
		for(int i=0; i<subset.length; i++)
			for(int j=0; j<remainingIntegers_multiset.length; j++)
				if( remainingIntegers_multiset[j].element    == subset[i].element ){
					remainingIntegers_multiset[j].repetition -= subset[i].repetition;
					break;
				}
                int counter=0;
		for(int i=0; i<remainingIntegers_multiset.length; i++)
			counter += remainingIntegers_multiset[i].repetition;
                int[] remainingIntegers_array = new int[ counter ];
		int index = 0;
		for(int i=0; i<remainingIntegers_multiset.length; i++)
			for(int j=0; j<remainingIntegers_multiset[i].repetition; j++){
				remainingIntegers_array[ index ] = remainingIntegers_multiset[i].element;
				index++;
			}	
                int[] newIntegers = new int[ node.subspace.integers.length ];
		int index1 = 0;
		int index2 = newIntegers.length-counter;		
		for(int i=0; i<node.subspace.integers.length; i++)
		{
			boolean found = false;		
			for(int j=0; j<remainingIntegers_array.length; j++){
				if( remainingIntegers_array[j] == node.subspace.integers[i] )
				{
					newIntegers[ index2 ] = node.subspace.integers[i];
					index2++;
					remainingIntegers_array[j] = -1;
					found = true;
					break;
				}
			}
			if( found == false ){
				newIntegers[ index1 ] = node.subspace.integers[i];
				index1++;
			}
		}
		node.subspace.integers = newIntegers;
	}
	private int getMaximumMultiplicity( int integer, Node node )
	{
            int maximumMultiplicity=0;
		for(int i=0; i<node.integerPartition.sortedMultiset.length; i++)
			if( integer == node.integerPartition.sortedMultiset[i].element )
				maximumMultiplicity = node.integerPartition.sortedMultiset[i].repetition;
                for(int k=0; k<node.subspace.relevantNodes.length; k++ )
		{
			Node curNode = node.subspace.relevantNodes[k];
			for(int i=0; i<curNode.integerPartition.sortedMultiset.length; i++)
				if( integer == curNode.integerPartition.sortedMultiset[i].element )
					if( maximumMultiplicity < curNode.integerPartition.sortedMultiset[i].repetition )
						maximumMultiplicity = curNode.integerPartition.sortedMultiset[i].repetition ; 
		}
		return( maximumMultiplicity );
	}
	private ElementOfMultiset[] getSubsetOfIntegersGivenTheirSum( ElementOfMultiset[] multiset, int sumOfIntegers )
	{
		if( multiset == null ) return( null );
		int maxSize = Math.min( sumOfIntegers, General.getCardinalityOfMultiset(multiset));
		ElementOfMultiset[] subset;
		for(int size=1; size<=maxSize; size++)
		{
			SubsetsOfMultiset subsetsOfMultiset = new SubsetsOfMultiset( multiset, size, false );
			subset = subsetsOfMultiset.getNextSubset();
			while(subset != null){
				int sum=0;
				for( int i=0; i<subset.length; i++ )
					sum += subset[i].element * subset[i].repetition;
				if( sum == sumOfIntegers )
					return( subset );
				else
					subset = subsetsOfMultiset.getNextSubset();
			}
		}
		return( null );
	}
	private ElementOfMultiset[] getSetOfIntegersGivenTheirRoot( int[] tempIntegers, int[] tempIntegerRoots, int root )
	{
		if( tempIntegerRoots == null ) return( null );
                int counter=0;
		for(int i=0; i<tempIntegerRoots.length; i++){
			if( tempIntegerRoots[i] == root ){
				counter++;
			}
		}
                if( counter == 0 ){
			return( null );
		}else{
			int[] resultAsArray = new int[ counter ];
			int index=0;
			for(int i=0; i<tempIntegerRoots.length; i++){
				if( tempIntegerRoots[i] == root ){
					resultAsArray[index] = tempIntegers[i];
					index++;
				}
			}
			return( General.convertArrayToMultiset( resultAsArray ) );
		}
	}
	public static void removeSubset( Node node, ElementOfMultiset[] subset, int[][] tempIntegers, int[][] tempIntegerRoots, int e )
	{
		if( subset == null ) return;
		int[] integers = General.copyArray( tempIntegers[e] );
		int[] integerRoots = General.copyArray( tempIntegerRoots[e] );
		ElementOfMultiset[] tempSubset = General.copyMultiset( subset );
		for(int i=0; i<integers.length; i++){
			for(int j=0; j<tempSubset.length; j++){
				if(( integers[i] == tempSubset[j].element )&&( tempSubset[j].repetition > 0 )){
					integers[i] = -1;
					tempSubset[j].repetition--;
					break;
				}
			}
		}
                int counter=0;
		for(int i=0; i<integers.length; i++){
			if( integers[i] > -1 ){
				counter++;
			}
		}
                if( counter == 0 ){
			tempIntegers[e] = integers;
			tempIntegerRoots[e] = integerRoots;
		}else{
			tempIntegers[e] = new int[ counter ];
			if( integerRoots != null )
				tempIntegerRoots[e] = new int[ counter ];
			else
				tempIntegerRoots[e] = null;
			int index=0;
			for(int i=0; i<integers.length; i++)
				if( integers[i] > -1 ){
					tempIntegers[e][index] = integers[i];
					if( integerRoots != null )
						tempIntegerRoots[e][index] = integerRoots[i];
					index++;
				}
		}
	}
	public long computeTotalNumOfExpansions( Result result )
	{
		long totalNumOfExpansions = 0;
		Node[][] nodes = result.ipIntegerPartitionGraph.nodes;
		for(int level=0; level<nodes.length; level++)
		{
			for(int i=0; i<nodes[level].length; i++)
			{
				Subspace curSubspace = nodes[level][i].subspace; 
				totalNumOfExpansions += curSubspace.totalNumOfExpansionsInSubspace;
			}
		}
		return totalNumOfExpansions;		
	}
}