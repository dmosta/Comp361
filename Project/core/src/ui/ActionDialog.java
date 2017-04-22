package ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import comp361.catan.Edge;
import comp361.catan.Skins;
import comp361.catan.Vertex;

public class ActionDialog extends Dialog{
	
	private TextButton buttonSettlement;
	private TextButton buttonCity;
	private TextButton buttonRoad;
	private TextButton buttonShip;
	private TextButton buttonKnight;
	private TextButton buttonWall;
	private TextButton buttonUpgradeKnight;
	private TextButton buttonMoveKnight;
	private TextButton buttonActivateKnight;
	private TextButton buttonDisplaceKnight;
	private Vertex vertex;
	private Edge edge;
	
	public ActionDialog(){
		super("Actions", Skins.METAL);
		buttonSettlement=new TextButton("Settlement", Skins.METAL);
		buttonCity=new TextButton("City", Skins.METAL);
		buttonRoad=new TextButton("Road", Skins.METAL);
		buttonShip=new TextButton("Ship", Skins.METAL);
		buttonKnight=new TextButton("Knight", Skins.METAL);
		buttonWall=new TextButton("Wall", Skins.METAL);
		buttonUpgradeKnight=new TextButton("Upgrade", Skins.METAL);
		buttonMoveKnight=new TextButton("Move Knight", Skins.METAL);
		buttonActivateKnight=new TextButton("Activate Knight", Skins.METAL);
		buttonDisplaceKnight=new TextButton("Displace Knight", Skins.METAL);
		this.pack();
		this.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Rectangle bounds=new Rectangle(ActionDialog.this.getX(), ActionDialog.this.getY(),
						ActionDialog.this.getWidth(), ActionDialog.this.getHeight());
				if(!bounds.contains(Gdx.input.getX(), Gdx.graphics.getHeight()-Gdx.input.getY()))
					ActionDialog.this.hide();
			}
		});
	}
	@Override
	public Dialog show(Stage stage) {
		this.getContentTable().clear();
		if(buttonSettlement.isVisible()){
			this.getContentTable().add(buttonSettlement);
			this.getContentTable().row();
		}
		if(buttonCity.isVisible()){
			this.getContentTable().add(buttonCity);
			this.getContentTable().row();
		}
		if(buttonRoad.isVisible()){
			this.getContentTable().add(buttonRoad);
			this.getContentTable().row();
		}
		if(buttonShip.isVisible()){
			this.getContentTable().add(buttonShip);
			this.getContentTable().row();
		}
		if(buttonKnight.isVisible()){
			this.getContentTable().add(buttonKnight);
			this.getContentTable().row();
		}
		if(buttonWall.isVisible()){
			this.getContentTable().add(buttonWall);
			this.getContentTable().row();
		}
		if(buttonUpgradeKnight.isVisible()){
			this.getContentTable().add(buttonUpgradeKnight);
			this.getContentTable().row();
		}
		if(buttonMoveKnight.isVisible()){
			this.getContentTable().add(buttonMoveKnight);
			this.getContentTable().row();
		}
		if(buttonActivateKnight.isVisible()){
			this.getContentTable().add(buttonActivateKnight);
			this.getContentTable().row();
		}
		if(buttonDisplaceKnight.isVisible()){
			this.getContentTable().add(buttonDisplaceKnight);
			this.getContentTable().row();
		}
		this.pack();
		return super.show(stage);
	}
	
	public TextButton getButtonSettlement(){
		return this.buttonSettlement;
	}
	
	public TextButton getButtonCity(){
		return this.buttonCity;
	}
	
	public TextButton getButtonRoad(){
		return this.buttonRoad;
	}
	
	public TextButton getButtonShip(){
		return this.buttonShip;
	}
	
	public TextButton getButtonKnight(){
		return this.buttonKnight;
	}
	
	public TextButton getButtonWall(){
		return this.buttonWall;
	}
	
	public TextButton getButtonUpgradeKnight(){
		return this.buttonUpgradeKnight;
	}
	
	public TextButton getButtonMoveKnight(){
		return this.buttonMoveKnight;
	}
	
	public TextButton getButtonActivateKnight(){
		return this.buttonActivateKnight;
	}
	
	public TextButton getButtonDisplaceKnight(){
		return this.buttonDisplaceKnight;
	}
	
	public void setVertex(Vertex vertex){
		this.vertex=vertex;
	}
	
	public void setEdge(Edge edge){
		this.edge=edge;
	}
	
	public Vertex getVertex(){
		return this.vertex;
	}
	
	public Edge getEdge(){
		return this.edge;
	}
}
