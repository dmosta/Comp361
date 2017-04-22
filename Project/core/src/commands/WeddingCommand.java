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

public class WeddingCommand extends GameCommand{
	
	private static final long serialVersionUID = -2692942543282968232L;
	private String playerID;
	boolean answer;
	HashMap<ResourceType, Integer> selection;
	
	public WeddingCommand(String playerID, boolean answer, HashMap<ResourceType, Integer> selection){
		super(1);
		this.playerID=playerID;
		this.answer=answer;
		this.selection=selection;
	}

	@Override
	protected void executeCommand() {
		CardAction action=(CardAction)getAction();
		final Player player=getPlayers().get(playerID);
		int myResources=Resource.calculateTotalResourcesCommodities(getPlayer());
		if(!answer){
			player.getCards().remove(PoliticsCard.WEDDING);
			getCards().add(PoliticsCard.WEDDING);
		}
		if(!answer && getPlayer().getVictoryPoints()>player.getVictoryPoints() && myResources>0){
			final int required=(myResources>1?2:1);
			final DiscardDialog dialog=new DiscardDialog(required, getPlayer());
			dialog.show(getStage());
			dialog.getButtonDone().addListener(new ClickListener(){
				@Override
				public void clicked(InputEvent event, float x, float y) {
					if(dialog.getTotal()==required){
						selection=new HashMap<ResourceType, Integer>(dialog.getSelection());
						dialog.hide();
						final WeddingCommand command=new WeddingCommand(
								getPlayer().getPeer().getId(), true, dialog.getSelection());
						JSONObject obj=GameCommand.getJsonCommand(command,  "progress_card");
						getSocket().emit("catan", obj, GameStage.ACK);
						resourceTransfer(getPlayer(), player);
					}else
						new Dialog("Please select "+required+" resources/commodities.", Skins.METAL).button("ok").show(getStage());
				}
			});
		}else if(answer && getPlayer().isTurn()){
			resourceTransfer(player, getPlayer());
			action.setAnswerCount(action.getAnswerCount()+1);
			if(action.getAnswerCount()==action.getRequestCount()){
				action.cancelProgressCard();
			}
		}
	}
	
	private void resourceTransfer(Player source, Player destination){
		int count=0;
		for(ResourceType type:selection.keySet()){
			count++;
			int num=selection.get(type);
			source.getResources().put(type, source.getResources().get(type)-num);
			destination.getResources().put(type, destination.getResources().get(type)+num);
		}
		getToolbar().getChatWindow().log("Player "+destination.getPeer().getName()+" has received "
				+count+" resources/commodities from player "+source.getPeer().getName()+" (wedding progress card)");
	}

}
