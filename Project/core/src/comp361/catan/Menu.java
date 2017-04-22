package comp361.catan;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.util.UUID;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

/**
 * The menu class loads the player profiles and provides the interface for
 * entering the lobby and the game editor.
 */
public class Menu extends Stage{
	
	private Table table;
	private Table profileTable;
	private Notification notification;
	private TextField ipField;
	
	
	public Menu(Notification notif){
		this.notification=notif;
		table=new Table();
		table.setFillParent(true);
		profileTable=new Table();
		prepareMainMenu();
	}
	
	private void displayProfiles(){
		final Preferences prefs=Gdx.app.getPreferences("profiles");
		profileTable.clear();
		for(final String username:prefs.get().keySet()){
			final String fingerprint=prefs.getString(username);
			Label userLabel=new Label("user: "+username, Skins.METAL);
			profileTable.add(userLabel).width(200);
			TextButton buttonConnect=new TextButton("Connect", Skins.METAL);
			profileTable.add(buttonConnect);
			buttonConnect.addListener(new ClickListener(){
				@Override
				public void clicked(InputEvent event, float x, float y) {
					Peer peer=new Peer(username, "", fingerprint);
					CatanLauncher.SERVER="http://"+ipField.getText()+":3000";
					notification.lobbyJoined(peer);
				}
			});
			TextButton buttonDelete=new TextButton("Delete", Skins.METAL);
			profileTable.add(buttonDelete);
			buttonDelete.addListener(new ClickListener(){
				@Override
				public void clicked(InputEvent event, float x, float y) {
					prefs.remove(username);
					prefs.flush();
					displayProfiles();
				}
			});
			profileTable.row().padTop(5);
		}
	}

	private void newProfileDialog(){
		final Preferences prefs=Gdx.app.getPreferences("profiles");
		if(prefs.get().size()==3){
			new Dialog("Max 3 profiles", Skins.METAL).text("The maximum number of profiles has"
					+ "been created.").button("ok").show(this);
			return;
		}
		Dialog dialog=new Dialog("New profile", Skins.METAL);
		final TextField textUsername=new TextField("", Skins.METAL);
		textUsername.setMaxLength(12);
		textUsername.setMessageText("username");
		dialog.getContentTable().add(textUsername);
		dialog.button("Cancel");
		TextButton buttonConfirm=new TextButton("Confirm", Skins.METAL);
		buttonConfirm.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				String username=textUsername.getText();
				if(!prefs.contains(username)){
					String fingerprint=UUID.randomUUID().toString();
					prefs.putString(username, fingerprint);
					prefs.flush();
					displayProfiles();
				}else{
					new Dialog("Username exists", Skins.METAL).button("ok").show(Menu.this);
				}
			}
		});
		dialog.button(buttonConfirm).show(this);
	}
	
	private void prepareMainMenu(){
		table.clear();
		String ip="(Not connected to internet)";
		try {
			URL url = new URL("http://checkip.amazonaws.com");
			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
			ip=reader.readLine();
		}catch (Exception e) {}
		Label ipLabel=new Label("My ip: "+ip, Skins.METAL);
		ipField=new TextField("localhost", Skins.METAL);
		ipField.setMessageText("Server IP address");
		TextButton buttonNewProfile=new TextButton("New Profile", Skins.METAL);
		TextButton buttonCreate=new TextButton("Create Map", Skins.METAL);
		table.add(ipLabel);
		table.row().padTop(5);
		table.add(buttonNewProfile).width(100);
		table.row().padTop(5);
		table.add(buttonCreate).width(100);
		table.row().padTop(5);
		table.add(ipField);
		table.row().padTop(5);
		this.addActor(table);

		buttonNewProfile.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				newProfileDialog();
			}
		});
		
		buttonCreate.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				super.clicked(event, x, y);
				notification.editorStarted();
			}
		});

		profileTable.clear();
		table.add(profileTable);
		table.row().padTop(5);
		displayProfiles();
	}
}
