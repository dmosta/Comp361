package commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.json.JSONObject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import actions.RollAction;
import algorithms.Algorithms;
import cards.Card;
import cards.PoliticsCard;
import cards.ScienceCard;
import cards.TradeCard;
import comp361.catan.City;
import comp361.catan.FishToken;
import comp361.catan.FishingGround;
import comp361.catan.GameStage;
import comp361.catan.Knight;
import comp361.catan.Map;
import comp361.catan.Player;
import comp361.catan.Resource;
import comp361.catan.ResourceType;
import comp361.catan.Settlement;
import comp361.catan.Skins;
import comp361.catan.Tile;
import comp361.catan.TileType;
import comp361.catan.Vertex;
import comp361.catan.VertexConstruction;

public class RollCommand extends GameCommand{

	private static final long serialVersionUID = -6553911987833559464L;
	private int redDie;
	private int yellowDie;
	private String eventDie;
	private String playerID, targetID;
	private int outcome;
	private HashMap<String, Integer> cards;
	private transient RollAction action;
	private ClickListener mapListener;
	private transient Player player, target;
	private int cityID;
	private ArrayList<String> targetPlayers;
	private Card chosenCard;
	private HashMap<String, ArrayList<Integer>> fish;
	private String bootTarget;
	
	public RollCommand(String playerID, int redDie, int yellowDie, String eventDie, HashMap<String, Integer> cards, String bootTarget, int phase){
		super(phase);
		this.redDie=redDie;
		this.yellowDie=yellowDie;
		this.eventDie=eventDie;
		this.cards=cards;
		this.playerID=playerID;
		outcome=redDie+yellowDie;
		this.bootTarget=bootTarget;
	}
	
	public void setFish(HashMap<String, ArrayList<Integer>> fish){
		this.fish=fish;
	}
	
	public void setChosenCard(Card card){
		this.chosenCard=card;
	}
	
	public void setTargetPlayers(ArrayList<String> players){
		this.targetPlayers=players;
	}
	
	public void setCity(int cityID){
		this.cityID=cityID;
	}
	
	public void setTarget(String targetID){
		this.targetID=targetID;
	}
	
