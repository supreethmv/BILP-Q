package general;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.Random;

public class General
{
	 
	public static long factorial( int n )
	{
	    long n_factorial=1;
	    for( int i=1; i<=n; i++ )
	        n_factorial = n_factorial*i;
	    return(n_factorial);
	}
	 
	public static int getMultiplicity( int element, int[] arrayOfElements )
	{
	    int result=0;
	    for(int j=0; j<arrayOfElements.length; j++)
	        if( arrayOfElements[j]==element ) result++;
	    return(result);
	}
	
	 
	public static int[] convertMultisetToArray( ElementOfMultiset[] multiset )
	{
 		int counter=0;
		for(int i=0; i<multiset.length; i++)
			counter += multiset[i].repetition;
		
		int[] array = new int[ counter ];
		int index = 0;
		for(int i=0; i<multiset.length; i++){
			for(int j=0; j<multiset[i].repetition; j++){
				array[ index ] = multiset[i].element;
				index++;
			}
		}				
		return( array );
	}
	
	 
	public static ElementOfMultiset[] convertArrayToMultiset( int[] array )
	{
		int[] underlyingSet = getUnderlyingSet( array );
		
		ElementOfMultiset[] multiset = new ElementOfMultiset[ underlyingSet.length ];
		for(int i=0; i<multiset.length; i++){
			multiset[i] = new ElementOfMultiset( underlyingSet[i], 0);
			for(int j=0; j<array.length; j++)
				if( multiset[i].element == array[j] )
					multiset[i].repetition++;
		}					
		return( multiset );
	}
	
	 
	public static int getCardinalityOfMultiset( ElementOfMultiset[] multiset )
	{
		if( multiset == null ){
			return(0);
		}
		int counter = 0;
		for(int i=0; i<multiset.length; i++){
			counter += multiset[i].repetition;
		}
		return(counter);
	}
	 
	public static int[] setMinus( int[] array, ElementOfMultiset[] multiset )
	{
		int[] tempArray = General.copyArray( array );
		for(int i=0; i<tempArray.length; i++)
		{
			for(int j=0; j<multiset.length; j++){
				if(( tempArray[i] == multiset[j].element )&&( multiset[j].repetition > 0 )){
					tempArray[i] = -1;
					multiset[j].repetition--;
					break;
				}
			}
		}
 		int counter=0;
		for(int i=0; i<tempArray.length; i++){
			if( tempArray[i] > -1 ){
				counter++;
			}
		}
 		if( counter == 0 ){
			return( null );
		}else{
			int[] result = new int[ counter ];
			int index=0;
			for(int i=0; i<tempArray.length; i++)
				if( tempArray[i] > -1 ){
					result[index] = tempArray[i];
					index++;
				}
			return( result );
		}
	}
	
	 
	public static ElementOfMultiset[] setMinus( ElementOfMultiset[] multiset1, ElementOfMultiset[] multiset2 )
	{
		ElementOfMultiset[] tempMultiset = copyMultiset( multiset1 );
		for(int i=0; i<tempMultiset.length; i++)
		{
			for(int j=0; j<multiset2.length; j++){
				if( tempMultiset[i].element == multiset2[j].element ){
					tempMultiset[i].repetition -= multiset2[j].repetition;
					if( tempMultiset[i].repetition < 0 )
						tempMultiset[i].repetition = 0 ;
					break;
				}
			}
		}
 		int counter=0;
		for(int i=0; i<tempMultiset.length; i++){
			if( tempMultiset[i].repetition > 0 ){
				counter++;
			}
		}
 		if( counter == 0 ){
			return( null );
		}else{
			ElementOfMultiset[] result = new ElementOfMultiset[ counter ];
			int index=0;
			for(int i=0; i<tempMultiset.length; i++)
				if( tempMultiset[i].repetition > 0 ){
					result[index] = new ElementOfMultiset( tempMultiset[i].element, tempMultiset[i].repetition);
					index++;
				}
			return( result );
		}
	}
	
	 
	public static int[] getUnderlyingSet( int[] array )
	{
 		int numOfUniqueElements =0;
		int[] uniqueElements = new int[array.length];
		for(int i=0; i<uniqueElements.length; i++)
			uniqueElements[i]=0;
		
 		for(int i=0; i<array.length; i++)
		{
			boolean weHaveSeenThisElementBefore=false;
			for(int j=0; j<numOfUniqueElements; j++){
				if( uniqueElements[j]==array[i] ){
					weHaveSeenThisElementBefore=true;
					break;
				}								
			}
			if( weHaveSeenThisElementBefore==false){
				uniqueElements[ numOfUniqueElements ] = array[i];
				numOfUniqueElements++;
			}				
		}
 	    int[] underlyingSet = new int[numOfUniqueElements];
	    for(int i=0; i<numOfUniqueElements; i++)
	    	underlyingSet[i] = uniqueElements[i];
	    
	    return(underlyingSet);
	}
	 
