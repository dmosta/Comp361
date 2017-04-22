package comp361.catan;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import io.socket.client.Socket;
import util.CardUtility;
import util.ColoredTexture;

/**
 * This is the main class that launches the application. Initially it sets Menu as the active
 * stage, and changes the current stage to different ones (lobby, editor, gamestage) when it receives
 * notifications
 */
public class CatanLauncher extends ApplicationAdapter {
	
	public static String SERVER="http://localhost:3000";
	public static final boolean DEBUG=true;
	private Stage currentStage;
	private Menu menu;
	private Editor editor;
	private Lobby lobby;
	private GameStage gameStage;
	
	@Override
	public void create () {
		ColoredTexture.initialize();
		CardUtility.loadCards();
			menu=new Menu(new NotificationAdapter(){
			@Override
			public void editorStarted() {
				editor=new Editor(new NotificationAdapter(){
					@Override
					public void enterMenu() {
						loadMenu();
					}
				});
				Gdx.input.setInputProcessor(editor);
				currentStage=editor;
			}
			@Override
			public void lobbyJoined(Peer peer) {
				lobby=new Lobby(new NotificationAdapter(){
					@Override
					public void enterMenu() {
						loadMenu();
					}
					@Override
					public void gameStarted(Game game, Socket socket, Peer myPeer) {
						setGameStage(game, socket, myPeer);
					}
				}, peer);
				Gdx.input.setInputProcessor(lobby);
				currentStage=lobby;
			}
		});
		currentStage=menu;
		Gdx.input.setInputProcessor(menu);
	}
	
	public void setGameStage(Game game, Socket socket, Peer myPeer){
		gameStage=new GameStage(game, socket, new NotificationAdapter(){
			public void enterMenu() {
				loadMenu();
			};
			// This is for reloading games without having to quit and recreate it.
			@Override
			public void gameStarted(Game game, Socket socket, Peer myPeer) {
				setGameStage(game, socket, myPeer);
			}
		}, myPeer);
		currentStage=gameStage;
		Gdx.input.setInputProcessor(gameStage);
	}
	
	private void loadMenu(){
		if(currentStage instanceof GameStage){
			currentStage.dispose();
			gameStage=null;
		}
		Gdx.input.setInputProcessor(menu);
		currentStage=menu;
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		if(currentStage!=null){
			currentStage.act(Gdx.graphics.getDeltaTime());
			currentStage.draw();
		}
	}
	
	public void resize (int width, int height) {
		if(currentStage!=null)
			currentStage.getViewport().update(width, height, true);
	}
	
	@Override
	public void dispose () {
		menu.dispose();
		if(editor!=null)
			editor.dispose();
		if(lobby!=null)
			lobby.dispose();
		if(gameStage!=null)
			gameStage.dispose();
	}
}

