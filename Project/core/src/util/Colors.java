package util;

import com.badlogic.gdx.graphics.Color;

import comp361.catan.TileType;

public class Colors {
	
	public static Color colorForTile(TileType type){
		switch(type){
			case OCEAN:
				return Color.BLUE;
			case PASTURE:
				return Color.GREEN;
			case HILL:
				return Color.BROWN;
			case MOUNTAIN:
				return Color.GRAY;
			case FIELD:
				return Color.YELLOW;
			case FOREST:
				return new Color(19f/255,117f/255,11f/255,1);
			case LAKE:
				return new Color(LAKE_COLOR);
			case GOLD:
				return new Color(255f/255,205f/255,0f/255,1);
		}
		return null;
	}
	
	public static final Color LAKE_COLOR=new Color(113f/255,199f/255,227f/255,1);
}
