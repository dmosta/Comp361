package util;

/**
 * Utility class for a tuple of objects of two types.
 */
public class Tuple <T,D>{
	
	private T first;
	private D second;
	
	public Tuple(T first, D second){
		this.first=first;
		this.second=second;
	}
	
	public T getFirst(){
		return this.first;
	}
	
	public D getSecond(){
		return this.second;
	}
	
}
