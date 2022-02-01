package ipSolver;

import java.util.Arrays;
import java.util.TreeSet;

import mainSolver.Result;
import inputOutput.Input;
import inputOutput.Output;
import inputOutput.SolverNames;
import general.Combinations;
import general.General;
import general.RandomPermutation;
import general.RandomSubsetOfGivenSet;

public class Subspace
{
	public long sizeOfSubspace;
	public long totalNumOfExpansionsInSubspace;
	public int[] integers;
	public int[] integersSortedAscendingly;
	public double UB,Avg,LB;
	public boolean enabled;
	public double priority;
	public long timeToSearchThisSubspace;
	public long numOfSearchedCoalitionsInThisSubspace;
	public double numOfSearchedCoalitionsInThisSubspace_confidenceInterval;
	public Node[] relevantNodes;
	public boolean isReachableFromBottomNode;
	
	 
	public Subspace( int[] integers )
	{
		this.integers = integers;
		this.integersSortedAscendingly = General.sortArray( integers, true );
		this.sizeOfSubspace   = computeNumOfCSInSubspace( this.integers );
	}
	
 	
	public Subspace( int[] integers, double[] avgValueForEachSize, double[][] maxValueForEachSize, Input input )
	{	
		this.integers = integers; 

                this.integersSortedAscendingly = General.sortArray( integers, true );
		this.timeToSearchThisSubspace = 0;
		this.enabled = true;
		

                this.sizeOfSubspace   = computeNumOfCSInSubspace( this.integers );
		

                this.totalNumOfExpansionsInSubspace = computeTotalNumOfExpansionsInSubspace( this.integers, input);
		

                if( this.integers.length == 2 )
		{
			int size1 = this.integers[0]; int size2 = this.integers[1];
			int numOfCombinationsOfSize1 = (int) Combinations.binomialCoefficient( input.numOfAgents, size1 );
			int temp; 
			if( size1 != size2 ) 
				temp = numOfCombinationsOfSize1;
			else
				temp =(numOfCombinationsOfSize1/2);

			this.numOfSearchedCoalitionsInThisSubspace = 2*temp;;
		}
		else
			if(( this.integers.length == 1 )||( this.integers.length == input.numOfAgents ))
				this.numOfSearchedCoalitionsInThisSubspace = this.integers.length;
			else
				this.numOfSearchedCoalitionsInThisSubspace = 0;
		

                int j=0;				
		this.UB=0;
		for(int k=0; k<=this.integers.length-1; k++)
		{
			if(( k>0 )&&( this.integers[k] == this.integers[k-1] ))
				j++;
			else
				j=0;					
			this.UB += maxValueForEachSize[ this.integers[k]-1 ][ j ];
		}

		
                
		this.Avg=0;
		for(int k=0; k<=this.integers.length-1; k++)
			this.Avg += avgValueForEachSize[ this.integers[k]-1 ];
		
		
                
		this.LB = this.Avg;
	}
	
        
	public int search( Input input, Output output, Result result, double acceptableValue, double[] avgValueForEachSize, double[] sumOfMax, int numOfIntegersToSplit)
	{
		long timeBeforeSearchingThisSubspace = System.currentTimeMillis();

		if( ((input.solverName == SolverNames.ODPIP)||(input.solverName == SolverNames.ODPinParallelWithIP)) && (result.get_dpHasFinished()) ){
			this.enabled = false;
			return(1);
		}
		if( input.printTheSubspaceThatIsCurrentlyBeingSearched )
			System.out.println( input.problemID+" - Searching "+General.convertArrayToString( integersSortedAscendingly )+"   -->   "+General.convertArrayToString( integers ) );
		
		if( input.useSamplingWhenSearchingSubspaces ){
			if( input.samplingDoneInGreedyFashion )
				searchUsingSamplingInGreedyFashion( input, output, result, acceptableValue );
			else
				searchUsingSampling( input, output, result );

                }else
			search_useBranchAndBound( input, output, result, acceptableValue, sumOfMax, numOfIntegersToSplit);


                if( result.get_dpHasFinished() == false ){
			if( input.solverName == SolverNames.ODPIP ){
				int[][] CS = result.idpSolver_whenRunning_ODPIP.dpSolver.getOptimalSplit( result.get_ipBestCSFound() );
				result.updateIPSolution(CS, input.getCoalitionStructureValue(CS));
			}
			if( input.solverName == SolverNames.ODPinParallelWithIP ){
				int[][] CS = result.odpSolver_runningInParallelWithIP.dpSolver.getOptimalSplit( result.get_ipBestCSFound() );
				result.updateIPSolution(CS, input.getCoalitionStructureValue(CS));
			}
		}
		timeToSearchThisSubspace = System.currentTimeMillis() - timeBeforeSearchingThisSubspace;
		output.printTimeRequiredForEachSubspace( this, input);
		this.enabled = false;
		int numOfSearchedSubspaces = 1;

                if(( input.solverName == SolverNames.ODPIP )&&( relevantNodes != null )){
			for(int i=0; i<relevantNodes.length; i++)
				if( relevantNodes[i].subspace.enabled ){
					relevantNodes[i].subspace.enabled = false;
					numOfSearchedSubspaces++;
					if( input.printTheSubspaceThatIsCurrentlyBeingSearched )
						System.out.println(input.problemID+" - ****** with DP's help, IP avoided the subspace: "+Arrays.toString(relevantNodes[i].integerPartition.partsSortedAscendingly));
				}
		}
		return( numOfSearchedSubspaces );
	}
	
 	
	private void searchUsingSampling( Input input, Output output, Result result )
	{
 		if(( integers.length == 1 )||( integers.length == input.numOfAgents )) {
			searchFirstOrLastLevel( integers,input,output,result );
			return;
		}		
 		int numOfSamples = Math.min( 100000, (int)Math.round(sizeOfSubspace*0.1));
		RandomPermutation randomPermutation = new RandomPermutation( input.numOfAgents );
		
 		for(int i=0; i<numOfSamples; i++)
		{
			int[] permutation = randomPermutation.get();
			double coalitionStructureValue=0;
			int[] coalitionStructure = new int[integers.length];
			int indexInPermutation=0;
 			for(int j=integers.length-1; j>=0; j--)
			{
				int size = integers[j];
				int currentCoalition = 0;
 				for(int k=0; k<size; k++){					
					currentCoalition += ( 1 << (permutation[ indexInPermutation ]) ); 
 					indexInPermutation++;
				}
				coalitionStructure[j] = currentCoalition;
				
 				coalitionStructureValue += input.getCoalitionValue( currentCoalition );
 			}				
 			if( result.get_ipValueOfBestCSFound() < coalitionStructureValue )
			{
				int[][] CSInByteFormat = Combinations.convertSetOfCombinationsFromBitToByteFormat( coalitionStructure, input.numOfAgents, integers);
				result.updateIPSolution( CSInByteFormat, coalitionStructureValue);
			}
		}
		output.printCurrentResultsOfIPToStringBuffer_ifPrevResultsAreDifferent( input, result );
	}
	
 	
	private void searchUsingSamplingInGreedyFashion( Input input, Output output, Result result, double acceptableValue )
	{
 		if(( integers.length == 1 )||( integers.length == input.numOfAgents )) {
			searchFirstOrLastLevel( integers,input,output,result );
			return;
		}
 		int[] numOfRemainingAgents = new int[ integers.length ];
		numOfRemainingAgents[0] = input.numOfAgents;
		for(int j=1; j<integers.length; j++)
			numOfRemainingAgents[j] = numOfRemainingAgents[j-1] - integers[j-1];

 		long[] numOfPossibleCoalitions = new long[ integers.length ];
		for(int j=0; j<integers.length; j++)
			numOfPossibleCoalitions[j] = Combinations.binomialCoefficient( numOfRemainingAgents[j], integers[j]);
		
 		int[] numOfCoalitionSamplesPerSizePerCS = new int[ integers.length ];
		for(int j=0; j<integers.length; j++)
			numOfCoalitionSamplesPerSizePerCS[j] = Math.min( 100000, (int)Math.ceil( numOfPossibleCoalitions[j] * 0.1 ) );
		
 		int numOfCoalitionStructureSamples = Math.min( 10000, (int)Math.ceil( sizeOfSubspace * 0.1 ));

 		int[] CS = new int[ integers.length ];
		
 		for(int i=0; i<numOfCoalitionStructureSamples; i++)
		{			
 			int setOfRemainingAgentsInBitFormat = (1 << input.numOfAgents) - 1;
			int[] setOfRemainingAgentsInByteFormat = Combinations.convertCombinationFromBitToByteFormat( setOfRemainingAgentsInBitFormat, input.numOfAgents);
			
			for(int j=0; j<integers.length - 1; j++)  
			{
				RandomSubsetOfGivenSet samplingObject = new RandomSubsetOfGivenSet( setOfRemainingAgentsInByteFormat, numOfRemainingAgents[j], integers[j]);
				double bestValueOfCurrentSize = Double.MIN_VALUE;				
 				for(int k=0; k < numOfCoalitionSamplesPerSizePerCS[j]; k++){
					int C = samplingObject.getSubsetInBitFormat();
 					double currentCoalitionValue = input.getCoalitionValue( C );
					if( bestValueOfCurrentSize < currentCoalitionValue ){
						bestValueOfCurrentSize = currentCoalitionValue ;
						CS[j] = C;
					}
				}
 				setOfRemainingAgentsInBitFormat -= CS[j];
				setOfRemainingAgentsInByteFormat = Combinations.convertCombinationFromBitToByteFormat( setOfRemainingAgentsInBitFormat, input.numOfAgents, numOfRemainingAgents[j]);
			}
 			CS[ integers.length - 1 ] = setOfRemainingAgentsInBitFormat;
			
 			if( result.get_ipValueOfBestCSFound() < input.getCoalitionStructureValue(CS) ){
				int[][] CSInByteFormat = Combinations.convertSetOfCombinationsFromBitToByteFormat(CS,input.numOfAgents,integers);
				result.updateIPSolution( CSInByteFormat, input.getCoalitionStructureValue(CS));
				if( result.get_ipValueOfBestCSFound() >= acceptableValue ){ 
 					output.printCurrentResultsOfIPToStringBuffer_ifPrevResultsAreDifferent( input, result );
					return;
				}
			}
		}
		output.printCurrentResultsOfIPToStringBuffer_ifPrevResultsAreDifferent( input, result );
	}
	
	
        
