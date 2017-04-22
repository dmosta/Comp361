package comp361.catan;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

import Math.Vector;
import util.Colors;

public class FishingGround extends TileConstruction{
	
	public Vertex vertex1;
	public Vertex vertex2;
	public Vertex vertex3;
	public ArrayList<Vertex> vertices=new ArrayList<Vertex>(3);
	public ArrayList<Edge> edges=new ArrayList<Edge>(2);
	private int roll;
	float x1, y1, x2, y2, x3, y3;
	float centerX, centerY;

	public FishingGround(int roll){
		this.roll=roll;
	}
	
	public void setLocation(Vertex vertex1, Vertex vertex2, Vertex vertex3, Tile tile){
		setLocation(tile);
		vertices.clear();
		edges.clear();
		this.vertex1=vertex1;
		this.vertex2=vertex2;
		this.vertex3=vertex3;
		vertices.add(vertex1);
		vertices.add(vertex2);
		vertices.add(vertex3);
		for(Edge e:tile.edges){
			int count=0;
			if(e.first==vertex1 || e.first==vertex2 || e.first==vertex3)
				count++;
			if(e.second==vertex1 || e.second==vertex2 || e.second==vertex3)
				count++;
			if(count==2)
				edges.add(e);
		}
		x1=(float)vertex1.getPosition().getX();
		y1=(float)vertex1.getPosition().getY();
		x2=(float)vertex2.getPosition().getX();
		y2=(float)vertex2.getPosition().getY();
		x3=(float)vertex3.getPosition().getX();
		y3=(float)vertex3.getPosition().getY();
		centerX=(float)tile.getX();
		centerY=(float)tile.getY();
	}
	
	@Override
	public void draw(ShapeRenderer renderer, SpriteBatch batch, BitmapFont font) {
		if(getLocation()!=null){
			renderer.begin(ShapeType.Filled);
			renderer.setColor(Colors.LAKE_COLOR);
			renderer.triangle(x1, y1, x2, y2, x3, y3);
			renderer.end();
			renderer.setColor(Color.BLACK);
			renderer.begin(ShapeType.Line);
			renderer.circle(centerX, centerY, 15);
			renderer.end();
			renderer.begin(ShapeType.Filled);
			renderer.setColor(Colors.LAKE_COLOR);
			renderer.circle(centerX, centerY, 15);
			renderer.end();
			
			renderer.setColor(Color.WHITE);
			batch.begin();
			GlyphLayout layout=new GlyphLayout(font, roll+"");
			font.setColor(Color.WHITE);
			font.draw(batch, roll+"", centerX-layout.width/2, centerY+layout.height/2);
			batch.end();
		}
	}
	
	public int getRoll(){
		return this.roll;
	}

}
