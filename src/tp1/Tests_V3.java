package tp1;

import java.nio.file.Paths;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class Tests_V3 {
	public static final String DIR = "tests/pr3/";
	public static final String FILE_PREFIXES[] = {
			"00_00-commands_errors",
			"00_01-commands",
			"00_02_file_errors",
			"00_03-file_reset",
			"00_04-reset_load_save",
			"00_05-movements",
			"01_06-winsNoColision",
			"01_07-loseNoColision",
			"01_08-colisions",
			"01_09-randomPlay",
			"01_10-addObjectCommand",
			"01_11-addObjectCommand_map",
			"01_12-addObjectCommand_newMap",
			"02_13-boxMushroom",
			"02_14-boxMushroom_addObject",
			"06_optionalTest"
			};

	private void testN(int n) {
		String mapa = FILE_PREFIXES[n].substring(0, 2);
		TestsUtils.parameterizedTest(Paths.get(DIR + FILE_PREFIXES[n] + "_input.txt"), 
				          Paths.get(DIR + FILE_PREFIXES[n] + "_expected.txt"),
				          Paths.get(DIR + FILE_PREFIXES[n] + "_output.txt"),
				new String[] { mapa, "NO_COLORS" });
	}
	
	@Test
	public void test00() { testN(0); }
	@Test
	public void test01() { testN(1); }
	@Test
	public void test02() { testN(2); }
	@Test
	public void test03() { testN(3); }
	@Test
	public void test04() { testN(4); }
	@Test
	public void test05() { testN(5); }
	@Test
	public void test06() { testN(6); }
	@Test
	public void test07() { testN(7); }
	@Test
	public void test08() { testN(8); }
	@Test
	public void test09() { testN(9); }
	@Test
	public void test10() { testN(10); }
	@Test
	public void test11() { testN(11); }
	@Test
	public void test12() { testN(12); }
	@Test
	public void test13() { testN(13); }
	@Test
	public void test14() { testN(14); }
	@Test
    @DisplayName("OPTIONAL test! (Mario's consistent behaviour)")
	public void testOPTIONAL() { testN(15); }
}
