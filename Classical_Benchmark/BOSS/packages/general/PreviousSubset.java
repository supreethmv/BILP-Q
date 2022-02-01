package general;

public class PreviousSubset {
	
	int h;
	int m;
	final int n;
	final int k;
	public PreviousSubset(int n, int k){
		this.h=k;
		this.m=0;
		this.n=n;
		this.k=k;
	}
	public void getPreviousCombination(int[] a)
	{

            if( m < n-h)
			h=1;
		else
			h++;
		m = a[k-h];

		int x=k-h-1;
		
		for(int j=1; j<=h; j++)
			a[x+j]=m+j;
	}
	
        
}
