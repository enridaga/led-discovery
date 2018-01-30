package led.discovery.db;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import led.discovery.nlp.Term;

public class H2TermsDatabase implements TermsDatabase {
	private static final Logger l = LoggerFactory.getLogger(H2TermsDatabase.class);
	protected String connectionString;
	protected String username;
	protected String password;

	static {
		try {
			Class.forName("org.h2.Driver");
		} catch (Exception e) {
			l.error("Error loading JDBC driver", e);
		}
	}

	public H2TermsDatabase(File location) {
		this(location, "led-discovery-terms", "ledd", "ledd");
	}

	public H2TermsDatabase(File location, String database, String user, String password) {
		this(location, database, user, password, "MVCC=TRUE;DB_CLOSE_ON_EXIT=TRUE;FILE_LOCK=NO");
	}

	public H2TermsDatabase(File location, String dbname, String user, String password, String options) {
		l.trace("Opening instance on folder {}", location);
		this.connectionString = "jdbc:h2:file:" + location.getAbsolutePath() + "/" + dbname + ";" + options;
		this.username = user;
		this.password = password;
		setup();
	}

	private void setup() {
		try (Connection conn = getConnection()) {
			conn.setAutoCommit(false);
			conn.createStatement().execute(H2Queries.CREATE_TABLE_DOCUMENTS);
			conn.createStatement().execute(H2Queries.CREATE_TABLE_TERMS);
			conn.createStatement().execute(H2Queries.CREATE_TABLE_TERM_DOCUMENT);
			conn.commit();
			conn.setAutoCommit(true);
		} catch (IOException | SQLException e) {
			l.error("SQL Exception", e.getMessage());
			throw new RuntimeException(e);
		}
	}

	public Connection getConnection() throws IOException {
		Connection conn;
		try {
			conn = DriverManager.getConnection(connectionString, username, password);
		} catch (SQLException e) {
			l.error("SQL Exception", e.getMessage());
			throw new IOException(e);
		}
		return conn;
	}
	
	@Override
	public boolean containsTerm(Term term) throws IOException {
		try (Connection conn = getConnection(); PreparedStatement st = conn.prepareStatement(H2Queries.SELECT_CONTAINS_TERM)) {
			st.setString(1, term.getLemma());
			st.setString(2, term.getPOS());
			ResultSet rs = st.executeQuery();
			return rs.first();
		} catch (IOException | SQLException e) {
			l.error("SQL Exception", e.getMessage());
			throw new IOException(e);
		}
	}

	@Override
	public int addTerm(Term term) throws IOException {
		if (containsTerm(term))
			return getTermId(term);
		try (Connection conn = getConnection(); PreparedStatement st = conn.prepareStatement(H2Queries.INSERT_TERM, Statement.RETURN_GENERATED_KEYS)) {
			st.setString(1, term.getLemma());
			st.setString(2, term.getPOS());
			st.execute();
			ResultSet rs = st.getGeneratedKeys();
			rs.first();
			int id = rs.getInt(1);
			return id;
		} catch (IOException | SQLException e) {
			l.error("SQL Exception", e.getMessage());
			throw new IOException(e);
		}
	}

	@Override
	public Term getTerm(int termId) throws IOException {
		try (Connection conn = getConnection(); PreparedStatement st = conn.prepareStatement(H2Queries.SELECT_GET_TERM_BY_ID)) {
			st.setInt(1, termId);
			ResultSet rs = st.executeQuery();
			if (rs.first()) {
				return Term.build(rs.getString(1), rs.getString(2));
			} else {
				throw new IOException("term does not exist");
			}
		} catch (IOException | SQLException e) {
			l.error("SQL Exception", e.getMessage());
			throw new IOException(e);
		}
	}