	@Override
	protected void executeCommand() {
		this.action=(RollAction)getAction();
		player=getPlayers().get(playerID);
		if(targetID!=null)
			target=getPlayers().get(targetID);
		
		if(getPhase()==0){//This gets called after all the events have been resolved
			generateRessources();
		}else if(getPhase()==1){//Resolve the event die
			String message="Player "+player.getPeer().getName()+" rolled "+redDie+" on the red die, "+yellowDie+" on the yellow die and "+eventDie+" on the event die.";
			getToolbar().getChatWindow().log(message);
			if(eventDie.equals("barbarian")){
				getMap().setBarbarianPosition(getMap().getBarbarianPosition()+1);
				if(getMap().getBarbarianPosition()==7){
					getMap().setFirstBarbarianAttack(true);
					getMap().setBarbarianPosition(0);
					if(player==getPlayer())
						barbarianAttack();
				}else if(player==getPlayer())
					resolveRobber();
				if(getMap().getBarbarianPosition()==6){
					Dialog diag=new Dialog("", Skins.METAL).button("ok");
					diag.getContentTable().add(new Label("The barbarians will arrive soon!", Skins.METAL));
					diag.getContentTable().row();
					Image img=new Image(new Texture(Gdx.files.internal("ui/barbarians.png")));
					float ratio=img.getWidth()/img.getHeight();
					diag.getContentTable().add(img).height(400).width(ratio*400);
					diag.show(getStage());
				}
			}else if(player==getPlayer())
				resolveRobber();
		}else if(getPhase()==2){//Choose which city to discard after barbarian attack
			deactivateKnights();
			for(String loserID:targetPlayers){
				Player loser=getPlayers().get(loserID);
				if(loser==getPlayer()){
					chooseCity();
					break;
				}
			}
			Dialog diag=new Dialog("", Skins.METAL).button("ok");
			Image img=new Image(new Texture(Gdx.files.internal("ui/lost.png")));
			float ratio=img.getWidth()/img.getHeight();
			diag.getContentTable().add(new Label("The barbarians have won", Skins.METAL));
			diag.getContentTable().row();
			diag.getContentTable().add(img).height(400).width(400*ratio);
			diag.show(getStage());
			if(targetPlayers.size()==0 && player==getPlayer())
				barbarianDone();
		}else if(getPhase()==3){//Remove cities that were chosen
			lostCity();
		}else if(getPhase()==4){//Receive defender Vp for best contributer
			receiveDefenderVP();
			Dialog diag=new Dialog("", Skins.METAL).button("ok");
			diag.getContentTable().add(new Label("The defenders have won against the barbarians!", Skins.METAL));
			diag.getContentTable().row();
			Image img=new Image(new Texture(Gdx.files.internal("ui/win.png")));
			float ratio=img.getWidth()/img.getHeight();
			diag.getContentTable().add(img).height(400).width(ratio*400);
			diag.show(getStage());
			deactivateKnights();
		}else if(getPhase()==5){//initiate the selection of progress cards for best contributors
			String message="The following defenders have contributed the most: ";
			ArrayList<String> ignoredPlayers=new ArrayList<String>();
			for(int i=0;i<targetPlayers.size();i++){
				Player p=getPlayers().get(targetPlayers.get(i));
				String str=p.getPeer().getName();
				message+=str;
				if(i<targetPlayers.size()-1)
					message+=", ";
				else message+=".";
				if(p.getCards().size()==4)
					ignoredPlayers.add(targetPlayers.get(i));
			}
			for(String ignored:ignoredPlayers)
				targetPlayers.remove(ignored);
			getToolbar().getChatWindow().log(message);
			if(targetPlayers.size()==0){
				RollCommand command=new RollCommand(playerID, redDie, yellowDie, eventDie, cards, bootTarget, 6);
				command.setTargetPlayers(targetPlayers);
				JSONObject obj=GameCommand.getJsonCommand(command, "roll");
				getSocket().emit("catan", obj, GameStage.ACK);
				command.execute(getStage(), getAction());
			}else{
				Player p=getPlayers().get(targetPlayers.get(0));
				if(p==getPlayer())
					chooseProgressCard();
			}
			Dialog diag=new Dialog("", Skins.METAL).button("ok");
			diag.getContentTable().add(new Label("The defenders have won against the barfbarians!", Skins.METAL));
			diag.getContentTable().row();
			Image img=new Image(new Texture(Gdx.files.internal("ui/win.png")));
			float ratio=img.getWidth()/img.getHeight();
			diag.getContentTable().add(img).height(400).width(ratio*400);
			diag.show(getStage());
			deactivateKnights();
		}else if(getPhase()==6){//received a selected progress card from a contributer
			if(targetID!=null){
				getCards().remove(chosenCard);
				target.getCards().add(chosenCard);
				String discipline="politics";
				if(chosenCard instanceof TradeCard)
					discipline="trade";
				if(chosenCard instanceof ScienceCard)
					discipline="science";
				getToolbar().getChatWindow().log("Player "+target.getPeer().getName()+" has received a "+discipline+" progress card "+
					"because he was a top contributer in the defense from the barbarians.");
			}
			if(targetPlayers.size()>0){
				Player p=getPlayers().get(targetPlayers.get(0));
				if(p==getPlayer())
					chooseProgressCard();
			}else if(targetPlayers.size()==0 && player==getPlayer()){
				barbarianDone();
			}
		}else if(getPhase()==7 && player==getPlayer()){
			resolveDie();
		}
	}
	
