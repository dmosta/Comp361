package comp361.catan;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import org.json.JSONObject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.Align;

import Math.Vector;
import ui.TileWindow;

/**
 * The editor stage handles the generation and randomization of maps,
 * as well as saving and retrieving maps from the disk
 */
public class Editor extends Stage{

	private SpriteBatch batch;
	private ShapeRenderer renderer;
	private Map map;
	private boolean initialized=false;
	private static final float MAX_SCALE=2;
	private static final float MIN_SCALE=0.5f;
	private static final float SCALE_INC=.1f;
	private static final double EPSILON=1e-6;
	private float scale=1;
	private TileWindow tileWindow;
	private Notification notification;
	private int hoverNum=0;
	private int levels;
	private Tile previousTile=null, selectedTile=null;
	
	public Editor(Notification notification){
		this.notification=notification;
		batch=new SpriteBatch();
		renderer=new ShapeRenderer();
		newMap();
		this.addListener(new InputListener(){
			@Override
			public boolean scrolled(InputEvent event, float x, float y, int amount) {
				if(initialized){
					scale+=amount*SCALE_INC;
					if(scale>MAX_SCALE)
						scale=MAX_SCALE;
					if(scale<MIN_SCALE)
						scale=MIN_SCALE;
					map.setScale(scale);
				}
				return super.scrolled(event, x, y, amount);
			}
			@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {

			}
		});
	}
	
