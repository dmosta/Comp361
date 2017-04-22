package actions;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONObject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import algorithms.Algorithms;
import cards.Card;
import cards.PoliticsCard;
import cards.ScienceCard;
import commands.GameCommand;
import commands.TestCommand;
import comp361.catan.CatanLauncher;
import comp361.catan.Edge;
import comp361.catan.GameStage;
import comp361.catan.Notification;
import comp361.catan.Skins;
import comp361.catan.Vertex;
import io.socket.client.Ack;
import io.socket.emitter.Emitter.Listener;

public class DebugAction extends GameAction{
	
	private InputListener listener;
	private Listener socketListener;
	public static boolean SET_ROLL=false;
	public static int RED_DIE=3;
	public static int YELLOW_DIE=4;
	public static String EVENT_DIE="trade";

	public DebugAction(Notification notif, GameStage gameStage) {
		super(notif, gameStage);
		if(CatanLauncher.DEBUG){
			getStage().addListener(listener=new InputListener(){
				@Override
				public boolean keyDown(InputEvent event, int keycode) {
					if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)){
						if(keycode==Input.Keys.UP || keycode==Input.Keys.DOWN || keycode==Input.Keys.LEFT || keycode==Input.Keys.RIGHT)
							return true;
						else if(keycode==Input.Keys.O)
							makeCardSelection();
						else if(keycode==Input.Keys.U)
							Algorithms.longestRoad(getMap(), getPlayer());
						else if(keycode==Input.Keys.M)
							setRoll();
						else sendTestCommandWithKey(keycode);
					}
					return true;
				}
			});
			setupSocket();
		}
	}
	
	private void setRoll(){
		final Dialog dialog=new Dialog("Choose roll", Skins.METAL);
		final CheckBox box=new CheckBox("set roll", Skins.METAL);
		final TextField red=new TextField("", Skins.METAL);
		red.setMessageText("red die");
		final TextField yellow=new TextField("", Skins.METAL);
		yellow.setMessageText("yellow die");
		TextButton done=new TextButton("done", Skins.METAL);
		final SelectBox<String> eventBox=new SelectBox<String>(Skins.METAL);
		eventBox.setItems(new String[]{"Barbarian", "Trade", "Politics", "Science"});
		dialog.getContentTable().add(box);
		dialog.getContentTable().row();
		dialog.getContentTable().add(red);
		dialog.getContentTable().row();
		dialog.getContentTable().add(yellow);
		dialog.getContentTable().row();
		dialog.getContentTable().add(eventBox);
		dialog.getContentTable().row();
		dialog.getContentTable().add(done);
		dialog.getContentTable().row();
		dialog.show(getStage());
		done.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				SET_ROLL=box.isChecked();
				int redDie=1, yellowDie=1;
				try{
					redDie=Integer.parseInt(red.getText());
					yellowDie=Integer.parseInt(yellow.getText());
				}catch(Exception e){}
				RED_DIE=redDie;
				YELLOW_DIE=yellowDie;
				EVENT_DIE=eventBox.getSelected().toLowerCase();
				dialog.hide();
			}
		});
	}
	
	private void makeCardSelection(){
		final Dialog diag=new Dialog("Choose a card", Skins.METAL);
		Table buttonTable=new Table();
		ScrollPane scrollPane=new ScrollPane(buttonTable);
		diag.add(scrollPane).height(250);
		buttonTable.add(new TextButton("Cancel", Skins.METAL));
		buttonTable.row();
		final HashMap<Card, Button> buttons=new HashMap<Card, Button>();
		for(Card card:getCards()){
			if(card!=ScienceCard.PRINTER && card!=PoliticsCard.CONSTITUTION && !buttons.containsKey(card)){
				TextButton button=new TextButton(card+"", Skins.METAL);
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
					if(event.getTarget().isDescendantOf(button)){
						selection=card;
						break;
					}
				}
				diag.hide();
				if(selection!=null){
					final TestCommand test=new TestCommand(getPlayer().getPeer().getId(), selection);
					JSONObject obj=GameCommand.getJsonCommand(test, "test_command");
					getSocket().emit("catan", obj, new Ack(){
						@Override
						public void call(Object... arg0) {
							test.execute(getStage(), DebugAction.this);
						}
					});
				}
			}
		});
	}
	
	private void setupSocket(){
		getSocket().on("catan", new Listener(){
			@Override
			public void call(final Object... arg0) {
				Gdx.app.postRunnable(new Runnable(){
					@Override
					public void run() {
						try{
							JSONObject obj=(JSONObject)arg0[0];
							Ack ack=(Ack)arg0[arg0.length-1];
							GameCommand command=null;
							switch(obj.getString("action")){
								case "test_command":
									command=GameCommand.stringToCommand(obj.getString("command"));
									break;
								default: return;
							}
							if(command!=null)
								command.execute(getStage(),  DebugAction.this);
							ack.call();
						}catch(Exception e){e.printStackTrace();}
					}
				});
			}
		});
	}
	
	private void sendTestCommandWithKey(int keycode){
		final TestCommand test=new TestCommand(getPlayer().getPeer().getId(), keycode);
		JSONObject obj=GameCommand.getJsonCommand(test, "test_command");
		getSocket().emit("catan", obj, new Ack(){
			@Override
			public void call(Object... arg0) {
				test.execute(getStage(), DebugAction.this);
			}
		});
	}

	@Override
	public void cleanUp() {
		if(CatanLauncher.DEBUG){
			getStage().removeListener(listener);
			getSocket().off("catan", socketListener);
		}
	}

}
