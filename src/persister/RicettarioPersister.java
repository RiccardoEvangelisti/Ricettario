package persister;

import java.io.IOException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javafx.scene.control.Alert.AlertType;
import modelli.AlertPanel;
import modelli.Portata;

public class RicettarioPersister {

	Connection connection;

	/**
	 * Questo statement crea (eventualmente) la tabella,
	 */
	Statement statement;

	/* ---- Costruttore --------------------------------------------------------------------------------------- */

	/**
	 * All'accensione del programma viene istanziato il persister che crea la connessione verso la tabella. Se la tabella non è presente, la crea.
	 * 
	 * @throws SQLException
	 */
	public RicettarioPersister() throws SQLException {

		/*
		 * Accensione del database
		 */
		String dbName = "ricettario_DB";
		connection = DriverManager.getConnection("jdbc:derby:" + "ricettario_DB" + ";create=true");

		System.out.println("Connected to and created database " + dbName);// TODO print inutile

		/*
		 * We want to control transactions manually. Autocommit is on by default in JDBC.
		 */
		connection.setAutoCommit(false);

		/*
		 * Creo lo statement
		 */
		statement = connection.createStatement();

		/*
		 *  Controllo che la tabella non esiste: se esiste viene creata
		 */
		if (!tableExists(connection)) {

			AlertPanel.says(AlertType.INFORMATION, "Creazione RICETTARIO_TABLE", "");

			statement.execute("CREATE TABLE RICETTE_TABLE(" + "	ID_RICETTA INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY, " + "	NOME_RICETTA VARCHAR(254) NOT NULL, "
						+ "	PORTATA VARCHAR(10) NOT NULL CHECK(PORTATA='ANTIPASTO' OR PORTATA='PRIMO' OR PORTATA='SECONDO' OR PORTATA='DOLCE' OR PORTATA='CONTORNO'), " + "	PORZIONI INTEGER NOT NULL, "
						+ "	IS_VEGETARIANA BOOLEAN NOT NULL, " + "	IS_SENZA_GLUTINE BOOLEAN NOT NULL, " + "	INGREDIENTI VARCHAR(500) NOT NULL, " + "	PREPARAZIONE VARCHAR(4000) NOT NULL, "
						+ "	TEMPO_PREPARAZIONE INTEGER NOT NULL CHECK(TEMPO_PREPARAZIONE > 0), " + "	COTTURA BOOLEAN NOT NULL " + ")");
		}

	}

	/**
	 * Controlla l'esistenza della tabella. Viene eseguita una UPDATE fittizzia (nessuna riga viene selezionata quindi la update non cambia nulla)
	 * 
	 * @param connessione
	 * @return true, se la tabella esiste, false altrimenti
	 * @throws SQLException
	 */
	public boolean tableExists(Connection connessione) throws SQLException {

		try {
			connessione.createStatement().execute("UPDATE RICETTE_TABLE " + "SET NOME_RICETTA = 'TEST ENTRY' " + "WHERE PORTATA = 'test' AND PORZIONI = 0");
		} catch (SQLException sqle) {

			/*
			 *  Se lo stato è "42X05" la tabella NON ESISTE
			 */
			if (sqle.getSQLState().equals("42X05")) {

				return false;
			}

			/*
			 *  Se lo stato è "42X14" o "42821", la tabella è definita incorrettamente
			 */
			else if (sqle.getSQLState().equals("42X14") || sqle.getSQLState().equals("42821")) {

				System.out.println("Incorrect table definition. Drop RICETTARIO_TABLE and rerun this program");
				throw sqle;
			}

			/*
			 * altrimenti...
			 */
			else {
				System.out.println("Unhandled SQLException");
				throw sqle;
			}
		}

		/*
		 * Se nessuna eccezione viene lanciata, la tabella esiste già.
		 */
		return true;
	}

	/* ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 */
	// SELECT RICETTE

