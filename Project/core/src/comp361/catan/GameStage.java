package comp361.catan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import actions.DebugAction;
import actions.GameAction;
import actions.RollAction;
import actions.SetupAction;
import actions.TurnAction;
import cards.Card;
import cards.PoliticsCard;
import cards.ScienceCard;
import cards.TradeCard;
import commands.GameCommand;
import commands.TestCommand;
import io.socket.client.Ack;
import io.socket.client.Socket;
import io.socket.emitter.Emitter.Listener;
import ui.*;
import util.CardUtility;

/**
 * This is the main in-game stage. It sets up the map by loading it from the disk,
 * it restores the game state from previous save files and it sets up the game actions.
 * 
 */
public class GameStage extends Stage{
	
	private Socket socket;
	private Game game;
	private Notification notification;
	public static GameState GAME_STATE=GameState.WAITING;
	private GameAction currentAction;
	private Toolbar toolbar;
	private Map map;
	private Peer myPeer;
	int hoverNum=0;
	private Player currentPlayer;
	private HashMap<String, Player> players=new HashMap<String, Player>();
	private boolean mapCreated=false;
	private ArrayList<Card> cards=new ArrayList<Card>();
	public static final Ack ACK=new Ack(){
		public void call(Object... arg0) {
			
		};
	};
	public static final HashMap<String, Object> STORE=new HashMap<String, Object>();
	private Music music;
	private boolean gameWon=false;
	private boolean ready=false;
	private boolean cheatActive=false;
	public boolean turnStarted=false;
	
	public GameStage(Game game, Socket soc, Notification notif, Peer myPeer){
		this.myPeer=myPeer;
		this.game=game;
		this.socket=soc;
		this.notification=notif;
		TurnAction.LOCKED=false;
		music=Gdx.audio.newMusic(Gdx.files.internal("music/music.mp3"));
		music.setLooping(true);
		music.setPosition(5);
		music.setVolume(0);
		music.play();
		if(game.getHost()==myPeer)
			loadMap(null);
		setupSocket();
	}
	
	@Override
	public void act(float delta) {
		super.act(delta);
		if(!mapCreated)
			return;
		
		if(Gdx.input.isKeyPressed(Input.Keys.RIGHT))
			map.scroll(8, 0);
		if(Gdx.input.isKeyPressed(Input.Keys.LEFT))
			map.scroll(-8, 0);
		if(Gdx.input.isKeyPressed(Input.Keys.UP))
			map.scroll(0, -8);
		if(Gdx.input.isKeyPressed(Input.Keys.DOWN))
			map.scroll(0, 8);
		
		map.setHovered(hoverNum==0);
		if(currentAction!=null)
			currentAction.act(delta);
		
		if(ready){
			int neededVP=game.getVictoryPoints();
			if(currentPlayer.hasBoot())
				neededVP++;
			if(!gameWon && currentPlayer.isTurn() && currentPlayer.getVictoryPoints()>=neededVP){
				gameWon=true;
				try {
					JSONObject obj=new JSONObject();
					obj.put("action", "win");
					getSocket().emit("catan", obj, GameStage.ACK);
					getSocket().off();
					winGame();
				} catch (JSONException e) {e.printStackTrace();}
			}
		}
	}
	
