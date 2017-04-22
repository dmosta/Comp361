package ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

import comp361.catan.Game;
import comp361.catan.Skins;

public class GameSlotsDialog extends Dialog{
	
	private Game game;
	private ArrayList<Integer> previousSlots;
	private HashMap<Integer, TextButton> buttons=new HashMap<Integer, TextButton>();
	private TextButton buttonCancel;
	
	public GameSlotsDialog(Game game){
		super("Choose a player", Skins.METAL);
		this.game=game;
		String colors[]=new String[]{"Red", "Magenta", "Orange", "Olive", "Firebrick"};
		for(int i=1;i<=5;i++){
			TextButton button=new TextButton("Player "+i+" ["+colors[i-1]+"]", Skins.METAL);
			buttons.put(i, button);
		}
		buttonCancel=new TextButton("Cancel", Skins.METAL);
		displayUI();
	}
	
	public void displayUI(){
		this.getContentTable().clearChildren();
		Collections.sort(game.getSlots());
		if(game.getSlots().size()==0){
			this.getContentTable().add(new Label("No slots available", Skins.METAL));
			this.getContentTable().row();
		}else{
			getContentTable().add(new Label("Join as:", Skins.METAL));
			getContentTable().row();
		}
		for(int order:game.getSlots()){
			System.out.println("order is "+order);
			getContentTable().add(buttons.get(order));
			getContentTable().row();
		}
		getContentTable().add(buttonCancel);
		pack();
	}
	
	public HashMap<Integer, TextButton> getButtons(){
		return buttons;
	}
	
	public TextButton getButtonCancel(){
		return this.buttonCancel;
	}
	
}
