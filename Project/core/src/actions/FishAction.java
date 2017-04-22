package actions;

import java.util.ArrayList;

import org.json.JSONObject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import commands.FishBankCommand;
import commands.FishBuildCommand;
import commands.FishCommand;
import commands.FishDrawCommand;
import commands.FishProgressCommand;
import commands.FishRobberCommand;
import commands.GameCommand;
import comp361.catan.FishToken;
import comp361.catan.GameStage;
import comp361.catan.Notification;
import comp361.catan.Player;
import comp361.catan.Resource;
import comp361.catan.Skins;
import io.socket.client.Ack;
import io.socket.emitter.Emitter.Listener;
import ui.TokenDialog;

public class FishAction extends GameAction{
	
	private Listener socketListener;
	private ClickListener clickListener;

	public FishAction(Notification notif, GameStage gameStage) {
		super(notif, gameStage);
		setupSocket();
		getToolbar().getFishButton().addListener(clickListener=new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if(getPlayer().isTurn() && !TurnAction.LOCKED){
					displayOptions();
				}
			}
		});
	}
	
	private void displayOptions(){
		int total=0;
		for(FishToken token:getPlayer().getFishToken())
			total+=token.getValue();
		final Dialog fishDialog=new Dialog("Choose an action", Skins.METAL);
		final TextButton buttonPirate=new TextButton("Remove pirate", Skins.METAL);
		final TextButton buttonRobber=new TextButton("Remove robber", Skins.METAL);
		final TextButton buttonDraw=new TextButton("Draw resource/commodity from player", Skins.METAL);
		final TextButton buttonBank=new TextButton("Take resource from bank", Skins.METAL);
		final TextButton buttonRoad=new TextButton("Build road", Skins.METAL);
		final TextButton buttonShip=new TextButton("Build ship", Skins.METAL);
		final TextButton buttonProgress=new TextButton("Choose progress card", Skins.METAL);
		final TextButton buttonCancel=new TextButton("Cancel", Skins.METAL);
		final Label labelTotal=new Label("Your fish tokens are worth "+total+" fish", Skins.METAL);
		fishDialog.getContentTable().add(labelTotal);
		fishDialog.getContentTable().row();
		int count=0;
		if(total>=2){
			count++;
			fishDialog.getContentTable().add(buttonRobber).width(250);
			fishDialog.getContentTable().row();
			fishDialog.getContentTable().add(buttonPirate).width(250);
			fishDialog.getContentTable().row();
		}
		if(total>=3){
			count++;
			fishDialog.getContentTable().add(buttonDraw).width(250);
			fishDialog.getContentTable().row();
		}
		if(total>=4){
			count++;
			fishDialog.getContentTable().add(buttonBank).width(250);
			fishDialog.getContentTable().row();
		}
		if(total>=5){
			count++;
			fishDialog.getContentTable().add(buttonRoad).width(250);
			fishDialog.getContentTable().row();
			fishDialog.getContentTable().add(buttonShip).width(250);
			fishDialog.getContentTable().row();
		}
		if(total>=7){
			count++;
			fishDialog.getContentTable().add(buttonProgress).width(250);
			fishDialog.getContentTable().row();
		}
		if(count==0){
			fishDialog.getContentTable().add(new Label("No actions possible", Skins.METAL)).width(250);
			fishDialog.getContentTable().row();
		}
		fishDialog.getContentTable().add(buttonCancel).width(250);
		fishDialog.show(getStage());
		fishDialog.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				boolean clicked=true;
				if(event.getTarget().isDescendantOf(buttonPirate)){
					if(getMap().getPirate().getLocation()==null)
						new Dialog("", Skins.METAL).text("The pirate is not on the board.").button("ok").show(getStage());
					else{
						FishRobberCommand command=new FishRobberCommand(getPlayer().getPeer().getId(), true);
						executeFishCommand(2, command, true);
					}
				}else if(event.getTarget().isDescendantOf(buttonRobber)){
					if(getMap().getPirate().getLocation()==null)
						new Dialog("", Skins.METAL).text("The robber is not on the board.").button("ok").show(getStage());
					else{
						FishRobberCommand command=new FishRobberCommand(getPlayer().getPeer().getId(), false);
						executeFishCommand(2, command, true);
					}
				}else if(event.getTarget().isDescendantOf(buttonDraw)){
					boolean canPlay=false;
					for(Player p:getPlayers().values())
						canPlay|=(p!=getPlayer() && Resource.calculateTotalResourcesCommodities(p)>0);
					if(!canPlay)
						new Dialog("", Skins.METAL).text("No player has resource/commodities.").button("ok").show(getStage());
					else{
						FishDrawCommand command=new FishDrawCommand(getPlayer().getPeer().getId(), 1);
						executeFishCommand(3, command, false);
					}
				}else if(event.getTarget().isDescendantOf(buttonBank)){
					FishBankCommand command=new FishBankCommand(getPlayer().getPeer().getId(), 0);
					executeFishCommand(4, command, false);
				}else if(event.getTarget().isDescendantOf(buttonRoad)){
					FishBuildCommand command=new FishBuildCommand(getPlayer().getPeer().getId(),true, 1);
					executeFishCommand(4, command, false);
				}else if(event.getTarget().isDescendantOf(buttonShip)){
					FishBuildCommand command=new FishBuildCommand(getPlayer().getPeer().getId(),false, 1);
					executeFishCommand(4, command, false);
				}else if(event.getTarget().isDescendantOf(buttonProgress)){
					if(getPlayer().getCards().size()==4)
						new Dialog("", Skins.METAL).text("You already have 4 progress cards").button("ok").show(getStage());
					else{
						FishProgressCommand command=new FishProgressCommand(getPlayer().getPeer().getId(), 1);
						executeFishCommand(7, command, false);
					}
				}else if(event.getTarget().isDescendantOf(buttonCancel)){

				}else clicked=false;
				
				if(clicked)
					fishDialog.hide();
			}
		});
	}
	
	private void executeFishCommand(final int cost, final FishCommand command, final boolean send){
		final TokenDialog tokenDialog=new TokenDialog(getPlayer(), cost);
		tokenDialog.show(getStage());
		tokenDialog.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				int total=tokenDialog.getTotal();
				if(event.getTarget().isDescendantOf(tokenDialog.getButtonAccept())){
					if(total<cost){
						new Dialog("", Skins.METAL).text("Please select more tokens").button("ok").show(getStage());
						return;
					}
					command.setSelectedTokens(tokenDialog.getSelectedTokens());
					if(send){
						JSONObject obj=GameCommand.getJsonCommand(command, "fish");
						getSocket().emit("catan", obj, GameStage.ACK);
					}
					command.execute(getStage(), FishAction.this);
				}else if(event.getTarget().isDescendantOf(tokenDialog.getButtonCancel())){
					
				}else return;
				tokenDialog.hide();
			}
		});
	}
	
	public void removeSelectedTokens(ArrayList<Integer> selectedTokens, Player player){
		ArrayList<FishToken> removed=new ArrayList<FishToken>();
		for(int index:selectedTokens){
			removed.add(player.getFishToken().get(index));
		}
		player.getFishToken().removeAll(removed);
		for(FishToken token:removed)
			getMap().getFishToken().add(token);
	}
	
	private boolean canBuildRoad(){
		return true;
	}
	
	private boolean canBuildShip(){
		return true;
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
							if(obj.getString("action").equals("fish")){
								GameCommand command=GameCommand.stringToCommand(obj.getString("command"));
								command.execute(getStage(), FishAction.this);
							}else
								return;
							ack.call();
						}catch(Exception e){e.printStackTrace();}
					}
				});
			}
		});
	}

	@Override
	public void cleanUp() {
		getSocket().off("catan", socketListener);
		getToolbar().getFishButton().removeListener(clickListener);
	}

}
