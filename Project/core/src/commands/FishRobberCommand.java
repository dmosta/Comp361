package commands;

import java.util.ArrayList;

import actions.FishAction;
import comp361.catan.Player;

public class FishRobberCommand extends FishCommand{

	private static final long serialVersionUID = -4385273910487313954L;
	private String playerID;
	private boolean pirate;
	
	public FishRobberCommand(String playerID, boolean pirate){
		super(0);
		this.playerID=playerID;
		this.pirate=pirate;
	}

	@Override
	protected void executeCommand() {
		Player player=getPlayers().get(playerID);
		if(pirate)
			getMap().getPirate().setLocation(null);
		else
			getMap().getRobber().setLocation(null);
		getToolbar().getChatWindow().log("Player "+player.getPeer().getName()+" has removed the "+(pirate?"pirate":"robber")+" from the game board using his fish.");
		((FishAction)getAction()).removeSelectedTokens(getSelectedTokens(), player);
	}
}
