package comp361.catan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.badlogic.gdx.graphics.Color;

import Math.Vector;
import cards.Card;
import cards.PoliticsCard;
import cards.ScienceCard;
import cards.TradeCard;
import util.CardUtility;

/**
 * Map parser does two things. First it allows to save the state of a game in json, and next
 * it allows to restore a game from the state stored in a json object.
 */
public class MapParser {
	
	/**
	 * Stores the state of the game in a json object
	 */
	public static JSONObject export(Map map, GameStage stage){
		HashMap<Integer, Tile> tiles=map.getTiles();
		HashMap<Integer, Edge> edges=map.getEdges();
		HashMap<Integer, Vertex> vertices=map.getVertices();
		HashMap<String, Player> players=new HashMap<String, Player>();
		HashMap<Integer, Harbor> harbors=map.getHarbors();
		HashMap<Integer, FishingGround> fishingGrounds=map.getFishingGrounds();
		ArrayList<Card> cards=new ArrayList<Card>();
		
		if(stage!=null){
			players=stage.getPlayers();
			cards=stage.getCards();
		}
		try{
			JSONArray vertArr=new JSONArray();
			for(Vertex vertex:vertices.values()){
				JSONObject obj=new JSONObject();
				obj.put("id", vertex.id);
				obj.put("x", vertex.getPosition().getX());
				obj.put("y", vertex.getPosition().getY());
				JSONArray vertEdges=new JSONArray();
				for(Edge edge:vertex.edges)
					vertEdges.put(edge.id);
				JSONArray vertTiles=new JSONArray();
				for(Tile tile:vertex.neighbors)
					vertTiles.put(tile.id);
				obj.put("edges", vertEdges);
				obj.put("tiles", vertTiles);
				if(vertex.getConstruction()!=null){
					JSONObject vertCons=new JSONObject();
					VertexConstruction cons=vertex.getConstruction();
					if(vertex.getConstruction() instanceof Settlement)
						vertCons.put("type", "settlement");
					else if(vertex.getConstruction() instanceof City){
						City city=(City)vertex.getConstruction();
						vertCons.put("type", "city");
						vertCons.put("hasWall", city.hasWall());
					}
					else if(vertex.getConstruction() instanceof Knight){
						Knight knight=(Knight)vertex.getConstruction();
						vertCons.put("level", knight.getLevel());
						vertCons.put("active", knight.isActive());
						vertCons.put("type", "knight");
					}
					vertCons.put("vertex", cons.getParent().id);
					vertCons.put("player", cons.getOwner().getOrder());
					obj.put("construction", vertCons);
				}
				vertArr.put(obj);
			}
			JSONArray edgeArr=new JSONArray();
			for(Edge edge:edges.values()){
				JSONObject obj=new JSONObject();
				obj.put("id", edge.id);
				obj.put("first", edge.first.id);
				obj.put("second", edge.second.id);
				JSONArray edgeTiles=new JSONArray();
				for(Tile tile:edge.neighbors)
					edgeTiles.put(tile.id);
				obj.put("tiles", edgeTiles);
				
				if(edge.getConstruction()!=null){
					JSONObject consObj=new JSONObject();
					EdgeConstruction cons=edge.getConstruction();
					if(cons instanceof Road)
						consObj.put("type", "road");
					else if(edge.getConstruction() instanceof Ship)
						consObj.put("type", "ship");
					consObj.put("edge", edge.id);
					consObj.put("player", cons.getOwner().getOrder());
					obj.put("construction", consObj);
				}
				edgeArr.put(obj);
			}
			JSONArray tileArr=new JSONArray();
			for(Tile tile:tiles.values()){
				JSONObject obj=new JSONObject();
				obj.put("id", tile.id);
				obj.put("x", tile.getX());
				obj.put("y", tile.getY());
				obj.put("type", tile.getTileType());
				obj.put("number", tile.getTileNumber());
				//vertices
				obj.put("vLeft", tile.vLeft.id);
				obj.put("vTopLeft", tile.vTopLeft.id);
				obj.put("vTopRight", tile.vTopRight.id);
				obj.put("vRight", tile.vRight.id);
				obj.put("vBotRight", tile.vBotRight.id);
				obj.put("vBotLeft", tile.vBotLeft.id);
				//edges
				obj.put("eTop", tile.eTop.id);
				obj.put("eTopRight", tile.eTopRight.id);
				obj.put("eBotRight", tile.eBotRight.id);
				obj.put("eBot", tile.eBot.id);
				obj.put("eBotLeft", tile.eBotLeft.id);
				obj.put("eTopLeft", tile.eTopLeft.id);
				JSONArray neighborArr=new JSONArray();
				for(Tile t:tile.neighbors)
					neighborArr.put(t.id);
				obj.put("neighbors", neighborArr);
				tileArr.put(obj);
			}
			
			JSONArray harborArr=new JSONArray();
			for(Harbor harbor:harbors.values()){
				JSONObject obj=new JSONObject();
				obj.put("tile", harbor.getTile().id);
				obj.put("edge", harbor.getEdge().id);
				obj.put("generalHarbor", harbor.isGeneralHarbor());
				obj.put("type", harbor.getType());
				harborArr.put(obj);
			}
			
			JSONArray fishingArr=new JSONArray();
			for(FishingGround ground:fishingGrounds.values()){
				JSONObject obj=new JSONObject();
				obj.put("location", ground.getLocation().id);
				obj.put("vertex1", ground.vertex1.id);
				obj.put("vertex2", ground.vertex2.id);
				obj.put("vertex3", ground.vertex3.id);
				obj.put("roll", ground.getRoll());
				fishingArr.put(obj);
			}
			
			JSONArray tokenArr=new JSONArray();
			for(FishToken token:map.getFishToken()){
				JSONObject obj=new JSONObject();
				obj.put("value", token.getValue());
				obj.put("isBoot", token.isBoot());
				tokenArr.put(obj);
			}
			
			JSONArray playerArr=new JSONArray();
			for(Player player:players.values()){
				JSONObject obj=new JSONObject();
				obj.put("order", player.getOrder());
				obj.put("fingerprint", player.getPeer().getFingerprint());
				obj.put("name", player.getPeer().getName());
				Color color=player.getColor();
				obj.put("red", color.r);
				obj.put("green", color.g);
				obj.put("blue", color.b);
				obj.put("current", player.isTurn());
				obj.put("politics", player.getPoliticsLevel());
				obj.put("trade", player.getTradeLevel());
				obj.put("science", player.getScienceLevel());
				obj.put("numWalls", player.getNumWalls());
				obj.put("victory", player.getVictoryPoints());
				obj.put("citiesRemaining", player.getCitiesRemaining());
				obj.put("settlementsRemaining", player.getSettlementsRemaining());
				obj.put("basicKnightsRemaining", player.getBasicKnightsRemaining());
				obj.put("strongKnightsRemaining", player.getStrongKnightsRemaining());
				obj.put("mightyKnightsRemaining", player.getMightyKnightsRemaining());
				obj.put("hasBoot", player.hasBoot());
				obj.put("longestRoad", player.hasLongestRoad());
				obj.put("roadLength", player.getRoadLength());
				obj.put("gold", player.getGold());
				JSONObject resourceObj=new JSONObject();
				for(ResourceType type:player.getResources().keySet())
					resourceObj.put(type+"", player.getResources().get(type));
				obj.put("resources", resourceObj);
				JSONArray playerCards=new JSONArray();
				for(Card card:player.getCards())
					playerCards.put(card.toString());
				obj.put("cards", playerCards);
				JSONArray playerTokens=new JSONArray();
				for(FishToken token:player.getFishToken()){
					JSONObject tokenObj=new JSONObject();
					tokenObj.put("value", token.getValue());
					tokenObj.put("isBoot", token.isBoot());
					playerTokens.put(tokenObj);
				}
				obj.put("tokens", playerTokens);
				playerArr.put(obj);
			}
			
			JSONObject main=new JSONObject();
			main.put("players", playerArr);
			main.put("vertices", vertArr);
			main.put("edges", edgeArr);
			main.put("tiles", tileArr);
			main.put("harbors", harborArr);
			main.put("fishing", fishingArr);
			main.put("tokens", tokenArr);
			main.put("state", GameStage.GAME_STATE+"");
			main.put("tradeMetropolis", map.getTradeMetropolis().getCity()==null?-1:map.getTradeMetropolis().getCity().getParent().id);
			main.put("scienceMetropolis", map.getScienceMetropolis().getCity()==null?-1:map.getScienceMetropolis().getCity().getParent().id);
			main.put("politicsMetropolis", map.getPoliticsMetropolis().getCity()==null?-1:map.getPoliticsMetropolis().getCity().getParent().id);
			main.put("barbarianPosition", map.getBarbarianPosition());
			main.put("defenderVPRemaining", map.getDefenderVPRemaining());
			main.put("firstBarbarianAttack", map.getFirstBarbarianAttack());
			main.put("pirateLocation", map.getPirate().getLocation()==null?-1:map.getPirate().getLocation().id);
			main.put("robberLocation", map.getRobber().getLocation()==null?-1:map.getRobber().getLocation().id);
			main.put("merchantLocation", map.getMerchant().getLocation()==null?-1:map.getMerchant().getLocation().id);
			main.put("merchantOwner", map.getMerchant().getOwner()==null?-1:map.getMerchant().getOwner().getOrder());
			if(stage!=null){
				main.put("victoryPoints", stage.getGame().getVictoryPoints());
			}
			JSONArray cardArr=new JSONArray();
			for(Card card:cards)
				cardArr.put(card.toString());
			main.put("cards", cardArr);
			return main;
		}catch(JSONException e){e.printStackTrace();}
		return null;
	}
	
