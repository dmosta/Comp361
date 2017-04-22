package comp361.catan.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import comp361.catan.CatanLauncher;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width=1100;
		config.height=680;
		config.title="The Affiliates Adventure of Catan in MS paint";
		config.resizable=false;
		config.x=0;
		config.y=0;
		new LwjglApplication(new CatanLauncher(), config);
	}
}