	@Override
	public int getTermId(Term term) throws IOException {
		try (Connection conn = getConnection(); PreparedStatement st = conn.prepareStatement(H2Queries.SELECT_GET_ID_OF_TERM)) {
			st.setString(1, term.getLemma());
			st.setString(2, term.getPOS());
			ResultSet rs = st.executeQuery();
			if (rs.first()) {
				return rs.getInt(1);
			} else {
				throw new IOException("term does not exist");
			}
		} catch (IOException | SQLException e) {
			l.error("SQL Exception", e.getMessage());
			throw new IOException(e);
		}
	}

	@Override
	public int countTerms(int docId) throws IOException {
		try (Connection conn = getConnection(); PreparedStatement st = conn.prepareStatement(H2Queries.SELECT_COUNT_TERMS_OF_DOCUMENT)) {
			st.setInt(1, docId);
			ResultSet rs = st.executeQuery();
			if (rs.first()) {
				return rs.getInt(1);
			} else {
				throw new IOException("term does not exist");
			}
		} catch (IOException | SQLException e) {
			l.error("SQL Exception", e.getMessage());
			throw new IOException(e);
		}
	}

	@Override
	public int getDocId(String name) throws IOException {
		try (Connection conn = getConnection(); PreparedStatement st = conn.prepareStatement(H2Queries.SELECT_GET_ID_OF_DOCUMENT)) {
			st.setString(1, name);
			ResultSet rs = st.executeQuery();
			if (rs.first()) {
				return rs.getInt(1);
			} else {
				throw new IOException("term does not exist");
			}
		} catch (IOException | SQLException e) {
			l.error("SQL Exception", e.getMessage());
			throw new IOException(e);
		}
	}

	@Override
	public String getDocName(int docId) throws IOException {
		try (Connection conn = getConnection(); PreparedStatement st = conn.prepareStatement(H2Queries.SELECT_GET_DOCUMENT_BY_ID)) {
			st.setInt(1, docId);
			ResultSet rs = st.executeQuery();
			if (rs.first()) {
				return rs.getString(1);
			} else {
				throw new IOException("term does not exist");
			}
		} catch (IOException | SQLException e) {
			l.error("SQL Exception", e.getMessage());
			throw new IOException(e);
		}
	}

	@Override
	public boolean containsDocument(String name) throws IOException {
		try (Connection conn = getConnection(); PreparedStatement st = conn.prepareStatement(H2Queries.SELECT_CONTAINS_DOCUMENT)) {
			st.setString(1, name);
			ResultSet rs = st.executeQuery();
			return rs.first();
		} catch (IOException | SQLException e) {
			l.error("SQL Exception", e.getMessage());
			throw new IOException(e);
		}
	}

	@Override
	public boolean containsDocument(int docId) throws IOException {
		try (Connection conn = getConnection(); PreparedStatement st = conn.prepareStatement(H2Queries.SELECT_CONTAINS_DOCUMENT_ID)) {
			st.setInt(1, docId);
			ResultSet rs = st.executeQuery();
			return rs.first();
		} catch (IOException | SQLException e) {
			l.error("SQL Exception", e.getMessage());
			throw new IOException(e);
		}
	}

	@Override
	public int addDocument(String name, List<Term> terms) throws IOException {
		int id;
		try (Connection conn = getConnection()) {
			conn.setAutoCommit(false);
			//

			try {
				try (PreparedStatement st = conn.prepareStatement(H2Queries.INSERT_DOCUMENT, Statement.RETURN_GENERATED_KEYS)) {
					st.setString(1, name);
					st.execute();
					ResultSet rs = st.getGeneratedKeys();
					rs.first();
					id = rs.getInt(1);
				}
				//
				try (PreparedStatement insert = conn.prepareStatement(H2Queries.INSERT_DOCUMENT_TERM)) {
					for (Term term : terms) {
						int lid = addTerm(term);
						insert.setInt(1, id);
						insert.setInt(2, lid);
						insert.execute();
					}

				}
				conn.commit();
			} finally {
				conn.setAutoCommit(true);
			}

		} catch (IOException | SQLException e) {
			l.error("SQL Exception", e.getMessage());
			throw new IOException(e);
		}
		return id;
	}

