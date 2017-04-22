package ui;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import comp361.catan.Game;
import comp361.catan.Lobby;
import comp361.catan.Peer;
import comp361.catan.Skins;
import util.Tuple;
import util.TupleSelectBox;

/**
 * Dialog for creating new games.
 */
public class NewGameDialog extends Dialog{
	
	public TextField textPlayers;
	public TextField textPassword;
	public TupleSelectBox<String, Tuple<Boolean, String>> presetBox;
	public TextButton buttonConfirm;
	public TextButton buttonCancel;
	public CheckBox checkSaved;
	public SelectBox<String> savedBox;
	public TextButton buttonDelete;
	public ArrayList<String> savedList;
	public TextField textVictory;
	
	
	public NewGameDialog(Peer myPeer) {
		super("New Game", Skins.METAL);
		checkSaved=new CheckBox("Saved game", Skins.METAL);
		getContentTable().add(checkSaved);
		getContentTable().row();
		Table table=new Table();
		savedBox=new SelectBox<String>(Skins.METAL);
		table.add(savedBox);
		buttonDelete=new TextButton("x", Skins.METAL);
		buttonDelete.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				deleteSave();
			}
		});
		table.add(buttonDelete).padLeft(5).width(25).height(25);
		getContentTable().add(table);
		getContentTable().row();
		FileHandle savedGames=Gdx.files.local("assets/maps/game saves");
		savedList=new ArrayList<String>();
		for(FileHandle file:savedGames.list())
			savedList.add(file.name());
		
		updateSaveBox();
		textPlayers=new TextField("", Skins.METAL);
		textPlayers.setMessageText("Number of players (2-5)");
		getContentTable().add(textPlayers);
		getContentTable().row();
		textPassword=new TextField("", Skins.METAL);
		textPassword.setPasswordMode(true);
		textPassword.setMessageText("password");
		getContentTable().add(textPassword);
		getContentTable().row();
		textVictory=new TextField("", Skins.METAL);
		textVictory.setMessageText("Victory points (6-18)");
		getContentTable().add(textVictory);
		getContentTable().row();
		presetBox=new TupleSelectBox<String, Tuple<Boolean, String>>(Skins.METAL);
		ArrayList<String> items=new ArrayList<String>();
		ArrayList<Tuple<Boolean, String>> values=new ArrayList<Tuple<Boolean, String>>();
		FileHandle saved=Gdx.files.local("assets/maps/saved");
		FileHandle presets=Gdx.files.local("assets/maps/presets");
		for(FileHandle file:presets.list()){
			items.add("Preset: "+file.name());
			Tuple<Boolean, String> tuple=new Tuple<Boolean, String>(true, file.name());
			values.add(tuple);
		}
		for(FileHandle file:saved.list()){
			items.add("Custom: "+file.name());
			Tuple<Boolean, String> tuple=new Tuple<Boolean, String>(false, file.name());
			values.add(tuple);
		}
		presetBox.setItems(items.toArray());
		presetBox.setValues(values);
		getContentTable().add(presetBox).row();
		HorizontalGroup group=new HorizontalGroup();
		buttonConfirm=new TextButton("Confirm", Skins.METAL);
		buttonCancel=new TextButton("Cancel", Skins.METAL);
		group.addActor(buttonConfirm);
		group.addActor(buttonCancel);
		getContentTable().add(group);
		pack();
	}
	
	private void deleteSave(){
		final TextButton btnYes=new TextButton("Yes", Skins.METAL);
		final TextButton btnNo=new TextButton("No", Skins.METAL);
		final Dialog confirmDialog=new Dialog("Delete save file?", Skins.METAL);
		confirmDialog.getContentTable().add(btnYes);
		confirmDialog.getContentTable().add(btnNo);
		confirmDialog.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if(event.getTarget().isDescendantOf(btnYes)){
					String fileName=savedBox.getSelected();
					savedList.remove(fileName);
					Gdx.files.local("assets/maps/game saves/"+fileName).delete();
					confirmDialog.hide();
					updateSaveBox();
				}else if(event.getTarget().isDescendantOf(btnNo)){
					confirmDialog.hide();
				}
			}
		});
		confirmDialog.show(getStage());
	}
	
	private void updateSaveBox(){
		savedBox.setItems(savedList.toArray(new String[0]));
		if(savedList.size()==0){
			checkSaved.setTouchable(Touchable.disabled);
			savedBox.setItems(new String[]{"No saves"});
			savedBox.setTouchable(Touchable.disabled);
			buttonDelete.setTouchable(Touchable.disabled);
		}
	}

}
