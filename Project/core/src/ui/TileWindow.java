package ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import comp361.catan.Edge;
import comp361.catan.FishingGround;
import comp361.catan.Harbor;
import comp361.catan.Map;
import comp361.catan.Skins;
import comp361.catan.Tile;
import comp361.catan.TileType;
import comp361.catan.Vertex;

public class TileWindow extends Window{
	
	private Tile tile;
	private SelectBox<TileType> boxType;
	private SelectBox<Integer> boxNumber;
	private SelectBox<String> boxHarbor;
	private SelectBox<TileType> harborType;
	private SelectBox<String> boxFishing;
	private TextButton buttonSave;
	private TextButton buttonQuit;
	private TextButton buttonRandom;
	private TextField textSave;
	private Map map;
	private Stage stage;
	private boolean canPlaceHarbor=false, canPlaceFishingGround=false;
	private boolean setup=false;
	private ArrayList<FishingGround> fishingGrounds=new ArrayList<FishingGround>();
	
	public TileWindow(Map map, Stage stage){
		super("Tile settings", Skins.METAL);
		
		this.map=map;
		this.stage=stage;
		createFishingGrounds();
		boxType=new SelectBox<TileType>(Skins.METAL);
		boxType.setItems(TileType.values());
		boxNumber=new SelectBox<Integer>(Skins.METAL);
		boxNumber.setItems(new Integer[]{2,3,4,5,6,8,9,10,11,12});
		boxHarbor=new SelectBox<String>(Skins.METAL);
		boxHarbor.setItems(new String[]{"No harbor", "Generic harbor", "Special harbor"});
		boxFishing=new SelectBox<String>(Skins.METAL);
		boxFishing.setItems(new String[]{"No fishing ground", "Fishing ground"});
		buttonRandom=new TextButton("Randomize", Skins.METAL);
		buttonSave=new TextButton("Save", Skins.METAL);
		buttonQuit=new TextButton("Quit", Skins.METAL);
		textSave=new TextField("", Skins.METAL);
		textSave.setMessageText("Save file name");
		
		harborType=new SelectBox<TileType>(Skins.METAL);
		ArrayList<TileType> validTypes=new ArrayList<TileType>(Arrays.asList(TileType.values()));
		validTypes.remove(TileType.LAKE);
		harborType.setItems(validTypes.toArray(new TileType[validTypes.size()]));
		
		createUI();
		addListeners();
	}
	
	private void createUI(){
		this.clearChildren();
		if(tile!=null){
			HorizontalGroup group1=new HorizontalGroup();
			group1.addActor(new Label("Tile type: ", Skins.METAL));
			group1.addActor(boxType);
			add(group1);
			row();
			
			if(tile.getTileType()!=TileType.LAKE){
				HorizontalGroup group2=new HorizontalGroup();
				group2.addActor(new Label("Tile number: ", Skins.METAL));
				group2.addActor(boxNumber);
				add(group2);
				row();
			}
			
			if(canPlaceHarbor){
				HorizontalGroup group3=new HorizontalGroup();
				group3.addActor(new Label("Harbor: ", Skins.METAL));
				group3.addActor(boxHarbor);
				add(group3);
				row();
			}
			
			if(canPlaceFishingGround){
				HorizontalGroup group4=new HorizontalGroup();
				group4.addActor(new Label("Fishing ground: ", Skins.METAL));
				group4.addActor(boxFishing);
				add(group4);
				row();
			}
		}else{
			add(new Label("No tile selected", Skins.METAL));
			row();
		}
		
		add(buttonRandom);
		row();
		
		add(textSave);
		row();
		
		HorizontalGroup group=new HorizontalGroup();
		group.addActor(buttonSave);
		group.addActor(buttonQuit);
		add(group);
		row();
		pack();
	}
	
	public void createFishingGrounds(){
		fishingGrounds.clear();
		for(int i=4;i<=10;i++){
			boolean exists=false;
			for(FishingGround ground:map.getFishingGrounds().values()){
				if(ground.getRoll()==i){
					exists=true;
					break;
				}
			}
			if(!exists)
				fishingGrounds.add(new FishingGround(i));
		}
	}
	
