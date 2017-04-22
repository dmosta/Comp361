package commands;

import java.util.ArrayList;
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
import comp361.catan.Resource;
import comp361.catan.ResourceType;
import comp361.catan.Skins;
import io.socket.client.Ack;

public class ResourceMonopolyCommand extends GameCommand{

	private static final long serialVersionUID = 2618269033979025068L;
	private String playerID;
	private ResourceType type;
	private transient Player player;
	private transient CardAction action;

	public ResourceMonopolyCommand(String playerID, ResourceType type, int phase) {
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
		ArrayList<String> players=new ArrayList<String>();
		for(Player p:getPlayers().values()){
			if(p!=player){
				int owned=p.getResources().get(type);
				int lost=0;
				if(owned==1)
					lost=1;
				else if(owned>0)
					lost=2;
				p.getResources().put(type, owned-lost);
				player.getResources().put(type, player.getResources().get(type)+lost);
				if(lost>0)
					players.add(p.getPeer().getName());
			}
		}
		String message="Player "+player.getPeer().getName()+" has received "+type+" from ";
		if(players.size()==0)
			message+="0 players";
		else{
			message+=" players ";
			for(int i=0;i<players.size();i++){
				String name=players.get(i);
				message+=name;
				if(i!=(players.size()-1))
					message+=", ";
			}
		}
		message+=" after playing the resource monopoly card.";
		getToolbar().getChatWindow().log(message);
		player.getCards().remove(TradeCard.RESOURCE_MONOPOLY);
		getCards().add(TradeCard.RESOURCE_MONOPOLY);
		action.cancelProgressCard();
	}
	
	private void phase1(){
		final Dialog resDialog=new Dialog("Choose a resource", Skins.METAL);
		final HashMap<ResourceType, Button> buttons=new HashMap<ResourceType, Button>();
		for(ResourceType type:ResourceType.values()){
			if(Resource.isResource(type)){
				TextButton button=new TextButton(type+"", Skins.METAL);
				resDialog.getContentTable().add(button);
				resDialog.getContentTable().row();
				buttons.put(type, button);
			}
		}
		final TextButton cancelButton=new TextButton("Cancel", Skins.METAL);
		resDialog.getContentTable().add(cancelButton);
		resDialog.show(getStage());
		resDialog.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				for(ResourceType type:buttons.keySet()){
					Button button=buttons.get(type);
					if(event.getTarget().isDescendantOf(button)){
						resDialog.hide();
						ResourceMonopolyCommand command=new ResourceMonopolyCommand(playerID, type, 2);
						JSONObject obj=GameCommand.getJsonCommand(command, "progress_card");
						getSocket().emit("catan", obj, GameStage.ACK);
						command.execute(getStage(), action);
						break;
					}
				}
				if(event.getTarget().isDescendantOf(cancelButton)){
					resDialog.hide();
					action.cancelProgressCard();
				}
			}
		});
	}

}
