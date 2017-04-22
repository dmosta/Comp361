package comp361.catan;

/**
 * A peer represents a remote connection with another player. This player has a unique id
 * that can be used to send him messages with the socket.
 */
public class Peer {
	private String name;
	private String id;
	private String fingerprint;
	private boolean marked=false;//Used when restoring players from a saved games
	private int slot=0;
	
	public Peer(String name, String id, String fingerprint){
		this.name=name;
		this.id=id;
		this.fingerprint=fingerprint;
	}
	
	public String getName(){
		return this.name;
	}
	
	public String getId(){
		return this.id;
	}
	
	public void setId(String id){
		this.id=id;
	}
	
	public String getFingerprint(){
		return this.fingerprint;
	}
	
	public void setMarked(boolean marked){
		this.marked=marked;
	}
	
	public boolean isMarked(){
		return this.marked;
	}
	
	public void setSlot(int slot){
		this.slot=slot;
	}
	
	public int getSlot(){
		return this.slot;
	}
}
