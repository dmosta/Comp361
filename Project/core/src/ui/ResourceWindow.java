package ui;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import comp361.catan.Player;
import comp361.catan.ResourceType;
import comp361.catan.Skins;

/**
 * This window displays information about the resources owned by a players.
 */
public class ResourceWindow extends Window{
	
	private TextButton buttonHide;
	private Player player;
	private Label resourceLabel;

	public ResourceWindow(Player player){
		super("My Resources", Skins.METAL);
		this.player=player;
		resourceLabel=new Label(getResourcesString(), Skins.METAL);
		resourceLabel.setWrap(true);
		buttonHide=new TextButton("Hide", Skins.METAL);
		buttonHide.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				ResourceWindow.this.setVisible(false);
			}
		});
		this.add(resourceLabel).fillX();
		this.row();
		this.add(buttonHide).fillX();
		this.pack();
	}
	
	private String getResourcesString(){
		String res="";
		res+="BRICK: "+player.getResources().get(ResourceType.BRICK)+"\n";
		res+="CLOTH: "+player.getResources().get(ResourceType.CLOTH)+"\n";
		res+="COIN: "+player.getResources().get(ResourceType.COIN)+"\n";
		res+="GRAIN: "+player.getResources().get(ResourceType.GRAIN)+"\n";
		res+="LUMBER: "+player.getResources().get(ResourceType.LUMBER)+"\n";
		res+="ORE: "+player.getResources().get(ResourceType.ORE)+"\n";
		res+="PAPER: "+player.getResources().get(ResourceType.PAPER)+"\n";
		res+="WOOL: "+player.getResources().get(ResourceType.WOOL);
		return res;
	}
	
	@Override
	public void act(float delta) {
		super.act(delta);
		resourceLabel.setText(getResourcesString());
	}
	
}
