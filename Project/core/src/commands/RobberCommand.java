package commands;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONObject;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import comp361.catan.City;
import comp361.catan.GameStage;
import comp361.catan.Player;
import comp361.catan.Resource;
import comp361.catan.ResourceType;
import comp361.catan.Settlement;
import comp361.catan.Skins;
import comp361.catan.Tile;
import comp361.catan.TileType;
import comp361.catan.Vertex;
import ui.DiscardDialog;

public class RobberCommand extends GameCommand{
	
	private static final long serialVersionUID = -4015792593538340758L;
	private String playerID, targetID;
	private transient Player player;
	private HashMap<ResourceType, Integer> resources;
	private transient ClickListener mapListener;
	private int tileID;
	private ResourceType stolen;
	private int redDie, yellowDie;
	private String eventDie;
	private String bootTarget;

	public RobberCommand(String playerID, int phase){
		super(phase);
		this.playerID=playerID;
	}
	
	public void setDie(int redDie, int yellowDie, String eventDie, String bootTarget){
		this.redDie=redDie;
		this.yellowDie=yellowDie;
		this.eventDie=eventDie;
		this.bootTarget=bootTarget;
	}
	
	public void setStolen(ResourceType stolen){
		this.stolen=stolen;
	}
	
	public void setTile(int tileID){
		this.tileID=tileID;
	}
	
	public void setTarget(String targetID){
		this.targetID=targetID;
	}
	
	public void setResources(HashMap<ResourceType, Integer> resources){
		this.resources=resources;
	}
	
	protected void executeCommand() {
		player=getPlayers().get(playerID);
		
		if(getPhase()==1){
			phase1();
		}else if(getPhase()==2){
			phase2();
		}else if(getPhase()==3){
			phase3();
		}else if(getPhase()==4){
			phase4();
		}
	}
	
	private void phase4(){
		if(stolen==null){
			getToolbar().getChatWindow().log("No resources were stolen by "+player.getPeer().getName());
		}else{
			Player target=getPlayers().get(targetID);
			target.getResources().put(stolen, target.getResources().get(stolen)-1);
			player.getResources().put(stolen, player.getResources().get(stolen)+1);
			getToolbar().getChatWindow().log("Player "+player.getPeer().getName()+" has stolen a resource/commodity from "+target.getPeer().getName());
		}
		if(player==getPlayer()){
			RollCommand command=new RollCommand(playerID, redDie, yellowDie, eventDie, null, bootTarget, 7);
			JSONObject obj=GameCommand.getJsonCommand(command, "roll");
			getSocket().emit("catan", obj, GameStage.ACK);
			command.execute(getStage(), getAction());
		}
	}
	
	private void phase3(){
		Tile tile=getMap().getTile(tileID);
		String message="Player "+player.getPeer().getName()+" has moved the ";
		if(tile.getTileType()==TileType.OCEAN){
			message+="pirate.";
			getMap().getPirate().setLocation(tile);
		}else{
			message+="robber.";
			getMap().getRobber().setLocation(tile);
		}
		getToolbar().getChatWindow().log(message);
		if(player==getPlayer()){
			final HashMap<Player, Button> targets=new HashMap<Player, Button>();
			final Dialog targetDialog=new Dialog("Choose a player", Skins.METAL);
			for(Vertex v:tile.vertices){
				if((v.getConstruction() instanceof City || v.getConstruction() instanceof Settlement) && v.getConstruction().getOwner()!=getPlayer()){
					Player owner=v.getConstruction().getOwner();
					if(Resource.calculateTotalResourcesCommodities(owner)>0 && !targets.containsKey(owner)){
						TextButton button=new TextButton(owner.getPeer().getName(), Skins.METAL);
						targetDialog.getContentTable().add(button);
						targetDialog.getContentTable().row();
						targets.put(owner, button);
					}
				}
			}
			if(targets.size()==0){
				Label labelInfo=new Label("There are no players with resources/commodities to steal from at the location you moved the robber or pirate.", Skins.METAL);
				Dialog diag=new Dialog("", Skins.METAL).button("ok");
				diag.getContentTable().add(labelInfo);
				diag.show(getStage());
				RobberCommand command=new RobberCommand(playerID, 4);
				command.setDie(redDie, yellowDie, eventDie, bootTarget);
				JSONObject obj=GameCommand.getJsonCommand(command, "roll");
				getSocket().emit("catan", obj, GameStage.ACK);
				command.execute(getStage(), getAction());
			}else{
				targetDialog.show(getStage());
				targetDialog.addListener(new ClickListener(){
					@Override
					public void clicked(InputEvent event, float x, float y) {
						for(Player p:targets.keySet()){
							if(event.getTarget().isDescendantOf(targets.get(p))){
								targetDialog.hide();
								ArrayList<ResourceType> owned=new ArrayList<ResourceType>();
								for(ResourceType type:p.getResources().keySet()){
									for(int i=0;i<p.getResources().get(type);i++){
										owned.add(type);
									}
								}
								int index=(int)(Math.random()*owned.size());
								RobberCommand command=new RobberCommand(playerID, 4);
								command.setDie(redDie, yellowDie, eventDie, bootTarget);
								command.setStolen(owned.get(index));
								command.setTarget(p.getPeer().getId());
								JSONObject obj=GameCommand.getJsonCommand(command, "roll");
								getSocket().emit("catan", obj, GameStage.ACK);
								command.execute(getStage(), getAction());
								break;
							}
						}
					}
				});
			}
		}
	}
	
