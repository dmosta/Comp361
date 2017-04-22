package commands;

import actions.CardAction;
import actions.TurnAction;
import cards.ScienceCard;
import comp361.catan.City;
import comp361.catan.Player;
import comp361.catan.ResourceType;
import comp361.catan.Settlement;
import comp361.catan.Vertex;

public class MedicineCommand extends GameCommand{
	
	private static final long serialVersionUID = 8026960988418271303L;
	private String playerID;
	private int vertexID;
	
	public MedicineCommand(String playerID, int vertexID){
		super(1);
		this.playerID=playerID;
		this.vertexID=vertexID;
	}

	@Override
	protected void executeCommand() {
		CardAction action=(CardAction)getAction();
		Player player=getPlayers().get(playerID);
		player.getResources().put(ResourceType.ORE, player.getResources().get(ResourceType.ORE)-2);
		player.getResources().put(ResourceType.GRAIN, player.getResources().get(ResourceType.GRAIN)-1);
		Vertex vertex=getMap().getVertex(vertexID);
		Settlement settlement=(Settlement)vertex.getConstruction();
		City city=new City(vertex, player);
		vertex.setConstruction(city);
		player.getVertexContructions().remove(settlement);
		player.getVertexContructions().add(city);
		player.setVictoryPoints(player.getVictoryPoints()+1);
		getToolbar().getChatWindow().log("Player "+player.getPeer().getName()+" has upgraded one of his settlements"
				+ "to a city using the medicine progress card.");
		player.getCards().remove(ScienceCard.MEDICINE);
		getCards().add(ScienceCard.MEDICINE);
		action.cancelProgressCard();
	}

}
