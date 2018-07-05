package led.discovery.spark;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import led.discovery.analysis.entities.DBPediaTypes;

/**
 * Generating Bags of Terms (Lemma+POS)
 * 
 * @author enridaga
 *
 */
public class GenTypesFeatures extends GenEntitiesFeatures {
	private File typesDump = null;
	protected static final Logger L = LoggerFactory.getLogger(GenTypesFeatures.class);
	private DBPediaTypes dbpediaTypes;

	public GenTypesFeatures(String[] args) throws IOException {
		super(args);
		this.typesDump = new File(trainingFile.getParentFile().getParentFile(), "dbpedia/instance_types_en.ttl");
		dbpediaTypes = new DBPediaTypes(typesDump);
	}

	protected String prepareFeatures(String sourceId, String txt, boolean positive) {
		String entities = super.prepareFeatures(sourceId, txt, positive);
		String[] entitiess = entities.split(" ");
		List<String> types = new ArrayList<String>();
		for (String entity : entitiess) {
			Set<String> et = dbpediaTypes.types(entity);
			types.addAll(et);
		}

		String[] s = types.toArray(new String[types.size()]);
		L.trace("{}", Arrays.asList(s));
		L.debug("{} types", s.length);
		return StringUtils.join(s, " ");
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {
		new GenTypesFeatures(args).run();
	}
}
