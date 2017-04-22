package commands;

import java.util.HashMap;

import org.json.JSONObject;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import actions.CardAction;
import actions.TurnAction;
import cards.ScienceCard;
import comp361.catan.City;
import comp361.catan.GameStage;
import comp361.catan.Metropolis;
import comp361.catan.Player;
import comp361.catan.ResourceType;
import comp361.catan.Skins;
import comp361.catan.Vertex;
import comp361.catan.VertexConstruction;
import io.socket.client.Ack;

public class UpgradeDisciplineCommand extends GameCommand{

	private static final long serialVersionUID = 368456153713534827L;
	private String playerID;
	private String discipline;
	private boolean craneCard;
	private transient Player player;
	private String socketAction;
	private int level;
	private int metropolisID=-1;
	private ClickListener mapListener;

	public UpgradeDisciplineCommand(String playerID, String discipline, boolean craneCard, int phase) {
		super(phase);
		this.playerID=playerID;
		this.discipline=discipline;
		this.craneCard=craneCard;
		if(craneCard)
			socketAction="progress_card";
		else
			socketAction="upgrade_discipline";
	}
	
	public void setMetropolis(int metropolis){
		this.metropolisID=metropolis;
	}
	
	public void setLevel(int level){
		this.level=level;
	}

	@Override
	protected void executeCommand() {
		player=getPlayers().get(playerID);
		if(getPhase()==0 && player==getPlayer()){
			// This is for the crane progress card only
			phase0();
		}else if(getPhase()==1 && player==getPlayer()){
			phase1();
		}else if(getPhase()==2){
			phase2();
		}
	}
	
	private boolean canUpgrade(String discipline){
		boolean valid=false;
		int currentLevel=player.getPoliticsLevel();
		ResourceType type=ResourceType.COIN;
		if(discipline.equals("trade")){
			currentLevel=getPlayer().getTradeLevel();
			type=ResourceType.CLOTH;
		}else if(discipline.equals("science")){
			currentLevel=getPlayer().getScienceLevel();
			type=ResourceType.PAPER;
		}
		int required=(currentLevel+1);
		if(craneCard)
			required--;
		if(currentLevel<5 && getPlayer().getResources().get(type)>=required){
			if(currentLevel>=3){
				for(VertexConstruction cons:getPlayer().getVertexContructions()){
					if(cons instanceof City){
						City city=(City)cons;
						if(city!=getMap().getTradeMetropolis().getCity() && city!=getMap().getScienceMetropolis().getCity() && city!=getMap().getPoliticsMetropolis().getCity()){
							valid=true;
							break;
						}
					}
				}
			}else valid=true;
		}
		return valid;
	}
	
	private int getCurrentLevel(String discipline){
		if(discipline.equals("trade"))
			return player.getTradeLevel();
		else if(discipline.equals("science"))
			return player.getScienceLevel();
		else if(discipline.equals("politics"))
			return player.getPoliticsLevel();
		return -1;
	}
	
