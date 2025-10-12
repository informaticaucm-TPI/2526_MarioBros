package tp1.control.commands;

import tp1.logic.Game;
import tp1.view.GameView;

public interface Command {

	public void execute(Game game, GameView view);	  
	public Command parse(String[] commandWords);

	public String helpText();
}
