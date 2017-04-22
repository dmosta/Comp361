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
import cards.TradeCard;
import comp361.catan.GameStage;
import comp361.catan.Player;
import comp361.catan.Resource;
import comp361.catan.ResourceType;
import comp361.catan.Skins;
import io.socket.client.Ack;

public class CommercialHarborCommand extends GameCommand{

	private static final long serialVersionUID = -5336527952364921282L;
	private String playerID;
	private String targetID;
	private ResourceType given;
	private ResourceType taken;
	private transient Player player, target;
	private transient CardAction action;

	public CommercialHarborCommand(String playerID, String targetID, ResourceType given, ResourceType taken, int phase) {
		super(phase);
		this.playerID=playerID;
		this.targetID=targetID;
		this.given=given;
		this.taken=taken;
	}

	@Override
	protected void executeCommand() {
		action=(CardAction)getAction();
		player=getPlayers().get(playerID);
		target=getPlayers().get(targetID);
		if(getPhase()==1 && player==getPlayer()){
			phase1();
		}else if(getPhase()==2 && getPlayer()==target){
			phase2();
		}else if(getPhase()==3){
			phase3();
		}else if(getPhase()==4){
			phase4();
		}
	}
	
	private void phase4(){
		getToolbar().getChatWindow().log("Player "+player.getPeer().getName()+" has finished playing the commercial harbor progress card");
		player.getCards().remove(TradeCard.COMMERCIAL_HARBOR);
		getCards().add(TradeCard.COMMERCIAL_HARBOR);
		if(player==getPlayer()){
			Dialog tradeDialog=(Dialog)GameStage.STORE.get("tradeDialog");
			tradeDialog.hide();
			action.cancelProgressCard();
		}
	}
	
	private void phase3(){
		target.getResources().put(taken, target.getResources().get(taken)-1);
		target.getResources().put(given, target.getResources().get(given)+1);
		player.getResources().put(taken, player.getResources().get(taken)+1);
		player.getResources().put(given, player.getResources().get(given)-1);
		getToolbar().getChatWindow().log("Player "+player.getPeer().getName()+" has traded resource "+
		given+" for commodity "+taken+" with player "+target.getPeer().getName()+" (commercial harbor progress card)");
		if(player==getPlayer()){
			action.clearTemporaryDialogs();
			Dialog tradeDialog=(Dialog)GameStage.STORE.get("tradeDialog");
			HashMap<Player, Button> buttons=(HashMap<Player, Button>)GameStage.STORE.get("playerButtons");
			tradeDialog.getContentTable().removeActor(buttons.get(target));
		}
	}
	
	private void phase2(){
		getToolbar().getInfoWindow().displayInfoMessage("Player "+player.getPeer().getName()+" has targeted you with the commercial harbor progress card.");
		final Dialog commodityDialog=new Dialog("Choose a commodity", Skins.METAL);
		final HashMap<ResourceType, Button> buttons=new HashMap<ResourceType, Button>();
		for(ResourceType type:getPlayer().getResources().keySet()){
			int num=getPlayer().getResources().get(type);
			if(Resource.isCommodity(type) && num>0){
				TextButton button=new TextButton(type+"", Skins.METAL);
				buttons.put(type,  button);
				commodityDialog.getContentTable().add(button);
				commodityDialog.getContentTable().row();
			}
		}
		commodityDialog.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				for(ResourceType type:buttons.keySet()){
					if(event.getTarget().isDescendantOf(buttons.get(type))){
						CommercialHarborCommand command=new CommercialHarborCommand(playerID, targetID, given, type, 3);
						JSONObject obj=GameCommand.getJsonCommand(command, "progress_card");
						getSocket().emit("catan", obj, GameStage.ACK);
						commodityDialog.hide();
						command.execute(getStage(), getAction());
						break;
					}
				}
			}
		});
		commodityDialog.show(getStage());
	}
	
	private void phase1(){
		final Dialog tradeDialog=new Dialog("Trades available", Skins.METAL);
		final HashMap<Player, Button> buttons=new HashMap<Player, Button>();
		for(Player p:getPlayers().values()){
			if(p!=getPlayer() && Resource.calculateTotalCommodities(p)>0){
				TextButton button=new TextButton(p.getPeer().getName()+"", Skins.METAL);
				tradeDialog.getContentTable().add(button);
				tradeDialog.getContentTable().row();
				buttons.put(p, button);
			}
		}
		if(buttons.size()==0){
			Label label=new Label("No other play posseses commodity cards.", Skins.METAL);
			label.setWrap(true);
			tradeDialog.getContentTable().add(label).width(200);
			tradeDialog.getContentTable().row();
		}
		final TextButton buttonDone=new TextButton("Done", Skins.METAL);
		tradeDialog.getContentTable().add(buttonDone);
		tradeDialog.getContentTable().row();
		tradeDialog.show(getStage());
		tradeDialog.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				for(final Player p:buttons.keySet()){
					if(event.getTarget().isDescendantOf(buttons.get(p))){
						final Dialog resourceDialog=new Dialog("Choose a resource", Skins.METAL);
						final HashMap<ResourceType, Button> resourceButtons=new HashMap<ResourceType, Button>();
						for(ResourceType type:player.getResources().keySet()){
							int num=player.getResources().get(type);
							if(Resource.isResource(type) && num>0){
								TextButton resourceButton=new TextButton(type+"", Skins.METAL);
								resourceDialog.getContentTable().add(resourceButton);
								resourceButtons.put(type,  resourceButton);
								resourceDialog.getContentTable().row();
							}
						}
						final TextButton buttonCancel=new TextButton("Cancel", Skins.METAL);
						if(resourceButtons.size()==0){
							Label label=new Label("You  don't have any resources to trade.", Skins.METAL);
							label.setWrap(true);
							resourceDialog.getContentTable().add(label).width(150);
							resourceDialog.getContentTable().row();
						}
						resourceDialog.getContentTable().add(buttonCancel);
						resourceDialog.addListener(new ClickListener(){
							public void clicked(InputEvent event, float x, float y) {
								for(ResourceType type:resourceButtons.keySet()){
									if(event.getTarget().isDescendantOf(resourceButtons.get(type))){
										Dialog waitingDialog=new Dialog("Waiting for other player...", Skins.METAL);
										action.addTemporaryDialog(waitingDialog);
										waitingDialog.show(getStage());
										CommercialHarborCommand command=new CommercialHarborCommand(playerID, p.getPeer().getId(), type, null, 2);
										JSONObject obj=GameCommand.getJsonCommand(command, "progress_card");
										getSocket().emit("catan",  obj, GameStage.ACK);
										command.execute(getStage(), getAction());
										resourceDialog.hide();
										break;
									}
								}
								if(event.getTarget().isDescendantOf(buttonCancel)){
									resourceDialog.hide();
								}
							};
						});
						resourceDialog.show(getStage());
						break;
					}
				}
				if(event.getTarget().isDescendantOf(buttonDone)){
					CommercialHarborCommand command=new CommercialHarborCommand(playerID, targetID, given, taken, 4);
					JSONObject obj=GameCommand.getJsonCommand(command, "progress_card");
					getSocket().emit("catan",  obj, GameStage.ACK);
					command.execute(getStage(),  getAction());
				}
			}
		});
		tradeDialog.show(getStage());
		GameStage.STORE.put("tradeDialog", tradeDialog);
		GameStage.STORE.put("playerButtons", buttons);
	}

}