	private void phase0(){
		final Dialog dialog=new Dialog("Choose a discipline", Skins.METAL);
		final HashMap<String, Button> buttons=new HashMap<String, Button>();
		String disciplines[]=new String[]{"politics", "science", "trade"};
		for(String discipline:disciplines){
			if(canUpgrade(discipline)){
				TextButton button=new TextButton(discipline.substring(0, 1).toUpperCase()+discipline.substring(1), Skins.METAL);
				buttons.put(discipline, button);
				dialog.getContentTable().add(button);
				dialog.getContentTable().row();
			}
		}
		if(buttons.size()==0){
			Label label=new Label("You cannot upgrade any discipline.", Skins.METAL);
			label.setWrap(true);
			dialog.getContentTable().add(label).width(150);
			dialog.getContentTable().row();
		}
		final TextButton buttonCancel=new TextButton("Cancel", Skins.METAL);
		dialog.getContentTable().add(buttonCancel);
		dialog.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				for(String discipline:buttons.keySet()){
					if(event.getTarget().isDescendantOf(buttons.get(discipline))){
						dialog.hide();
						UpgradeDisciplineCommand command=new UpgradeDisciplineCommand(playerID, discipline, craneCard, 1);
						command.execute(getStage(), getAction());
						break;
					}
				}
				if(event.getTarget().isDescendantOf(buttonCancel)){
					CardAction action=(CardAction)getAction();
					dialog.hide();
					action.cancelProgressCard();
				}
			}
		});
		dialog.show(getStage());
	}
	
	private void phase1(){
		if(canUpgrade(discipline)){
			TurnAction.LOCKED=true;
			this.level=getCurrentLevel(discipline)+1;
			verifyMetropolis();
		}
	}
	
	private void phase1Done(){
		UpgradeDisciplineCommand command=new UpgradeDisciplineCommand(playerID, discipline, craneCard, 2);
		command.setLevel(level);
		command.setMetropolis(metropolisID);
		JSONObject obj=GameCommand.getJsonCommand(command, socketAction);
		getSocket().emit("catan", obj, GameStage.ACK);
		command.execute(getStage(), getAction());
	}
	
	private void verifyMetropolis(){
		boolean canUpdate=false;
		Metropolis metropolis;
		if(discipline.equals("trade")){
			metropolis=getMap().getTradeMetropolis();
			if((level==4 && metropolis.getCity()==null) ||
					(level==5 && metropolis.getCity().getOwner()!=getPlayer() && metropolis.getCity().getOwner().getTradeLevel()==4)){
				canUpdate=true;
			}
		}else if(discipline.equals("science")){
			metropolis=getMap().getScienceMetropolis();
			if((level==4 && metropolis.getCity()==null) ||
					(level==5 && metropolis.getCity().getOwner()!=getPlayer() && metropolis.getCity().getOwner().getScienceLevel()==4)){
				canUpdate=true;
			}
		}else if(discipline.equals("politics")){
			metropolis=getMap().getPoliticsMetropolis();
			if((level==4 && metropolis.getCity()==null) ||
					(level==5 && metropolis.getCity().getOwner()!=getPlayer() && metropolis.getCity().getOwner().getPoliticsLevel()==4)){
				canUpdate=true;
			}
		}
		if(!canUpdate){
			phase1Done();
		}else{
			getToolbar().getInfoWindow().displayInfoMessage("Choose where to place the metropolis");
			getMap().addListener(mapListener=new ClickListener(){
				@Override
				public void clicked(InputEvent event, float x, float y) {
					Vertex vertex=getMap().getCurrentVertex();
					if(vertex!=null && vertex.getConstruction() instanceof City && vertex.getConstruction().getOwner()==getPlayer()){
						getMap().removeListener(mapListener);
						UpgradeDisciplineCommand.this.metropolisID=vertex.id;
						phase1Done();
					}
				}
			});
		}
	}
	
	private void phase2(){
		Metropolis metropolis=null;
		if(discipline.equals("trade")){
			metropolis=getMap().getTradeMetropolis();
			player.setTradeLevel(level);
			player.getResources().put(ResourceType.CLOTH, player.getResources().get(ResourceType.CLOTH)-level);
		}else if(discipline.equals("politics")){
			metropolis=getMap().getPoliticsMetropolis();
			player.setPoliticsLevel(level);
			player.getResources().put(ResourceType.COIN, player.getResources().get(ResourceType.COIN)-level);
		}else if(discipline.equals("science")){
			metropolis=getMap().getScienceMetropolis();
			player.setScienceLevel(level);
			player.getResources().put(ResourceType.PAPER, player.getResources().get(ResourceType.PAPER)-level);
		}
		
		String message="Player "+player.getPeer().getName()+" has upgraded his "+discipline+" to level "+level;
		if(craneCard)
			message+=" with the crane progress card";
		else message+=".";
		
		if(metropolisID!=-1){
			metropolis.setCity((City)getMap().getVertex(metropolisID).getConstruction());
			message+="He has gained a metropolis.";
		}
		
		getToolbar().getChatWindow().log(message);
		TurnAction.LOCKED=false;
		getToolbar().getChartWindow().refresh();
		getToolbar().getInfoWindow().clearMessage();
		if(craneCard){
			CardAction action=(CardAction)getAction();
			player.getCards().remove(ScienceCard.CRANE);
			getCards().add(ScienceCard.CRANE);
			action.cancelProgressCard();
		}
	}

}
