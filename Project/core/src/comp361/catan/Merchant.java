package comp361.catan;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

public class Merchant extends TileConstruction{
	
	private final static float OFFSET=15;
	private Player owner;
	
	public void setOwner(Player owner){
		this.owner=owner;
	}
	
	public Player getOwner(){
		return this.owner;
	}

	@Override
	public void draw(ShapeRenderer renderer, SpriteBatch batch, BitmapFont font) {
		if(getLocation()!=null){
			renderer.setColor(owner.getColor());
			renderer.begin(ShapeType.Filled);
			float x=(float)getLocation().getX();
			float y=(float)getLocation().getY();
			renderer.rectLine(x-OFFSET, y-OFFSET, x, y+2*OFFSET, 2);
			renderer.rectLine(x-OFFSET, y-OFFSET, x+OFFSET, y-OFFSET, 2);
			renderer.rectLine(x, y+2*OFFSET, x+OFFSET, y-OFFSET, 2);
			renderer.end();
		}
	}

}