	public int[] orderParts( int[] integers, double[] avgValueForEachSize, double[][] maxValueForEachSize, Input input )
	{

            int[] underlyingSet = General.getUnderlyingSet( integers );
		int[] multiplicity = new int[ input.numOfAgents ];
		for(int i=0; i<input.numOfAgents; i++)
			multiplicity[i] = 0;
		for(int i=0; i<integers.length; i++)
			multiplicity[ integers[i]-1 ]++;


                int[] newUnderlyingSet = new int[ underlyingSet.length ];
		for(int i=0; i<newUnderlyingSet.length; i++){
			newUnderlyingSet[i] = 0;
		}

                for(int i=0; i<newUnderlyingSet.length; i++)
		{
			double biggestDifference=-1;
			int sizeWithMaxDifference=-1;
			for(int j=0; j<underlyingSet.length; j++)
			{
				int curSize = underlyingSet[j];


                                boolean sizeAlreadyAddedToList = false;
				for(int k=0; k<i; k++)
					if( newUnderlyingSet[k] == curSize ){
						sizeAlreadyAddedToList = true;
						break;
					}
				if( sizeAlreadyAddedToList ) continue;


                                if( biggestDifference < (maxValueForEachSize[ curSize-1 ][0] - avgValueForEachSize[ curSize-1 ]) ){
					biggestDifference = (maxValueForEachSize[ curSize-1 ][0] - avgValueForEachSize[ curSize-1 ]) ;
					sizeWithMaxDifference = curSize;
				}			
			}
			newUnderlyingSet[i] = sizeWithMaxDifference;
		}

                int[] newIntegers = new int[ integers.length ];
		int index=0;
		for(int i=0; i<newUnderlyingSet.length; i++){
			int curSize = newUnderlyingSet[i];
			for(int j=0; j<multiplicity[ curSize-1 ]; j++){
				newIntegers[ index ] = curSize;
				index++;
			}
		}
		return( newIntegers );
	}

	
        