	@Override
	public int countTerm(int docId, int termId) throws IOException {
		try (Connection conn = getConnection(); PreparedStatement st = conn.prepareStatement(H2Queries.SELECT_COUNT_TERM_IN_DOCUMENT)) {
			st.setInt(1, docId);
			st.setInt(2, termId);
			ResultSet rs = st.executeQuery();
			if (rs.first()) {
				return rs.getInt(1);
			} else {
				throw new IOException("term does not exist");
			}
		} catch (IOException | SQLException e) {
			l.error("SQL Exception", e.getMessage());
			throw new IOException(e);
		}
	}

	@Override
	public int countTerms() throws IOException {
		try (Connection conn = getConnection(); PreparedStatement st = conn.prepareStatement(H2Queries.SELECT_COUNT_TERMS)) {
			ResultSet rs = st.executeQuery();
			if (rs.first()) {
				return rs.getInt(1);
			} else {
				throw new IOException("term does not exist");
			}
		} catch (IOException | SQLException e) {
			l.error("", e.getMessage());
			throw new IOException(e);
		}
	}

	@Override
	public int countDocuments() throws IOException {
		try (Connection conn = getConnection(); PreparedStatement st = conn.prepareStatement(H2Queries.SELECT_COUNT_DOCUMENTS)) {
			ResultSet rs = st.executeQuery();
			if (rs.first()) {
				return rs.getInt(1);
			} else {
				throw new IOException("term does not exist");
			}
		} catch (IOException | SQLException e) {
			l.error("", e.getMessage());
			throw new IOException(e);
		}
	}

	@Override
	public int countDocumentsContainingTerm(int termId) throws IOException {
		try (Connection conn = getConnection(); PreparedStatement st = conn.prepareStatement(H2Queries.SELECT_COUNT_DOCUMENTS_OF_TERM)) {
			st.setInt(1, termId);
			ResultSet rs = st.executeQuery();
			if (rs.first()) {
				return rs.getInt(1);
			} else {
				throw new IOException("term does not exist");
			}
		} catch (IOException | SQLException e) {
			l.error("", e.getMessage());
			throw new IOException(e);
		}
	}

	@Override
	public int getTermCount(int docId, Term term) throws IOException {
		int termId = getTermId(term);
		try (Connection conn = getConnection(); PreparedStatement st = conn.prepareStatement(H2Queries.SELECT_COUNT_TERM_IN_DOCUMENT)) {
			st.setInt(1, docId);
			st.setInt(2, termId);
			ResultSet rs = st.executeQuery();
			if (rs.first()) {
				return rs.getInt(1);
			} else {
				throw new IOException("Document does not exist");
			}
		} catch (IOException | SQLException e) {
			l.error("", e.getMessage());
			throw new IOException(e);
		}
	}

	@Override
	public List<Integer> getDocumentIds() throws IOException {
		List<Integer> ids = new ArrayList<Integer>();
		try (Connection conn = getConnection(); PreparedStatement st = conn.prepareStatement(H2Queries.SELECT_GET_DOCUMENT_IDS)) {
			ResultSet rs = st.executeQuery();
			while (rs.next()) {
				ids.add(rs.getInt(1));
			}
		} catch (IOException | SQLException e) {
			l.error("", e.getMessage());
			throw new IOException(e);
		}
		return Collections.unmodifiableList(ids);
	}

	@Override
	public List<Integer> getTermIds() throws IOException {
		List<Integer> ids = new ArrayList<Integer>();
		try (Connection conn = getConnection(); PreparedStatement st = conn.prepareStatement(H2Queries.SELECT_GET_TERM_IDS)) {
			ResultSet rs = st.executeQuery();
			while (rs.next()) {
				ids.add(rs.getInt(1));
			}
		} catch (IOException | SQLException e) {
			l.error("", e.getMessage());
			throw new IOException(e);
		}
		return Collections.unmodifiableList(ids);
	}

