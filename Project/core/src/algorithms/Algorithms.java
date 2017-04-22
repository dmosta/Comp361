package algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import comp361.catan.City;
import comp361.catan.Edge;
import comp361.catan.EdgeConstruction;
import comp361.catan.GameStage;
import comp361.catan.Knight;
import comp361.catan.Map;
import comp361.catan.Player;
import comp361.catan.Settlement;
import comp361.catan.Tile;
import comp361.catan.TileType;
import comp361.catan.Vertex;
import comp361.catan.VertexConstruction;

public class Algorithms {

	private static boolean areConnected(Vertex first, Vertex second, Player player, int constraint){
		boolean connected=false;
		ArrayList<Vertex> reachable=new ArrayList<Vertex>();
		ArrayList<Vertex> visited=new ArrayList<Vertex>();
		reachable.add(first);
		Vertex current;
		while(!reachable.isEmpty()){
			current=reachable.remove(0);
			current.visited=true;
			visited.add(current);
			boolean currentConstraint=false;
			if(constraint==1){
				currentConstraint=(current==second);
			}else if(constraint==2){
				currentConstraint=(current.getConstruction()==null && current!=first);
			}else if(constraint==3){
				if(current!=first && current.getConstruction() instanceof Knight && current.getConstruction().getOwner()==player){
					currentConstraint=((Knight)first.getConstruction()).getLevel()<((Knight)current.getConstruction()).getLevel();
				}
			}
			if(currentConstraint){
				connected=true;
				break;
			}
			for(Edge e:current.edges){
				if(e.getConstruction()!=null && e.getConstruction().getOwner()==player){
					if(!e.first.visited)
						reachable.add(e.first);
					if(!e.second.visited)
						reachable.add(e.second);
				}
			}
		}
		for(Vertex v:visited)
			v.visited=false;
		return connected;
	}
	
	public static boolean areConnected(Vertex first, Vertex second, Player player){
		return areConnected(first, second, player, 1);
	}
	
	public static boolean isConnectedToUnoccupiedVertex(Vertex first, Player player){
		return areConnected(first, null, player, 2);
	}
	
	public static boolean isConnectedToBetterKnight(Vertex first, Player knightOwner){
		return areConnected(first, null, knightOwner, 3);
	}
	
	public static boolean isEdgeValid(Edge edge, Player player, boolean road, boolean ship, Map map){
		if(edge==null)
			return false;
		boolean valid=false;
		valid|=(edge.first.getConstruction()!=null && edge.first.getConstruction().getOwner()==player);
		valid|=(edge.second.getConstruction()!=null && edge.second.getConstruction().getOwner()==player);
		if(edge.first.getConstruction()==null || edge.first.getConstruction().getOwner()==player)
			for(Edge e:edge.first.edges)
				valid|=(e!=edge && e.getConstruction()!=null && e.getConstruction().getOwner()==player);
		if(edge.second.getConstruction()==null || edge.second.getConstruction().getOwner()==player)
			for(Edge e:edge.second.edges)
				valid|=(e!=edge && e.getConstruction()!=null && e.getConstruction().getOwner()==player);
		boolean nearLand=false;
		boolean nearSea=false;
		boolean nearPirate=false;
		for(Tile tile:edge.neighbors){
			nearLand|=(tile.getTileType()!=TileType.OCEAN);
			nearSea|=(tile.getTileType()==TileType.OCEAN);
			nearPirate|=(map.getPirate().getLocation()==tile);
		}
		boolean shipValid=(ship && nearSea && !nearPirate);
		boolean roadValid=(road && nearLand);
		return valid && (shipValid || roadValid);
	}

	public static void updateLongestRoad(Map map, HashMap<String, Player> players){
		int bestLength=0;
		Player currentBest=null;
		for(Player p:players.values()){
			int length=longestRoad(map, p);
			p.setRoadLength(length);
			if(p.hasLongestRoad())
				currentBest=p;
			if(length>bestLength)
				bestLength=length;
		}
		System.out.println("Best is "+bestLength);
		ArrayList<Player> candidates=new ArrayList<Player>();
		for(Player p:players.values())
			if(p.getRoadLength()==bestLength && bestLength>=5)
				candidates.add(p);
		
		if(currentBest!=null && !candidates.contains(currentBest)){
			currentBest.setVictoryPoints(currentBest.getVictoryPoints()-2);
			currentBest.setLongestRoad(false);
		}
		if((currentBest==null || !candidates.contains(currentBest)) && candidates.size()==1){
			Player newBest=candidates.get(0);
			newBest.setLongestRoad(true);
			newBest.setVictoryPoints(newBest.getVictoryPoints()+2);
		}
	}
	
	public static int longestRoad(Map map, Player player){
		int best=0;
		HashSet<Vertex> vertices=new HashSet<Vertex>();
		for(EdgeConstruction cons:player.getEdgeConstructions()){
			vertices.add(cons.getParent().first);
			vertices.add(cons.getParent().second);
		}
		for(Vertex v:vertices){
			v.pred=-1;
			v.visited=true;
			int length=dfs(v, 0, player);
			if(length>best)
				best=length;
			v.visited=false;
		}
		if(best==0 && player.getEdgeConstructions().size()>0)
			best=1;
		return best;
	}
	
	public static int dfs(Vertex vertex, int cost, Player player){
		ArrayList<Vertex> vertices=new ArrayList<Vertex>();
		int best=cost;
		if(vertex.getConstruction()!=null && vertex.getConstruction().getOwner()!=player)
			return best;
		for(Edge e:vertex.edges){
			if(e.getConstruction()!=null && e.getConstruction().getOwner()==player){
				Vertex other=null;
				if(e.second!=vertex)
					other=e.second;
				else if(e.first!=vertex )
					other=e.first;
				if(other!=null && other.pred!=vertex.id){
					if(!other.visited)
						vertices.add(other);
					else{
						if(vertex.pred!=other.id)
							best=cost+1;
					}
				}
			}
		}
		for(Vertex v:vertices){
			v.pred=vertex.id;
			v.visited=true;
			int length=dfs(v, cost+1, player);
			if(length>best)
				best=length;
			v.pred=-1;
			v.visited=false;
		}
		return best;
	}

}
