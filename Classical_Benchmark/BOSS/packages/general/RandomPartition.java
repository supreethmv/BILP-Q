package general;

import java.math.BigDecimal;
import java.util.Random;

public class RandomPartition {
	
	private int n;
	private double[][] probabilityOfNew;
	
	public RandomPartition(int n) {
		this.n = n;
		this.calculateWeights(n);
	}
	

        public int[] get() {
		int partition[] = new int[n];
		
		Random randomGenerator = new Random();
		int coalitionsCount = 1;
		partition[0] = 1;
		for(int i=1; i<n; ++i) {
			if (randomGenerator.nextDouble() < this.probabilityOfNew[i][coalitionsCount]) {
				partition[i] = coalitionsCount+1;
				++coalitionsCount;
			} else partition[i] = randomGenerator.nextInt(coalitionsCount)+1;
		}
		


                int[] coalitionStructure = new int[ coalitionsCount ];
		for(int i=0; i<coalitionsCount; i++){
			coalitionStructure[i]=0;
		}
		for(int i=0; i<n; i++){
			coalitionStructure[ partition[i]-1 ] += (1<<i); 

                }
		
		return coalitionStructure;
	}
	
	private void calculateWeights(int n) {
		BigDecimal[][] weights = new BigDecimal[n+1][n+1];
		double[][] weightsDouble = new double[n+1][n+1];
		this.probabilityOfNew = new double[n+1][n+1];
		
		weights[0][0] = BigDecimal.ONE;
		weightsDouble[0][0] = 1;
		BigDecimal bellNInverse = BigDecimal.ONE.divide(this.getBellNumber(n), 500, BigDecimal.ROUND_UP);
		for(int i=1; i<=n; ++i) {
			weights[n][i] = bellNInverse;
			weightsDouble[n][i] = weights[n][i].doubleValue();
		}
		
		for(int j=n-1; j>=1; --j) {
			for(int k=1; k<=j; ++k) {
				weights[j][k] = weights[j+1][k].multiply(new BigDecimal(k)).add(weights[j+1][k+1]);
				weightsDouble[j][k] = weights[j][k].doubleValue();
				this.probabilityOfNew[j][k] = weights[j+1][k+1].divide(weights[j][k], 500, BigDecimal.ROUND_UP).doubleValue();
			}
		}
	}
	
	private BigDecimal getBellNumber(int n) {
		BigDecimal[] binomials;
		BigDecimal[] bell = new BigDecimal[n+1];
		bell[0] = BigDecimal.ONE;
		bell[1] = BigDecimal.ONE;
				
		for(int i=2; i<=n; ++i) {
			bell[i] = BigDecimal.ZERO;
			binomials = computeBinomials(i-1); 
			for(int k=0; k<i; ++k) bell[i] = bell[i].add(bell[k].multiply(binomials[k]));
		}
		
		return bell[n];
	}
	
	private BigDecimal[] computeBinomials(int n) {
		BigDecimal[] binomials = new BigDecimal[n+1];
		
		binomials[0] = BigDecimal.ONE;
		for(int i=1; i<=Math.floor(n/2); ++i) {
			binomials[i] = binomials[i-1].multiply(new BigDecimal(n-i+1)).divide(new BigDecimal(i), 100, BigDecimal.ROUND_UP);
		}
		for(int i=(int)Math.floor(n/2)+1; i<=n; ++i) binomials[i] = binomials[n-i];
		
		return binomials; 
	}
	
	
}
