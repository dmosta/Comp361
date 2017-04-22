package ui;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextTooltip;
import com.badlogic.gdx.scenes.scene2d.ui.Tooltip;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;

import comp361.catan.GameStage;
import comp361.catan.Player;
import comp361.catan.Skins;

/**
 * Toolbar is an agglomerate of various UI elements and windows that are used to visually
 * represent the state of the game and provide interactivity.
 */
public class Toolbar extends Table{
	
	private ImageButton chatButton;
	private ImageButton turnButton;
	private ImageButton cardButton;
	private ImageButton chartButton;
	private ImageButton resourceButton;
	private ImageButton infoButton;
	private ImageButton tradeButton;
	private TextButton cancelButton;
	private ImageButton fishButton;
	private ImageButton quitButton;
	private ImageButton muteButton;
	private ImageButton saveButton;
	private ImageButton cheatButton;
	private Table playersTable;
	private GameStage stage;
	private CardWindow cardWindow;
	private ChatWindow chatWindow;
	private InfoWindow infoWindow;
	private ResourceWindow resourceWindow;
	private TradeWindow tradeWindow;
	private ChartWindow chartWindow;
	private Drawable rollImage, turnImage, muteImage, unmuteImage;
	private ArrayList<PlayerCell> playerCells=new ArrayList<PlayerCell>();
	private ActionDialog actionDialog;
	private Label labelCurrentAction;
	private Label labelBarbarianProgress;
	private ImageButton helpButton;
	private Label labelVictory;
	
