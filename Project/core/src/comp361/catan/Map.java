package comp361.catan;

import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONObject;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;

import Math.Vector;

/**
 * The map class is essentially the game board. It stores the tiles, vertices and edges
 * and displays them.
 */
public class Map extends Actor{
	
	private HashMap<Integer, Tile> tiles;
	private HashMap<Integer, Edge> edges;
	private HashMap<Integer, Vertex> vertices;
	private ShapeRenderer renderer;
	private boolean hovered=true;
	private Tile currentTile;
	private Edge currentEdge;
	private Vertex currentVertex;
	private SpriteBatch mapBatch=new SpriteBatch();
	private BitmapFont font=new BitmapFont();
	private Vector min=new Vector(Double.MAX_VALUE, Double.MAX_VALUE);
	private Vector max=new Vector(Double.MIN_VALUE, Double.MIN_VALUE);
	private Vector trans=new Vector();
	private Rectangle bounds;
	private Robber robber=new Robber();
	private Pirate pirate=new Pirate();
	private Merchant merchant=new Merchant();
	private boolean edgeClosest=false;
	private int barbarianPosition=0;
	private Metropolis tradeMetropolis=new Metropolis("trade");
	private Metropolis scienceMetropolis=new Metropolis("science");
	private Metropolis politicsMetropolis=new Metropolis("politics");
	private int defenderVPRemaining=6;
	private boolean firstBarbarianAttack=false;
	private HashMap<Integer, Harbor> harbors=new HashMap<Integer, Harbor>();
	private HashMap<Integer, FishingGround> fishingGrounds=new HashMap<Integer, FishingGround>();
	private ArrayList<FishToken> fishTokens=new ArrayList<FishToken>();
	
