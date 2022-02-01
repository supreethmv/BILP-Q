package inputOutput;
import general.General;
import general.Combinations;
import general.RandomPartition;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Random;
import java.util.TreeSet;
import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.BetaDistributionImpl;
import org.apache.commons.math.distribution.ExponentialDistributionImpl;
import org.apache.commons.math.distribution.GammaDistributionImpl;
public class Input
{
 	public final int maxAllowedCoalitionValue = Integer.MAX_VALUE; 
 	public final boolean ipUsesBranchAndBound = true;
	public final boolean ipPruneSubspaces = true; 
 	public final boolean printPercentageOf_v_equals_f = false; 
 	public final boolean printDistributionOfThefTable = false; 
 	public final boolean printCoalitionValueDistribution = false; 
 	public final boolean printCoalitionStructureValueDistribution = false; 
 	public final boolean printTheSubspaceThatIsCurrentlyBeingSearched = false; 
 	public final boolean printTheCoalitionsWhenPrintingTheirValues = true;
	public final boolean useSamplingWhenSearchingSubspaces = false; 
 	public final boolean samplingDoneInGreedyFashion = false;
	public final boolean useEfficientImplementationOfIDP = true; 
 	private final double sigma = 0.1; 
	public boolean odpipUsesLocalBranchAndBound=true; 
 	public boolean odpipSearchesMultipleSubspacesSimultaneiously=true; 
	public SolverNames solverName;
	public boolean readCoalitionValuesFromFile;
	public boolean storeCoalitionValuesInFile;
 	public boolean orderIntegerPartitionsAscendingly; 
 	public double  acceptableRatio; 
 	public boolean printDetailsOfSubspaces; 
 	public boolean printNumOfIntegerPartitionsWithRepeatedParts;
	public boolean printInterimResultsOfIPToFiles; 
 	public boolean printTimeTakenByIPForEachSubspace; 
 	public TreeSet<Integer> feasibleCoalitions;
	public int numOfAgents;
	public long numOfRunningTimes;	
	public double[] coalitionValues;
	public String outputFolder;
	public ValueDistribution valueDistribution;
	public String folderInWhichCoalitionValuesAreStored = "E:/coalitionValues"; 
	public String problemID = ""; 
	public void initInput()
	{
		feasibleCoalitions = null; 
 		numOfRunningTimes = 1; 
 		storeCoalitionValuesInFile = false; 
 		printDetailsOfSubspaces = false;
		valueDistribution = ValueDistribution.UNKNOWN;
	}
	public double getCoalitionValue( int coalitionInBitFormat ){
		return( coalitionValues[ coalitionInBitFormat ]);
	}
	public double getCoalitionValue( int[] coalitionInByteFormat ){
		int coalitionInBitFormat = Combinations.convertCombinationFromByteToBitFormat( coalitionInByteFormat );
		return( getCoalitionValue( coalitionInBitFormat ) );
	}
	public double getCoalitionStructureValue( int[][] coalitionStructure ){
		double valueOfCS = 0;
		for(int i=0; i<coalitionStructure.length; i++)
				valueOfCS += getCoalitionValue( coalitionStructure[i] );
		return( valueOfCS );
	}	
	public double getCoalitionStructureValue( int[] coalitionStructure ){
		double valueOfCS = 0;
		for(int i=0; i<coalitionStructure.length; i++)
			valueOfCS += getCoalitionValue( coalitionStructure[i] );
		return( valueOfCS );
	}	
	public void generateCoalitionValues()
	{
 		double maxCoalitionValue = Double.MIN_VALUE;
		Random random = new Random();
		long time = System.currentTimeMillis();
		if(( coalitionValues == null )||( coalitionValues.length != (int)Math.pow(2,numOfAgents) ))
			coalitionValues = new double[ (int)Math.pow(2,numOfAgents) ];
		coalitionValues[0] = 0;  
 		double[] agentStrength_normal  = new double[numOfAgents];
		double[] agentStrength_uniform = new double[numOfAgents];
		for( int agent=1; agent<=numOfAgents; agent++){
			agentStrength_normal [agent-1] = Math.max( 0, General.getRandomNumberFromNormalDistribution( 10,sigma,random) );
			agentStrength_uniform[agent-1] = random.nextDouble() * 10;
		}
 		if( valueDistribution == ValueDistribution.UNIFORM )
			for(int coalition = coalitionValues.length-1; coalition>0; coalition--){
				coalitionValues[ coalition ] = Math.round( (Integer.bitCount(coalition) * random.nextDouble()) * 10000000 );
				if( maxCoalitionValue < coalitionValues[ coalition ] ) maxCoalitionValue = coalitionValues[ coalition ]; 
			}
 		if( valueDistribution == ValueDistribution.NORMAL )
			for(int coalition = coalitionValues.length-1; coalition>0; coalition--){
				coalitionValues[ coalition ] = Math.max( 0, Integer.bitCount(coalition) * General.getRandomNumberFromNormalDistribution( 10,sigma,random) );
				coalitionValues[ coalition ] = Math.round( coalitionValues[ coalition ] * 10000000 );
				if( maxCoalitionValue < coalitionValues[ coalition ] ) maxCoalitionValue = coalitionValues[ coalition ];
			}
 		if( valueDistribution == ValueDistribution.MODIFIEDUNIFORM )
			for(int coalition = coalitionValues.length-1; coalition>0; coalition--){
				coalitionValues[ coalition ] = random.nextDouble()*10*Integer.bitCount(coalition);
				int probability = random.nextInt(100);
				if(probability <=20) coalitionValues[ coalition ] += random.nextDouble() * 50;
				coalitionValues[ coalition ] = Math.round( coalitionValues[ coalition ] * 10000000 );
				if( maxCoalitionValue < coalitionValues[ coalition ] ) maxCoalitionValue = coalitionValues[ coalition ];
			}
 		if( valueDistribution == ValueDistribution.MODIFIEDNORMAL )
			for(int coalition = coalitionValues.length-1; coalition>0; coalition--){
				coalitionValues[ coalition ] = Math.max( 0, Integer.bitCount(coalition) * General.getRandomNumberFromNormalDistribution( 10,sigma,random) );
				int probability = random.nextInt(100);
 				if(probability <=20) coalitionValues[ coalition ] += General.getRandomNumberFromNormalDistribution( 25,sigma,random);
				coalitionValues[ coalition ] = Math.round( coalitionValues[ coalition ] * 10000000 );
				if( maxCoalitionValue < coalitionValues[ coalition ] ) maxCoalitionValue = coalitionValues[ coalition ];
			}
 		if( valueDistribution == ValueDistribution.EXPONENTIAL ){
			ExponentialDistributionImpl exponentialDistributionImpl = new ExponentialDistributionImpl(1);
			for(int coalition = coalitionValues.length-1; coalition>0; coalition--){
				boolean repeat = false;
				do{
					try { coalitionValues[ coalition ] = Math.max( 0, Integer.bitCount(coalition) * exponentialDistributionImpl.sample() );
					} catch (MathException e) { repeat = true; }
				}while( repeat == true );
				coalitionValues[ coalition ] = Math.round( coalitionValues[ coalition ] * 10000000 );
				if( maxCoalitionValue < coalitionValues[ coalition ] ) maxCoalitionValue = coalitionValues[ coalition ];
			}
		}
 		if( valueDistribution == ValueDistribution.BETA ){
			BetaDistributionImpl betaDistributionImpl =new BetaDistributionImpl(0.5, 0.5);
			for(int coalition = coalitionValues.length-1; coalition>0; coalition--){
				boolean repeat = false;
				do{
					try { coalitionValues[ coalition ] = Math.max( 0, Integer.bitCount(coalition) * betaDistributionImpl.sample() );
					} catch (MathException e) { repeat = true; }
				}while( repeat == true );
				coalitionValues[ coalition ] = Math.round( coalitionValues[ coalition ] * 10000000 );
				if( maxCoalitionValue < coalitionValues[ coalition ] ) maxCoalitionValue = coalitionValues[ coalition ];
			}
		}
 		if( valueDistribution == ValueDistribution.GAMMA ){
			GammaDistributionImpl gammaDistributionImpl = new GammaDistributionImpl(2, 2);
			for(int coalition = coalitionValues.length-1; coalition>0; coalition--){
				boolean repeat = false;
				do{
					try { coalitionValues[ coalition ] = Math.max( 0, Integer.bitCount(coalition) * gammaDistributionImpl.sample() );
					} catch (MathException e) { repeat = true; }
				}while( repeat == true );
				coalitionValues[ coalition ] = Math.round( coalitionValues[ coalition ] * 10000000 );
				if( maxCoalitionValue < coalitionValues[ coalition ] ) maxCoalitionValue = coalitionValues[ coalition ];
			}
		}
 		if( valueDistribution == ValueDistribution.AGENTBASEDUNIFORM )
			for(int coalition = coalitionValues.length-1; coalition>0; coalition--){
				int[] members = Combinations.convertCombinationFromBitToByteFormat(coalition, numOfAgents);
 				double percentage = 100;
				coalitionValues[ coalition ] = 0;
				for( int m=0; m<Integer.bitCount(coalition); m++){
					double rangeSize = (percentage/(double)100) * agentStrength_uniform[members[m]-1] * 2;
					double startOfRange = ((100 - percentage)/(double)100) * agentStrength_uniform[members[m]-1];
					coalitionValues[ coalition ] += startOfRange + (random.nextDouble()*rangeSize);
				}
				coalitionValues[ coalition ] = Math.round( coalitionValues[ coalition ] * 10000000 );
				if( maxCoalitionValue < coalitionValues[ coalition ] ) maxCoalitionValue = coalitionValues[ coalition ];
			}		
 		if( valueDistribution == ValueDistribution.AGENTBASEDNORMAL )
			for(int coalition = coalitionValues.length-1; coalition>0; coalition--){
				int[] members = Combinations.convertCombinationFromBitToByteFormat(coalition, numOfAgents);
				coalitionValues[ coalition ] = 0;
				for( int m=0; m<Integer.bitCount(coalition); m++){
					double newValue;
					newValue = General.getRandomNumberFromNormalDistribution(agentStrength_normal[members[m]-1], sigma, random);
					coalitionValues[ coalition ] += Math.max( 0, newValue );
				}
				coalitionValues[ coalition ] = Math.round( coalitionValues[ coalition ] * 10000000 );	
				if( maxCoalitionValue < coalitionValues[ coalition ] ) maxCoalitionValue = coalitionValues[ coalition ];
			}
 		if( valueDistribution == ValueDistribution.NDCS )
			for(int coalition = coalitionValues.length-1; coalition>0; coalition--){
				do{ 
					coalitionValues[ coalition ] = (General.getRandomNumberFromNormalDistribution(Integer.bitCount(coalition),Math.sqrt(Integer.bitCount(coalition)),random));
				}while( coalitionValues[ coalition ] <= 0 );
				coalitionValues[ coalition ] = Math.round( coalitionValues[ coalition ] * 10000000 );	
				if( maxCoalitionValue < coalitionValues[ coalition ] ) maxCoalitionValue = coalitionValues[ coalition ];
			}
 		if( maxCoalitionValue > maxAllowedCoalitionValue )
			for(int c=coalitionValues.length-1; c>=0; c--)  
				coalitionValues[c] = (int)Math.round(  coalitionValues[c]  *  maxAllowedCoalitionValue / maxCoalitionValue  );
		System.out.println(numOfAgents+" agents, "+ValueDistribution.toString(valueDistribution)+" distribution. The time required to generate the coalition values (in milli second): "+(System.currentTimeMillis()-time));
 		if( printCoalitionValueDistribution ) printCoalitionValueDistribution();
 		if( printCoalitionStructureValueDistribution ) printCoalitionStructureValueDistribution( numOfAgents );
	}	
	public void storeCoalitionValuesInFile( int problemID )
	{
 		General.createFolder( folderInWhichCoalitionValuesAreStored );
 		String filePathAndName = folderInWhichCoalitionValuesAreStored;
		filePathAndName += "/"+numOfAgents+"Agents_"+ValueDistribution.toString(valueDistribution)+"_"+problemID+".txt";
		General.clearFile( filePathAndName );
 		StringBuffer tempStringBuffer = new StringBuffer();
		for(int coalition = 0; coalition<coalitionValues.length; coalition++)
			tempStringBuffer.append( coalitionValues[ coalition ]+"\n");
 		General.printToFile( filePathAndName, tempStringBuffer.toString(), false);
		tempStringBuffer.setLength(0);
	}
	public void readCoalitionValuesFromFile( int problemID )
	{
		coalitionValues = new double[ (int)Math.pow(2,numOfAgents) ];
 		String filePathAndName = folderInWhichCoalitionValuesAreStored;
		filePathAndName += "/"+numOfAgents+"Agents_"+ValueDistribution.toString(valueDistribution)+"_"+problemID+".txt";
		try{
			BufferedReader bufferedReader = new BufferedReader( new FileReader(filePathAndName) );
			for(int coalition = 0; coalition<coalitionValues.length; coalition++)
				coalitionValues[ coalition ] = (new Double(bufferedReader.readLine())).doubleValue();			
			bufferedReader.close();
		}
		catch (Exception e){
			System.out.println(e);
		}		
	}	
	public void readCoalitionValuesFromFile( String fileName )
	{
		coalitionValues = new double[ (int)Math.pow(2,numOfAgents) ];
 		String filePathAndName = folderInWhichCoalitionValuesAreStored;
		filePathAndName += "/"+fileName;
		try{
			BufferedReader bufferedReader = new BufferedReader( new FileReader(filePathAndName) );
			for(int coalition = 0; coalition<coalitionValues.length; coalition++)
				coalitionValues[ coalition ] = (new Double(bufferedReader.readLine())).doubleValue();			
			bufferedReader.close();
		}
		catch (Exception e){
			System.out.println(e);
		}		
	}	
	public void printCoalitionValueDistribution()
	{
 		int[] counter = new int[40];
		for(int i=0; i<counter.length; i++){
			counter[i]=0;
		}		
 		long min = Integer.MAX_VALUE;
		long max = Integer.MIN_VALUE;
		for(int coalition=1; coalition<coalitionValues.length; coalition++){
			long currentWeightedValue = (long)Math.round( coalitionValues[coalition] / Integer.bitCount(coalition) );
			if( min > currentWeightedValue )
				min = currentWeightedValue ;
			if( max < currentWeightedValue )
				max = currentWeightedValue ;
		}
		System.out.println("The maximum weighted coalition value (i.e., value of coalition divided by size of that coalition) is  "+max+"  and the minimum one is  "+min);
 		for(int coalition=1; coalition<coalitionValues.length; coalition++){
			long currentWeightedValue = (long)Math.round( coalitionValues[coalition] / Integer.bitCount(coalition) );
			int percentageOfMax = (int)Math.round( (currentWeightedValue-min) * (counter.length-1) / (max-min) );
			counter[percentageOfMax]++;
		}
 		System.out.println("The distribution of the weighted coalition values (i.e., every value of coalition is divided by size of that coalition) is:");
		System.out.print(ValueDistribution.toString(valueDistribution)+"_coalition = [");
		for(int i=0; i<counter.length; i++)
			System.out.print(counter[i]+" ");
		System.out.println("]");
	}
	public void printCoalitionStructureValueDistribution( int n )
	{
		RandomPartition randomPartition = new RandomPartition(n);
		int numOfSamples = 10000000;		
		long[] sampleValues = new long[numOfSamples];
		for(int i=0; i<numOfSamples; i++){
			int[] currentCoalitionStructure = randomPartition.get();
			long value = 0;
			for(int j=0; j<currentCoalitionStructure.length; j++)
				value += coalitionValues[ currentCoalitionStructure[j] ];
 			sampleValues[i] = value;
		}
 		int[] counter = new int[200];
		for(int i=0; i<counter.length; i++){
			counter[i]=0;
		}		
 		long min = Integer.MAX_VALUE;
		long max = Integer.MIN_VALUE;
		for(int i=1; i<sampleValues.length; i++){
			long currentValue = sampleValues[i];
			if( min > currentValue )
				min = currentValue ;
			if( max < currentValue )
				max = currentValue ;
		}
		System.out.println("The maximum weighted coalition value (i.e., value of coalition divided by size of that coalition) is  "+max+"  and the minimum one is  "+min);
 		for(int i=1; i<sampleValues.length; i++){
			long currentValue = sampleValues[i];
			int percentageOfMax = (int)Math.round( (currentValue-min) * (counter.length-1) / (max-min) );
			counter[percentageOfMax]++;
		}
 		System.out.println("The distribution of the weighted coalition STRUCTURE values  (i.e., every value of coalition is divided by size of that coalition) is:");
		System.out.print(ValueDistribution.toString(valueDistribution)+"_structure = [");
		for(int i=0; i<counter.length; i++)
			System.out.print(counter[i]+" ");
		System.out.println("]");
	}
}