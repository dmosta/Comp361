package commands;

import org.json.JSONObject;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import actions.CardAction;
import algorithms.Algorithms;
import cards.TradeCard;
import comp361.catan.City;
import comp361.catan.GameStage;
import comp361.catan.Player;
import comp361.catan.Settlement;
import comp361.catan.Tile;
import comp361.catan.Vertex;

public class MerchantCommand extends GameCommand{
	
	private String playerID;
	private transient Player player;
	private ClickListener mapListener;
	private int merchantID;
	private transient CardAction action;

	public MerchantCommand(String playerID, int merchantID, int phase) {
		super(phase);
		this.playerID=playerID;
		this.merchantID=merchantID;
	}
	
	@Override
	protected void executeCommand() {
		player=getPlayers().get(playerID);
		action=(CardAction)getAction();
		
		if(getPhase()==1 && player==getPlayer()){
			phase1();
		}else if(getPhase()==2){
			phase2();
		}
		
	}
	
	private void phase2(){
		getToolbar().getChatWindow().log("Player "+player.getPeer().getName()+" has moved the merchant with the merchant progress card.");
		Player owner=getMap().getMerchant().getOwner();
		if(owner==null || owner!=player){
			player.setVictoryPoints(player.getVictoryPoints()+1);
			if(owner!=null)
				owner.setVictoryPoints(owner.getVictoryPoints()-1);
		}
		Tile location=getMap().getTile(merchantID);
		getMap().getMerchant().setLocation(location);
		getMap().getMerchant().setOwner(player);
		player.getCards().remove(TradeCard.MERCHANT);
		getCards().add(TradeCard.MERCHANT);
		action.cancelProgressCard();
	}
	
	private void phase1(){
		getToolbar().getInfoWindow().displayInfoMessage("Choose a new location where to move the merchant.");
		final Tile previousLocation=getMap().getMerchant().getLocation();
		getMap().addListener(mapListener=new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Tile tile=getMap().getCurrentTile();
				if(tile!=null && tile!=previousLocation){
					for(Vertex v:tile.vertices){
						if((v.getConstruction() instanceof Settlement || v.getConstruction() instanceof City)
								&& v.getConstruction().getOwner()==getPlayer()){
							getMap().removeListener(mapListener);
							getToolbar().getInfoWindow().clearMessage();
							MerchantCommand command=new MerchantCommand(playerID, tile.id, 2);
							JSONObject obj=GameCommand.getJsonCommand(command, "progress_card");
							getSocket().emit("catan",  obj, GameStage.ACK);
							command.execute(getStage(), action);
						}
					}
				}
			}
		});
	}

}
