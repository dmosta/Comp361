package commands;

import java.util.HashMap;

import org.json.JSONObject;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import actions.FishAction;
import comp361.catan.GameStage;
import comp361.catan.Player;
import comp361.catan.Resource;
import comp361.catan.ResourceType;
import comp361.catan.Skins;

public class FishBankCommand extends FishCommand{

	private static final long serialVersionUID = -352579268006034909L;
	private String playerID;
	private transient Player player;
	private ResourceType resource;

	public FishBankCommand(String playerID, int phase) {
		super(phase);
		this.playerID=playerID;
	}
	
	public void setResource(ResourceType resource){
		this.resource=resource;
	}

	@Override
	protected void executeCommand() {
		player=getPlayers().get(playerID);
		if(getPhase()==0){
			phase0();
		}else if(getPhase()==1){
			phase1();
		}
	}
	
	private void phase1(){
		player.getResources().put(resource, player.getResources().get(resource)+1);
		getToolbar().getChatWindow().log("Player "+player.getPeer().getName()+" has receiveved 1 "+resource+" in exchange of fish");
		((FishAction)getAction()).removeSelectedTokens(getSelectedTokens(), player);
	}
	
	private void phase0(){
		final Dialog dialog=new Dialog("Choose a resource", Skins.METAL);
		final HashMap<ResourceType, TextButton> buttons=new HashMap<ResourceType, TextButton>();
		for(ResourceType type:ResourceType.values()){
			if(Resource.isResource(type)){
				TextButton button=new TextButton(type+"", Skins.METAL);
				buttons.put(type, button);
				dialog.getContentTable().add(button);
				dialog.getContentTable().row();
			}
		}
		dialog.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				for(ResourceType type:buttons.keySet()){
					if(event.getTarget().isDescendantOf(buttons.get(type))){
						dialog.hide();
						FishBankCommand command=new FishBankCommand(playerID, 1);
						command.setResource(type);
						command.setSelectedTokens(getSelectedTokens());
						JSONObject obj=GameCommand.getJsonCommand(command, "fish");
						getSocket().emit("catan",  obj, GameStage.ACK);
						command.execute(getStage(), getAction());
						break;
					}
				}
			}
		});
		dialog.show(getStage());
	}

}
