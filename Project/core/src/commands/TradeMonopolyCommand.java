package commands;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONObject;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
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

public class TradeMonopolyCommand extends GameCommand{

	private static final long serialVersionUID = 7109784351489662589L;
	private String playerID;
	private ResourceType type;
	private transient Player player;
	private transient CardAction action;
	
	
	public TradeMonopolyCommand(String playerID, ResourceType type, int phase){
		super(phase);
		this.playerID=playerID;
		this.type=type;
	}

	@Override
	protected void executeCommand() {
		action=(CardAction)getAction();
		player=getPlayers().get(playerID);
		
		if(getPhase()==1 && player==getPlayer()){
			phase1();
		}else if(getPhase()==2){
			phase2();
		}
	}
	
	private void phase2(){
		ArrayList<String> names=new ArrayList<String>();
		for(Player p:getPlayers().values()){
			if(p!=player){
				int owned=p.getResources().get(type);
				if(owned>0){
					p.getResources().put(type, owned-1);
					player.getResources().put(type, player.getResources().get(type)+1);
					names.add(p.getPeer().getName());
				}
			}
		}
		String message="Player "+player.getPeer().getName()+" has played the trade monopoly progress card and stolen "+type+" from ";
		if(names.size()==0)
			message+="0 players";
		else{
			for(int i=0;i<names.size();i++){
				String name=names.get(i);
				message+=name;
				if(i!=names.size()-1)
					message+=", ";
			}
		}
		getToolbar().getChatWindow().log(message);
		player.getCards().remove(TradeCard.TRADE_MONOPOLY);
		getCards().add(TradeCard.TRADE_MONOPOLY);
		action.cancelProgressCard();
	}
	
	private void phase1(){
		final Dialog dialog=new Dialog("Choose a commodity", Skins.METAL);
		final HashMap<ResourceType, TextButton> buttons=new HashMap<ResourceType, TextButton>();
		for(ResourceType type:ResourceType.values()){
			if(Resource.isCommodity(type)){
				TextButton button=new TextButton(type+"", Skins.METAL);
				dialog.getContentTable().add(button);
				dialog.getContentTable().row();
				buttons.put(type,  button);
			}
		}
		final TextButton cancelButton=new TextButton("Cancel", Skins.METAL);
		dialog.getContentTable().add(cancelButton);
		dialog.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				for(ResourceType type:buttons.keySet()){
					TextButton button=buttons.get(type);
					if(event.getTarget().isDescendantOf(button)){
						dialog.hide();
						TradeMonopolyCommand command=new TradeMonopolyCommand(playerID, type, 2);
						JSONObject obj=GameCommand.getJsonCommand(command, "progress_card");
						getSocket().emit("catan", obj, GameStage.ACK);
						command.execute(getStage(), action);
						break;
					}
				}
				if(event.getTarget().isDescendantOf(cancelButton)){
					dialog.hide();
					action.cancelProgressCard();
				}
			}
		});
		dialog.show(getStage());
	}
}
