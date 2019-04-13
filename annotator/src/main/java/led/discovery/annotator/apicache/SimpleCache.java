package led.discovery.annotator.apicache;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.orc.util.Murmur3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Credits:
 * 
 * SimpleCache Caching Class Originally published in PHP by: Gilbert Pellegrom
 * Webpage: https://github.com/gilbitron/PHP-SimpleCache
 * 
 * Ported to Java by HiddenMotives
 * 
 * Released under the MIT license.
 * http://www.opensource.org/licenses/mit-license.php
 * 
 */

public final class SimpleCache {

	private static Logger log = LoggerFactory.getLogger(SimpleCache.class);
	// Cache time is defined in day intervals. Default is 0=never update.
	private int cache_time = 0;

	// Directory where cached files will be stored
	private String cache_path = "cache/";

	// Extension of the cached files
	private String cache_extension = ".cache";

	public SimpleCache() {
		this.set_cache_path(cache_path);
	}

	public SimpleCache(File cache_directory) {
		this.set_cache_path(cache_directory.getAbsolutePath());
	}

	public SimpleCache(File cache_directory, int cache_time_days) {
		this.set_cache_path(cache_directory.getAbsolutePath());
		this.cache_time = cache_time_days;
	}

	public int get_cache_time() {
		return this.cache_time;
	}

	public String get_cache_path() {
		return this.cache_path;
	}

	public String get_cache_extension() {
		return this.cache_extension;
	}

	public void set_cache_time(int days) {
		this.cache_time = days;
	}

	public void set_cache_path(String path) {
		this.cache_path = path;
		if (!(new File(this.cache_path).isDirectory())) {
			new File(this.cache_path).mkdirs();
		}
	}

	public void set_cache_extension(String ext) {
		this.cache_extension = ext;
	}

	/**
	 * Check if a file is cached or not.
	 * 
	 * @param label
	 * @return
	 */
	public boolean is_cached(String label) {
		String filename = this.safe_filename(label) + this.cache_extension;
		File file = new File(String.valueOf(this.cache_path), filename);
		log.debug("{} {}", label, file.exists());
		// If cache_time is 0 days never update.
		if (this.cache_time <= 0) {
			return file.exists();
		}
		long diff = new Date().getTime() - file.lastModified();
		return file.exists() && (!(diff > (long) this.cache_time * 24 * 60 * 60 * 1000));
	}

	/**
	 * Function that allows you to grab the cache from a label.
	 * 
	 * @param label
	 * @return
	 */
	public String get_cache(String label) {
		log.debug("[get] {}", label);
		if (this.is_cached(label)) {
			String filename = this.safe_filename(label) + this.cache_extension;
			try {
				return FileUtils.readFileToString(new File(String.valueOf(this.cache_path), filename),
						StandardCharsets.UTF_8);
			} catch (IOException e) {
				log.error(e.getMessage());
			}
		}
		return null;
	}

	public Object get_cache_as_object(String label) {
		log.debug("[get] {}", label);
		if (this.is_cached(label)) {
			File filename = new File(String.valueOf(this.cache_path), this.safe_filename(label) + this.cache_extension);

			try {
				Object o;
				FileInputStream fis = null;
				ObjectInputStream in = null;
				fis = new FileInputStream(filename);
				in = new ObjectInputStream(fis);
				o = in.readObject();
				in.close();
				return o;
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}
		return null;
	}

	/**
	 * Set a cache file using a custom label and predefined data.
	 * 
	 * @param label
	 * @param data
	 */
	public void set_cache(String label, String data) {
		log.debug("[set] {}", label);
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(new File(this.cache_path, this.safe_filename(label) + this.cache_extension)),
				"utf-8"))) {
			writer.write(data);
			writer.close();
		} catch (IOException ex) {
			log.error(ex.getMessage());
		}
	}

	public void set_cache(String label, Serializable object) {
		log.debug("[set] {}", label);
		try (ObjectOutputStream oos = new ObjectOutputStream(
				new FileOutputStream(new File(this.cache_path, this.safe_filename(label) + this.cache_extension)))) {
			oos.writeObject(object);
			oos.close();
		} catch (IOException ex) {
			log.error(ex.getMessage());
		}
	}

	/**
	 * Function that tries to cached data, if no cached file is found it will try
	 * and cache the data from the URL.
	 * 
	 * @param label Label of the cached file
	 * @param url
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public String get_data(String label, String url) throws MalformedURLException, IOException {
		String data = null;

		if (this.get_cache(label) != null) {
			data = this.get_cache(label);
			return data;
		}

		data = this.grab_url(url);
		this.set_cache(label, data);
		return data;
	}

	/**
	 * Function that reads and returns the contents of a URL. Using the specified
	 * user agent and timeout when making the URL request.
	 * 
	 * @param url
	 * @param timeout   in milliseconds
	 * @param userAgent
	 * @return Contents of the URL.
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public String grab_url(String url, int timeout, String userAgent) throws MalformedURLException, IOException {
		StringBuilder response = new StringBuilder();
		URL website = new URL(url);
		URLConnection connection = website.openConnection();
		connection.setConnectTimeout(timeout);
		connection.setRequestProperty("User-Agent", userAgent);
		try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
			while ((url = in.readLine()) != null) {
				response.append(url);
			}

			in.close();
		}

		return response.toString();
	}

	/**
	 * Function that reads and returns the contents of a URL.
	 * 
	 * @param url
	 * @return Contents of the URL
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public String grab_url(String url) throws MalformedURLException, IOException {
		StringBuilder response = new StringBuilder();
		URL website = new URL(url);
		URLConnection connection = website.openConnection();
		connection.setConnectTimeout(5000);
		try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
			while ((url = in.readLine()) != null) {
				response.append(url);
			}

			in.close();
		}
		return response.toString();
	}

	/**
	 * Remove all Cached files
	 */
	public void clearCache() {
		for (File file : new File(this.cache_path).listFiles()) {
			file.delete();
		}
	}

	public void clearCache(String label) throws FileNotFoundException {
		String filename = this.safe_filename(label) + this.cache_extension;
		File file = new File(String.valueOf(this.cache_path), filename);
		if (file.exists()) {
			file.delete();
		} else {
			throw new FileNotFoundException();
		}
	}

	/**
	 * Function the number of cached files currently stored
	 * 
	 * @return total cached files
	 */
	public int get_total_cached() {
		return new File(this.cache_path).listFiles().length;
	}

	/**
	 * Helper function to help validate file names
	 * 
	 * @param filename
	 * @return
	 */
	private String safe_filename(String filename) {
		return filename.replaceAll("/[^0-9a-z\\.\\_\\-]/i", "");
	}

	public static final String hashLabel(String text) {
		return Integer.toHexString(Murmur3.hash32(text.getBytes()));
	}
}
