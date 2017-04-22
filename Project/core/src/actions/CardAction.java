package actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.json.JSONObject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import algorithms.Algorithms;
import cards.Card;
import cards.PoliticsCard;
import cards.ScienceCard;
import cards.TradeCard;
import commands.*;
import comp361.catan.City;
import comp361.catan.Edge;
import comp361.catan.EdgeConstruction;
import comp361.catan.GameStage;
import comp361.catan.Knight;
import comp361.catan.Notification;
import comp361.catan.Player;
import comp361.catan.Resource;
import comp361.catan.ResourceType;
import comp361.catan.Settlement;
import comp361.catan.Skins;
import comp361.catan.Tile;
import comp361.catan.TileType;
import comp361.catan.Vertex;
import comp361.catan.VertexConstruction;
import io.socket.client.Ack;
import io.socket.emitter.Emitter.Listener;
import ui.CardWindow;

public class CardAction extends GameAction{
	
	private CardWindow cardWindow;
	private ClickListener cardListener, cancelListener, mapListener;
	private Vertex vertex, firstVertex, secondVertex;
	private Edge edge, firstEdge, secondEdge;
	private Card currentCard=null;
	private Listener socketListener;
	private Tile tile, firstTile, secondTile;
	private int numKnights=0;
	private int answerCount=0, requestCount=0;
	private ArrayList<Dialog> temporaryDialogs=new ArrayList<Dialog>();

