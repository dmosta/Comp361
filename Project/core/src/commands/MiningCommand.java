package commands;

import actions.CardAction;
import actions.TurnAction;
import cards.ScienceCard;
import comp361.catan.Player;
import comp361.catan.ResourceType;
import comp361.catan.Tile;
import comp361.catan.TileType;
import comp361.catan.Vertex;

public class MiningCommand extends GameCommand{
	
	private static final long serialVersionUID = -6825091788920351499L;
	private String playerID;
	
	public MiningCommand(String playerID){
		super(1);
		this.playerID=playerID;
	}

	@Override
	protected void executeCommand() {
		CardAction action=(CardAction)getAction();
		Player player=getPlayers().get(playerID);
		int numOre=0;
		for(Tile tile:getMap().getTiles().values()){
			if(tile.getTileType()==TileType.MOUNTAIN){
				for(Vertex v:tile.vertices){
					if(v.getConstruction()!=null && v.getConstruction().getOwner()==player){
						numOre+=2;
						break;
					}
				}
			}
		}
		player.getResources().put(ResourceType.ORE, player.getResources().get(ResourceType.ORE)+numOre);
		player.getCards().remove(ScienceCard.MINING);
		getCards().add(ScienceCard.MINING);
		getToolbar().getChatWindow().log("PLayer "+player.getPeer().getName()+" has received "+numOre+" ore after playing the mining progress card");
		action.cancelProgressCard();
	}

}
