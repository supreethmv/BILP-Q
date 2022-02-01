package ipSolver;

import mainSolver.Result;
import dpSolver.DPSolver;
import inputOutput.Input;
import inputOutput.SolverNames;

public class IDPSolver_whenRunning_ODPIP extends Thread
{
	private Input inputToIDPSolver;
	private Result result;
	private double[] valueOfBestPartitionFound;
	private boolean stop = false;
	public DPSolver dpSolver;

	public void setStop( boolean value )
	{
		if( dpSolver != null )  dpSolver.setStop(value);
		stop = value;
	}
	
	 
	public IDPSolver_whenRunning_ODPIP(  Input input, Result result )
	{
		this.stop = false;
		this.result= result;
		this.valueOfBestPartitionFound = new double[ 1 << input.numOfAgents ];
		
 		this.inputToIDPSolver = getInputToIDPSolver( input );
		
 		this.dpSolver = new DPSolver( inputToIDPSolver, result );
	}
	
	 
	public synchronized void updateValueOfBestPartitionFound( int coalition, double value ){
		if( valueOfBestPartitionFound[coalition] < value )
			valueOfBestPartitionFound[coalition] = value ;
	}
	 
	public double getValueOfBestPartitionFound( int coalition ){
		return valueOfBestPartitionFound[coalition];
	}
	
	 
	public void clearDPTable(){
		valueOfBestPartitionFound = null;
	}

	 
	public void run()
	{
		this.dpSolver.runDPorIDP();		
	}
	
	 
	public void initValueOfBestPartitionFound(Input input, double[][] maxValueForEachSize)
	{
		long startTime = System.currentTimeMillis();
		
		valueOfBestPartitionFound[0]=0;
		
 		result.init_max_f( input, maxValueForEachSize );
		
		 
		for(int coalitionInBitFormat = valueOfBestPartitionFound.length-1; coalitionInBitFormat >= 1; coalitionInBitFormat--)
			valueOfBestPartitionFound[coalitionInBitFormat] = input.getCoalitionValue(coalitionInBitFormat);

 	}
	
	 
	private Input getInputToIDPSolver( Input input )
	{
		Input inputToDPSolver = new Input();
		inputToDPSolver.coalitionValues = input.coalitionValues;
		inputToDPSolver.problemID = input.problemID;
		
 		inputToDPSolver.solverName = SolverNames.ODPIP;
		
 		inputToDPSolver.storeCoalitionValuesInFile = false;
		inputToDPSolver.printDetailsOfSubspaces = false;
		inputToDPSolver.printNumOfIntegerPartitionsWithRepeatedParts = false;
		inputToDPSolver.printInterimResultsOfIPToFiles = false;
		inputToDPSolver.printTimeTakenByIPForEachSubspace = false;

 		inputToDPSolver.feasibleCoalitions = null;
		inputToDPSolver.numOfAgents = input.numOfAgents;
		inputToDPSolver.numOfRunningTimes = 1;	
		
		return( inputToDPSolver );
	}
}