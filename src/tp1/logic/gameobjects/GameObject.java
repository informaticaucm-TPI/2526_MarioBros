package tp1.logic.gameobjects;

import tp1.logic.Action;
import tp1.logic.Game;
import tp1.logic.Position;

public abstract class GameObject { // TODO 

	protected Position pos; // If you can, make it private.
	private boolean isAlive;
	protected Game game; 
	
	public GameObject(Game game, Position pos) {
		this.isAlive = true;
		this.pos = pos;
		this.game = game;
	}
	
	public boolean isInPosition(Position p) {
		// TODO fill your code here, it should depends on the status of the object
		return false;
	}
 	
	public boolean isAlive() {
		return isAlive;
	}
	
	public void dead(){
		this.isAlive = false;
	}
	
	// TODO implement and decide, Which one is abstract?
	// public boolean isSolid()
	// public void update()
	
	public abstract String getIcon();

	// Not mandatory but recommended
	protected void move(Action dir) {
		// TODO Auto-generated method stub
	}
}