	public String trimStringAfterChar( String string, char c)
	{
		int last;
		if(	string.indexOf(c) == -1 )
			last = string.length();
		else
			last = string.indexOf(c);
		return( string.substring(0,last) ); 				
	}
	
	 
	public static String setDecimalPrecision( double doubleValue, int precision)
	{
		String precisionString = "0.0";
		for(int i=2; i<=precision; i++)	{
			precisionString += "#";
		}
		return( (new DecimalFormat( precisionString )).format(doubleValue) );
	}	
	 
	public static boolean compareTwoArrays_ignoreElementOrder( int[] array1, int[] array2 )
	{
 		if( array1.length != array2.length ) { return( false );	}

 		int[] sortedArray1 = sortArray( array1, true );
		int[] sortedArray2 = sortArray( array2, true );		

 		int length = array1.length;
		for(int i=0; i<length; i++)	{
			if( sortedArray1[i] != sortedArray2[i] ) {
				return( false );
			}
		}		
 		return( true );
	}
	
	 
	public static boolean compareTwoArrays_considerElementOrder( int[] array1, int[] array2 )
	{
 		if( array1.length != array2.length ) { return( false ); }

 		int length = array1.length;
		for(int i=0; i<length; i++) {
			if( array1[i] != array2[i] ) {
				return( false );
			}
		}		
 		return( true );
	}
	
	 
	public static void randomizeElementsInArray( double[] array )
	{
 		Random r=new Random();
		double[] tempArray = new double[ array.length ];
		int[] availableIndices = new int[ array.length ];
		for(int i=0; i<availableIndices.length; i++ ) availableIndices[i] = i;
		
 		int indexInTemp=0;
		for( int max=array.length; max>=1; max-- )
		{
			int i = r.nextInt(max); 
 			int chosenIndex = availableIndices[ i ];
			tempArray[ indexInTemp ] = array[ chosenIndex ];
			indexInTemp++;

 			for( int j=i; j<=max-2; j++ )
				availableIndices[j] = availableIndices[j+1];
		}
		for( int i=0; i<array.length; i++ )	array[i] = tempArray[i];
	}	
	
	 
	public static double getRandomNumberFromGammaDistribution(double k, double theta, Random random) {
		boolean accept = false;
		if (k < 1) {
 			double c = (1 / k);
			double d = ((1 - k) * Math.pow(k, (k / (1 - k))));
			double u, v, z, e, x;
			do {
				u = random.nextDouble();
				v = random.nextDouble();
				z = -Math.log(u);
				e = -Math.log(v);
				x = Math.pow(z, c);
				if ((z + e) >= (d + x)) {
					accept = true;
				}
			} while (!accept);
			return (x * theta);
		} else {
 			double b = (k - Math.log(4));
			double c = (k + Math.sqrt(2 * k - 1));
			double lam = Math.sqrt(2 * k - 1);
			double cheng = (1 + Math.log(4.5));
			double u, v, x, y, z, r;
			do {
				u = random.nextDouble();
				v = random.nextDouble();
				y = ((1 / lam) * Math.log(v / (1 - v)));
				x = (k * Math.exp(y));
				z = (u * v * v);
				r = (b + (c * y) - x);
				if ((r >= ((4.5 * z) - cheng)) ||
						(r >= Math.log(z))) {
					accept = true;
				}
			} while (!accept);
			return (x * theta);
		}
	}
	
	 
	public static double getRandomNumberFromNormalDistribution( Random random )
	{		
		double U = random.nextDouble();
		double V = random.nextDouble();
		return Math.sin(2 * Math.PI * V) * Math.sqrt((-2 * Math.log(1 - U)));
	}
	  