	public long computeNumOfCSInSubspace( int[] integers )
	{

            int numOfAgents=0;
		for(int i=0; i<integers.length; i++) numOfAgents += integers[i];


                if(( integers.length==1 )||( integers.length==numOfAgents ))
			return( 1 );


                int[]     length_of_A               =  init_length_of_A( numOfAgents, integers );
		int[]     max_first_member_of_M     =  init_max_first_member_of_M( integers, length_of_A, false );
		long[][]  numOfCombinations         =  init_numOfCombinations( integers, length_of_A, max_first_member_of_M );
		long[]    sumOf_numOfCombinations   =  init_sumOf_numOfCombinations( numOfCombinations, integers, length_of_A, max_first_member_of_M );
		long[]    numOfRemovedCombinations  =  init_numOfRemovedCombinations( integers, length_of_A, max_first_member_of_M);
		long[][]  increment                 =  init_increment( integers, numOfCombinations, sumOf_numOfCombinations, max_first_member_of_M, false);


                long sizeOfSubspace = 0;
		if( numOfRemovedCombinations[0]==0 ) { 
                    
			sizeOfSubspace = increment[0][0]*sumOf_numOfCombinations[0];
		}
		else { 
                    
			for(int i=0; i<=max_first_member_of_M[0]-1; i++)
				sizeOfSubspace += increment[0][i]*numOfCombinations[0][i] ;
		}		
		return( sizeOfSubspace );
		
		  }	

	 
	public static long computeTotalNumOfExpansionsInSubspace( int[] integers, Input input)
	{
 		int numOfAgents = 0;
		for( int i=0; i<integers.length; i++)
			numOfAgents += integers[i];
		
 		int[] sortedIntegers = General.sortArray( integers, input.orderIntegerPartitionsAscendingly );

		int[] alpha = new int[ sortedIntegers.length ];
		long[][] gamma = new long[ sortedIntegers.length ][];
		for( int j=0; j<sortedIntegers.length; j++ )
		{
 			int maxIndex=0;
			for( int k=0; k<sortedIntegers.length; k++)
				if( sortedIntegers[k] == sortedIntegers[j] )
					maxIndex = k;
			
 			alpha[j] = numOfAgents + 1;
			for( int k=0; k<=maxIndex; k++)
				alpha[j] -= sortedIntegers[k];
			
 			gamma[j] = new long[alpha[j]];
			for( int beta=0; beta<alpha[j]; beta++ )
			{
				int sumOfPreviousIntegers = 0;
				for( int k=0; k<j; k++)
					sumOfPreviousIntegers += sortedIntegers[k];
				
				if( j==0 )
					gamma[j][beta] = Combinations.binomialCoefficient( numOfAgents-sumOfPreviousIntegers-(beta+1) , sortedIntegers[j]-1 );
				else{
					int lambda;
					if( sortedIntegers[j] == sortedIntegers[j-1] )
						lambda = beta;
					else
						lambda = alpha[j-1] - 1;

					long sum = 0;
					for( int k=0; k<=lambda; k++ )
						sum += gamma[j-1][k];
					gamma[j][beta] = sum * Combinations.binomialCoefficient( numOfAgents-sumOfPreviousIntegers-(beta+1) , sortedIntegers[j]-1 );
				}
			}
		}
		long numOfExpansionsInSubspace=0;
		for( int j=0; j<sortedIntegers.length; j++ )
			for( int beta=0; beta<alpha[j]; beta++ )
				numOfExpansionsInSubspace += gamma[j][beta];
		
		return( numOfExpansionsInSubspace );
	}

	 
	private void search_useBranchAndBound( Input input,Output output,Result result,double acceptableValue,double[] sumOfMax, int numOfIntegersToSplit)
	{
 		if(( integers.length == 1 )||( integers.length == input.numOfAgents )) {
			searchFirstOrLastLevel( integers,input,output,result );
			return;
		}
 		final int numOfIntegers = integers.length;
		final long ipNumOfSearchedCoalitions_beforeSearchingThisSubspace  = result.ipNumOfExpansions;
		final int numOfAgents = input.numOfAgents;
		final int numOfIntsToSplit = numOfIntegersToSplit;
		final boolean ipUsesBranchAndBound = input.ipUsesBranchAndBound;
		final boolean constraintsExist;
		boolean this_CS_is_useless;
		double valueOfCS=0;
		if( input.feasibleCoalitions == null ) constraintsExist = false;
		else constraintsExist = true;
		final boolean ODPIsHelpingIP; if( input.solverName == SolverNames.ODPIP ) ODPIsHelpingIP = true; else ODPIsHelpingIP = false;
		final boolean ODPinParallelWithIP; if( input.solverName == SolverNames.ODPinParallelWithIP ) ODPinParallelWithIP = true; else ODPinParallelWithIP = false;
		
 		final int[]     bit                      =  init_bit( numOfAgents );
		final int[]     length_of_A              =  init_length_of_A( numOfAgents, integers );
		final int[]     max_first_member_of_M    =  init_max_first_member_of_M( integers,length_of_A, input.odpipUsesLocalBranchAndBound );
		final long[][]  numOfCombinations        =  init_numOfCombinations( integers, length_of_A, max_first_member_of_M );
		final long[]    sumOf_numOfCombinations  =  init_sumOf_numOfCombinations( numOfCombinations, integers, length_of_A, max_first_member_of_M );
		final long[]    numOfRemovedCombinations =  init_numOfRemovedCombinations( integers, length_of_A, max_first_member_of_M);
		final long[][]  increment                =  init_increment( integers, numOfCombinations, sumOf_numOfCombinations, max_first_member_of_M, input.odpipUsesLocalBranchAndBound );
		final long[]    indexToStartAt           =  init_indexToStartAt(numOfIntegers, numOfRemovedCombinations, sumOf_numOfCombinations);
		final long[]    indexToStopAt            =  init_indexToStopAt(numOfIntegers, numOfRemovedCombinations);
		long[]    index_of_M   =  init_index_of_M( 1,integers,increment,max_first_member_of_M,numOfCombinations,numOfRemovedCombinations,sumOf_numOfCombinations);
		int[][]   M            =  init_M( index_of_M, integers, length_of_A, numOfAgents );
		int[][]   A            =  init_A( numOfAgents, integers, M, length_of_A );
		int[]     CS           =  init_CS_hashTableVersion( M, A, length_of_A, bit, numOfIntegers );
		int[]     sumOf_agents =  init_sumOf_agents_hashTableVersion( numOfIntegers, CS );
		double[]  sumOf_values =  init_sumOf_values_hashTableVersion( numOfIntegers, CS, input );
		result.ipNumOfExpansions  += integers.length-2;
		IDPSolver_whenRunning_ODPIP localBranchAndBoundObject = result.idpSolver_whenRunning_ODPIP;

		main_loop: while(true)
		{
			if(( (ODPIsHelpingIP)||(ODPinParallelWithIP) )&&( result.get_dpHasFinished() )){  
				numOfSearchedCoalitionsInThisSubspace = result.ipNumOfExpansions - ipNumOfSearchedCoalitions_beforeSearchingThisSubspace;
				return;						
			}
 			do{ 
				setTheLastTwoCoalitionsInCS( CS, M[numOfIntegers-2], A, numOfIntegers, bit);

 				this_CS_is_useless = false;
				if(( constraintsExist )&&( checkIfLastTwoCoalitionsSatisfyConstraints( CS,input.feasibleCoalitions )==false ))
					this_CS_is_useless = true;

 				if( this_CS_is_useless == false )
				{
 					switch( numOfIntsToSplit ){
					case 0:  valueOfCS = sumOf_values[ numOfIntegers-2 ] + input.getCoalitionValue(CS[numOfIntegers-2])    + input.getCoalitionValue(CS[numOfIntegers-1]); break;
					case 1:  valueOfCS = sumOf_values[ numOfIntegers-2 ] + input.getCoalitionValue(CS[numOfIntegers-2])    + localBranchAndBoundObject.getValueOfBestPartitionFound( CS[numOfIntegers-1] ); break;
					default: valueOfCS = sumOf_values[ numOfIntegers-2 ] + localBranchAndBoundObject.getValueOfBestPartitionFound(CS[numOfIntegers-2]) + localBranchAndBoundObject.getValueOfBestPartitionFound( CS[numOfIntegers-1] );
					}
 					if( result.get_ipValueOfBestCSFound() < valueOfCS )
					{
						int[][] CSInByteFormat = Combinations.convertSetOfCombinationsFromBitToByteFormat(CS,numOfAgents,integers);
						result.updateIPSolution( CSInByteFormat, valueOfCS);

						if( result.get_ipValueOfBestCSFound() >= acceptableValue ) 

						{
							output.printCurrentResultsOfIPToStringBuffer_ifPrevResultsAreDifferent( input, result );
							numOfSearchedCoalitionsInThisSubspace = result.ipNumOfExpansions - ipNumOfSearchedCoalitions_beforeSearchingThisSubspace;
							return;
						}
					}
				}
				index_of_M[ numOfIntegers-2 ]--;
				Combinations.getPreviousCombination( length_of_A[numOfIntegers-2], integers[numOfIntegers-2], M[numOfIntegers-2]);
			}
			while( index_of_M[ numOfIntegers-2 ] >= indexToStopAt [ numOfIntegers-2 ] );

			 
			int s1 = numOfIntegers-3;
			sub_loop: while(s1>=0)
			{
				if( index_of_M[s1] > indexToStopAt[s1] ) 
 				{
					if( s1==0 ){
						output.printCurrentResultsOfIPToStringBuffer_ifPrevResultsAreDifferent( input, result );
						if(( (ODPIsHelpingIP)||(ODPinParallelWithIP) )&&( result.get_dpHasFinished() )){  
							numOfSearchedCoalitionsInThisSubspace = result.ipNumOfExpansions - ipNumOfSearchedCoalitions_beforeSearchingThisSubspace;
							return;						
						}
					}
					for(int s2=s1; s2<=numOfIntegers-3; s2++)
					{
						boolean firstTime = true;
						do{
							result.ipNumOfExpansions++;  
 
							if(( firstTime )&&( s2 > s1 ) ) {
								
 								set_M_and_index_of_M( M, index_of_M, length_of_A, indexToStartAt, s2 );
								firstTime = false;
							}else{
 								Combinations.getPreviousCombination(length_of_A[s2], integers[s2], M[s2]);
								index_of_M[s2]--;								
							}
 							int temp3=0;
							for(int j1=integers[s2]-1; j1>=0; j1--)
								temp3 |= bit[ A[ s2 ][ M[ s2 ][j1]-1 ] ];
							CS[ s2 ]=temp3;


                                                        this_CS_is_useless=false;
							if( constraintsExist ){
								if( input.feasibleCoalitions.contains(new Integer(CS[s2])) == false )
									this_CS_is_useless = true;
							}
 							if(( ipUsesBranchAndBound )&&( this_CS_is_useless == false ))
							{
 								int newCoalition = CS[s2];
								double valueOfNewCoalition;
								if( s2 >= numOfIntegers-numOfIntsToSplit ){
									valueOfNewCoalition = localBranchAndBoundObject.getValueOfBestPartitionFound( CS[s2] );
								}else{
									valueOfNewCoalition = input.getCoalitionValue( CS[s2] );
								}
								sumOf_values[s2+1] = sumOf_values[s2] + valueOfNewCoalition;
								sumOf_agents[s2+1] = sumOf_agents[s2] + CS[s2];
								
 								double upperBoundForRemainingAgents = sumOfMax[s2+2];
 								if( ( (sumOf_values[s2+1]+upperBoundForRemainingAgents) - result.get_ipValueOfBestCSFound() <-0.00000000005)
										||( (ODPIsHelpingIP) && (useLocalBranchAndBound( input,localBranchAndBoundObject,sumOf_values,sumOf_agents,s2,newCoalition,valueOfNewCoalition)) ))
									this_CS_is_useless = true;
							}
 							if( this_CS_is_useless == false ) break;	
						}
						while( index_of_M[s2] > indexToStopAt[s2] );

						if( this_CS_is_useless ) { 
 							s1 = s2-1;
							continue sub_loop;
						}
						update_A( A[s2+1], A[s2], length_of_A[s2], M[s2], integers[s2]);
					}
 					int s2 = numOfIntegers-2;
					set_M_and_index_of_M( M, index_of_M, length_of_A, indexToStartAt, s2 );
					
					continue main_loop;
				}
				s1--;
			}
			break main_loop;
		}
		output.printCurrentResultsOfIPToStringBuffer_ifPrevResultsAreDifferent( input, result );
		numOfSearchedCoalitionsInThisSubspace = result.ipNumOfExpansions - ipNumOfSearchedCoalitions_beforeSearchingThisSubspace;
	}
	
	 
	private void searchFirstOrLastLevel(int[] integers, Input input, Output output, Result result)
	{
 		int numOfAgents = input.numOfAgents; int[][] curCS;
		
		if( integers.length==1 )
		{
			curCS = new int[1][numOfAgents];
			for(int i=0; i<=(int)(numOfAgents-1); i++)
				curCS[0][i]=(int)(i+1);
		}
		else {
			curCS = new int[numOfAgents][1];
			for(int i=0; i<=numOfAgents-1; i++)
				curCS[i][0]=(int)(i+1);
		}
		double valueOfCurCS = input.getCoalitionStructureValue( curCS );

		result.updateIPSolution( curCS, valueOfCurCS);

		output.printCurrentResultsOfIPToStringBuffer_ifPrevResultsAreDifferent( input, result );
	}
	 
