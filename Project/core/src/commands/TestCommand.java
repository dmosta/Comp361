package commands;

import java.util.ArrayList;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;

import cards.Card;
import cards.PoliticsCard;
import cards.ScienceCard;
import comp361.catan.FishToken;
import comp361.catan.GameStage;
import comp361.catan.Player;
import comp361.catan.ResourceType;
import comp361.catan.Skins;
import comp361.catan.Tile;
import comp361.catan.TileType;

public class TestCommand extends GameCommand{
	
	private static final long serialVersionUID = 1192146660414165190L;
	private String playerID;
	private int keycode;
	private Card card;
	
	public TestCommand(String playerID, int keycode){
		super(1);
		this.playerID=playerID;
		this.keycode=keycode;
	}
	
	public TestCommand(String playerID, Card card){
		super(1);
		this.playerID=playerID;
		this.card=card;
	}
	
	@Override
	protected void executeCommand() {
		Player player=getPlayers().get(playerID);
		if(keycode==Input.Keys.P){
			for(ResourceType key:player.getResources().keySet())
				player.getResources().put(key, 30);
			/*
			for(FishToken token:getMap().getFishToken()){
				if(token.isBoot()){
					player.getFishToken().add(token);
					player.setHasBoot(true);
					getMap().getFishToken().remove(token);
					break;
				}
			}
			for(Player p:getPlayers().values())
				for(ResourceType type:p.getResources().keySet())
					p.getResources().put(type, 10);
					
			*/
		}else if(card!=null){
			if(player.getCards().size()<4){
				for(Card c:getCards()){
					if(c==card){
						player.getCards().add(card);
						getCards().remove(card);
						break;
					}
				}
			}
		}else if(keycode==Input.Keys.I){
			for(ResourceType key:player.getResources().keySet())
				player.getResources().put(key, 0);
		}else if(keycode==Input.Keys.V){
			player.setVictoryPoints(player.getVictoryPoints()+1);
		}else if(keycode==Input.Keys.K){
			for(ResourceType key:player.getResources().keySet())
				player.getResources().put(key, 0);
			player.getResources().put(ResourceType.WOOL, 1);
		}else if(keycode==Input.Keys.B){
			getMap().setBarbarianPosition(6);
		}else if(keycode==Input.Keys.N){
			getMap().setBarbarianPosition(0);
		}else if(keycode==Input.Keys.F){
			int i=0;
			ArrayList<FishToken> tokens=new ArrayList<FishToken>();
			for(FishToken token:getMap().getFishToken()){
				player.getFishToken().add(token);
				if(token.isBoot())
					player.setHasBoot(true);
				tokens.add(token);
				i++;
				if(i==7)
					break;
			}
			getMap().getFishToken().removeAll(tokens);
		}else if(keycode==Input.Keys.G){
			for(FishToken token:player.getFishToken()){
				getMap().getFishToken().add(token);
				if(token.isBoot())
					player.setHasBoot(false);
			}
			player.getFishToken().clear();
		}else if(keycode==Input.Keys.L){
			for(Tile tile:getMap().getTiles().values()){
				if(tile.getTileType()==TileType.OCEAN){
					getMap().getPirate().setLocation(tile);
					break;
				}
			}
		}else if(keycode==Input.Keys.T){
			player.setGold(50);
		}else if(keycode==Input.Keys.R){
			getSocket().emit("reset_game");
		}
	}

}
