package ui;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;

import comp361.catan.Skins;

/**
 * This window displays tips to the player, like what action he needs to take next.
 */
public class InfoWindow extends Window{
	
	private Label messageLabel;
	
	public InfoWindow(){
		super("Info", Skins.METAL);
		messageLabel=new Label("", Skins.METAL);
		messageLabel.setWrap(true);
		ScrollPane pane=new ScrollPane(messageLabel);
		this.add(pane).width(150).height(100).row();
		TextButton buttonHide=new TextButton("Hide", Skins.METAL);
		buttonHide.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				InfoWindow.this.setVisible(false);
			}
		});
		this.add(buttonHide).fillX();
		this.pack();
	}
	
	public void setInfoMessage(String message){
		messageLabel.setText(message);
	}
	
	public void displayInfoMessage(String message){
		setInfoMessage(message);
		this.setVisible(true);
		this.toFront();
	}
	
	public void clearMessage(){
		setInfoMessage("");
		this.setVisible(false);
	}
}
