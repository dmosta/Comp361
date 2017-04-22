package comp361.catan;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

/**
 * This class is the representation of a knight on the game board
 */
public class Knight extends VertexConstruction{
	
	private final static float RADIUS=10;
	private int level=1;
	private boolean promoted=false;
	private boolean active=false;
	private boolean activatedThisTurn=true;

	public Knight(Vertex parent, Player owner) {
		super(parent, owner);
	}

	@Override
	public void draw(ShapeRenderer renderer, SpriteBatch batch, BitmapFont font) {
		renderer.begin(ShapeType.Filled);
		renderer.setColor(getOwner().getColor());
		renderer.circle((float)getParent().getPosition().getX(), (float)getParent().getPosition().getY(), RADIUS);
		renderer.end();
		if(active){
			renderer.begin(ShapeType.Line);	
			renderer.setColor(Color.WHITE);
			renderer.circle((float)getParent().getPosition().getX(), (float)getParent().getPosition().getY(), RADIUS);
			renderer.end();
		}
		batch.begin();
		GlyphLayout layout=new GlyphLayout(font, level+"");
		font.setColor(Color.WHITE);
		if(this.highlight)
			font.setColor(Color.GOLD);
		font.draw(batch, level+"", (float)this.getParent().getPosition().getX()-layout.width/2, 
				(float)this.getParent().getPosition().getY()+layout.height/2);
		batch.end();
	}
	
	public void setLevel(int level){
		this.level=level;
		this.promoted=true;
	}
	
	public int getLevel(){
		return this.level;
	}
	
	public boolean isPromoted(){
		return this.promoted;
	}
	
	public void setPromoted(boolean promoted){
		this.promoted=promoted;
	}
	
	public void setActive(boolean active){
		this.active=active;
	}
	
	public boolean isActive(){
		return this.active;
	}
	
	public void setActivatedThisTurn(boolean activatedThisTurn){
		this.activatedThisTurn=activatedThisTurn;
	}
	
	public boolean wasActivatedThisTurn(){
		return this.activatedThisTurn;
	}

}
