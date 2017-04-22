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
import comp361.catan.ResourceType;
import comp361.catan.Skins;

public class MerchantFleetCommand extends GameCommand{

	private static final long serialVersionUID = -7531354726473284052L;
	private String playerID;
	private transient Player player;
	private ResourceType type;

	public MerchantFleetCommand(String playerID, ResourceType type, int phase) {
		super(phase);
		this.playerID=playerID;
		this.type=type;
	}
	
	@Override
	protected void executeCommand() {
		if(getPhase()==1){
			phase1();
		}else if(getPhase()==2){
			phase2();
		}
	}
	
	private void phase2(){
		player=getPlayers().get(playerID);
		player.getCards().remove(TradeCard.MERCHANT_FLEET);
		getCards().add(TradeCard.MERCHANT_FLEET);
		getToolbar().getChatWindow().log("Player "+player.getPeer().getName()+" has obtained trading rate 2:1 trading rate with "+type+" using the merchant fleet progress card");
		player.getTurnInfo().setFleetResource(type);
		((CardAction)getAction()).cancelProgressCard();
	}
	
	private void phase1(){
		final Dialog resourceDialog=new Dialog("", Skins.METAL);
		Label label=new Label("Choose the resource or commodity that will obtain the trading rate 2:1 this turn.", Skins.METAL);
		label.setWrap(true);
		resourceDialog.getContentTable().add(label).width(100);
		resourceDialog.getContentTable().row();
		final HashMap<ResourceType, Button> buttons=new HashMap<ResourceType, Button>();
		for(ResourceType type:ResourceType.values()){
			TextButton button=new TextButton(type+"", Skins.METAL);
			buttons.put(type,  button);
			resourceDialog.getContentTable().add(button);
			resourceDialog.getContentTable().row();
		}
		resourceDialog.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				for(ResourceType type:buttons.keySet()){
					if(event.getTarget().isDescendantOf(buttons.get(type))){
						resourceDialog.hide();
						MerchantFleetCommand command=new MerchantFleetCommand(playerID, type, 2);
						JSONObject obj=GameCommand.getJsonCommand(command, "progress_card");
						getSocket().emit("catan", obj, GameStage.ACK);
						command.execute(getStage(), getAction());
						break;
					}
				}
			}
		});
		resourceDialog.show(getStage());
	}

}