	private void phase2(){
		if(targetID!=null){
			Player target=getPlayers().get(targetID);
			for(ResourceType type:target.getResources().keySet())
				target.getResources().put(type,  resources.get(type));
			getToolbar().getChatWindow().log("Player "+target.getPeer().getName()+" has discarded half his commodities/resources.");
		}
		if(player==getPlayer()){
			ArrayList<String> remaining=(ArrayList<String>)GameStage.STORE.get("robberRemaining");
			if(targetID!=null)
				remaining.remove(targetID);
			boolean done=remaining.size()==0;
			System.out.println("first? "+getMap().getFirstBarbarianAttack());
			if(done && getMap().getFirstBarbarianAttack()){
				moveRobber();
			}else if(done){
				RollCommand command=new RollCommand(playerID, redDie, yellowDie, eventDie, null, bootTarget, 7);
				JSONObject obj=GameCommand.getJsonCommand(command, "roll");
				getSocket().emit("catan", obj, GameStage.ACK);
				command.execute(getStage(), getAction());
			}
		}
	}
	
	private void moveRobber(){
		getToolbar().getInfoWindow().displayInfoMessage("Choose where to move the robber/pirate");
		getMap().addListener(mapListener=new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Tile tile=getMap().getCurrentTile();
				if(tile!=null && getMap().getRobber().getLocation()!=tile && getMap().getPirate().getLocation()!=tile){
					getMap().removeListener(mapListener);
					RobberCommand command=new RobberCommand(playerID, 3);
					command.setDie(redDie, yellowDie, eventDie, bootTarget);
					command.setTile(tile.id);
					JSONObject obj=GameCommand.getJsonCommand(command, "roll");
					getSocket().emit("catan",  obj, GameStage.ACK);
					command.execute(getStage(), getAction());
				}
			}
		});
	}
	
	private void phase1(){
		int owned=Resource.calculateTotalResourcesCommodities(getPlayer());
		if(owned>7){
			final int required=owned/2;
			getToolbar().getInfoWindow().displayInfoMessage("A 7 has been rolled. You need to discard half your resources/commodities.");
			final DiscardDialog dialog=new DiscardDialog(required, getPlayer());
			dialog.show(getStage());
			dialog.getButtonDone().addListener(new ClickListener(){
				@Override
				public void clicked(InputEvent event, float x, float y) {
					if(dialog.getTotal()!=required){
						new Dialog("You need to discard a total of "+required+" resources/commodities", Skins.METAL).button("ok").show(getStage());
					}else{
						getToolbar().getInfoWindow().clearMessage();
						resources=new HashMap<ResourceType, Integer>();
						for(ResourceType type:dialog.getSelection().keySet()){
							int num=dialog.getSelection().get(type);
							resources.put(type, getPlayer().getResources().get(type)-num);
						}
						dialog.hide();
						RobberCommand command=new RobberCommand(playerID, 2);
						command.setTarget(getPlayer().getPeer().getId());
						command.setResources(resources);
						command.setDie(redDie,  yellowDie, eventDie, bootTarget);
						JSONObject obj=GameCommand.getJsonCommand(command, "roll");
						getSocket().emit("catan", obj, GameStage.ACK);
						command.execute(getStage(), getAction());
					}
				}
			});
		}else if(player==getPlayer()){ // In the case that no player has more than 7 resources
			boolean hasSeven=false;
			for(Player p:getPlayers().values()){
				if(Resource.calculateTotalResourcesCommodities(p)>7)
					hasSeven=true;
			}
			if(!hasSeven){
				RobberCommand command=new RobberCommand(playerID, 2);
				command.setDie(redDie,  yellowDie, eventDie, bootTarget);
				JSONObject obj=GameCommand.getJsonCommand(command, "roll");
				getSocket().emit("catan", obj, GameStage.ACK);
				command.execute(getStage(), getAction());
			}
		}
	}
}