	private boolean checkIfLastTwoCoalitionsSatisfyConstraints( int[] CS, TreeSet<Integer> feasibleCoalitions)
	{
		if( feasibleCoalitions.contains(new Integer(CS[CS.length-1])) == false )
			return(false);
		if( feasibleCoalitions.contains(new Integer(CS[CS.length-2])) == false )
			return(false);
		return(true);
	}
	
 
	private int[] init_bit( int numOfAgents )
	{
		int[] bit = new int[numOfAgents+1];
		for(int i=0; i<numOfAgents; i++)
			bit[i+1] = 1 << i;
		return( bit );
	}
	
	  
	private long[] init_indexToStartAt( int numOfIntegers, long[] numOfRemovedCombinations, long[] sumOf_numOfCombinations)
	{
		long[] indexToStartAt = new long[ numOfIntegers ];
		for(int i=0; i<numOfIntegers; i++)
		{
			indexToStartAt[i] = sumOf_numOfCombinations[i] + numOfRemovedCombinations[i];
		}
		return( indexToStartAt );
	}
	
	 	
	private long[] init_indexToStopAt( int numOfIntegers, long[] numOfRemovedCombinations )
	{
		long[] indexToStopAt  = new long[ numOfIntegers ];
		for(int i=0; i<numOfIntegers; i++)
		{
			indexToStopAt[i] = numOfRemovedCombinations[i] + 1;
		}
		return( indexToStopAt );
	}
	 
