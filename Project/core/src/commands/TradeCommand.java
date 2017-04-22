package commands;

import java.util.HashMap;

import org.json.JSONObject;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import actions.TurnAction;
import comp361.catan.GameStage;
import comp361.catan.Player;
import comp361.catan.ResourceType;
import comp361.catan.Skins;
import ui.TradeWindow;

public class TradeCommand extends GameCommand{

	private static final long serialVersionUID = -6856718229144844196L;
	private String playerID, targetID;
	private boolean bankTrade;
	private Player player;
	private Player target;
	private transient TradeWindow tradeWindow;
	private transient ClickListener listener, cancelListener;
	private HashMap<ResourceType, Integer> wanted;
	private HashMap<ResourceType, Integer> offered;
	private boolean accept;
	private int gold;

	public TradeCommand(String playerID, String targetID, boolean bankTrade, int phase) {
		super(phase);
		this.playerID=playerID;
		this.targetID=targetID;
		this.bankTrade=bankTrade;
	}
	
	public void setGold(int gold){
		this.gold=gold;
	}
	
	private void setAccept(boolean accept){
		this.accept=accept;
	}
	
	private void setTrade(HashMap<ResourceType, Integer> wanted, HashMap<ResourceType, Integer> offered){
		this.wanted=wanted;
		this.offered=offered;
	}

	@Override
	protected void executeCommand() {
		player=getPlayers().get(playerID);
		target=getPlayers().get(targetID);
		tradeWindow=getToolbar().getTradeWindow();
		
		if(getPhase()==0){
			phase0();
		}else if(getPhase()==1){
			if(bankTrade){
				HashMap<ResourceType, Integer> resources=player.getResources();
				String exchange="Player "+player.getPeer().getName()+" has traded ";
				for(ResourceType type:offered.keySet()){
					resources.put(type, resources.get(type)-offered.get(type));
					if(offered.get(type)>0)
						exchange+=offered.get(type)+" "+type+" ";
				}
				if(gold>0){
					player.setGold(player.getGold()-gold);
					exchange+=gold+" GOLD";
				}
				exchange+=" for ";
				for(ResourceType type:wanted.keySet()){
					resources.put(type, resources.get(type)+wanted.get(type));
					if(wanted.get(type)>0)
						exchange+=wanted.get(type)+" "+type+" ";
				}
				getToolbar().getChatWindow().log(exchange+" with the bank");
				TurnAction.LOCKED=false;
			}else if(getPlayer()==target)
				receivedOffer();
		}else if(getPhase()==2){
			if(player==getPlayer()){
				Dialog waitingDialog=(Dialog)GameStage.STORE.get("waitingDialog");
				waitingDialog.hide();
				new Dialog("", Skins.METAL).text("Trade was "+(accept?"accepted.":"refused.")).button("ok").show(getStage());
			}
			String message="Player "+target.getPeer().getName()+" has "+(accept?"accepted":"refused")+" to trade ";
			for(ResourceType type:offered.keySet()){
				if(offered.get(type)>0)
					message+=offered.get(type)+" "+type+" ";
				if(accept){
					player.getResources().put(type, player.getResources().get(type)-offered.get(type));
					target.getResources().put(type, target.getResources().get(type)+offered.get(type));
				}
			}
			if(gold>0){
				target.setGold(target.getGold()+gold);
				player.setGold(player.getGold()-gold);
				message+=gold+" GOLD";
			}
			message+=" for ";
			for(ResourceType type:wanted.keySet()){
				if(wanted.get(type)>0)
					message+=wanted.get(type)+" "+type+" ";
				if(accept){
					player.getResources().put(type, player.getResources().get(type)+wanted.get(type));
					target.getResources().put(type, target.getResources().get(type)-wanted.get(type));
				}
			}
			getToolbar().getChatWindow().log(message+" with player "+player.getPeer().getName());
			TurnAction.LOCKED=false;
		}
	}
	
	private void receivedOffer(){
		tradeWindow.show(false, true, player.getPeer().getName());
		tradeWindow.setOffer(wanted, offered);
		tradeWindow.setGold(gold);
		tradeWindow.addListener(listener=new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				boolean accept=event.getTarget().isDescendantOf(tradeWindow.getButtonAccept());
				boolean refuse=event.getTarget().isDescendantOf(tradeWindow.getButtonRefuse());
				if(accept){
					for(ResourceType type:getPlayer().getResources().keySet()){
						if(getPlayer().getResources().get(type)<wanted.get(type)){
							new Dialog("", Skins.METAL).text("You do not have enough resources to accept the trade").button("ok").show(getStage());
							return;
						}
					}
				}
				if(accept || refuse){
					tradeWindow.removeListener(listener);
					TradeCommand command =new TradeCommand(playerID, targetID, bankTrade, 2);
					command.setTrade(wanted, offered);
					command.setGold(gold);
					command.setAccept(accept);
					JSONObject obj=GameCommand.getJsonCommand(command, "trade");
					getSocket().emit("catan",  obj, GameStage.ACK);
					command.execute(getStage(), getAction());
					tradeWindow.hide();
				}
			}
		});
	}
	
	private void phase0(){
		tradeWindow.show(bankTrade, false, target.getPeer().getName());
		tradeWindow.getSendButton().addListener(listener=new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				int totalGive=tradeWindow.getTotalGive();
				int totalTake=tradeWindow.getTotalTake();
				if(totalTake==0 && totalGive==0){
					new Dialog("Incomplete", Skins.METAL).text("You need to trade something").button("ok").show(getStage());
					return;
				}else if(totalGive!=totalTake && bankTrade){
					new Dialog("Incomplete", Skins.METAL).text("Select more resources to receive").button("ok").show(getStage());
					return;
				}
				tradeWindow.getSendButton().removeListener(listener);
				tradeWindow.getButtonCancel().removeListener(cancelListener);
				TradeCommand command=new TradeCommand(playerID, targetID, bankTrade, 1);
				command.setTrade(tradeWindow.getWanted(), tradeWindow.getOffered());
				System.out.println("gold "+tradeWindow.getGold());
				command.setGold(tradeWindow.getGold());
				JSONObject obj=GameCommand.getJsonCommand(command, "trade");
				getSocket().emit("catan",  obj, GameStage.ACK);
				command.execute(getStage(), getAction());
				TurnAction.LOCKED=false;
				tradeWindow.hide();//leave this at the end
				if(!bankTrade){
					Dialog waitingDialog=new Dialog("Waiting for answer...", Skins.METAL);
					waitingDialog.show(getStage());
					GameStage.STORE.put("waitingDialog", waitingDialog);
				}
			}
		});
		tradeWindow.getButtonCancel().addListener(cancelListener=new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				tradeWindow.getButtonCancel().removeListener(cancelListener);
				tradeWindow.getSendButton().removeListener(listener);
				tradeWindow.hide();
				TurnAction.LOCKED=false;
			}
		});
	}

}
