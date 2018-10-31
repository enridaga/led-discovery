package led.discovery.app.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;

import com.google.common.hash.Hashing;

public class FileCache {
	private File folder;

	public FileCache(String folder) {
		this(new File(folder));
	}

	public FileCache(File folder) {
		this.folder = folder;
	}

	public boolean containsHash(String hash) {
		return new File(folder, hash).exists();
	}

	public boolean contains(String... key) {
		return new File(folder, buildHash(key)).exists();
	}

	public void putHash(String hash, String value) throws IOException {
		OutputStream fos = putStream(hash);
		IOUtils.write(value, fos, StandardCharsets.UTF_8);
	}

	public boolean clear(String hash) {
		File cacheFile = new File(folder, hash);
		if (cacheFile.exists()) {
			cacheFile.delete();
			return true;
		}
		return false;
	}

	public void put(String value, String... key) throws IOException {
		putHash(buildHash(key), value);
	}

	public OutputStream putStream(String hash) throws IOException {
		File cacheFile = new File(folder, hash);
		FileOutputStream fos = new FileOutputStream(cacheFile);
		return fos;
	}

	public String buildHash(String... key) {
		StringBuilder sb = new StringBuilder();
		for (String k : key) {
			sb.append(k);
		}
		String hash = Hashing.sha256().hashString(sb.toString(), StandardCharsets.UTF_8).toString();
		return hash;
	}

	public String get(String... key) throws IOException {
		return getByHash(buildHash(key));
	}

	public String getByHash(String hash) throws IOException {
		return IOUtils.toString(new FileInputStream(new File(folder, hash)), StandardCharsets.UTF_8);
	}
}
