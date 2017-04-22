package comp361.catan;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

/**
 * This class represents a city on the game board
 */
public class City extends VertexConstruction{
	
	private float x1, y1, x2, y2, x3, y3;
	private final static float HALF_WIDTH=15;
	private boolean hasWall=false;

	public City(Vertex parent, Player owner) {
		super(parent, owner);
		float x=(float)parent.getPosition().getX();
		float y=(float)parent.getPosition().getY();
		x1=x-HALF_WIDTH;
		y1=y-HALF_WIDTH;
		x2=x;
		y2=y+HALF_WIDTH;
		x3=x+HALF_WIDTH;
		y3=y-HALF_WIDTH;
	}
	
	public void setWall(boolean hasWall){
		this.hasWall=hasWall;
	}
	
	public boolean hasWall(){
		return this.hasWall;
	}

	@Override
	public void draw(ShapeRenderer renderer, SpriteBatch batch, BitmapFont font) {
		renderer.begin(ShapeType.Filled);
		renderer.setColor(getOwner().getColor());
		if(hasWall)
			renderer.rect(x1, y1, x3-x1, y2-y1);
		renderer.triangle(x1, y1, x2, y2, x3, y3);
		renderer.end();
		renderer.begin(ShapeType.Line);
		renderer.setColor(Color.BLACK);
		renderer.triangle(x1, y1, x2, y2, x3, y3);
		renderer.end();
	}

}
