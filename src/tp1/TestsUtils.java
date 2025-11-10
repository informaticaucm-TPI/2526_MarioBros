package tp1.tests;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Path;

public final class TestsUtils {
	private TestsUtils() {} // prevents instances (static class)

	private static boolean areSegmentsEquivalent(String segment1, String segment2) {
	    // Ordena los caracteres de ambos segmentos y los compara
	    return sortCharacters(segment1.trim()).equals(sortCharacters(segment2.trim()));
	}

	private static String sortCharacters(String segment) {
	    return segment.chars()
	                  .sorted()
	                  .collect(StringBuilder::new,
	                           StringBuilder::appendCodePoint,
	                           StringBuilder::append)
	                  .toString();
	}

	
	public static boolean areLinesEquivalent(String expectedLine, String actualLine) {
        // Dividir ambas líneas por los delimitadores '|'
        String[] expectedParts = expectedLine.split("┃");
        String[] actualParts = actualLine.split("┃");

        // 1. Ambas líneas deben tener exactamente dos delimitadores
        if (expectedParts.length != 3 || actualParts.length != 3) {
            return false;
        }

        // 2. El contenido fuera de los delimitadores debe ser idéntico
        if (!expectedParts[0].equals(actualParts[0]) || !expectedParts[2].equals(actualParts[2])) {
            return false;
        }

        // 3. El contenido dentro de los delimitadores debe dividirse en DIM_X trozos
        String[] expectedSegments = splitIntoSegments(expectedParts[1], tp1.logic.Game.DIM_X);
        String[] actualSegments = splitIntoSegments(actualParts[1], tp1.logic.Game.DIM_X);

        if (expectedSegments == null || actualSegments == null) {
            return false; // Si no se pueden dividir correctamente
        }

        // Comparar cada segmento entre ambas líneas
        for (int i = 0; i < tp1.logic.Game.DIM_X; i++) {
            if (!areSegmentsEquivalent(expectedSegments[i], actualSegments[i])) {
            	//System.out.println("Expected segment: " + expectedSegments[i]);
            	//System.out.println("Actual   segment: " + actualSegments[i]);
                return false;
            }
        }

        // Si todas las condiciones se cumplen, las líneas son equivalentes
        return true;
    }

    private static String[] splitIntoSegments(String content, int segments) {
        if (content.length() % segments != 0) {
            return null; // No se puede dividir en partes iguales
        }

        int segmentLength = content.length() / segments;
        String[] result = new String[segments];

        for (int i = 0; i < segments; i++) {
            result[i] = content.substring(i * segmentLength, (i + 1) * segmentLength);
        }

        return result;
    }
	
    private static void checkOutput(Path expectedPath, Path actualPath) throws FileNotFoundException, IOException {
		try (BufferedReader expected = new BufferedReader(new FileReader(expectedPath.toFile()));
				BufferedReader actual = new BufferedReader(new FileReader(actualPath.toFile()))) {

			String expectedLine = expected.readLine();
			String actualLine = actual.readLine();
			int lineNumber = 1;
			while (expectedLine != null && actualLine != null &&
					   ( expectedLine.equals(actualLine)
					   || areLinesEquivalent(expectedLine,actualLine))) { // ORDER not important

				expectedLine = expected.readLine();
				actualLine = actual.readLine();
				lineNumber++;
			}
			
			if (expectedLine != null || actualLine != null) {
				String lineMessage = "Line: %d%n".formatted(lineNumber);
				String expectedMessage = "Expected: %s%n".formatted(
						expectedLine == null? "EOF":expectedLine);
				String actualMessage = "Actual  : %s%n".formatted(
						actualLine == null? "EOF":actualLine);
				System.out.println(lineMessage + expectedMessage + actualMessage);
				
				fail(lineMessage + expectedMessage + actualMessage);
     		}
		}
	}

    public static void parameterizedTest(Path input, Path expected, Path output, String[] args) {
		try (PrintStream out = new PrintStream(output.toFile()); InputStream in = new FileInputStream(input.toFile())) {
			PrintStream oldOut = System.out;
			InputStream oldIn = System.in;

			System.setOut(out);
			System.setIn(in);

			Main.main(args);

			System.setOut(oldOut);
			System.setIn(oldIn);

			checkOutput(expected, output);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail("File not found!");
		} catch (IOException e1) {
			e1.printStackTrace();
			fail("IOExpception");
		}
	}
}
