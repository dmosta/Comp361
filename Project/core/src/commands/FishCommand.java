package commands;

import java.util.ArrayList;

import actions.GameAction;
import comp361.catan.GameStage;

public abstract class FishCommand extends GameCommand{

	private static final long serialVersionUID = 3794766737747761905L;
	private ArrayList<Integer> selectedTokens;

	public FishCommand(int phase) {
		super(phase);
	}
	
	public void setSelectedTokens(ArrayList<Integer> selectedTokens){
		this.selectedTokens=selectedTokens;
	}
	
	public ArrayList<Integer> getSelectedTokens(){
		return this.selectedTokens;
	}
	
}
