package general;
import java.util.Random;
public class RandomPermutation {
	private int n;
	private int[] availableNumbersToChooseFrom;
	public RandomPermutation(int n) {
		this.n = n;
		this.availableNumbersToChooseFrom = new int[n];
	}
 	public int[] get()
	{
		Random randomGenerator = new Random();
        for(int i=n-1; i>=0; i--){
			availableNumbersToChooseFrom[i] = i;
		}
		int[] permutation = new int[n];
        for(int i=n-1; i>=0; i--){
			int j = randomGenerator.nextInt(i+1);
			permutation[i] = availableNumbersToChooseFrom[ j ] + 1;
			availableNumbersToChooseFrom[ j ] = availableNumbersToChooseFrom[i]; 
        }
		return permutation;
	}
}