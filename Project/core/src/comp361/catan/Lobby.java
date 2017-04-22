package comp361.catan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter.Listener;
import ui.GameCell;
import ui.GameSlotsDialog;
import ui.NewGameDialog;
import util.Tuple;
import util.TupleSelectBox;

/**
 * The lobby stage connects players to the server and handles the creation and
 * the joining of games
 */
public class Lobby extends Stage{
	
	private Notification notification;
	private Socket socket;
	private HashMap<String, Peer> peers=new HashMap<String, Peer>();
	private HashMap<String, Game> games=new HashMap<String, Game>();
	private Label labelPlayers;
	private ScrollPane gamePane;
	private Table gameTable;
	private Peer myPeer;
	private Game currentGame;
	private ArrayList<Dialog> gameDialogs=new ArrayList<Dialog>();
	private boolean connected=false;
	private Timer connectionTimer;
	private Dialog connectionDialog;
	private GameSlotsDialog slotsDialog;
	
	public Lobby(Notification notif, Peer myPeer){
		this.notification=notif;
		this.myPeer=myPeer;
		connect(CatanLauncher.SERVER);
	}
	
	private void connect(String ip){
		setupUI();
		connectionDialog=new Dialog("Connecting", Skins.METAL){
			protected void result(Object object) {
				socket.disconnect();
				notification.enterMenu();
			};
		}.text("Connecting to server.").button("cancel").show(this);
		connectionTimer=new Timer();
		connectionTimer.scheduleTask(new Task(){
			@Override
			public void run() {
				Gdx.app.postRunnable(new Runnable(){
					@Override
					public void run() {
						if(!connected)
							connectionError();
						else
							connectionDialog.remove();
					}
				});
			}
		}, 15);
		setupSocket(ip);
		connectionTimer.start();
	}
	