	public static double getRandomNumberFromNormalDistribution(double mu, double sigma, Random random) {
		return mu + sigma * getRandomNumberFromNormalDistribution( random );
	}	
	
	 
	public static int[] sortArray( int[] array, boolean ascending )
	{
 		int[] sortedArray = new int[array.length];
		for(int i=0; i<array.length; i++)
			sortedArray[i] = array[i];
		
		if( ascending )  
		{
			for(int i = sortedArray.length - 1; i >= 0; i--)     
			{
				int highestIndex = i;         
				for(int j = i; j >= 0; j--)    
 				{
					if(sortedArray[highestIndex] < sortedArray[j])    

 						highestIndex = j;    
 				}
 				int temp = sortedArray[i];
				sortedArray[i] = sortedArray[highestIndex];
				sortedArray[highestIndex] = temp;
			}
		}
		else 
 		{
			for(int i = 0; i <= sortedArray.length - 1; i++)    
 			{
				int highestIndex = i;        
 				for(int j = i; j <= sortedArray.length - 1; j++)    
 				{
					if(sortedArray[highestIndex] < sortedArray[j])    
 						highestIndex = j;    
 				}
 				int temp = sortedArray[i];
				sortedArray[i] = sortedArray[highestIndex];
				sortedArray[highestIndex] = temp;
			}
		}
		return( sortedArray );
	}

	 
	public static int[][] sortArrayLexicographically( int[][] array, boolean ascending )
	{
 		String[] stringArray = new String[array.length];
		for(int i=0; i<array.length; i++){
			stringArray[i] = "";
			for(int j=0; j<array[i].length; j++)
				stringArray[i] += array[i][j];
		}
		if( ascending )  
		{
			for(int i = stringArray.length - 1; i >= 0; i--)     
			{
				int highestIndex = i;         
				for(int j = i; j >= 0; j--)    
 				{
					if( stringArray[highestIndex].compareTo(stringArray[j]) < 0 )    
 						highestIndex = j;    
 				}
 				String temp = stringArray[i];
				stringArray[i] = stringArray[highestIndex];
				stringArray[highestIndex] = temp;
			}
		}
		else  
		{
			for(int i = 0; i <= stringArray.length - 1; i++)    
 			{
				int highestIndex = i;        
 				for(int j = i; j <= stringArray.length - 1; j++)    
 				{
					if(stringArray[highestIndex].compareTo(stringArray[j]) < 0)    
 						highestIndex = j;    
 				}
 				String temp = stringArray[i];
				stringArray[i] = stringArray[highestIndex];
				stringArray[highestIndex] = temp;
			}
		}
 		int[][] sortedArray = new int[ stringArray.length ][];
		for(int i=0; i<sortedArray.length; i++)
		{
			sortedArray[i] = new int[ stringArray[i].length() ];
			for(int j=0; j<sortedArray[i].length; j++)
				sortedArray[i][j] = ( new Integer(stringArray[i].charAt(j)) ).intValue();				
		}
		return( sortedArray );
	}
 
