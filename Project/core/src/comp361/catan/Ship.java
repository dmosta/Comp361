package comp361.catan;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

import Math.Vector;

/**
 * This class represents a ship on the game board
 */
public class Ship extends EdgeConstruction{
	
	private float angle;
	private float ellipseX;
	private float ellipseY;
	private static final float WIDTH=8;
	
	public Ship(Edge parent, Player owner) {
		super(parent, owner);
		Vector edgeVec=new Vector(getParent().second.getPosition());
		edgeVec.selfSub(getParent().first.getPosition());
		angle=(float)edgeVec.getAngleDegrees();
		ellipseX=(float)(getParent().getMid().getX()-Tile.SIDE/2);
		ellipseY=(float)(getParent().getMid().getY()-WIDTH/2);
	}

	@Override
	public void draw(ShapeRenderer renderer, SpriteBatch batch, BitmapFont font) {
		renderer.begin(ShapeType.Filled);
		renderer.setColor(getOwner().getColor());
		renderer.ellipse(ellipseX, ellipseY, 
				(float)Tile.SIDE, 8, angle);
		renderer.end();
	}

}
