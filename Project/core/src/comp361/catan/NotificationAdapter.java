package comp361.catan;

import actions.GameAction;
import io.socket.client.Socket;

/**
 * Adapter for the notification listener, to avoid having to define all methods
 */
public class NotificationAdapter implements Notification{
	
	@Override
	public void enterMenu() {

	}
	
	@Override
	public void lobbyJoined(Peer peer) {

	}

	@Override
	public void gameStarted(Game game, Socket socket, Peer myPeer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void editorStarted() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void actionCompleted(GameAction nextAction) {
		// TODO Auto-generated method stub
		
	}

}