	private int[] init_max_first_member_of_M( int[] integers, int[] length_of_A, boolean ipUsesLocalBranchAndBound )
	{
		int[] max_first_member_of_M = new int[ integers.length ];
		int i=integers.length-1;		
		
		if(( ipUsesLocalBranchAndBound )&&( relevantNodes != null )&&( integers.length > 2 )){
			max_first_member_of_M[i] = (int)( length_of_A[i]-integers[i]+1 );
			i--;
		}
		while(i>=0)
		{
			max_first_member_of_M[i] = (int)( length_of_A[i]-integers[i]+1 );
			i--;
			while(( i>=0 )&&( integers[i]==integers[i+1] ))
			{
				max_first_member_of_M[i] = max_first_member_of_M[i+1];
				i--;
			}
		}
		return( max_first_member_of_M );
	}
	
	 
	private long[][] init_numOfCombinations( int[] integers, int[] length_of_A, int[] max_first_member_of_M)
	{	
		long[][] numOfCombinations = new long[ integers.length ][];
		for(int i=0; i<=integers.length-1; i++ )
		{
			if( length_of_A[i]==integers[i] )
			{
				numOfCombinations[i]=new long[1];
				numOfCombinations[i][0]=1;
			}
			else
			{
				numOfCombinations[i] = new long[max_first_member_of_M[i]];
				for(int j=0; j<=max_first_member_of_M[i]-1; j++)
				{
					numOfCombinations[i][j] = Combinations.binomialCoefficient( (int)(length_of_A[i]-(j+1)) , (int)(integers[i]-1) );
				}
			}
		}
		return( numOfCombinations );
	}
	
	 
	private long[] init_sumOf_numOfCombinations( long[][] numOfCombinations, int[] integers, int[] length_of_A, int[] max_first_member_of_M)
	{	
		long[] sumOf_numOfCombinations = new long[ integers.length ];
		
		for(int i=0; i<=integers.length-1; i++ )
		{
			if( length_of_A[i]==integers[i] )
			{
				sumOf_numOfCombinations[i]=1;
			}
			else
			{
				sumOf_numOfCombinations[i]=0;
				for(int j=0; j<=max_first_member_of_M[i]-1; j++)
				{
					sumOf_numOfCombinations[i] = sumOf_numOfCombinations[i] + numOfCombinations[i][j];
				}
			}
		}
		return( sumOf_numOfCombinations );
	}
	
	 
	private long[] init_numOfRemovedCombinations( int[] integers, int[] length_of_A, int[] max_first_member_of_M)
	{	
		long[] numOfRemovedCombinations = new long[ integers.length ];
		
		for(int i=0; i<=integers.length-1; i++ )
		{
			if( length_of_A[i]==integers[i] )
			{
				numOfRemovedCombinations[i]=0;
			}
			else
			{
				numOfRemovedCombinations[i]=0;
				for(int j=max_first_member_of_M[i]; j<=length_of_A[i]-integers[i]; j++)
				{
					numOfRemovedCombinations[i] = numOfRemovedCombinations[i] + Combinations.binomialCoefficient( (int)(length_of_A[i]-(j+1)) , (int)(integers[i]-1) );
				}
			}
		}
		return( numOfRemovedCombinations );
	}	
	
	 
	private long[][] init_increment( int[] integers, long[][] numOfCombinations, long[] sumOf_numOfCombinations, int[] max_first_member_of_M, boolean ipUsesLocalBranchAndBound )
	{
		long[][] increment = new long[ integers.length ][];
		increment[ integers.length-1 ] = new long[1];
		increment[ integers.length-1 ][0]=1;
		
		int s=integers.length-2;
		while(s>=0)  
		{
			if(( integers[s]!=integers[s+1] )||( (ipUsesLocalBranchAndBound) && (s==integers.length-2) && (integers.length>2) ))					
			{
 				increment[s]=new long[1];
				increment[s][0]=sumOf_numOfCombinations[s+1] * increment[s+1][0];
				s--;
			}
			else
			{
				 
				increment[s]=new long[max_first_member_of_M[s]];
				for(int i=0; i<=max_first_member_of_M[s]-1; i++)
				{					
					increment[s][i]=0;
					for(int j=i; j<=max_first_member_of_M[s]-1; j++)
						increment[s][i] += ( numOfCombinations[s+1][j] * increment[s+1][ 0 ] );
				}
				s--;
				
				 
				while(( s>=0 )&&( integers[s]==integers[s+1] ))
				{
					increment[s]=new long[max_first_member_of_M[s]];
					for(int i=0; i<=max_first_member_of_M[s]-1; i++)
					{
						increment[s][i]=0;
						for(int j=i; j<=max_first_member_of_M[s]-1; j++)
							increment[s][i] += ( numOfCombinations[s+1][j] * increment[s+1][ j ] );
					}
					s--;
				}
				
				 
				if( s>=0 )
				{
					increment[s]=new long[1];
					increment[s][0]=0;
					for(int j=0; j<=max_first_member_of_M[s+1]-1; j++)
						increment[s][0] += ( numOfCombinations[s+1][j] * increment[s+1][j] );
					s--;
				}
			}
		}
		return( increment );
	}
	
	 
	private int[][] init_M( long[] index_of_M, int[] integers, int[] length_of_A, int numOfAgents )
	{
		long[][] pascalMatrix = Combinations.initPascalMatrix( numOfAgents+1, numOfAgents+1 );
		
 		int[][] M = new int[ integers.length ][];
		for(int s=0; s<=integers.length-1; s++)
		{
			M[s] = new int[integers[s]];
		}
		
 		for(int i=0; i<=integers.length-1; i++)
		{
			int j=1; long index=index_of_M[i]; int s1=integers[i];
			
			boolean done=false;
			do
			{
 				int x=1; while( pascalMatrix[s1-1][x-1] < index ) x++;
				
				  M[i][j-1]=(int)( (length_of_A[i]-s1+1)-x+1 );
				  if( pascalMatrix[s1-1][x-1]==index )
				{
 					for(int k=j; k<=integers[i]-1; k++) M[i][k]=(int)(M[i][k-1]+1);
					done=true;
				}
 				else
				{
					j=j+1;  index=index-pascalMatrix[s1-1][x-2];  s1=s1-1;
				}
			}
			while(done==false);
		}
		return(M);
	}
	
	 
	private long[] init_index_of_M( long index, int[] integers, long[][] increment, int[] max_first_member_of_M, long[][] numOfCombinations, long[] numOfRemovedCombinations, long[] sumOf_numOfCombinations)
	{
		
		
		long counter1=0;
		long counter2=1;
		long[] index_of_M=new long[ integers.length ];
		
		index_of_M[integers.length-1]=1;		
		
		int min_first_member_of_M=0;
		for(int i=0; i<=integers.length-2; i++)
		{
			if( sumOf_numOfCombinations[i]==1 ) 
			{
				index_of_M[i]=1;
			}
			else
				if( increment[i].length == 1 ) 
				{
					counter1=0;
					counter2=1;
					if( min_first_member_of_M>0 )
						for(int j=0; j<=min_first_member_of_M-1; j++)
							counter2 += numOfCombinations[i][j];
					
					long steps = (long) ( Math.ceil( index / (double)increment[i][0] ) -1);
					counter1 += steps*increment[i][0]; 
					counter2 += steps;
					
					index_of_M[i] = counter2;
					index        -= counter1;
					
					if(( i>=integers.length-1 )||( integers[i]!=integers[i+1] )) min_first_member_of_M=0;
				}
				else
 				{
					counter1=0;
					counter2=1;
					if( min_first_member_of_M > 0 )
						for(int j=0; j<min_first_member_of_M; j++)
							counter2 += numOfCombinations[i][j];
					
					 
					for(int j=min_first_member_of_M; j<max_first_member_of_M[i]; j++)
					{
						if( index <= counter1+(numOfCombinations[i][j]*increment[i][j]) )
						{
							long steps = (long) Math.ceil( (index-counter1) / (double)increment[i][j] ) - 1;
							counter1 += steps*increment[i][j];
							counter2 += steps;

							index_of_M[i] = counter2;
							index        -= counter1;
							
							if(( i<integers.length-1 )&&( integers[i]==integers[i+1] ))
								min_first_member_of_M = j;
							else
								min_first_member_of_M = 0;
							
							break;
						}
						else
						{
							long steps = numOfCombinations[i][j];
							counter1 += steps*increment[i][j];
							counter2 += steps;
						}
					}
				}
		}			

 		for(int i=0; i<=index_of_M.length-1; i++)
			index_of_M[i] = (sumOf_numOfCombinations[i]+numOfRemovedCombinations[i]) - index_of_M[i] + 1;
		
		return( index_of_M );
	}
	
	 
	private int[][] init_A( int numOfAgents, int[] integers, int[][] M, int[] length_of_A )
	{
		 
		int[][] A = new int[ integers.length-1 ][];
		for(int s=0; s<=integers.length-2; s++)
		{
			A[s]=new int[length_of_A[s]];
			if( s==0 )
			{
				for(int j1=0; j1<=numOfAgents-1; j1++)
				{
					A[s][j1]=(int)(j1+1);
				}
			}
			else
			{
				int j1=0;int j2=0;
				for(int j3=0; j3<=length_of_A[s-1]-1; j3++)
				{
					if(( j1>=M[s-1].length )||( j3+1 != M[s-1][j1] ))
					{
						A[s][j2]=A[s-1][j3];
						j2++;
					}
					else j1++;
				}
			}
		}
		return( A );
	}
	
	 
	private int[] init_length_of_A( int numOfAgents, int[] integers)
	{
		int[] length_of_A = new int[ integers.length ];
		
		length_of_A[0]=numOfAgents;
		if( integers.length > 1 )
			for(int s=1; s<=integers.length-1; s++)
				length_of_A[s]=(int)(length_of_A[s-1]-integers[s-1]);

		return( length_of_A );
	}
	 
