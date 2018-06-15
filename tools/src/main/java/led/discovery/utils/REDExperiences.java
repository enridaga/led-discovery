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

public class REDExperiences {
	private static Logger log = LoggerFactory.getLogger(REDExperiences.class);
	public static void downloadExperiences(String target, int limit) throws IOException {
		String queryString = "PREFIX dcterms: <http://purl.org/dc/terms/>\n" + 
			"SELECT *\n" + 
			"FROM <http://data.open.ac.uk/context/red> \n" + 
			" {\n" + 
			"  ?experience <http://purl.org/spar/cito/providesExcerptFor> []; dcterms:description ?text\n" + 
			"    \n" + 
			"} ";
		//System.err.println(queryString);
		if (limit > 0) {
			queryString = new StringBuilder().append(queryString).append(" LIMIT ").append(limit).toString();
		}//else return;
		log.debug("{}", queryString);
		File directory = new File(target);
		if (!directory.exists()) {
			directory.mkdirs();
		} else if (!directory.isDirectory()) {
			throw new IOException("Not a directory: " + directory);
		}
		Query query = QueryFactory.create(queryString);
		ResultSet res = QueryExecutionFactory.createServiceRequest("http://data.open.ac.uk/sparql", query).execSelect();
		while (res.hasNext()) {
			QuerySolution qs = res.next();
			String experienceUri = qs.get("experience").asResource().getURI();
			log.info("Writing {}", experienceUri);
			String experienceId = experienceUri.substring(experienceUri.lastIndexOf('/'));
			String text = qs.get("text").asLiteral().getLexicalForm();
			String fileId = new StringBuilder().append(experienceId).append(".txt").toString();
			
			File location = new File(directory, fileId);
			if(!location.exists()) {
				location.createNewFile();
			}
			FileOutputStream os = new FileOutputStream(location);
			IOUtils.write(text, os, "utf-8");
			os.close();
		}
	}

	public static void main(String[] args) throws IOException {
		log.info("{}",args);
		String target = ".";
		int limit = -1;
		if(args.length > 0) {
			target = args[0];
			if(args.length > 1) {
				limit = Integer.parseInt(args[1]);
			}
		}
		
		REDExperiences.downloadExperiences(target, limit);
	}
}
