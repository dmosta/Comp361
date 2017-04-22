package commands;

import java.util.HashMap;

import org.json.JSONObject;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import actions.CardAction;
import cards.TradeCard;
import comp361.catan.GameStage;
import comp361.catan.Player;
import comp361.catan.ResourceType;
import comp361.catan.Skins;
import ui.StealResourceDialog;

public class MasterMerchantCommand extends GameCommand{
	
	private String playerID, targetID;
	private ResourceType first, second;
	private transient Player player, target;
	private transient CardAction action;

	public MasterMerchantCommand(String playerID, String targetID, int phase) {
		super(phase);
		this.playerID=playerID;
		this.targetID=targetID;
	}
	
	public void setResources(ResourceType first, ResourceType second){
		this.first=first;
		this.second=second;
	}

	@Override
	protected void executeCommand() {
		player=getPlayers().get(playerID);
		target=getPlayers().get(targetID);
		action=(CardAction)getAction();
		
		if(getPhase()==1 && player==getPlayer()){
			phase1();
		}else if(getPhase()==2){
			phase2();
		}
	}
	
	private void phase2(){
		int count=0;
		String stolen=" nothing ";
		if(first!=null){
			count++;
			target.getResources().put(first, target.getResources().get(first)-1);
			player.getResources().put(first, player.getResources().get(first)+1);
			stolen="1 "+first;
		}
		if(second!=null){
			count++;
			target.getResources().put(second, target.getResources().get(second)-1);
			player.getResources().put(second, player.getResources().get(second)+1);
			stolen=" 1 "+second;
		}
		if(first!=null && first==second)
			stolen="2 "+first;
		getToolbar().getChatWindow().log("Player "+player.getPeer().getName()+" has stolen "+stolen+" from "+target.getPeer().getName()+" with the master merchant progress card.");
		player.getCards().remove(TradeCard.MASTER_MERCHANT);
		getCards().add(TradeCard.MASTER_MERCHANT);
		if(player==getPlayer())
			action.cancelProgressCard();
	}
	
	private void phase1(){
		final Dialog playerDialog=new Dialog("Choose a player", Skins.METAL);
		final HashMap<Player, Button> playerButtons=new HashMap<Player, Button>();
		for(Player p:getPlayers().values()){
			if(p.getVictoryPoints()>player.getVictoryPoints()){
				TextButton button=new TextButton(p.getPeer().getName(), Skins.METAL);
				playerButtons.put(p,  button);
				playerDialog.getContentTable().add(button);
				playerDialog.getContentTable().row();
			}
		}
		if(playerButtons.size()==0){
			new Dialog("", Skins.METAL).text("There are no players with more victory points than you").button("ok").show(getStage());
			action.cancelProgressCard();
		}else{
			playerDialog.addListener(new ClickListener(){
				@Override
				public void clicked(InputEvent event, float x, float y) {
					for(final Player p:playerButtons.keySet()){
						if(event.getTarget().isDescendantOf(playerButtons.get(p))){
							playerDialog.hide();
							final StealResourceDialog stealDialog=new StealResourceDialog(p, 2);
							stealDialog.show(getStage());
							stealDialog.getButtonDone().addListener(new ClickListener(){
								public void clicked(InputEvent event, float x, float y) {
									stealDialog.hide();
									HashMap<ResourceType, Integer> selection=stealDialog.getSelection();
									for(ResourceType type:selection.keySet()){
										int count=selection.get(type);
										if(count==2){
											first=type;
											second=type;
											break;
										}else if(count==1 && first==null){
											first=type;
										}else if(count==1 && first!=null){
											second=type;
										}
									}
									MasterMerchantCommand command=new MasterMerchantCommand(playerID, p.getPeer().getId(), 2);
									command.setResources(first, second);
									JSONObject obj=GameCommand.getJsonCommand(command, "progress_card");
									getSocket().emit("catan", obj, GameStage.ACK);
									command.execute(getStage(), getAction());
								};
							});
							break;
						}
					}
				}
			});
			playerDialog.show(getStage());
		}
	}
	
}
