package comp361.catan;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

/**
 * This class represents a road on the game board
 */
public class Road extends EdgeConstruction{

	private static final float WIDTH=4;
	
	public Road(Edge parent, Player owner){
		super(parent, owner);
	}
	
	@Override
	public void draw(ShapeRenderer renderer, SpriteBatch batch, BitmapFont font) {
		renderer.begin(ShapeType.Filled);
		renderer.setColor(getOwner().getColor());
		renderer.rectLine((float)(getParent().first.getPosition().getX()), (float)(getParent().first.getPosition().getY()),
				(float)(getParent().second.getPosition().getX()), (float)(getParent().second.getPosition().getY()), WIDTH);
		renderer.end();
	}

}
