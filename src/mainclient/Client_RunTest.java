package mainclient;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

public class Client_RunTest {	
	@Test
	public void testRandom_char() {
		Client_Run client = new Client_Run();
		ArrayList<Character> tests = null;
		tests.add('a');
		tests.add('b');
		tests.add('c');
		String result = client.random_char(tests) + "";
		assertTrue(Character.isLetter(result.charAt(0)));
	}
	
	@Test
	public void testGenerate_map() {
		Client_Run client = new Client_Run();
		String test = client.generate_map();
		assertEquals(test.length(), 16);
	}
	
	@Test
	public void testSplit_map() {
		Client_Run client = new Client_Run();
		String[] test = client.split_map();
		assertEquals(test.length, 4);		
	}

}