	private void newMap(){
		final Dialog dialog=new Dialog("New Map", Skins.METAL);
		dialog.setResizable(true);
		final CheckBox cbLoad=new CheckBox("load map", Skins.METAL);
		TextButton button=new TextButton("Confirm", Skins.METAL);
		final TextField field=new TextField("", Skins.METAL);
		field.setMessageText("Map levels (4-9)");
		final SelectBox<String> savedMaps=new SelectBox<String>(Skins.METAL);
		button.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				super.clicked(event, x, y);
				try{
					if(!cbLoad.isChecked()){
						levels=Integer.parseInt(field.getText());
						if(levels>3 && levels<10){
							dialog.remove();
							populate(levels);
							createSettings("");
						}
					}else{
						dialog.remove();
						loadMap(savedMaps.getSelected());
						createSettings(savedMaps.getSelected());
					}
				}catch(NumberFormatException e){}
			}
		});
		TextButton cancelButton=new TextButton("Cancel", Skins.METAL);
		cancelButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				notification.enterMenu();
			}
		});
		final ArrayList<String> items=new ArrayList<String>();
		final TextButton deleteButton=new TextButton("Delete", Skins.METAL);
		deleteButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Gdx.files.local("assets/maps/saved/"+savedMaps.getSelected()).delete();
				items.remove(savedMaps.getSelected());
				savedMaps.setItems(items.toArray(new String[0]));
				if(items.size()==0){
					cbLoad.setChecked(false);
					cbLoad.remove();
					savedMaps.remove();
					deleteButton.remove();
					dialog.pack();
				}
			}
		});
		FileHandle saved[]=Gdx.files.local("assets/maps/saved/").list();
		if(saved.length>0){
			dialog.getContentTable().add(cbLoad).row();
			dialog.getContentTable().add(savedMaps).row();
			dialog.getContentTable().add(deleteButton).row();
			int i=0;
			for(FileHandle file:saved){
				items.add(file.name());
				i++;
			}
			savedMaps.setItems(items.toArray(new String[0]));
		}
		final Color colField=new Color(field.getColor());
		final Color colSaved=new Color(savedMaps.getColor());
		final Color colDelete=new Color(deleteButton.getColor());
		savedMaps.setTouchable(Touchable.disabled);
		savedMaps.setColor(Color.GRAY);
		deleteButton.setTouchable(Touchable.disabled);
		deleteButton.setColor(Color.GRAY);
		cbLoad.addListener(new ChangeListener(){
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				field.setTouchable(cbLoad.isChecked()?Touchable.disabled:Touchable.enabled);
				field.setColor(cbLoad.isChecked()?Color.GRAY:colField);
				deleteButton.setTouchable(!cbLoad.isChecked()?Touchable.disabled:Touchable.enabled);
				deleteButton.setColor(!cbLoad.isChecked()?Color.GRAY:colDelete);
				savedMaps.setTouchable(!cbLoad.isChecked()?Touchable.disabled:Touchable.enabled);
				savedMaps.setColor(!cbLoad.isChecked()?Color.GRAY:colSaved);
			}
		});
		HorizontalGroup group=new HorizontalGroup();
		group.addActor(button);
		group.addActor(cancelButton);
		dialog.getContentTable().add(field).row();
		dialog.getContentTable().add(group);
		dialog.show(this);
	}
	
	private void loadMap(String mapName){
		try{
			FileHandle file=Gdx.files.local("assets/maps/saved/"+mapName);
			map=new Map();
			MapParser.parse(new JSONObject(file.readString()), map, new HashMap(), new ArrayList(), null);
		}catch(Exception e){e.printStackTrace();}
	}
	
	private void createSettings(String fileName){
		tileWindow=new TileWindow(map, this);
		tileWindow.getTextSave().setText(fileName);
		tileWindow.addListener(new ClickListener(){
			@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
				hoverNum++;
			}
			@Override
			public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
				hoverNum--;
			}
		});
		tileWindow.getButtonRandom().addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				tileWindow.setTile(null);
				randomize();
			}
		});
		tileWindow.getButtonQuit().addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				notification.enterMenu();
			}
		});
		tileWindow.getButtonSave().addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				saveMap(tileWindow.getTextSave().getText());
			}
		});
		this.addActor(map);
		this.addActor(tileWindow);
		map.setBounds(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		setupMapEvents();
		initialized=true;
	}
	
	private void setupMapEvents(){
		map.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Tile tile=map.getCurrentTile();
				if(tile!=null && tile!=selectedTile){
					if(selectedTile!=null)
						for(Edge e:selectedTile.edges)
							e.selected=false;
					selectedTile=tile;
					for(Edge e:selectedTile.edges)
						e.selected=true;
					tileWindow.setTile(selectedTile);
				}
			}
		});
		map.addListener(new InputListener(){
			@Override
			public boolean mouseMoved(InputEvent event, float x, float y) {
				Tile tile=map.getCurrentTile();
				if(tile!=null && tile!=previousTile){
					if(previousTile!=null)
						previousTile.highlight=false;
					previousTile=tile;
					previousTile.highlight=true;
				}
				return true;
			}
		});
	}
	
	private boolean checkValidity(){
		int tileNumbers[]=new int[11];
		HashMap<TileType, Integer> tileTypes=new HashMap<TileType, Integer>();
		tileTypes.put(TileType.LAKE, 0);
		tileTypes.put(TileType.FIELD, 0);
		tileTypes.put(TileType.FOREST, 0);
		tileTypes.put(TileType.HILL, 0);
		tileTypes.put(TileType.MOUNTAIN, 0);
		tileTypes.put(TileType.PASTURE, 0);
		tileTypes.put(TileType.GOLD, 0);
		Tile lake=null;
		for(int i=0;i<tileNumbers.length;i++)
			tileNumbers[i]=0;
		for(Tile tile:map.getTiles().values()){
			tileNumbers[tile.getTileNumber()-2]++;
			if(tileTypes.containsKey(tile.getTileType())){
				int tileType=tileTypes.get(tile.getTileType());
				tileType++;
				tileTypes.put(tile.getTileType(), tileType);
			}
			if(tile.getTileType()==TileType.LAKE)
				lake=tile;
		}
		
		for(int i=0;i<tileNumbers.length;i++)
			//need at least one of each tile numbers
			if(i!=(7-2) && tileNumbers[i]==0)
				return false;
		
		if(tileTypes.get(TileType.LAKE)!=1)
			return false;
		
		for(int numType:tileTypes.values())
			if(numType==0)
				return false;
		
		if(map.getFishingGrounds().size()!=7)
			return false;
		
		for(Tile tile:lake.neighbors)
			if(tile.getTileType()==TileType.OCEAN)
				return false;
		
		return true;
	}
	
	private void saveMap(final String fileName){
		boolean validName=false;
		for(int i=0;i<fileName.length();i++)
			validName|=(fileName.charAt(i)!=' ');
		if(!validName)
			new Dialog("Invalid file name", Skins.METAL).button("ok").show(this);
		else if(!checkValidity()){
			Label errorLabel=new Label("Map does not meet requirements. "
					+ "You need exactly one lake tile, and each "
					+ "tile number must appear once at least, and each terrain type "
					+ "other than ocean must appear at least once. "
					+"The lake cannot be adjacent to an ocean tile, and your map must "
					+"contain all seven fishing grounds (which can be placed on ocean hexes on the coast "
					+"which are adjacent to 2 land hexes.", Skins.METAL);
			errorLabel.setWrap(true);
			Dialog errorDiag=new Dialog("Invalid map.", Skins.METAL).button("ok");
			errorDiag.getContentTable().add(errorLabel).width(200);
			errorDiag.show(Editor.this);
		}else if(Gdx.files.local("assets/maps/saved").list().length>=10)
			new Dialog("You can have at most 10 save files", Skins.METAL).button("ok").show(this);
		else{
			if(Gdx.files.local("assets/maps/saved/"+fileName).exists()){
				TextButton yesButton=new TextButton("yes", Skins.METAL);
				yesButton.addListener(new ClickListener(){
					@Override
					public void clicked(InputEvent event, float x, float y) {
						writeSaveFile(fileName);
					}
				});
				new Dialog("File exists, overwrite?", Skins.METAL).button(yesButton).button("no").show(this);
			}else writeSaveFile(fileName);
		}
	}
	
	private void writeSaveFile(String fileName){
		String mapString=map.export().toString();
		FileHandle handle=Gdx.files.local("assets/maps/saved/"+fileName);
		handle.writeString(mapString, false);
		//handle=Gdx.files.local("assets/maps/presets/preset");
		//handle.writeString(mapString, false);
		new Dialog("File was saved.", Skins.METAL).button("ok").show(this);
	}
	
	private void randomize(){
		map.getFishingGrounds().clear();
		tileWindow.createFishingGrounds();
		ArrayList<FishingGround> fishingGrounds=tileWindow.getFishingGrounds();
		ArrayList<Tile> inner=new ArrayList<Tile>();
		TileType types[]=new TileType[]{TileType.PASTURE, TileType.HILL, TileType.MOUNTAIN, 
				TileType.FIELD, TileType.FOREST};
		int numbers[]=new int[]{2,3,4,5,6,8,9,10,11,12};
		ArrayList<TileType> tileTypes=new ArrayList<TileType>();
		ArrayList<Integer> tileNumbers=new ArrayList<Integer>();
		for(Tile tile:map.getTiles().values()){
			tile.setTileType(TileType.FIELD);
			tile.setHarbor(null);
		}
		map.getHarbors().clear();
		
		ArrayList<Tile> oceanTiles=new ArrayList<Tile>();
		for(Tile tile:map.getTiles().values()){
			if(tile.neighbors.size()!=6){
				oceanTiles.add(tile);
				tile.setTileType(TileType.OCEAN);
			}else
				inner.add(tile);
		}

		ArrayList<Tile> lakeCandidates=new ArrayList<Tile>();
		for(Tile tile:inner){
			boolean candidate=true;
			for(Tile t:tile.neighbors)
				if(t.getTileType()==TileType.OCEAN){
					candidate=false;
					break;
				}
			if(candidate)
				lakeCandidates.add(tile);
		}
		
		Tile lakeTile=lakeCandidates.remove((int)(Math.random()*lakeCandidates.size()));
		inner.remove(lakeTile);
		lakeTile.setTileNumber(7);
		lakeTile.setTileType(TileType.LAKE);
		
		for(int i=0;i<inner.size();i++){
			if(i==inner.size()-1)
				tileTypes.add(TileType.GOLD);
			else
				tileTypes.add(types[i%types.length]);
			tileNumbers.add(numbers[i%numbers.length]);
		}
		
		for(Tile tile:inner){
			int typeIndex=(int)(Math.random()*tileTypes.size());
			int numberIndex=(int)(Math.random()*tileNumbers.size());
			TileType tileType=tileTypes.get(typeIndex);
			tile.setTileType(tileType);
			tileTypes.remove(typeIndex);
			tile.setTileNumber(tileNumbers.get(numberIndex));
			tileNumbers.remove(numberIndex);
		}

		ArrayList<Tile> fishingTiles=new ArrayList<Tile>();
		for(Tile tile:oceanTiles){
			int landCount=0;
			for(Tile t:tile.neighbors)
				if(t.getTileType()!=TileType.OCEAN)
					landCount++;
			if(landCount==2)
				fishingTiles.add(tile);
		}

		while(!fishingGrounds.isEmpty()){
			FishingGround ground=fishingGrounds.remove((int)(Math.random()*fishingGrounds.size()));
			Tile tile=fishingTiles.remove((int)(Math.random()*fishingTiles.size()));
			oceanTiles.remove(tile);
			ArrayList<Vertex> vertices=new ArrayList<Vertex>();
			for(Edge e:tile.edges)
				for(Tile t:e.neighbors)
					if(t.getTileType()!=TileType.OCEAN){
						if(!vertices.contains(e.first))
							vertices.add(e.first);
						if(!vertices.contains(e.second))
							vertices.add(e.second);
					}
			ground.setLocation(vertices.get(0), vertices.get(1), vertices.get(2), tile);
			map.getFishingGrounds().put(tile.id, ground);
		}
		

		TileType harborTypes[]=new TileType[]{TileType.FIELD, TileType.FOREST, TileType.HILL, TileType.MOUNTAIN, TileType.PASTURE, null, null, null, null};
		ArrayList<TileType> harborList=new ArrayList<TileType>(Arrays.asList(harborTypes));
		
		while(!harborList.isEmpty()){
			TileType harborType=harborList.remove((int)(Math.random()*harborList.size()));
			Tile tile=oceanTiles.remove((int)(Math.random()*oceanTiles.size()));
			ArrayList<Edge> candidates=new ArrayList<Edge>(2);
			for(Edge e:tile.edges)
				for(Tile t:e.neighbors)
					if(t.getTileType()!=TileType.OCEAN)
						candidates.add(e);
			Edge edge=candidates.get((int)(Math.random()*candidates.size()));
			Harbor harbor=new Harbor(tile, edge, harborType==null, harborType==null?TileType.FIELD:harborType);
			tile.setHarbor(harbor);
			map.getHarbors().put(tile.id, harbor);
		}
		
	}
	
	private void populate(int levels){
		ArrayList<Edge> edgeList=new ArrayList<Edge>();
		ArrayList<Vertex> vertexList=new ArrayList<Vertex>();
		ArrayList<Tile> tileList=new ArrayList<Tile>();
		int range=levels-1;
		double startX=Gdx.graphics.getWidth()/2-range*(Tile.RADIUS+Tile.SIDE/2);
		double startY=Gdx.graphics.getHeight()/2-range*Tile.APOTHEM;
		double incX=Tile.RADIUS+Tile.SIDE/2;
		double incY=2*Tile.APOTHEM;
		int id=1;
		for(int j=0;j<2*levels-1;j++){
			for(int i=0;i<2*levels-1;i++){
				int offsetY=range-Math.abs(range-j);
				if((i-offsetY)<=range){
					Tile tile=new Tile(startX+j*incX, startY+i*incY-offsetY*Tile.APOTHEM, 2);
					tile.id=id;
					tileList.add(tile);
					id++;
					Edge eTop=null, eTopRight=null, eBotRight=null, eBot=null, eBotLeft=null, eTopLeft=null;
					Vertex vTopRight=null, vRight=null, vBotRight=null, vBotLeft=null, vLeft=null, vTopLeft=null;
					// Find shared edges
					for(Edge other:edgeList){
						if(tile.eTop.distanceTo(other.getMid())<EPSILON)
							eTop=other;
						else if(tile.eTopRight.distanceTo(other.getMid())<EPSILON)
							eTopRight=other;
						else if(tile.eBotRight.distanceTo(other.getMid())<EPSILON)
							eBotRight=other;
						else if(tile.eBot.distanceTo(other.getMid())<EPSILON)
							eBot=other;
						else if(tile.eBotLeft.distanceTo(other.getMid())<EPSILON)
							eBotLeft=other;
						else if(tile.eTopLeft.distanceTo(other.getMid())<EPSILON)
							eTopLeft=other;
					}
					if(eTop!=null)
						tile.eTop=eTop;
					else edgeList.add(tile.eTop);
					if(eTopRight!=null)
						tile.eTopRight=eTopRight;
					else edgeList.add(tile.eTopRight);
					if(eBotRight!=null)
						tile.eBotRight=eBotRight;
					else edgeList.add(tile.eBotRight);
					if(eBot!=null)
						tile.eBot=eBot;
					else edgeList.add(tile.eBot);
					if(eBotLeft!=null)
						tile.eBotLeft=eBotLeft;
					else edgeList.add(tile.eBotLeft);
					if(eTopLeft!=null)
						tile.eTopLeft=eTopLeft;
					else edgeList.add(tile.eTopLeft);
					
					// Find shared vertices
					for(Vertex other:vertexList){
						if(tile.vTopRight.getPosition().distanceTo(other.getPosition())<EPSILON)
							vTopRight=other;
						else if(tile.vRight.getPosition().distanceTo(other.getPosition())<EPSILON)
							vRight=other;
						else if(tile.vBotRight.getPosition().distanceTo(other.getPosition())<EPSILON)
							vBotRight=other;
						else if(tile.vBotLeft.getPosition().distanceTo(other.getPosition())<EPSILON)
							vBotLeft=other;
						else if(tile.vLeft.getPosition().distanceTo(other.getPosition())<EPSILON)
							vLeft=other;
						else if(tile.vTopLeft.getPosition().distanceTo(other.getPosition())<EPSILON)
							vTopLeft=other;
					}
					if(vTopRight!=null)
						tile.vTopRight=vTopRight;
					else vertexList.add(tile.vTopRight);
					if(vRight!=null)
						tile.vRight=vRight;
					else vertexList.add(tile.vRight);
					if(vBotRight!=null)
						tile.vBotRight=vBotRight;
					else vertexList.add(tile.vBotRight);
					if(vBotLeft!=null)
						tile.vBotLeft=vBotLeft;
					else vertexList.add(tile.vBotLeft);
					if(vLeft!=null)
						tile.vLeft=vLeft;
					else vertexList.add(tile.vLeft);
					if(vTopLeft!=null)
						tile.vTopLeft=vTopLeft;
					else vertexList.add(tile.vTopLeft);
				}
			}
		}
		id=1;
		int edgeId=1;
		//Find shared edge endpoints
		for(Vertex vertex:vertexList){
			vertex.id=id;
			id++;
			for(Edge edge:edgeList){
				edge.id=edgeId;
				edgeId++;
				if(edge.first.distanceTo(vertex.getPosition())<EPSILON)
					edge.first=vertex;
				if(edge.second.distanceTo(vertex.getPosition())<EPSILON)
					edge.second=vertex;
				if(Math.abs(Tile.SIDE/2-edge.distanceTo(vertex.getPosition()))<EPSILON)
					vertex.edges.add(edge);
			}
		}

		for(Tile tile:tileList){
			tile.edges.add(tile.eTop);
			tile.edges.add(tile.eTopRight);
			tile.edges.add(tile.eBotRight);
			tile.edges.add(tile.eBot);
			tile.edges.add(tile.eBotLeft);
			tile.edges.add(tile.eTopLeft);
			tile.vertices.add(tile.vTopRight);
			tile.vertices.add(tile.vRight);
			tile.vertices.add(tile.vBotRight);
			tile.vertices.add(tile.vBotLeft);
			tile.vertices.add(tile.vLeft);
			tile.vertices.add(tile.vTopLeft);
			for(Edge e:tile.edges)
				e.neighbors.add(tile);
			for(Vertex v:tile.vertices)
				v.neighbors.add(tile);
		}
		// Find the neighboring tiles
		for(Tile first:tileList){
			for(Tile second:tileList){
				if(first!=second){
					for(Edge edge:first.edges){
						if(second.edges.contains(edge)){
							if(!first.neighbors.contains(second))
								first.neighbors.add(second);
							if(!second.neighbors.contains(first))
								second.neighbors.add(first);
							break;
						}
					}
				}
			}
		}
		HashMap<Integer, Vertex> vertices=new HashMap<Integer, Vertex>();
		HashMap<Integer, Edge> edges=new HashMap<Integer, Edge>();
		HashMap<Integer, Tile> tiles=new HashMap<Integer, Tile>();
		for(Vertex v:vertexList)
			vertices.put(v.id, v);
		for(Edge e:edgeList)
			edges.put(e.id, e);
		for(Tile t:tileList)
			tiles.put(t.id,  t);
		map=new Map(tiles, edges, vertices);
		for(int i=0;i<11;i++)
			map.getFishToken().add(new FishToken(1, false));
		for(int i=0;i<10;i++)
			map.getFishToken().add(new FishToken(2, false));
		for(int i=0;i<8;i++)
			map.getFishToken().add(new FishToken(3, false));
		map.getFishToken().add(new FishToken(0, true));
		Collections.shuffle(map.getFishToken());
	}
	
	@Override
	public void draw() {
		super.draw();
	}
	
	@Override
	public void act(float delta) {
		super.act(delta);
		if(initialized){
			if(Gdx.input.isKeyPressed(Input.Keys.RIGHT))
				map.scroll(8, 0);
			if(Gdx.input.isKeyPressed(Input.Keys.LEFT))
				map.scroll(-8, 0);
			if(Gdx.input.isKeyPressed(Input.Keys.UP))
				map.scroll(0, -8);
			if(Gdx.input.isKeyPressed(Input.Keys.DOWN))
				map.scroll(0, 8);
			map.setHovered(hoverNum==0 && this.getActors().size==2);
		}
	}
	
	@Override
	public void dispose() {
		super.dispose();
		renderer.dispose();
		batch.dispose();
	}
	
	class SmallWindow extends Window{
		
		public SmallWindow(String title, Skin skin) {
			super(title, skin);
		}
		@Override
		public float getMinHeight() {
			return 50;
		}
		@Override
		public float getMinWidth() {
			return 50;
		}
		public Rectangle getBounds(){
			return new Rectangle(getX(), getY(), getWidth(), getHeight());
		}
	}
}
