package ipSolver;
import general.IntegerPartition;
public class IntegerPartitionGraph
{
	public Node[][] nodes;	
	public int largestIntegerBeingSplitInThisGraph;
	public IntegerPartitionGraph( Subspace[][] subspaces, int numOfAgents, int largestIntegerBeingSplitInThisGraph )
	{
		this.largestIntegerBeingSplitInThisGraph = largestIntegerBeingSplitInThisGraph;
		nodes = new Node[ numOfAgents ][];
		for(int level=0; level<numOfAgents; level++){  
			nodes[level] = new Node[ subspaces[level].length ];
			for(int i=0; i<subspaces[level].length; i++){  
				nodes[level][i] = new Node( subspaces[level][i] );
			}
		}
		for(int level=0; level<numOfAgents - 1; level++){  
			for(int i=0; i<nodes[level].length; i++)  
			{
				IntegerPartition[] listOfDirectlyConnectedIntegerPartitions = nodes[level][i].integerPartition.getListOfDirectedlyConnectedIntegerPartitions( largestIntegerBeingSplitInThisGraph, 0 );
				if( listOfDirectlyConnectedIntegerPartitions == null ){
					nodes[level][i].edgesFromThisNode = null;
				}else{
					nodes[level][i].edgesFromThisNode = new Edge[ listOfDirectlyConnectedIntegerPartitions.length ];
					int index=0;			
					for(int j=0; j<nodes[level+1].length; j++){  
						int[] integersThatResultedFromTheSplit = getIntegersThatResultedFromTheSplit( nodes[level][i], listOfDirectlyConnectedIntegerPartitions, nodes[level+1][j], largestIntegerBeingSplitInThisGraph );
						if( integersThatResultedFromTheSplit != null )
						{
							int[] sortedParts1 = nodes[level  ][i].integerPartition.partsSortedAscendingly; 
							int[] sortedParts2 = nodes[level+1][j].integerPartition.partsSortedAscendingly;
							int partThatWasSplit= -1;
							for(int k=sortedParts1.length-1; k>=0; k--){  
								if( sortedParts1[k] != sortedParts2[k+1]){
									partThatWasSplit = sortedParts1[k];
									break;
								}
							}
							nodes[level][i].edgesFromThisNode[ index ] = new Edge( nodes[level+1][j], partThatWasSplit, integersThatResultedFromTheSplit );												
							index++;
							if( index == nodes[level][i].edgesFromThisNode.length )
								break;
						}
					}
				}
			}
		}				
	}
	public void updateEdges( int numOfAgents, int largestIntegerBeingSplitInThisGraph )
	{
		int prev_largestIntegerBeingSplitInThisGraph = this.largestIntegerBeingSplitInThisGraph;
		if( prev_largestIntegerBeingSplitInThisGraph >= largestIntegerBeingSplitInThisGraph )
			return;
		for(int level=1; level<numOfAgents - 1; level++){  
			for(int i=0; i<nodes[level].length; i++)  
			{
				IntegerPartition[] listOfDirectlyConnectedIntegerPartitions = nodes[level][i].integerPartition.getListOfDirectedlyConnectedIntegerPartitions( largestIntegerBeingSplitInThisGraph, prev_largestIntegerBeingSplitInThisGraph );
				if( listOfDirectlyConnectedIntegerPartitions != null )
				{
					int index;
					if( nodes[level][i].edgesFromThisNode == null ){  
						index = 0;
						nodes[level][i].edgesFromThisNode = new Edge[ listOfDirectlyConnectedIntegerPartitions.length ];
					}else{  
						index = nodes[level][i].edgesFromThisNode.length;
						Edge[] tempListOfEdges = new Edge[ nodes[level][i].edgesFromThisNode.length + listOfDirectlyConnectedIntegerPartitions.length ]; 
						for(int j=0; j<nodes[level][i].edgesFromThisNode.length; j++)
							tempListOfEdges[j] = nodes[level][i].edgesFromThisNode[j];
						nodes[level][i].edgesFromThisNode = tempListOfEdges;						
					}
					for(int j=0; j<nodes[level+1].length; j++){  
						int[] integersThatResultedFromTheSplit = getIntegersThatResultedFromTheSplit( nodes[level][i], listOfDirectlyConnectedIntegerPartitions, nodes[level+1][j], largestIntegerBeingSplitInThisGraph ); 
						if( integersThatResultedFromTheSplit != null )
						{
							int[] sortedParts1 = nodes[level  ][i].integerPartition.partsSortedAscendingly; 
							int[] sortedParts2 = nodes[level+1][j].integerPartition.partsSortedAscendingly;
							int partThatWasSplit= -1;
							for(int k=sortedParts1.length-1; k>=0; k--){  
								if( sortedParts1[k] != sortedParts2[k+1]){
									partThatWasSplit = sortedParts1[k];
									break;
								}
							}
							nodes[level][i].edgesFromThisNode[ index ] = new Edge( nodes[level+1][j], partThatWasSplit, integersThatResultedFromTheSplit );												
							index++;
							if( index == nodes[level][i].edgesFromThisNode.length )
								break;
						}
					}
				}
			}
		}
		this.largestIntegerBeingSplitInThisGraph = largestIntegerBeingSplitInThisGraph;
	}
	private int[] getIntegersThatResultedFromTheSplit( Node nodeOnLowLevel, IntegerPartition[] listOfDirectlyConnectedIntegerPartitions,
			Node nodeOnHighLevel, int largestIntegerBeingSplitInThisGraph )
	{
		int[] multiplicity1  = nodeOnHighLevel.integerPartition.sortedMultiplicity;
		int   underlyingSet1 = nodeOnHighLevel.integerPartition.sortedUnderlyingSetInBitFormat;
		for(int i=0; i<listOfDirectlyConnectedIntegerPartitions.length; i++)
		{
			int[] multiplicity2  = listOfDirectlyConnectedIntegerPartitions[i].sortedMultiplicity;
			int   underlyingSet2 = listOfDirectlyConnectedIntegerPartitions[i].sortedUnderlyingSetInBitFormat;
			if( underlyingSet1 != underlyingSet2 )
				continue;
			boolean notEqual = false;
			for(int j=0; j<multiplicity1.length; j++){
				if( multiplicity1[j] != multiplicity2[j] ){
					notEqual = true;
					break;
				}
			}
			if( notEqual )
				continue;
			return( listOfDirectlyConnectedIntegerPartitions[i].tempIntegersThatResultedFromASplit );
		}
		return( null );
	}
	public void setReachabilityOfSubspaces( int m, int numOfAgents )
	{
 		for(int level=0; level<2; level++)
			for(int i=0; i<nodes[level].length; i++)
				nodes[level][i].subspace.isReachableFromBottomNode = true;
		for(int level = 2; level<numOfAgents; level++)
			for(int i=0; i<nodes[level].length; i++)
				nodes[level][i].subspace.isReachableFromBottomNode = false;
 		for(int level = 1; level<numOfAgents-1; level++){
			for(int i=0; i<nodes[level].length; i++){
				Node curNode = nodes[level][i];
				if( curNode.edgesFromThisNode != null )
					for(int j=0; j<curNode.edgesFromThisNode.length; j++){
						if((curNode.edgesFromThisNode[j] != null )&&( curNode.edgesFromThisNode[j].partThatWasSplit >= m ))
							curNode.edgesFromThisNode[j]  = null;
					}
			}
		}
 		for(int level=1; level<numOfAgents-1; level++){
			for(int i=0; i<nodes[level].length; i++){
				Node curNode = nodes[level][i];
				if( curNode.subspace.isReachableFromBottomNode == false ){
					continue;
				}
				if( curNode.edgesFromThisNode != null )
					for(int j=0; j<curNode.edgesFromThisNode.length; j++){
						if( curNode.edgesFromThisNode[j] != null )
							curNode.edgesFromThisNode[j].node.subspace.isReachableFromBottomNode = true;					
					}
			}
		}
	}
	public Node[] getReachableNodes( Node node )
	{
		if( node.edgesFromThisNode == null ) return( null );
		int numOfIntegersInNode = node.integerPartition.partsSortedAscendingly.length;
 		node.tempIntegerRoots = null;
		for(int level=numOfIntegersInNode; level<nodes.length; level++) {
			for(int i=0; i<nodes[level].length; i++){
				nodes[level][i].tempFlag = false;				
				nodes[level][i].tempIntegerRoots = null;
			}
		}
 		for( int i=0; i<node.edgesFromThisNode.length; i++ ){
			node.edgesFromThisNode[i].node.tempFlag = true;
			setIntegerRoots( node, node.edgesFromThisNode[i].node, node.edgesFromThisNode[i].twoPartsThatResultedFromTheSplit, node.edgesFromThisNode[i].partThatWasSplit ); 
		}
 		int numOfReachableNodes = 0;
		for(int level=numOfIntegersInNode; level<nodes.length-1; level++) {
			for(int i=0; i<nodes[level].length; i++){
				if( nodes[level][i].tempFlag ){
					numOfReachableNodes++;
					if( nodes[level][i].edgesFromThisNode != null ){
						for( int j=0; j<nodes[level][i].edgesFromThisNode.length; j++ ){
							if( nodes[level][i].edgesFromThisNode[j].node.tempFlag == false ){
								nodes[level][i].edgesFromThisNode[j].node.tempFlag = true;
								setIntegerRoots( nodes[level][i], nodes[level][i].edgesFromThisNode[j].node, nodes[level][i].edgesFromThisNode[j].twoPartsThatResultedFromTheSplit, nodes[level][i].edgesFromThisNode[j].partThatWasSplit );
							}
						}
					}
				}
			}
		}
 		int index=0;
		Node[] listOfReachableNodes = new Node[ numOfReachableNodes ];
		for(int level=numOfIntegersInNode; level<nodes.length-1; level++) {
			for(int i=0; i<nodes[level].length; i++){
				if( nodes[level][i].tempFlag ){
					listOfReachableNodes[index] = nodes[level][i];
					index++;
				}
			}
		}		
		return( listOfReachableNodes );
	}
	private void setIntegerRoots( Node lowerNode, Node upperNode, int[] twoPartsThatResultedFromTheSplit, int partThatWasSplit )
	{
		int[] upperIntegers = upperNode.integerPartition.partsSortedAscendingly;
		int[] lowerIntegers = lowerNode.integerPartition.partsSortedAscendingly;
 		upperNode.tempIntegerRoots = new int[ upperIntegers.length ];
		for(int i=0; i < upperNode.tempIntegerRoots.length; i++)
			upperNode.tempIntegerRoots[i] = -1;
		if( lowerNode.tempIntegerRoots == null )
		{
 			for(int k=0; k<twoPartsThatResultedFromTheSplit.length; k++)
				for(int j=0; j < upperIntegers.length; j++)
					if(( twoPartsThatResultedFromTheSplit[k] == upperIntegers[j] )&&( upperNode.tempIntegerRoots[j] == -1 )){
						upperNode.tempIntegerRoots[j] = partThatWasSplit;
						break;
					}
 			for(int j=0; j < upperIntegers.length; j++)
				if( upperNode.tempIntegerRoots[j] == -1 )
					upperNode.tempIntegerRoots[j] = upperIntegers[j];			
		}else{
 			int newRoot = -10;
			int indexOfNewRoot = -10;
 			for(int i=0; i<lowerIntegers.length; i++)
				if( lowerIntegers[i] == partThatWasSplit ){
					indexOfNewRoot = i;
					newRoot = lowerNode.tempIntegerRoots[i];
				}
 			for(int i=0; i < lowerNode.tempIntegerRoots.length; i++)
			{
				if( i == indexOfNewRoot ) continue;
				for(int j=0; j < upperIntegers.length; j++)
					if(( upperIntegers[j] == lowerIntegers[i] )&&( upperNode.tempIntegerRoots[j] == -1 )){
						upperNode.tempIntegerRoots[j] = lowerNode.tempIntegerRoots[i];
						break;
					}
			}
 			for(int j=0; j < upperIntegers.length; j++)
				if( upperNode.tempIntegerRoots[j] == -1 )
					upperNode.tempIntegerRoots[j] =  newRoot;
		}
	}
}