	public ResultSet selectRicette(String selectString) throws SQLException {

		/*
		 * A ResultSet objectis automatically closed by the Statement object that generated it 
		 * when that Statement object is closed, re-executed, or is used to retrieve the next result from asequence of multiple results. 
		 */
		System.out.println(selectString);// TODO print inutile
		ResultSet resultSet = statement.executeQuery(selectString);

		return resultSet;
	}

	/* ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 */
	// SALVA RICETTA

	public void salvaRicetta(String nomeRicetta, Portata portataScelta, Integer porzioni, boolean isVegetariano, boolean isSenzaGlutine, String strIngredienti, String preparazione,
				Integer tempoPreparazione, boolean isCotto, String immPiattoURL, List<String> immPreparazioneURLList) throws SQLException, IOException {

		/* It is recommended to use PreparedStatements when you are
		 * repeating execution of an SQL statement. PreparedStatements also
		 * allows you to parameterize variables. By using PreparedStatements
		 * you may increase performance (because the Derby engine does not
		 * have to recompile the SQL statement each time it is executed) and
		 * improve security (because of Java type checking).
		 */

		// creo lo statement con l'opzione RETURN_GENERATED_KEYS
		PreparedStatement psInsert = connection
					.prepareStatement("INSERT INTO RICETTE_TABLE (NOME_RICETTA, PORTATA, PORZIONI, IS_VEGETARIANA, IS_SENZA_GLUTINE, INGREDIENTI, PREPARAZIONE, TEMPO_PREPARAZIONE, COTTURA) "
								+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);

		// setto tutti i "?"
		psInsert.setString(1, nomeRicetta);
		psInsert.setString(2, portataScelta.toString());
		psInsert.setInt(3, porzioni);
		psInsert.setBoolean(4, isVegetariano);
		psInsert.setBoolean(5, isSenzaGlutine);
		psInsert.setString(6, strIngredienti);
		psInsert.setString(7, preparazione);
		psInsert.setInt(8, tempoPreparazione);
		psInsert.setBoolean(9, isCotto);

		// esecuzione effettiva dell'INSERT
		if (psInsert.executeUpdate() != 1) {
			throw new SQLException("ERRORE: l'esecuzione di psInsert.executeUpdate() non è andata a buon fine");
		}

		/* ---- PREPARAZIONE SALVATAGGIO IMMAGINI --------------------------------------------------------------------------------------------------- */

		// grazie al flag RETURN_GENERATED_KEYS, posso ricavare l'ID generato automaticamente, inserito dentro un ResultSet.
		ResultSet set_Chiavi = psInsert.getGeneratedKeys();
		int id_auto_generated = -1;
		// se esiste la chiave, leggila come intero
		if (set_Chiavi.next()) {
			id_auto_generated = set_Chiavi.getInt(1);
		}

		// chiudo perché non è importante
		set_Chiavi.close();

		/* ---- SALVATAGGIO IMMAGINE PIATTO---------------------------------------------------------------------------------------------------------------- */

		// se è stato inserito un URL...
		if (!immPiattoURL.equals("")) {
			// il nuovo nome è formato da: id_auto_generated_foto_piatto.png (l'estensione dell'immagine originale)
			String nomeImmPiattoURL = String.valueOf(id_auto_generated) + "_foto_piatto" + immPiattoURL.substring(immPiattoURL.lastIndexOf("."));
			// SPOSTO l'immagine dall'origine (qualunque essa sia) a user.home/AppData/Local/Ricettario/id_auto_generated_foto_piatto.png
			Files.move(Paths.get(URI.create(immPiattoURL)), Paths.get(System.getProperty("user.home") + "/AppData/Local/Ricettario/" + nomeImmPiattoURL));
		}

		/* ---- SALVATAGGIO IMMAGINI PREPARAZIONE---------------------------------------------------------------------------------------------------------------- */

		// se la lista non è vuota...
		if (!immPreparazioneURLList.isEmpty()) {
			String nomeImmPrepURL = "";

			// per ogni immagine_preparazione_URL...
			for (int index1 = 0; index1 < 4; index1++) {

				// il nuovo nome è formato da: id_auto_generated__foto_prep_1.png (l'estensione dell'immagine originale)
				nomeImmPrepURL = String.valueOf(id_auto_generated) + "_foto_prep_" + String.valueOf(index1 + 1)
							+ immPreparazioneURLList.get(index1).substring(immPreparazioneURLList.get(index1).lastIndexOf("."));

				// SPOSTO l'immagine dall'origine (qualunque essa sia) a user.home/AppData/Local/Ricettario/id_auto_generated__foto_prep_1.png
				Files.move(Paths.get(URI.create(immPreparazioneURLList.get(index1))), Paths.get(System.getProperty("user.home") + "/AppData/Local/Ricettario/" + nomeImmPrepURL));
			}
		}

		// rilascio delle risorse
		psInsert.close();
	}

