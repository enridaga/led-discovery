package led.discovery.spark;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.util.ArrayUtils;

/**
 * Generating Bags of Terms (Lemma+POS)
 * 
 * @author enridaga
 *
 */
public class GenTrainingFile {
	private File positivesFolder;
	private File[] negativesFolders;
	private File outputFile;
	private static final Logger L = LoggerFactory.getLogger(GenTrainingFile.class);
	private int mode = 1;
	public GenTrainingFile(String[] args) {
		this.positivesFolder = new File(args[0]);
		String[] negs = args[1].split(",");
		List<File> negatives = new ArrayList<File>();
		for (String n : negs) {
			negatives.add(new File(n));
		}
		this.negativesFolders = negatives.toArray(new File[negatives.size()]);
		this.outputFile = new File(args[2]);
		if(args.length > 3) {
			mode = Integer.parseInt(args[3]);
			if(mode !=0 && mode !=1) {
				throw new RuntimeException("Invalid value for param mode");
			}
		}
	}

	private void _clean() {
		if (outputFile.exists()) {
			outputFile.delete();
		}
	}

	public void run() throws FileNotFoundException, IOException {
		_clean();
		L.info("Loading positive examples");
		// List<Row> data = new ArrayList<Row>();
		Map<String, String> posMap = new HashMap<String, String>();
		List<Integer> plen = new ArrayList<Integer>();
		int numberOf = 0;
		for (File f : this.positivesFolder.listFiles()) {
			try (FileInputStream fis = new FileInputStream(f)) {
				String content = IOUtils.toString(fis, StandardCharsets.UTF_8);
				plen.add(content.length());
				posMap.put(f.getName(), content);
				numberOf++;
			} catch (IOException e) {
				L.error("", e);
			}
		}
		L.info("{} positive examples loaded", numberOf);
		List<File> files = new ArrayList<File>();
		for (File folder : this.negativesFolders) {
			files.addAll(Arrays.asList(folder.listFiles()));
		}
		
		Map<String, String> negMap = new HashMap<String, String>();

		/* Plain mode */
		if(mode == 0) {
			L.info("Loading negative examples");
			for(File f: files) {
				try (FileInputStream fis = new FileInputStream(f)) {
					String content = IOUtils.toString(fis, StandardCharsets.UTF_8);
					String fname = f.getName();
					negMap.put(fname, content);
				} catch (IOException e) {
					L.error("", e);
				}
			}
		}
		/* Size-aware mode */
		if (mode == 1) {
			L.info("Building negative examples");
			int numberOfNeg = 0;
			
			String content = "";
			String fname = "";
			String remainedContent = "";
			int combined = 0;
			int avgDifference = 0;
			
			while (numberOfNeg < numberOf && !files.isEmpty()) {
				if (content.equals("") || plen.get(numberOfNeg) > content.length()) {
					// Get another file
					File f = files.remove(0);
					L.info("Get file {}", f);
					try (FileInputStream fis = new FileInputStream(f)) {
						content = new StringBuilder().append(content).append(" ").append(IOUtils.toString(fis, StandardCharsets.UTF_8)).toString();
						fname = new StringBuilder().append(fname).append("__").append(f.getName()).toString();
						combined++;
						continue;
					} catch (IOException e) {
						L.error("", e);
					}
				}
				boolean load = false;
				// If positives are bigger and this neg is larger then the
				// respective, take it.
				if (avgDifference >= 0 && content.length() >= plen.get(numberOfNeg)) {
					L.info("[take] positives are bigger, neg bigger");
					load = true;
				} else // If negatives are bigger and this neg is smaller, take
						// it
				if (avgDifference <= 0 && content.length() <= plen.get(numberOfNeg)) {
					L.info("[take] negatives are bigger, neg smaller");
					load = true;
				}
				// If positives are bigger and this neg is smaller, continue
				// appending
				// If positives are smaller and this neg is bigger, crop it and
				// take
				// it!
				else if (avgDifference <= 0 && content.length() >= plen.get(numberOfNeg)) {
					String[] splitc = new String[] { content.substring(0, plen.get(numberOfNeg)), content.substring(1, plen.get(numberOfNeg)) };
					content = splitc[0];
					remainedContent = splitc[1];
					fname = new StringBuilder().append(fname).append("--").append(content.hashCode()).toString();
					load = true;
				}
				if (load) {
					negMap.put(fname, content);
					L.info("{} negative ({} combined,plen:{},nlen:{})", new Object[] { numberOfNeg +
						1, combined, plen.get(numberOfNeg), content.length() });
					avgDifference = (avgDifference + (plen.get(numberOfNeg) - content.length())) /
						2;
					L.info("avgd {}", avgDifference);
					numberOfNeg++;
					if (!"".equals(remainedContent)) {
						content = new StringBuilder().append(remainedContent).append(". ").toString();
						fname = fname.substring(fname.lastIndexOf("--")) + "--" +
							content.hashCode();
					} else {
						content = "";
						fname = "";
					}
					remainedContent = "";
					combined = 0;
				}

				if (numberOfNeg >= numberOf) {
					L.info("Reached the limit of {} negative examples", numberOfNeg);
					break;
				}
			}
			L.info("{} negative examples loaded (avgd: {})", numberOfNeg, avgDifference);
		}
		L.info("Writing {} positive entries", posMap.size());
		L.info("Writing {} negative entries", negMap.size());
		try (FileWriter fw = new FileWriter(outputFile, true)) {
			for (Entry<String, String> entry : posMap.entrySet()) {
				writeEntry(true, entry.getKey(), entry.getValue(), fw);
			}
			for (Entry<String, String> entry : negMap.entrySet()) {
				writeEntry(false, entry.getKey(), entry.getValue(), fw);
			}
		}
	}

	private void writeEntry(boolean positive, String name, String content, FileWriter w) throws IOException {
		w.write((positive) ? "1" : "0");
		w.write(",");
		w.write(name);
		w.write(",");
		w.write(Integer.toString(content.length()));
		w.write(",");
		w.write(StringEscapeUtils.escapeCsv(content.replaceAll("\\R+", " ")));
		w.write("\n");
	}

	public static final void main(String[] args) throws FileNotFoundException, IOException {
		new GenTrainingFile(args).run();
	}
}
