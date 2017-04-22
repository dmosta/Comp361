package commands;

import org.json.JSONObject;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import actions.RollAction;
import cards.PoliticsCard;
import cards.ScienceCard;
import comp361.catan.GameStage;
import comp361.catan.Player;
import comp361.catan.Skins;

public class AlchemistCommand extends GameCommand{
	
	private int red;
	private int yellow;
	private String event;
	private String playerID;
	private transient Player player;
	private transient RollAction action;

	public AlchemistCommand(String playerID, int phase) {
		super(phase);
		this.playerID=playerID;
	}
	
	public void setRoll(int red, int yellow, String event){
		this.red=red;
		this.yellow=yellow;
		this.event=event;
	}
	
	@Override
	protected void executeCommand() {
		player=getPlayers().get(playerID);
		action=(RollAction)getAction();
		if(getPhase()==1){
			phase1();
		}else if(getPhase()==2){
			phase2();
		}
	}
	
	private void phase2(){
		getToolbar().getChatWindow().log("Player "+player.getPeer().getName()+" has chosen to roll "+red+" on the red die and "+yellow+
				" on the yellow die and "+event+" on the event die");
		player.getCards().remove(ScienceCard.ALCHEMIST);
		getCards().add(ScienceCard.ALCHEMIST);
		action.setRoll(true, red, yellow, event);
	}
	
	private void phase1(){
		final Dialog rollDialog=new Dialog("Choose the dice outcome", Skins.METAL);
		final TextField redDie=new TextField("", Skins.METAL);
		redDie.setMessageText("Red die (1-6)");
		final TextField yellowDie=new TextField("", Skins.METAL);
		yellowDie.setText("Yellow die (1-6)");
		final SelectBox<String> eventBox=new SelectBox<String>(Skins.METAL);
		eventBox.setItems(new String[]{"Barbarian", "Trade", "Politics", "Science"});
		final TextButton buttonAccept=new TextButton("Accept", Skins.METAL);
		rollDialog.getContentTable().add(redDie);
		rollDialog.getContentTable().row();
		rollDialog.getContentTable().add(yellowDie);
		rollDialog.getContentTable().row();
		rollDialog.getContentTable().add(eventBox);
		rollDialog.getContentTable().row();
		rollDialog.getContentTable().add(buttonAccept);
		rollDialog.getContentTable().row();
		rollDialog.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if(event.getTarget().isDescendantOf(buttonAccept)){
					try{
						int red=Integer.parseInt(redDie.getText());
						int yellow=Integer.parseInt(yellowDie.getText());
						if(red<1 || red>6 || yellow<1 || yellow>6){
							new Dialog("Invalid", Skins.METAL).text("Invalid dice roll").button("ok").show(getStage());
						}else{
							rollDialog.hide();
							AlchemistCommand command=new AlchemistCommand(playerID, 2);
							command.setRoll(red, yellow, eventBox.getSelected().toLowerCase());
							JSONObject obj=GameCommand.getJsonCommand(command, "roll");
							getSocket().emit("catan", obj, GameStage.ACK);
							command.execute(getStage(), getAction());
						}
					}catch(NumberFormatException e){
						new Dialog("Invalid", Skins.METAL).text("Invalid dice roll").button("ok").show(getStage());
					}
				}
			}
		});
		rollDialog.show(getStage());
	}

}
