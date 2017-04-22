package comp361.catan;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

public class Pirate extends TileConstruction implements Drawable{
	
	private Texture texture;
	
	public Pirate(){
		texture=new Texture(Gdx.files.internal("constructions/pirate.png"));
	}

	@Override
	public void draw(ShapeRenderer renderer, SpriteBatch batch, BitmapFont font) {
		if(getLocation()!=null){
			float x=(float)getLocation().getX();
			float y=(float)getLocation().getY();
			batch.begin();
			batch.draw(texture, x-texture.getWidth()/2, y-texture.getHeight()/2);
			batch.end();
		}
	}

}
