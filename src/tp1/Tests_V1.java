package tp1;

import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

public class Tests_V1 {
	public static final String DIR = "tests/pr1/";
	public static final String FILE_PREFIXES[] = {
			"00_0-helpPaint",
			"00_1-commands",
			"00_2-movements",
			"01_3-winsNoColision",
			"01_4-loseNoColision",
			"01_5-colisions",
			"01_6-randomPlay"
			};

	private void testN(int n) {
		String mapa = FILE_PREFIXES[n].substring(0, 2);
		TestsUtils.parameterizedTest(
				          Paths.get(DIR + FILE_PREFIXES[n] + "_input.txt"), 
				          Paths.get(DIR + FILE_PREFIXES[n] + "_expected.txt"),
				          Paths.get(DIR + FILE_PREFIXES[n] + "_output.txt"),
				new String[] { mapa, "NO_COLORS" });
	}
	
	@Test
	public void test00() { 	testN(0); }
	@Test
	public void test01() { 	testN(1); }
	@Test
	public void test02() { 	testN(2); }
	@Test
	public void test03() { 	testN(3); }
	@Test
	public void test04() { 	testN(4); }
	@Test
	public void test05() { 	testN(5); }
	@Test
	public void test06() { 	testN(6); }
}
