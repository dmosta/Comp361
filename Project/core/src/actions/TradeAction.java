package actions;

import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONObject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import commands.GameCommand;
import commands.TradeCommand;
import comp361.catan.GameStage;
import comp361.catan.Notification;
import comp361.catan.Player;
import comp361.catan.ResourceType;
import comp361.catan.Skins;
import io.socket.client.Ack;
import io.socket.emitter.Emitter.Listener;
import ui.*;

/**
 * This game action handles trades with the bank and other players.
 */
public class TradeAction extends GameAction{
	
	private ClickListener tradeListener;
	private TradeWindow tradeWindow;
	private Listener socketListener;
	private boolean isBankTrade=false;
	private Player other;

	public TradeAction(Notification notif, GameStage gameStage) {
		super(notif, gameStage);
		tradeWindow=gameStage.getToolbar().getTradeWindow();
		
		tradeListener=new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if(getStage().getCurrentPlayer().isTurn() && !TurnAction.LOCKED){
					boolean found=false;
					for(PlayerCell cell:getStage().getToolbar().getPlayerCells()){
						if(event.getTarget().isDescendantOf(cell)){
							found=true;
							other=cell.getPlayer();
							break;
						}
					}
					TurnAction.LOCKED=true;
					String target=found?other.getPeer().getId():getPlayer().getPeer().getId();
					TradeCommand command=new TradeCommand(getPlayer().getPeer().getId(), target, !found, 0);
					command.execute(getStage(), TradeAction.this);
				}
			}
		};
		for(PlayerCell cell:getStage().getToolbar().getPlayerCells())
			cell.getTradeButton().addListener(tradeListener);
		gameStage.getToolbar().getTradeButton().addListener(tradeListener);
		setupSocket();
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
								case "trade":
									GameCommand command=GameCommand.stringToCommand(obj.getString("command"));
									command.execute(getStage(), TradeAction.this);
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
		for(PlayerCell cell:getStage().getToolbar().getPlayerCells())
			cell.getTradeButton().removeListener(tradeListener);
		getStage().getToolbar().getTradeButton().removeListener(tradeListener);
		getSocket().off("catan", socketListener);
	}

}
