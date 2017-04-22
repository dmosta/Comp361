package ui;

import java.util.HashMap;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import comp361.catan.Player;
import comp361.catan.ResourceType;
import comp361.catan.Skins;

public class DiscardDialog extends Dialog{
	
	private int required;
	private int total=0;
	private Player player;
	private HashMap<ResourceType, CustomGroup> resourceGroups=new HashMap<ResourceType, CustomGroup>();
	private HashMap<ResourceType, Integer> selection=new HashMap<ResourceType, Integer>();
	private TextButton buttonDone;
	private Label labelMissing;
	
	public DiscardDialog(int required, Player plyr){
		super("Discard "+required+" resources/commodities.", Skins.METAL);
		this.player=plyr;
		this.required=required;
		labelMissing=new Label("Discard "+required+" more", Skins.METAL);
		getContentTable().add(labelMissing);
		getContentTable().row();
		for(ResourceType type:player.getResources().keySet()){
			selection.put(type, 0);
			if(player.getResources().get(type)>0){
				CustomGroup group=new CustomGroup(type);
				resourceGroups.put(type,  group);
				getContentTable().add(group);
				getContentTable().row();
			}
		}
		buttonDone=new TextButton("Done", Skins.METAL);
		getContentTable().add(buttonDone);
		this.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				for(ResourceType type:resourceGroups.keySet()){
					CustomGroup group=resourceGroups.get(type);
					if(event.getTarget().isDescendantOf(group.getButtonIncrement()))
						group.clickedButton(1);
					else if(event.getTarget().isDescendantOf(group.getButtonDecrement()))
						group.clickedButton(-1);
				}
			}
		});
	}
	
	public int getTotal(){
		return this.total;
	}
	
	public Button getButtonDone(){
		return this.buttonDone;
	}
	
	public HashMap<ResourceType, Integer> getSelection(){
		return this.selection;
	}
	
	class CustomGroup extends Table{
		
		private Label label;
		private TextButton buttonIncrement;
		private TextButton buttonDecrement;
		private int count=0;
		private int previousCount=0;
		private ResourceType type;
		private Label labelRemaining;
		
		public CustomGroup(ResourceType type){
			this.type=type;
			label=new Label(type+": 0", Skins.METAL);
			add(label).width(100);
			buttonIncrement=new TextButton("+", Skins.METAL);
			add(buttonIncrement).width(30);
			buttonDecrement=new TextButton("-", Skins.METAL);
			add(buttonDecrement).width(30);
			labelRemaining=new Label("("+(player.getResources().get(type))+" remaining)", Skins.METAL);
			add(labelRemaining).width(100);
		}
		
		public void clickedButton(int change){
			if(change==1 && player.getResources().get(type)>count && total<required){
				total++;
				count++;
			}else if(change==-1 && count>0){
				total--;
				count--;
			}
			selection.put(type, count);
		}
		
		@Override
		public void act(float delta) {
			super.act(delta);
			if(count!=previousCount){
				previousCount=count;
				label.setText(type+": "+count);
				labelRemaining.setText("("+(player.getResources().get(type)-count)+" remaining)");
				labelMissing.setText("Discard "+(required-total)+" more");
			}
		}
		
		public TextButton getButtonIncrement(){
			return this.buttonIncrement;
		}
		
		public TextButton getButtonDecrement(){
			return this.buttonDecrement;
		}
	}
}
