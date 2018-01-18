package led.discovery.h2;

public class H2Queries {
	public static final String CREATE_TABLE_TERMS = "CREATE TABLE IF NOT EXISTS TERM(ID INT AUTO_INCREMENT PRIMARY KEY, LEMMA VARCHAR(255), POS VARCHAR(5) NOT NULL, UNIQUE(LEMMA,POS));";
	public static final String CREATE_TABLE_DOCUMENTS = "CREATE TABLE IF NOT EXISTS DOCUMENT(ID INT AUTO_INCREMENT PRIMARY KEY, DOCUMENT VARCHAR(255) NOT NULL, UNIQUE(DOCUMENT));";
	public static final String CREATE_TABLE_TERM_DOCUMENT = "CREATE TABLE IF NOT EXISTS DOCUMENT_TERM(ID INT AUTO_INCREMENT PRIMARY KEY, DOCUMENT INT NOT NULL, TERM INT NOT NULL)";
	//
	public static final String SELECT_CONTAINS_TERM = "SELECT ID FROM TERM WHERE LEMMA = ? AND POS = ? LIMIT 1";
	public static final String SELECT_CONTAINS_DOCUMENT = "SELECT ID FROM DOCUMENT WHERE DOCUMENT = ? LIMIT 1";
	public static final String SELECT_CONTAINS_DOCUMENT_ID = "SELECT ID FROM DOCUMENT WHERE ID = ? LIMIT 1";
	//
	public static final String SELECT_TERMS = "SELECT ID, LEMMA, POS FROM TERM";
	public static final String SELECT_GET_TERM_BY_ID = "SELECT LEMMA, POS FROM TERM WHERE ID = ?";
	public static final String SELECT_GET_DOCUMENT_BY_ID = "SELECT DOCUMENT FROM DOCUMENT WHERE ID = ?";
	public static final String SELECT_GET_ID_OF_TERM = "SELECT ID FROM TERM WHERE LEMMA = ? AND POS = ?";
	public static final String SELECT_GET_ID_OF_DOCUMENT = "SELECT ID FROM DOCUMENT WHERE DOCUMENT = ?";
	public static final String SELECT_GET_TERM_IDS = "SELECT ID FROM TERM";
	public static final String SELECT_GET_DOCUMENT_IDS = "SELECT ID FROM DOCUMENT";
	public static final String SELECT_GET_TERMS = "SELECT LEMMA, POS FROM TERM";
	public static final String SELECT_GET_DOCUMENTS = "SELECT DOCUMENT FROM DOCUMENT";
	//
	public static final String SELECT_COUNT_TERMS = "SELECT COUNT(ID) FROM TERM";
	public static final String SELECT_COUNT_DOCUMENTS = "SELECT COUNT(ID) FROM DOCUMENT";
	public static final String SELECT_COUNT_TERMS_OF_DOCUMENT = "SELECT COUNT(TERM) FROM DOCUMENT_TERM WHERE DOCUMENT = ?";
	public static final String SELECT_COUNT_DISTINCT_TERMS_OF_DOCUMENT = "SELECT COUNT(DISTINCT TERM) FROM DOCUMENT_TERM WHERE DOCUMENT = ?";
	public static final String SELECT_COUNT_TERM_IN_DOCUMENT = "SELECT COUNT(TERM) FROM DOCUMENT_TERM WHERE DOCUMENT = ? AND TERM = ?";
	public static final String SELECT_COUNT_DOCUMENTS_OF_TERM = "SELECT COUNT(DISTINCT DOCUMENT) FROM DOCUMENT_TERM WHERE TERM = ?";
	public static final String SELECT_COUNT_EACH_TERM_IN_DOCUMENT = "SELECT TERM, COUNT(*) FROM DOCUMENT_TERM WHERE DOCUMENT = ? GROUP BY TERM";
	public static final String SELECT_COUNT_DOCUMENTS_HAVING_TERM = "SELECT TERM, COUNT(DISTINCT DOCUMENT) FROM DOCUMENT_TERM GROUP BY TERM";
	public static final String SELECT_COUNT_DOCUMENTS_TERMS = "SELECT DOCUMENT, COUNT(TERM) FROM DOCUMENT_TERM GROUP BY DOCUMENT";;
	//
	public static final String INSERT_TERM = "INSERT INTO TERM (LEMMA,POS) VALUES (?,?)";
	public static final String INSERT_DOCUMENT = "INSERT INTO DOCUMENT (DOCUMENT) VALUES (?)";
	public static final String INSERT_DOCUMENT_TERM = "INSERT INTO DOCUMENT_TERM (DOCUMENT, TERM) VALUES (?,?)";
	
	
	
	
	
}
