package commands;

import org.json.JSONObject;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import actions.FishAction;
import actions.TurnAction;
import algorithms.Algorithms;
import comp361.catan.Edge;
import comp361.catan.GameStage;
import comp361.catan.Player;
import comp361.catan.Road;
import comp361.catan.Ship;
import comp361.catan.Tile;
import comp361.catan.TileType;

public class FishBuildCommand extends FishCommand{

	private static final long serialVersionUID = -696997674790907121L;
	private String playerID;
	private boolean road;
	private transient ClickListener mapListener, cancelListener;
	private int edgeID;

	public FishBuildCommand(String playerID, boolean road, int phase) {
		super(phase);
		this.playerID=playerID;
		this.road=road;
	}
	
	public void setEdge(int edgeID){
		this.edgeID=edgeID;
	}

	@Override
	protected void executeCommand() {
		if(getPhase()==1){
			phase1();
		}else if(getPhase()==2){
			phase2();
		}
	}
	
	private void phase2(){
		Player player=getPlayers().get(playerID);
		Edge edge=getMap().getEdge(edgeID);
		if(road){
			Road road=new Road(edge, player);
			edge.setConstruction(road);
			player.getEdgeConstructions().add(road);
		}else{
			Ship ship=new Ship(edge, player);
			edge.setConstruction(ship);
			player.getEdgeConstructions().add(ship);
		}
		getToolbar().getChatWindow().log("Player "+player.getPeer().getName()+" has placed a "+(road?"road":"ship")+
			" using his fish token.");
		((FishAction)getAction()).removeSelectedTokens(getSelectedTokens(), player);
		Algorithms.updateLongestRoad(getMap(), getPlayers());
	}
	
	private void phase1(){
		TurnAction.LOCKED=true;
		getToolbar().getInfoWindow().displayInfoMessage("Choose where to place the "+(road?"road":"ship"));
		getToolbar().activateCancelButton(true, "Build "+(road?"road":"ship"));
		getToolbar().getCancelButton().addListener(cancelListener=new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				finishCommand();
			}
		});
		getMap().addListener(mapListener=new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Edge edge=getMap().getCurrentEdge();
				boolean valid=false;
				valid|=(edge.first.getConstruction()!=null && edge.first.getConstruction().getOwner()==getPlayer());
				valid|=(edge.second.getConstruction()!=null && edge.second.getConstruction().getOwner()==getPlayer());
				for(Edge e:edge.first.edges)
					valid|=(e!=edge && e.getConstruction()!=null && e.getConstruction().getOwner()==getPlayer());
				for(Edge e:edge.second.edges)
					valid|=(e!=edge && e.getConstruction()!=null && e.getConstruction().getOwner()==getPlayer());
				if(!road){
					if(getMap().getPirate().getLocation()!=null){
						for(Edge e:getMap().getPirate().getLocation().edges){
							if(e==edge)
								valid=false;
						}
					}
					boolean nearSea=false;
					for(Tile tile:edge.neighbors)
						nearSea|=(tile.getTileType()==TileType.OCEAN);
					valid&=nearSea;
				}else{
					boolean nearLand=false;
					for(Tile tile:edge.neighbors)
						nearLand|=(tile.getTileType()!=TileType.OCEAN);
					valid&=nearLand;
				}
				if(valid){
					finishCommand();
					FishBuildCommand command=new FishBuildCommand(playerID, road, 2);
					command.setSelectedTokens(getSelectedTokens());
					command.setEdge(edge.id);
					JSONObject obj=GameCommand.getJsonCommand(command, "fish");
					getSocket().emit("catan", obj, GameStage.ACK);
					command.execute(getStage(), getAction());
				}
			}
		});
	}
	
	private void finishCommand(){
		getToolbar().getInfoWindow().clearMessage();
		TurnAction.LOCKED=false;
		getMap().removeListener(mapListener);
		getToolbar().getCancelButton().removeListener(cancelListener);
		getToolbar().activateCancelButton(false, "");
	}

}
