package comp361.catan;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

/**
 * This class represents a settlement on the game board
 */
public class Settlement extends VertexConstruction implements Drawable{
	
	private static final float WIDTH=20;
	private static final float HEIGHT=12;
	private static final float HALF_WIDTH=WIDTH/2;
	private static final float HALF_HEIGHT=HEIGHT/2;
	
	public Settlement(Vertex parent, Player owner){
		super(parent, owner);
	}
	
	@Override
	public void draw(ShapeRenderer renderer, SpriteBatch batch, BitmapFont font) {
		renderer.begin(ShapeType.Filled);
		renderer.setColor(getOwner().getColor());
		renderer.rect((float)getParent().getPosition().getX()-HALF_WIDTH, (float)getParent().getPosition().getY()-HALF_HEIGHT,
				WIDTH, HEIGHT);
		renderer.end();
		renderer.begin(ShapeType.Line);
		renderer.setColor(Color.BLACK);
		renderer.rect((float)getParent().getPosition().getX()-HALF_WIDTH, (float)getParent().getPosition().getY()-HALF_HEIGHT,
				WIDTH, HEIGHT);
		renderer.end();
	}
	
}
