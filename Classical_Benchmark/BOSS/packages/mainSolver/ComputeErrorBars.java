package mainSolver;

import general.AvVar;
import general.IntegerPartition;
import inputOutput.Input;
import inputOutput.SolverNames;
import ipSolver.IntegerPartitionGraph;
import ipSolver.Node;
import ipSolver.Subspace;

 
public class ComputeErrorBars
{
	public AvVar ipTime, cplexTime, inclusionExclusionTime, ipValueOfBestCS, cplexValueOfBestCS, ipNumOfSearchedCS,
	ipNumOfSearchedCoalitions, ipTimeForScanningTheInput, ipUpperBoundOnOptimalValue;
	
	public AvVar[][] ipNumOfSearchedCoalitionsInSubspaces;

 	
	public ComputeErrorBars( Input input ) {
		ipTime    = new AvVar(); 
		cplexTime = new AvVar();
		inclusionExclusionTime = new AvVar();
		ipValueOfBestCS = new AvVar();
		cplexValueOfBestCS = new AvVar();
		ipNumOfSearchedCS  = new AvVar();
		ipNumOfSearchedCoalitions = new AvVar();
		ipTimeForScanningTheInput = new AvVar();
		ipUpperBoundOnOptimalValue= new AvVar();
		int[][][] integerPartitions = IntegerPartition.getIntegerPartitions( input.numOfAgents, input.orderIntegerPartitionsAscendingly);
		ipNumOfSearchedCoalitionsInSubspaces = new AvVar[ integerPartitions.length ][];
		for(int i=0; i<ipNumOfSearchedCoalitionsInSubspaces.length; i++)
		{
			ipNumOfSearchedCoalitionsInSubspaces[i] = new AvVar[ integerPartitions[i].length ];
			for(int j=0; j<ipNumOfSearchedCoalitionsInSubspaces[i].length; j++)
				ipNumOfSearchedCoalitionsInSubspaces[i][j] = new AvVar();
		}
	}
	
	 
	
	public void addResults( Result result, Input input )
	{
		if( input.solverName == SolverNames.InclusionExclusion ){
			inclusionExclusionTime.add( result.inclusionExclusionTime );
		}
		if(( input.solverName == SolverNames.IP )||( input.solverName == SolverNames.ODPIP )||( input.solverName == SolverNames.ODPinParallelWithIP ))
		{
			ipTime.add( result.ipTime );
			ipValueOfBestCS.add( result.get_ipValueOfBestCSFound() );
			ipNumOfSearchedCoalitions.add( result.ipNumOfExpansions );
			ipUpperBoundOnOptimalValue.add( result.ipUpperBoundOnOptimalValue );
			Node[][] nodes = result.ipIntegerPartitionGraph.nodes;
			for(int i=0; i<nodes.length; i++)
				for(int j=0; j<nodes[i].length; j++){
					Subspace curSubspace = nodes[i][j].subspace;
					ipNumOfSearchedCoalitionsInSubspaces[i][j].add( curSubspace.numOfSearchedCoalitionsInThisSubspace );
				}
		}
	}
	
	 
	public Result setAverageResultAndConfidenceIntervals( Input input )
	{
		Result result = new Result(input);
		if( input.solverName == SolverNames.InclusionExclusion )
		{
 			result.inclusionExclusionTime = (long) inclusionExclusionTime.average();

 			result.inclusionExclusionTime_confidenceInterval = 1.96 * (inclusionExclusionTime.stddev() / Math.sqrt(inclusionExclusionTime.num()));
		}
		if(( input.solverName == SolverNames.IP )||( input.solverName == SolverNames.ODPIP )||( input.solverName == SolverNames.ODPinParallelWithIP ))
		{
 			result.ipUpperBoundOnOptimalValue = ipUpperBoundOnOptimalValue.average();
			result.ipTimeForScanningTheInput = (long) ipTimeForScanningTheInput.average();
			result.ipNumOfExpansions = (long) ipNumOfSearchedCoalitions.average();
			result.ipTime = (long) ipTime.average();
			result.set_ipValueOfBestCSFound( ipValueOfBestCS.average() );

 			int[][][] integers=IntegerPartition.getIntegerPartitions(input.numOfAgents,input.orderIntegerPartitionsAscendingly);
			Subspace[][] subspaces = new Subspace[ integers.length ][];
			for(int level=0; level<input.numOfAgents; level++){
				subspaces[level]=new Subspace[ integers[level].length ];
				for(int i=0; i<integers[level].length; i++){
					subspaces[level][i] = new Subspace( integers[level][i] );
				}
			}	
			result.ipIntegerPartitionGraph = new IntegerPartitionGraph( subspaces, input.numOfAgents, ((int)Math.floor(2*input.numOfAgents/(double)3)));

 			Node[][] nodes = result.ipIntegerPartitionGraph.nodes;
			for(int i=0; i<nodes.length; i++)
				for(int j=0; j<nodes[i].length; j++)
				{
					AvVar temp = ipNumOfSearchedCoalitionsInSubspaces[i][j];
					Subspace curSubspace = nodes[i][j].subspace;
					curSubspace.numOfSearchedCoalitionsInThisSubspace = (long)temp.average();
				}

 			result.ipUpperBoundOnOptimalValue_confidenceInterval = 1.96 * ( ipUpperBoundOnOptimalValue.stddev() / Math.sqrt(ipUpperBoundOnOptimalValue.num()) );
			result.ipTimeForScanningTheInput_confidenceInterval = 1.96 * (ipTimeForScanningTheInput.stddev() / Math.sqrt(ipTimeForScanningTheInput.num()));
			result.ipNumOfExpansions_confidenceInterval = 1.96 * ( ipNumOfSearchedCoalitions.stddev() / Math.sqrt(ipNumOfSearchedCoalitions.num()) );		
			result.ipValueOfBestCS_confidenceInterval = 1.96 * ( ipValueOfBestCS.stddev() / Math.sqrt(ipValueOfBestCS.num()) );
			result.ipTime_confidenceInterval = 1.96 * ( ipTime.stddev() / Math.sqrt(ipTime.num()) );
			for(int i=0; i<nodes.length; i++)
				for(int j=0; j<nodes[i].length; j++)
				{	
					AvVar temp = ipNumOfSearchedCoalitionsInSubspaces[i][j];
					Subspace curSubspace = nodes[i][j].subspace;
					curSubspace.numOfSearchedCoalitionsInThisSubspace_confidenceInterval = 1.96 * ( temp.stddev() / Math.sqrt(temp.num()) );
				}
		}
		return( result );
	}
}