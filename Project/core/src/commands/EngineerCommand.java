package commands;

import actions.CardAction;
import actions.TurnAction;
import cards.Card;
import cards.ScienceCard;
import comp361.catan.City;
import comp361.catan.GameStage;
import comp361.catan.Player;
import comp361.catan.Vertex;

public class EngineerCommand extends GameCommand{
	
	private static final long serialVersionUID = -1850894849135965290L;
	private String playerID;
	private int vertexID;
	
	public EngineerCommand(String playerID, int vertexID){
		super(1);
		this.playerID=playerID;
		this.vertexID=vertexID;
	}
	
	@Override
	protected void executeCommand() {
		CardAction action=(CardAction)getAction();
		Vertex vertex=getMap().getVertex(vertexID);
		City city=(City)vertex.getConstruction();
		city.setWall(true);
		Player player=getPlayers().get(playerID);
		player.getCards().remove(ScienceCard.ENGINEER);
		getCards().add(ScienceCard.ENGINEER);
		player.setNumWalls(player.getNumWalls()+1);
		action.cancelProgressCard();
		getToolbar().getChatWindow().log("Player "+player.getPeer().getName()+" has played the engineer"
				+ "and added a wall to one of his cities for free");
	}

}
