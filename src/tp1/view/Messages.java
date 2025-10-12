package tp1.view;

import tp1.util.MyStringUtils;

public class Messages {
	
	public static final String VERSION = "2.X";

	public static final String GAME_NAME = "MarioBross";

	public static final String USAGE = "Usage: %s [<level>]".formatted(GAME_NAME);

	public static final String WELCOME = String.format("%s %s%n", GAME_NAME, VERSION);

	public static final String LEVEL_NOT_A_NUMBER = "The level must be a number";
	public static final String INVALID_LEVEL_NUMBER = "Not valid level number";

	public static final String LEVEL_NOT_A_NUMBER_ERROR = String.format("%s: %%s", LEVEL_NOT_A_NUMBER);

	public static final String PROMPT = "Command > ";

	public static final String DEBUG = "[DEBUG] Executing: %s%n";
	public static final String ERROR = "[ERROR] Error: %s";

	// GAME STATUS
	public static final String NUMBER_OF_CYCLES = "Number of cycles: %s";

	public static final String REMAINING_TIME = "Time: %s";
	public static final String POINTS = "Points: %s";
	public static final String NUM_LIVES = "Lives: %s";

	// GAME END MESSAGE
	public static final String GAME_OVER = "Game over";
	public static final String PLAYER_QUITS = "Player leaves the game";
	public static final String MARIO_WINS = "Thanks, Mario! Your mission is complete.";
	// Position format
	public static final String POSITION = "(%s,%s)";

	// Other
	public static final String SPACE = " ";
	public static final String TAB = "   ";
	public static final String LINE_SEPARATOR = System.lineSeparator();
	public static final String LINE = "%s" + LINE_SEPARATOR;
	public static final String LINE_TAB = TAB + LINE;
	public static final String LINE_2TABS = TAB + LINE_TAB;

//Commands
	// Errores de factorías:
	public static final String UNKNOWN_COMMAND = "Unknown command: %s";
	public static final String INVALID_GAME_OBJECT = "Invalid game object: %s";
	// Errores de commandos:
	public static final String COMMAND_PARAMETERS_MISSING = "Missing parameters";
	public static final String COMMAND_INCORRECT_PARAMETER_NUMBER = "Incorrect parameter number";
	public static final String UNKNOWN_ACTION = "Unknown action: \"%s\"";
	public static final String ILLEGAL_ACTION = "Illegal action: \"%s\"";
	public static final String INVALID_COMMAND = "Invalid command: %s";
	public static final String INVALID_COMMAND_PARAMETERS = "Invalid command parameters";
	public static final String ERROR_COMMAND_EXECUTE = "Command execute problem";

	
	public static final String HELP_AVAILABLE_COMMANDS = "Available commands:";
	@Deprecated
	/* @formatter:off */
	public static final String[] HELP_LINES = new String[] { HELP_AVAILABLE_COMMANDS,
		"[a]ction [[R]IGHT | [L]EFT | [U]P | [D]OWN | [S]TOP]+: user performs actions",
		"[u]pdate | \"\": user does not perform any action",
		"[r]eset [numLevel]: reset the game to initial configuration if not numLevel else load the numLevel map",
		"[h]elp: print this help message",
		"[e]xit: exits the game"
	};
	/* @formatter:on */
	@Deprecated
	public static final String HELP = String.join(LINE_SEPARATOR+"   ", HELP_LINES) + LINE_SEPARATOR;
	public static final String COMMAND_HELP_TEXT = "%s: %s";
	
	// UPDATE
	public static final String COMMAND_UPDATE_NAME = "update";
	public static final String COMMAND_UPDATE_SHORTCUT = "u";
	public static final String COMMAND_UPDATE_DETAILS = "[u]pdate | \"\"";
	public static final String COMMAND_UPDATE_HELP = "user does not perform any action";
		
	// EXIT
	public static final String COMMAND_EXIT_NAME = "exit";
	public static final String COMMAND_EXIT_SHORTCUT = "e";
	public static final String COMMAND_EXIT_DETAILS = "[e]xit";
	public static final String COMMAND_EXIT_HELP = "exits the game";
	
	// HELP
	public static final String COMMAND_HELP_NAME = "help";
	public static final String COMMAND_HELP_SHORTCUT = "h";
	public static final String COMMAND_HELP_DETAILS = "[h]elp";
	public static final String COMMAND_HELP_HELP = "print this help message";

//Symbols
	public static final String EMPTY = "";
	public static final String LAND = MyStringUtils.repeat("▓",ConsoleView.CELL_SIZE);
	public static final String EXIT_DOOR = "🚪";
	public static final String MARIO_STOP = "🧑";
	public static final String MARIO_RIGHT = "🧍";//"🧍➡️";
	public static final String MARIO_LEFT = "🚶";//"⬅️🚶";
	public static final String GOOMBA = "🐻";
	
	public static final String MUSHROOM = "🍄";
	public static final String BOX = MyStringUtils.repeat("?",ConsoleView.CELL_SIZE);
	public static final String EMPTY_BOX = MyStringUtils.repeat("0",ConsoleView.CELL_SIZE);
}
