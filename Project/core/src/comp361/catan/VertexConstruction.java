package comp361.catan;

/**
 * Base class for vertex constructions (settlements, cities, knights, ...)
 */
public abstract class VertexConstruction implements Drawable{

	private Vertex parent;
	private Player owner;
	public boolean highlight=false;
	
	public VertexConstruction(Vertex parent, Player owner){
		this.parent=parent;
		this.owner=owner;
	}
	
	public void setParent(Vertex parent){
		this.parent=parent;
	}
	
	public Vertex getParent(){
		return this.parent;
	}
	
	public Player getOwner(){
		return this.owner;
	}
}
