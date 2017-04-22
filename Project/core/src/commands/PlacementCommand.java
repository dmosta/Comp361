package commands;

import actions.SetupAction;
import comp361.catan.City;
import comp361.catan.Edge;
import comp361.catan.GameStage;
import comp361.catan.GameState;
import comp361.catan.Player;
import comp361.catan.Resource;
import comp361.catan.ResourceType;
import comp361.catan.Road;
import comp361.catan.Settlement;
import comp361.catan.Ship;
import comp361.catan.Tile;
import comp361.catan.TileType;
import comp361.catan.Vertex;

public class PlacementCommand extends GameCommand{

	private static final long serialVersionUID = -4393786677378027597L;
	private String playerID;
	private Integer settlementID;
	private Integer roadID;
	private boolean first;
	private transient Player player;
	private transient SetupAction action;
	
	/**
	 * Either settlementID or roadID has to be null. First indicates if it is the first or second road/settlement
	 */
	public PlacementCommand(String playerID, Integer settlementID, Integer roadID, boolean first){
		super(1);
		this.playerID=playerID;
		this.settlementID=settlementID;
		this.roadID=roadID;
		this.first=first;
	}
	
	@Override
	protected void executeCommand() {
		this.action=(SetupAction)getAction();
		player=getPlayers().get(playerID);
		if(roadID==null){
			Vertex vertex=getMap().getVertex(settlementID);
			if(first){
				Settlement settlement=new Settlement(vertex, player);
				vertex.setConstruction(settlement);
				player.getVertexContructions().add(settlement);
				player.setSettlementsRemaining(player.getSettlementsRemaining()-1);
				player.setVictoryPoints(player.getVictoryPoints()+1);
				getToolbar().getChatWindow().log("Player "+player.getPeer().getName()+" has placed his first settlement.");
			}else{
				City city=new City(vertex, player);
				vertex.setConstruction(city);
				player.getVertexContructions().add(city);
				player.setCitiesRemaining(player.getCitiesRemaining()-1);
				player.setVictoryPoints(player.getVictoryPoints()+2);
				getToolbar().getChatWindow().log("Player "+player.getPeer().getName()+" has placed his first City.");
			}
		}else if(settlementID==null){
			Edge edge=getMap().getEdge(roadID);
			boolean nearSea=false;
			for(Tile tile:edge.neighbors)
				nearSea|=(tile.getTileType()==TileType.OCEAN);
			if(nearSea){
				Ship ship=new Ship(edge, player);
				player.getEdgeConstructions().add(ship);
				edge.setConstruction(ship);
			}else{
				Road road=new Road(edge, player);
				player.getEdgeConstructions().add(road);
				edge.setConstruction(road);
			}
			getToolbar().getChatWindow().log("Player "+player.getPeer().getName()+" has placed his "+(first?"first":"second")+" road");
			if(first)
				firstPlacementDone();
			else secondPlacementDone();
		}
	}
	
	private void firstPlacementDone(){
		getToolbar().getInfoWindow().clearMessage();
		if(player.getOrder()==getPlayers().values().size()){
			GameStage.GAME_STATE=GameState.SECOND_PLACEMENT;
			if(getPlayer().isTurn())
				getToolbar().getInfoWindow().displayInfoMessage("Place your city and second road.");
		}else{
			for(Player p:getPlayers().values())
				p.setTurn(p.getOrder()==(player.getOrder()+1));
			if(getPlayer().isTurn())
				getToolbar().getInfoWindow().displayInfoMessage("Place your first settlement and road.");
		}
	}
	
	private void secondPlacementDone(){
		getToolbar().getInfoWindow().clearMessage();
		if(player.getOrder()==1)
			generateResources();
		else{
			for(Player p:getPlayers().values())
				p.setTurn(p.getOrder()==player.getOrder()-1);
			if(getPlayer().isTurn())
				getToolbar().getInfoWindow().displayInfoMessage("Place your city and second road.");
		}
	}
	
	private void generateResources(){
		for(Vertex vertex:getMap().getVertices().values()){
			if(vertex.getConstruction()!=null && vertex.getConstruction() instanceof City){
				Player owner=vertex.getConstruction().getOwner();
				for(Tile neighbor:vertex.neighbors){
					ResourceType res=Resource.resourceFromTile(neighbor.getTileType());
					if(res!=null){
						int num=owner.getResources().get(res);
						owner.getResources().put(res, num+1);
					}
				}
			}
		}
		getToolbar().getChatWindow().log("Ressources have been generated");
		action.done();
	}

}
