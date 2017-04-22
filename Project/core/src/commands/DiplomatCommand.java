package commands;

import org.json.JSONObject;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import actions.CardAction;
import algorithms.Algorithms;
import cards.PoliticsCard;
import comp361.catan.Edge;
import comp361.catan.GameStage;
import comp361.catan.Player;
import comp361.catan.Road;
import comp361.catan.Skins;
import io.socket.client.Ack;

public class DiplomatCommand extends GameCommand{

	private static final long serialVersionUID = -1446588078983381216L;
	private String playerID;
	private int edgeID;
	private ClickListener mapListener;
	private transient Player player;
	private int newEdgeID=-1;
	
	public DiplomatCommand(String playerID, int edgeID, int phase){
		super(phase);
		this.playerID=playerID;
		this.edgeID=edgeID;
	}
	
	public void setNewEdge(int newEdgeID){
		this.newEdgeID=newEdgeID;
	}

	@Override
	protected void executeCommand() {
		player=getPlayers().get(playerID);
		
		if(getPhase()==1 && player==getPlayer()){
			phase1();
		}else if(getPhase()==2){
			phase2();
		}else if(getPhase()==3){
			phase3();
		}
	}
	
	private void phase3(){
		Edge newEdge=getMap().getEdge(newEdgeID);
		Road road=new Road(newEdge, player);
		newEdge.setConstruction(road);
		getToolbar().getChatWindow().log("Player "+player.getPeer().getName()+" has placed the edge he removed with "+
				"the diplomat progress card at a new location.");
		done();
	}
	
	private void phase2(){
		Edge edge=getMap().getEdge(edgeID);
		String owner=edge.getConstruction().getOwner().getPeer().getName();
		boolean owned=(edge.getConstruction().getOwner()!=player);
		player.getEdgeConstructions().remove(edge.getConstruction());
		edge.setConstruction(null);
		if(owned){
			getToolbar().getChatWindow().log("Player "+player.getPeer().getName()+" has removed a road owned by "+
					owner+" with the diplomat card");
			done();
		}else{
			getToolbar().getChatWindow().log("Player "+player.getPeer().getName()+
					" has removed one of his roads with the diplomat card");
			if(player!=getPlayer())
				return;
			final TextButton buttonYes=new TextButton("Yes", Skins.METAL);
			final TextButton buttonNo=new TextButton("No", Skins.METAL);
			Label label=new Label("Do you want to replace the road you removed?", Skins.METAL);
			label.setWrap(true);
			final Dialog replaceDialog=new Dialog("Replace", Skins.METAL);
			replaceDialog.getContentTable().add(label).width(100);
			replaceDialog.getContentTable().row();
			replaceDialog.getContentTable().add(buttonYes);
			replaceDialog.getContentTable().row();
			replaceDialog.getContentTable().add(buttonNo);
			replaceDialog.show(getStage());
			
			replaceDialog.addListener(new ClickListener(){
				@Override
				public void clicked(InputEvent event, float x, float y) {
					if(event.getTarget().isDescendantOf(buttonNo)){
						replaceDialog.hide();
						done();
					}else if(event.getTarget().isDescendantOf(buttonYes)){
						replaceDialog.hide();
						getToolbar().getInfoWindow().displayInfoMessage("Choose where to place your road");
						getMap().addListener(mapListener=new ClickListener(){
							@Override
							public void clicked(InputEvent event, float x, float y) {
								Edge selection=getMap().getCurrentEdge();
								if(selection!=null && selection.getConstruction()==null){
									boolean valid=false;
									for(Edge e:selection.first.edges)
										valid|=(e!=selection && e.getConstruction()!=null && e.getConstruction().getOwner()==getPlayer());
									for(Edge e:selection.second.edges)
										valid|=(e!=selection && e.getConstruction()!=null && e.getConstruction().getOwner()==getPlayer());
									valid|=(selection.first.getConstruction()!=null && selection.first.getConstruction().getOwner()==getPlayer());
									valid|=(selection.second.getConstruction()!=null && selection.second.getConstruction().getOwner()==getPlayer());
									if(valid){
										getToolbar().getInfoWindow().clearMessage();
										getMap().removeListener(mapListener);
										DiplomatCommand command=new DiplomatCommand(playerID, edgeID, 3);
										command.setNewEdge(selection.id);
										JSONObject obj=GameCommand.getJsonCommand(command, "progress_card");
										getSocket().emit("catan", obj, GameStage.ACK);
										command.execute(getStage(), getAction());
									}
								}
							}
						});
					}
				}
			});
		}
	}
	
	private void done(){
		CardAction action=(CardAction)getAction();
		player.getCards().remove(PoliticsCard.DIPLOMAT);
		getCards().add(PoliticsCard.DIPLOMAT);
		action.cancelProgressCard();
		Algorithms.updateLongestRoad(getMap(), getPlayers());
	}
	
	private void phase1(){
		getToolbar().getInfoWindow().displayInfoMessage("Choose which edge to remove");
		getMap().addListener(mapListener=new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Edge edge=getMap().getCurrentEdge();
				if(edge.getConstruction() instanceof Road){
					int count=0;
					if(edge.first.getConstruction()==null){
						for(Edge e:edge.first.edges)
							if(e.getConstruction()!=null)
								count++;
					}
					if(count!=1 && edge.second.getConstruction()==null){
						count=0;
						for(Edge e:edge.second.edges)
							if(e.getConstruction()!=null)
								count++;
					}
					if(count==1){
						getToolbar().getInfoWindow().clearMessage();
						getMap().removeListener(mapListener);
						DiplomatCommand command=new DiplomatCommand(playerID, edge.id, 2);
						JSONObject obj=GameCommand.getJsonCommand(command, "progress_card");
						getSocket().emit("catan", obj, GameStage.ACK);
						command.execute(getStage(), getAction());
					}
				}
			}
		});
	}
	
}
