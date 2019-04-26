package led.discovery.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.util.StringUtils;
import led.discovery.nlp.StanfordNLPProvider;
import led.discovery.utils.GutenbergZipFileSourceFactory;

public class CLI {
	private static Logger log = LoggerFactory.getLogger(CLI.class);

	abstract class Command {
		public abstract void perform() throws Exception;
	}

	class LoadDocsTermsInTSVCommand extends Command {
		File from;
		File tsvFile;

		public LoadDocsTermsInTSVCommand(File from, File tsvFile) {
			this.from = from;
			this.tsvFile = tsvFile;
		}

		@Override
		public void perform() throws Exception {
			GutenbergZipFileSourceFactory fac = new GutenbergZipFileSourceFactory();
			FileSourceProvider provider = new FileSourceProvider(fac);
			DocsTermsToTSV loader = new DocsTermsToTSV(tsvFile, provider, new StanfordNLPProvider());
			loader.load();
		}
	}

	class CreateSingleDocList extends Command {
		File from;
		File tsvFile;

		public CreateSingleDocList(File from, File tsvFile) {
			this.from = from;
			this.tsvFile = tsvFile;
		}

		@Override
		public void perform() throws Exception {
			GutenbergZipFileSourceFactory fac = new GutenbergZipFileSourceFactory();
			FileSourceProvider provider = new FileSourceProvider(fac);
			if (from.isDirectory()) {
				provider.addFromDirectory(from);
			} else if (from.isFile()) {
				List<String> lines = Files.readAllLines(from.toPath());
				for(String f : lines) {
					// Support reference to home directory
					f = f.replaceFirst("^~", System.getProperty("user.home"));
					File q = new File(f);
					try {
						provider.add(q);
					}catch(Exception e) {
						log.error(e.getMessage());
					}
				}
			}else {
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
	}

	class CreateListeningExperiencesTSV extends Command {
		File from;
		File tsvFile;

		public CreateListeningExperiencesTSV(File from, File tsvFile) {
			this.from = from;
			this.tsvFile = tsvFile;
		}

		@Override
		public void perform() throws Exception {
			FileSourceProvider provider = new FileSourceProvider(new FileSourceFactory());
			provider.addFromDirectory(from);
			DocsTermsToTSV loader = new DocsTermsToTSV(tsvFile, provider, new StanfordNLPProvider());
			loader.load();
		}
	}

	private Options getOptions() {
		Option parseToTSV = Option.builder().longOpt("parseToTSV")
				.desc("parseToTSV command. From a direcotry of zips to a TSV of {docId,position,term[pos]}").build();
		Option wrap = Option.builder().longOpt("wrap").desc("wrap command").build();
		Option parseLe = Option.builder().longOpt("parse-le").desc("parse leds command").build();
		Option from = Option.builder().longOpt("from").argName("from").hasArg()
				.desc("Folder to load files from or file containing a list of files.").build();
		Option to = Option.builder().longOpt("to").argName("to").hasArg().desc("File to write to").build();

		Option docId = Option.builder().longOpt("docId").argName("docId").hasArg().desc("DOC Id").build();
		Option limit = Option.builder().longOpt("limit").argName("limit").hasArg().desc("Limit").build();

		Options options = new Options();

		options.addOption(parseToTSV);
		options.addOption(parseLe);
		options.addOption(wrap);

		options.addOption(from);
		options.addOption(to);

		options.addOption(docId);
		options.addOption(limit);
		return options;
	}

	private void start(String[] args) {
		System.out.println("start");
		System.out.println(StringUtils.join(args," "));
		// Available Commands
		Options options = getOptions();

		// create the parser
		CommandLineParser parser = new DefaultParser();
		try {
			// parse the command line arguments
			CommandLine line = parser.parse(options, args);

			if (line.hasOption("parseToTSV") && line.hasOption("from") && line.hasOption("to")) {
				File from = new File(line.getOptionValue("from"));
				File to = new File(line.getOptionValue("to"));
				new LoadDocsTermsInTSVCommand(from, to).perform();
			} else if (line.hasOption("parse-le") && line.hasOption("from") && line.hasOption("to")) {
				File from = new File(line.getOptionValue("from"));
				File to = new File(line.getOptionValue("to"));
				new CreateListeningExperiencesTSV(from, to).perform();
			} else if (line.hasOption("wrap") && line.hasOption("from") && line.hasOption("to")) {
				File from = new File(line.getOptionValue("from"));
				File to = new File(line.getOptionValue("to"));
				new CreateSingleDocList(from, to).perform();
			} else if (line.hasOption("help")) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("led", options);
			} else {
				System.out.println("Insufficient or wrong arguments");
			}

		} catch (IndexOutOfBoundsException exp) {
			// oops, something went wrong
			System.err.println("Arguments missing" + exp.getMessage());
		} catch (ParseException exp) {
			// oops, something went wrong
			System.err.println("Parsing failed.  Reason: " + exp.getMessage());
		} catch (Exception e) {
			log.error("A problem occurred.", e);
		}
	}

	public static void main(String[] args) {
		CLI cli = new CLI();
		cli.start(args);
	}

}
