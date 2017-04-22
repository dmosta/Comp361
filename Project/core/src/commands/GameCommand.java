package commands;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;

import org.json.JSONObject;

import actions.GameAction;
import cards.Card;
import comp361.catan.GameStage;
import comp361.catan.Map;
import comp361.catan.Player;
import io.socket.client.Ack;
import io.socket.client.Socket;
import ui.Toolbar;

public abstract class GameCommand implements Serializable{
	
	private static final long serialVersionUID = -4014141065745748518L;
	private transient GameStage gameStage;
	private transient GameAction action;
	private int phase;
	public static final transient Ack ACK=new Ack(){
		public void call(Object... arg0) {
			
		};
	};
	
	public GameCommand(int phase){
		this.phase=phase;
	}

	public static String commandToString(GameCommand command){
		try{
			ByteArrayOutputStream byteOut=new ByteArrayOutputStream();
			ObjectOutputStream dataOut=new ObjectOutputStream(byteOut);
			dataOut.writeObject(command);
			dataOut.close();
			return new String(Base64.getEncoder().encode(byteOut.toByteArray()));
		} catch (Exception e) {e.printStackTrace();}
		return null;
	}
	
	public static GameCommand stringToCommand(String commandString){
		try{
			byte bytes[] = Base64.getDecoder().decode(commandString.getBytes()); 
			ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
			ObjectInputStream dataIn = new ObjectInputStream(byteIn);
			return (GameCommand)dataIn.readObject();
		} catch (Exception e) {e.printStackTrace();}
		return null;
	}
	
	public static JSONObject getJsonCommand(GameCommand command, String action){
		try{
			JSONObject obj=new JSONObject();
			obj.put("action", action);
			obj.put("command", commandToString(command));
			return obj;
		}catch(Exception e){e.printStackTrace();}
		return null;
	}
	
	public void execute(GameStage gameStage, GameAction action){
		this.gameStage=gameStage;
		this.action=action;
		executeCommand();
	};
	
	protected abstract void executeCommand();
	
	public int getPhase(){
		return this.phase;
	}
	
	public void setPhase(int phase){
		this.phase=phase;
	}
	
	public GameStage getStage(){
		return this.gameStage;
	}
	
	public GameAction getAction(){
		return this.action;
	}
	
	public Map getMap(){
		return gameStage.getMap();
	}
	
	public HashMap<String, Player> getPlayers(){
		return gameStage.getPlayers();
	}
	
	public Player getPlayer(){
		return gameStage.getCurrentPlayer();
	}
	
	public ArrayList<Card> getCards(){
		return gameStage.getCards();
	}
	
	public Toolbar getToolbar(){
		return gameStage.getToolbar();
	}
	
	public Socket getSocket(){
		return gameStage.getSocket();
	}
}
