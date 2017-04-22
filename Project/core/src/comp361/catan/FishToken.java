package comp361.catan;

public class FishToken {
	
	private int value;
	private boolean boot;
	
	public FishToken(int value, boolean boot){
		this.value=value;
		this.boot=boot;
	}
	
	public int getValue(){
		return this.value;
	}
	
	public boolean isBoot(){
		return this.boot;
	}
}
