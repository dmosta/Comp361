package actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import cards.Card;
import cards.PoliticsCard;
import cards.ScienceCard;
import cards.TradeCard;
import commands.AlchemistCommand;
import commands.GameCommand;
import commands.RollCommand;
import comp361.catan.*;
import io.socket.client.Ack;
import io.socket.client.Socket;
import io.socket.emitter.Emitter.Listener;

/**
 * This game action handles rolling the dice, generating resources as well as moving
 * the robber and all the other actions associated with the first phase of a turn.
 */
public class RollAction extends GameAction{
	
	private ClickListener turnListener;
	private Listener socketListener;
	private ClickListener cardListener;
	private boolean rollDetermined=false;
	private int red=0;
	private int yellow=0;
	private String event="";

	public RollAction(Notification notif, GameStage gameStage) {
		super(notif, gameStage);
		gameStage.setTurnStarted(true);
		GameStage.GAME_STATE=GameState.TURN_FIRST_PHASE;
		gameStage.saveGame();
		setupSocket();
		gameStage.getToolbar().toggleTurnButtonImage(true);
		gameStage.getToolbar().getTurnButton().addListener(turnListener=new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				try{
					if(getPlayer().isTurn() && !getPlayer().getTurnInfo().isDiceRolled()){
						getPlayer().getTurnInfo().setDiceRolled(true);
						checkBoot();
					}
				}catch(Exception e){e.printStackTrace();}
			}
		});
		getStage().getToolbar().getCardWindow().getButtonPlay().addListener(cardListener=new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if(getPlayer().isTurn() && getStage().getToolbar().getCardWindow().getCurrentCard()==ScienceCard.ALCHEMIST){
					AlchemistCommand command=new AlchemistCommand(getPlayer().getPeer().getId(), 1);
					command.execute(getStage(), RollAction.this);
				}
			}
		});
		if(getPlayer().isTurn())
			getToolbar().getInfoWindow().displayInfoMessage("It is your turn to roll");
	}
	
	public void setRoll(boolean determined, int red, int yellow, String event){
		this.rollDetermined=determined;
		this.red=red;
		this.yellow=yellow;
		this.event=event;
	}
	
	private void checkBoot(){
		boolean giveBoot=false;
		final Dialog bootDialog=new Dialog("Give old boot", Skins.METAL);
		final HashMap<Player, TextButton> buttons=new HashMap<Player, TextButton>();
		for(Player p:getPlayers().values()){
			if(p!=getPlayer() && getPlayer().hasBoot() && p.getVictoryPoints()>=getPlayer().getVictoryPoints()){
				giveBoot=true;
				TextButton button=new TextButton(p.getPeer().getName(), Skins.METAL);
				buttons.put(p,  button);
				bootDialog.getContentTable().add(button);
				bootDialog.getContentTable().row();
			}
		}
		final TextButton buttonCancel=new TextButton("Keep boot", Skins.METAL);
		bootDialog.getContentTable().add(buttonCancel);
		bootDialog.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				for(Player p:buttons.keySet()){
					if(event.getTarget().isDescendantOf(buttons.get(p))){
						bootDialog.hide();
						rollDice(p.getPeer().getId());
						break;
					}
				}
				if(event.getTarget().isDescendantOf(buttonCancel)){
					bootDialog.hide();
					rollDice("");
				}
			}
		});
		if(giveBoot)
			bootDialog.show(getStage());
		else rollDice("");
	}
	
	private void rollDice(String bootTarget){
		Gdx.input.setInputProcessor(null);
		getToolbar().getInfoWindow().clearMessage();
		int redDie=(int)(Math.random()*6+1);
		int yellowDie=(int)(Math.random()*6+1);
		String[] events=new String[]{"science", "politics", "trade", "barbarian", "barbarian", "barbarian"};
		String eOutcome=events[(int)((Math.random()*6))];
		if(DebugAction.SET_ROLL){
			redDie=DebugAction.RED_DIE;
			yellowDie=DebugAction.YELLOW_DIE;
			eOutcome=DebugAction.EVENT_DIE;
		}
		if(rollDetermined){
			redDie=red;
			yellowDie=yellow;
			eOutcome=event;
		}
		final int outcome=redDie+yellowDie;
		final String eventOutcome=eOutcome;
		final RollCommand command=new RollCommand(getPlayer().getPeer().getId(), redDie, yellowDie, eventOutcome, null, bootTarget, 1);
		JSONObject obj=GameCommand.getJsonCommand(command, "roll");
		getSocket().emit("catan", obj, GameStage.ACK);
		Gdx.input.setInputProcessor(getStage());
		command.execute(getStage(), RollAction.this);
	}
	
	private void setupSocket(){
		final Socket socket=getSocket();
		socket.on("catan", socketListener=new Listener(){
			@Override
			public void call(final Object... arg0) {
				Gdx.app.postRunnable(new Runnable(){
					@Override
					public void run() {
						try{
							JSONObject obj=(JSONObject)arg0[0];
							Ack ack=(Ack)arg0[arg0.length-1];
							switch(obj.getString("action")){
								case "roll":
									GameCommand command=GameCommand.stringToCommand(obj.getString("command"));
									command.execute(getStage(), RollAction.this);
									break;
								default:
									return;
							}
							ack.call();
						}catch(Exception e){e.printStackTrace();}
					}
				});
			}
		});
	}
	
	public void done(){
		cleanUp();
		getNotification().actionCompleted(new TurnAction(getNotification(), getStage()));
	}
	
	@Override
	public void cleanUp(){
		getStage().getToolbar().getTurnButton().removeListener(turnListener);
		getStage().getToolbar().getCardWindow().getButtonPlay().removeListener(cardListener);
		getSocket().off("catan", socketListener);
	}
}
