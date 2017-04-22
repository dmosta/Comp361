package actions;

import org.json.JSONObject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import commands.GameCommand;
import commands.UpgradeDisciplineCommand;
import comp361.catan.City;
import comp361.catan.GameStage;
import comp361.catan.Metropolis;
import comp361.catan.Notification;
import comp361.catan.Player;
import comp361.catan.ResourceType;
import comp361.catan.VertexConstruction;
import io.socket.client.Ack;
import io.socket.emitter.Emitter.Listener;
import ui.ChartWindow;

public class DevelopmentAction extends GameAction{
	
	private ChartWindow chartWindow;
	private ClickListener clickListener;
	private Listener socketListener;

	public DevelopmentAction(Notification notif, GameStage gameStage) {
		super(notif, gameStage);
		chartWindow=getToolbar().getChartWindow();
		setupSocket();
		chartWindow.getButtonUpgrade().addListener(clickListener=new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if(getPlayer().isTurn() && !TurnAction.LOCKED){
					UpgradeDisciplineCommand command=new UpgradeDisciplineCommand(
							getPlayer().getPeer().getId(), chartWindow.getDiscipline(), false, 1);
					command.execute(getStage(), DevelopmentAction.this);
				}
			}
		});
	}
	
	private void setupSocket(){
		getSocket().on("catan", socketListener=new Listener(){
			@Override
			public void call(final Object... arg0) {
				Gdx.app.postRunnable(new Runnable(){
					@Override
					public void run() {
						try{
							JSONObject obj=(JSONObject)arg0[0];
							Ack ack=(Ack)arg0[arg0.length-1];
							switch(obj.getString("action")){
								case "upgrade_discipline":
									GameCommand command=GameCommand.stringToCommand(obj.getString("command"));
									command.execute(getStage(), DevelopmentAction.this);
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

	@Override
	public void cleanUp() {
		chartWindow.getButtonUpgrade().removeListener(clickListener);
		getSocket().off("catan", socketListener);
	}

}
