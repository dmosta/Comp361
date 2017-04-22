package commands;

import actions.CardAction;
import cards.PoliticsCard;
import comp361.catan.Knight;
import comp361.catan.Player;
import comp361.catan.VertexConstruction;

public class WarlordCommand extends GameCommand{

	private static final long serialVersionUID = 3595502656110244244L;
	private String playerID;
	
	public WarlordCommand(String playerID){
		super(1);
		this.playerID=playerID;
	}

	@Override
	protected void executeCommand() {
		CardAction action=(CardAction)getAction();
		Player player=getPlayers().get(playerID);
		int count=0;
		for(VertexConstruction construction:player.getVertexContructions()){
			if(construction instanceof Knight){
				count++;
				Knight knight=(Knight)construction;
				knight.setActive(true);
				knight.setActivatedThisTurn(true);
			}
		}
		player.getCards().remove(PoliticsCard.WARLORD);
		getCards().add(PoliticsCard.WARLORD);
		getToolbar().getChatWindow().log("Player "+player.getPeer().getName()+" has activated "
				+count+" of his knights with the warlord progress card.");
		action.cancelProgressCard();
	}

}