	/**
	 * Restores the state of the game from a jsonobject
	 */
	public static void parse(JSONObject main, Map map, HashMap<Integer, Player> players, ArrayList<Card> cards, GameStage stage){
		HashMap<Integer, Tile> tiles=new HashMap<Integer, Tile>();
		HashMap<Integer, Vertex> vertices=new HashMap<Integer, Vertex>();
		HashMap<Integer, Edge> edges=new HashMap<Integer, Edge>();
		HashMap<Integer, Harbor> harbors=new HashMap<Integer, Harbor>();
		HashMap<Integer, FishingGround> fishingGrounds=new HashMap<Integer, FishingGround>();
		try{
			JSONArray vertArr=main.getJSONArray("vertices");
			JSONArray edgeArr=main.getJSONArray("edges");
			JSONArray tileArr=main.getJSONArray("tiles");
			JSONArray playerArr=main.getJSONArray("players");
			JSONArray cardArr=main.getJSONArray("cards");
			JSONArray harborArr=main.getJSONArray("harbors");
			JSONArray fishingArr=main.getJSONArray("fishing");
			JSONArray tokenArr=main.getJSONArray("tokens");
			
			for(int i=0;i<cardArr.length();i++)
				cards.add(CardUtility.getCardFromString(cardArr.getString(i)));
			for(int i=0;i<playerArr.length();i++){
				JSONObject obj=playerArr.getJSONObject(i);
				Peer peer=new Peer(obj.getString("name"), "", obj.getString("fingerprint"));
				Color color=new Color((float)obj.getDouble("red"), (float)obj.getDouble("green"), 
						(float)obj.getDouble("blue"), 1);
				Player player=new Player(peer, color, obj.getInt("order"));
				player.setPoliticsLevel(obj.getInt("politics"));
				player.setTradeLevel(obj.getInt("trade"));
				player.setScienceLevel(obj.getInt("science"));
				player.setTurn(obj.getBoolean("current"));
				player.setNumWalls(obj.getInt("numWalls"));
				player.setVictoryPoints(obj.getInt("victory"));
				player.setCitiesRemaining(obj.getInt("citiesRemaining"));
				player.setSettlementsRemaining(obj.getInt("settlementsRemaining"));
				player.setBasicKnightsRemaining(obj.getInt("basicKnightsRemaining"));
				player.setStrongKnightsRemaining(obj.getInt("strongKnightsRemaining"));
				player.setMightyKnightsRemaining(obj.getInt("mightyKnightsRemaining"));
				player.setHasBoot(obj.getBoolean("hasBoot"));
				player.setLongestRoad(obj.getBoolean("longestRoad"));
				player.setRoadLength(obj.getInt("roadLength"));
				player.setGold(obj.getInt("gold"));
				players.put(obj.getInt("order"), player);
				JSONObject resourceObj=obj.getJSONObject("resources");
				Iterator it=resourceObj.keys();
				while(it.hasNext()){
					String key=(String)it.next();
					ResourceType type=ResourceType.valueOf(key);
					int num=resourceObj.getInt(key);
					player.getResources().put(type, num);
				}
				JSONArray playerCards=obj.getJSONArray("cards");
				for(int j=0;j<playerCards.length();j++)
					player.getCards().add(CardUtility.getCardFromString(playerCards.getString(j)));
				JSONArray playerTokens=obj.getJSONArray("tokens");
				for(int j=0;j<playerTokens.length();j++){
					JSONObject tokenObj=playerTokens.getJSONObject(j);
					FishToken token=new FishToken(tokenObj.getInt("value"), tokenObj.getBoolean("isBoot"));
					player.getFishToken().add(token);
				}
			}
			for(int i=0;i<vertArr.length();i++){
				JSONObject obj=vertArr.getJSONObject(i);
				Vertex vertex=new Vertex(new Vector(obj.getDouble("x"), obj.getDouble("y")));
				vertex.id=obj.getInt("id");
				vertices.put(vertex.id, vertex);
			}
			for(int i=0;i<edgeArr.length();i++){
				JSONObject obj=edgeArr.getJSONObject(i);
				Edge edge=new Edge(vertices.get(obj.getInt("first")), vertices.get(obj.getInt("second")));
				edge.id=obj.getInt("id");
				edges.put(edge.id, edge);
			}
			for(int i=0;i<tileArr.length();i++){
				JSONObject obj=tileArr.getJSONObject(i);
				Tile tile=new Tile(obj.getDouble("x"), obj.getDouble("y"), obj.getInt("number"));
				tile.id=obj.getInt("id");
				tile.setTileType(TileType.valueOf(obj.getString("type")));
				tile.setTileNumber(obj.getInt("number"));
				//vertices
				tile.vLeft=vertices.get(obj.getInt("vLeft"));
				tile.vTopLeft=vertices.get(obj.getInt("vTopLeft"));
				tile.vTopRight=vertices.get(obj.getInt("vTopRight"));
				tile.vRight=vertices.get(obj.getInt("vRight"));
				tile.vBotRight=vertices.get(obj.getInt("vBotRight"));
				tile.vBotLeft=vertices.get(obj.getInt("vBotLeft"));
				//edges
				tile.eTop=edges.get(obj.getInt("eTop"));
				tile.eTopRight=edges.get(obj.getInt("eTopRight"));
				tile.eBotRight=edges.get(obj.getInt("eBotRight"));
				tile.eBot=edges.get(obj.getInt("eBot"));
				tile.eBotLeft=edges.get(obj.getInt("eBotLeft"));
				tile.eTopLeft=edges.get(obj.getInt("eTopLeft"));
				//add tiles and edges to array
				tile.edges.add(tile.eTop);tile.edges.add(tile.eTopRight);tile.edges.add(tile.eBotRight);
				tile.edges.add(tile.eBot);tile.edges.add(tile.eBotLeft);tile.edges.add(tile.eTopLeft);
				tile.vertices.add(tile.vTopRight);tile.vertices.add(tile.vRight);tile.vertices.add(tile.vBotRight);
				tile.vertices.add(tile.vBotLeft);tile.vertices.add(tile.vLeft);tile.vertices.add(tile.vTopLeft);
				tiles.put(tile.id, tile);
			}
			for(int i=0;i<tileArr.length();i++){
				JSONObject obj=tileArr.getJSONObject(i);
				JSONArray neighborArr=obj.getJSONArray("neighbors");
				Tile tile=tiles.get(obj.getInt("id"));
				for(int j=0;j<neighborArr.length();j++)
					tile.neighbors.add(tiles.get(neighborArr.getInt(j)));
			}
			for(int i=0;i<vertArr.length();i++){
				JSONObject obj=vertArr.getJSONObject(i);
				JSONArray vertEdges=obj.getJSONArray("edges");
				Vertex vertex=vertices.get(obj.getInt("id"));
				for(int j=0;j<vertEdges.length();j++){
					Edge edge=edges.get(vertEdges.getInt(j));
					vertex.edges.add(edge);
				}
				JSONArray vertTiles=obj.getJSONArray("tiles");
				for(int j=0;j<vertTiles.length();j++){
					Tile tile=tiles.get(vertTiles.getInt(j));
					vertex.neighbors.add(tile);
				}
				if(obj.has("construction")){
					JSONObject objCons=obj.getJSONObject("construction");
					Player player=players.get(objCons.getInt("player"));
					if(objCons.getString("type").equals("settlement")){
						Settlement settlement=new Settlement(vertex, player);
						vertex.setConstruction(settlement);
						player.getVertexContructions().add(settlement);
					}
					else if(objCons.getString("type").equals("city")){
						City city=new City(vertex, player);
						city.setWall(objCons.getBoolean("hasWall"));
						vertex.setConstruction(city);
						player.getVertexContructions().add(city);
					}
					else if(objCons.getString("type").equals("knight")){
						Knight knight=new Knight(vertex, player);
						knight.setActive(objCons.getBoolean("active"));
						knight.setLevel(objCons.getInt("level"));
						vertex.setConstruction(knight);
						player.getVertexContructions().add(knight);
					}
				}
			}
			for(int i=0;i<edgeArr.length();i++){
				JSONObject obj=edgeArr.getJSONObject(i);
				Edge edge=edges.get(obj.getInt("id"));
				JSONArray edgeTiles=obj.getJSONArray("tiles");
				for(int j=0;j<edgeTiles.length();j++){
					Tile tile=tiles.get(edgeTiles.getInt(j));
					edge.neighbors.add(tile);
				}
				if(obj.has("construction")){
					JSONObject objCons=obj.getJSONObject("construction");
					Player player=players.get(objCons.getInt("player"));
					if(objCons.getString("type").equals("road")){
						Road road=new Road(edge, player);
						edge.setConstruction(road);
						player.getEdgeConstructions().add(road);
					}else if(objCons.getString("type").equals("road")){
						Ship ship=new Ship(edge, player);
						edge.setConstruction(ship);
						player.getEdgeConstructions().add(ship);
					}
				}
			}
			
			for(int i=0;i<harborArr.length();i++){
				JSONObject obj=harborArr.getJSONObject(i);
				boolean generalHarbor=obj.getBoolean("generalHarbor");
				int tileID=obj.getInt("tile");
				int edgeID=obj.getInt("edge");
				Tile tile=tiles.get(tileID);
				Edge edge=edges.get(edgeID);
				TileType type=TileType.valueOf(obj.getString("type"));
				Harbor harbor=new Harbor(tile, edge, generalHarbor, type);
				tile.setHarbor(harbor);
				harbors.put(tileID, harbor);
			}
			
			for(int i=0;i<fishingArr.length();i++){
				JSONObject obj=fishingArr.getJSONObject(i);
				FishingGround ground=new FishingGround(obj.getInt("roll"));
				Tile location=tiles.get(obj.getInt("location"));
				Vertex v1=vertices.get(obj.getInt("vertex1"));
				Vertex v2=vertices.get(obj.getInt("vertex2"));
				Vertex v3=vertices.get(obj.getInt("vertex3"));
				ground.setLocation(v1, v2, v3, location);
				fishingGrounds.put(location.id, ground);
			}
			
			for(int i=0;i<tokenArr.length();i++){
				JSONObject obj=tokenArr.getJSONObject(i);
				FishToken token=new FishToken(obj.getInt("value"), obj.getBoolean("isBoot"));
				map.getFishToken().add(token);
			}
			
			map.setBarbarianPosition(main.getInt("barbarianPosition"));
			map.setDefenderVPRemaining(main.getInt("defenderVPRemaining"));
			map.setFirstBarbarianAttack(main.getBoolean("firstBarbarianAttack"));
			int tradeMetropolis=main.getInt("tradeMetropolis");
			if(tradeMetropolis!=-1)
				map.getTradeMetropolis().setCity((City)vertices.get(tradeMetropolis).getConstruction());
			int scienceMetropolis=main.getInt("scienceMetropolis");
			if(scienceMetropolis!=-1)
				map.getScienceMetropolis().setCity((City)vertices.get(scienceMetropolis).getConstruction());
			int politicsMetropolis=main.getInt("politicsMetropolis");
			if(politicsMetropolis!=-1)
				map.getPoliticsMetropolis().setCity((City)vertices.get(politicsMetropolis).getConstruction());
			int robberLocation=main.getInt("robberLocation");
			if(robberLocation!=-1)
				map.getRobber().setLocation(map.getTile(robberLocation));
			int pirateLocation=main.getInt("pirateLocation");
			if(pirateLocation!=-1)
				map.getPirate().setLocation(map.getTile(pirateLocation));
			int merchantLocation=main.getInt("merchantLocation");
			if(merchantLocation!=-1)
				map.getMerchant().setLocation(tiles.get(merchantLocation));
			int merchantOwner=main.getInt("merchantOwner");
			for(Player p:players.values()){
				if(p.getOrder()==merchantOwner){
					map.getMerchant().setOwner(p);
					break;
				}
			}
			if(stage!=null && stage.getGame().isSaved()){
				int vp=main.getInt("victoryPoints");
				stage.getGame().setVictoryPoints(vp);
			}
			
		}catch(Exception e){e.printStackTrace();}
		map.setEdges(edges);
		map.setVertices(vertices);
		map.setTiles(tiles);
		map.setHarbors(harbors);
		map.setFishingGrounds(fishingGrounds);
	}
}
