package mainserver;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class Property_FileTest {
	@Test
	void testGet_property() {
		assertEquals("localhost",Property_File.get_property("db_host"));
	}

}