	public CardAction(Notification notif, GameStage gameStage) {
		super(notif, gameStage);
		cardWindow=getToolbar().getCardWindow();
		setupSocket();
		addCardListener();
		getToolbar().getCancelButton().addListener(cancelListener=new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				cancelProgressCard();
			}
		});
		addMapListener();
	}
	
	public void clearTemporaryDialogs(){
		for(Dialog dialog:temporaryDialogs)
			dialog.hide();
		temporaryDialogs.clear();
	}
	
	public void addTemporaryDialog(Dialog dialog){
		temporaryDialogs.add(dialog);
	}
	
	public void cancelProgressCard(){
		getToolbar().activateCancelButton(false, "");
		firstTile=null;
		secondTile=null;
		if(firstEdge!=null){
			firstEdge.highlight=false;
			firstEdge=null;
		}
		if(secondEdge!=null){
			secondEdge.highlight=false;
			secondEdge=null;
		}
		if(firstVertex!=null){
			firstVertex.highlight=false;
			firstVertex=null;
		}
		if(secondVertex!=null){
			secondVertex.highlight=false;
			secondVertex=null;
		}
		currentCard=null;
		numKnights=0;
		if(getPlayer().isTurn())
			getToolbar().getInfoWindow().clearMessage();
		TurnAction.LOCKED=false;
		for(Dialog dialog:temporaryDialogs)
			dialog.hide();
		temporaryDialogs.clear();
	}
	
	private void addCardListener(){
		cardWindow.getButtonPlay().addListener(cardListener=new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if(currentCard==null && cardWindow.getCurrentCard()!=null && !TurnAction.LOCKED){
					currentCard=cardWindow.getCurrentCard();
					TurnAction.LOCKED=true;
					if(currentCard==ScienceCard.ENGINEER){
						getToolbar().activateCancelButton(true, "Playing engineer");
						getToolbar().getInfoWindow().displayInfoMessage("Select the city to build a wall on");
					}else if(currentCard==ScienceCard.INVENTOR){
						getToolbar().activateCancelButton(true, "Playing inventor");
						getToolbar().getInfoWindow().displayInfoMessage("Select the first tile you want to switch number"
								+ " (cannot be 2, 12, 6 or 8)");
					}else if(currentCard==ScienceCard.IRRIGATION){
						playIrrigationCard();
					}else if(currentCard==ScienceCard.CRANE){
						playCraneCard();
					}else if(currentCard==ScienceCard.MEDICINE){
						currentCard=null;
						if(!(getPlayer().getResources().get(ResourceType.ORE)>1 && getPlayer().getResources().get(ResourceType.GRAIN)>0))
							new Dialog("Cannot play this card", Skins.METAL).text("You need 2 ore and 1 grain to play this card.").button("ok").show(getStage());
						else{
							currentCard=ScienceCard.MEDICINE;
							getToolbar().activateCancelButton(true, "Playing medicine");
							getToolbar().getInfoWindow().displayInfoMessage("Select the settlement to upgrade.");
						}
					}else if(currentCard==ScienceCard.MINING){
						playMiningCard();
					}else if(currentCard==ScienceCard.ROAD_BUILDING){
						getToolbar().activateCancelButton(true, "Playing road building");
						getToolbar().getInfoWindow().displayInfoMessage("Select the first road/ship to place.");
					}else if(currentCard==ScienceCard.SMITH && canPlaySmithCard()){
						getToolbar().activateCancelButton(true, "Playing smith");
						getToolbar().getInfoWindow().displayInfoMessage("Select the first knight to upgrade.");
					}else if(currentCard==PoliticsCard.DESERTER){
						playDeserterCard();
					}else if(currentCard==PoliticsCard.INTRIGUE){
						playIntrigueCard();
					}else if(currentCard==PoliticsCard.SPY){
						playSpyCard();
					}else if(currentCard==PoliticsCard.WARLORD){
						playWarlordCard();
					}else if(currentCard==PoliticsCard.WEDDING){
						playWeddingCard();
					}else if(currentCard==PoliticsCard.DIPLOMAT){
						playDiplomatCard();
					}else if(currentCard==PoliticsCard.SABOTEUR){
						playSaboteurCard();
					}else if(currentCard==TradeCard.RESOURCE_MONOPOLY){
						playResourceMonopolyCard();
					}else if(currentCard==TradeCard.TRADE_MONOPOLY){
						playTradeMonopolyCard();
					}else if(currentCard==TradeCard.COMMERCIAL_HARBOR){
						playCommercialHarborCard();
					}else if(currentCard==TradeCard.MASTER_MERCHANT){
						playMasterMerchantCard();
					}else if(currentCard==TradeCard.MERCHANT){
						playMerchantCard();
					}else if(currentCard==PoliticsCard.BISHOP){
						playBishopCard();
					}else if(currentCard==TradeCard.MERCHANT_FLEET){
						playMerchantFleetCard();
					}else{
						cancelProgressCard();
					}
				}
			}
		});
	}
	
	private void addMapListener(){
		getMap().addListener(mapListener=new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if(currentCard!=null){
					vertex=getMap().getCurrentVertex();
					tile=getMap().getCurrentTile();
					edge=getMap().getCurrentEdge();
					if(currentCard==ScienceCard.ENGINEER){
						playEngineerCard();
					}else if(currentCard==ScienceCard.INVENTOR){
						playInventorCard();
					}else if(currentCard==ScienceCard.MEDICINE){
						playMedicineCard();
					}else if(currentCard==ScienceCard.ROAD_BUILDING){
						playRoadBuildingCard();
					}else if(currentCard==ScienceCard.SMITH){
						playSmithCard();
					}
				}
			}
		});
	}
	
	private void playMerchantFleetCard(){
		MerchantFleetCommand command=new MerchantFleetCommand(getPlayer().getPeer().getId(), null, 1);
		command.execute(getStage(), this);
	}
	
	private void playBishopCard(){
		BishopCommand command=new BishopCommand(getPlayer().getPeer().getId(), 1);
		command.execute(getStage(), this);
	}
	
	private void playMasterMerchantCard(){
		String playerID=getPlayer().getPeer().getId();
		MasterMerchantCommand command=new MasterMerchantCommand(playerID, playerID, 1);
		command.execute(getStage(), this);
	}
	
	private void playMerchantCard(){
		MerchantCommand command=new MerchantCommand(getPlayer().getPeer().getId(), -1, 1);
		command.execute(getStage(), this);
	}
	
	private void playCommercialHarborCard(){
		String myID=getPlayer().getPeer().getId();
		CommercialHarborCommand command=new CommercialHarborCommand(myID, myID, null, null ,1);
		command.execute(getStage(), this);
	}
	
	private void playTradeMonopolyCard(){
		TradeMonopolyCommand command=new TradeMonopolyCommand(getPlayer().getPeer().getId(), null, 1);
		command.execute(getStage(), this);
	}
	
	private void playResourceMonopolyCard(){
		ResourceMonopolyCommand command=new ResourceMonopolyCommand(getPlayer().getPeer().getId(), null, 1);
		command.execute(getStage(), this);
	}
	
	private void playSaboteurCard(){
		String myID=getPlayer().getPeer().getId();
		SaboteurCommand command=new SaboteurCommand(myID, myID, 1);
		command.execute(getStage(), CardAction.this);
	}
	
	private void playDiplomatCard(){
		DiplomatCommand command=new DiplomatCommand(getPlayer().getPeer().getId(), -1, 1);
		command.execute(getStage(), this);
	}
	
	private void playWeddingCard(){
		boolean canPlay=false;
		requestCount=0;
		for(Player p:getPlayers().values()){
			if(p.getVictoryPoints()>getPlayer().getVictoryPoints() && Resource.calculateTotalResourcesCommodities(p)>0){
				canPlay=true;
				requestCount++;
			}
		}
		if(!canPlay){
			cancelProgressCard();
			new Dialog("Cannot play", Skins.METAL).text("No opponent has more victory points than you, or all the players"
					+ "that have more victory points than you posess no ressources.").button("ok").show(getStage());
		}else{
			this.setRequestCount(requestCount);
			this.setAnswerCount(0);
			final WeddingCommand command=new WeddingCommand(getPlayer().getPeer().getId(), false, null);
			JSONObject obj=GameCommand.getJsonCommand(command, "progress_card");
			getSocket().emit("catan",  obj, new Ack(){public void call(Object... arg0) {}});
			command.execute(getStage(), this);
			Dialog waitingDialog=new Dialog("Waiting", Skins.METAL).text("Waiting for other players.").show(getStage());
			temporaryDialogs.add(waitingDialog);
		}
	}
	
	private void playWarlordCard(){
		boolean hasInactiveKnight=false;
		for(VertexConstruction construction:getPlayer().getVertexContructions()){
			if(construction instanceof Knight){
				Knight knight=(Knight)construction;
				if(!knight.isActive()){
					hasInactiveKnight=true;
					break;
				}
			}
		}
		if(!hasInactiveKnight){
			new Dialog("Cannot play", Skins.METAL).text("You do not have any inactive knights.").button("Ok").show(getStage());
			cancelProgressCard();
		}else{
			final WarlordCommand command=new WarlordCommand(getPlayer().getPeer().getId());
			JSONObject obj=GameCommand.getJsonCommand(command, "progress_card");
			getSocket().emit("catan", obj, GameStage.ACK);
			command.execute(getStage(), CardAction.this);
		}
	}
	
	private void playSpyCard(){
		this.cancelProgressCard();
		final SpyCommand command=new SpyCommand(getPlayer().getPeer().getId());
		final Dialog playerDialog=new Dialog("Choose a player.", Skins.METAL);
		final HashMap<String, TextButton> playerButtons=new HashMap<String, TextButton>();
		final HashMap<Card, Button> cardButtons=new HashMap<Card, Button>();
		int count=0;
		for(Player p:getPlayers().values()){
			if(p!=getPlayer() && p.getCards().size()>0){
				count++;
				TextButton btn=new TextButton("Player "+p.getPeer().getName(), Skins.METAL);
				playerDialog.getContentTable().add(btn);
				playerDialog.getContentTable().row();
				playerButtons.put(p.getPeer().getId(), btn);
			}
		}
		if(count==0){
			playerDialog.getContentTable().add(new Label("No opponent has progress cards in his hands.", Skins.METAL));
			playerDialog.getContentTable().row();
		}
		final TextButton cancelButton=new TextButton("Cancel", Skins.METAL);
		playerDialog.getContentTable().add(cancelButton);
		playerDialog.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				for(String key:playerButtons.keySet()){
					if(event.getTarget().isDescendantOf(playerButtons.get(key))){
						playerDialog.getContentTable().clearChildren();
						playerDialog.getTitleLabel().setText("Choose a card");
						Player target=getPlayers().get(key);
						command.setTarget(key);
						for(Card card:target.getCards()){
							if(!cardButtons.containsKey(card)){
								TextButton cardButton=new TextButton(card+"", Skins.METAL);
								cardButtons.put(card, cardButton);
								playerDialog.getContentTable().add(cardButton);
								playerDialog.getContentTable().row();
							}
						}
						break;
					}
				}
				for(Card card:cardButtons.keySet()){
					if(event.getTarget().isDescendantOf(cardButtons.get(card))){
						command.setStolen(card);
						JSONObject obj=GameCommand.getJsonCommand(command, "progress_card");
						getSocket().emit("catan",  obj, GameStage.ACK);
						command.execute(getStage(), CardAction.this);
						playerDialog.hide();
						break;
					}
				}
				if(event.getTarget().isDescendantOf(cancelButton)){
					playerDialog.hide();
				}
			}
		});
		playerDialog.show(getStage());
	}
	
	private void playIntrigueCard(){
		String playerID=getPlayer().getPeer().getId();
		IntrigueCommand command=new IntrigueCommand(playerID, playerID, 1);
		command.execute(getStage(), this);
	}
	
	private void playDeserterCard(){
		final Dialog dialog=new Dialog("Choose player to steal from.", Skins.METAL);
		int count=0;
		final HashMap<String, TextButton> playerButtons=new HashMap<String, TextButton>();
		for(Player p:getPlayers().values()){
			if(p!=getPlayer()){
				for(VertexConstruction cons:p.getVertexContructions()){
					if(cons instanceof Knight){
						count++;
						TextButton button=new TextButton("Player "+p.getPeer().getName(), Skins.METAL);
						playerButtons.put(p.getPeer().getId(), button);
						dialog.getContentTable().add(button);
						dialog.getContentTable().row();
						break;
					}
				}
			}
		}
		if(count==0){
			dialog.getContentTable().add(new Label("There are no players to steal knights from.", Skins.METAL));
			dialog.getContentTable().row();
		}
		final TextButton buttonCancel=new TextButton("Cancel", Skins.METAL);
		dialog.getContentTable().add(buttonCancel);
		dialog.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				for(String key:playerButtons.keySet()){
					Player p=getPlayers().get(key);
					if(event.getTarget().isDescendantOf(playerButtons.get(p.getPeer().getId()))){
						String targetID=p.getPeer().getId();
						dialog.hide();
						Dialog waitingDialog=new Dialog("Waiting for targeted player...", Skins.METAL);
						waitingDialog.show(getStage());
						temporaryDialogs.add(waitingDialog);
						final DeserterCommand command=new DeserterCommand(getPlayer().getPeer().getId(), targetID, 1);
						JSONObject obj=GameCommand.getJsonCommand(command, "progress_card");
						getSocket().emit("catan", obj, new Ack(){public void call(Object... arg0) {}});
						break;
					}
				}
				if(event.getTarget().isDescendantOf(buttonCancel)){
					dialog.hide();
				}
			}
		});
		dialog.show(getStage());
	}
	
	private boolean canPlaySmithCard(){
		for(Vertex v:getMap().getVertices().values()){
			if(v.getConstruction() instanceof Knight && v.getConstruction().getOwner()==getPlayer()){
				Knight knight=(Knight)v.getConstruction();
				if(knight.getLevel()<2 || (knight.getLevel()<3 && getPlayer().getPoliticsLevel()>=3))
					numKnights++;
			}
		}
		if(numKnights==0){
			new Dialog("You need knights.", Skins.METAL).button("ok").text("You need to have at least one"
					+ " upgradable knight in order to play this progress card.").show(getStage());
			cancelProgressCard();
			return false;
		}
		return true;
	}
	
	private void playSmithCard(){
		if((firstVertex==null || (secondVertex==null && vertex!=firstVertex)) && vertex.getConstruction() instanceof Knight && vertex.getConstruction().getOwner()==getPlayer()){
			Knight knight=(Knight)vertex.getConstruction();
			if(knight.getLevel()<2 || (knight.getLevel()<3 && getPlayer().getPoliticsLevel()>=3)){
				if(firstVertex==null){
					firstVertex=vertex;
					firstVertex.highlight=true;
				}
				else if(secondVertex==null){
					secondVertex=vertex;
					secondVertex.highlight=true;
				}
				if(numKnights==1)
					sendSmithCommand();
				else if(numKnights==2 && secondVertex==null)
					getToolbar().getInfoWindow().setInfoMessage("Select the second knight to upgrade.");
				else
					sendSmithCommand();
			}
		
		}
	}
	
	private void sendSmithCommand(){
		getToolbar().activateCancelButton(false,  "");
		int v1=firstVertex.id;
		int v2=-1;
		if(secondVertex!=null)
			v2=secondVertex.id;
		final SmithCommand command=new SmithCommand(getPlayer().getPeer().getId(), v1, v2);
		JSONObject obj=GameCommand.getJsonCommand(command, "progress_card");
		getSocket().emit("catan", obj, GameStage.ACK);
		command.execute(getStage(), CardAction.this);
	}
	
	private void playRoadBuildingCard(){
		if(edge!=null && edge.getConstruction()==null){
			if(firstEdge==null && Algorithms.isEdgeValid(edge, getPlayer(), true, true, getMap())){
				firstEdge=edge;
				firstEdge.highlight=true;
				getToolbar().getInfoWindow().displayInfoMessage("Select the second road/ship to place.");
			}else if(edge!=firstEdge && secondEdge==null && Algorithms.isEdgeValid(edge, getPlayer(), true, true, getMap())){
				secondEdge=edge;
				final RoadBuildingCommand command=new RoadBuildingCommand(getPlayer().getPeer().getId(), firstEdge.id, secondEdge.id);
				JSONObject obj=GameCommand.getJsonCommand(command, "progress_card");
				getToolbar().activateCancelButton(false, "");
				getSocket().emit("catan", obj, GameStage.ACK);
				command.execute(getStage(), CardAction.this);
			}
		}
	}
	
	private void playMiningCard(){
		final MiningCommand command=new MiningCommand(getPlayer().getPeer().getId());
		JSONObject obj=GameCommand.getJsonCommand(command,  "progress_card");
		getSocket().emit("catan", obj, GameStage.ACK);
		command.execute(getStage(),  CardAction.this);
	}
	
	private void playMedicineCard(){
		if(!(vertex!=null && vertex.getConstruction() instanceof Settlement && vertex.getConstruction().getOwner()==getPlayer()))
			return;
		else{
			getToolbar().activateCancelButton(false, "");
			final MedicineCommand command=new MedicineCommand(getPlayer().getPeer().getId(), vertex.id);
			JSONObject obj=GameCommand.getJsonCommand(command,  "progress_card");
			getSocket().emit("catan", obj, GameStage.ACK);
			command.execute(getStage(), CardAction.this);
		}
	}
	
	private void playCraneCard(){
		// UpgradeDisciplineCommand is both for the crane card and the regular discipline upgrade
		UpgradeDisciplineCommand command=new UpgradeDisciplineCommand(getPlayer().getPeer().getId(), "", true, 0);
		command.execute(getStage(), this);
	}
	
	private void playIrrigationCard(){
		final IrrigationCommand command=new IrrigationCommand(getPlayer().getPeer().getId());
		JSONObject obj=GameCommand.getJsonCommand(command, "progress_card");
		getSocket().emit("catan", obj, GameStage.ACK);
		command.execute(getStage(), CardAction.this);
	}
	
	private void playInventorCard(){
		ArrayList<Integer> restricted=new ArrayList<Integer>(Arrays.asList(new Integer[]{2,12,6,8,7}));
		if(tile!=null && !restricted.contains(tile.getTileNumber())){
			if(firstTile==null){
				firstTile=tile;
				getToolbar().getInfoWindow().displayInfoMessage("Select the second tile you want to switch number"
						+ " (cannot be 2, 12, 6 or 8)");
			}else if(secondTile==null && tile!=firstTile){
				secondTile=tile;
				getToolbar().activateCancelButton(false, "");
				final InventorCommand command=new InventorCommand(getPlayer().getPeer().getId(), firstTile.id, secondTile.id);
				JSONObject obj=GameCommand.getJsonCommand(command, "progress_card");
				getSocket().emit("catan", obj, GameStage.ACK);
				command.execute(getStage(), CardAction.this);
			}
		}
	}
	
	private void playEngineerCard(){
		boolean valid=false;
		if(vertex!=null && vertex.getConstruction() instanceof City){
			City city=(City)vertex.getConstruction();
			if(!city.hasWall() && city.getOwner()==getPlayer() && getPlayer().getNumWalls()<3)
				valid=true;
		}
		if(!valid)
			return;
		getToolbar().activateCancelButton(false, "");
		final EngineerCommand command=new EngineerCommand(getPlayer().getPeer().getId(), vertex.id);
		JSONObject obj=GameCommand.getJsonCommand(command,  "progress_card");
		getSocket().emit("catan", obj, GameStage.ACK);
		command.execute(getStage(), CardAction.this);
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
								case "progress_card":
									GameCommand.stringToCommand(obj.getString("command")).execute(getStage(), CardAction.this);;
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

	public void setAnswerCount(int count){
		this.answerCount=count;
	}
	
	public int getAnswerCount(){
		return this.answerCount;
	}
	
	public void setRequestCount(int count){
		this.requestCount=count;
	}
	
	public int getRequestCount(){
		return this.answerCount;
	}
	
	@Override
	public void cleanUp() {
		cardWindow.getButtonPlay().removeListener(cardListener);
		getToolbar().getCancelButton().removeListener(cancelListener);
		getSocket().off("catan", socketListener);
	}

}