	public Toolbar(GameStage stage_){
		this.stage=stage_;
		this.setFillParent(true);
		actionDialog=new ActionDialog();
		playersTable=new Table();
		this.add(playersTable).left();
		
		chatWindow=new ChatWindow(stage.getSocket(), stage.getCurrentPlayer());
		stage.addActor(chatWindow);
		chatWindow.setVisible(false);
		this.row().expand().right().bottom();
		Table bottomTable=new Table();
		bottomTable.add(chatWindow.getLabelNewLog()).fillX();
		bottomTable.row();
		bottomTable.add(chatWindow.getLabelNewSocial()).fillX();
		bottomTable.row();
		Table rowTable=new Table();
		Pixmap pm1 = new Pixmap(1, 1, Format.RGB565);
		pm1.setColor(Color.WHITE);
		pm1.fill();
		rowTable.setBackground(new TextureRegionDrawable(new TextureRegion(new Texture(pm1))));
		labelCurrentAction=new Label("", Skins.METAL);
		cancelButton=new TextButton("Cancel", Skins.METAL);
		cancelButton.setVisible(false);
		VerticalGroup vertGroup=new VerticalGroup();
		vertGroup.addActor(labelCurrentAction);
		vertGroup.addActor(cancelButton);

		labelBarbarianProgress=new Label("BP: 0/7", Skins.METAL);
		rowTable.add(vertGroup);
		rowTable.add(labelBarbarianProgress);
		labelVictory=new Label(stage.getGame().getVictoryPoints()+" VP  ", Skins.METAL);
		LabelStyle style=new LabelStyle(labelVictory.getStyle());
		style.fontColor=Color.RED;
		labelVictory.setStyle(style);
		rowTable.add(labelVictory).padLeft(8);
		infoWindow=new InfoWindow();
		stage.addActor(infoWindow);
		infoWindow.setVisible(false);
		quitButton=new ImageButton(new Image(new Texture(Gdx.files.internal("ui/quit.png"))).getDrawable());
		rowTable.add(quitButton).width(40).height(40);
		saveButton=new ImageButton(new Image(new Texture(Gdx.files.internal("ui/save.png"))).getDrawable());
		rowTable.add(saveButton).width(40).height(40);
		muteImage=new Image(new Texture(Gdx.files.internal("ui/mute.png"))).getDrawable();
		unmuteImage=new Image(new Texture(Gdx.files.internal("ui/unmute.png"))).getDrawable();
		muteButton=new ImageButton(stage.getMusic().getVolume()==0?unmuteImage:muteImage);
		rowTable.add(muteButton).width(40).height(40);
		rollImage=new Image(new Texture(Gdx.files.internal("ui/dice.png"))).getDrawable();
		turnImage=new Image(new Texture(Gdx.files.internal("ui/turn.png"))).getDrawable();
		turnButton=new ImageButton(rollImage);
		rowTable.add(turnButton).width(40).height(40);
		resourceButton=new ImageButton(new Image(new Texture(Gdx.files.internal("ui/ressources.png"))).getDrawable());
		rowTable.add(resourceButton).width(40).height(40);
		chartButton=new ImageButton(new Image(new Texture(Gdx.files.internal("ui/chart.png"))).getDrawable());
		rowTable.add(chartButton).width(40).height(40);
		cardButton=new ImageButton(new Image(new Texture(Gdx.files.internal("ui/card.png"))).getDrawable());
		rowTable.add(cardButton).width(40).height(40);
		tradeButton=new ImageButton(new Image(new Texture(Gdx.files.internal("ui/trade.png"))).getDrawable());
		rowTable.add(tradeButton).width(40).height(40);
		fishButton=new ImageButton(new Image(new Texture(Gdx.files.internal("ui/fish.png"))).getDrawable());
		rowTable.add(fishButton).width(40).height(40);
		cheatButton=new ImageButton(new Image(new Texture(Gdx.files.internal("ui/cheat.png"))).getDrawable());
		rowTable.add(cheatButton).width(40).height(40);
		infoButton=new ImageButton(new Image(new Texture(Gdx.files.internal("ui/info.png"))).getDrawable());
		rowTable.add(infoButton).width(40).height(40);
		helpButton=new ImageButton(new Image(new Texture(Gdx.files.internal("ui/help.png"))).getDrawable());
		rowTable.add(helpButton).width(40).height(40);
		chatButton=new ImageButton(new Image(new Texture(Gdx.files.internal("ui/chat.png"))).getDrawable());
		rowTable.add(chatButton).width(40).height(40);
		bottomTable.add(rowTable);
		this.add(bottomTable);
		
		resourceWindow=new ResourceWindow(stage.getCurrentPlayer());
		this.addActor(resourceWindow);
		resourceWindow.setVisible(false);
		tradeWindow=new TradeWindow(stage.getCurrentPlayer(), stage.getMap());
		this.addActor(tradeWindow);
		tradeWindow.setVisible(false);
		chartWindow=new ChartWindow(stage.getCurrentPlayer());
		chartWindow.setVisible(false);
		this.addActor(chartWindow);
		cardWindow=new CardWindow(stage.getCurrentPlayer());
		cardWindow.setVisible(false);
		this.addActor(cardWindow);
		muteButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if(stage.getMusic().getVolume()==0){
					stage.getMusic().setVolume(1);
					muteButton.getStyle().imageUp=muteImage;
				}else{
					stage.getMusic().setVolume(0);
					muteButton.getStyle().imageUp=unmuteImage;
				}
			}
		});
		quitButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				stage.disconnect();
			}
		});
		resourceButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				resourceWindow.setVisible(!resourceWindow.isVisible());
				resourceWindow.toFront();
			}
		});
		infoButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				infoWindow.setVisible(!infoWindow.isVisible());
				infoWindow.toFront();
			}
		});
		chatButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				chatWindow.setVisible(!chatWindow.isVisible());
				chatWindow.toFront();
			}
		});
		chartButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				chartWindow.setVisible(!chartWindow.isVisible());
				chartWindow.toFront();
			}
		});
		cardButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				cardWindow.setVisible(!cardWindow.isVisible());
				cardWindow.toFront();
			}
		});
		helpButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if(Desktop.isDesktopSupported()){
				    try {
				    	File helpFile=new File("help.pdf");
				        Desktop.getDesktop().open(helpFile);
				    }catch(Exception e){e.printStackTrace();}
				}else
			    	new Dialog("", Skins.METAL).text("Unable to open help.pdf, please open it manually from the Project folder").button("ok").show(stage);
			}
		});
		saveButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				//if(stage.getTurnStarted())
					new SaveDialog(stage, stage.getGame());
				//else{
				//	new Dialog("Cannot save", Skins.METAL).text("You can only save after the initial setup phase.").button("ok").show(stage);
				//}
			}
		});
	}
	
	public ChatWindow getChatWindow(){
		return this.chatWindow;
	}
	
	public void addPlayer(Player player){
		PlayerCell cell=new PlayerCell(player, player==stage.getCurrentPlayer());
		playerCells.add(cell);
		playersTable.add(cell).left().padLeft(2);
	}
	
	public ArrayList<PlayerCell> getPlayerCells(){
		return this.playerCells;
	}
	
	public ImageButton getChatButton(){
		return this.chatButton;
	}
	
	public ImageButton getTurnButton(){
		return this.turnButton;
	}
	
	public InfoWindow getInfoWindow(){
		return this.infoWindow;
	}
	
	public ResourceWindow getResourceWindow(){
		return this.resourceWindow;
	}
	
	public TradeWindow getTradeWindow(){
		return this.tradeWindow;
	}

	public void toggleTurnButtonImage(boolean isRoll){
		turnButton.getStyle().imageUp=isRoll?rollImage:turnImage;
	}
	
	public ImageButton getTradeButton(){
		return this.tradeButton;
	}
	
	public ActionDialog getActionDialog(){
		return this.actionDialog;
	}
	
	public ImageButton getCardButton(){
		return this.cardButton;
	}
	
	public ChartWindow getChartWindow(){
		return this.chartWindow;
	}
	
	public CardWindow getCardWindow(){
		return this.cardWindow;
	}
	
	public void activateCancelButton(boolean active, String message){
		labelCurrentAction.setText(active?message:"");
		cancelButton.setVisible(active);
	}
	
	public Button getCancelButton(){
		return this.cancelButton;
	}
	
	int previousBarbarian=0;
	@Override
	public void act(float delta) {
		super.act(delta);
		if(previousBarbarian!=stage.getMap().getBarbarianPosition()){
			previousBarbarian=stage.getMap().getBarbarianPosition();
			labelBarbarianProgress.setText("BP: "+previousBarbarian+"/7");
		}
	}
	
	public ImageButton getFishButton(){
		return this.fishButton;
	}
	
	public ImageButton getCheatButton(){
		return this.cheatButton;
	}
}