	public Map(HashMap<Integer, Tile> tiles, HashMap<Integer, Edge> edges, HashMap<Integer, Vertex> vertices){
		this.tiles=tiles;
		this.edges=edges;
		this.vertices=vertices;
		renderer=new ShapeRenderer();
		bounds=new Rectangle(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		calculateScrollBounds();
	}
	
	private void calculateScrollBounds(){
		for(Tile tile:tiles.values()){
			if(tile.getX()<min.getX())
				min.set(tile.getX(), min.getY());
			if(tile.getY()<min.getY())
				min.set(min.getX(), tile.getY());
			if(tile.getX()>max.getX())
				max.set(tile.getX(), max.getY());
			if(tile.getY()>max.getY())
				max.set(max.getX(), tile.getY());
		}
	}
	
	public Map(){
		this(new HashMap<Integer, Tile>(), new HashMap<Integer, Edge>(), new HashMap<Integer, Vertex>());
	}
	
	public HashMap<Integer, Tile> getTiles(){
		return this.tiles;
	}
	
	public HashMap<Integer, Vertex> getVertices(){
		return this.vertices;
	}
	
	public HashMap<Integer, Edge> getEdges(){
		return this.edges;
	}
	
	public void setTiles(HashMap<Integer, Tile> tiles){
		this.tiles=tiles;
		calculateScrollBounds();
	}
	
	public void setVertices(HashMap<Integer, Vertex> vertices){
		this.vertices=vertices;
	}
	
	public void setEdges(HashMap<Integer, Edge> edges){
		this.edges=edges;
	}
	
	private Vector transformPoint(double x, double y){
		Vector point=new Vector(x-trans.getX(), y-trans.getY());
		return point;
	}
	
	public void setHovered(boolean hovered){
		this.hovered=hovered;
	}
	
	public JSONObject export(){
		return MapParser.export(this, null);
	}
	
	public void scroll(double x, double y){
		if(x>0){
			if(trans.getX()+x+min.getX()>Gdx.graphics.getWidth())
				trans.setX(Gdx.graphics.getWidth()-(float)min.getX());
			else trans.setX(trans.getX()+(float)x);
		}
		if(x<0){
			if(trans.getX()+x+max.getX()<0)
				trans.setX((float)-max.getX());
			else trans.setX(trans.getX()+(float)x);
		}
		if(y>0){
			if(trans.getY()+y+min.getY()>Gdx.graphics.getHeight())
				trans.setY(Gdx.graphics.getHeight()-(float)min.getY());
			else trans.setY(trans.getY()+(float)y);
		}
		if(y<0){
			if(trans.getY()+x+max.getY()<0)
				trans.setY((float)-max.getY());
			else trans.setY(trans.getY()+(float)y);
		}
	}
	
	public Vertex getCurrentVertex(){
		return this.currentVertex;
	}
	
	public Edge getCurrentEdge(){
		return this.currentEdge;
	}
	
	public Tile getCurrentTile(){
		return this.currentTile;
	}
	
	public Edge getEdge(int id){
		return edges.get(id);
	}
	
	public Vertex getVertex(int id){
		return vertices.get(id);
	}
	
	public Tile getTile(int id){
		return tiles.get(id);
	}
	
	@Override
	public void draw(Batch batch, float parentAlpha) {
		batch.end();//Otherwise UI won't draw over
		renderer.identity();
		renderer.translate((float)trans.getX(), (float)trans.getY(), 0);
		mapBatch.getTransformMatrix().idt();
		mapBatch.getTransformMatrix().translate((float)trans.getX(),  (float)trans.getY(),  0);
		currentTile=null;
		currentEdge=null;
		currentVertex=null;
		Vector trans=transformPoint(Gdx.input.getX(), Gdx.graphics.getHeight()-Gdx.input.getY());
		for(Tile tile:tiles.values()){
			if(hovered && tile.intersects(trans)){
				currentTile=tile;
				currentEdge=currentTile.getClosestEdge(trans);
				currentVertex=currentTile.getClosestVertex(trans);
				if(currentVertex!=null && currentEdge!=null)
					edgeClosest=currentEdge.distanceTo(trans)<currentVertex.distanceTo(trans);
			}
			tile.draw(renderer, mapBatch, font);
		}
		for(Harbor harbor:harbors.values())
			harbor.draw(renderer, mapBatch, font);
		for(FishingGround ground:fishingGrounds.values())
			ground.draw(renderer, mapBatch, font);
		for(Edge edge:edges.values()){
			edge.draw(renderer, mapBatch, font);
		}
		for(Vertex vertex:vertices.values()){
			vertex.draw(renderer, mapBatch, font);
		}
		tradeMetropolis.draw(renderer, mapBatch, font);
		scienceMetropolis.draw(renderer, mapBatch, font);
		politicsMetropolis.draw(renderer, mapBatch, font);
		robber.draw(renderer, mapBatch, font);
		pirate.draw(renderer, mapBatch, font);
		merchant.draw(renderer, mapBatch, font);
		batch.begin();
		super.draw(batch, parentAlpha);
	}
	
	public boolean isEdgeClosest(){
		return this.edgeClosest;
	}
	
	public Robber getRobber(){
		return this.robber;
	}
	
	public Pirate getPirate(){
		return this.pirate;
	}
	
	public Merchant getMerchant(){
		return this.merchant;
	}
	
	public void setBarbarianPosition(int barbarianPosition){
		this.barbarianPosition=barbarianPosition;
	}
	
	public int getBarbarianPosition(){
		return this.barbarianPosition;
	}
	
	public Metropolis getTradeMetropolis(){
		return this.tradeMetropolis;
	}
	
	public Metropolis getScienceMetropolis(){
		return this.scienceMetropolis;
	}
	
	public Metropolis getPoliticsMetropolis(){
		return this.politicsMetropolis;
	}
	
	public int getDefenderVPRemaining(){
		return this.defenderVPRemaining;
	}
	
	public void setDefenderVPRemaining(int vp){
		this.defenderVPRemaining=vp;
	}
	
	public void setFirstBarbarianAttack(boolean firstBarbarianAttack){
		this.firstBarbarianAttack=firstBarbarianAttack;
	}
	
	public boolean getFirstBarbarianAttack(){
		return this.firstBarbarianAttack;
	}
	
	public Harbor getHarbor(int id){
		return harbors.get(id);
	}
	
	public HashMap<Integer, Harbor> getHarbors(){
		return this.harbors;
	}
	
	public void setHarbors(HashMap<Integer, Harbor> harbors){
		this.harbors=harbors;
	}
	
	public FishingGround getFishingGround(int id){
		return fishingGrounds.get(id);
	}
	
	public HashMap<Integer, FishingGround> getFishingGrounds(){
		return this.fishingGrounds;
	}
	
	public void setFishingGrounds(HashMap<Integer, FishingGround> fishingGrounds){
		this.fishingGrounds=fishingGrounds;
	}
	
	public ArrayList<FishToken> getFishToken(){
		return this.fishTokens;
	}
	
}
