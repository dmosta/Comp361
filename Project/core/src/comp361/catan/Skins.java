package comp361.catan;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/**
 * This utility class preloads ui skins so that they can be used when creating ui elements
 */
public class Skins {
	public final static Skin METAL=new Skin(Gdx.files.internal("skins/metal/skin/metal-ui.json"));
}
