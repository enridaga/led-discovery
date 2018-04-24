package led.discovery.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Reuters21578 {
	private static Logger log = LoggerFactory.getLogger(Reuters21578.class);

	static String readFile(File file, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(file.toPath());
		return new String(encoded, encoding);
	}

	public static void extractNews(String from, String target) throws IOException {
		log.info("{} to {}", from, target);
		Collection<File> coll = FileUtils.listFiles(new File(from).getAbsoluteFile(), new String[] { "sgm" }, false);
		Iterator<File> it = coll.iterator();
		log.info("{}", it.hasNext());
		int ID = 1;
		while (it.hasNext()) {
			File qs = it.next();
			log.info("Reading {}", qs);
			String patternString = "<BODY>([^<]+)?</BODY>";
			Pattern pattern = Pattern.compile(patternString, Pattern.DOTALL);
			String text = readFile(qs, Charset.forName("UTF-8"));
			Matcher matcher = pattern.matcher(text);

			while (matcher.find()) {
				String m = matcher.group(1);
				log.info("Writing {} {}", ID, m.length());
				ID++;
				String fileId = new StringBuilder().append("Reuters21578-").append(Integer.toString(ID)).append(".txt").toString();

				File directory = new File(target);
				if (!directory.exists()) {
					directory.mkdirs();
				} else if (!directory.isDirectory()) {
					throw new IOException("Not a directory: " + directory);
				}
				File location = new File(directory, fileId);
				if (!location.exists()) {
					location.createNewFile();
				}
				FileOutputStream os = new FileOutputStream(location);
				IOUtils.write(m, os, "utf-8");
				os.close();
			}
		}
	}

	public static void main(String[] args) throws IOException {
		String source = args[0];
		String target = args[1];
		Reuters21578.extractNews(source, target);
	}
}
