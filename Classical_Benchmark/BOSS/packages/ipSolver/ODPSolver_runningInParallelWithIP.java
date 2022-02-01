package ipSolver;

import java.util.TreeSet;

import mainSolver.Result;
import dpSolver.DPSolver;
import inputOutput.Input;
import inputOutput.SolverNames;
import inputOutput.ValueDistribution;

public class ODPSolver_runningInParallelWithIP extends Thread
{
	private Input inputToODPSolver;
	private Result result;
	private double[] valueOfBestPartitionFound;
	private boolean stop = false;
	public DPSolver dpSolver;

	public void setStop( boolean value )
	{
		if( dpSolver != null )  dpSolver.setStop(value);
		stop = value;
	}
	
	 public ODPSolver_runningInParallelWithIP(  Input input, Result result )
	{
		this.stop = false;
		this.result= result;
		this.valueOfBestPartitionFound = new double[ 1 << input.numOfAgents ];
		
 		this.inputToODPSolver = getInputToODPSolver( input );
		
 		this.dpSolver = new DPSolver( inputToODPSolver, result );
	}
	
	 	public void updateValueOfBestPartitionFound( int coalition, double value ){
		if( valueOfBestPartitionFound[coalition] < value )
			valueOfBestPartitionFound[coalition] = value ;
	}
	 
	public double getValueOfBestPartitionFound( int coalition ){
		return valueOfBestPartitionFound[coalition];
	}
	
	 
	public void clearDPTable(){
		valueOfBestPartitionFound = null;
	}
 
	public void initValueOfBestPartitionFound(Input input, double[][] maxValueForEachSize)
	{
		long startTime = System.currentTimeMillis();
		
		valueOfBestPartitionFound[0]=0;
		

                result.init_max_f( input, maxValueForEachSize );
		


                for(int coalitionInBitFormat = valueOfBestPartitionFound.length-1; coalitionInBitFormat >= 1; coalitionInBitFormat--)
			valueOfBestPartitionFound[coalitionInBitFormat] = input.getCoalitionValue(coalitionInBitFormat);


        }
	
	 
	private Input getInputToODPSolver( Input input )
	{
		Input inputToDPSolver = new Input();
		inputToDPSolver.coalitionValues = input.coalitionValues;
		inputToDPSolver.problemID = input.problemID;
		
 		inputToDPSolver.solverName = SolverNames.ODPinParallelWithIP;
		
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