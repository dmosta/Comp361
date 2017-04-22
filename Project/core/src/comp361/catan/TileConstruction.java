package comp361.catan;

public abstract class TileConstruction implements Drawable{
	
	private Tile location;
	
	public void setLocation(Tile location){
		this.location=location;
	}
	
	public Tile getLocation(){
		return this.location;
	}
}
