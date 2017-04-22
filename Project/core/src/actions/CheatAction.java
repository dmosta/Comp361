package actions;

import java.util.HashMap;

import org.json.JSONObject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import cards.Card;
import cards.PoliticsCard;
import cards.ScienceCard;
import commands.CheatCommand;
import commands.GameCommand;
import commands.TestCommand;
import comp361.catan.GameStage;
import comp361.catan.Notification;
import comp361.catan.Skins;
import io.socket.client.Ack;
import io.socket.emitter.Emitter.Listener;

public class CheatAction extends GameAction{
	
	private ClickListener listener;
	private Listener socketListener;

	public CheatAction(Notification notif, GameStage gameStage) {
		super(notif, gameStage);
		getToolbar().getCheatButton().addListener(listener=new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if(getPlayer().isTurn() && !TurnAction.LOCKED)
					showCheats();
			}
		});
		setupSocket();
	}
	
	private void showCheats(){	
		if(!getStage().getCheatActive()){
			final Dialog passDialog=new Dialog("Enter cheat code", Skins.METAL);
			final TextField textPass=new TextField("", Skins.METAL);
			textPass.setMessageText("Password");
			passDialog.getContentTable().add(textPass);
			passDialog.getContentTable().row();
			final TextButton buttonAccept=new TextButton("Accept", Skins.METAL);
			final TextButton buttonCancel=new TextButton("Cancel", Skins.METAL);
			HorizontalGroup group=new HorizontalGroup();
			group.addActor(buttonCancel);
			group.addActor(buttonAccept);
			passDialog.getContentTable().add(group);
			passDialog.addListener(new ClickListener(){
				@Override
				public void clicked(InputEvent event, float x, float y) {
					if(event.getTarget().isDescendantOf(buttonCancel)){
						passDialog.hide();
					}else if(event.getTarget().isDescendantOf(buttonAccept)){
						if(textPass.getText().equals("comp361")){
							getStage().setCheatActive(true);
							openCheatMenu();
							passDialog.hide();
						}else{
							new Dialog("Wrong code", Skins.METAL).text("You have entered the wrong cheat code").text("(Hint: read the help document)").button("ok").show(getStage());
						}
					}
				}
			});
			passDialog.show(getStage());
		}else
			openCheatMenu();
	}
	
	private void openCheatMenu(){
		System.out.println("Cheat menu");
		final Dialog cheatDialog=new Dialog("Cheats", Skins.METAL);
		Label explanation=new Label("", Skins.METAL);
		String message="Welcome to the cheat menu! The following actions can only be executed while it is currently your turn (after you have rolled) and after the initial setup phase has been completed.";
		message+="The cheats cannot be executed while another action is taking place (e.g. trading with the bank).";
		explanation.setWrap(true);
		explanation.setText(message);
		cheatDialog.getContentTable().add(explanation).width(300);
		cheatDialog.getContentTable().row();
		final TextButton buttonRestart=new TextButton("Restart turn", Skins.METAL);
		final TextButton buttonVictory=new TextButton("Get a victory point", Skins.METAL);
		final TextButton buttonProgress=new TextButton("Get a progress card from deck", Skins.METAL);
		final TextButton buttonResources=new TextButton("Get 5 of each resources/commodities", Skins.METAL);
		final TextButton buttonAdvance=new TextButton("Advance barbarian to 6", Skins.METAL);
		final TextButton buttonRewind=new TextButton("Rewind barbarian to 0", Skins.METAL);
		final TextButton buttonKnight=new TextButton("Activate all knights (w/o activated this turn)", Skins.METAL);
		final TextButton buttonFish=new TextButton("Steal remaining fish supply (except boot)", Skins.METAL);
		final TextButton buttonCancel=new TextButton("Cancel", Skins.METAL);
		cheatDialog.getContentTable().add(buttonRestart).width(300);
		cheatDialog.getContentTable().row();
		cheatDialog.getContentTable().add(buttonVictory).width(300);
		cheatDialog.getContentTable().row();
		cheatDialog.getContentTable().add(buttonProgress).width(300);
		cheatDialog.getContentTable().row();
		cheatDialog.getContentTable().add(buttonResources).width(300);
		cheatDialog.getContentTable().row();
		cheatDialog.getContentTable().add(buttonAdvance).width(300);
		cheatDialog.getContentTable().row();
		cheatDialog.getContentTable().add(buttonRewind).width(300);
		cheatDialog.getContentTable().row();
		cheatDialog.getContentTable().add(buttonKnight).width(300);
		cheatDialog.getContentTable().row();
		cheatDialog.getContentTable().add(buttonFish).width(300);
		cheatDialog.getContentTable().row();
		cheatDialog.getContentTable().add(buttonCancel).width(300);
		cheatDialog.getContentTable().row();
		cheatDialog.show(getStage());
		cheatDialog.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if(event.getTarget().isDescendantOf(buttonCancel)){

				}else if(event.getTarget().isDescendantOf(buttonRestart)){
					getSocket().emit("reset_game");
				}else if(event.getTarget().isDescendantOf(buttonVictory)){
					sendCommand(1);
				}else if(event.getTarget().isDescendantOf(buttonProgress)){
					chooseCard();
				}else if(event.getTarget().isDescendantOf(buttonResources)){
					sendCommand(2);
				}else if(event.getTarget().isDescendantOf(buttonAdvance)){
					sendCommand(3);
				}else if(event.getTarget().isDescendantOf(buttonRewind)){
					sendCommand(4);
				}else if(event.getTarget().isDescendantOf(buttonKnight)){
					sendCommand(5);
				}else if(event.getTarget().isDescendantOf(buttonFish)){
					sendCommand(7);
				}else return;
				cheatDialog.hide();
			}
		});
	}
	
	private void sendCommand(int phase){
		CheatCommand command=new CheatCommand(getPlayer().getPeer().getId(), phase);
		JSONObject obj=GameCommand.getJsonCommand(command, "cheat");
		getSocket().emit("catan", obj, GameStage.ACK);
		command.execute(getStage(), this);
	}
	
	private void chooseCard(){
		if(getPlayer().getCards().size()==4){
			Label labelInfo=new Label("Before using this cheat, you need to use one of your progress cards since you already own 4.", Skins.METAL);
			labelInfo.setWrap(true);
			Dialog errDiag=new Dialog("Cannot use cheat code", Skins.METAL);
			errDiag.getContentTable().add(labelInfo).width(200);
			errDiag.getContentTable().row();
			errDiag.button("ok").show(getStage());
		}else{
			final Dialog diag=new Dialog("Choose a card", Skins.METAL);
			diag.getContentTable().add(new Label("Scroll to see more", Skins.METAL));
			diag.getContentTable().row();
			Table buttonTable=new Table();
			ScrollPane scrollPane=new ScrollPane(buttonTable);
			diag.getContentTable().add(scrollPane).height(250);
			final TextButton buttonCancel=new TextButton("Cancel", Skins.METAL);
			buttonTable.add(buttonCancel);
			buttonTable.row();
			final HashMap<Card, Button> buttons=new HashMap<Card, Button>();
			for(Card card:getCards()){
				if(!buttons.containsKey(card)){
					final TextButton button=new TextButton(card+"", Skins.METAL);
					buttons.put(card, button);
					buttonTable.add(button);
					buttonTable.row();
				}
			}
			buttonTable.addListener(new ClickListener(){
				@Override
				public void clicked(InputEvent event, float x, float y) {
					Card selection=null;
					for(Card card:buttons.keySet()){
						Button button=buttons.get(card);
						System.out.println("here ");
						if(event.getTarget().isDescendantOf(button)){
							diag.hide();
							selection=card;
							break;
						}
					}
					if(event.getTarget().isDescendantOf(buttonCancel))
						diag.hide();
					if(selection!=null){
						CheatCommand command=new CheatCommand(getPlayer().getPeer().getId(), 6);
						command.setCard(selection);
						JSONObject obj=GameCommand.getJsonCommand(command, "cheat");
						getSocket().emit("catan", obj, GameStage.ACK);
						command.execute(getStage(), CheatAction.this);
					}
				}
			});
			diag.show(getStage());
		}
	}
	
	
	private void setupSocket(){
		getSocket().on("catan", socketListener=new Listener(){
			@Override
			public void call(final Object... arg0) {
				Gdx.app.postRunnable(new Runnable(){
					@Override
					public void run() {
						JSONObject obj=(JSONObject)arg0[0];
						Ack ack=(Ack)arg0[arg0.length-1];
						try{
							if(obj.getString("action").equals("cheat")){
								GameCommand command=GameCommand.stringToCommand(obj.getString("command"));
								command.execute(getStage(), CheatAction.this);
							}else
								return;
							ack.call();
						}catch(Exception ex){ex.printStackTrace();}
					}
				});
			}
		});
	}

	@Override
	public void cleanUp() {
		getToolbar().getCheatButton().removeListener(listener);
		getSocket().off("catan", socketListener);
	}

}
