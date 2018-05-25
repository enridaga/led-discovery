package led.discovery.analysis.entities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GenEntityTypes {
	private static final Logger L = LoggerFactory.getLogger(GenEntityTypes.class);
	private File dataDir;
	private File output;
	private File entities;

	public GenEntityTypes(String[] args) {
		dataDir = new File(args[0]);
		entities = new File(dataDir, "analysis/experiences-entities.csv");
		output = new File(dataDir, "analysis/entities-types.csv");
	}

	void run() throws FileNotFoundException, IOException {
		Set<String> allEntities = new HashSet<String>();
		try (CSVParser reader = new CSVParser(new FileReader(entities), CSVFormat.DEFAULT);) {
			Iterator<CSVRecord> i = reader.iterator();
			while (i.hasNext()) {
				CSVRecord r = i.next();
				allEntities.add(r.get(1));
			}
		}
		// Prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>
		//
		// CONSTRUCT
		// {<%X%> a ?t . <http://dbpedia.org/resource/Organist> a ?t1 }
		// WHERE
		// {{<%X%> a ?t . optional {?t rdfs:subClassOf ?t1}}}
		
//		Prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>
//
//			CONSTRUCT 
//			{<XXXXX> a ?t . <XXXXX> a ?t1 } 
//			WHERE
//			{{<XXXXX> a ?t . optional {?t rdfs:subClassOf+ ?t1}}}
		try (FileWriter fw = new FileWriter(output, true)) {
			String url = "http://dbpedia.org/sparql?default-graph-uri=http%3A%2F%2Fdbpedia.org&query=Prefix+rdfs%3A+%3Chttp%3A%2F%2Fwww.w3.org%2F2000%2F01%2Frdf-schema%23%3E%0D%0A%0D%0ACONSTRUCT+%0D%0A%7B%3CXXXXX%3E+a+%3Ft+.+%3CXXXXX%3E+a+%3Ft1+%7D+%0D%0AWHERE%0D%0A%7B%7B%3CXXXXX%3E+a+%3Ft+.+optional+%7B%3Ft+rdfs%3AsubClassOf%2B+%3Ft1%7D%7D%7D&format=text%2Fplain&CXML_redir_for_subjs=121&CXML_redir_for_hrefs=&timeout=30000&debug=on&run=+Run+Query+";
			for (String entity : allEntities) {
				L.info("{}", entity);
				try (InputStream is = new URL(url.replaceAll("XXXXX", entity)).openConnection().getInputStream();) {
					String str = IOUtils.toString(is, StandardCharsets.UTF_8);
					fw.write(str);
					if(!str.endsWith("\n")) {
						fw.write("\n");
					}
					Thread.sleep(500);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static final void main(String[] args) throws IOException {
		new GenEntityTypes(args).run();
	}
}