	public static int[] removeElementOnceFromArray( int element, int[] array )
	{
 		int lengthOfArray = array.length;
		int[] newArray = new int[ lengthOfArray-1 ];
		
		 
		for(int i=0; i<lengthOfArray; i++)
		{
			if( array[i] == element )
			{
				int j=i+1;
				while( j < lengthOfArray )
				{
					newArray[j-1] = array[j];
					j++;
				}				
				break;
			}
			else newArray[i] = array[i];
		}
		return( newArray );
	}
	
	 
	public static int[] removeElementsOnceFromArray( int[] elements, int[] array )
	{
 		int lengthOfArray = array.length;
		int numOfElements = elements.length;
		int[] tempArray = new int[array.length];
		for(int i=0; i<array.length; i++)
			tempArray[i] = array[i];
		
		for( int k=0; k<numOfElements; k++)  
		{
			int curElement = elements[k];  
			for( int i=0; i<lengthOfArray; i++)
			{
				if( tempArray[i] == curElement )  
				{
					int j=i+1;
					while( j < lengthOfArray )
					{
						tempArray[j-1] = tempArray[j];
						j++;
					}				
					break;
				}
			}
		}
		
 		int lengthOfNewArray = lengthOfArray-numOfElements;
		int[] newArray = new int[ lengthOfNewArray ];
		for(int i=0; i<lengthOfNewArray; i++)
		{
			newArray[i] = tempArray[i];
		}
		return( newArray );
	}
	
	 
	public static String readFile( String filePathAndName )
	{
		try{
			BufferedReader bufferReader = new BufferedReader( new FileReader(filePathAndName) );
			String string="";
			String line;
			while( true )
			{
				line = bufferReader.readLine();
				if( line==null ) break;
				string += line;
			}
			bufferReader.close();
			return( string );
		}
		catch (Exception e){
			System.out.println(e);
			return( null );
		}
	}
	
	 
	public static void mergeTwoFiles( String firstFile, String secondFile, String mergedFile )
	{
		if(( (new String(mergedFile)).compareTo (firstFile) != 0 )&&( (new String(mergedFile)).compareTo (secondFile) != 0 )){
			clearFile( mergedFile );
		}
		if(( (new String(mergedFile)).compareTo (firstFile) == 0 )||( (new String(mergedFile)).compareTo (secondFile) != 0 )){
			try{
				BufferedReader bufferReader = new BufferedReader( new FileReader(secondFile) );
				String line;
				while( true ){
					line = bufferReader.readLine();
					if( line==null ) break;
					printToFile(mergedFile, line+"\n", false);
				}
				bufferReader.close();
			}
			catch (Exception e){ System.out.println(e); }
		}
		if(( (new String(mergedFile)).compareTo (secondFile) == 0 )||( (new String(mergedFile)).compareTo (firstFile) != 0 )){
			try{
				BufferedReader bufferReader = new BufferedReader( new FileReader(firstFile) );
				String line;
				while( true ){
					line = bufferReader.readLine();
					if( line==null ) break;
					printToFile(mergedFile, line+"\n", false);
				}
				bufferReader.close();
			}
			catch (Exception e){ System.out.println(e); }
		}
	}

	 
	public static void printToFile( String filePathAndName, String string, boolean erasePreviousContent )
	{
		boolean append = !erasePreviousContent;
		try{
			FileWriter file = new FileWriter(filePathAndName,append);
			file.write(string);
			file.close();
		}
		catch (Exception e){ System.out.println(e); }		
	}
	
	 
	public static void createFolder( String filePathAndName )
	{
		File folder = new File(filePathAndName);
		folder.mkdirs();
	}	
	 
	public static void clearFile( String filePathAndName )
	{
		try{
			FileWriter file = new FileWriter(filePathAndName);
			file.write("");
			file.close();
		}
		catch (Exception e){ System.out.println(e); }		
	}
	
 
	public static String convertArrayToString(byte[] array)
	{
		String tempStr="[";
		for( int i=0; i<array.length-1; i++ )
			tempStr += (new Byte(array[i]).toString())+", ";
		tempStr += (new Byte(array[ array.length-1 ]).toString())+"]";
		return( tempStr );
	}

	public static String convertArrayToString(int[] array)
	{
		String tempStr="[";
		for( int i=0; i<array.length-1; i++ )
			tempStr += (new Integer(array[i]).toString())+", ";
		tempStr += (new Integer(array[ array.length-1 ]).toString())+"]";
		return( tempStr );
	}
	