	private boolean canPlaceHarbor(Tile location){
		if(location!=null && location.getTileType()==TileType.OCEAN){
			for(Edge e:location.edges){
				for(Tile other:e.neighbors)
					if(other!=location && other.getTileType()!=TileType.OCEAN)
						return true;
			}
		}
		return false;
	}
	
	private boolean canPlaceFishingGround(Tile location){
		if(location!=null && location.getTileType()==TileType.OCEAN){
			ArrayList<Vertex> vertices=new ArrayList<Vertex>();
			int edgeCount=0;
			for(Edge e:location.edges){
				for(Tile neighbor:e.neighbors){
					if(neighbor!=location && neighbor.getTileType()!=TileType.OCEAN){
						edgeCount++;
						if(!vertices.contains(e.first))
							vertices.add(e.first);
						if(!vertices.contains(e.second))
							vertices.add(e.second);
					}
				}
			}
			if(edgeCount==2 && vertices.size()==3)
				return true;
		}
		return false;
	}
	
	private void addListeners(){
		boxType.addListener(new ChangeListener(){
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if(tile!=null && !setup)
					chooseTileType();
			}
		});
		boxNumber.addListener(new ChangeListener(){
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if(tile!=null && !setup)
					chooseTileNumber();
			}
		});
		boxHarbor.addListener(new ChangeListener(){
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if(tile!=null && !setup)
					chooseHarbor();
			}
		});
		boxFishing.addListener(new ChangeListener(){
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if(tile!=null && !setup)
					chooseFishingGround();
			}
		});
	}
	
	private void chooseFishingGround(){
		String choice=boxFishing.getSelected();
		if(choice.equals("No fishing ground")){
			for(int key:map.getFishingGrounds().keySet()){
				FishingGround ground=map.getFishingGround(key);
				if(ground.getLocation()==tile){
					map.getFishingGrounds().remove(key);
					fishingGrounds.add(ground);
					break;
				}
			}
		}else if(fishingGrounds.size()==0)
				new Dialog("", Skins.METAL).text("All fishings ground have been placed. You need to remove one in order to place it on this tile.").button("ok").show(stage);
		else{
			final Dialog fishingDialog=new Dialog("Choose fishing ground production number", Skins.METAL);
			final HashMap<FishingGround, TextButton> buttons=new HashMap<FishingGround, TextButton>();
			for(FishingGround ground:fishingGrounds){
				TextButton button=new TextButton("Fishing ground "+ground.getRoll(), Skins.METAL);
				buttons.put(ground, button);
				fishingDialog.getContentTable().add(button);
				fishingDialog.getContentTable().row();
			}
			fishingDialog.addListener(new ClickListener(){
				@Override
				public void clicked(InputEvent event, float x, float y) {
					for(FishingGround ground:buttons.keySet()){
						if(event.getTarget().isDescendantOf(buttons.get(ground))){
							fishingDialog.hide();
							fishingGrounds.remove(ground);
							ArrayList<Vertex> vertices=new ArrayList<Vertex>();
							for(Edge e:tile.edges)
								for(Tile t:e.neighbors)
									if(t!=tile && t.getTileType()!=TileType.OCEAN){
										if(!vertices.contains(e.first))
											vertices.add(e.first);
										if(!vertices.contains(e.second))
											vertices.add(e.second);
									}
							ground.setLocation(vertices.get(0), vertices.get(1), vertices.get(2), tile);
							map.getFishingGrounds().put(tile.id, ground);
							if(tile.getHarbor()!=null){
								tile.setHarbor(null);
								map.getHarbors().remove(tile.id);
							}
							setTile(tile);
							break;
						}
					}
				}
			});
			fishingDialog.show(stage);
		}
	}
	
	private void chooseTileType(){
		TileType previousType=tile.getTileType();
		TileType type=boxType.getSelected();
		tile.setTileType(type);
		if(previousType==TileType.OCEAN){
			if(tile.getHarbor()!=null){
				map.getHarbors().remove(tile.id);
				tile.setHarbor(null);
			}
			for(int key:map.getFishingGrounds().keySet()){
				FishingGround ground=map.getFishingGround(key);
				if(ground.getLocation()==tile){
					System.out.println("It has happened");
					ground.setLocation(null);
					map.getFishingGrounds().remove(key);
					fishingGrounds.add(ground);
					break;
				}
			}
		}else if(type==TileType.OCEAN){
			for(Tile other:tile.neighbors){
				if(other.getHarbor()!=null && !canPlaceHarbor(other)){
					other.setHarbor(null);
					map.getHarbors().remove(other.id);
				}
				for(int key:map.getFishingGrounds().keySet()){
					FishingGround ground=map.getFishingGround(key);
					if(ground.getLocation()==other && !canPlaceFishingGround(other)){
						ground.setLocation(null);
						map.getFishingGrounds().remove(key);
						fishingGrounds.add(ground);
						break;
					}
				}
			}
		}else if(previousType==TileType.LAKE){
			tile.setTileNumber(2);
		}
		setTile(tile);
	}
	
	private void chooseTileNumber(){
		int number=boxNumber.getSelected();
		tile.setTileNumber(number);
	}
	
	private void chooseHarbor(){
		String selection=boxHarbor.getSelected();
		if(selection.equals("No harbor")){
			if(tile.getHarbor()!=null){
				map.getHarbors().remove(tile.id);
				tile.setHarbor(null);
			}
		}else{
			final boolean generic=selection.equals("Generic harbor");
			final Dialog edgeDialog=new Dialog("Choose an edge", Skins.METAL);
			if(!generic){
				edgeDialog.getContentTable().add(harborType);
				edgeDialog.getContentTable().row();
			}
			final HashMap<Edge, TextButton> edgeButtons=new HashMap<Edge, TextButton>();
			int i=0;
			for(Edge e:tile.edges){
				for(Tile other:e.neighbors){
					if(other!=tile && other.getTileType()!=TileType.OCEAN){
						TextButton button=new TextButton("Edge "+(++i), Skins.METAL);
						edgeButtons.put(e, button);
						edgeDialog.getContentTable().add(button);
						edgeDialog.getContentTable().row();
					}
				}
			}
			edgeDialog.addListener(new ClickListener(){
				public void clicked(InputEvent event, float x, float y) {
					for(Edge e:edgeButtons.keySet()){
						if(event.getTarget().isDescendantOf(edgeButtons.get(e))){
							Harbor harbor=new Harbor(tile, e, generic, generic?TileType.FIELD:harborType.getSelected());
							tile.setHarbor(harbor);
							map.getHarbors().put(tile.id, harbor);
							edgeDialog.hide();
							for(FishingGround ground:map.getFishingGrounds().values()){
								if(ground.getLocation()==tile){
									ground.setLocation(null);
									map.getFishingGrounds().remove(tile.id);
									fishingGrounds.add(ground);
									break;
								}
							}
							setTile(tile);
							break;
						}
					}
				};
			});
			edgeDialog.show(stage);
		}
	}
	
	public TextField getTextSave(){
		return this.textSave;
	}
	
	public TextButton getButtonSave(){
		return this.buttonSave;
	}
	
	public TextButton getButtonQuit(){
		return this.buttonQuit;
	}
	
	public TextButton getButtonRandom(){
		return this.buttonRandom;
	}
	
	public void setTile(Tile tile){
		setup=true;
		this.tile=tile;
		canPlaceHarbor=canPlaceHarbor(tile);
		canPlaceFishingGround=canPlaceFishingGround(tile);
		createUI();
		if(tile!=null){
			boxType.setSelected(tile.getTileType());
			boxNumber.setSelected(tile.getTileNumber());
			if(tile.getHarbor()==null)
				boxHarbor.setSelected("No harbor");
			else if(tile.getHarbor().isGeneralHarbor())
				boxHarbor.setSelected("Generic harbor");
			else if(!tile.getHarbor().isGeneralHarbor())
				boxHarbor.setSelected("Special harbor");
			boolean hasFishingGround=false;
			for(FishingGround ground:map.getFishingGrounds().values()){
				if(ground.getLocation()==tile){
					hasFishingGround=true;
					break;
				}
			}
			boxFishing.setSelected(hasFishingGround?"Fishing ground":"No fishing ground");
		}
		setup=false;
	}
	
	public ArrayList<FishingGround> getFishingGrounds(){
		return this.fishingGrounds;
	}
	
}
