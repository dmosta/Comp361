package comp361.catan;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

/**
 * This class represents a robber on the game board.
 */
public class Robber extends TileConstruction implements Drawable{
	
	private Texture texture;
	
	public Robber(){
		texture=new Texture(Gdx.files.internal("constructions/robber1.png"));
	}

	@Override
	public void draw(ShapeRenderer renderer, SpriteBatch batch, BitmapFont font) {
		if(getLocation()!=null){
			float x=(float)getLocation().getX();
			float y=(float)getLocation().getY();
			batch.begin();
			batch.draw(texture, x-32, y-32, 64, 64);
			batch.end();
		}
	}
}
