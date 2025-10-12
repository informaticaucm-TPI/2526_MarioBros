package tp1.control;

import tp1.control.commands.Command;
import tp1.control.commands.CommandGenerator;
import tp1.logic.Game;
import tp1.view.GameView;
import tp1.view.Messages;

/**
 *  Accepts user input and coordinates the game execution logic
 */
public class Controller {

	private Game game;
	private GameView view;

	public Controller(Game game, GameView view) {
		this.game = game;
		this.view = view;
	}


	/**
	 * Runs the game logic, coordinate Model(game) and View(view)
	 */
	public void run() {

		view.showWelcome();

		view.showGame();
		
		while ( !game.isFinished()) {
			String[] words = view.getPrompt();
			Command command = CommandGenerator.parse(words);

			if (command != null)
				command.execute(game, view);
			else 
				view.showError(Messages.UNKNOWN_COMMAND.formatted(String.join(" ", words)));
		}
		view.showEndMessage();
	}
}
