package led.discovery.app.model;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileCacheTest {

	Logger L = LoggerFactory.getLogger(FileCacheTest.class);
	FileCache C;
	String RECOLL;
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Before
	public void before() throws IOException {
		C = new FileCache(folder.newFolder());
		RECOLL = new String(IOUtils.toCharArray(getClass().getClassLoader().getResourceAsStream("./data/RECOLL.txt"),
				StandardCharsets.UTF_8));

	}

	@Test
	public void testHash() throws IOException {
		String key = "Recollection1";
		C.put(RECOLL, key);
		String hash = C.buildHash(key);
		Assert.assertTrue(C.containsHash(hash));
		Assert.assertTrue(C.containsHash(C.buildHash(key)));
		String key2 = "Recollection2";
		Assert.assertTrue(!C.containsHash(C.buildHash(key2)));
	}

	@Test
	public void testDelete() throws IOException {
		String key = "Recollection1";
		C.put(RECOLL, key);
		String hash = C.buildHash(key);
		Assert.assertTrue(C.containsHash(hash));
		Assert.assertTrue(C.containsHash(C.buildHash(key)));
		String key2 = "Recollection2";
		Assert.assertTrue(!C.containsHash(C.buildHash(key2)));
	}
}
