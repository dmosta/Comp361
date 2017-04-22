package util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

/**
 * Utility class that preloads some textures.
 */
public class ColoredTexture {
	public static Texture GOLD;
	public static Texture SKY_BLUE;
	public static Texture YELLOW;
	public static TextureRegionDrawable SKY_BLUE_DRAWABLE;
	private static boolean initialized=false;
	
	public static void initialize(){
		if(!initialized){
			initialized=true;
			GOLD=getTexture(Color.GOLD);
			SKY_BLUE=getTexture(new Color(105f/255, 156f/255, 214f/255, 1));
			YELLOW=getTexture(Color.YELLOW);
			SKY_BLUE_DRAWABLE=new TextureRegionDrawable(new TextureRegion(SKY_BLUE));
		}
	}
	public static Texture getTexture(Color color){
		Pixmap map=new Pixmap(1,1,Format.RGB565);
		map.setColor(color);
		map.fill();
		return new Texture(map);
	}
}
