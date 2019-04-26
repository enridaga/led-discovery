package led.discovery.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Base64;
import java.util.Iterator;

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

import led.discovery.nlp.StanfordNLPProvider;
import led.discovery.utils.GutenbergZipFileSourceFactory;

public class CLI {
	private static Logger log = LoggerFactory.getLogger(CLI.class);

	abstract class Command {
		public abstract void perform() throws Exception;
	}

	class LoadInTSVCommand extends Command {
		File from;
		File tsvFile;

		public LoadInTSVCommand(File from, File tsvFile) {
			this.from = from;
			this.tsvFile = tsvFile;
		}

		@Override
		public void perform() throws Exception {
			GutenbergZipFileSourceFactory fac = new GutenbergZipFileSourceFactory();
			FileSourceProvider provider = new FileSourceProvider(fac);
			provider.addFromDirectory(from);
			ProcessToTSV loader = new ProcessToTSV(tsvFile, provider, new StanfordNLPProvider());
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
			provider.addFromDirectory(from);
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
						//System.out.println("encoded value is " + new String(bytesEncoded));
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
						log.info("{} [loaded in {}{}]", new Object[] { s.getDocumentName(), ((end - start) / 1000), "s" });
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
			ProcessToTSV loader = new ProcessToTSV(tsvFile, provider, new StanfordNLPProvider());
			loader.load();
		}
	}


	private Options getOptions() {
		Option load = Option.builder().longOpt("load").desc("load command").build();
		Option wrap = Option.builder().longOpt("wrap").desc("wrap command").build();
		Option parseLe = Option.builder().longOpt("parse-le").desc("parse leds command").build();
		Option from = Option.builder().longOpt("from").argName("from").hasArg().desc("Folder to load files from").build();
		Option to = Option.builder().longOpt("to").argName("to").hasArg().desc("File to write to").build();


		Option docId = Option.builder().longOpt("docId").argName("docId").hasArg().desc("DOC Id").build();
		Option limit = Option.builder().longOpt("limit").argName("limit").hasArg().desc("Limit").build();

		Options options = new Options();
		
		
		options.addOption(load);
		options.addOption(parseLe);
		options.addOption(wrap);
		
		options.addOption(from);
		options.addOption(to);
		
		options.addOption(docId);
		options.addOption(limit);
		return options;
	}

	private void start(String[] args) {
		// Available Commands
		Options options = getOptions();

		// create the parser
		CommandLineParser parser = new DefaultParser();
		try {
			// parse the command line arguments
			CommandLine line = parser.parse(options, args);

			if (line.hasOption("load") && line.hasOption("from") && line.hasOption("to")) {
				File from = new File(line.getOptionValue("from"));
				File to = new File(line.getOptionValue("to"));
				new LoadInTSVCommand(from, to).perform();
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
