package led.discovery.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import led.discovery.app.model.OutputModel;
import led.discovery.app.model.OutputModel.Block;
import led.discovery.nlp.StanfordNLPProvider;
import led.discovery.utils.GutenbergZipFileSourceFactory;

public class Tools {
	private static Logger log = LoggerFactory.getLogger(Tools.class);

	public static class LoadDocsTermsInTSVCommand {
		File from;
		File tsvFile;

		public LoadDocsTermsInTSVCommand(File from, File tsvFile) {
			this.from = from;
			this.tsvFile = tsvFile;
		}

		public void perform() throws Exception {
			GutenbergZipFileSourceFactory fac = new GutenbergZipFileSourceFactory();
			FileSourceProvider provider = new FileSourceProvider(fac);
			DocsTermsToTSV loader = new DocsTermsToTSV(tsvFile, provider, new StanfordNLPProvider());
			loader.load();
		}

		public static void main(String[] args) throws Exception {
			File from = new File(args[0]);
			File to = new File(args[1]);
			new LoadDocsTermsInTSVCommand(from, to).perform();
		}
	}

	public static class CreateSingleDocList {
		File from;
		File tsvFile;

		public CreateSingleDocList(File from, File tsvFile) {
			this.from = from;
			this.tsvFile = tsvFile;
		}

		public void perform() throws Exception {
			GutenbergZipFileSourceFactory fac = new GutenbergZipFileSourceFactory();
			FileSourceProvider provider = new FileSourceProvider(fac);
			if (from.isDirectory()) {
				provider.addFromDirectory(from);
			} else if (from.isFile()) {
				List<String> lines = Files.readAllLines(from.toPath());
				for (String f : lines) {
					// Support reference to home directory
					f = f.replaceFirst("^~", System.getProperty("user.home"));
					File q = new File(f);
					try {
						provider.add(q);
					} catch (Exception e) {
						log.error(e.getMessage());
					}
				}
			} else {
				throw new IOException("Invalid file type " + from.exists());
			}
			FileWriter fw = null;
			Iterator<Source> i = provider.getSources();
			fw = new FileWriter(tsvFile);
			try (BufferedWriter bw = new BufferedWriter(fw)) {
				while (i.hasNext()) {
					Source s = i.next();
					try {
						long start = System.currentTimeMillis();
						String id = s.getDocumentName();
						String text = IOUtils.toString(s.getContent(), s.getEncoding());
						byte[] bytesEncoded = Base64.getEncoder().encode(text.getBytes());
						// System.out.println("encoded value is " + new String(bytesEncoded));
						bw.write(id);
						bw.write("\t");
						bw.write("GUTENBERG");
						bw.write("\t");
						bw.write(Integer.toString(bytesEncoded.length));
						bw.write("\t");
						bw.write(s.getEncoding().name());
						bw.write("\t");
						bw.write(new String(bytesEncoded));
						bw.write("\n");
						long end = System.currentTimeMillis();
						log.info("{} [loaded in {}{}]",
								new Object[] { s.getDocumentName(), ((end - start) / 1000), "s" });
					} catch (IOException e) {
						log.error("Cannot load source {}: {}", s.getDocumentName(), e.getMessage());
					}

				}
			} finally {
				fw.close();
			}
		}

		public static void main(String[] args) throws Exception {
			File from = new File(args[0]);
			File to = new File(args[1]);
			new CreateSingleDocList(from, to).perform();
		}
	}

	public static class ExploreCache {
		public void list(File cache, String filename) throws FileNotFoundException, IOException {
			OutputModel m = OutputModel.fromJSON(IOUtils.toString(new FileReader(new File(cache, filename))));
			Iterator<Block> i = m.blocks();
			while (i.hasNext()) {
				Block b = i.next();
				if (b.isLE()) {
					System.out.println("--------------------------------------------------------------");
					System.out.print(b.offsetStart());
					System.out.print(":");
					System.out.println(b.offsetEnd());
					System.out.println(b.getText());
					Map<String, String> meta = b.getMetadata();
					for (Entry<String, String> mm : meta.entrySet()) {
						System.out.print(mm.getKey());
						System.out.print(": ");
						System.out.println(mm.getValue());
					}
				}
			}
		}
		public static void main(String[] args) throws Exception {
			File cache = new File(args[0]);
			new ExploreCache().list(cache, args[1]);
		}
	}

	public static class BuildDocsCache {
		File fromTsv;
		File listSources;
		File cacheDir;

		public BuildDocsCache(File fromTsv, File outputFile, File cacheDir) {
			this.fromTsv = fromTsv;
			this.listSources = outputFile;
			this.cacheDir = cacheDir;
		}

		public void perform() throws Exception {
			RunAnnotator ann = new RunAnnotator(cacheDir.getAbsolutePath());
			System.out.println("perform()");
			try (FileOutputStream fos = new FileOutputStream(listSources);
					BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
					BufferedReader reader = new BufferedReader(new FileReader(fromTsv));) {

				String line;
				while ((line = reader.readLine()) != null) {
					String[] fields = line.split("\t");
					String sourceId = fields[0];
					log.info(sourceId);
					String text = new String(Base64.getDecoder().decode(fields[4].getBytes()), fields[3]);
					// System.out.println(text.substring(0, 200));
					OutputModel m = ann.annotate(sourceId, text);
					String l = new StringBuilder().append(sourceId).append(" ").append(m.getMetadata("hash"))
							.append(" ").append(m.getMetadata("maxScore")).append(" ")
							.append(Integer.toString(m.numberOfLEFound())).append("\n").toString();
					System.out.println(l);
					bw.write(l);
				}
				reader.close();
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public static void main(String[] args) throws Exception {
			File fromTsv = new File(args[0]);
			File outputFile = new File(args[1]);
			File cacheDir = new File(args[2]);
			new BuildDocsCache(fromTsv, outputFile, cacheDir).perform();
		}
	}

	public static class CreateListeningExperiencesTSV {
		File from;
		File tsvFile;

		public CreateListeningExperiencesTSV(File from, File tsvFile) {
			this.from = from;
			this.tsvFile = tsvFile;
		}

		public void perform() throws Exception {
			FileSourceProvider provider = new FileSourceProvider(new FileSourceFactory());
			provider.addFromDirectory(from);
			DocsTermsToTSV loader = new DocsTermsToTSV(tsvFile, provider, new StanfordNLPProvider());
			loader.load();
		}

		public static void main(String[] args) throws Exception {
			File from = new File(args[0]);
			File to = new File(args[1]);
			new CreateListeningExperiencesTSV(from, to).perform();
		}
	}
}
