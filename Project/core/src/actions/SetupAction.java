package actions;

import org.json.JSONObject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import commands.PlacementCommand;
import commands.GameCommand;
import comp361.catan.Edge;
import comp361.catan.GameStage;
import comp361.catan.GameState;
import comp361.catan.Notification;
import comp361.catan.Tile;
import comp361.catan.TileType;
import comp361.catan.Vertex;
import comp361.catan.VertexConstruction;
import io.socket.client.Ack;
import io.socket.client.Socket;
import io.socket.emitter.Emitter.Listener;

/**
 * This game action handles the initial road and settlement placement at the beginning of the game.
 */
public class SetupAction extends GameAction{
	
	private Edge previousEdge;
	private Vertex previousVertex;
	private Listener catanListener;
	private ClickListener clickListener;
	private InputListener inputListener;
	private boolean blocked=false;
	private boolean first=true;
	
	public SetupAction(Notification notif, GameStage gameStage) {
		super(notif, gameStage);
		setupSocket();
		getPlayer().setTurn(getPlayer().getOrder()==1);
		if(getPlayer().isTurn())
			getToolbar().getInfoWindow().displayInfoMessage("Place your first settlement and road.");
		getMap().addListener(inputListener=new InputListener(){
			@Override
			public boolean mouseMoved(InputEvent event, float x, float y) {
				if(previousVertex!=null)
					previousVertex.selected=false;
				if(previousEdge!=null)
					previousEdge.selected=false;
				previousVertex=null;
				previousEdge=null;
				if(getPlayer().isTurn()){
					if(first){
						Vertex vertex=getMap().getCurrentVertex();
						if(vertex!=null){
							boolean validPosition=false;
							for(Tile tile:vertex.neighbors)
								validPosition|=tile.getTileType()!=TileType.OCEAN;
							for(Edge edge:vertex.edges){
								validPosition&=edge.first.getConstruction()==null;
								validPosition&=edge.second.getConstruction()==null;
							}
							if(validPosition){
								vertex.selected=true;
								previousVertex=vertex;
							}
						}
					}else{
						Edge edge=getMap().getCurrentEdge();
						if(edge!=null){
							VertexConstruction v=edge.first.getConstruction();
							if(v==null || v.getOwner()!=getPlayer())
								v=edge.second.getConstruction();
							if(v!=null && v.getOwner()==getPlayer()){
								boolean valid=true;
								for(Edge e:v.getParent().edges)
									valid&=e.getConstruction()==null;
								if(valid){
									edge.selected=true;
									previousEdge=edge;
								}
							}
						}
					}
				}
				return true;
			}
		});
		getMap().addListener(clickListener=new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if(getPlayer().isTurn() && !blocked){
					if(previousVertex!=null){
						previousVertex.selected=false;
						blocked=true;
						final PlacementCommand command=new PlacementCommand(getPlayer().getPeer().getId(), previousVertex.id, null, GameStage.GAME_STATE==GameState.FIRST_PLACEMENT);
						JSONObject obj=GameCommand.getJsonCommand(command, "placement");
						getSocket().emit("catan", obj, GameStage.ACK);
						command.execute(getStage(), SetupAction.this);
						blocked=false;
						first=false;
					}else if(previousEdge!=null){
						previousEdge.selected=false;
						blocked=true;
						final PlacementCommand command=new PlacementCommand(getPlayer().getPeer().getId(), null, previousEdge.id, GameStage.GAME_STATE==GameState.FIRST_PLACEMENT);
						JSONObject obj=GameCommand.getJsonCommand(command, "placement");
						getSocket().emit("catan", obj, GameStage.ACK);
						command.execute(getStage(), SetupAction.this);
						blocked=false;
						first=true;
					}
				}
			}
		});
	}
	
	private void setupSocket(){
		final Socket socket=getSocket();
		socket.on("catan", catanListener=new Listener(){
			@Override
			public void call(final Object... arg0) {
				Gdx.app.postRunnable(new Runnable(){
					@Override
					public void run() {
						Ack ack=(Ack)arg0[arg0.length-1];
						try{
							JSONObject obj=(JSONObject)arg0[0];
							String action=obj.getString("action");
							if(action.equals("placement")){
								PlacementCommand command=(PlacementCommand)GameCommand.stringToCommand(obj.getString("command"));
								command.execute(getStage(), SetupAction.this);
							}else
								return;
						}catch(Exception e){e.printStackTrace();};
						ack.call();
					}
				});
			}
		});
	}
	
	public void done(){
		cleanUp();
		if(getPlayer().isCurrentPlayer())
			getPlayer().getTurnInfo().newTurn();
		getNotification().actionCompleted(new RollAction(getNotification(), getStage()));
	}
	
	@Override
	public void cleanUp(){
		getSocket().off("catan", catanListener);
		getMap().removeListener(clickListener);
		getMap().removeListener(inputListener);
	}

}
