package ui;

import java.util.ArrayList;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import comp361.catan.FishToken;
import comp361.catan.Player;
import comp361.catan.Skins;

public class TokenDialog extends Dialog{
	
	private int required;
	private Player player;
	private int total=0;
	private Label labelTotal;
	private TextButton buttonAccept;
	private TextButton buttonCancel;
	private TokenCell cell1, cell2, cell3;

	public TokenDialog(Player player, int required) {
		super("Choose "+required+" tokens", Skins.METAL);
		this.required=required;
		this.player=player;
		setupUI();
	}
	
	private void setupUI(){
		labelTotal=new Label("Total value of selected tokens: "+total, Skins.METAL);
		getContentTable().add(labelTotal);
		getContentTable().row();
		int tot1=0, tot2=0, tot3=0;
		for(FishToken token:player.getFishToken()){
			if(token.getValue()==1)
				tot1++;
			else if(token.getValue()==2)
				tot2++;
			else if(token.getValue()==3)
				tot3++;
		}
		cell1=new TokenCell(1, tot1);
		if(tot1>0){
			getContentTable().add(cell1);
			getContentTable().row();
		}
		cell2=new TokenCell(2, tot2);
		if(tot2>0){
			getContentTable().add(cell2);
			getContentTable().row();
		}
		cell3=new TokenCell(3, tot3);
		if(tot3>0){
			getContentTable().add(cell3);
			getContentTable().row();
		}
		HorizontalGroup group=new HorizontalGroup();
		buttonAccept=new TextButton("Accept", Skins.METAL);
		group.addActor(buttonAccept);
		buttonCancel=new TextButton("Cancel", Skins.METAL);
		group.addActor(buttonCancel);
		getContentTable().add(group);
		getContentTable().row();
		this.pack();
	}
	
	public ArrayList<Integer> getSelectedTokens(){
		ArrayList<Integer> selected=new ArrayList<Integer>();
		int current1=cell1.getCurrent();
		int current2=cell2.getCurrent();
		int current3=cell3.getCurrent();
		for(int i=0;i<player.getFishToken().size();i++){
			FishToken token=player.getFishToken().get(i);
			if(current1>0 && token.getValue()==1){
				current1--;
				selected.add(i);
			}else if(current2>0 && token.getValue()==2){
				current2--;
				selected.add(i);
			}else if(current3>0 && token.getValue()==3){
				current3--;
				selected.add(i);
			}
		}
		return selected;
	}
	
	public int getTotal(){
		return this.total;
	}
	
	public Button getButtonAccept(){
		return this.buttonAccept;
	}
	
	public Button getButtonCancel(){
		return this.buttonCancel;
	}
	
	private void updateUI(){
		labelTotal.setText("Total value of selected tokens: "+total);
	}
	
	
	class TokenCell extends Table{
		
		int value;
		int owned;
		int current=0;
		TextButton buttonAdd;
		TextButton buttonSubstract;
		Label labelGive;
		
		public TokenCell(int value, int owned){
			this.value=value;
			this.owned=owned;
			buttonAdd=new TextButton("+", Skins.METAL);
			buttonSubstract=new TextButton("-", Skins.METAL);
			Label label=new Label("Value "+value+" tokens ("+owned+" owned)", Skins.METAL);
			add(label).width(200);
			labelGive=new Label("Give 0 ", Skins.METAL);
			add(labelGive).width(60);
			add(buttonAdd).width(30).height(30);
			add(buttonSubstract).width(30).height(30);
			buttonAdd.addListener(new ClickListener(){
				@Override
				public void clicked(InputEvent event, float x, float y) {
					change(1);
				}
			});
			buttonSubstract.addListener(new ClickListener(){
				@Override
				public void clicked(InputEvent event, float x, float y) {
					change(-1);
				}
			});
		}
		
		public int getCurrent(){
			return this.current;
		}
		
		private void change(int sign){
			if(sign==1 && current<owned){
				current++;
				total+=value;
			}
			else if(sign==-1 && current>0){
				current--;
				total-=value;
			}
			updateTable();
			updateUI();
		}
		
		private void updateTable(){
			labelGive.setText("Give "+current+" ");
		}
	}

}
