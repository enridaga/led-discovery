package led.discovery.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

import led.discovery.db.H2TermsDatabase;
import led.discovery.nlp.StanfordNLPProvider;
import led.discovery.nlp.Term;
import led.discovery.tfidf.TFIDF;
import led.discovery.utils.GutenbergZipFileSourceFactory;

public class CLI {
	private static Logger log = LoggerFactory.getLogger(CLI.class);

	abstract class Command {
		public abstract void perform() throws Exception;
	}

	class LoadDocumentsCommand extends Command {
		File from;
		File ledDbHome;

		public LoadDocumentsCommand(File from, File ledDbHome) {
			this.from = from;
			this.ledDbHome = ledDbHome;
		}

		@Override
		public void perform() throws Exception {
			H2TermsDatabase db = new H2TermsDatabase(ledDbHome);
			GutenbergZipFileSourceFactory fac = new GutenbergZipFileSourceFactory();
			FileSourceProvider provider = new FileSourceProvider(fac);
			provider.addFromDirectory(from);
			Loader loader = new Loader(db, provider, new StanfordNLPProvider());
			loader.load();
		}
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

	class QueryDatabaseCommand extends Command {
		File ledDbHome;
		String query;

		public QueryDatabaseCommand(String query, File ledDbHome) {
			this.ledDbHome = ledDbHome;
			this.query = query;
		}

		final public void printResultSet(ResultSet rs) throws SQLException {
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnsNumber = rsmd.getColumnCount();
			while (rs.next()) {
				for (int i = 1; i <= columnsNumber; i++) {
					if (i > 1)
						System.out.print(" | ");
					System.out.print(rs.getString(i));
				}
				System.out.println("");
			}
		}

		@Override
		public void perform() throws Exception {
			H2TermsDatabase db = new H2TermsDatabase(ledDbHome);
			try (Connection conn = db.getConnection(); PreparedStatement st = conn.prepareStatement(query)) {
				ResultSet rs = st.executeQuery();
				printResultSet(rs);
			} catch (IOException | SQLException e) {
				throw new IOException(e);
			}
		}
	}

	class H2ShellCommand extends Command {
		File ledDbHome;
		String[] args;

		H2ShellCommand(File ledDbHome, String[] args) {
			this.ledDbHome = ledDbHome;
			this.args = args;
		}

		@Override
		public void perform() throws Exception {
			org.h2.tools.Shell shell = new org.h2.tools.Shell();
			shell.runTool(new H2TermsDatabase(ledDbHome).getConnection(), args);
		}
	}

	class TFIDFCommand extends Command {
		File ledDbHome;
		int docId;
		int limit;

		TFIDFCommand(File ledDbHome, int docId, int limit) {
			this.ledDbHome = ledDbHome;
			this.docId = docId;
			this.limit = limit;
		}

		@Override
		public void perform() throws Exception {
			TFIDF tfidf = new TFIDF(new H2TermsDatabase(ledDbHome));
			tfidf.init();
			Map<Integer, Double> idf = tfidf.idf();
			List<Map.Entry<Term, Double>> data = tfidf.compute(docId, idf);
			int count = 0;
			for (Map.Entry<Term, Double> t : data) {
				if (limit != -1 && count > limit)
					break;
				System.out.print(t.getKey());
				System.out.print(" ");
				System.out.print(t.getValue());
				System.out.println();
				count++;
			}
		}
	}

	private Options getOptions() {
		Option load = Option.builder().longOpt("load").desc("load command").build();
		Option wrap = Option.builder().longOpt("wrap").desc("wrap command").build();
		Option parseLe = Option.builder().longOpt("parse-le").desc("parse leds command").build();
		Option db = Option.builder().longOpt("db").argName("db").hasArg().desc("Path to the db").build();
		Option from = Option.builder().longOpt("from").argName("from").hasArg().desc("Folder to load files from").build();
		Option to = Option.builder().longOpt("to").argName("to").hasArg().desc("File to write to").build();

		Option query = Option.builder().longOpt("query").argName("query").hasArg().desc("Perform an SQL query").build();

		Option shell = Option.builder().longOpt("shell").argName("shell").desc("Folder to load files from").build();

		Option tfidf = Option.builder().longOpt("tfidf").argName("tfidf").desc("Show TFIDF for document").build();
		Option docId = Option.builder().longOpt("docId").argName("docId").hasArg().desc("DOC Id").build();
		Option limit = Option.builder().longOpt("limit").argName("limit").hasArg().desc("Limit").build();

		Options options = new Options();
		
		options.addOption(query);
		options.addOption(load);
		options.addOption(parseLe);
		options.addOption(wrap);
		options.addOption(db);
		options.addOption(from);
		options.addOption(to);
		options.addOption(shell);
		options.addOption(tfidf);
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

			if (line.hasOption("load") && line.hasOption("from") && line.hasOption("db")) {
				File from = new File(line.getOptionValue("from"));
				File db = new File(line.getOptionValue("db"));
				new LoadDocumentsCommand(from, db).perform();
			} else if (line.hasOption("load") && line.hasOption("from") && line.hasOption("to")) {
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
			} else if (line.hasOption("query") && line.hasOption("db")) {
				File db = new File(line.getOptionValue("db"));
				String query = line.getOptionValue("query");
				new QueryDatabaseCommand(query, db).perform();
			} else if (line.hasOption("shell") && line.hasOption("db")) {
				File db = new File(line.getOptionValue("db"));
				new H2ShellCommand(db, line.getArgs()).perform();
			} else if (line.hasOption("tfidf") && line.hasOption("docId") && line.hasOption("db")) {
				int limit = -1;
				if (line.hasOption("limit")) {
					limit = Integer.parseInt(line.getOptionValue("limit"));
				}
				File db = new File(line.getOptionValue("db"));
				int docId = Integer.parseInt(line.getOptionValue("docId"));
				new TFIDFCommand(db, docId, limit).perform();
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