	@Override
	public List<String> getDocuments() throws IOException {
		List<String> ids = new ArrayList<String>();
		try (Connection conn = getConnection(); PreparedStatement st = conn.prepareStatement(H2Queries.SELECT_GET_DOCUMENTS)) {
			ResultSet rs = st.executeQuery();
			while (rs.next()) {
				ids.add(rs.getString(1));
			}
		} catch (IOException | SQLException e) {
			l.error("", e.getMessage());
			throw new IOException(e);
		}
		return Collections.unmodifiableList(ids);
	}

	@Override
	public List<Term> getTerms() throws IOException {
		List<Term> terms = new ArrayList<Term>();
		try (Connection conn = getConnection(); PreparedStatement st = conn.prepareStatement(H2Queries.SELECT_GET_TERMS)) {
			ResultSet rs = st.executeQuery();
			while (rs.next()) {
				terms.add(Term.build(rs.getString(1), rs.getString(2)));
			}
		} catch (IOException | SQLException e) {
			l.error("", e.getMessage());
			throw new IOException(e);
		}
		return Collections.unmodifiableList(terms);
	}
	
	@Override
	public Map<Integer, Integer> countEachTerm(int docId) throws IOException {
		Map<Integer, Integer> countEachTerm = new HashMap<Integer,Integer>();
		try (Connection conn = getConnection(); PreparedStatement st = conn.prepareStatement(H2Queries.SELECT_COUNT_EACH_TERM_IN_DOCUMENT)) {
			st.setInt(1, docId);
			ResultSet rs = st.executeQuery();
			while (rs.next()) {
				countEachTerm.put(rs.getInt(1), rs.getInt(2));
			}
		} catch (IOException | SQLException e) {
			l.error("", e.getMessage());
			throw new IOException(e);
		}
		return Collections.unmodifiableMap(countEachTerm);
	}
	@Override
	public Map<Integer, Integer> countDocumentsContainingTermIds() throws IOException {
		Map<Integer, Integer> countDocuments = new HashMap<Integer,Integer>();
		try (Connection conn = getConnection(); PreparedStatement st = conn.prepareStatement(H2Queries.SELECT_COUNT_DOCUMENTS_HAVING_TERM)) {
			ResultSet rs = st.executeQuery();
			while (rs.next()) {
				// 1 - termId 2 - number of docs
				countDocuments.put(rs.getInt(1), rs.getInt(2));
			}
		} catch (IOException | SQLException e) {
			l.error("", e.getMessage());
			throw new IOException(e);
		}
		return Collections.unmodifiableMap(countDocuments);
	}
	
	@Override
	public Map<Integer, Integer> countDocumentTerms() throws IOException {
		Map<Integer, Integer> countTerms = new HashMap<Integer,Integer>();
		try (Connection conn = getConnection(); PreparedStatement st = conn.prepareStatement(H2Queries.SELECT_COUNT_DOCUMENTS_TERMS)) {
			ResultSet rs = st.executeQuery();
			while (rs.next()) {
				// 1 - docId 2 - number of terms
				countTerms.put(rs.getInt(1), rs.getInt(2));
			}
		} catch (IOException | SQLException e) {
			l.error("", e.getMessage());
			throw new IOException(e);
		}
		return Collections.unmodifiableMap(countTerms);
	}
	
	@Override
	public Map<Integer, Term> getTermAndIds() throws IOException {
		Map<Integer, Term> termsAndIds = new HashMap<Integer,Term>();
		try (Connection conn = getConnection(); PreparedStatement st = conn.prepareStatement(H2Queries.SELECT_TERMS)) {
			ResultSet rs = st.executeQuery();
			while (rs.next()) {
				// 1 - docId 2 - number of terms
				termsAndIds.put(rs.getInt(1), Term.build(rs.getString(2), rs.getString(3)));
			}
		} catch (IOException | SQLException e) {
			l.error("", e.getMessage());
			throw new IOException(e);
		}
		return Collections.unmodifiableMap(termsAndIds);
	}
}
