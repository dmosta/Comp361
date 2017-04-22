package actions;

import java.util.ArrayList;

import org.json.JSONObject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import algorithms.Algorithms;
import comp361.catan.City;
import comp361.catan.Edge;
import comp361.catan.GameStage;
import comp361.catan.GameState;
import comp361.catan.Knight;
import comp361.catan.Notification;
import comp361.catan.NotificationAdapter;
import comp361.catan.Player;
import comp361.catan.ResourceType;
import comp361.catan.Settlement;
import comp361.catan.Skins;
import comp361.catan.Tile;
import comp361.catan.TileType;
import comp361.catan.Vertex;
import comp361.catan.VertexConstruction;
import io.socket.client.Ack;
import io.socket.emitter.Emitter.Listener;
import ui.ActionDialog;

/**
 * This game action handles the second phase of turns. It contains many sub-actions such
 * as place constructions and trade. It ends when the current player ends his turn.
 */
public class TurnAction extends GameAction{
	
	public static boolean LOCKED=false;
	private ConstructionAction constructionAction;
	private TradeAction tradeAction;
	private CardAction cardAction;
	private DevelopmentAction developmentAction;
	private FishAction fishAction;
	private CheatAction cheatAction;
	private ClickListener turnListener;
	private Listener socketListener;
	private ClickListener mapListener;
	private InputListener mapMouseListener;
	private Vertex previousVertex;
	private Edge previousEdge;

