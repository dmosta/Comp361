package comp361.catan;

import java.util.HashMap;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

import Math.Vector;
import util.Colors;

/**
 * This class is the representation of a harbor on the game board
 *
 */
public class Harbor implements Drawable{
	
	private Tile tile;
	private Edge edge;
	private int rate=3;
	private Vector position;
	private boolean generalHarbor;
	private TileType type;
	private Color color;
	private String strRate;
	
	public Harbor(Tile tile, Edge edge, boolean generalHarbor, TileType type){
		this.tile=tile;
		this.edge=edge;
		this.type=type;
		this.generalHarbor=generalHarbor;
		if(!generalHarbor)
			rate=2;
		position=new Vector(tile.getX(), tile.getY());
		Vector dir=edge.getMid().sub(position);
		dir.selfScale(0.7);
		position.selfAdd(dir);
		color=new Color(1, 36f/255, 36f/255, 1);
		if(!generalHarbor)
			color=Colors.colorForTile(type);
		strRate=(generalHarbor?3:2)+"";
	}
	
	public Tile getTile(){
		return this.tile;
	}
	
	public Edge getEdge(){
		return this.edge;
	}
	
	public int getRate(){
		return this.rate;
	}
	
	public boolean isGeneralHarbor(){
		return generalHarbor;
	}
	
	public TileType getType(){
		return this.type;
	}

	@Override
	public void draw(ShapeRenderer renderer, SpriteBatch batch, BitmapFont font) {
		float x=(float)position.getX();
		float y=(float)position.getY();
		
		renderer.begin(ShapeType.Line);
		renderer.setColor(Color.BLACK);
		renderer.circle(x, y, 10);
		renderer.end();
		renderer.begin(ShapeType.Filled);
		renderer.setColor(color);
		renderer.circle(x, y, 10);
		renderer.end();
		
		batch.begin();
		GlyphLayout layout=new GlyphLayout(font, strRate);
		font.setColor(Color.WHITE);
		font.draw(batch, strRate, x-layout.width/2, y+layout.height/2);
		batch.end();
	}
	
}
