package comp361.catan;

import actions.GameAction;
import io.socket.client.Socket;

/**
 * Notification is essentially a listener for some custom events. Calling the methods notifies
 * the notification creator that an event has occured.
 */
public interface Notification {
	
	public void editorStarted();
	public void enterMenu();
	public void lobbyJoined(Peer peer);
	public void gameStarted(Game game, Socket socket, Peer myPeer);
	public void actionCompleted(GameAction nextAction);
	
}