	public TurnAction(Notification notif, GameStage gameStage) {
		super(notif, gameStage);
		GameStage.GAME_STATE=GameState.TURN_SECOND_PHASE;
		gameStage.getToolbar().toggleTurnButtonImage(false);

		for(Vertex vertex:getMap().getVertices().values())
			if(vertex.getConstruction() instanceof Knight){
				Knight knight=(Knight)vertex.getConstruction();
				knight.setPromoted(false);
				knight.setActivatedThisTurn(false);
			}
		
		constructionAction=new ConstructionAction(new NotificationAdapter(), getStage());
		tradeAction=new TradeAction(new NotificationAdapter(), getStage());
		cardAction=new CardAction(new NotificationAdapter(), getStage());
		developmentAction=new DevelopmentAction(new NotificationAdapter(), getStage());
		fishAction=new FishAction(new NotificationAdapter(), getStage());
		cheatAction=new CheatAction(new NotificationAdapter(), getStage());
		gameStage.getToolbar().getTurnButton().addListener(turnListener=new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if(getStage().getCurrentPlayer().isTurn())
					endTurn();
			}
		});
		prepareActionDialog();
		setupSocket();
	}
	
	private boolean canPlaceSettlement(){
		Player player=getPlayer();
		if(previousVertex==null || !previousVertex.selected || player.getSettlementsRemaining()==0 || previousVertex.getConstruction()!=null)
			return false;
		int lumber=player.getResources().get(ResourceType.LUMBER);
		int brick=player.getResources().get(ResourceType.BRICK);
		int grain=player.getResources().get(ResourceType.GRAIN);
		int wool=player.getResources().get(ResourceType.WOOL);
		boolean distanceRule=true;
		boolean roadRule=false;
		boolean landRule=false;
		for(Edge edge:previousVertex.edges){
			distanceRule&=((edge.first.getConstruction()==null || edge.first.getConstruction() instanceof Knight)
					&& (edge.second.getConstruction()==null || edge.second.getConstruction() instanceof Knight));
			roadRule|=(edge.getConstruction()!=null && edge.getConstruction().getOwner()==player);
		}
		for(Tile tile:previousVertex.neighbors)
			landRule|=tile.getTileType()!=TileType.OCEAN;
		distanceRule&=landRule;
		distanceRule&=roadRule;
		return distanceRule && lumber>0 && brick>0 && grain>0 && wool>0;
	}
	
	private boolean canPlaceCity(){
		Player player=getPlayer();
		if(previousVertex==null || !previousVertex.selected || player.getCitiesRemaining()==0)
			return false;
		int ore=player.getResources().get(ResourceType.ORE);
		int grain=player.getResources().get(ResourceType.GRAIN);
		VertexConstruction cons=previousVertex.getConstruction();
		return cons instanceof Settlement && cons.getOwner()==player
				&& ore>2 && grain>1;
	}
	
	private boolean canPlaceRoad(){
		if(previousEdge==null || !previousEdge.selected)
			return false;
		int brick=getPlayer().getResources().get(ResourceType.BRICK);
		int lumber=getPlayer().getResources().get(ResourceType.LUMBER);
		return Algorithms.isEdgeValid(previousEdge, getPlayer(), true, false, getMap()) && brick>0 && lumber>0;
	}
	
	private boolean canPlaceShip(){
		if(previousEdge==null || !previousEdge.selected)
			return false;
		int brick=getPlayer().getResources().get(ResourceType.WOOL);
		int lumber=getPlayer().getResources().get(ResourceType.LUMBER);
		return Algorithms.isEdgeValid(previousEdge, getPlayer(), false, true, getMap()) && brick>0 && lumber>0;
	}
	
	private boolean canPlaceKnight(){
		if(previousVertex==null || !previousVertex.selected || getPlayer().getBasicKnightsRemaining()==0)
			return false;
		int ore=getPlayer().getResources().get(ResourceType.ORE);
		int wool=getPlayer().getResources().get(ResourceType.WOOL);
		boolean valid=false;
		for(Edge e:previousVertex.edges)
			valid|=(e.getConstruction()!=null && e.getConstruction().getOwner()==getPlayer());
		boolean land=false;
		for(Tile tile:previousVertex.neighbors)
			land|=tile.getTileType()!=TileType.OCEAN;
		return valid && land && previousVertex.getConstruction()==null && ore>0 && wool>0;
	}
	
	private boolean canPlaceWall(){
		if(previousVertex==null || !previousVertex.selected)
			return false;
		int brick=getPlayer().getResources().get(ResourceType.BRICK);
		return previousVertex.getConstruction() instanceof City && getPlayer().getNumWalls()<3
				&& previousVertex.getConstruction().getOwner()==getPlayer() && brick>1 && !((City)previousVertex.getConstruction()).hasWall();
	}
	
	private boolean canActivateKnight(){
		if(previousVertex==null || !previousVertex.selected)
			return false;
		boolean valid=previousVertex.getConstruction() instanceof Knight;
		int grain=getPlayer().getResources().get(ResourceType.GRAIN);
		if(valid){
			Knight knight=(Knight)previousVertex.getConstruction();
			valid&=knight.getOwner()==getPlayer() && !knight.isActive() && grain>0;
		}
		return valid;
	}
	
	private boolean canDisplaceKnight(){
		if(previousVertex==null || !previousVertex.selected)
			return false;
		boolean valid=false;
		if(previousVertex.getConstruction() instanceof Knight && previousVertex.getConstruction().getOwner()!=getPlayer()){
			valid=Algorithms.isConnectedToBetterKnight(previousVertex, getPlayer());
		}
		return valid;
	}
	
	private boolean canMoveKnight(){
		if(previousVertex==null || !previousVertex.selected)
			return false;
		boolean valid=previousVertex.getConstruction() instanceof Knight;
		if(valid){
			Knight knight=(Knight)previousVertex.getConstruction();
			valid&=knight.isActive()&&!knight.wasActivatedThisTurn();
			boolean possible=false;
			ArrayList<Vertex> reachable=new ArrayList<Vertex>();
			ArrayList<Vertex> visited=new ArrayList<Vertex>();
			reachable.add(previousVertex);
			Vertex current;
			int count=0;
			while(!reachable.isEmpty()){
				current=reachable.remove(0);
				current.visited=true;
				visited.add(current);
				if(current.getConstruction()==null){
					possible=true;
					break;
				}
				for(Edge e:current.edges){
					if(e.getConstruction()!=null && e.getConstruction().getOwner()==getPlayer()){
						if(!e.first.visited)
							reachable.add(e.first);
						if(!e.second.visited)
							reachable.add(e.second);
					}
				}
			}
			for(Vertex v:visited)
				v.visited=false;
			valid&=possible;
		}
		return valid;
	}
	
	private boolean canUpgradeKnight(){
		if(previousVertex==null || !previousVertex.selected)
			return false;
		boolean valid=previousVertex.getConstruction() instanceof Knight;
		int wool=getPlayer().getResources().get(ResourceType.WOOL);
		int ore=getPlayer().getResources().get(ResourceType.ORE);
		if(valid){
			Knight knight=(Knight)previousVertex.getConstruction();
			boolean remaining=false;
			if(knight.getLevel()==1)
				remaining=getPlayer().getStrongKnightsRemaining()>0;
			else if(knight.getLevel()==2)
				remaining=getPlayer().getMightyKnightsRemaining()>0;
			valid&=knight.getOwner()==getPlayer() && knight.getLevel()<(getPlayer().getPoliticsLevel()>=3?3:2) && !knight.isPromoted()
					&& wool>0 && ore>0 && remaining;
		}
		return valid;
	}
	
	private void prepareActionDialog(){
		getMap().addListener(mapListener=new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				ActionDialog dialog=getToolbar().getActionDialog();
				if(getPlayer().isTurn() && !TurnAction.LOCKED){
					dialog.setVertex(previousVertex);
					dialog.setEdge(previousEdge);
					int count=0;
					boolean canPlaceSettlement=canPlaceSettlement();
					if(canPlaceSettlement)
						count++;
					dialog.getButtonSettlement().setVisible(canPlaceSettlement);
					boolean canPlaceCity=canPlaceCity();
					if(canPlaceCity)
						count++;
					dialog.getButtonCity().setVisible(canPlaceCity);
					boolean canPlaceRoad=canPlaceRoad();
					if(canPlaceRoad)
						count++;
					dialog.getButtonRoad().setVisible(canPlaceRoad);
					boolean canPlaceShip=canPlaceShip();
					if(canPlaceShip)
						count++;
					dialog.getButtonShip().setVisible(canPlaceShip);
					boolean canPlaceKnight=canPlaceKnight();
					if(canPlaceKnight)
						count++;
					dialog.getButtonKnight().setVisible(canPlaceKnight);
					boolean canPlaceWall=canPlaceWall();
					if(canPlaceWall)
						count++;
					dialog.getButtonWall().setVisible(canPlaceWall);
					boolean canUpgradeKnight=canUpgradeKnight();
					if(canUpgradeKnight)
						count++;
					dialog.getButtonUpgradeKnight().setVisible(canUpgradeKnight);
					boolean canMoveKnight=canMoveKnight();
					if(canMoveKnight)
						count++;
					dialog.getButtonMoveKnight().setVisible(canMoveKnight);
					boolean canActivateKnight=canActivateKnight();
					if(canActivateKnight)
						count++;
					dialog.getButtonActivateKnight().setVisible(canActivateKnight);
					boolean canDisplaceKnight=canDisplaceKnight();
					if(canDisplaceKnight)
						count++;
					dialog.getButtonDisplaceKnight().setVisible(canDisplaceKnight);
					if(count>0)
						dialog.show(getStage());
				}
			}
		});
		getMap().addListener(mapMouseListener=new InputListener(){
			@Override
			public boolean mouseMoved(InputEvent event, float x, float y) {
				if(previousVertex!=null)
					previousVertex.selected=false;
				if(previousEdge!=null)
					previousEdge.selected=false;
				previousVertex=getMap().getCurrentVertex();
				previousEdge=getMap().getCurrentEdge();
				if(previousEdge!=null &&  previousVertex!=null && getMap().isEdgeClosest()){
					previousEdge.selected=true;
				}else if (previousVertex!=null)
					previousVertex.selected=true;
				return true;
			}
		});
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
							switch(obj.getString("action")){
								case "end_turn":
									prepareNextTurn();
									break;
								default:
									return;
							}
							ack.call();
						}catch(Exception e){e.printStackTrace();}
					}
				});
			}
		});
	}
	
	private void endTurn(){
		if(TurnAction.LOCKED){
			new Dialog("", Skins.METAL).text("Please finish your current action before ending your turn.").button("ok").show(getStage());
			return;
		}
		try{
			JSONObject obj=new JSONObject();
			obj.put("action", "end_turn");
			getSocket().emit("catan", obj, GameStage.ACK);
			prepareNextTurn();
		}catch(Exception e){e.printStackTrace();}
	}
	
	private void prepareNextTurn(){
		cleanUp();
		int order=0;
		for(Player p:getStage().getPlayers().values())
			if(p.isTurn())
				order=p.getOrder();
		int next=(order%getStage().getPlayers().size())+1;
		for(Player player:getStage().getPlayers().values()){
			player.setTurn(next==player.getOrder());
			player.getTurnInfo().newTurn();
		}
		getNotification().actionCompleted(new RollAction(getNotification(), getStage()));
	}

	@Override
	public void cleanUp() {
		getStage().getToolbar().getTurnButton().removeListener(turnListener);
		getSocket().off("catan", socketListener);
		getMap().removeListener(mapListener);
		getMap().removeListener(mapMouseListener);
		constructionAction.cleanUp();
		tradeAction.cleanUp();
		cardAction.cleanUp();
		developmentAction.cleanUp();
		fishAction.cleanUp();
		cheatAction.cleanUp();
	}

}
