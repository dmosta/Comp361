package comp361.catan;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * This interface is needed for game board elements that get drawn (like settlements and roads)
 */
public interface Drawable {
	public void draw(ShapeRenderer renderer, SpriteBatch batch, BitmapFont font);
}
