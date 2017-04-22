package ui;

import org.json.JSONObject;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;

import comp361.catan.Player;
import comp361.catan.Skins;
import io.socket.client.Socket;
import util.ColoredTexture;

/**
 * Window that contains all the chat-related ui. This class contains functions to display
 * messages and logs.
 */
public class ChatWindow extends Window{
	private ScrollPane messageScroll;
	private Label messageLabel;
	private TextField messageField;
	private TextButton sendButton;
	private Socket socket;
	private TextButton hideButton;
	private Player player;
	private String socialString="";
	private String logString="";
	private boolean socialActive=true;
	private Label labelNewLog;
	private Label labelNewSocial;
	private int socialMessages=0, logMessages=0;
	
	public ChatWindow(Socket socket, Player player) {
		super("Chat", Skins.METAL);
		this.player=player;
		this.socket=socket;
		setupUI();
	}
	private void setupUI(){
		labelNewLog=new Label("0 new log messages", Skins.METAL);
		labelNewLog.setAlignment(Align.right);
		labelNewSocial=new Label("0 new social messages", Skins.METAL);
		labelNewSocial.setAlignment(Align.right);
		final TextButton buttonSocial=new TextButton("Social", Skins.METAL);
		final TextButton buttonLog=new TextButton("Game info", Skins.METAL);
		this.add(buttonSocial).fillX();
		this.row();
		this.add(buttonLog).fillX();
		this.row();
		final TextButtonStyle style1=new TextButtonStyle(buttonSocial.getStyle());
		final TextButtonStyle style2=new TextButtonStyle(buttonSocial.getStyle());
		style2.fontColor=Color.RED;
		buttonSocial.setStyle(style2);
		buttonSocial.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				messageLabel.setText(socialString);
				socialActive=true;
				buttonSocial.setStyle(style2);
				buttonLog.setStyle(style1);
			}
		});
		buttonLog.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				messageLabel.setText(logString);
				socialActive=false;
				buttonSocial.setStyle(style1);
				buttonLog.setStyle(style2);
			}
		});
		messageLabel=new Label("", Skins.METAL);
		messageLabel.setStyle(new LabelStyle(messageLabel.getStyle()));
		messageLabel.getStyle().background=new Image(ColoredTexture.GOLD).getDrawable();
		messageLabel.setWrap(true);
		messageScroll=new ScrollPane(messageLabel);
		messageScroll.setScrollingDisabled(true, false);
		messageScroll.setVelocityY(0.5f);
		sendButton=new TextButton("Send", Skins.METAL);
		messageField=new TextField("", Skins.METAL);
		HorizontalGroup group=new HorizontalGroup();
		group.addActor(messageField);
		group.addActor(sendButton);
		add(messageScroll).fillX().height(150);
		row();
		add(group);
		row();
		sendButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				try{
					if(!messageField.getText().equals("")){
						JSONObject obj=new JSONObject();
						obj.put("action", "message");
						obj.put("message", messageField.getText());
						obj.put("id", player.getPeer().getId());
						socket.emit("catan", obj);
						socialActive=true;
						receivedMessage(messageField.getText(), player);
						messageField.setText("");
					}
				}catch(Exception e){e.printStackTrace();}
			}
		});
		hideButton=new TextButton("Hide", Skins.METAL);
		add(hideButton).fillX();
		hideButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				ChatWindow.this.setVisible(false);
			}
		});
		messageScroll.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if(socialActive)
					updateNewMessages(0, logMessages);
				else
					updateNewMessages(socialMessages, 0);
			}
		});
		pack();
	}
	
	private void updateNewMessages(int social, int log){
		socialMessages=social;
		logMessages=log;
		labelNewLog.setText(log+" new log messages");
		labelNewSocial.setText(social+" new social messages");
	}
	
	public void log(String message){
		logString="[LOG] "+message+"\n\n"+logString;
		if(!socialActive)
			messageLabel.setText(logString);
		logMessages++;
		updateNewMessages(socialMessages, logMessages);
	}
	
	public void receivedMessage(String message, Player other){
		if(other!=player){
			socialMessages++;
			updateNewMessages(socialMessages, logMessages);
		}
		socialString="["+other.getPeer().getName()+"] "+message+"\n\n"+socialString;
		if(socialActive)
			messageLabel.setText(socialString);
	}
	
	public Label getLabelNewLog(){
		return this.labelNewLog;
	}
	
	public Label getLabelNewSocial(){
		return this.labelNewSocial;
	}

}
