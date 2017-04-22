package commands;

import java.util.HashMap;

import org.json.JSONObject;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import actions.CardAction;
import algorithms.Algorithms;
import cards.PoliticsCard;
import comp361.catan.Edge;
import comp361.catan.GameStage;
import comp361.catan.Knight;
import comp361.catan.Player;
import comp361.catan.Skins;
import comp361.catan.Vertex;
import comp361.catan.VertexConstruction;
import io.socket.client.Ack;

public class IntrigueCommand extends GameCommand{

	private static final long serialVersionUID = -9056754957340760800L;
	String senderID;
	String targetID;
	private transient Player sender;
	private transient Player target;
	private ClickListener mapListener;
	private transient CardAction action;
	private ClickListener cancelListener;
	private int targetVertexID=-1;
	private transient Vertex targetVertex;
	private int targetMoveID=-1;
	private int newKnightID=-1;
	
	public IntrigueCommand(String senderID, String targetID, int phase){
		super(phase);
		this.senderID=senderID;
		this.targetID=targetID;
	}
	
	public void setTargetMove(int targetMoveID){
		this.targetMoveID=targetMoveID;
	}
	
	public void setTargetKnight(int targetVertexID){
		this.targetVertexID=targetVertexID;
	}
	
	public void setNewKnight(int newKnightID){
		this.newKnightID=newKnightID;
	}

	@Override
	protected void executeCommand() {
		action=(CardAction)getAction();
		sender=getPlayers().get(senderID);
		if(getPlayers().containsKey(targetID))
			target=getPlayers().get(targetID);
		if(targetVertexID!=1)
			targetVertex=getMap().getVertex(targetVertexID);

		if(getPhase()==1 && sender==getPlayer()){
			phase1();
		}else if(getPhase()==2 && target==getPlayer()){
			phase2();
		}else if(getPhase()==3){
			phase3();
		}
	}
	
	private void phase3(){
		Knight targetKnight=(Knight)targetVertex.getConstruction();
		targetVertex.setConstruction(null);
		if(targetMoveID==-1){
			target.getVertexContructions().remove(targetKnight);
			if(targetKnight.getLevel()==1)
				target.setBasicKnightsRemaining(target.getBasicKnightsRemaining()+1);
			else if(targetKnight.getLevel()==2)
				target.setStrongKnightsRemaining(target.getStrongKnightsRemaining()+1);
			else if(targetKnight.getLevel()==3)
				target.setMightyKnightsRemaining(target.getMightyKnightsRemaining()+1);
		}else{
			Vertex move=getMap().getVertex(targetMoveID);
			move.setConstruction(targetKnight);
			targetKnight.setParent(move);
		}
		
		if(sender.getBasicKnightsRemaining()>0){
			Knight newKnight=new Knight(targetVertex, sender);
			targetVertex.setConstruction(newKnight);
			sender.setBasicKnightsRemaining(sender.getBasicKnightsRemaining()-1);
			getToolbar().getChatWindow().log("Player "+sender.getPeer().getName()+" has displaced a knight owned by"+
					target.getPeer().getName()+" with the intrigue progress card. He has moved a basic knight to the displaced location.");
		}else{
			getToolbar().getChatWindow().log("Player "+sender.getPeer().getName()+" has displaced a knight owned by"+
					target.getPeer().getName()+" with the intrigue progress card. He could not move a basic knight to the displaced location,"
							+ "since his two basics knights are already on the game board.");
		}
		sender.getCards().remove(PoliticsCard.INTRIGUE);
		getCards().add(PoliticsCard.INTRIGUE);
		action.cancelProgressCard();
		Algorithms.updateLongestRoad(getMap(), getPlayers());
	}
	
	private void phase2(){
		final IntrigueCommand command=new IntrigueCommand(senderID, targetID, 3);
		command.setTargetKnight(targetVertexID);
		boolean valid=Algorithms.isConnectedToUnoccupiedVertex(targetVertex, getPlayer());
		if(!valid){
			getToolbar().getInfoWindow().displayInfoMessage("Player "+sender.getPeer().getName()+" has displaced your knight with the intrigue progress card (it cannot be moved).");
			sendCommand(command);
		}else{
			targetVertex.highlight=true;
			getToolbar().getInfoWindow().displayInfoMessage("Player "+sender.getPeer().getName()+" has displaced your knight with the intrigue progress card. Select where you want to move it");
			getMap().addListener(mapListener=new ClickListener(){
				@Override
				public void clicked(InputEvent event, float x, float y) {
					Vertex vertex=getMap().getCurrentVertex();
					if(vertex!=null && vertex.getConstruction()==null && Algorithms.areConnected(targetVertex, vertex, getPlayer())){
						targetVertex.highlight=false;
						getToolbar().getInfoWindow().clearMessage();
						getMap().removeListener(mapListener);
						command.setTargetMove(vertex.id);
						sendCommand(command);
					}
				}
			});
		}
	}
	
	private void sendCommand(IntrigueCommand command){
		JSONObject obj=GameCommand.getJsonCommand(command, "progress_card");
		getSocket().emit("catan", obj, GameStage.ACK);
		command.execute(getStage(), action);
	}
	
	private void phase1(){
		getToolbar().getInfoWindow().displayInfoMessage("Choose which knight to displace");
		getToolbar().activateCancelButton(true, "intrigue card");
		getMap().addListener(mapListener=new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Vertex vertex=getMap().getCurrentVertex();
				if(vertex.getConstruction() instanceof Knight && vertex.getConstruction().getOwner()!=getPlayer()){
					boolean valid=false;
					for(Edge e:vertex.edges)
						valid|=(e.getConstruction()!=null && e.getConstruction().getOwner()==getPlayer());
					if(valid){
						getMap().removeListener(mapListener);
						getToolbar().getCancelButton().removeListener(cancelListener);
						getToolbar().activateCancelButton(false, "");
						getToolbar().getInfoWindow().clearMessage();
						Dialog waitingDialog=new Dialog("Waiting for other player...", Skins.METAL).show(getStage());
						action.addTemporaryDialog(waitingDialog);
						IntrigueCommand command=new IntrigueCommand(sender.getPeer().getId(), vertex.getConstruction().getOwner().getPeer().getId(), 2);
						command.setTargetKnight(vertex.id);
						JSONObject obj=GameCommand.getJsonCommand(command, "progress_card");
						getSocket().emit("catan", obj, GameStage.ACK);
					}
				}
			}
		});
		getToolbar().getCancelButton().addListener(cancelListener=new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				getToolbar().getInfoWindow().clearMessage();
				getToolbar().getCancelButton().removeListener(cancelListener);
				getMap().removeListener(mapListener);
				getToolbar().activateCancelButton(false, "");
				action.cancelProgressCard();
			}
		});
	}

}
