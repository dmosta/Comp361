package actions;

import java.util.ArrayList;
import java.util.HashMap;

import cards.Card;
import comp361.catan.GameStage;
import comp361.catan.Map;
import comp361.catan.Notification;
import comp361.catan.Player;
import io.socket.client.Socket;
import ui.Toolbar;

/**
 * Base class for a game actions.
 */
public abstract class GameAction{
	
	private Notification notification;//Notification to notify when the action is completed
	private GameStage gameStage;
	
	public GameAction(Notification notif, GameStage gameStage){
		this.notification=notif;
		this.gameStage=gameStage;
	}
	
	/**
	 * This method needs to remove all the listeners that the game action has added.
	 * It gets called when the game action gets replaced by another one
	 */
	public abstract void cleanUp();
	
	public GameStage getStage(){
		return this.gameStage;
	}
	
	public Map getMap(){
		return gameStage.getMap();
	}
	
	public Notification getNotification(){
		return notification;
	}
	
	public Player getPlayer(){
		return gameStage.getCurrentPlayer();
	}
	
	public Socket getSocket(){
		return gameStage.getSocket();
	}
	
	public void act(float delta){
		
	}
	
	public HashMap<String, Player> getPlayers(){
		return gameStage.getPlayers();
	}
	
	public Toolbar getToolbar(){
		return gameStage.getToolbar();
	}
	
	public ArrayList<Card> getCards(){
		return gameStage.getCards();
	}
	
}
