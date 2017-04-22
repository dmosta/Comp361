package comp361.catan;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;

import Math.Vector;
import util.Colors;

/**
 * This class represents a tile(hex) on the gameboard.
 */
public class Tile implements Drawable{
	
	private double x;
	private double y;
	public boolean selected=false;
	public boolean highlight=false;
	private int number;
	public Vertex vLeft, vTopLeft, vTopRight, vRight, vBotRight, vBotLeft;
	public Edge eTopLeft, eTop, eTopRight, eBotRight, eBot, eBotLeft;
	public ArrayList<Tile> neighbors=new ArrayList<Tile>(6);
	public ArrayList<Edge> edges=new ArrayList<Edge>(6);
	public ArrayList<Vertex> vertices=new ArrayList<Vertex>(6);
	public static final double SIDE=50;
	public static final double APOTHEM=SIDE/(2*Math.tan(Math.toRadians(180/6)));
	public static final double RADIUS=SIDE/(2*Math.sin(Math.toRadians(180/6)));
	private TileType tileType=TileType.OCEAN;
	private Rectangle bounds;
	public int id=0;
	private Harbor harbor;
	private Color tileColor;
	
	public Tile(double x, double y, int number){
		this.x=x;
		this.y=y;
		this.number=number;
		bounds=new Rectangle((float)(x-RADIUS), (float)(y-RADIUS),
				2*(float)RADIUS, 2*(float)RADIUS);
		vLeft=new Vertex(new Vector(x-RADIUS, y));
		vTopLeft=new Vertex(new Vector(x-SIDE/2, y+APOTHEM));
		vTopRight=new Vertex(new Vector(x+SIDE/2, y+APOTHEM));
		vRight=new Vertex(new Vector(x+RADIUS, y));
		vBotRight=new Vertex(new Vector(x+SIDE/2, y-APOTHEM));
		vBotLeft=new Vertex(new Vector(x-SIDE/2, y-APOTHEM));
		eTopLeft=new Edge(vLeft, vTopLeft);
		eTop=new Edge(vTopLeft, vTopRight);
		eTopRight=new Edge(vTopRight, vRight);
		eBotRight=new Edge(vRight, vBotRight);
		eBot=new Edge(vBotRight, vBotLeft);
		eBotLeft=new Edge(vBotLeft, vLeft);
		setColor();
	}
	
	private void setColor(){
		tileColor=Colors.colorForTile(tileType);
	}
	
	public void setHarbor(Harbor harbor){
		this.harbor=harbor;
	}
	
	public Harbor getHarbor(){
		return this.harbor;
	}
	
	public Rectangle getBounds(){
		return this.bounds;
	}
	
	public void setTileType(TileType tileType){
		this.tileType=tileType;
		setColor();
	}
	
	public TileType getTileType(){
		return this.tileType;
	}
	
	public void setTileNumber(int number){
		this.number=number;
	}
	
	public int getTileNumber(){
		return this.number;
	}
	public Vertex getClosestVertex(Vector position){
		Vertex closest=vTopRight;
		double distance=vTopRight.distanceTo(position);
		double otherDistance=vRight.distanceTo(position);
		if(otherDistance<distance){
			distance=otherDistance;
			closest=vRight;
		}
		otherDistance=vBotRight.distanceTo(position);
		if(otherDistance<distance){
			distance=otherDistance;
			closest=vBotRight;
		}
		otherDistance=vBotLeft.distanceTo(position);
		if(otherDistance<distance){
			distance=otherDistance;
			closest=vBotLeft;
		}
		otherDistance=vLeft.distanceTo(position);
		if(otherDistance<distance){
			distance=otherDistance;
			closest=vLeft;
		}
		otherDistance=vTopLeft.distanceTo(position);
		if(otherDistance<distance){
			distance=otherDistance;
			closest=vTopLeft;
		}
		return closest;
	}
	
	public Edge getClosestEdge(Vector position){
		Edge closest=eTop;
		double distance=position.distanceTo(closest.getMid());
		double otherDistance=position.distanceTo(eTopRight.getMid());
		if(otherDistance<distance){
			distance=otherDistance;
			closest=eTopRight;
		}
		otherDistance=position.distanceTo(eBotRight.getMid());
		if(otherDistance<distance){
			distance=otherDistance;
			closest=eBotRight;
		}
		otherDistance=position.distanceTo(eBot.getMid());
		if(otherDistance<distance){
			distance=otherDistance;
			closest=eBot;
		}
		otherDistance=position.distanceTo(eBotLeft.getMid());
		if(otherDistance<distance){
			distance=otherDistance;
			closest=eBotLeft;
		}
		otherDistance=position.distanceTo(eTopLeft.getMid());
		if(otherDistance<distance){
			distance=otherDistance;
			closest=eTopLeft;
		}
		return closest;
	}
	
	public boolean intersects(Vector location){
		
		double range=2*RADIUS;
		double d1=eTopLeft.distanceTo(location);
		double d2=eTop.distanceTo(location);
		double d3=eTopRight.distanceTo(location);
		double d4=eBotRight.distanceTo(location);
		double d5=eBot.distanceTo(location);
		double d6=eBotLeft.distanceTo(location);
		
		return d1>=0 && d2>=0 && d3>=0 && d4>=0 && d5>=0 && d6>=0
				&& d1<=range && d2<=range&& d3<=range && d4<=range && d5<=range && d6<=range;
	}
	
	public double getX(){
		return this.x;
	}
	
	public double getY(){
		return this.y;
	}
	
	public Color getTileColor(){
		return this.tileColor;
	}

	@Override
	public void draw(ShapeRenderer renderer, SpriteBatch batch, BitmapFont font) {
		Gdx.graphics.getGL20().glEnable(GL20.GL_BLEND);
		renderer.begin(ShapeType.Filled);
		tileColor.a=highlight?0.6f:1;
		renderer.setColor(tileColor);
		if(selected)
			renderer.setColor(Color.RED);
		renderer.triangle((float)vLeft.getPosition().getX(), (float)vLeft.getPosition().getY(), (float)vTopLeft.getPosition().getX(), 
				(float)vTopLeft.getPosition().getY(), (float)vBotLeft.getPosition().getX(), (float)vBotLeft.getPosition().getY());
		renderer.triangle((float)vRight.getPosition().getX(), (float)vRight.getPosition().getY(), (float)vTopRight.getPosition().getX(), 
				(float)vTopRight.getPosition().getY(), (float)vBotRight.getPosition().getX(), (float)vBotRight.getPosition().getY());
		renderer.rect((float)vBotLeft.getPosition().getX(), (float)vBotLeft.getPosition().getY(), (float)SIDE, 2*(float)APOTHEM);
		renderer.end();
		Gdx.graphics.getGL20().glDisable(GL20.GL_BLEND);
		if(tileType!=TileType.OCEAN){
			batch.begin();
			font.setColor(tileType==TileType.LAKE?Color.WHITE:Color.BLACK);
			if(tileType==TileType.LAKE){
				GlyphLayout layout=new GlyphLayout(font, "2, 3, 11, 12");
				font.draw(batch, "2, 3, 11, 12", (float)getX()-layout.width/2, (float)getY()+layout.height/2);
			}else{
				GlyphLayout layout=new GlyphLayout(font, number+"");
				font.draw(batch, number+"", (float)getX()-layout.width/2, (float)getY()+layout.height/2);
			}
			batch.end();
		}
	}
}
