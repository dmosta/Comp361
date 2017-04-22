package comp361.catan;

import java.util.ArrayList;
import java.util.HashMap;

import com.badlogic.gdx.graphics.Color;

import cards.Card;

/**
 * The player class stores the information about each player in the game (resources, victory points, ...).
 * Each player is associated with a peer whose id can be used to communicate across the socket.
 */
public class Player {
	
	private Peer peer;
	private Color color;
	private int order;
	private boolean turn=false;
	private boolean currentPlayer=false;
	private TurnInfo turnInfo=new TurnInfo();
	private HashMap<ResourceType, Integer> resources=new HashMap<ResourceType, Integer>();
	private int politicsLevel=0;
	private int tradeLevel=0;
	private int scienceLevel=0;
	private ArrayList<Card> cards=new ArrayList<Card>();
	public int numWalls=0;
	private int victoryPoints=0;
	private ArrayList<EdgeConstruction> edgeConstructions=new ArrayList<EdgeConstruction>();
	private ArrayList<VertexConstruction> vertexConstructions=new ArrayList<VertexConstruction>();
	private int citiesRemaining=4, settlementsRemaining=5, basicKnightsRemainging=2, strongKnightsRemainging=2, mightyKnightsRemainging=2;
	private ArrayList<FishToken> fishTokens=new ArrayList<FishToken>();
	private boolean hasBoot=false;
	private boolean longestRoad=false;
	private int roadLength=0;
	private int gold=2;
	
	public Player(Peer peer, Color color, int order){
		this.peer=peer;
		this.color=color;
		this.order=order;
		resources.put(ResourceType.ORE, 0);
		resources.put(ResourceType.GRAIN, 0);
		resources.put(ResourceType.LUMBER, 0);
		resources.put(ResourceType.BRICK, 0);
		resources.put(ResourceType.WOOL, 0);
		resources.put(ResourceType.CLOTH, 0);
		resources.put(ResourceType.COIN, 0);
		resources.put(ResourceType.PAPER, 0);
	}
	
	public int getOrder(){
		return this.order;
	}
	
	public void setPeer(Peer peer){
		this.peer=peer;
	}
	
	public Peer getPeer(){
		return this.peer;
	}
	
	public Color getColor(){
		return this.color;
	}
	
	public boolean isTurn(){
		return turn;
	}
	
	public void setTurn(boolean turn){
		this.turn=turn;
	}
	
	public void setCurrentPlayer(boolean currentPlayer){
		this.currentPlayer=currentPlayer;
	}
	
	public boolean isCurrentPlayer(){
		return this.currentPlayer;
	}
	
	public TurnInfo getTurnInfo(){
		return this.turnInfo;
	}
	
	public HashMap<ResourceType, Integer> getResources(){
		return this.resources;
	}
	
	public void setOrder(int order){
		this.order=order;
	}
	
	public int getPoliticsLevel(){
		return this.politicsLevel;
	}
	
	public int getTradeLevel(){
		return this.tradeLevel;
	}
	
	public int getScienceLevel(){
		return this.scienceLevel;
	}
	
	public void setTradeLevel(int tradeLevel){
		this.tradeLevel=tradeLevel;
	}
	
	public void setPoliticsLevel(int politicsLevel){
		this.politicsLevel=politicsLevel;
	}
	
	public void setScienceLevel(int scienceLevel){
		this.scienceLevel=scienceLevel;
	}
	
	public ArrayList<Card> getCards(){
		return this.cards;
	}
	
	public int getNumWalls(){
		return this.numWalls;
	}
	
	public void setNumWalls(int numWalls){
		this.numWalls=numWalls;
	}
	
	public int getVictoryPoints(){
		return this.victoryPoints;
	}
	
	public void setVictoryPoints(int victoryPoints){
		this.victoryPoints=victoryPoints;
	}
	
	public ArrayList<VertexConstruction> getVertexContructions(){
		return this.vertexConstructions;
	}
	
	public ArrayList<EdgeConstruction> getEdgeConstructions(){
		return this.edgeConstructions;
	}
	
	public int getCitiesRemaining(){
		return this.citiesRemaining;
	}
	
	public int getSettlementsRemaining(){
		return this.settlementsRemaining;
	}
	
	public int getBasicKnightsRemaining(){
		return this.basicKnightsRemainging;
	}
	
	public int getStrongKnightsRemaining(){
		return this.strongKnightsRemainging;
	}
	
	public int getMightyKnightsRemaining(){
		return this.mightyKnightsRemainging;
	}
	
	public void setCitiesRemaining(int citiesRemaining){
		this.citiesRemaining=citiesRemaining;
	}
	
	public void setSettlementsRemaining(int settlementsRemaining){
		this.settlementsRemaining=settlementsRemaining;
	}
	
	public void setBasicKnightsRemaining(int basicKnightsRemaining){
		this.basicKnightsRemainging=basicKnightsRemaining;
	}
	
	public void setStrongKnightsRemaining(int strongKnightsRemaining){
		this.strongKnightsRemainging=strongKnightsRemaining;
	}
	
	public void setMightyKnightsRemaining(int mightyKnightsRemaining){
		this.mightyKnightsRemainging=mightyKnightsRemaining;
	}
	
	public ArrayList<FishToken> getFishToken(){
		return this.fishTokens;
	}
	
	public void setHasBoot(boolean hasBoot){
		this.hasBoot=hasBoot;
	}
	
	public boolean hasBoot(){
		return this.hasBoot;
	}
	
	public boolean hasLongestRoad(){
		return this.longestRoad;
	}
	
	public void setLongestRoad(boolean longestRoad){
		this.longestRoad=longestRoad;
	}
	
	public void setRoadLength(int roadLength){
		this.roadLength=roadLength;
	}
	
	public int getRoadLength(){
		return this.roadLength;
	}
	
	public int getGold(){
		return this.gold;
	}
	
	public void setGold(int gold){
		this.gold=gold;
	}
}
