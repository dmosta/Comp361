package comp361.catan;

import java.util.ArrayList;

/**
 * The resource class maps tile types to resource types and commodities.
 */
public class Resource {
	
	public static ResourceType resourceFromTile(TileType type){
		ResourceType resourceType;
		switch(type){
			case PASTURE:
				resourceType=ResourceType.WOOL;
				break;
			case HILL:
				resourceType=ResourceType.BRICK;
				break;
			case MOUNTAIN:
				resourceType=ResourceType.ORE;
				break;
			case FIELD:
				resourceType=ResourceType.GRAIN;
				break;
			case FOREST:
				resourceType=ResourceType.LUMBER;
				break;
			default:
				resourceType=null;
			
		}
		return resourceType;
	}
	
	public static ResourceType commodityFromTile(TileType type){
		ResourceType resourceType;
		switch(type){
		case PASTURE:
			resourceType=ResourceType.CLOTH;
			break;
		case HILL:
			resourceType=ResourceType.BRICK;
			break;
		case MOUNTAIN:
			resourceType=ResourceType.COIN;
			break;
		case FIELD:
			resourceType=ResourceType.GRAIN;
			break;
		case FOREST:
			resourceType=ResourceType.PAPER;
			break;
		default:
			resourceType=null;
		}
		return resourceType;
	}
	
	public static int calculateTotalResourcesCommodities(Player player){
		return calculateTotalResources(player)+calculateTotalCommodities(player);
	}
	
	public static int calculateTotalCommodities(Player player){
		int count=0;
		for(ResourceType type:player.getResources().keySet())
			if(Resource.isCommodity(type))
				count+=player.getResources().get(type);
		return count;
	}
	
	public static int calculateTotalResources(Player player){
		int count=0;
		for(ResourceType type:player.getResources().keySet())
			if(Resource.isResource(type))
				count+=player.getResources().get(type);
		return count;
	}
	
	public static boolean isCommodity(ResourceType type){
		return type==ResourceType.CLOTH || type==ResourceType.PAPER || type==ResourceType.COIN;
	}
	
	public static boolean isResource(ResourceType type){
		return !isCommodity(type);
	}
	
	public static ResourceType randomResourceFromPlayer(Player player){
		ArrayList<ResourceType> list=new ArrayList<ResourceType>();
		for(ResourceType type:player.getResources().keySet()){
			for(int i=0;i<player.getResources().get(type);i++){
				list.add(type);
			}
		}
		if(list.size()>0)
			return list.remove((int)(Math.random()*list.size()));
		else return null;
	}
}
