package commands;

import actions.CardAction;
import actions.TurnAction;
import algorithms.Algorithms;
import cards.ScienceCard;
import comp361.catan.Edge;
import comp361.catan.Player;
import comp361.catan.Road;
import comp361.catan.Ship;
import comp361.catan.Tile;
import comp361.catan.TileType;

public class RoadBuildingCommand extends GameCommand{
	
	private static final long serialVersionUID = -7206309014777747408L;
	private int firstEdgeID;
	private int secondEdgeID;
	private String playerID;
	private transient Player player;
	
	public RoadBuildingCommand(String playerID, int firstEdgeID, int secondEdgeID){
		super(1);
		this.playerID=playerID;
		this.firstEdgeID=firstEdgeID;
		this.secondEdgeID=secondEdgeID;
	}

	@Override
	protected void executeCommand() {
		CardAction action=(CardAction)getAction();
		player=getPlayers().get(playerID);
		place(firstEdgeID);
		place(secondEdgeID);
		player.getCards().remove(ScienceCard.ROAD_BUILDING);
		getCards().add(ScienceCard.ROAD_BUILDING);
		getToolbar().getChatWindow().log("Player "+player.getPeer().getName()+" has placed 2 ships/roads with the road building progress card.");
		action.cancelProgressCard();
		Algorithms.updateLongestRoad(getMap(), getPlayers());
	}
	
	private void place(int edgeID){
		Edge edge=getMap().getEdge(edgeID);
		boolean nearSea=false;
		boolean nearPirate=false;
		for(Tile tile:edge.neighbors){
			nearSea|=tile.getTileType()==TileType.OCEAN;
			nearPirate|=getMap().getPirate().getLocation()==tile;
		}
		if(nearSea && !nearPirate){
			Ship ship=new Ship(edge, player);
			edge.setConstruction(ship);
			player.getEdgeConstructions().add(ship);
		}else{
			Road road=new Road(edge, player);
			edge.setConstruction(road);
			player.getEdgeConstructions().add(road);
		}
	}

}