	/* ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 */
	// UPDATE RICETTA

	public void updateRicetta(int iD_ricettaDaModificare, String nomeRicetta, Portata portataScelta, Integer porzioni, boolean isVegetariano, boolean isSenzaGlutine, String strIngredienti,
				String preparazione, Integer tempoPreparazione, boolean isCotto, String immPiattoURL, List<String> immPreparazioneURLList) throws SQLException, IOException {

		// creo lo statement
		PreparedStatement psUpdate = connection.prepareStatement(
					"UPDATE RICETTE_TABLE SET NOME_RICETTA = ?, PORTATA = ?, PORZIONI = ?, IS_VEGETARIANA = ?, IS_SENZA_GLUTINE = ?, INGREDIENTI = ?, PREPARAZIONE = ?, TEMPO_PREPARAZIONE = ?, COTTURA = ? "
								+ " WHERE ID_RICETTA = ? ");

		// setto tutti i "?"
		psUpdate.setString(1, nomeRicetta);
		psUpdate.setString(2, portataScelta.toString());
		psUpdate.setInt(3, porzioni);
		psUpdate.setBoolean(4, isVegetariano);
		psUpdate.setBoolean(5, isSenzaGlutine);
		psUpdate.setString(6, strIngredienti);
		psUpdate.setString(7, preparazione);
		psUpdate.setInt(8, tempoPreparazione);
		psUpdate.setBoolean(9, isCotto);

		psUpdate.setInt(10, iD_ricettaDaModificare);

		// esecuzione
		if (psUpdate.executeUpdate() != 1) {
			throw new SQLException("ERRORE: l'esecuzione di psUpdate.executeUpdate() non è andata a buon fine");
		}

		/*TODO ERRORE DI IMPLEMENTAZIONE: la funzione updateRicetta() si limita a prendere nuova immagine (il suo percorso completo) e sovrascriverla alla vecchia immagine.
		 * Se la nuova immagine non è presente, a significare che vuole essere rimossa, il programma non entra negli if e non cancella le immagini. 
		 * Dunque cancellare un'immagine non è possibile: è possible solamente cancellarla dalla cartella Ricettario, oppure si sovrascrive con una nuova immagine.
		 * */

		/* ---- SALVATAGGIO IMMAGINE PIATTO---------------------------------------------------------------------------------------------------------------- */

		// se è stato inserito un URL...
		if (!immPiattoURL.equals("")) {

			// il nuovo nome è formato da: id_auto_generated_foto_piatto.png (l'estensione dell'immagine originale)
			String nomeImmPiattoURL = String.valueOf(iD_ricettaDaModificare) + "_foto_piatto" + immPiattoURL.substring(immPiattoURL.lastIndexOf("."));
			// SPOSTO l'immagine dall'origine (qualunque essa sia) a user.home/AppData/Local/Ricettario/id_auto_generated_foto_piatto.png
			Files.move(Paths.get(URI.create(immPiattoURL)), Paths.get(System.getProperty("user.home") + "/AppData/Local/Ricettario/" + nomeImmPiattoURL),
						java.nio.file.StandardCopyOption.REPLACE_EXISTING);
		}

		/* ---- SALVATAGGIO IMMAGINI PREPARAZIONE---------------------------------------------------------------------------------------------------------------- */

		// se la lista non è vuota...
		if (!immPreparazioneURLList.isEmpty()) {

			String nomeImmPrepURL = "";

			// per ogni immagine_preparazione_URL...
			for (int index1 = 0; index1 < 4; index1++) {

				// il nuovo nome è formato da: id_auto_generated__foto_prep_1.png (l'estensione dell'immagine originale)
				nomeImmPrepURL = String.valueOf(iD_ricettaDaModificare) + "_foto_prep_" + String.valueOf(index1 + 1)
							+ immPreparazioneURLList.get(index1).substring(immPreparazioneURLList.get(index1).lastIndexOf("."));

				// SPOSTO l'immagine dall'origine (qualunque essa sia) a user.home/AppData/Local/Ricettario/id_auto_generated__foto_prep_1.png
				Files.move(Paths.get(URI.create(immPreparazioneURLList.get(index1))), Paths.get(System.getProperty("user.home") + "/AppData/Local/Ricettario/" + nomeImmPrepURL),
							java.nio.file.StandardCopyOption.REPLACE_EXISTING);
			}
		}

		// rilascio delle risorse
		psUpdate.close();
	}

