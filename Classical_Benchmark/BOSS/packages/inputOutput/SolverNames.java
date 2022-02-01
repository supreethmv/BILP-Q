package inputOutput;

public enum SolverNames {
	
	DP,  
	
	IDP,  
	ODP,  

	IP,  
	
	ODPIP,  
	
	ODPinParallelWithIP,  
	
	InclusionExclusion;  
	
	public static String toString( SolverNames solverName ){
		 
		if( solverName == ODPIP ) return( "BOSS" );
		 
		return "";
	}
}
