package comp361.catan;

/**
 * Base class for edge constructions like roads and ships on the game board
 */
public abstract class EdgeConstruction implements Drawable{
	
	private Edge parent;
	private Player owner;
	public boolean hightlight=false;
	
	public EdgeConstruction(Edge parent, Player owner){
		this.parent=parent;
		this.owner=owner;
	}
	
	public void setParent(Edge parent){
		this.parent=parent;
	}
	
	public Edge getParent(){
		return this.parent;
	}
	
	public Player getOwner(){
		return this.owner;
	}
	
}