	private int[] init_CS_hashTableVersion( int[][] M,int[][] A,int[] length_of_A,int[] bit,int numOfIntegers)
	{
		int[] CS = new int[ integers.length ];
		
		for(int s=0; s<=integers.length-2; s++)
		{
			CS[s]=0;
			for(int j1=0; j1<M[s].length; j1++)
			{
				CS[s] |= bit[ A[ s ][ M[s][j1]-1 ] ];
			}
		}
		setTheLastTwoCoalitionsInCS(CS, M[numOfIntegers-2], A, numOfIntegers, bit);
		return( CS );
	}
	
	 
	private double[] init_sumOf_values_hashTableVersion( int numOfIntegers, int[] CS, Input input )
	{
		double[] sumOf_values = new double[ numOfIntegers+1 ];		
		
		sumOf_values[0] = 0;
		for(int i=1; i<=numOfIntegers; i++)
		{
			sumOf_values[i] = sumOf_values[i-1] + input.getCoalitionValue( CS[i-1] );
		}

		return( sumOf_values );
	}	

	 
	private int[] init_sumOf_agents( int numOfIntegers, int[][] CS, int[] bit )
	{
		int[] sumOf_agents = new int[ numOfIntegers+1 ];		
		
		sumOf_agents[0] = 0;
		
		for(int i=1; i<=numOfIntegers; i++)
		{			
			sumOf_agents[i] = sumOf_agents[i-1];
			for(int j=0; j<integers[i-1]; j++)
			{
				sumOf_agents[i] += bit[ CS[i-1][j] ];
			}
		}

		return( sumOf_agents );
	}	
	
	 
	private int[] init_sumOf_agents_hashTableVersion( int numOfIntegers, int[] CS )
	{
		int[] sumOf_agents = new int[ numOfIntegers+1 ];		
		
		sumOf_agents[0] = 0;
		
		for(int i=1; i<=numOfIntegers; i++)
		{
			sumOf_agents[i] = sumOf_agents[i-1] + CS[i-1];
		}

		return( sumOf_agents );
	}
	 
