package tp1.view;

import tp1.logic.Game;

public abstract class GameView implements ViewInterface{

	protected Game game;
	
	public GameView(Game game) {
		this.game = game;
	}
	
}