	public static String convertArrayToString(long[] array)
	{
		String tempStr="[";
		for( int i=0; i<array.length-1; i++ )
			tempStr += (new Long(array[i]).toString())+", ";
		tempStr += (new Long(array[ array.length-1 ]).toString())+"]";
		return( tempStr );
	}
	
	public static String convertArrayToString(double[] array)
	{
		String tempStr="[";
		for( int i=0; i<array.length-1; i++ )
			tempStr += (new Double(array[i]).toString())+", ";
		tempStr += (new Double(array[ array.length-1 ]).toString())+"]";
		return( tempStr );
	}	
	
 	
	public static String convertArrayToString( final byte[][] array )
	{
		String tempStr = "[";
		for( int i=0; i<array.length-1; i++)
		{
			tempStr += "[";
			for( int j=0; j<array[i].length-1; j++ ) {
				tempStr += (new Byte(array[i][j]).toString())+", ";
			}
			int j=array[i].length-1;
			tempStr += (new Byte(array[i][j]).toString())+"], ";
		}
		int i=array.length-1;
		tempStr += "[";
		for( int j=0; j<array[i].length-1; j++ ) {
			tempStr += (new Byte(array[i][j]).toString())+", ";
		}
		int j=array[i].length-1;
		tempStr += (new Byte(array[i][j]).toString())+"]]";
		return(tempStr);
	}
	
	public static String convertArrayToString( final int[][] array )
	{
		String tempStr = "[";
		for( int i=0; i<array.length-1; i++)
		{
			tempStr += "[";
			for( int j=0; j<array[i].length-1; j++ ) {
				tempStr += (new Integer(array[i][j]).toString())+", ";
			}
			int j=array[i].length-1;
			tempStr += (new Integer(array[i][j]).toString())+"], ";
		}
		int i=array.length-1;
		tempStr += "[";
		for( int j=0; j<array[i].length-1; j++ ) {
			tempStr += (new Integer(array[i][j]).toString())+", ";
		}
		int j=array[i].length-1;
		tempStr += (new Integer(array[i][j]).toString())+"]]";
		return(tempStr);
	}
	
	public static String convertArrayToString( final long[][] array )
	{
		String tempStr = "[";
		for( int i=0; i<array.length-1; i++) {
			tempStr += "[";
			for( int j=0; j<array[i].length-1; j++ ) {
				tempStr += (new Long(array[i][j]).toString())+", ";
			}
			int j=array[i].length-1;
			tempStr += (new Long(array[i][j]).toString())+"], ";
		}
		int i=array.length-1;
		tempStr += "[";
		for( int j=0; j<array[i].length-1; j++ ) {
			tempStr += (new Long(array[i][j]).toString())+", ";
		}
		int j=array[i].length-1;
		tempStr += (new Long(array[i][j]).toString())+"]]";
		return(tempStr);
	}
	
