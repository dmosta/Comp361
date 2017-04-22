package commands;

import actions.CardAction;
import cards.Card;
import cards.PoliticsCard;
import comp361.catan.Player;

public class SpyCommand extends GameCommand{

	private static final long serialVersionUID = 1814551042755123083L;
	private String playerID;
	private String targetID;
	private Card stolen;
	
	public SpyCommand(String playerID, String targetID, Card stolen){
		super(1);
		this.playerID=playerID;
		this.targetID=targetID;
		this.stolen=stolen;
	}
	
	public SpyCommand(String playerID){
		super(1);
		this.playerID=playerID;
	}
	
	public void setTarget(String targetID){
		this.targetID=targetID;
	}
	
	public void setStolen(Card stolen){
		this.stolen=stolen;
	}

	@Override
	protected void executeCommand() {
		CardAction action=(CardAction)getAction();
		Player player=getPlayers().get(playerID);
		Player target=getPlayers().get(targetID);
		player.getCards().remove(PoliticsCard.SPY);
		player.getCards().add(stolen);
		target.getCards().remove(stolen);
		getToolbar().getChatWindow().log("Player "+player.getPeer().getName()+" has played the spy progress card and "
				+"stolen a progress card from "+target.getPeer().getName()+".");
		action.cancelProgressCard();
	}

}