	private void chooseProgressCard(){
		final Dialog chooseDialog=new Dialog("Choose a card.", Skins.METAL);
		final TextButton buttonPolitics=new TextButton("Politics", Skins.METAL);
		final TextButton buttonTrade=new TextButton("Trade", Skins.METAL);
		final TextButton buttonScience=new TextButton("Science", Skins.METAL);
		chooseDialog.getContentTable().add(buttonPolitics);
		chooseDialog.getContentTable().row();
		chooseDialog.getContentTable().add(buttonTrade);
		chooseDialog.getContentTable().row();
		chooseDialog.getContentTable().add(buttonScience);
		chooseDialog.getContentTable().row();
		chooseDialog.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Class cardType=null;
				if(event.getTarget().isDescendantOf(buttonPolitics))
					cardType=PoliticsCard.class;
				if(event.getTarget().isDescendantOf(buttonTrade))
					cardType=TradeCard.class;
				if(event.getTarget().isDescendantOf(buttonScience))
					cardType=ScienceCard.class;
				targetPlayers.remove(getPlayer().getPeer().getId());
				if(cardType!=null){
					for(Card card:getCards()){
						if(cardType.isInstance(card)){
							chooseDialog.hide();
							RollCommand command=new RollCommand(playerID, redDie, yellowDie, eventDie, cards, bootTarget, 6);
							command.setTargetPlayers(targetPlayers);
							command.setTarget(getPlayer().getPeer().getId());
							command.setChosenCard(card);
							JSONObject obj=GameCommand.getJsonCommand(command, "roll");
							getSocket().emit("catan", obj, GameStage.ACK);
							command.execute(getStage(), getAction());
							break;
						}
					}
				}
			}
		});
		chooseDialog.show(getStage());
	}
	
	private void deactivateKnights(){
		for(Vertex vertex:getMap().getVertices().values()){
			if(vertex.getConstruction() instanceof Knight){
				Knight knight=(Knight)vertex.getConstruction();
				knight.setActive(false);
			}
		}
	}
	
	private void receiveDefenderVP(){
		String message="Player "+target.getPeer().getName()+" has contributed the most so the defense. ";
		if(getMap().getDefenderVPRemaining()>0){
			target.setVictoryPoints(target.getVictoryPoints()+1);
			getMap().setDefenderVPRemaining(getMap().getDefenderVPRemaining()-1);
			message+="He has received a defender of catan victory point card.";
		}else{
			message+="He has not received a defender of catan victory point card since they have all been taken.";
		}
		getToolbar().getChatWindow().log(message);
		if(player==getPlayer())
			barbarianDone();
	}
	
	private void lostCity(){
		Vertex vertex=getMap().getVertex(cityID);
		City city=(City)vertex.getConstruction();
		String message="";
		if(city.hasWall()){
			city.setWall(false);
			target.setNumWalls(target.getNumWalls()+1);
			message="Player "+target.getPeer().getName()+" has lost one of his city walls because of the barbarian attack.";
		}else{
			vertex.setConstruction(null);
			target.getVertexContructions().remove(city);
			target.setCitiesRemaining(target.getCitiesRemaining()+1);
			if(target.getSettlementsRemaining()>0){
				target.setVictoryPoints(target.getVictoryPoints()-1);
				Settlement settlement=new Settlement(vertex, target);
				vertex.setConstruction(settlement);
				message="Player "+target.getPeer().getName()+" has downgraded one of his cities to a settlement because of the barbarian attack.";
			}else{
				target.setVictoryPoints(target.getVictoryPoints()-2);
				message="Player "+target.getPeer().getName()+" has lost one of his cities because of the barbarian attack.";
				Algorithms.updateLongestRoad(getMap(), getPlayers());
			}
		}
		getToolbar().getChatWindow().log(message);
		if(player==getPlayer()){
			int requests=(int)GameStage.STORE.get("requests");
			int answers=(int)GameStage.STORE.get("answers");
			answers++;
			GameStage.STORE.put("answers", answers);
			if(answers==requests){
				barbarianDone();
			}
		}
	}
	
	private void chooseCity(){
		getToolbar().getInfoWindow().displayInfoMessage("You have contributed the least to the defense from the barbarians. Select a city to lose.");
		getMap().addListener(mapListener=new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Vertex vertex=getMap().getCurrentVertex();
				if(vertex!=null && vertex.getConstruction() instanceof City && vertex.getConstruction().getOwner()==getPlayer()){
					City city=(City)vertex.getConstruction();
					if(getMap().getTradeMetropolis().getCity()!=city && getMap().getScienceMetropolis().getCity()!=city && getMap().getPoliticsMetropolis().getCity()!=city){
						getToolbar().getInfoWindow().clearMessage();
						getMap().removeListener(mapListener);
						RollCommand command=new RollCommand(playerID, redDie, yellowDie, eventDie, cards, bootTarget, 3);
						command.setTarget(getPlayer().getPeer().getId());
						command.setCity(vertex.id);
						JSONObject obj=GameCommand.getJsonCommand(command, "roll");
						getSocket().emit("catan", obj, GameStage.ACK);
						command.execute(getStage(),  getAction());
					}
				}
			}
		});
	}
	
	private void barbarianAttack(){
		int defenderStrength=0;
		int barbarianStrength=0;
		int minContribution=Integer.MAX_VALUE;
		int maxContribution=Integer.MIN_VALUE;
		HashMap<Player, Integer> contributions=new HashMap<Player, Integer>();
		for(Player p:getPlayers().values()){
			int contribution=0;
			boolean hasCity=false;
			for(VertexConstruction cons:p.getVertexContructions()){
				if(cons instanceof Knight){
					Knight knight=(Knight)cons;
					if(knight.isActive()){
						contribution+=knight.getLevel();
						defenderStrength+=knight.getLevel();
					}
				}else if(cons instanceof City){
					barbarianStrength+=1;
					if(getMap().getTradeMetropolis().getCity()!=cons && getMap().getScienceMetropolis().getCity()!=cons && getMap().getPoliticsMetropolis().getCity()!=cons)
						hasCity=true;
				}
			};
			if(contribution<minContribution && hasCity)
				minContribution=contribution;
			if(contribution>maxContribution)
				maxContribution=contribution;
			if(hasCity)
				contributions.put(p, contribution);
			else contributions.put(p, Integer.MAX_VALUE);
		}
		if(barbarianStrength>defenderStrength){
			ArrayList<String> losers=new ArrayList<String>();
			for(Player p:getPlayers().values()){
				int contribution=contributions.get(p);
				if(contribution==minContribution){
					losers.add(p.getPeer().getId());
				}
			}
			GameStage.STORE.put("requests", losers.size());
			GameStage.STORE.put("answers", 0);
			RollCommand command=new RollCommand(playerID, redDie, yellowDie, eventDie, cards, bootTarget, 2);
			command.setTargetPlayers(losers);
			JSONObject obj=GameCommand.getJsonCommand(command, "roll");
			getSocket().emit("catan", obj, GameStage.ACK);
			command.execute(getStage(), getAction());
		}else{
			int numMaxContributions=0;
			String cont[]=new String[6];
			ArrayList<String> bestContributers=new ArrayList<String>();
			for(Player p:contributions.keySet()){
				int contribution=contributions.get(p);
				if(contribution==maxContribution){
					numMaxContributions++;
					cont[p.getOrder()-1]=p.getPeer().getId();
				}
			}
			// Order the players in clockwise order
			for(int i=0;i<cont.length;i++){
				int index=(i+(getPlayer().getOrder()-1))%cont.length;
				if(cont[index]!=null)
					bestContributers.add(cont[index]);
			}
			if(numMaxContributions==1){
				RollCommand command=new RollCommand(playerID, redDie, yellowDie, eventDie, cards, bootTarget, 4);
				command.setTarget(bestContributers.get(0));
				JSONObject obj=GameCommand.getJsonCommand(command, "roll");
				getSocket().emit("catan", obj, GameStage.ACK);
				command.execute(getStage(), getAction());
			}else{
				RollCommand command=new RollCommand(playerID, redDie, yellowDie, eventDie, cards, bootTarget, 5);
				command.setTargetPlayers(bestContributers);
				JSONObject obj=GameCommand.getJsonCommand(command, "roll");
				getSocket().emit("catan", obj, GameStage.ACK);
				command.execute(getStage(), getAction());
			}
		}
	}
	
	private void barbarianDone(){
		getToolbar().getInfoWindow().displayInfoMessage("The barbarian attack is over");
		resolveRobber();
	}
	
	private int nextCard(int after, String discipline){
		for(int i=after+1;i<=getStage().getCards().size();i++){
			Card card=getStage().getCards().get(i);
			if(discipline.equals("politics") && card instanceof PoliticsCard)
				return i;
			else if(discipline.equals("trade") && card instanceof TradeCard)
				return i;
			else if(discipline.equals("science") && card instanceof ScienceCard)
				return i;
		}
		return -1;
	}
	
	private void resolveRobber(){
		if((redDie+yellowDie)==7){
			ArrayList<String> remaining=new ArrayList<String>();
			for(Player p:getPlayers().values()){
				if(Resource.calculateTotalResourcesCommodities(p)>7)
					remaining.add(p.getPeer().getId());
			}
			GameStage.STORE.put("robberRemaining", remaining);
			RobberCommand command=new RobberCommand(playerID, 1);
			command.setDie(redDie, yellowDie, eventDie, bootTarget);
			JSONObject obj=GameCommand.getJsonCommand(command, "roll");
			getSocket().emit("catan", obj, GameStage.ACK);
			command.execute(getStage(), getAction());
		}else resolveDie();
	}
	
	private void resolveDie(){
		int index=-1;
		final HashMap<String, Integer> playerCards=new HashMap<String, Integer>();
		for(Player player:getStage().getPlayers().values()){
			playerCards.put(player.getPeer().getId(), -1);
			if(!eventDie.equals("barbarian")){
				int indexBefore=index;
				if(player.getCards().size()<4){
					if(eventDie.equals("politics") && redDie<=player.getPoliticsLevel())
						index=nextCard(index, "politics");
					else if(eventDie.equals("trade") && redDie<=player.getTradeLevel())
						index=nextCard(index, "trade");
					else if(eventDie.equals("science") && redDie<=player.getScienceLevel())
						index=nextCard(index, "science");
				}
				if(index!=indexBefore)
					playerCards.put(player.getPeer().getId(), index);
			}
		}
		int numFish=0;
		HashMap<Player, Integer> playerFish=new HashMap<Player, Integer>();
		HashMap<String, ArrayList<Integer>> fishIndex=new HashMap<String, ArrayList<Integer>>();
		for(Player p:getPlayers().values()){
			playerFish.put(p, 0);
			fishIndex.put(p.getPeer().getId(), new ArrayList<Integer>(2));
		}

		if(outcome==2 || outcome==3 || outcome==11 || outcome==12){
			for(Tile tile:getMap().getTiles().values()){
				if(tile.getTileType()==TileType.LAKE){
					for(Vertex vertex:tile.vertices){
						int total=0;
						if(vertex.getConstruction() instanceof Settlement)
							total=1;
						else if(vertex.getConstruction() instanceof City)
							total=2;
						if(total!=0){
							Player p=vertex.getConstruction().getOwner();
							if(playerFish.get(p)==0){
								playerFish.put(p, total);
								numFish+=total;
							}
						}
					}
					break;
				}
			}
		}else{
			for(FishingGround ground:getMap().getFishingGrounds().values()){
				if(ground.getRoll()==outcome){
					for(Vertex vertex:ground.vertices){
						int total=0;
						if(vertex.getConstruction() instanceof Settlement)
							total=1;
						else if(vertex.getConstruction() instanceof City)
							total=2;
						if(total!=0){
							Player p=vertex.getConstruction().getOwner();
							if(playerFish.get(p)==0){
								playerFish.put(p, total);
								numFish+=total;
							}
						}
					}
				}
				break;
			}
		}
		ArrayList<FishToken> copy=new ArrayList<FishToken>();
		copy.addAll(getMap().getFishToken());
		if(numFish<=getMap().getFishToken().size()){
			for(Player p:playerFish.keySet()){
				for(int i=0;i<playerFish.get(p);i++){
					FishToken token=copy.remove((int)(Math.random()*copy.size()));
					fishIndex.get(p.getPeer().getId()).add(getMap().getFishToken().indexOf(token));
				}
			}
		}
		RollCommand command=new RollCommand(playerID, redDie, yellowDie, eventDie, playerCards, bootTarget, 0);
		command.setFish(fishIndex);
		JSONObject obj=GameCommand.getJsonCommand(command, "roll");
		getSocket().emit("catan",  obj, GameStage.ACK);
		command.execute(getStage(), getAction());
	}
	
	private void generateRessources(){
		getToolbar().getInfoWindow().clearMessage();
		Map map=getMap();
		ArrayList<Player> receivers=new ArrayList<Player>();
		if(outcome!=7){
			for(Tile tile:map.getTiles().values()){
				if(tile.getTileNumber()==outcome && getMap().getRobber().getLocation()!=tile && getMap().getPirate().getLocation()!=tile){
					ResourceType resType=Resource.resourceFromTile(tile.getTileType());
					ResourceType comType=Resource.commodityFromTile(tile.getTileType());
					if(resType!=null || tile.getTileType()==TileType.GOLD){
						for(Vertex vertex:tile.vertices){
							if(vertex.getConstruction() instanceof City || vertex.getConstruction() instanceof Settlement){
								Player owner=vertex.getConstruction().getOwner();
								if(!receivers.contains(owner))
									receivers.add(owner);
								if(tile.getTileType()==TileType.GOLD){
									if(vertex.getConstruction() instanceof City)
										owner.setGold(owner.getGold()+4);
									else
										owner.setGold(owner.getGold()+2);
								}else{
									int numRes=owner.getResources().get(resType);
									int numCom=owner.getResources().get(comType);
									owner.getResources().put(resType, numRes+1);
									if(vertex.getConstruction() instanceof City)
										owner.getResources().put(comType,  numCom+1);
								}
							}
						}
					}
				}
			}
		}
		if(!bootTarget.equals("")){
			for(FishToken token:player.getFishToken()){
				if(token.isBoot()){
					Player other=getPlayers().get(bootTarget);
					player.setHasBoot(false);
					player.getFishToken().remove(token);
					other.getFishToken().add(token);
					other.setHasBoot(true);
					getToolbar().getChatWindow().log("Player "+player.getPeer().getName()+" has given the old boot to "+other.getPeer().getName());
					break;
				}
			}
		}
		ArrayList<Integer> removedCards=new ArrayList<Integer>();
		for(String otherID:cards.keySet()){
			Player other=getPlayers().get(otherID);
			int index=cards.get(otherID);
			if(index!=-1){
				Card card=getCards().get(index);
				removedCards.add(index);
				if(card==ScienceCard.PRINTER || card==PoliticsCard.CONSTITUTION){
					other.setVictoryPoints(other.getVictoryPoints()+1);
					String type=card==ScienceCard.PRINTER?"printer":"constitution";
					getToolbar().getChatWindow().log("Player "+other.getPeer().getName()+" has received 1 victory point for the "+type+" card");
				}else{
					getToolbar().getChatWindow().log("Player "+other.getPeer().getName()+" has received a progress card.");
					other.getCards().add(card);
				}
			}
		}
		Collections.sort(removedCards);
		for(int i=removedCards.size()-1;i>=0;i--){
			int index=removedCards.get(i);
			getCards().remove(index);
		}
		String message="";
		if(receivers.size()>0){
			message="The following players have received resources/commodities/gold from the dice roll: ";
			for(int i=0;i<receivers.size();i++){
				message+=receivers.get(i).getPeer().getName();
				if(i<receivers.size()-1)
					message+=", ";
				else message+=".";
			}
		}else{
			message="No players have received resources/commodities from the dice roll";
		}
		ArrayList<FishToken> removedFish=new ArrayList<FishToken>();
		String fishMessage="";
		for(String pid:fish.keySet()){
			Player p=getPlayers().get(pid);
			ArrayList<Integer> indices=fish.get(pid);
			for(int index:indices){
				FishToken token=getMap().getFishToken().get(index);
				if(token.isBoot())
					p.setHasBoot(true);
				p.getFishToken().add(token);
				removedFish.add(token);
			}
			if(indices.size()>0)
				message+="Player "+p.getPeer().getName()+" has received "+indices.size()+" fish tokens.";
		}
		if(fishMessage.equals(""))
			fishMessage="No players have received fish.";

		for(FishToken token:removedFish)
			getMap().getFishToken().remove(token);

		getToolbar().getChatWindow().log(message);
		getToolbar().getChatWindow().log(fishMessage);
		action.done();
	}
}
