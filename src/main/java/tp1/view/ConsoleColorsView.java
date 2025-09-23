package tp1.view;

import static tp1.util.MyStringUtils.repeat;

import tp1.logic.Game;
import tp1.util.MyStringUtils;

public class ConsoleColorsView extends ConsoleView {
	
    private static final String MARIO_COLOR  = ConsoleColorsAnsiCodes.ANSI_RED;
    private static final String GOOMBA_COLOR  = ConsoleColorsAnsiCodes.ANSI_BROWN;;

    private static final String COLOR_LAND_BACKGROUND  = ConsoleColorsAnsiCodes.ANSI_RGB_BACKGROUND.formatted(218, 165, 32); // ConsoleColorsAnsiCodes.ANSI_CYAN_BACKGROUND
    private static final String COLOR_EXIT_BACKGROUND  = ConsoleColorsAnsiCodes.ANSI_CYAN_BACKGROUND;
	
	private static final String CELL_TXT = repeat(SPACE, CELL_SIZE);
	private static final String EMPTY_CELL = CELL_TXT;
	private static final String LAND_CELL = COLOR_LAND_BACKGROUND + CELL_TXT + ConsoleColorsAnsiCodes.ANSI_RESET;
	private static final String EXIT_TEXT = ConsoleColorsAnsiCodes.ANSI_WHITE + MyStringUtils.center(Messages.EXIT_DOOR, CELL_SIZE);
	private static final String EXIT_CELL = COLOR_EXIT_BACKGROUND + EXIT_TEXT + ConsoleColorsAnsiCodes.ANSI_RESET;
	private static final String EXIT_PLUS = COLOR_EXIT_BACKGROUND + "%s" + ConsoleColorsAnsiCodes.ANSI_RESET;

	public ConsoleColorsView(Game game) {
		super(game);
	}
	
	@Override
	protected String consoleCell(String celStr) {
		String consoleStr = celStr;

		if (celStr.equals(Messages.EMPTY)) consoleStr = EMPTY_CELL;
		else if (celStr.equals(Messages.LAND)) consoleStr = LAND_CELL;
		else if (celStr.equals(Messages.EXIT_DOOR)) consoleStr = EXIT_CELL;
		else if (celStr.contains(Messages.EXIT_DOOR)) consoleStr = exitCel(celStr);

		else  consoleStr = characterCell(celStr);
		
		return consoleStr;
	}

	private static String otherCell(String color, String str) {
		return color + MyStringUtils.center(str, CELL_SIZE) + ConsoleColorsAnsiCodes.ANSI_RESET;
	}
	private static String characterCell(String str) {
		return otherCell(characterColor(str), str);
	}
	private static String characterColor(String str) {
		String colorStr = ConsoleColorsAnsiCodes.ANSI_GREEN;

		if (str.contains(Messages.GOOMBA)) colorStr = GOOMBA_COLOR;
		else if (str.contains(Messages.MARIO_STOP) ||
				 str.contains(Messages.MARIO_LEFT) ||
				 str.contains(Messages.MARIO_RIGHT)  ) colorStr = MARIO_COLOR;
		
		return colorStr;
	}
	private static String exitCel(String celStr) {
		return EXIT_PLUS.formatted(MyStringUtils.center(celStr, CELL_SIZE));
	}
}
