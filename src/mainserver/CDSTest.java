package mainserver;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CDSTest {

	@Test
	void testRandom_different_numbers() {		
		assertTrue(CDS.random_different_numbers(4)[0] <= 3 
				&& CDS.random_different_numbers(4)[1] >=0);
	}

	@Test
	void testIs_map_full() {
	
		String[] client1_map = {"GGGMGGGG","GGGGGMGG","GGGGGWGG","GGGGGGG"};
		CDS.client1_map = client1_map;
		
		String[] client2_map = {"GGGMGGGG","GGGGGMGG","GGGGGWGG","GGGGGGG"};
		CDS.client2_map = client2_map;
		
		assertEquals( true, CDS.is_map_full(0));
		
	}

	@Test
	void testCheck_move(){ 		
		char[][] map_secret = {{'G','G','1','G','G','G','W','2'},
								{'G','W','W','G','G','G','W','G'},
								{'G','G','G','G','W','G','G','G'},
								{'G','G','G','G','G','M','M','G'},
								{'G','G','M','G','G','G','G','G'},
								{'G','G','G','G','G','W','G','G'},
								{'G','G','G','M','G','W','8','G'},
								{'7','G','G','G','G','G','G','G'}};
		
		CDS.map_secret = map_secret;
		
		assertEquals( 1, CDS.check_move(0,0, 7));
	}

}
