package comp361.catan;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

public class Metropolis implements Drawable{
	
	private City city;
	private String discipline;
	
	public Metropolis(String discipline){
		this.discipline=discipline;
	}
	
	public void setCity(City city){
		this.city=city;
	}
	
	public City getCity(){
		return this.city;
	}

	@Override
	public void draw(ShapeRenderer renderer, SpriteBatch batch, BitmapFont font) {
		if(city!=null){
			if(discipline.equals("science"))
				renderer.setColor(Color.GREEN);
			else if(discipline.equals("trade"))
				renderer.setColor(Color.YELLOW);
			else if(discipline.equals("politics"))
				renderer.setColor(Color.BLUE);
			float x=(float)city.getParent().getPosition().getX();
			float y=(float)city.getParent().getPosition().getY();
			renderer.begin(ShapeType.Filled);
			renderer.triangle(x-8, y-3, x, y+7, x+8, y-3);
			renderer.triangle(x-8, y-3, x, y-13, x+8, y-3);
			renderer.end();
			renderer.setColor(Color.BLACK);
			renderer.begin(ShapeType.Line);
			renderer.line(x-8, y-3, x, y+7);
			renderer.line(x-8, y-3, x, y-13);
			renderer.line(x, y+7, x+8, y-3);
			renderer.line(x, y-13, x+8, y-3);
			renderer.end();
		}
	}
	
}
