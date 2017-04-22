package ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import comp361.catan.Player;
import comp361.catan.Skins;

/**
 * This class displays information about a specific player. It is part of the toolbar
 * of the game stage.
 */
public class PlayerCell extends Table{
	
	private Label labelName;
	private Label labelVictory;
	private Label labelResources;
	private Label labelPolitics;
	private Label labelTrade;
	private Label labelScience;
	private Label labelCards;
	private Label labelTokens;
	private TextureRegionDrawable colorInactive;
	private TextureRegionDrawable colorActive;
	private Player player;
	private TextButton tradeButton;
	private Label labelBoot;
	private Label labelLongestRoad;
	private Label labelGold;
	
	public PlayerCell(Player player, boolean myPlayer){
		this.player=player;
		labelName=new Label(player.getPeer().getName(), Skins.METAL);
		this.add(labelName).right();
		this.row();
		labelVictory=new Label("0 victory points", Skins.METAL);
		this.add(labelVictory).right();
		this.row();
		Pixmap map=new Pixmap(1,1,Format.RGBA8888);
		map.setColor(player.getColor());
		map.fill();
		colorActive=new TextureRegionDrawable(new TextureRegion(new Texture(map)));
		Pixmap map2=new Pixmap(1,1,Format.RGBA8888);
		Color col=new Color(player.getColor());
		col.set(col.r, col.g, col.b, .55f);
		map2.setColor(col);
		map2.fill();
		colorInactive=new TextureRegionDrawable(new TextureRegion(new Texture(map2)));
		this.setBackground(colorInactive);
		labelResources=new Label("0 resources", Skins.METAL);
		this.add(labelResources);
		this.row();
		labelGold=new Label("0 gold coins", Skins.METAL);
		this.add(labelGold);
		this.row();
		labelPolitics=new Label("Politics level: 1", Skins.METAL);
		this.add(labelPolitics);
		this.row();
		labelTrade=new Label("Trade level: 1", Skins.METAL);
		this.add(labelTrade);
		this.row();
		labelScience=new Label("Science level: 1", Skins.METAL);
		this.add(labelScience);
		this.row();
		labelCards=new Label("0 progress cards", Skins.METAL);
		this.add(labelCards);
		this.row();
		labelTokens=new Label("0 fish tokens", Skins.METAL);
		this.add(labelTokens);
		this.row();
		labelBoot=new Label("", Skins.METAL);
		this.add(labelBoot);
		this.row();
		labelLongestRoad=new Label("", Skins.METAL);
		this.add(labelLongestRoad);
		this.row();
		tradeButton=new TextButton("Trade", Skins.METAL);
		this.add(tradeButton);
		if(myPlayer)
			tradeButton.setVisible(false);
	}
	
	public TextButton getTradeButton(){
		return this.tradeButton;
	}
	
	public Player getPlayer(){
		return this.player;
	}
	
	@Override
	public void act(float delta) {
		super.act(delta);
		this.setBackground(player.isTurn()?colorActive:colorInactive);
		int num=0;
		for(int numRes:player.getResources().values())
			num+=numRes;
		labelResources.setText(num+" resources");
		labelGold.setText(player.getGold()+" gold coins");
		labelPolitics.setText("Politics level: "+player.getPoliticsLevel());
		labelTrade.setText("Trade level: "+player.getTradeLevel());
		labelScience.setText("Science level: "+player.getScienceLevel());
		labelCards.setText(player.getCards().size()+" progress cards");
		labelVictory.setText(player.getVictoryPoints()+" victory points");
		String msgTokens=player.getFishToken().size()-(player.hasBoot()?1:0)+" fish tokens";
		labelTokens.setText(msgTokens);
		labelBoot.setText(player.hasBoot()?"Old boot owner":"");
		labelLongestRoad.setText(player.hasLongestRoad()?"Longest road owner":"");
	}
}
