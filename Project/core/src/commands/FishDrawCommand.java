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

public class FishDrawCommand extends FishCommand{

	private static final long serialVersionUID = -4677902608912318819L;
	private String playerID;
	private String targetID;
	private ResourceType stolen;

	public FishDrawCommand(String playerID, int phase) {
		super(phase);
		this.playerID=playerID;
	}
	
	public void setTarget(String targetID, ResourceType stolen){
		this.targetID=targetID;
		this.stolen=stolen;
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
		Player player=getPlayers().get(playerID);
		Player target=getPlayers().get(targetID);
		target.getResources().put(stolen, target.getResources().get(stolen)-1);
		player.getResources().put(stolen, player.getResources().get(stolen)+1);
		String message="Player "+player.getPeer().getName()+" has stolen a resource/commodity from "+target.getPeer().getName()
				+" using his fish token";
		getToolbar().getChatWindow().log(message);
		((FishAction)getAction()).removeSelectedTokens(getSelectedTokens(), player);
	}
	
	private void phase1(){
		final Dialog playerDialog=new Dialog("Choose a player", Skins.METAL);
		final HashMap<Player, TextButton> buttons=new HashMap<Player, TextButton>();
		for(Player p:getPlayers().values()){
			if(p!=getPlayer() && Resource.calculateTotalResourcesCommodities(p)>0){
				TextButton button=new TextButton(p.getPeer().getName(), Skins.METAL);
				buttons.put(p, button);
				playerDialog.getContentTable().add(button);
				playerDialog.getContentTable().row();
			}
		}
		playerDialog.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				for(Player p:buttons.keySet()){
					if(event.getTarget().isDescendantOf(buttons.get(p))){
						playerDialog.hide();
						FishDrawCommand command=new FishDrawCommand(playerID, 2);
						command.setTarget(p.getPeer().getId(), Resource.randomResourceFromPlayer(p));
						command.setSelectedTokens(getSelectedTokens());
						JSONObject obj=GameCommand.getJsonCommand(command, "fish");
						getSocket().emit("catan", obj, GameStage.ACK);
						command.execute(getStage(), getAction());
						break;
					}
				}
			}
		});
		playerDialog.show(getStage());
	}

}
