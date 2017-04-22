package comp361.catan;

import java.util.ArrayList;

/**
 * The game class stores the game rules and the peers of an active game. It is used in the
 * lobby.
 */
public class Game {
	private Peer host;
	private Peer[] players;
	private int connected=0;
	private String password;
	private boolean preset;
	private String mapName;
	private String mapContent;
	private boolean saved;
	private boolean visible=true;
	private ArrayList<Integer> slots=new ArrayList<Integer>();
	private int victoryPoints;
	
	public Game(int numPlayers, Peer host, String password, boolean preset, String mapName, boolean saved, int victoryPoints){
		players=new Peer[numPlayers];
		this.victoryPoints=victoryPoints;
		this.host=host;
		this.password=password;
		this.preset=preset;
		this.mapName=mapName;
		this.saved=saved;
	}
	
	public void setMapContent(String mapContent){
		this.mapContent=mapContent;
	}
	
	public String getMapContent(){
		return this.mapContent;
	}
	
	public int getNumPlayers(){
		return players.length;
	}
	
	public int getConnectedPlayers(){
		return connected;
	}
	
	public Peer getHost(){
		return this.host;
	}
	
	public String getPassword(){
		return this.password;
	}
	
	public boolean isPreset(){
		return this.preset;
	}
	
	public Peer[] getPlayers(){
		return players;
	}
	
	public String getMapName(){
		return this.mapName;
	}
	
	public boolean isSaved(){
		return this.saved;
	}
	
	public void addPlayer(Peer player){
		for(int i=0;i<players.length;i++){
			if(players[i]==null){
				players[i]=player;
				connected++;
				break;
			}
		}
	}
	
	public void removePlayer(Peer player){
		for(int i=0;i<players.length;i++){
			if(players[i]==player){
				players[i]=null;
				connected--;
				break;
			}
		}
	}
	
	public boolean isVisible(){
		return this.visible;
	}
	
	public void setVisible(boolean visible){
		this.visible=visible;
	}
	
	public void setSaved(boolean saved){
		this.saved=saved;
	}
	
	public void setSlots(ArrayList<Integer> slots){
		this.slots=slots;
	}
	
	public ArrayList<Integer> getSlots(){
		return this.slots;
	}
	
	public int getVictoryPoints(){
		return this.victoryPoints;
	}
	
	public void setVictoryPoints(int victoryPoints){
		this.victoryPoints=victoryPoints;
	}
}
