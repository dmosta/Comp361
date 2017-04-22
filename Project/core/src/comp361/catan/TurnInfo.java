package comp361.catan;

/**
 * TurnInfo stores information needed about the progress of a player's turn.
 */
public class TurnInfo {
	
	private boolean diceRolled=false;
	private ResourceType fleetResource;
	
	public void setDiceRolled(boolean diceRolled){
		this.diceRolled=diceRolled;
	}
	
	public boolean isDiceRolled(){
		return this.diceRolled;
	}
	
	public void setFleetResource(ResourceType fleetResource){
		this.fleetResource=fleetResource;
	}
	
	public ResourceType getFleetResource(){
		return this.fleetResource;
	}
	
	public void newTurn(){
		diceRolled=false;
		fleetResource=null;
	}
}
