package general;
import java.lang.Math;
import java.util.*;
public class AvVar implements Cloneable {
	public boolean checkValidity = true;
	private long num;
	private double av;
	private double sav;
	private double min, max;
	private long indexMin, indexMax;
	private boolean doMedian = false;
	ArrayList values = null;
public AvVar() {
	init();
	return;
}
public AvVar(boolean doMedian) {
	init();
	this.doMedian = doMedian;
	if (doMedian)
		values = new ArrayList();
	return;
}
public void add(double d) {
	num++;
	av += d;
	sav += d*d;
	if (d<min) {
		min = d;
		indexMin = num;
	}
	if (d>max) {
		max = d;
		indexMax = num;
	}
	if (doMedian)
		values.add(new Double(d));
}
public double average() {
	if (num==0)
		return 0;
	else
		return av/num;
}
public Object clone() {
	AvVar clone = null;
	try {
		clone = (AvVar) super.clone();
	} catch (CloneNotSupportedException e) {
		System.err.println("CloneNotSupportedException in AvVar.clone: " + e.getMessage());
		System.exit(-1);
	}
	return clone;
}
public long indexMax() {
	return indexMax;
}
public long indexMin() {
	return indexMin;
}
public void init() {
	num = 0;
	av = 0.0;
	sav = 0.0;
	min = Double.MAX_VALUE;
	max = -Double.MAX_VALUE;  
}
public double max() {
	return max;
}
public double median() {
	if (num==0 || !doMedian)
		return 0;
	Object[] dvalues = values.toArray();
	Arrays.sort(dvalues);
 	if (num % 2 == 0)  
 		return (((Double)dvalues[(int)(num/2-1)]).doubleValue()+((Double)dvalues[(int)(num/2)]).doubleValue())/2.0;
 	else  
 		return ((Double)dvalues[(int)(num/2)]).doubleValue();
}
public double min() {
	return min;
}
public long num() {
	return num;
}
public double stddev() {
	double var = variance();
	double stddev = var;
	if ((var >= 0.0) || (checkValidity)) {
		stddev = Math.sqrt(var);
		if (Double.isNaN(stddev)) {
			stddev = 0.0;
		}
	}
	return stddev;
}
public String toString() {
	return "Aver="+average()+" std.="+stddev();
}
public double variance() {
	double res = 0;
	if (num != 0) {
		double d = av/(double)num;
		res = sav/(double)num - d*d;
	}
	return res;
}
}