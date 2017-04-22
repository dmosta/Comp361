package ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import comp361.catan.Player;
import comp361.catan.Skins;

public class ChartWindow extends Window{
	
	private TextButton buttonNext;
	private TextButton buttonPrev;
	private Label disciplineLabel;
	private String discipline="politics";
	private Label labelLevel;
	private Label labelNextLevel;
	private Label labelDice;
	private TextButton buttonUpgrade;
	private ArrayList<String> disciplines=new ArrayList<String>(Arrays.asList(new String[]{"politics", "trade", "science"}));
	private TextButton buttonHide;
	private Player player;
	
	public ChartWindow(Player player){
		super("Development flip-chart", Skins.METAL);
		this.player=player;
		buttonNext=new TextButton("Next", Skins.METAL);
		buttonPrev=new TextButton("Prev", Skins.METAL);
		disciplineLabel=new Label("Politics", Skins.METAL);
		disciplineLabel.setAlignment(Align.center);
		buttonHide=new TextButton("Hide", Skins.METAL);
		labelLevel=new Label("", Skins.METAL);
		labelLevel.setAlignment(Align.center);
		labelDice=new Label("", Skins.METAL);
		labelDice.setAlignment(Align.center);
		labelNextLevel=new Label("", Skins.METAL);
		labelNextLevel.setAlignment(Align.center);
		buttonUpgrade=new TextButton("Upgrade", Skins.METAL);
		Table group=new Table();
		group.add(buttonPrev);
		group.add(disciplineLabel).width(100);
		group.add(buttonNext);
		this.add(group);
		this.row();
		this.add(labelLevel);
		this.row();
		this.add(labelDice);
		this.row();
		this.add(labelNextLevel);
		this.row();
		this.add(buttonUpgrade);
		this.row();
		this.add(buttonHide).fillX();
		this.pack();
		buttonPrev.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				updateDiscipline(-1);
			}
		});
		buttonNext.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				updateDiscipline(1);
			}
		});
		buttonHide.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				ChartWindow.this.setVisible(false);
			}
		});
		refresh();
	}
	
	private void updateDiscipline(int direction){
		int index=disciplines.indexOf(discipline);
		index+=direction;
		if(index==3)
			index=0;
		else if(index==-1)
			index=2;
		discipline=disciplines.get(index);
		String name=(discipline.charAt(0)+"").toUpperCase()+discipline.substring(1);
		disciplineLabel.setText(name);
		int level=0;
		HashMap<String, String[]> titles=new HashMap<String, String[]>();
		titles.put("politics", new String[]{"","Town Hall", "Church", "Fortress", "Cathedral", "High Assembly"});
		titles.put("trade", new String[]{"","Market", "Trading House", "Merchant Guild", "Bank", "Great Exchange"});
		titles.put("science", new String[]{"","Abbey", "Library", "Aqueduct", "Theater", "University"});
		if(discipline.equals("politics"))
			level=player.getPoliticsLevel();
		else if(discipline.equals("trade"))
			level=player.getTradeLevel();
		else if(discipline.equals("science"))
			level=player.getScienceLevel();
		String title="No city improvements yet.";
		if(level>0)
			title="Level "+level+" ("+titles.get(discipline)[level]+")";
		String diceRolls="Red die rolls: ";
		for(int i=1;i<=level;i++){
			diceRolls+=i;
			if(i!=level)
				diceRolls+=", ";
		}
		String nextLevel="Max level reached.";
		if(level<5){
			String commodity="Coin";
			if(discipline.equals("science"))
				commodity="Paper";
			else if(discipline.equals("trade"))
				commodity="Cloth";
			nextLevel="Next upgrade: "+titles.get(discipline)[level+1]+" ("+(level+1)+" "+commodity+")";
		}
		labelLevel.setText(title);
		labelDice.setText(diceRolls);
		labelNextLevel.setText(nextLevel);
	}
	
	public TextButton getButtonUpgrade(){
		return this.buttonUpgrade;
	}
	
	public void refresh(){
		this.updateDiscipline(0);
	}
	
	public String getDiscipline(){
		return this.discipline;
	}
}
