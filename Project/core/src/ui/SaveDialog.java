package ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import comp361.catan.Game;
import comp361.catan.Skins;

public class SaveDialog extends Dialog{
	
	private TextButton buttonCancel;
	private TextButton buttonSave;
	private TextField textName;
	private Label labelExplanation;
	private Stage stage;
	private Game game;
	
	
	public SaveDialog(Stage stage, Game game){
		super("Save game", Skins.METAL);
		this.stage=stage;
		this.game=game;
		setupUI();
		show(stage);
	}
	
	private void setupUI(){
		labelExplanation=new Label("", Skins.METAL);
		String text="Enter the name under which you wish to save the game. Note that the save file will only contain the state of the game as it was at the beginning of this turn."+
				"Any actions you or other players made since will not be saved.";
		labelExplanation.setText(text);
		labelExplanation.setWrap(true);
		getContentTable().add(labelExplanation).width(200);
		getContentTable().row();
		textName=new TextField("", Skins.METAL);
		textName.setMessageText("Save file name");
		textName.setMaxLength(50);
		getContentTable().add(textName).width(200);
		getContentTable().row();
		HorizontalGroup group=new HorizontalGroup();
		buttonCancel=new TextButton("Cancel", Skins.METAL);
		buttonSave=new TextButton("Save", Skins.METAL);
		group.addActor(buttonCancel);
		group.addActor(buttonSave);
		getContentTable().add(group);
		pack();
		setupEvents();
	}
	
	private void setupEvents(){
		buttonCancel.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				SaveDialog.this.hide();
			}
		});
		buttonSave.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				final String fileName=textName.getText();
				boolean validName=false;
				for(int i=0;i<fileName.length();i++)
					validName|=(fileName.charAt(i)!=' ');
				FileHandle file=Gdx.files.local("assets/maps/game saves/"+fileName);
				if(!validName){
					new Dialog("Invalid file name", Skins.METAL).button("ok").show(stage);
				}else if(file.exists()){
					final Dialog confirmDialog=new Dialog("This save already exists", Skins.METAL);
					confirmDialog.getContentTable().add(new Label("Are you sure you want to overwrite it?", Skins.METAL));
					confirmDialog.getContentTable().row();
					final TextButton btnYes=new TextButton("Yes", Skins.METAL);
					final TextButton btnNo=new TextButton("No", Skins.METAL);
					HorizontalGroup g=new HorizontalGroup();
					g.addActor(btnYes);
					g.addActor(btnNo);
					confirmDialog.getContentTable().add(g);
					confirmDialog.addListener(new ClickListener(){
						public void clicked(InputEvent event, float x, float y) {
							if(event.getTarget().isDescendantOf(btnYes)){
								executeSave(fileName);
								confirmDialog.hide();
								SaveDialog.this.hide();
							}else if(event.getTarget().isDescendantOf(btnNo))
								confirmDialog.hide();
						}
					});
					confirmDialog.show(stage);
				}else{
					executeSave(fileName);
					SaveDialog.this.hide();
				}
			}
		});
	}
	
	private void executeSave(String fileName){
		FileHandle file=Gdx.files.local("assets/maps/game saves/"+fileName);
		file.writeString(game.getMapContent(), false);
		new Dialog("Game has been saved", Skins.METAL).button("ok").show(getStage());
	}
}
