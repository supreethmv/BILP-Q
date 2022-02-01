package mainSolver;


import inputOutput.*;
import dpSolver.*;
import ipSolver.*;

public class MainSolver
{
 	
	public Result solve( Input input )	
	{
		ComputeErrorBars computeErrorBars = new ComputeErrorBars(input); 
 
		for( int problemID=1; problemID<=input.numOfRunningTimes; problemID++) 
 		{			
			input.problemID = (new Integer(problemID)).toString();
			Result result = new Result(input);
			Output output = new Output();
			output.initOutput( input );

 			if( input.storeCoalitionValuesInFile )
				input.storeCoalitionValuesInFile( problemID );
			
 			 
			 if ( (input.solverName == SolverNames.IP) || (input.solverName == SolverNames.ODPIP)  || (input.solverName == SolverNames.ODPinParallelWithIP) ){
				IPSolver ipSolver = new IPSolver(); ipSolver.solve( input, output, result );
			}
			if( input.numOfRunningTimes == 1 ) {
				return( result );
			}else{
				computeErrorBars.addResults( result, input );
				if( problemID < input.numOfRunningTimes ){
					if( input.readCoalitionValuesFromFile )
						input.readCoalitionValuesFromFile( problemID+1 );
					else
						input.generateCoalitionValues();
				}
				System.out.println(input.numOfAgents+" agents, "+ValueDistribution.toString(input.valueDistribution)+" distribution. The solver just finished solving "+input.problemID+" problems out of  "+input.numOfRunningTimes);
				if ( input.solverName == SolverNames.InclusionExclusion ) System.out.println("runtime for Inclusion-Exclusion (in milliseconds) was: "+result.inclusionExclusionTime);
			}
		}		
		Result averageResult = computeErrorBars.setAverageResultAndConfidenceIntervals( input );
		return( averageResult );
	}
}