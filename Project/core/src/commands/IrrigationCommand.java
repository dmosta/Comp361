package commands;

import actions.CardAction;
import actions.TurnAction;
import cards.ScienceCard;
import comp361.catan.Player;
import comp361.catan.ResourceType;
import comp361.catan.Tile;
import comp361.catan.TileType;
import comp361.catan.Vertex;

public class IrrigationCommand extends GameCommand{
	
	private static final long serialVersionUID = 8771439747236273555L;
	private String playerID;
	
	public IrrigationCommand(String playerID){
		super(1);
		this.playerID=playerID;
	}

	@Override
	protected void executeCommand() {
		CardAction action=(CardAction)getAction();
		Player player=getPlayers().get(playerID);
		int numGrain=0;
		for(Tile tile:getMap().getTiles().values()){
			if(tile.getTileType()==TileType.FIELD){
				for(Vertex vertex:tile.vertices){
					if(vertex.getConstruction()!=null && vertex.getConstruction().getOwner()==player){
						numGrain+=2;
						break;
					}
				}
			}
		}
		player.getResources().put(ResourceType.GRAIN, player.getResources().get(ResourceType.GRAIN)+numGrain);
		getToolbar().getChatWindow().log("Player "+player.getPeer().getName()+" has played the irrigation card "
				+ "and received "+numGrain+" grain (for cities/settlements adjacent to "+(numGrain/2)+" fields)");
		player.getCards().remove(ScienceCard.IRRIGATION);
		getCards().add(ScienceCard.IRRIGATION);
		action.cancelProgressCard();
	}

}
