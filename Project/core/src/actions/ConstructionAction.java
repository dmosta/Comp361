package actions;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import algorithms.Algorithms;
import commands.GameCommand;
import comp361.catan.City;
import comp361.catan.Edge;
import comp361.catan.GameStage;
import comp361.catan.Knight;
import comp361.catan.Notification;
import comp361.catan.Player;
import comp361.catan.ResourceType;
import comp361.catan.Road;
import comp361.catan.Settlement;
import comp361.catan.Ship;
import comp361.catan.Skins;
import comp361.catan.Tile;
import comp361.catan.TileType;
import comp361.catan.Vertex;
import comp361.catan.VertexConstruction;
import io.socket.client.Ack;
import io.socket.emitter.Emitter.Listener;
import ui.*;

/**
 * This game action handles the placement and upgrading of vertex and edge constructions
 */
public class ConstructionAction extends GameAction{

	private Toolbar toolbar;
	private ClickListener constructionListener;
	private Listener socketListener;
	private HashMap<ResourceType, Integer> resources;
	private ActionDialog actionDialog;
	private ClickListener mapListener;
	private ClickListener cancelListener;

	public ConstructionAction(Notification notif, GameStage gameStage) {
		super(notif, gameStage);
		toolbar=getStage().getToolbar();
		resources=getPlayer().getResources();
		actionDialog=toolbar.getActionDialog();
		constructionListener=new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				try{
					if(getPlayer().isTurn()){
						TurnAction.LOCKED=false;
						boolean valid=false;
						if(valid=event.getTarget().isDescendantOf(actionDialog.getButtonSettlement()))
							purchaseSettlement();
						else if(valid=event.getTarget().isDescendantOf(actionDialog.getButtonRoad()))
							purchaseRoad();
						else if(valid=event.getTarget().isDescendantOf(actionDialog.getButtonShip()))
							purchaseShip();
						else if(valid=event.getTarget().isDescendantOf(actionDialog.getButtonCity()))
							purchaseCity();
						else if(valid=event.getTarget().isDescendantOf(actionDialog.getButtonKnight()))
							purchaseKnight();
						else if(valid=event.getTarget().isDescendantOf(actionDialog.getButtonWall()))
							purchaseWall();
						else if(valid=event.getTarget().isDescendantOf(actionDialog.getButtonUpgradeKnight()))
							purchaseKnightUpgrade();
						else if(valid=event.getTarget().isDescendantOf(actionDialog.getButtonActivateKnight()))
							purchaseKnightActivation();
						else if(valid=event.getTarget().isDescendantOf(actionDialog.getButtonMoveKnight()))
							purchaseKnightPlacement();
						else if(valid=event.getTarget().isDescendantOf(actionDialog.getButtonDisplaceKnight()))
							purchaseKnightDisplacement();
						if(valid)
							actionDialog.hide();
					}
				}catch(Exception e){e.printStackTrace();}
			}
		};
		actionDialog.getButtonCity().addListener(constructionListener);
		actionDialog.getButtonKnight().addListener(constructionListener);
		actionDialog.getButtonRoad().addListener(constructionListener);
		actionDialog.getButtonSettlement().addListener(constructionListener);
		actionDialog.getButtonShip().addListener(constructionListener);
		actionDialog.getButtonWall().addListener(constructionListener);
		actionDialog.getButtonUpgradeKnight().addListener(constructionListener);
		actionDialog.getButtonMoveKnight().addListener(constructionListener);
		actionDialog.getButtonActivateKnight().addListener(constructionListener);
		actionDialog.getButtonDisplaceKnight().addListener(constructionListener);
		setupSocket();
	}
	
	private void placeSettlement(Vertex vertex, Player player){
		Settlement settlement=new Settlement(vertex, player);
		vertex.setConstruction(settlement);
		player.getVertexContructions().add(settlement);
		player.setSettlementsRemaining(player.getSettlementsRemaining()-1);
		HashMap<ResourceType, Integer> res=player.getResources();
		res.put(ResourceType.BRICK, res.get(ResourceType.BRICK)-1);
		res.put(ResourceType.LUMBER, res.get(ResourceType.LUMBER)-1);
		res.put(ResourceType.GRAIN, res.get(ResourceType.GRAIN)-1);
		res.put(ResourceType.WOOL, res.get(ResourceType.WOOL)-1);
		getToolbar().getChatWindow().log("Player "+player.getPeer().getName()+
				" has built a settlement.");
		player.setVictoryPoints(player.getVictoryPoints()+1);
		Algorithms.updateLongestRoad(getMap(), getPlayers());
	}
	
	private void placeWall(Vertex location){
		City city=(City)location.getConstruction();
		city.setWall(true);
		city.getOwner().setNumWalls(city.getOwner().getNumWalls()+1);
		HashMap<ResourceType, Integer> res=city.getOwner().getResources();
		res.put(ResourceType.BRICK, res.get(ResourceType.BRICK)-2);
		getToolbar().getChatWindow().log("Player "+location.getConstruction().getOwner().getPeer().getName()+
				" has built a wall.");
	}
	
	private void placeCity(Vertex location){
		Settlement set=(Settlement)location.getConstruction();
		Player player=set.getOwner();
		player.setVictoryPoints(player.getVictoryPoints()+1);
		player.setSettlementsRemaining(player.getSettlementsRemaining()+1);
		player.setCitiesRemaining(player.getCitiesRemaining()-1);
		City city=new City(set.getParent(), player);
		location.setConstruction(city);
		player.getVertexContructions().remove(set);
		player.getVertexContructions().add(city);
		HashMap<ResourceType, Integer> res=set.getOwner().getResources();
		res.put(ResourceType.ORE, res.get(ResourceType.ORE)-3);
		res.put(ResourceType.GRAIN, res.get(ResourceType.GRAIN)-2);
		getToolbar().getChatWindow().log("Player "+location.getConstruction().getOwner().getPeer().getName()+
				" has upgraded a settlement to a city.");
	}
	
	private void placeKnight(Vertex location, Player player){
		Knight knight=new Knight(location, player);
		location.setConstruction(knight);
		player.getVertexContructions().add(knight);
		player.setBasicKnightsRemaining(player.getBasicKnightsRemaining()-1);
		HashMap<ResourceType, Integer> res=player.getResources();
		res.put(ResourceType.WOOL, res.get(ResourceType.WOOL)-1);
		res.put(ResourceType.ORE, res.get(ResourceType.ORE)-1);
		getToolbar().getChatWindow().log("Player "+player.getPeer().getName()+
				" has built a knight.");
		Algorithms.updateLongestRoad(getMap(), getPlayers());
	}
	
	private void placeRoad(Edge location, Player player){
		HashMap<ResourceType, Integer> res=player.getResources();
		Road road=new Road(location, player);
		location.setConstruction(road);
		player.getEdgeConstructions().add(road);
		res.put(ResourceType.LUMBER, res.get(ResourceType.LUMBER)-1);
		res.put(ResourceType.BRICK, res.get(ResourceType.BRICK)-1);
		getToolbar().getChatWindow().log("Player "+player.getPeer().getName()+
				" has built a road");
		Algorithms.updateLongestRoad(getMap(), getPlayers());
	}
	
	private void placeShip(Edge location, Player player){
		HashMap<ResourceType, Integer> res=player.getResources();
		Ship ship=new Ship(location, player);
		location.setConstruction(ship);
		player.getEdgeConstructions().add(ship);
		res.put(ResourceType.LUMBER, res.get(ResourceType.LUMBER)-1);
		res.put(ResourceType.WOOL, res.get(ResourceType.WOOL)-1);
		getToolbar().getChatWindow().log("Player "+player.getPeer().getName()+
				" has built a ship");
		Algorithms.updateLongestRoad(getMap(), getPlayers());
	}
	
	private void placeKnightUpgrade(Vertex location){
		Knight knight=(Knight)location.getConstruction();
		HashMap<ResourceType, Integer> res=knight.getOwner().getResources();
		Player player=knight.getOwner();
		knight.setLevel(knight.getLevel()+1);
		if(knight.getLevel()==3){
			player.setMightyKnightsRemaining(player.getMightyKnightsRemaining()-1);
			player.setStrongKnightsRemaining(player.getStrongKnightsRemaining()+1);
		}
		else if(knight.getLevel()==2){
			player.setStrongKnightsRemaining(player.getStrongKnightsRemaining()-1);
			player.setBasicKnightsRemaining(player.getBasicKnightsRemaining()+1);
		}
		res.put(ResourceType.ORE, res.get(ResourceType.ORE)-1);
		res.put(ResourceType.WOOL, res.get(ResourceType.WOOL)-1);
		getToolbar().getChatWindow().log("Player "+location.getConstruction().getOwner().getPeer().getName()+
				" has upgraded a knight to level "+knight.getLevel());
	}
	
	private void placeKnightActivation(Vertex location){
		Knight knight=(Knight)location.getConstruction();
		HashMap<ResourceType, Integer> res=knight.getOwner().getResources();
		knight.setActive(true);
		knight.setActivatedThisTurn(true);
		res.put(ResourceType.GRAIN, res.get(ResourceType.GRAIN)-1);
		getToolbar().getChatWindow().log("Player "+location.getConstruction().getOwner().getPeer().getName()+
				" has activated a knight");
	}
	
	private void moveKnight(Vertex source, Vertex destination){
		Knight knight=(Knight)source.getConstruction();
		knight.setParent(destination);
		destination.setConstruction(knight);
		source.setConstruction(null);
		knight.setActive(false);
		getToolbar().getChatWindow().log("Player "+destination.getConstruction().getOwner().getPeer().getName()+
				" has moved a knight");
		Algorithms.updateLongestRoad(getMap(), getPlayers());
	}
	
	private void setupSocket(){
		getSocket().on("catan", socketListener=new Listener(){
			@Override
			public void call(final Object... arg0) {
				Gdx.app.postRunnable(new Runnable(){
					@Override
					public void run() {
						try{
							JSONObject obj=(JSONObject)arg0[0];
							Ack ack=(Ack)arg0[arg0.length-1];
							GameCommand command=null;
							switch(obj.getString("action")){
								case "place_settlement":
									placeSettlement(getMap().getVertex(obj.getInt("vertex")),
											getPlayers().get(obj.getString("player")));
									break;
								case "place_road":
									placeRoad(getMap().getEdge(obj.getInt("edge")),
											getPlayers().get(obj.getString("player")));
									break;
								case "place_ship":
									placeShip(getMap().getEdge(obj.getInt("edge")),
											getPlayers().get(obj.getString("player")));
									break;
								case "place_city":
									placeCity(getMap().getVertex(obj.getInt("vertex")));
									break;
								case "place_knight":
									placeKnight(getMap().getVertex(obj.getInt("vertex")),
											getPlayers().get(obj.getString("player")));
									break;
								case "place_wall":
									placeWall(getMap().getVertex(obj.getInt("vertex")));
									break;
								case "knight_upgrade":
									placeKnightUpgrade(getMap().getVertex(obj.getInt("vertex")));
									break;
								case "knight_activation":
									placeKnightActivation(getMap().getVertex(obj.getInt("vertex")));
									break;
								case "knight_move":
									moveKnight(getMap().getVertex(obj.getInt("source")), getMap().getVertex(obj.getInt("destination")));
									break;
								case "displace_knight":
									moveDisplacedKnight(getMap().getVertex(obj.getInt("initiater")), getMap().getVertex(obj.getInt("target")));
									break;
								case "displacement_answer":
									receivedDisplacementAnswer(obj.getBoolean("canMove"), getMap().getVertex(obj.getInt("initiater")),
											getMap().getVertex(obj.getInt("target")), getMap().getVertex(obj.getInt("newLocation")));
									break;
								default:
									return;
							}
							if(command!=null)
								command.execute(getStage(), ConstructionAction.this);
							ack.call();
						}catch(Exception e){e.printStackTrace();}
					}
				});
			}
		});
	}
	
	private void purchaseWall() throws JSONException{
		VertexConstruction cons=actionDialog.getVertex().getConstruction();
		if(cons!=null && cons instanceof City && cons.getOwner()==getPlayer()){
			placeWall(actionDialog.getVertex());
			JSONObject obj=new JSONObject();
			obj.put("action", "place_wall");
			obj.put("vertex", actionDialog.getVertex().id);
			getSocket().emit("catan", obj, GameStage.ACK);
		}
	}
	
	private void purchaseKnight() throws JSONException{
		placeKnight(actionDialog.getVertex(), getPlayer());
		JSONObject obj=new JSONObject();
		obj.put("action", "place_knight");
		obj.put("vertex", actionDialog.getVertex().id);
		obj.put("player", getPlayer().getPeer().getId());
		getSocket().emit("catan", obj, GameStage.ACK);
	}
	
	private void purchaseCity() throws JSONException{
		placeCity(actionDialog.getVertex());
		JSONObject obj=new JSONObject();
		obj.put("action", "place_city");
		obj.put("vertex", actionDialog.getVertex().id);
		getSocket().emit("catan", obj, GameStage.ACK);
	}
	
	private void purchaseShip() throws JSONException{
		placeShip(actionDialog.getEdge(), getPlayer());
		JSONObject obj=new JSONObject();
		obj.put("action", "place_ship");
		obj.put("edge", actionDialog.getEdge().id);
		obj.put("player", getPlayer().getPeer().getId());
		getSocket().emit("catan", obj, GameStage.ACK);
	}
	
	private void purchaseRoad() throws JSONException{
		Edge edge=actionDialog.getEdge();
		placeRoad(edge, getPlayer());
		JSONObject obj=new JSONObject();
		obj.put("action", "place_road");
		obj.put("edge", edge.id);
		obj.put("player", getPlayer().getPeer().getId());
		getSocket().emit("catan", obj, GameStage.ACK);
	}
	
	private void purchaseSettlement() throws JSONException{
		placeSettlement(actionDialog.getVertex(), getPlayer());
		JSONObject obj=new JSONObject();
		obj.put("action", "place_settlement");
		obj.put("player", getPlayer().getPeer().getId());
		obj.put("vertex", actionDialog.getVertex().id);
		getSocket().emit("catan", obj, GameStage.ACK);
	}
	
	private void purchaseKnightUpgrade() throws JSONException{
		placeKnightUpgrade(actionDialog.getVertex());
		JSONObject obj=new JSONObject();
		obj.put("action", "knight_upgrade");
		obj.put("vertex", actionDialog.getVertex().id);
		getSocket().emit("catan", obj, GameStage.ACK);
	}
	
	private void purchaseKnightActivation() throws JSONException{
		placeKnightActivation(actionDialog.getVertex());
		JSONObject obj=new JSONObject();
		obj.put("action", "knight_activation");
		obj.put("vertex", actionDialog.getVertex().id);
		getSocket().emit("catan", obj, GameStage.ACK);
	}
	
	private void purchaseKnightPlacement(){
		TurnAction.LOCKED=true;
		getToolbar().activateCancelButton(true, "Moving knight");
		getToolbar().getCancelButton().addListener(cancelListener=new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				TurnAction.LOCKED=false;
				getToolbar().getCancelButton().removeListener(cancelListener);
				getToolbar().activateCancelButton(false, "");
				getMap().removeListener(mapListener);
			}
		});
		getMap().addListener(mapListener=new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Vertex vertex=getMap().getCurrentVertex();
				boolean connected=Algorithms.areConnected(actionDialog.getVertex(), vertex, getPlayer());
				if(vertex!=null && vertex.getConstruction()==null && connected){
					try{
						getToolbar().activateCancelButton(false, "");
						TurnAction.LOCKED=false;
						getToolbar().getCancelButton().removeListener(cancelListener);
						getMap().removeListener(mapListener);
						JSONObject obj=new JSONObject();
						moveKnight(actionDialog.getVertex(), vertex);
						obj.put("action", "knight_move");
						obj.put("source", actionDialog.getVertex().id);
						obj.put("destination", vertex.id);
						getSocket().emit("catan", obj, GameStage.ACK);
					}catch(Exception e){e.printStackTrace();}
				}
			}
		});
	}
	
	private Dialog displacementDialog;
	
	// Phase 1, when the user purchases the knight displacement
	private void purchaseKnightDisplacement(){
		final Vertex target=actionDialog.getVertex();
		target.highlight=true;
		TurnAction.LOCKED=true;
		getToolbar().getInfoWindow().displayInfoMessage("Click on the activated knight you will use to execute the displacement");
		getToolbar().activateCancelButton(true, "Displacing knight");
		getToolbar().getCancelButton().addListener(cancelListener=new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				getToolbar().getCancelButton().removeListener(cancelListener);
				getMap().removeListener(mapListener);
				TurnAction.LOCKED=false;
				getToolbar().activateCancelButton(false, "");
				target.highlight=false;
			}
		});
		getMap().addListener(mapListener=new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Vertex vertex=getMap().getCurrentVertex();
				if(vertex!=null && vertex!=target && vertex.getConstruction() instanceof Knight){
					Knight knight=(Knight)vertex.getConstruction();
					Knight other=(Knight)target.getConstruction();
					if(knight.getOwner()==getPlayer() && knight.getLevel()>other.getLevel()
							&& knight.isActive() && !knight.wasActivatedThisTurn() && Algorithms.areConnected(vertex, target, getPlayer())){
						try{
							target.highlight=false;
							TurnAction.LOCKED=false;
							getToolbar().getInfoWindow().clearMessage();
							getMap().removeListener(mapListener);
							getToolbar().getCancelButton().removeListener(cancelListener);
							getToolbar().activateCancelButton(false, "");
							displacementDialog=new Dialog("Waiting for other player...", Skins.METAL).show(getStage());
							JSONObject obj=new JSONObject();
							obj.put("action", "displace_knight");
							obj.put("target", target.id);
							obj.put("initiater", vertex.id);
							getSocket().emit("catan", obj, GameStage.ACK);
						}catch(Exception ex){ex.printStackTrace();}
					}
				}
			}
		});
	}
	
	private void moveDisplacedKnight(final Vertex initiater, final Vertex target){
		Knight knight=(Knight)target.getConstruction();
		if(knight.getOwner()==getPlayer()){
			boolean canMove=Algorithms.isConnectedToUnoccupiedVertex(target, getPlayer());
			if(!canMove)
				sendDisplacedMovement(canMove, initiater, target, null);
			else{
				target.highlight=true;
				getToolbar().getInfoWindow().displayInfoMessage("Player "+initiater.getConstruction().getOwner().getPeer().getName()+
						" has initiated a displacement on one of your knights. Select where to move it.");
				getMap().addListener(mapListener=new ClickListener(){
					@Override
					public void clicked(InputEvent event, float x, float y) {
						Vertex newVertex=getMap().getCurrentVertex();
						if(newVertex.getConstruction()==null && Algorithms.areConnected(target, newVertex, getPlayer())){
							getToolbar().getInfoWindow().clearMessage();
							getMap().removeListener(mapListener);
							target.highlight=false;
							sendDisplacedMovement(true, initiater, target, newVertex);
						}
					}
				});
			}
		}
	}
	
	private void receivedDisplacementAnswer(boolean canMove, Vertex initiater, Vertex target, Vertex newLocation){
		Player initiaterPlayer=initiater.getConstruction().getOwner();
		if(initiaterPlayer==getPlayer()){
			TurnAction.LOCKED=false;
			getToolbar().activateCancelButton(false, "");
			displacementDialog.hide();
		}
		Player targetPlayer=target.getConstruction().getOwner();
		Knight knight=(Knight)initiater.getConstruction();
		knight.setActive(false);
		Knight targetKnight=(Knight)target.getConstruction();
		target.setConstruction(knight);
		knight.setParent(target);
		initiater.setConstruction(null);
		if(!canMove){
			if(targetKnight.getLevel()==1)
				targetPlayer.setBasicKnightsRemaining(targetPlayer.getBasicKnightsRemaining()+1);
			else if(targetKnight.getLevel()==2)
				targetPlayer.setStrongKnightsRemaining(targetPlayer.getStrongKnightsRemaining()+1);
			else if(targetKnight.getLevel()==3)
				targetPlayer.setMightyKnightsRemaining(targetPlayer.getMightyKnightsRemaining()+1);
		}
		else{
			newLocation.setConstruction(targetKnight);
			targetKnight.setParent(newLocation);
		}
		getToolbar().getChatWindow().log("Player "+initiaterPlayer.getPeer().getName()+" has displaced a knight owned by "+
			targetPlayer.getPeer().getName()+".");
		Algorithms.updateLongestRoad(getMap(), getPlayers());
	}
	
	private void sendDisplacedMovement(boolean canMove, Vertex initiater, Vertex target, Vertex newLocation){
		try{
			JSONObject obj=new JSONObject();
			obj.put("action", "displacement_answer");
			obj.put("canMove", canMove);
			obj.put("initiater", initiater.id);
			obj.put("target", target.id);
			if(newLocation!=null)
				obj.put("newLocation", newLocation.id);
			else obj.put("newLocation", target.id);
			getSocket().emit("catan", obj, GameStage.ACK);
			receivedDisplacementAnswer(canMove, initiater, target, newLocation);
			
		}catch(Exception e){e.printStackTrace();}
	}

	@Override
	public void cleanUp() {
		actionDialog.getButtonCity().removeListener(constructionListener);
		actionDialog.getButtonKnight().removeListener(constructionListener);
		actionDialog.getButtonRoad().removeListener(constructionListener);
		actionDialog.getButtonSettlement().removeListener(constructionListener);
		actionDialog.getButtonShip().removeListener(constructionListener);
		actionDialog.getButtonWall().removeListener(constructionListener);
		actionDialog.getButtonUpgradeKnight().removeListener(constructionListener);
		actionDialog.getButtonMoveKnight().removeListener(constructionListener);
		actionDialog.getButtonActivateKnight().removeListener(constructionListener);
		actionDialog.getButtonDisplaceKnight().removeListener(constructionListener);
		getSocket().off("catan", socketListener);
	}

}
