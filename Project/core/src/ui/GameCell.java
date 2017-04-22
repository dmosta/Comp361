package ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;

import comp361.catan.Skins;
import util.ColoredTexture;

/**
 * This class represents a game row in the lobby.
 * It displays information about the state of a game.
 */
public class GameCell extends Table{
	public Label hostLabel;
	public Label playersLabel;
	public Label mapLabel;
	public Label passwordLabel;
	public TextButton joinButton;
	
	public GameCell(){
		setBackground(ColoredTexture.SKY_BLUE_DRAWABLE);
		hostLabel=new Label("", Skins.METAL);
		hostLabel.setStyle(new LabelStyle(hostLabel.getStyle().font, Color.GOLD));
		add(hostLabel).padLeft(5);
		playersLabel=new Label("", Skins.METAL);
		playersLabel.setStyle(new LabelStyle(playersLabel.getStyle().font, Color.PURPLE));
		add(playersLabel).padLeft(5);
		mapLabel=new Label("", Skins.METAL);
		mapLabel.setStyle(new LabelStyle(mapLabel.getStyle().font, Color.FIREBRICK));
		add(mapLabel).padLeft(5);
		passwordLabel=new Label("", Skins.METAL);
		passwordLabel.setStyle(new LabelStyle(passwordLabel.getStyle().font, Color.BLACK));
		add(passwordLabel).padLeft(5);
		joinButton=new TextButton("Join", Skins.METAL);
		add(joinButton).padLeft(5);
		add(new Label("    ", Skins.METAL)).padLeft(5);
	}
}
