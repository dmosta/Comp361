package ui;

import java.util.ArrayList;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import cards.Card;
import comp361.catan.Player;
import comp361.catan.Skins;
import util.CardUtility;

public class CardWindow extends Window{
	
	private Player player;
	private TextButton buttonNext;
	private Label labelNoCards;
	private Table cardTable;
	private TextButton buttonHide;
	private ArrayList<Card> previousCards=new ArrayList<Card>();
	private Card currentCard;
	private Button buttonPlay;
	int index=0;
	
	public CardWindow(Player plyr){
		super("Progress Cards", Skins.METAL);
		this.player=plyr;
		buttonNext=new TextButton("Next", Skins.METAL);
		labelNoCards=new Label("No cards", Skins.METAL);
		cardTable=new Table();
		cardTable.setTouchable(Touchable.enabled);
		buttonHide=new TextButton("Hide", Skins.METAL);
		buttonPlay=new TextButton("Play", Skins.METAL);
		buttonNext.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if(player.getCards().size()>0)
					index=(index+1)%previousCards.size();
				display();
			}
		});
		buttonHide.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				CardWindow.this.setVisible(false);
			}
		});
		display();
	}
	private void display(){
		boolean hasCards=player.getCards().size()>0;
		
		this.clearChildren();
		this.add(buttonNext);
		this.row();
		if(!hasCards){
			labelNoCards.setText("No cards");
			currentCard=null;
			index=0;
			this.add(labelNoCards);
			this.row();
		}else{
			cardTable.clearChildren();
			currentCard=player.getCards().get(index);
			cardTable.add(CardUtility.getImage(currentCard));
			this.add(cardTable);
			this.row();
			this.add(buttonPlay);
			this.row();
		}
		this.add(buttonHide).fillX();
		this.pack();
	}
	
	@Override
	public void act(float delta) {
		super.act(delta);
		if(!previousCards.equals(player.getCards())){
			previousCards=new ArrayList<Card>(player.getCards());
			if(previousCards.size()>0)
				if(index>=previousCards.size())
					index=previousCards.size()-1;
			display();
		}
	}
	
	public Card getCurrentCard(){
		return this.currentCard;
	}
	
	public Button getButtonPlay(){
		return this.buttonPlay;
	}
	
}
