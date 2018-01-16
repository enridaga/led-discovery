package led.discovery.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LEDExperiences {
	private static Logger log = LoggerFactory.getLogger(LEDExperiences.class);
	public static void downloadExperiences(String target, int limit) throws IOException {
		String queryString = "PREFIX led: <http://led.kmi.open.ac.uk/term/>\n" + 
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
				"\n" + 
				"SELECT ?experience ?text\n" + 
				"FROM <http://data.open.ac.uk/context/led>\n" + 
				"WHERE {\n" + 
				"  ?experience rdf:type led:ListeningExperience .\n" + 
				"  OPTIONAL {?experience led:is_reported_in/rdf:value ?text} .\n" + 
				"  OPTIONAL {?experience led:has_evidence_text ?text}\n" + 
				"}";
		if (limit > 0) {
			queryString = new StringBuilder().append(queryString).append(" LIMIT ").append(limit).toString();
		}
		log.debug("{}", queryString);
		
		Query query = QueryFactory.create(queryString);
		ResultSet res = QueryExecutionFactory.createServiceRequest("http://data.open.ac.uk/sparql", query).execSelect();
		while (res.hasNext()) {
			
			QuerySolution qs = res.next();
			String experienceUri = qs.get("experience").asResource().getURI();
			log.info("Downloading {}", experienceUri);
			String experienceId = experienceUri.substring(experienceUri.lastIndexOf('/'));
			String text = qs.get("text").asLiteral().getLexicalForm();
			String fileId = new StringBuilder().append(experienceId).append(".txt").toString();
			
			File directory = new File(target);
			if (!directory.exists()) {
				directory.mkdirs();
			} else if (!directory.isDirectory()) {
				throw new IOException("Not a directory: " + directory);
			}
			File location = new File(directory, fileId);
			if(!location.exists()) {
				location.createNewFile();
			}
			IOUtils.write(text, new FileOutputStream(location), "utf-8");
		}
	}

	public static void main(String[] args) throws IOException {
		String target = ".";
		int limit = -1;
		if(args.length > 0) {
			target = args[0];
			if(args.length > 1) {
				limit = Integer.parseInt(args[1]);
			}
		}
		
		LEDExperiences.downloadExperiences(target, limit);
	}
}