	/* ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 */
	// ELIMINA RICETTA

	public void eliminaRicetta(Integer ID_da_eliminare) throws SQLException, IOException {

		// creo lo statement
		PreparedStatement psDelete = connection.prepareStatement("DELETE FROM RICETTE_TABLE WHERE ID_RICETTA = ?");

		// setto il "?"
		psDelete.setInt(1, ID_da_eliminare);

		// esecuzione
		if (psDelete.executeUpdate() != 1) {
			throw new SQLException("ERRORE: l'esecuzione di psDelete.executeUpdate() non è andata a buon fine");
		}

		// rilascio delle risorse
		psDelete.close();

		/* ----  IMMAGINE PIATTO---------------------------------------------------------------------------------------------------------------- */

		// elimino l'immagine esistente:
		// cerco l'immagine di piatto e la salvo nello streamFotoPiatto
		DirectoryStream<Path> streamFotoPiatto = Files.newDirectoryStream(Paths.get(System.getProperty("user.home") + "/AppData/Local/Ricettario/"),
					String.valueOf(ID_da_eliminare) + "_foto{_piatto.*}");
		// scorro lo stream e cancello l'immagine corrispondente
		for (Path entry : streamFotoPiatto) {
			Files.delete(entry);
		}

		/* ----  IMMAGINI PREPARAZIONE---------------------------------------------------------------------------------------------------------------- */

		// cerco le immagini di preparazione e le salvo nello streamFotoPreparazione
		DirectoryStream<Path> streamFotoPreparazione = Files.newDirectoryStream(Paths.get(System.getProperty("user.home") + "/AppData/Local/Ricettario/"),
					String.valueOf(ID_da_eliminare) + "_foto_prep_{1.*,2.*,3.*,4.*}");

		// scorro lo stream e cancello l'immagine corrispondente
		for (Path entry : streamFotoPreparazione) {
			Files.delete(entry);
		}

	}

	/* ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 */

	/**
	 * Metodo che chiude la connessione e spegne il database. <br>
	 * Viene invocato quando si chiude lo stage del ricettario. <br>
	 * Avviene tutto correttamente se viene lanciata una precisa eccezione, che se non è quella giusta il metodo lancia a sua volta una SQLException.
	 * 
	 * @throws SQLException
	 */

	public void closeDatabase() throws SQLException {

		/*
		 * Comando che comunica al server il termine delle operazioni
		 */
		connection.commit();

		/*
		 *  Rilascio delle risorse
		 */
		statement.close();
		connection.close();

		/*
		 * In embedded mode, an application should shut down Derby.
		 * Shutdown throws the XJ015 exception to confirm success.
		 */
		try {
			DriverManager.getConnection("jdbc:derby:;shutdown=true");
		} catch (SQLException se) {

			if (se.getSQLState().equals("XJ015") && se.getErrorCode() == 50000) {

				// success
				System.out.println("Derby shut down normally"); // TODO print inutile
			} else {

				// failure
				throw se;
			}
		}

	}

}
