package mainSolver;
import inputOutput.Input;
import general.Combinations;
import general.General;
import ipSolver.IntegerPartitionGraph;
import ipSolver.IDPSolver_whenRunning_ODPIP;
import ipSolver.ODPSolver_runningInParallelWithIP;
public class Result
{
	public Result( Input input )  
	{
		totalNumOfCS = Combinations.getNumOfCS( input.numOfAgents );
		totalNumOfCoalitionsInSearchSpace = Combinations.getNumOfCoalitionsInSearchSpace( input.numOfAgents );
		dpMaxSizeThatWasComputedSoFar = 1;
	}
	public int       numOfAgents;  
	public long      totalNumOfCS; 
	public long      totalNumOfCoalitionsInSearchSpace; 
	public long      totalNumOfExpansions; 
	public IDPSolver_whenRunning_ODPIP idpSolver_whenRunning_ODPIP;
	public ODPSolver_runningInParallelWithIP odpSolver_runningInParallelWithIP;
	public long     inclusionExclusionTime; 
	public double   inclusionExclusionTime_confidenceInterval; 
	public  int[][] inclusionExclusionBestCSFound; 
	public  double  inclusionExclusionValueOfBestCSFound; 
	public double   inclusionExclusionValueOfBestCSFound_confidenceInterval; 
	public long     cplexTime;  
	public double   cplexTime_confidenceInterval; 
	public  int[][] cplexBestCSFound; 
	public  double  cplexValueOfBestCSFound; 
	public double   cplexValueOfBestCSFound_confidenceInterval; 
	private  int     dpMaxSizeThatWasComputedSoFar;  
	private  boolean dpHasFinished;  
	private  int[][] dpBestCSFound;  
	private  double  dpValueOfBestCSFound;  
	public long      dpTime;  
	public long[]    dpTimeForEachSize ;  
	private  int[][] ipBestCSFound;  
	private  double  ipValueOfBestCSFound;  
	public double    ipValueOfBestCS_confidenceInterval;  
	public long      ipStartTime;  
	public double    ipTimeForScanningTheInput_confidenceInterval;  
	public long      ipTime;  
	public double    ipTime_confidenceInterval;  
	public long      ipTimeForScanningTheInput;  
	public long      ipNumOfExpansions;  
	public double    ipNumOfExpansions_confidenceInterval;  
	public double    ipUpperBoundOnOptimalValue;  
	public double    ipUpperBoundOnOptimalValue_confidenceInterval;  		
	public double    ipLowerBoundOnOptimalValue;  
	public IntegerPartitionGraph ipIntegerPartitionGraph;  
	private double[]  max_f; 
	public void initializeIPResults(){
		ipStartTime=System.currentTimeMillis();
		ipNumOfExpansions=0;
		ipValueOfBestCSFound=-1;
		ipBestCSFound = null;
		totalNumOfExpansions = 0;
	}
	public void updateDPSolution( int[][] CS, double value ){
		if( get_dpValueOfBestCSFound() < value ){
			set_dpValueOfBestCSFound( value );
			set_dpBestCSFound( General.copyArray(CS) );
		}
	}
	public synchronized void updateIPSolution( int[][] CS, double value ){
		if( get_ipValueOfBestCSFound() <= value ){
			set_ipValueOfBestCSFound( value );
			set_ipBestCSFound( General.copyArray(CS) );
		}
	}
	public synchronized void set_dpHasFinished( boolean dpHasFinished ){
		this.dpHasFinished = dpHasFinished;
	}
	public synchronized boolean get_dpHasFinished(){
		return dpHasFinished;
	}
	public synchronized void set_dpMaxSizeThatWasComputedSoFar( int size ){
		dpMaxSizeThatWasComputedSoFar = size;
	}
	public synchronized int get_dpMaxSizeThatWasComputedSoFar(){
		return dpMaxSizeThatWasComputedSoFar;
	}
	public synchronized void set_dpBestCSFound( int[][] CS ){
		dpBestCSFound = General.copyArray( CS );		
	}
	public synchronized int[][] get_dpBestCSFound(){
		return dpBestCSFound;		
	}
	public synchronized void set_dpValueOfBestCSFound(double value){
		dpValueOfBestCSFound = value;				
	}
	public synchronized double get_dpValueOfBestCSFound(){
		return dpValueOfBestCSFound;				
	}
	public   void set_ipBestCSFound( int[][] CS ){
		ipBestCSFound = General.copyArray( CS );		
	}
	public   int[][] get_ipBestCSFound(){
		return ipBestCSFound;		
	}
	public   void set_ipValueOfBestCSFound(double value){
		ipValueOfBestCSFound = value;				
	}
	public   double get_ipValueOfBestCSFound(){
		return ipValueOfBestCSFound;				
	}
	public void set_max_f( int index, double value){
		max_f[ index ] = value;
	}
	public double get_max_f( int index ){
		return( max_f[index] );
	}
	public void init_max_f( Input input, double[][] maxValueForEachSize ){
		max_f = new double[input.numOfAgents];
    	for( int i=0; i<input.numOfAgents; i++ )
    		set_max_f( i, 0 );
		for(int i=0; i<input.numOfAgents; i++){
			double value = input.getCoalitionValue( (1<<i) );  
			if( get_max_f( 0 ) < value )
				set_max_f( 0 ,   value );
		}
		for(int i=1; i<input.numOfAgents; i++)  
			set_max_f( i, maxValueForEachSize[i][0] );
	}
}