	private void set_M_and_index_of_M(int[][] M, long[] index_of_M, 
			final int[] length_of_A, final long[] indexToStartAt, int s2)
	{
 		index_of_M[s2] = indexToStartAt[s2];

 		if( integers[s2]==integers[s2-1] ){
			if( M[s2-1][0]>1 )
				for(int j=1; j<M[s2-1][0]; j++)
					index_of_M[s2]= index_of_M[s2] - Combinations.binomialCoefficient( length_of_A[s2]-j, integers[s2]-1);

			for(int j1=0; j1<integers[s2]; j1++)
				M[s2][j1]=(int)(M[s2-1][0]+j1);
		}
		else
			for(int j1=0; j1<integers[s2]; j1++)
				M[s2][j1]=(int)(1+j1);
	}
	
	 
	private void update_A(int[] A1, int[] A2, final int numOfAgents, int[] M, final int length_of_M )
	{
		int j1=0;
		int j2=0;
		for(int j3=0; j3<A2.length; j3++)
		{
			if(( j1>=length_of_M )||( (j3+1)!=M[j1] ))
			{
				A1[j2]=A2[j3];
				j2++;
			}
			else j1++;
		}
	}
	
	 
	private void setTheLastTwoCoalitionsInCS( int[][] CS, int[][] M,
			int[][] A, final int[] length_of_A, final int numOfIntegers ) 
	{
    	int j1=0;
		int j2=0;		
		for(int j3=0; j3<length_of_A[ numOfIntegers-2 ]; j3++)
		{
			if(( j1>=integers[ numOfIntegers-2 ] )||( (j3+1)!=M[ numOfIntegers-2 ][j1] ))
			{
				CS[ numOfIntegers-1 ][j2] = A[ numOfIntegers-2 ][j3];
				j2++;
			}
			else
			{
				CS[ numOfIntegers-2 ][j1] = A[ numOfIntegers-2 ][j3];
				j1++;
			}
		}
	}
	
	 
	private void setTheLastTwoCoalitionsInCS( int[] CS, int[] M,
			int[][] A, final int numOfIntegers, final int[] bit)
	{
		int result1=0;
		int result2=0;		
		int m=integers[ numOfIntegers-2 ]-1;
		int a=A[ numOfIntegers-2 ].length-1;
		do
		{
			if( a == M[m]-1 )
			{
				result1 += bit[ A[ numOfIntegers-2 ][a] ];
				if( m==0 )
				{
					a--;
					break;
				}
				m--;
			}
			else
				result2 += bit[ A[ numOfIntegers-2 ][a] ];
			
			a--;
		}
		while( a>=0 );
		
		while( a>=0 )
		{
			result2 += bit[ A[ numOfIntegers-2 ][a] ];
			a--;
		}		
		CS[ numOfIntegers-2 ] = result1;
		CS[ numOfIntegers-1 ] = result2;
	}
	
	 
	private void set_CS_given_its_index( final int numOfAgents, int[][] CS, final long index,
			final int[] length_of_A, final long[][] increment, final int[] max_first_member_of_M,
			final long[][] numOfCombinations, final long[] numOfRemovedCombinations, final long[] sumOf_numOfCombinations)
	{
 		long[] index_of_M = init_index_of_M( index, integers, increment, max_first_member_of_M, numOfCombinations, numOfRemovedCombinations, sumOf_numOfCombinations);
		
 		int[][] M = init_M( index_of_M, integers, length_of_A, numOfAgents );
		
 		int[][] A = init_A( numOfAgents,integers, M, length_of_A );

 		for(int s=0; s<=integers.length-3; s++)
			for(int i=0; i<=M[s].length-1; i++)
				CS[s][i]=A[ s ][ M[s][i]-1 ];		
		setTheLastTwoCoalitionsInCS( CS, M, A, length_of_A, integers.length );
	}	
	
	 
	private void set_CS_given_its_index( final int numOfAgents, int[] CS, final long index, final int[] length_of_A, final long[][] increment,
			final int[] max_first_member_of_M,	final long[][] numOfCombinations, final long[] numOfRemovedCombinations, final long[] sumOf_numOfCombinations, final int[] bit)
	{
 		long[] index_of_M = init_index_of_M( index, integers, increment, max_first_member_of_M, numOfCombinations, numOfRemovedCombinations, sumOf_numOfCombinations);
		
 		int[][] M = init_M( index_of_M, integers, length_of_A, numOfAgents );
		
 		int[][]  A =  init_A( numOfAgents, integers, M, length_of_A );
		
 		for(int s=0; s<=integers.length-3; s++)
		{
			CS[s]=0;
			for(int j1=0; j1<=M[s].length-1; j1++)
				CS[s] |= bit[ A[ s ][ M[s][j1]-1 ] ];
		}
		
		int remainingAgents = 0;
		for(int i=length_of_A[ integers.length-2 ]-1; i>=0; i--)
		{
		   	remainingAgents |= bit[ A[ integers.length-2 ][i] ];
		}		
		setTheLastTwoCoalitionsInCS(CS,M[integers.length-2], A, integers.length, bit);		
	}
	
	 
	private boolean useLocalBranchAndBound( Input input,IDPSolver_whenRunning_ODPIP localBranchAndBoundObject,double[] sumOf_values, int[] sumOf_agents, int s2, int newCoalition, double valueOfNewCoalition)
	{
		if( input.odpipUsesLocalBranchAndBound == false ) return false;

		boolean result = false;
		
 		if(( localBranchAndBoundObject.getValueOfBestPartitionFound(sumOf_agents[s2+1]) - sumOf_values[s2+1] >0.00000000005 )||( localBranchAndBoundObject.getValueOfBestPartitionFound(newCoalition) - valueOfNewCoalition >0.00000000005 ))
			result = true;

 		if( localBranchAndBoundObject.getValueOfBestPartitionFound(sumOf_agents[s2+1]) - sumOf_values[s2+1] <-0.00000000005 )
			localBranchAndBoundObject.updateValueOfBestPartitionFound( sumOf_agents[s2+1], sumOf_values[s2+1] );
		
 		if( localBranchAndBoundObject.getValueOfBestPartitionFound( newCoalition ) - valueOfNewCoalition <-0.00000000005 )
			localBranchAndBoundObject.updateValueOfBestPartitionFound( newCoalition, valueOfNewCoalition );
		
		return( result );
		
	}
}