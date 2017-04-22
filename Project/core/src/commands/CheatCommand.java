package commands;

import java.util.ArrayList;

import cards.Card;
import cards.PoliticsCard;
import cards.ScienceCard;
import comp361.catan.FishToken;
import comp361.catan.Knight;
import comp361.catan.Player;
import comp361.catan.ResourceType;
import comp361.catan.VertexConstruction;

public class CheatCommand extends GameCommand{
	
	private String playerID;
	private transient Player player;
	private Card card;

	public CheatCommand(String playerID, int phase) {
		super(phase);
		this.playerID=playerID;
	}
	
	public void setCard(Card card){
		this.card=card;
	}

	@Override
	protected void executeCommand() {
		player=getPlayers().get(playerID);
		if(getPhase()==1){
			player.setVictoryPoints(player.getVictoryPoints()+1);
			getToolbar().getChatWindow().log("Player "+player.getPeer().getName()+" has received 1 VP with a cheat code");
		}else if(getPhase()==2){
			for(ResourceType type:player.getResources().keySet()){
				player.getResources().put(type, player.getResources().get(type)+5);
			}
			getToolbar().getChatWindow().log("Player "+player.getPeer().getName()+" has received 5 resources/commodities of each time with a cheat code");
		}else if(getPhase()==3){
			getMap().setBarbarianPosition(6);
			getToolbar().getChatWindow().log("Player "+player.getPeer().getName()+" has advanced the barbarian counter to 6 with a cheat code");
		}else if(getPhase()==4){
			getMap().setBarbarianPosition(0);
			getToolbar().getChatWindow().log("Player "+player.getPeer().getName()+" has reset the barbarian counter with a cheat code");
		}else if(getPhase()==5){
			for(VertexConstruction cons:player.getVertexContructions()){
				if(cons instanceof Knight){
					((Knight)cons).setActive(true);
					((Knight)cons).setActivatedThisTurn(false);
				}
			}
			getToolbar().getChatWindow().log("Player "+player.getPeer().getName()+" has activated all his knights and (without them suffering the 'activated this turn' limitation on actions) with a cheat code");
		}else if(getPhase()==6){
			getCards().remove(card);
			if(card==ScienceCard.PRINTER || card==PoliticsCard.CONSTITUTION){
				getToolbar().getChatWindow().log("Player "+player.getPeer().getName()+" has taken the "+card+" from the deck with a cheat code. He received 1 VP");
				player.setVictoryPoints(player.getVictoryPoints()+1);
			}else{
				getToolbar().getChatWindow().log("Player "+player.getPeer().getName()+" has taken a progress card from the deck with a cheat code");
				player.getCards().add(card);
			}
		}else if(getPhase()==7){
			int count=0;
			ArrayList<FishToken> removed=new ArrayList<FishToken>();
			for(FishToken token:getMap().getFishToken()){
				if(!token.isBoot()){
					count++;
					player.getFishToken().add(token);
					removed.add(token);
				}
			}
			getMap().getFishToken().removeAll(removed);
			if(count>0)
				getToolbar().getChatWindow().log("Player "+player.getPeer().getName()+" has taken "+count+" fish token from the supply with a cheat code");
		}
	}

}