	private void winGame(){
		TurnAction.LOCKED=true;
		Player winner=null;
		for(Player p:players.values()){
			if(p.isTurn()){
				winner=p;
				break;
			}
		}
		Dialog winDialog=new Dialog("Game ended!", Skins.METAL);
		Label label=new Label("Player "+winner.getPeer().getName()+" won the game!", Skins.METAL);
		winDialog.getContentTable().add(label);
		winDialog.getContentTable().row();
		Image img=null;
		if(winner==currentPlayer)
			img=new Image(new Texture(Gdx.files.internal("ui/wingame.png")));
		else
			img=new Image(new Texture(Gdx.files.internal("ui/losegame.png")));
		float ratio=img.getWidth()/img.getHeight();
		winDialog.getContentTable().add(img).height(400).width(400*ratio);
		winDialog.getContentTable().row();
		TextButton buttonDone=new TextButton("Leave", Skins.METAL);
		winDialog.getContentTable().add(buttonDone);
		winDialog.getContentTable().row();
		winDialog.show(this);
		buttonDone.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				getSocket().disconnect();
				notification.enterMenu();
			}
		});
	}
	
	public Music getMusic(){
		return this.music;
	}
	
	public void disconnect(){
		socket.disconnect();
		notification.enterMenu();
	}

	public void saveGame(){
		game.setSaved(true);
		String mapString=MapParser.export(map, this).toString();
		game.setMapContent(mapString);
	}
	
	private void loadMap(JSONArray order){
		gameWon=false;
		try{
			GAME_STATE=GameState.FIRST_PLACEMENT;
			HashMap<Integer, Player> playerMap=new HashMap<Integer, Player>();
			JSONObject obj=new JSONObject(game.getMapContent());
			map=new Map();
			MapParser.parse(obj, map, playerMap, cards, this);
			if(game.isSaved()){
				for(Player p:playerMap.values()){
					for(Peer peer:game.getPlayers()){
						if(peer!=null){
							System.out.println("Peer has order "+peer.getSlot());
						}
						if(peer!=null && p.getOrder()==peer.getSlot()){
							p.setPeer(peer);
							players.put(peer.getId(), p);
						}
					}
					if(p.getOrder()==myPeer.getSlot())
						currentPlayer=p;
				}
				GAME_STATE=GameState.valueOf(obj.getString("state"));
			}else initCards();
			map.setBounds(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			this.addActor(map);
			mapCreated=true;
		}catch(Exception e){e.printStackTrace();}
	}
	
	private void initCards(){
		addCard(ScienceCard.ALCHEMIST, 2);
		addCard(ScienceCard.INVENTOR, 2);
		addCard(ScienceCard.CRANE, 2);
		addCard(ScienceCard.IRRIGATION, 2);
		addCard(ScienceCard.ENGINEER, 1);
		addCard(ScienceCard.MEDICINE, 2);
		addCard(ScienceCard.MINING, 2);
		addCard(ScienceCard.PRINTER, 1);
		addCard(ScienceCard.ROAD_BUILDING, 2);
		addCard(ScienceCard.SMITH, 2);
		addCard(PoliticsCard.BISHOP, 2);
		addCard(PoliticsCard.DIPLOMAT, 2);
		addCard(PoliticsCard.CONSTITUTION, 1);
		addCard(PoliticsCard.DESERTER, 2);
		addCard(PoliticsCard.INTRIGUE, 2);
		addCard(PoliticsCard.SABOTEUR, 2);
		addCard(PoliticsCard.SPY, 3);
		addCard(PoliticsCard.WARLORD, 2);
		addCard(PoliticsCard.WEDDING, 2);
		addCard(TradeCard.COMMERCIAL_HARBOR, 2);
		addCard(TradeCard.MASTER_MERCHANT, 2);
		addCard(TradeCard.MERCHANT, 6);
		addCard(TradeCard.MERCHANT_FLEET, 2);
		addCard(TradeCard.RESOURCE_MONOPOLY, 4);
		addCard(TradeCard.TRADE_MONOPOLY, 2);
		Collections.shuffle(cards);
	}
	
	private void addCard(Card card, int number){
		for(int i=0;i<number;i++)
			cards.add(card);
	}
	
	private void setupUI(){
		addActor(toolbar=new Toolbar(this));
		InputListener listener=new InputListener(){
			@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
				hoverNum++;
			}
			@Override
			public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
				hoverNum--;
			}
		};
		for(Actor actor:toolbar.getChildren())
			actor.addListener(listener);
		toolbar.addPlayer(currentPlayer);
		for(Player p:players.values())
			if(p!=currentPlayer)
				toolbar.addPlayer(p);
	}

	private void beginGame(){
		try{
			ArrayList<Integer> turnOrder=new ArrayList<Integer>();
			for(int i=0;i<game.getPlayers().length;i++)
				turnOrder.add(i+1);
			Color colors[]=new Color[]{Color.RED, Color.MAGENTA, Color.ORANGE, Color.OLIVE, Color.FIREBRICK};
			JSONObject obj=new JSONObject();
			for(Peer peer:game.getPlayers()){
				if(peer!=null){
					int index=(int)(turnOrder.size()*Math.random());
					int order=turnOrder.get(index);
					turnOrder.remove(index);
					Color col=colors[order-1];
					JSONObject peerObj=new JSONObject();
					peerObj.put("order", order);
					peerObj.put("red", (double)col.r);
					peerObj.put("green", (double)col.g);
					peerObj.put("blue", (double)col.b);
					obj.put(peer.getId(), peerObj);
					Player player=new Player(peer, col, order);
					if(player.getOrder()==1)
						player.setTurn(true);
					player.getPeer().setSlot(player.getOrder());
					players.put(player.getPeer().getId(), player);
					if(peer==myPeer){
						currentPlayer=player;
						currentPlayer.setCurrentPlayer(true);
					}
				}
			}
			JSONArray cardArr=new JSONArray();
			for(Card card:cards)
				cardArr.put(card.toString());
			obj.put("cards", cardArr);
			obj.put("action", "turn_order");
			setupUI();
			socket.emit("catan", obj, new Ack(){
				@Override
				public void call(Object... arg0) {
					setCurrentAction();
				}
			});
		}catch(Exception e){e.printStackTrace();}
	}
	
	private void setCurrentAction(){
		NotificationAdapter adapter=new NotificationAdapter(){
			@Override
			public void actionCompleted(GameAction nextAction) {
				currentAction=nextAction;
			}
		};
		if(GAME_STATE==GameState.FIRST_PLACEMENT)
			currentAction=new SetupAction(adapter, GameStage.this);
		else if(GAME_STATE==GameState.TURN_FIRST_PHASE)
			currentAction=new RollAction(adapter, GameStage.this);
		else if(GAME_STATE==GameState.TURN_SECOND_PHASE)
			currentAction=new TurnAction(adapter, GameStage.this);
		new DebugAction(new NotificationAdapter(), GameStage.this);
		ready=true;
	}
	
	private void sendMap(){
		try{
			JSONObject obj=new JSONObject();
			obj.put("action", "send_map");
			obj.put("map", game.getMapContent());
			JSONArray order=new JSONArray();
			if(game.isSaved()){
				for(Player player:players.values()){
					JSONObject orderObj=new JSONObject();
					orderObj.put("id", player.getPeer().getId());
					orderObj.put("turn", player.getOrder());
					order.put(orderObj);
				}
				obj.put("order", order);
			}else obj.put("order", new JSONArray());
			socket.emit("catan", obj, new Ack(){
				@Override
				public void call(Object... arg0) {
					Gdx.app.postRunnable(new Runnable(){@Override
					public void run() {
						if(!game.isSaved())
							beginGame();
						else{
							setupUI();
							setCurrentAction();
						}
					}});
				}
			});
		}catch(Exception e){e.printStackTrace();}
	}
	
	private void setupSocket(){
		socket.on("ready", new Listener(){
			@Override
			public void call(final Object... arg0) {
				Gdx.app.postRunnable(new Runnable(){
					@Override
					public void run() {
						JSONArray readyArr=(JSONArray)arg0[0];
						if(readyArr.length()==game.getPlayers().length && game.getHost()==myPeer)
							sendMap();
					}
				});
			}
		}).on("catan", new Listener(){
			@Override
			public void call(Object... arg0) {
				final Ack ack=(Ack)arg0[arg0.length-1];
				final JSONObject obj=(JSONObject)arg0[0];
				Gdx.app.postRunnable(new Runnable(){
					public void run() {
						try{
							switch(obj.getString("action")){
								case "message":
									receivedMessage(obj);
									break;
								case "turn_order":
									receivedTurnOrder(obj);
									break;
								case "send_map":
									game.setMapContent(obj.getString("map"));
									loadMap(obj.getJSONArray("order"));
									if(game.isSaved()){
										setupUI();
										setCurrentAction();
									}
									break;
								case "win":
									getSocket().off();
									ack.call();
									winGame();
									return;
								default:
									return;
							}
						}catch(Exception e){e.printStackTrace();}
						ack.call();
					}
				});
			}
		}).on("player_left", new Listener(){
			@Override
			public void call(Object... arg0) {
				Gdx.app.postRunnable(new Runnable(){
					@Override
					public void run() {
						socket.disconnect();
						notification.enterMenu();
					}
				});
			}
		}).on("reset_game", new Listener(){
			@Override
			public void call(Object... arg0) {
				Gdx.input.setInputProcessor(null);
				Gdx.app.postRunnable(new Runnable(){
					@Override
					public void run() {
						for(Player p:getPlayers().values())
							p.getPeer().setMarked(false);
						socket.off();
						game.setSaved(true);
						music.stop();
						music.dispose();
						notification.gameStarted(game, socket, myPeer);
					}
				});
			}
		});
		socket.emit("ready", "");
	}

	private void receivedTurnOrder(JSONObject obj) throws JSONException{
		for(Peer peer:game.getPlayers()){
			JSONObject peerObj=obj.getJSONObject(peer.getId());
			Color peerColor=new Color((float)peerObj.getDouble("red")
					,(float)peerObj.getDouble("green"),(float)peerObj.getDouble("blue"),1);
			final Player player=new Player(peer, peerColor, peerObj.getInt("order"));
			players.put(player.getPeer().getId(), player);
			if(player.getOrder()==1)
				player.setTurn(true);
			player.getPeer().setSlot(player.getOrder());
			if(peer==myPeer){
				currentPlayer=player;
				currentPlayer.setCurrentPlayer(true);
			}
		}
		JSONArray cardArr=obj.getJSONArray("cards");
		cards.clear();
		for(int i=0;i<cardArr.length();i++)
			cards.add(CardUtility.getCardFromString(cardArr.getString(i)));
		setupUI();
		setCurrentAction();
	}
	
	private void receivedMessage(JSONObject obj) throws JSONException{
		Player other=players.get(obj.getString("id"));
		toolbar.getChatWindow().receivedMessage(obj.getString("message"), other);
	}
	
	public Toolbar getToolbar(){
		return this.toolbar;
	}
	
	public Player getCurrentPlayer(){
		return this.currentPlayer;
	}
	
	public HashMap<String, Player> getPlayers(){
		return this.players;
	}
	
	public Map getMap(){
		return this.map;
	}
	
	public Socket getSocket(){
		return this.socket;
	}
	
	public Game getGame(){
		return this.game;
	}
	
	public ArrayList<Card> getCards(){
		return this.cards;
	}
	
	@Override
	public void dispose() {
		if(music!=null)
			music.dispose();
		super.dispose();
	}
	
	public boolean getCheatActive(){
		return this.cheatActive;
	}
	
	public void setCheatActive(boolean cheatActive){
		this.cheatActive=cheatActive;
	}
	
	public boolean getTurnStarted(){
		return this.turnStarted;
	}
	
	public void setTurnStarted(boolean turnStarted){
		this.turnStarted=turnStarted;
	}
	
}
