package comp361.catan;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

import Math.Vector;

/**
 * This class represents an edge on the game board
 */
public class Edge implements Drawable{
	
	public Vertex first;
	public Vertex second;
	private Vector mid;
	public int id=0;
	public boolean selected=false;
	public boolean visited=false;
	private EdgeConstruction construction;
	public ArrayList<Tile> neighbors=new ArrayList<Tile>(2);
	public boolean highlight=false;
	
	public Edge(Vertex first, Vertex second){
		this.first=first;
		this.second=second;
		mid=new Vector(first.getPosition());
		mid.selfAdd(second.getPosition());
		mid.selfScale(0.5);
	}
	
	public void setConstruction(EdgeConstruction construction){
		this.construction=construction;
	}
	
	public EdgeConstruction getConstruction(){
		return this.construction;
	}

	@Override
	public void draw(ShapeRenderer renderer, SpriteBatch batch, BitmapFont font) {
		renderer.begin(ShapeType.Filled);
		renderer.setColor(Color.BLACK);
		if(!highlight)
			renderer.rectLine((float)first.getPosition().getX(), (float)first.getPosition().getY(), (float)second.getPosition().getX(), (float)second.getPosition().getY(), selected?3:1);
		else{
			renderer.setColor(Color.WHITE);
			renderer.rectLine((float)first.getPosition().getX(), (float)first.getPosition().getY(), (float)second.getPosition().getX(), (float)second.getPosition().getY(), 3);
		}
		renderer.end();
		if(construction!=null){
			construction.hightlight=highlight;
			construction.draw(renderer, batch, font);
		}
	}
	
	public double distanceTo(Vector location){
		return mid.distanceTo(location);
	}
	
	public Vector getMid(){
		return this.mid;
	}
	
}
