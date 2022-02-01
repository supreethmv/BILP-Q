package dpSolver;
import mainSolver.Result;
import inputOutput.*;
import general.Combinations;
import general.General;
import general.SubsetEnumerator;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
public class DPSolver {
	int[] t; 
    private double[] f;
    private boolean stop = false;
    private Input input;
    private Result result;
    public DPSolver( Input input, Result result ){
    	this.input = input;
    	this.result = result;
    }
    public void set_f( int index, double value ){
    	if( input.solverName == SolverNames.ODPIP )
    		result.idpSolver_whenRunning_ODPIP.updateValueOfBestPartitionFound( index, value);
    	else
    		if( input.solverName == SolverNames.ODPinParallelWithIP ) 
        		result.odpSolver_runningInParallelWithIP.updateValueOfBestPartitionFound( index, value);
    		else
    			f[ index ] = value;
    }
    public double get_f( int index ){
    	if( input.solverName == SolverNames.ODPIP )
    		return result.idpSolver_whenRunning_ODPIP.getValueOfBestPartitionFound( index );
    	else
        	if( input.solverName == SolverNames.ODPinParallelWithIP )
        		return result.odpSolver_runningInParallelWithIP.getValueOfBestPartitionFound( index );
        	else
        		return f[ index ];
    }
    public void clear_f(){
    	if( input.solverName == SolverNames.ODPIP )
    		result.idpSolver_whenRunning_ODPIP.clearDPTable();
    	else
        	if( input.solverName == SolverNames.ODPinParallelWithIP )
        		result.odpSolver_runningInParallelWithIP.clearDPTable();
        	else
        		f = null;
    }
	public void setStop( boolean value )
	{
		stop = value;
	}
	private boolean getStop()
	{
		return stop;
	}
    public void runDPorIDP()
    {    	
		result.set_dpHasFinished(false);
		final boolean IDPisHelpingIP;
    	if(( input.solverName != SolverNames.ODPIP )&&( input.solverName != SolverNames.ODPinParallelWithIP )){
    		IDPisHelpingIP = false;
    		f = new double[ input.coalitionValues.length ];
    		for(int i=0; i<f.length; i++)
    			set_f( i, input.getCoalitionValue(i) );
    	}else
    		IDPisHelpingIP = true;
    	int numOfAgents = input.numOfAgents;
    	set_f(0,0);  
     	long[] requiredTimeForEachSize = new long[ numOfAgents+1 ];
    	requiredTimeForEachSize[1] = 0;
    	long[] startTimeForEachSize = new long[ numOfAgents+1 ];
    	int grandCoalition = (1 << numOfAgents) - 1;
    	int numOfCoalitions = 1 << numOfAgents;
    	int bestHalfOfGrandCoalition=-1;
        long startTime = System.currentTimeMillis();
        result.set_dpMaxSizeThatWasComputedSoFar(1);
         if( input.solverName == SolverNames.DP ){ 
        	t = new int[numOfCoalitions];
        	for (int coalition = 0; coalition < numOfCoalitions; coalition++)
        		t[coalition] = coalition;
        }        
         for (int curSize = 2; curSize <= numOfAgents; curSize++)
        {
        	if( (input.solverName == SolverNames.IDP)||(input.solverName == SolverNames.ODPIP) )
        		if(( (int)(Math.floor((2*numOfAgents)/(double)3)) < curSize )&&( curSize<numOfAgents )) continue;
         	boolean allSplitsOfCurSizeWillBeEvaluated = true;
        	for(int sizeOfFirstHalf=(int)Math.ceil(curSize/(double)2); sizeOfFirstHalf<curSize; sizeOfFirstHalf++)
        		if( (input.solverName == SolverNames.IDP)||(input.solverName == SolverNames.ODPIP) )
        			if(( sizeOfFirstHalf > numOfAgents-curSize )&&( curSize!=numOfAgents )){
        				allSplitsOfCurSizeWillBeEvaluated = false;
        				break;
        			}
        	startTimeForEachSize[ curSize ] = System.currentTimeMillis();        	
         	if( curSize < numOfAgents )
        	{
        		int numOfCoalitionsOfCurSize = (int)Combinations.binomialCoefficient(numOfAgents, curSize);
        		if( allSplitsOfCurSizeWillBeEvaluated ) 
         		{
         			int numOfPossibleSplits = 1 << curSize;
         			int[] curCoalition = Combinations.getCombinationAtGivenIndex( curSize, numOfCoalitionsOfCurSize-1, numOfAgents);
        			evaluateSplitsEfficiently( curCoalition, curSize, numOfPossibleSplits );
        			for(int i=1; i<numOfCoalitionsOfCurSize; i++ ) {
        				Combinations.getPreviousCombination( numOfAgents, curSize, curCoalition );
        				evaluateSplitsEfficiently( curCoalition, curSize, numOfPossibleSplits );
        				if(( IDPisHelpingIP )&&( getStop() )) break;
        			}
        		}
        		else
        		{
         			int numOfPossibleSplits = 1 << curSize;
         			long[] numOfPossibleSplitsBasedOnSizeOfFirstHalf = computeNumOfPossibleSplitsBasedOnSizeOfFirstHalf( curSize );
         			int[] curCoalition = Combinations.getCombinationAtGivenIndex( curSize, numOfCoalitionsOfCurSize-1, numOfAgents);
        			for(int i=1; i<numOfCoalitionsOfCurSize; i++ ) {
        				Combinations.getPreviousCombination( numOfAgents, curSize, curCoalition );
        				if( (input.solverName == SolverNames.DP) || (input.useEfficientImplementationOfIDP) )
            				evaluateSplitsEfficiently( curCoalition, curSize, numOfPossibleSplits );        				
        				else
        					evaluateSplits( curCoalition, curSize, numOfPossibleSplitsBasedOnSizeOfFirstHalf );
        				if( getStop() ) break;
        			}
        		}
        		if(( IDPisHelpingIP )&&( getStop() )) break;
        	}else
        		bestHalfOfGrandCoalition = evaluateSplitsOfGrandCoalition();
         	if( curSize < numOfAgents ){
   				bestHalfOfGrandCoalition = evaluateSplitsOfGrandCoalition();
   				int[] bestCSFoundSoFar = getOptimalSplit( grandCoalition, bestHalfOfGrandCoalition);
   				int[][] bestCSFoundSoFar_byteFormat = Combinations.convertSetOfCombinationsFromBitToByteFormat( bestCSFoundSoFar, numOfAgents );
   				result.updateDPSolution( bestCSFoundSoFar_byteFormat , +input.getCoalitionStructureValue( bestCSFoundSoFar_byteFormat ) );
   				if(( input.solverName == SolverNames.ODPIP )||( input.solverName == SolverNames.ODPinParallelWithIP ))
   					result.updateIPSolution( bestCSFoundSoFar_byteFormat , input.getCoalitionStructureValue( bestCSFoundSoFar_byteFormat ) );
   			}
     		requiredTimeForEachSize[ curSize ] = System.currentTimeMillis() - startTimeForEachSize[curSize];
    		if( input.solverName == SolverNames.DP ){
    			System.out.println("   The time for Dynamic Programming to finish evaluating the splittings of coalitions of size "+curSize+" is: "+requiredTimeForEachSize[curSize]);
    		}else{  
    			System.out.print("   The time for Dynamic Programming to finish evaluating the splittings of coalitions of size "+curSize+" is: "+requiredTimeForEachSize[curSize]);
    			System.out.println(".  The best CS found so far by IDP is : "+General.convertArrayToString(result.get_dpBestCSFound())+" , its value is: "+result.get_dpValueOfBestCSFound());
    		}
    			result.set_dpMaxSizeThatWasComputedSoFar( curSize );
        }
        if( getStop() ){ result.set_dpHasFinished(true);  clear_f();  return; }
        result.dpTimeForEachSize = requiredTimeForEachSize;
         int[] bestCSFound = getOptimalSplit( grandCoalition, bestHalfOfGrandCoalition);
         result.dpTime = System.currentTimeMillis() - startTime;
        int[][] dpBestCSInByteFormat = Combinations.convertSetOfCombinationsFromBitToByteFormat( bestCSFound, numOfAgents );
        result.updateDPSolution( dpBestCSInByteFormat , input.getCoalitionStructureValue( dpBestCSInByteFormat ) );
        if(( input.solverName == SolverNames.ODPIP )||( input.solverName == SolverNames.ODPinParallelWithIP ))
        	result.updateIPSolution( dpBestCSInByteFormat , input.getCoalitionStructureValue( dpBestCSInByteFormat ) );
		result.set_dpHasFinished(true);  clear_f();
         if( input.printDistributionOfThefTable )
        	printDistributionOfThefTable();        
        if( input.printPercentageOf_v_equals_f )
        	printPercentageOf_v_equals_f();
    }
    private long[] computeNumOfPossibleSplitsBasedOnSizeOfFirstHalf( int size )
    {
    	long[] numOfPossibleSplitsBasedOnSizeOfFirstHalf = new long[size];
    	for(int sizeOfFirstHalf=(int)Math.ceil(size/(double)2); sizeOfFirstHalf<size; sizeOfFirstHalf++){
    		int sizeOfSecondHalf = (int)(size - sizeOfFirstHalf);
    		if(( (size % 2) == 0 )&&( sizeOfFirstHalf == sizeOfSecondHalf ))
    			numOfPossibleSplitsBasedOnSizeOfFirstHalf[ sizeOfFirstHalf ] = Combinations.binomialCoefficient(size, sizeOfFirstHalf )/2;
    		else
    			numOfPossibleSplitsBasedOnSizeOfFirstHalf[ sizeOfFirstHalf ] = Combinations.binomialCoefficient(size, sizeOfFirstHalf );    		
    	}
    	return( numOfPossibleSplitsBasedOnSizeOfFirstHalf );
    }    
    private int[] getOptimalSplit( int coalitionInBitFormat, int bestHalfOfCoalition )
    {
    	int[] optimalSplit;
    	if( bestHalfOfCoalition == coalitionInBitFormat )
    	{
    		optimalSplit = new int[1];
    		optimalSplit[0] = coalitionInBitFormat;
    	}
    	else
    	{
 			int[] arrayOfBestHalf = new int[2];
			int[][] arrayOfOptimalSplit = new int[2][];
    		int[] arrayOfCoalitionInBitFormat = new int[2]; 
     		arrayOfCoalitionInBitFormat[0] = bestHalfOfCoalition;
    		arrayOfCoalitionInBitFormat[1] = coalitionInBitFormat - bestHalfOfCoalition;
     		for( int i=0; i<2; i++ )
    		{
    			if( (input.solverName == SolverNames.DP) ) 
    				arrayOfBestHalf[i] = t[ arrayOfCoalitionInBitFormat[i] ];
    			else
    				arrayOfBestHalf[i] = getBestHalf( arrayOfCoalitionInBitFormat[i] ); 
    			arrayOfOptimalSplit[i] = getOptimalSplit( arrayOfCoalitionInBitFormat[i], arrayOfBestHalf[i] );
    		}
        	optimalSplit = new int[ arrayOfOptimalSplit[0].length + arrayOfOptimalSplit[1].length ];
        	int k=0;
        	for( int i=0; i<2; i++ )
        		for( int j=0; j<arrayOfOptimalSplit[i].length; j++ )
        		{
        			optimalSplit[k] = arrayOfOptimalSplit[i][j];
        			k++;
        		}
    	}
    	return( optimalSplit );
    }
    public int[][] getOptimalSplit( int[][] CS )
    {   
     	int[][] optimalSplit = new int[ CS.length ][]; 
    	int numOfCoalitionsInFinalResult=0;
     	for( int i=0; i<CS.length; i++ )
    	{
   			int coalitionInBitFormat = Combinations.convertCombinationFromByteToBitFormat( CS[i] );
   			int bestHalfOfCoalitionInBitFormat = getBestHalf( coalitionInBitFormat );
   			optimalSplit[i] = getOptimalSplit( coalitionInBitFormat, bestHalfOfCoalitionInBitFormat );
    		numOfCoalitionsInFinalResult += optimalSplit[i].length;
    	}
     	int[][] finalResult = new int[ numOfCoalitionsInFinalResult ][];
    	int k=0;
    	for( int i=0; i<CS.length; i++ ) 
    	{
    		for( int j=0; j<optimalSplit[i].length; j++ ) 
    		{
    			finalResult[k] = Combinations.convertCombinationFromBitToByteFormat( optimalSplit[i][j] , input.numOfAgents );
    			k++;
    		}
    	}
    	return( finalResult );
    }
    private int evaluateSplitsOfGrandCoalition()
    {    	
     	double curValue=-1;
    	double bestValue=-1;
    	int bestHalfOfGrandCoalitionInBitFormat=-1;
    	int numOfCoalitions = 1 << input.numOfAgents;
    	int grandCoalition = (1 << input.numOfAgents) - 1;
     	for (int firstHalfOfGrandCoalition=(numOfCoalitions/2)-1; firstHalfOfGrandCoalition<numOfCoalitions; firstHalfOfGrandCoalition++)
    	{
    		int secondHalfOfGrandCoalition = numOfCoalitions-1-firstHalfOfGrandCoalition;
    		curValue = get_f(firstHalfOfGrandCoalition) + get_f(secondHalfOfGrandCoalition); 
    		if( curValue > bestValue )
    		{
    			bestValue = curValue;
    			bestHalfOfGrandCoalitionInBitFormat = firstHalfOfGrandCoalition;
    		}
    	}    	
     	int firstHalfOfGrandCoalition = grandCoalition;
    	curValue = get_f( firstHalfOfGrandCoalition );
		if( curValue > bestValue )
		{
			bestValue = curValue;
			bestHalfOfGrandCoalitionInBitFormat = firstHalfOfGrandCoalition;
		}    	
 		set_f( grandCoalition, bestValue);
		if( input.solverName == SolverNames.DP ) 
        	t[ grandCoalition ] = bestHalfOfGrandCoalitionInBitFormat;
    	return( bestHalfOfGrandCoalitionInBitFormat );
    }
    private void evaluateSplits( int[] coalitionInByteFormat, int coalitionSize, long[] numOfPossibleSplitsBasedOnSizeOfFirstHalf )
    {
    	double curValue=-1;
    	double bestValue=-1;
    	int bestHalfOfCoalitionInBitFormat=-1;
    	int numOfAgents = input.numOfAgents;
    	int coalitionInBitFormat = Combinations.convertCombinationFromByteToBitFormat( coalitionInByteFormat );
 		int[] bit = new int[numOfAgents+1];
		for(int i=0; i<numOfAgents; i++)
			bit[i+1] = 1 << i;
     	for(int sizeOfFirstHalf=(int)Math.ceil(coalitionSize/(double)2); sizeOfFirstHalf<coalitionSize; sizeOfFirstHalf++)
    	{
    		 if(( (input.solverName == SolverNames.IDP)||(input.solverName == SolverNames.ODPIP) )&&( sizeOfFirstHalf > numOfAgents-coalitionSize )&&( coalitionSize!=numOfAgents )) continue;
     		int[] indicesOfMembersOfFirstHalf = new int[ sizeOfFirstHalf ];
    		for(int i=0; i<sizeOfFirstHalf; i++)
    			indicesOfMembersOfFirstHalf[i] = (int)(i+1);
     		int firstHalfInBitFormat=0;
        	for(int i=0; i<sizeOfFirstHalf; i++)
        		firstHalfInBitFormat += bit[ coalitionInByteFormat[ indicesOfMembersOfFirstHalf[i]-1 ] ];
         	int secondHalfInBitFormat = (coalitionInBitFormat-firstHalfInBitFormat);        	
             curValue = get_f( firstHalfInBitFormat ) + get_f( secondHalfInBitFormat );
            if( bestValue < curValue ){
                bestValue = curValue;
                if( input.solverName == SolverNames.DP )
                	bestHalfOfCoalitionInBitFormat = firstHalfInBitFormat;
            }            
             for(int j=1; j<numOfPossibleSplitsBasedOnSizeOfFirstHalf[ sizeOfFirstHalf ]; j++)
            {
     			Combinations.getPreviousCombination( coalitionSize, sizeOfFirstHalf, indicesOfMembersOfFirstHalf );
             	firstHalfInBitFormat=0;
            	for(int i=0; i<sizeOfFirstHalf; i++)
            		firstHalfInBitFormat += bit[ coalitionInByteFormat[ indicesOfMembersOfFirstHalf[i]-1 ] ];
            	secondHalfInBitFormat = (coalitionInBitFormat-firstHalfInBitFormat);           	
             	curValue = get_f( firstHalfInBitFormat ) + get_f( secondHalfInBitFormat );
            	if( bestValue < curValue ) {
            		bestValue = curValue;
            		if( input.solverName == SolverNames.DP )
            			bestHalfOfCoalitionInBitFormat = firstHalfInBitFormat;
            	}
            }            
    	}    	
 		int firstHalfInBitFormat = coalitionInBitFormat;
		curValue = get_f( firstHalfInBitFormat );
        if( bestValue < curValue ){
            bestValue = curValue;
            if( input.solverName == SolverNames.DP )
            	bestHalfOfCoalitionInBitFormat = firstHalfInBitFormat;
        }    	
         if(( input.solverName == SolverNames.ODPIP )||( input.solverName == SolverNames.ODPinParallelWithIP ))
        	if( result.get_max_f( coalitionSize-1 ) < bestValue )
        		result.set_max_f( coalitionSize-1 ,   bestValue );
         set_f( coalitionInBitFormat, bestValue );
        if( input.solverName == SolverNames.DP )
        	t[ coalitionInBitFormat ] = bestHalfOfCoalitionInBitFormat;
    }
    private void evaluateSplitsEfficiently( int[] coalitionInByteFormat, int coalitionSize, int numOfPossibilities )
    {
    	double curValue=-1;
    	double bestValue=-1;
    	int bestHalfOfCoalitionInBitFormat=-1;
    	int coalitionInBitFormat = Combinations.convertCombinationFromByteToBitFormat( coalitionInByteFormat );
    	bestValue=input.getCoalitionValue(coalitionInBitFormat);
    	bestHalfOfCoalitionInBitFormat=coalitionInBitFormat;
     	for (int firstHalfInBitFormat = coalitionInBitFormat-1 & coalitionInBitFormat; ; firstHalfInBitFormat = firstHalfInBitFormat-1 & coalitionInBitFormat)
    	{
    		int secondHalfInBitFormat = coalitionInBitFormat^firstHalfInBitFormat;
     		curValue = get_f( firstHalfInBitFormat ) + get_f( secondHalfInBitFormat );
    		if(bestValue<=curValue)
    		{
    			bestValue = curValue;
    			if( input.solverName == SolverNames.DP ){  
    				if( Integer.bitCount(firstHalfInBitFormat) > Integer.bitCount(secondHalfInBitFormat) )
    					bestHalfOfCoalitionInBitFormat = firstHalfInBitFormat;
    				else
    					bestHalfOfCoalitionInBitFormat = secondHalfInBitFormat;
    			}
    		}
    		if((firstHalfInBitFormat & (firstHalfInBitFormat-1))==0)break;
    	}
     	if(( input.solverName == SolverNames.ODPIP )||( input.solverName == SolverNames.ODPinParallelWithIP ))
    		if( result.get_max_f( coalitionSize-1 ) < bestValue )
    			result.set_max_f( coalitionSize-1 ,   bestValue );
     	set_f( coalitionInBitFormat, bestValue );
    	if( input.solverName == SolverNames.DP )
    		t[ coalitionInBitFormat ] = bestHalfOfCoalitionInBitFormat;
    }
    private int getBestHalf( int coalitionInBitFormat )
    {
    	double valueOfBestSplit = input.getCoalitionValue( coalitionInBitFormat );
    	int best_firstHalfInBitFormat = coalitionInBitFormat;
 		int[] bit = new int[input.numOfAgents+1];
		for(int i=0; i<input.numOfAgents; i++)
			bit[i+1] = 1 << i;
 		int[] coalitionInByteFormat = Combinations.convertCombinationFromBitToByteFormat(coalitionInBitFormat, input.numOfAgents);
 		int coalitionSize = (int)coalitionInByteFormat.length;
		for(int sizeOfFirstHalf=(int)Math.ceil(coalitionSize/(double)2); sizeOfFirstHalf<coalitionSize; sizeOfFirstHalf++)
    	{
    		int sizeOfSecondHalf = (int)(coalitionSize - sizeOfFirstHalf);
     		long numOfPossibleSplits;
    		if(( (coalitionSize % 2) == 0 )&&( sizeOfFirstHalf == sizeOfSecondHalf ))
    			numOfPossibleSplits = Combinations.binomialCoefficient(coalitionSize, sizeOfFirstHalf )/2;
    		else
				numOfPossibleSplits = Combinations.binomialCoefficient(coalitionSize, sizeOfFirstHalf );    		
     		int[] indicesOfMembersOfFirstHalf = new int[ sizeOfFirstHalf ];
    		for(int i=0; i<sizeOfFirstHalf; i++)
    			indicesOfMembersOfFirstHalf[i] = (int)(i+1);
     		int firstHalfInBitFormat=0;
        	for(int i=0; i<sizeOfFirstHalf; i++)
        		firstHalfInBitFormat += bit[ coalitionInByteFormat[ indicesOfMembersOfFirstHalf[i]-1 ] ];
         	int secondHalfInBitFormat = (coalitionInBitFormat-firstHalfInBitFormat);
         	if( get_f( firstHalfInBitFormat ) + get_f( secondHalfInBitFormat ) > valueOfBestSplit ){
            	best_firstHalfInBitFormat = firstHalfInBitFormat;
            	valueOfBestSplit = get_f( firstHalfInBitFormat ) + get_f( secondHalfInBitFormat );
        	}
             for(int j=1; j<numOfPossibleSplits; j++)
            {
    			Combinations.getPreviousCombination( coalitionSize, sizeOfFirstHalf, indicesOfMembersOfFirstHalf );
             	firstHalfInBitFormat=0;
            	for(int i=0; i<sizeOfFirstHalf; i++)
            		firstHalfInBitFormat += bit[ coalitionInByteFormat[ indicesOfMembersOfFirstHalf[i]-1 ] ];
             	secondHalfInBitFormat = (coalitionInBitFormat-firstHalfInBitFormat);
             	if( get_f( firstHalfInBitFormat ) + get_f( secondHalfInBitFormat ) > valueOfBestSplit ){
                	best_firstHalfInBitFormat = firstHalfInBitFormat;
                	valueOfBestSplit = get_f( firstHalfInBitFormat ) + get_f( secondHalfInBitFormat );
            	}
            }
    	}
     	return( best_firstHalfInBitFormat );	
    }
    private void printPercentageOf_v_equals_f()
    {
    	int numOfAgents = input.numOfAgents;
    	int totalNumOfCoalitions = 1 << numOfAgents;
    	int totalCounter=0;
    	long[] numOfCoalitionsOfParticularSize = new long[numOfAgents+1];
    	int[] counter = new int[numOfAgents+1];
    	for(int i=1; i<numOfAgents+1; i++){
    		counter[i]=0;
    		numOfCoalitionsOfParticularSize[i] = Combinations.binomialCoefficient(numOfAgents, i);
    	}
    	for(int i=1; i<totalNumOfCoalitions; i++)
    		if(input.getCoalitionValue(i)==get_f(i)){
    			counter[ Integer.bitCount(i) ]++;
    			totalCounter++;
    		}
    	System.out.println("percentage of all coalitions of that are optimal partitions of themselves is: "+((double)totalCounter/totalNumOfCoalitions));
    	for(int i=2; i<=numOfAgents; i++){
    		System.out.println("size: "+i+"  percentage: "+((double)counter[i]/numOfCoalitionsOfParticularSize[i]));
    	}
    }
	private void printDistributionOfThefTable()
	{
 		int totalNumOfCoalitions = 1 << input.numOfAgents;
		int[] counter = new int[40];
		for(int i=0; i<counter.length; i++){
			counter[i]=0;
		}		
 		long min = Integer.MAX_VALUE;
		long max = Integer.MIN_VALUE;
		for(int i=1; i<totalNumOfCoalitions; i++){
			long currentWeightedValue = (long)Math.round( get_f(i) / Integer.bitCount(i) );
			if( min > currentWeightedValue )
				min = currentWeightedValue ;
			if( max < currentWeightedValue )
				max = currentWeightedValue ;
		}
		System.out.println("The maximum weighted f value is  "+max+"  and the minimum one is  "+min);
 		for(int i=1; i<totalNumOfCoalitions; i++){
			long currentWeightedValue = (long)Math.round( get_f(i) / Integer.bitCount(i) );
			int percentageOfMax = (int)Math.round( (currentWeightedValue-min) * (counter.length-1) / (max-min) );
			counter[percentageOfMax]++;
		}
 		System.out.println("The distribution of the weighted coalition values:");
		System.out.print(ValueDistribution.toString(input.valueDistribution)+"_f = [");
		for(int i=0; i<counter.length; i++)
			System.out.print(counter[i]+" ");
		System.out.println("]");
	}
}