package comp361.catan;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

import Math.Vector;

/**
 * Vertex represents a vertex (intersection) on the game board.
 */
public class Vertex implements Drawable{
	
	private Vector position;
	public ArrayList<Edge> edges=new ArrayList<Edge>(3);
	public ArrayList<Tile> neighbors=new ArrayList<Tile>(3);
	public int id=0;
	public boolean selected=false;
	public boolean visited=false;
	public int pred;
	private VertexConstruction construction;
	public boolean highlight=false;
	
	public Vertex(Vector position){
		this.position=position;
	}
	
	public Vector getPosition(){
		return this.position;
	}
	
	public double distanceTo(Vector vec){
		return position.distanceTo(vec);
	}

	public void setConstruction(VertexConstruction construction){
		this.construction=construction;
	}
	
	public VertexConstruction getConstruction(){
		return this.construction;
	}
	
	@Override
	public void draw(ShapeRenderer renderer, SpriteBatch batch, BitmapFont font) {
		if(selected){
			renderer.setColor(Color.BLACK);
			renderer.begin(ShapeType.Filled);
			renderer.circle((float)position.getX(), (float)position.getY(), 5);
			renderer.end();
		}
		if(construction!=null){
			construction.highlight=highlight;
			construction.draw(renderer, batch, font);
		}
	}
	
}
