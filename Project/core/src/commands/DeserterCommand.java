package commands;

import org.json.JSONObject;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import actions.CardAction;
import actions.TurnAction;
import algorithms.Algorithms;
import cards.PoliticsCard;
import comp361.catan.Edge;
import comp361.catan.EdgeConstruction;
import comp361.catan.GameStage;
import comp361.catan.Knight;
import comp361.catan.Player;
import comp361.catan.Vertex;
import io.socket.client.Ack;

public class DeserterCommand extends GameCommand{

	private static final long serialVersionUID = 914861551279056620L;
	private String senderID, targetID;
	private int loseVertexID=-1, gainVertexID=-1;
	private int loseLevel=-1, gainLevel=-1;
	private transient Player sender;
	private transient Player target;
	private transient Vertex loseVertex;
	private transient Vertex gainVertex;
	private ClickListener mapListener;
	private transient CardAction action;
	
	public DeserterCommand(String senderID, String targetID, int phase){
		super(phase);
		this.senderID=senderID;
		this.targetID=targetID;
	}
	
	public void setLoss(int loseVertexID, int loseLevel){
		this.loseVertexID=loseVertexID;
		this.loseLevel=loseLevel;
	}
	
	public void setGain(int gainVertexID, int gainLevel){
		this.gainVertexID=gainVertexID;
		this.gainLevel=gainLevel;
	}

	@Override
	protected void executeCommand() {
		action=(CardAction)getAction();
		sender=getPlayers().get(senderID);
		target=getPlayers().get(targetID);
		if(loseVertexID!=-1)
			loseVertex=getMap().getVertex(loseVertexID);
		if(gainVertexID!=-1)
			gainVertex=getMap().getVertex(gainVertexID);
		
		if(getPhase()==1 && target==getPlayer()){
			phase1();
		}else if(getPhase()==2){
			phase2();
		}else if(getPhase()==3){
			phase3();
		}
	}
	
	private void phase3(){
		if(gainVertex==null)
			getToolbar().getChatWindow().log("Player "+sender.getPeer().getName()+" did not gain any knight with his deserter card");
		else{
			getToolbar().getChatWindow().log("Player "+sender.getPeer().getName()+" has gained a level "+gainLevel+" knight with his deserter card.");
			Knight knight=new Knight(gainVertex, sender);
			knight.setLevel(gainLevel);
			gainVertex.setConstruction(knight);
			sender.getVertexContructions().remove(knight);
		}
		if(sender==getPlayer())
			getToolbar().getInfoWindow().clearMessage();
		sender.getCards().remove(PoliticsCard.DESERTER);
		getCards().add(PoliticsCard.DESERTER);
		action.cancelProgressCard();
		Algorithms.updateLongestRoad(getMap(), getPlayers());
	}
	
	private void phase2(){
		if(loseVertex!=null){
			Knight knight=(Knight)loseVertex.getConstruction();
			if(knight.getLevel()==1)
				target.setBasicKnightsRemaining(target.getBasicKnightsRemaining()+1);
			else if(knight.getLevel()==2)
				target.setStrongKnightsRemaining(target.getStrongKnightsRemaining()+1);
			else if(knight.getLevel()==3)
				target.setMightyKnightsRemaining(target.getMightyKnightsRemaining()+1);
			target.getVertexContructions().remove(knight);
			loseVertex.setConstruction(null);
		}
		
		if(target==getPlayer())
			getToolbar().getInfoWindow().clearMessage();
		
		getToolbar().getChatWindow().log("Player "+target.getPeer().getName()+" has discarded a level "+loseLevel+
				"knight(deserter card is currently being playerd by "+sender.getPeer().getName()+")");
		if(sender==getPlayer()){
			action.clearTemporaryDialogs();
			if(loseLevel==1 && sender.getBasicKnightsRemaining()>0)
				gainLevel=1;
			else if(loseLevel==2){
				if(sender.getStrongKnightsRemaining()>0)
					gainLevel=2;
				else if(sender.getBasicKnightsRemaining()>0)
					gainLevel=1;
			}else if(loseLevel==3){
				if(sender.getPoliticsLevel()>=3 && sender.getMightyKnightsRemaining()>0)
					gainLevel=3;
				else if(sender.getStrongKnightsRemaining()>0)
					gainLevel=2;
				else if(sender.getBasicKnightsRemaining()>0)
					gainLevel=-1;
			}
			boolean canPlace=false;
			for(EdgeConstruction cons:getPlayer().getEdgeConstructions()){
				Edge edge=cons.getParent();
				canPlace|=(edge.first.getConstruction()==null);
				canPlace|=(edge.second.getConstruction()==null);
			}
			if(gainLevel==-1 || !canPlace){
				final DeserterCommand command=new DeserterCommand(senderID, targetID, 3);
				command.setGain(-1, -1);
				JSONObject obj=GameCommand.getJsonCommand(command,  "progress_card");
				getSocket().emit("catan", obj, GameStage.ACK);
				command.execute(getStage(), action);
			}else
				selectGain();
		}
	}
	
	private void phase1(){
		getToolbar().getInfoWindow().displayInfoMessage("You must select one of your knights to discard ("+
				"player "+sender.getPeer().getName()+" has played the deserter progress card on you.");
		getMap().addListener(mapListener=new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Vertex vertex=getMap().getCurrentVertex();
				if(vertex.getConstruction() instanceof Knight && vertex.getConstruction().getOwner()==getPlayer()){
					loseVertex=vertex;
					Knight knight=(Knight)loseVertex.getConstruction();
					getMap().removeListener(mapListener);
					final DeserterCommand command=new DeserterCommand(senderID, targetID, 2);
					command.setLoss(loseVertex.id, knight.getLevel());
					JSONObject obj=GameCommand.getJsonCommand(command,  "progress_card");
					getSocket().emit("catan", obj, GameStage.ACK);
					command.execute(getStage(), action);
				}
			}
		});
	}
	
	private void selectGain(){
		getToolbar().getInfoWindow().displayInfoMessage("Choose where to place your level "+gainLevel+" knight.");
		getMap().addListener(mapListener=new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Vertex vertex=getMap().getCurrentVertex();
				if(vertex!=null && vertex.getConstruction()==null){
					boolean valid=true;
					for(Edge e:vertex.edges)
						valid|=(e.getConstruction()!=null && e.getConstruction().getOwner()==getPlayer());
					if(valid){
						getMap().removeListener(mapListener);
						gainVertexID=vertex.id;
						final DeserterCommand command=new DeserterCommand(senderID, targetID, 3);
						command.setGain(gainVertexID, gainLevel);
						JSONObject obj=GameCommand.getJsonCommand(command,  "progress_card");
						getSocket().emit("catan", obj, GameStage.ACK);
						command.execute(getStage(), action);
					}
				}
			}
		});
	}
}
