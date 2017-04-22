package commands;

import actions.CardAction;
import actions.TurnAction;
import cards.ScienceCard;
import comp361.catan.Knight;
import comp361.catan.Player;
import comp361.catan.Vertex;

public class SmithCommand extends GameCommand{
	
	private static final long serialVersionUID = -4840267299154215845L;
	private int vertexID1;
	private int vertexID2;
	private String playerID;
	
	public SmithCommand(String playerID, int vertexID1, int vertexID2){
		super(1);
		this.vertexID1=vertexID1;
		this.vertexID2=vertexID2;
		this.playerID=playerID;
	}

	@Override
	protected void executeCommand() {
		CardAction action=(CardAction)getAction();
		Player player=getPlayers().get(playerID);
		Vertex v1=getMap().getVertex(vertexID1);
		Knight knight1=(Knight)v1.getConstruction();
		knight1.setLevel(knight1.getLevel()+1);
		if(vertexID2!=-1){
			Vertex v2=getMap().getVertex(vertexID2);
			Knight knight2=(Knight)v2.getConstruction();
			knight2.setLevel(knight2.getLevel()+1);
		}
		player.getCards().remove(ScienceCard.SMITH);
		getCards().add(ScienceCard.SMITH);
		getToolbar().getChatWindow().log("Player "+player.getPeer().getName()+" has upgraded his knights with the smith progress card.");
		action.cancelProgressCard();
		TurnAction.LOCKED=false;
	}

}
