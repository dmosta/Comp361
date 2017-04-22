package commands;

import java.util.HashMap;

import org.json.JSONObject;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import actions.FishAction;
import cards.Card;
import cards.PoliticsCard;
import cards.ScienceCard;
import comp361.catan.GameStage;
import comp361.catan.Player;
import comp361.catan.Skins;

public class FishProgressCommand extends FishCommand{

	private static final long serialVersionUID = -2172970496286335954L;
	private Card card;
	private String playerID;
	private transient Player player;

	public FishProgressCommand(String playerID, int phase) {
		super(phase);
		this.playerID=playerID;
	}
	
	public void setCard(Card card){
		this.card=card;
	}

	@Override
	protected void executeCommand() {
		player=getPlayers().get(playerID);
		if(getPhase()==1){
			phase1();
		}else if(getPhase()==2){
			phase2();
		}
	}
	
	private void phase2(){
		getToolbar().getChatWindow().log("Player "+player.getPeer().getName()+" has purchased a progress card with his fish token");
		if(card==ScienceCard.PRINTER || card==PoliticsCard.CONSTITUTION){
			getToolbar().getChatWindow().log("Player "+player.getPeer().getName()+" has received 1 VP for the "+card+" progress card");
			player.setVictoryPoints(player.getVictoryPoints()+1);
		}else{
			player.getCards().add(card);
			getCards().remove(card);
		}
		((FishAction)getAction()).removeSelectedTokens(getSelectedTokens(), player);
	}
	
	private void phase1(){
		final Dialog progressDialog=new Dialog("Choose progress card", Skins.METAL);
		Label label=new Label("(Scroll to see more)", Skins.METAL);
		progressDialog.getContentTable().add(label);
		progressDialog.getContentTable().row();
		Table buttonTable=new Table();
		ScrollPane scrollPane=new ScrollPane(buttonTable);
		progressDialog.getContentTable().add(scrollPane).width(200).height(250);
		final HashMap<Card, TextButton> buttons=new HashMap<Card, TextButton>();
		for(Card card:getCards()){
			if(!buttons.containsKey(card)){
				TextButton button=new TextButton(card+"", Skins.METAL);
				buttons.put(card, button);
				buttonTable.add(button);
				buttonTable.row();
			}
		}
		progressDialog.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				for(Card card:buttons.keySet()){
					if(event.getTarget().isDescendantOf(buttons.get(card))){
						FishProgressCommand command=new FishProgressCommand(playerID, 2);
						command.setSelectedTokens(getSelectedTokens());
						command.setCard(card);
						JSONObject obj=GameCommand.getJsonCommand(command, "fish");
						getSocket().emit("catan", obj, GameStage.ACK);
						command.execute(getStage(), getAction());
						progressDialog.hide();
						break;
					}
				}
			}
		});
		progressDialog.show(getStage());
	}

}
