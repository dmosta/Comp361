package commands;

import actions.CardAction;
import actions.TurnAction;
import cards.ScienceCard;
import comp361.catan.GameStage;
import comp361.catan.Player;
import comp361.catan.Tile;

public class InventorCommand extends GameCommand{
	
	private static final long serialVersionUID = -1290267665296070506L;
	private int tile1;
	private int tile2;
	private String playerID;
	
	public InventorCommand(String playerID, int tile1, int tile2){
		super(1);
		this.playerID=playerID;
		this.tile1=tile1;
		this.tile2=tile2;
	}
	
	@Override
	protected void executeCommand() {
		CardAction action=(CardAction)getAction();
		Player player=getPlayers().get(playerID);
		player.getCards().remove(ScienceCard.INVENTOR);
		getCards().add(ScienceCard.INVENTOR);
		Tile firstTile=getMap().getTile(tile1);
		Tile secondTile=getMap().getTile(tile2);
		int num1=firstTile.getTileNumber();
		int num2=secondTile.getTileNumber();
		firstTile.setTileNumber(num2);
		secondTile.setTileNumber(num1);
		getToolbar().getChatWindow().log("Player "+player.getPeer().getName()+" has played the inventor and switched the "
				+ "numbers of two tiles ("+num1+" and "+num2+")");
		action.cancelProgressCard();
	}
}