	public static String convertArrayToString( final double[][] array )
	{
		String tempStr = "[";
		for( int i=0; i<array.length-1; i++) {
			tempStr += "[";
			for( int j=0; j<array[i].length-1; j++ ) {
				tempStr += (new Double(array[i][j]).toString())+", ";
			}
			int j=array[i].length-1;
			tempStr += (new Double(array[i][j]).toString())+"], ";
		}
		int i=array.length-1;
		tempStr += "[";
		for( int j=0; j<array[i].length-1; j++ ) {
			tempStr += (new Double(array[i][j]).toString())+", ";
		}
		int j=array[i].length-1;
		tempStr += (new Double(array[i][j]).toString())+"]]";
		return(tempStr);
	}	
	
 	
	public static void printArray(String initialString, final byte[] array) {
		System.out.print( initialString + convertArrayToString( array ) );
	}	
	public static void printArray(String initialString, final int[] array) {
		System.out.print( initialString + convertArrayToString( array ) );
	}	
	public static void printArray(String initialString, final long[] array) {
		System.out.print( initialString + convertArrayToString( array ) );
	}	
	public static void printArray(String initialString, final double[] array) {
		System.out.print( initialString + convertArrayToString( array ) );
	}
	public static void printArray(String initialString, final byte[][] array) {
		System.out.print( initialString + convertArrayToString( array ) );
	}	
	public static void printArray(String initialString, final int[][] array) {
		System.out.print( initialString + convertArrayToString( array ) );
	}	
	public static void printArray(String initialString, final long[][] array) {
		System.out.print( initialString + convertArrayToString( array ) );
	}	
	public static void printArray(String initialString, final double[][] array) {
		System.out.print( initialString + convertArrayToString( array ) );
	}
	
 	
	public static byte[] copyArray(final byte[] array) {
		if( array == null )return(null);
		byte[] newArray = new byte[ array.length ];
		for(int i=0; i<array.length; i++) newArray[i] = array[i];
		return newArray;
	}	
	public static int[] copyArray(final int[] array) {
		if( array == null )return(null);
		int[] newArray = new int[ array.length ];
		for(int i=0; i<array.length; i++) newArray[i] = array[i];
		return newArray;		
	}	
	public static long[] copyArray(final long[] array) {
		if( array == null )return(null);
		long[] newArray = new long[ array.length ];
		for(int i=0; i<array.length; i++) newArray[i] = array[i];
		return newArray;
	}	
	public static double[] copyArray(final double[] array) {
		if( array == null )return(null);
		double[] newArray = new double[ array.length ];
		for(int i=0; i<array.length; i++) newArray[i] = array[i];
		return newArray;
	}
	public static byte[][] copyArray(final byte[][] array) {
		if( array == null )return(null);
		byte[][] newArray = new byte[ array.length ][];
		for(int i=0; i<array.length; i++){
			newArray[i] = new byte[ array[i].length ];
			for(int j=0; j<array[i].length; j++)
				newArray[i][j] = array[i][j];
		}return newArray;
	}	
	public static int[][] copyArray(final int[][] array) {
		if( array == null )return(null);
		int[][] newArray = new int[ array.length ][];
		for(int i=0; i<array.length; i++){
			newArray[i] = new int[ array[i].length ];
			for(int j=0; j<array[i].length; j++)
				newArray[i][j] = array[i][j];
		}return newArray;		
	}	
	public static long[][] copyArray(final long[][] array) {
		if( array == null )return(null);
		long[][] newArray = new long[ array.length ][];
		for(int i=0; i<array.length; i++){
			newArray[i] = new long[ array[i].length ];
			for(int j=0; j<array[i].length; j++)
				newArray[i][j] = array[i][j];
		}return newArray;		
	}	
	public static double[][] copyArray(final double[][] array) {
		if( array == null )return(null);
		double[][] newArray = new double[ array.length ][];
		for(int i=0; i<array.length; i++){
			newArray[i] = new double[ array[i].length ];
			for(int j=0; j<array[i].length; j++)
				newArray[i][j] = array[i][j];
		}return newArray;		
	}
	
	 
	public static String convertMultisetToString( ElementOfMultiset[] multiset )
	{
		String tempStr = "[";
		for(int i=0; i<multiset.length; i++)
			for(int j=0; j<multiset[i].repetition; j++)
				if(( i==multiset.length-1 )&&( j==multiset[i].repetition-1 ))
					tempStr += multiset[i].element;
				else
					tempStr += multiset[i].element + ", ";
		tempStr+="]";
		return( tempStr );
	}
	
	 
	public static ElementOfMultiset[] copyMultiset( ElementOfMultiset[] multiset )
	{
		if( multiset == null ) return( null );
		
		ElementOfMultiset[] copy = new ElementOfMultiset[ multiset.length ];
		for( int i=0; i<multiset.length; i++ ){
			copy[i] = new ElementOfMultiset( multiset[i].element, multiset[i].repetition );
		}
		return( copy );
	}
}