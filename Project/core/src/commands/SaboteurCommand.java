package commands;

import java.util.HashMap;

import org.json.JSONObject;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import actions.CardAction;
import cards.PoliticsCard;
import comp361.catan.GameStage;
import comp361.catan.Player;
import comp361.catan.Resource;
import comp361.catan.ResourceType;
import comp361.catan.Skins;
import io.socket.client.Ack;
import ui.DiscardDialog;

public class SaboteurCommand extends GameCommand{
	
	private static final long serialVersionUID = 5899600261533233760L;
	private String playerID;
	private String receiverID;
	private transient Player player;
	private transient Player receiver;
	private transient CardAction action;
	private HashMap<ResourceType, Integer> selection;

	public SaboteurCommand(String playerID, String receiverID, int phase) {
		super(phase);
		this.playerID=playerID;
		this.receiverID=receiverID;
	}
	
	public void setSelection(HashMap<ResourceType, Integer> selection){
		this.selection=selection;
	}

	@Override
	protected void executeCommand() {
		action=(CardAction)getAction();
		player=getPlayers().get(playerID);
		receiver=getPlayers().get(receiverID);
		
		if(getPhase()==1 && player==getPlayer()){
			phase1();
		}else if(getPhase()==2 && receiver==getPlayer()){
			phase2();
		}else if(getPhase()==3){
			phase3();
		}
	}
	
	private void phase3(){
		for(ResourceType type:selection.keySet()){
			int num=selection.get(type);
			receiver.getResources().put(type, receiver.getResources().get(type)-num);
		}
		getToolbar().getChatWindow().log("Player "+receiver.getPeer().getName()+" has lost half his resources/commodities "+
		"because of the saboteur progress card played by "+player.getPeer().getName());
		player.getCards().remove(PoliticsCard.SABOTEUR);
		getCards().add(PoliticsCard.SABOTEUR);
		if(player==getPlayer()){
			action.setAnswerCount(action.getAnswerCount()+1);
			if(action.getAnswerCount()==action.getRequestCount()){
				action.setAnswerCount(0);
				action.setRequestCount(0);
				action.cancelProgressCard();
			}
		}
	}
	
	private void phase2(){
		int owned=Resource.calculateTotalResourcesCommodities(getPlayer());
		final int required=owned/2;
		if(owned>1){
			final DiscardDialog dialog=new DiscardDialog(required, getPlayer());
			dialog.show(getStage());
			dialog.getButtonDone().addListener(new ClickListener(){
				@Override
				public void clicked(InputEvent event, float x, float y) {
					if(dialog.getTotal()!=required){
						new Dialog("Discard more", Skins.METAL).text("You need to discard "+required+" resources").button("ok").show(getStage());
					}else{
						dialog.hide();
						SaboteurCommand command=new SaboteurCommand(playerID, receiverID, 3);
						command.setSelection(dialog.getSelection());
						JSONObject obj=GameCommand.getJsonCommand(command, "progress_card");
						getSocket().emit("catan", obj, GameStage.ACK);
						command.execute(getStage(), getAction());
					}
				}
			});
		}
	}
	
	private void phase1(){
		int count=0;
		for(Player p:getPlayers().values()){
			if(p!=getPlayer() && Resource.calculateTotalResourcesCommodities(p)>1 && p.getVictoryPoints() >= player.getVictoryPoints()){
				count++;
			}
		}
		if(count==0){
			new Dialog("Cannot play", Skins.METAL).text("There are no players with more victory points than you (or they don't have more than 1 resource).").button("ok").show(getStage());
			action.cancelProgressCard();
		}else{
			Dialog waitingDialog=new Dialog("Waiting for others...", Skins.METAL).show(getStage());
			action.addTemporaryDialog(waitingDialog);
			action.setRequestCount(count);
			action.setAnswerCount(0);
			for(Player p:getPlayers().values()){
				if(p!=getPlayer() && Resource.calculateTotalResourcesCommodities(p)>1 && p.getVictoryPoints() >= player.getVictoryPoints()){
					SaboteurCommand command=new SaboteurCommand(playerID, p.getPeer().getId(), 2);
					JSONObject obj=GameCommand.getJsonCommand(command, "progress_card");
					getSocket().emit("catan", obj, GameStage.ACK);
				}
			}
		}
	}

}