	private void setupUI(){
		Table table=new Table();
		table.setFillParent(true);
		table.align(Align.top);
		this.addActor(table);
		labelPlayers=new Label("1 player connected", Skins.METAL);
		table.add(labelPlayers);
		table.row();
		HorizontalGroup btnGroup=new HorizontalGroup();
		final TextButton buttonNewGame=new TextButton("New Game", Skins.METAL);
		btnGroup.addActor(buttonNewGame);
		buttonNewGame.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				newGameWindow();
			}
		});
		TextButton buttonDisconnect = new TextButton("Disconnect", Skins.METAL);
		buttonDisconnect.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				socket.disconnect();
				notification.enterMenu();
			}
		});
		btnGroup.addActor(new Label("   ", Skins.METAL));
		btnGroup.addActor(buttonDisconnect);
		table.add(btnGroup);
		table.row();
		gameTable=new Table();
		gamePane=new ScrollPane(gameTable, Skins.METAL);
		gamePane.setFlickScroll(false);
		gamePane.setForceScroll(false, true);
		table.add(gamePane);
	}
	
	private Table getGameCell(int index, final Game game){
		final GameCell rowTable=new GameCell();
		rowTable.hostLabel.setText(index+". "+game.getHost().getName());
		rowTable.playersLabel.setText(game.getConnectedPlayers()+"/"+game.getNumPlayers()+" connected ");
		rowTable.mapLabel.setText(!game.isPreset()?"Custom game":"Preset");
		boolean locked=!game.getPassword().equals("");
		String passwordLabel=locked?"":"[Password]";
		if(game.isSaved()){
			rowTable.mapLabel.setText("");
			if(!locked)
				passwordLabel="[Saved]";
			else passwordLabel="[Saved - Password]";
		}
		rowTable.passwordLabel.setText(passwordLabel);
		rowTable.joinButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				showDetails(1, game);
			}
		});
		return rowTable;
	}
	
	private void showDetails(int gameNumber, final Game game){
		Dialog dialog=new Dialog("Details", Skins.METAL);
		dialog.text("Host: "+game.getHost().getName());
		dialog.getContentTable().row();
		dialog.text(game.getConnectedPlayers()+"/"+game.getNumPlayers()+" Players");
		dialog.getContentTable().row();
		String gameType=game.isPreset()?"Preset":"Custom map";
		if(game.isSaved())
			gameType+=" [saved]";
		dialog.text(gameType);
		dialog.getContentTable().row();
		TextButton joinButton=new TextButton("join", Skins.METAL);
		joinButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if(!game.getPassword().equals("")){
					final TextField passText=new TextField("", Skins.METAL);
					Dialog passDiag=new Dialog("Enter password: ", Skins.METAL){
						protected void result(Object object) {
							if(passText.getText().equals(game.getPassword()))
								joinGame(game);
							else
								new Dialog("Invalid password!", Skins.METAL).button("ok").show(Lobby.this);
						};
					}.button("ok");
					passText.setMessageText("password");
					passDiag.getContentTable().add(passText);
					passDiag.show(Lobby.this);
				}else joinGame(game);
			}
		});
		TextButton cancelButton=new TextButton("cancel", Skins.METAL);
		dialog.button(joinButton).button(cancelButton);
		dialog.show(this);
	}
	
	private void joinGame(final Game game){
		try{
			if(game.getConnectedPlayers()==game.getNumPlayers()){
				new Dialog("This game is full", Skins.METAL).button("ok").show(Lobby.this);
				return;
			}
			if(game.isSaved()){
				slotsDialog=new GameSlotsDialog(game);
				slotsDialog.addListener(new ClickListener(){
					public void clicked(InputEvent event, float x, float y) {
						if(event.getTarget().isDescendantOf(slotsDialog.getButtonCancel()))
							slotsDialog.hide();
						else{
							for(int order:slotsDialog.getButtons().keySet()){
								if(event.getTarget().isDescendantOf(slotsDialog.getButtons().get(order))){
									slotsDialog.hide();
									try{
										joinGameWait(game, order);
									}catch(Exception ex){ex.printStackTrace();}
									break;
								}
							}
						}
					}
				});
				slotsDialog.show(Lobby.this);
			}else
				joinGameWait(game, 0);
		}catch(Exception e){e.printStackTrace();}
	}
	
	private void joinGameWait(final Game game, int slot) throws JSONException{
		currentGame=game;
		JSONObject obj=new JSONObject();
		myPeer.setSlot(slot);
		obj.put("id", game.getHost().getId());
		obj.put("slot", slot);
		socket.emit("join", obj);
		TextButton btnCancel=new TextButton("Cancel", Skins.METAL);
		Dialog joinDialog=new Dialog("Waiting for other players", Skins.METAL).button(btnCancel).show(Lobby.this);
		btnCancel.addListener(new ClickListener(){
			public void clicked(InputEvent event, float x, float y) {
				try{
					currentGame=null;
					JSONObject obj=new JSONObject();
					obj.put("id", game.getHost().getId());
					obj.put("slot", myPeer.getSlot());
					socket.emit("cancel_join", obj);
				}catch(Exception e){e.printStackTrace();}
			}
		});
		gameDialogs.add(joinDialog);
	}
	
	private void setupSocket(String ip){
		try {
			socket = IO.socket(ip);
			socket.on(Socket.EVENT_CONNECT, new Listener() {
				@Override
				public void call(Object... args) {
					Gdx.app.postRunnable(new Runnable(){
						@Override
						public void run() {
							connected();
						}
					});
				}
			});
			socket.on(Socket.EVENT_CONNECT_TIMEOUT, new Listener(){
				@Override
				public void call(Object... arg0) {
					connectionError();
				}
			});
			socket.on(Socket.EVENT_CONNECT_ERROR, new Listener(){
				@Override
				public void call(Object... arg0) {
					connectionError();
				}
			});
			socket.on(Socket.EVENT_ERROR, new Listener(){
				@Override
				public void call(Object... arg0) {
					connectionError();
				}
			});
			socket.on("players", new Listener(){
				@Override
				public void call(final Object... arg0) {
					connected=true;
					Gdx.app.postRunnable(new Runnable(){
						@Override
						public void run() {
							connectionTimer.stop();
							connectionDialog.remove();
							receivedLobbyInfo((JSONObject)arg0[0]);
						}
					});
				}
			}).on("player", new Listener(){
				@Override
				public void call(final Object... arg0) {
					Gdx.app.postRunnable(new Runnable(){
						@Override
						public void run() {
							receivedPlayer((JSONObject)arg0[0]);
						}
					});
				}
			}).on("disconnected", new Listener(){
				@Override
				public void call(final Object... arg0) {
					Gdx.app.postRunnable(new Runnable(){
						@Override
						public void run() {
							receivedDisconnection((JSONObject)arg0[0]);
						}
					});
				}
			}).on("game", new Listener(){
				@Override
				public void call(final Object... arg0) {
					Gdx.app.postRunnable(new Runnable(){
						@Override
						public void run() {
							receivedGame((JSONObject)arg0[0]);
						}
					});
				}
			}).on("cancel_game",new Listener(){
				@Override
				public void call(final Object... arg0) {
					Gdx.app.postRunnable(new Runnable(){
						@Override
						public void run() {
							cancelGame((JSONObject)arg0[0]);
						}
					});
				}
			}).on("start", new Listener(){
				@Override
				public void call(final Object... arg0) {
					Gdx.app.postRunnable(new Runnable(){
						@Override
						public void run() {
							gameStarted((JSONObject)arg0[0]);
						}
					});
				}
			}).on("join", new Listener(){
				@Override
				public void call(final Object... arg0) {
					Gdx.app.postRunnable(new Runnable(){
						@Override
						public void run() {
							playerJoined((JSONObject)arg0[0]);
						}
					});
				}
			}).on("cancel_join", new Listener(){
				@Override
				public void call(final Object... arg0) {
					Gdx.app.postRunnable(new Runnable(){
						@Override
						public void run() {
							cancelJoin((JSONObject)arg0[0]);
						}
					});
				}
			});
			socket.connect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void cancelJoin(JSONObject obj){
		try{
			Game game=games.get(obj.getString("id"));
			if(game!=null){
				game.removePlayer(peers.get(obj.getString("peer")));
				if(game.isSaved()){
					int slot=obj.getInt("slot");
					System.out.println("cancelled with slot "+slot);
					game.getSlots().add(Integer.valueOf(slot));
					if(slotsDialog!=null && slotsDialog.isVisible())
						slotsDialog.displayUI();
				}else System.out.println("not saved");
				displayGames();
			}
		}catch(Exception e){e.printStackTrace();}
	}
	
	private void playerJoined(JSONObject obj){
		try{
			Game game=games.get(obj.getString("id"));
			if(game!=null){
				int slot=obj.getInt("slot");
				Peer peer=peers.get(obj.getString("peer"));
				peer.setSlot(slot);
				game.addPlayer(peer);
				if(game.isSaved()){
					game.getSlots().remove(Integer.valueOf(slot));
					if(slotsDialog!=null && slotsDialog.isVisible())
						slotsDialog.displayUI();
				}
				displayGames();
				if(game.getHost()==myPeer && game.getConnectedPlayers()==game.getNumPlayers())
					socket.emit("start_game", new JSONObject());
			}
		}catch(Exception e){e.printStackTrace();}
	}
	
	private void gameStarted(final JSONObject obj){
		//We need to invoke stage change as a post runnable because active dialogs are
		// not running on the same thread, crashes otherwise
		Gdx.app.postRunnable(new Runnable(){
			@Override
			public void run() {
				try{
					if(currentGame==games.get(obj.getString("id")))
						notification.gameStarted(currentGame, socket, myPeer);
					else{
						games.remove(obj.getString("id"));
						displayGames();
					}
				}catch(Exception e){e.printStackTrace();}
			}
		});
	}
	private void connectionError(){
		socket.disconnect();
		Gdx.app.postRunnable(new Runnable(){
			@Override
			public void run() {
				new Dialog("Error connecting to the server.", Skins.METAL){
					protected void result(Object object) {
						notification.enterMenu();
					};
				}.button("ok").show(Lobby.this);
			}
		});
	}
	
	private void displayGames(){
		gameTable.clear();
		int i=1;
		for(Game game:games.values()){
			if(game.isVisible()){
				gameTable.add(getGameCell(i, game));
				gameTable.row();
				i++;
			}
		}
	}
	private void cancelGame(JSONObject game){
		try {
			for(Dialog diag:gameDialogs){
				diag.remove();
			}
			gameDialogs.clear();
			if(currentGame==games.get(game.getString("id")));
				currentGame=null;
			games.remove(game.getString("id"));
			displayGames();
		} catch (JSONException e) {e.printStackTrace();}
	}
	private void receivedGame(JSONObject obj){
		try {
			boolean saved=obj.getBoolean("saved");
			Game game=new Game(obj.getInt("numPlayers"), peers.get(obj.getString("id")),
					obj.getString("password"), obj.getBoolean("preset"), obj.getString("map"), obj.getBoolean("saved"), obj.getInt("victoryPoints"));
			games.put(obj.getString("id"), game);
			Peer peer=peers.get(obj.getString("id"));
			peer.setSlot(obj.getInt("slot"));
			game.addPlayer(peer);
			if(saved){
				JSONArray slots=obj.getJSONArray("slots");
				for(int i=0;i<slots.length();i++){
					game.getSlots().add(slots.getInt(i));
				}
			}
			displayGames();
		} catch (JSONException e) {e.printStackTrace();}
	}
	
	private void connected(){
		try {
			JSONObject obj=new JSONObject();
			obj.put("name", myPeer.getName());
			obj.put("hash", 422010402);
			obj.put("fingerprint", myPeer.getFingerprint());
			socket.emit("lobby", obj);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	private void receivedLobbyInfo(JSONObject obj){
		try {
			myPeer.setId(obj.getString("id"));
			peers.put(obj.getString("id"), myPeer);
			JSONArray jsonPlayers = obj.getJSONArray("players");
			for(int i=0;i<jsonPlayers.length();i++){
				JSONObject jsonPlayer=jsonPlayers.getJSONObject(i);
				Peer peer=new Peer(jsonPlayer.getString("name"), jsonPlayer.getString("id"), jsonPlayer.getString("fingerprint"));
				peer.setSlot(jsonPlayer.getInt("slot"));
				peers.put(jsonPlayer.getString("id"), peer);
				updatePlayerCount();
			}
			JSONArray jsonGames = obj.getJSONArray("games");
			for(int i=0;i<jsonGames.length();i++){
				JSONObject jsonGame=jsonGames.getJSONObject(i);
				boolean saved=jsonGame.getBoolean("saved");
				Peer host=peers.get(jsonGame.getString("id"));
				Game game=new Game(jsonGame.getInt("numPlayers"), host,
						jsonGame.getString("password"), jsonGame.getBoolean("preset"), jsonGame.getString("map"), jsonGame.getBoolean("saved"), jsonGame.getInt("victoryPoints"));
				JSONArray players=jsonGame.getJSONArray("players");
				for(int j=0;j<players.length();j++){
					Peer other=peers.get(players.getString(j));
					game.addPlayer(other);
					if(other==host)
						other.setSlot(jsonGame.getInt("slot"));
				}
				if(saved){
					JSONArray slotsArr=jsonGame.getJSONArray("slots");
					for(int j=0;j<slotsArr.length();j++){
						System.out.println("here "+slotsArr.getInt(j));
						game.getSlots().add(slotsArr.getInt(j));
					}
					
					Collections.sort(game.getSlots());
				}
				games.put(jsonGame.getString("id"), game);
			}
			displayGames();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void receivedPlayer(JSONObject obj){
		try {
			Peer peer=new Peer(obj.getString("name"), obj.getString("id"), obj.getString("fingerprint"));
			peers.put(obj.getString("id"), peer);
			updatePlayerCount();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void receivedDisconnection(JSONObject obj){
		try {
			peers.remove(obj.getString("id"));
			updatePlayerCount();
		} catch (JSONException e) {e.printStackTrace();}
	}
	
	private void updatePlayerCount(){
		labelPlayers.setText((peers.size())+" players connected");
	}
	
	private void newGameWindow(){
		final NewGameDialog dialog=new NewGameDialog(myPeer);
		dialog.buttonConfirm.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				try {
					final JSONObject game=new JSONObject();
					if(dialog.checkSaved.isChecked()){
						FileHandle saved=Gdx.files.local("assets/maps/game saves/"+dialog.savedBox.getSelected());
						String mapString=saved.readString();
						JSONObject saveObj=new JSONObject(mapString);
						JSONArray players=saveObj.getJSONArray("players");
						ArrayList<Integer> slots=new ArrayList<Integer>();
						for(int i=0;i<players.length();i++){
							JSONObject objPlayer=players.getJSONObject(i);
							slots.add(objPlayer.getInt("order"));
						}
						String password=dialog.textPassword.getText();
						game.put("saved", true);
						game.put("preset", false);
						game.put("map", "");
						game.put("password", password);
						game.put("numPlayers", players.length());
						int vp=saveObj.getInt("victoryPoints");
						game.put("victoryPoints", vp);
						currentGame=new Game(players.length(), myPeer, password, true, "", true, vp);
						currentGame.addPlayer(myPeer);
						currentGame.setMapContent(mapString);
						currentGame.setSlots(slots);
						games.put(myPeer.getId(), currentGame);
						slotsDialog=new GameSlotsDialog(currentGame);
						slotsDialog.addListener(new ClickListener(){
							public void clicked(InputEvent event, float x, float y) {
								if(event.getTarget().isDescendantOf(slotsDialog.getButtonCancel())){
									slotsDialog.hide();
								}
								for(int order:slotsDialog.getButtons().keySet()){
									if(event.getTarget().isDescendantOf(slotsDialog.getButtons().get(order))){
										slotsDialog.hide();
										myPeer.setSlot(order);
										currentGame.getSlots().remove(Integer.valueOf(order));
										JSONArray slots=new JSONArray();
										for(int slot:currentGame.getSlots())
											slots.put(slot);
										try{
											game.put("slots", slots);
											game.put("slot", order);
										}catch(Exception e){e.printStackTrace();}
										socket.emit("game", game);
										dialog.remove();
										displayGames();
										waitForPlayers();
										break;
									}
								}
							}
						});
						slotsDialog.show(Lobby.this);
					}else{
						int numPlayers=Integer.parseInt(dialog.textPlayers.getText());
						if(numPlayers<2 || numPlayers>5){
							new Dialog("Invalid number of players", Skins.METAL).button("ok").show(Lobby.this);
							return;
						}
						int vp=Integer.parseInt(dialog.textVictory.getText());
						if(vp<6 || vp>18){
							new Dialog("VPs must be between 6 and 18", Skins.METAL).button("ok").show(Lobby.this);
							return;
						}
						Tuple<Boolean, String> tuple=(Tuple<Boolean, String>)dialog.presetBox.getSelected();
						game.put("saved", false);
						game.put("fingerprints", new JSONArray());
						game.put("numPlayers", numPlayers);
						game.put("preset", tuple.getFirst());
						game.put("map", tuple.getSecond());
						game.put("password", dialog.textPassword.getText());
						game.put("slots", new JSONArray());
						game.put("slot", 0);
						game.put("victoryPoints", vp);
						currentGame=new Game(numPlayers, myPeer, dialog.textPassword.getText(), tuple.getFirst(), tuple.getSecond(), false, vp);
						currentGame.addPlayer(myPeer);
						games.put(myPeer.getId(), currentGame);
						currentGame.setMapContent(Gdx.files.local("assets/maps/"+(tuple.getFirst()?"presets":"saved")
								+ "/"+tuple.getSecond()).readString());

						socket.emit("game", game);
						dialog.remove();
						displayGames();
						waitForPlayers();
					}
				} catch (NumberFormatException e) {
					new Dialog("Invalid players or victory points", Skins.METAL).button("ok").show(Lobby.this);
				} catch (JSONException e) {e.printStackTrace();}
			}
		});
		dialog.buttonCancel.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				dialog.hide();
			}
		});
		dialog.show(this);
	}
	
	private void waitForPlayers(){
		Dialog dialog=new Dialog("Waiting for other players...", Skins.METAL){
			@Override
			protected void result(Object object) {
				currentGame=null;
				games.remove(myPeer.getId());
				displayGames();
				socket.emit("cancel_game", new JSONObject());
			}
		};
		dialog.button("Cancel");
		dialog.show(this);
	}
}
