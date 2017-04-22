package ui;

import java.util.HashMap;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import comp361.catan.Player;
import comp361.catan.ResourceType;
import comp361.catan.Skins;

public class StealResourceDialog extends Dialog{
	
	private Player target;
	private HashMap<ResourceType, ResourceTable> tables;
	private TextButton buttonDone;
	private int max;
	private int total=0;
	
	public StealResourceDialog(Player target, int max){
		super("Steal up to "+max+" resources/commodities from "+target.getPeer().getName(), Skins.METAL);
		this.max=max;
		this.target=target;
		tables=new HashMap<ResourceType, ResourceTable>();
		for(ResourceType type:target.getResources().keySet()){
			ResourceTable table=new ResourceTable(type);
			getContentTable().add(table);
			getContentTable().row();
			tables.put(type,  table);
		}
		buttonDone=new TextButton("Done", Skins.METAL);
		getContentTable().add(buttonDone);
		this.pack();
	}
	
	public HashMap<ResourceType, Integer> getSelection(){
		HashMap<ResourceType, Integer> selection=new HashMap<ResourceType, Integer>();
		for(ResourceType type:tables.keySet()){
			selection.put(type, tables.get(type).count);
		}
		return selection;
	}
	
	public TextButton getButtonDone(){
		return this.buttonDone;
	}
	
	class ResourceTable extends Table{
		
		public Label owned;
		public Label take;
		public int count=0;
		public ResourceType type;
		
		public  ResourceTable(final ResourceType type){
			this.type=type;
			owned=new Label(type+": Owns "+target.getResources().get(type), Skins.METAL);
			final TextButton buttonAdd=new TextButton("+", Skins.METAL);
			final TextButton buttonRemove=new TextButton("-", Skins.METAL);
			add(owned).width(125);
			add(buttonAdd).width(30);
			add(buttonRemove).width(30);
			take=new Label(" Take 0", Skins.METAL);
			add(take).width(125);
			ClickListener listener=new ClickListener(){
				@Override
				public void clicked(InputEvent event, float x, float y) {
					int current=target.getResources().get(type);
					if(event.getTarget().isDescendantOf(buttonAdd)){
						if(count<current && total<max){
							total++;
							count++;
						}
					}else if(event.getTarget().isDescendantOf(buttonRemove)){
						if(count>0){
							total--;
							count--;
						}
					}
					updateLabel();
				}
			};
			buttonAdd.addListener(listener);
			buttonRemove.addListener(listener);
		}
		
		private void updateLabel(){
			take.setText(" Take "+count);
		}
		
	}

}
