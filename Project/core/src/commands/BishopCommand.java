package commands;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONObject;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import actions.CardAction;
import cards.PoliticsCard;
import comp361.catan.City;
import comp361.catan.GameStage;
import comp361.catan.Player;
import comp361.catan.Resource;
import comp361.catan.ResourceType;
import comp361.catan.Settlement;
import comp361.catan.Skins;
import comp361.catan.Tile;
import comp361.catan.TileType;
import comp361.catan.Vertex;

public class BishopCommand extends GameCommand{

	private static final long serialVersionUID = -9132744140558239036L;
	private String playerID;
	private transient ClickListener mapListener;
	private transient Player player;
	private int robberID;
	private HashMap<String, ResourceType> stolen;

	public BishopCommand(String playerID, int phase) {
		super(phase);
		this.playerID=playerID;
	}
	
	public void setRobber(int robberID, HashMap<String, ResourceType> stolen){
		this.robberID=robberID;
		this.stolen=stolen;
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
		Tile tile=getMap().getTile(robberID);
		getMap().getRobber().setLocation(tile);
		String message="Player "+player.getPeer().getName()+" has moved the robber with the bishop progress card.";
		String msgStolen="He has stolen a random resource/commodity from ";
		int count=0;
		for(String pid:stolen.keySet()){
			count++;
			ResourceType type=stolen.get(pid);
			Player p=getPlayers().get(pid);
			p.getResources().put(type, p.getResources().get(type)-1);
			player.getResources().put(type, player.getResources().get(type)+1);
			msgStolen+=p.getPeer().getName();
			if(count!=stolen.size())
				msgStolen+=", ";
		}
		if(count==0)
			msgStolen="He has not stolen any resource/commodity";
		getToolbar().getChatWindow().log(message+msgStolen);
		player.getCards().remove(PoliticsCard.BISHOP);
		getCards().add(PoliticsCard.BISHOP);
		((CardAction)getAction()).cancelProgressCard();
	}
	
	private void phase1(){
		getToolbar().getInfoWindow().displayInfoMessage("Choose where to move the robber");
		getMap().addListener(mapListener=new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Tile tile=getMap().getCurrentTile();
				if(tile!=null && tile.getTileType()!=TileType.OCEAN && getMap().getRobber().getLocation()!=tile){
					HashMap<String, ResourceType> stolen=new HashMap<String, ResourceType>();
					for(Vertex vertex:tile.vertices){
						if((vertex.getConstruction() instanceof Settlement || vertex.getConstruction() instanceof City) && vertex.getConstruction().getOwner()!=getPlayer()){
							
							Player p=vertex.getConstruction().getOwner();
							if(stolen.get(p.getPeer().getId())==null && Resource.calculateTotalResourcesCommodities(p)>0){
								ArrayList<ResourceType> owned=new ArrayList<ResourceType>();
								for(ResourceType type:p.getResources().keySet()){
									for(int i=0;i<p.getResources().get(type);i++){
										owned.add(type);
									}
								}
								ResourceType random=owned.get((int)(Math.random()*owned.size()));
								stolen.put(p.getPeer().getId(), random);
							}
							
						}
					}
					
					getToolbar().getInfoWindow().clearMessage();
					getMap().removeListener(mapListener);
					BishopCommand command=new BishopCommand(playerID, 2);
					command.setRobber(tile.id, stolen);
					JSONObject obj=GameCommand.getJsonCommand(command, "progress_card");
					getSocket().emit("catan", obj, GameStage.ACK);
					command.execute(getStage(), getAction());
				}
			}
		});
	}

}
