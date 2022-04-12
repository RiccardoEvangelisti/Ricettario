package modelli;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringTokenizer;

public class Ricetta {

	private int ID_RICETTA;
	private String NOME_RICETTA;
	private String PORTATA;
	private int PORZIONI;
	private boolean IS_VEGETARIANA;
	private boolean IS_SENZA_GLUTINE;
	private String PREPARAZIONE;
	private int TEMPO_PREPARAZIONE;
	private boolean COTTURA;

	private List<String> ingredientiList = new ArrayList<>();

	// la lista può contenere URL delle immagini preparazione
	private List<String> listaImmPreparazioneURL = new ArrayList<>();
	// se non è stata trovata un'immagine del piatto, immPiattoURL = Optional.empty()
	Optional<String> immPiattoURL = Optional.empty();

	public Ricetta(int iD_RICETTA, String nOME_RICETTA, String pORTATA, int pORZIONI, boolean iS_VEGETARIANA, boolean iS_SENZA_GLUTINE, String iNGREDIENTI, String pREPARAZIONE, int tEMPO_PREPARAZIONE,
				boolean cOTTURA) throws IOException {
		this.ID_RICETTA = iD_RICETTA;
		this.NOME_RICETTA = nOME_RICETTA;
		this.PORTATA = pORTATA;
		this.PORZIONI = pORZIONI;
		this.IS_VEGETARIANA = iS_VEGETARIANA;
		this.IS_SENZA_GLUTINE = iS_SENZA_GLUTINE;
		this.PREPARAZIONE = pREPARAZIONE;
		this.TEMPO_PREPARAZIONE = tEMPO_PREPARAZIONE;
		this.COTTURA = cOTTURA;

		// tokenizzo gli ingredienti
		StringTokenizer strtok = new StringTokenizer(iNGREDIENTI, ";");
		while (strtok.hasMoreTokens()) {
			ingredientiList.add(strtok.nextToken());
		}

		/* ogni entry.toString() ritorna:<C:\Users\evang.HOMEEVANGELISTI\AppData\Local\Ricettario\17_foto_prep_4.jpg>
		 * mentre per il nodo ImageView e per Files.move deve essere:<file:/C:/Users/evang.HOMEEVANGELISTI/Downloads/nomeImmagine.jpg>
		 */

		// cerco l'immagine di piatto e la salvo nello streamFotoPiatto
		DirectoryStream<Path> streamFotoPiatto = Files.newDirectoryStream(Paths.get(System.getProperty("user.home") + "/AppData/Local/Ricettario/"), String.valueOf(iD_RICETTA) + "_foto{_piatto.*}");
		// scorro lo stream e salvo l'unica foto possibilmente trovata in immPiattoURL
		for (Path entry : streamFotoPiatto) {
			immPiattoURL = Optional.of("file:/" + entry.toString().replace("\\", "/"));
		}

		// cerco le immagini di preparazione e le salvo nello streamFotoPreparazione
		DirectoryStream<Path> streamFotoPreparazione = Files.newDirectoryStream(Paths.get(System.getProperty("user.home") + "/AppData/Local/Ricettario/"),
					String.valueOf(iD_RICETTA) + "_foto_prep_{1.*,2.*,3.*,4.*}");

		// scorro lo stream e salvo l'URL di ciò che ho trovato nella listaImmPreparazioneURL
		for (Path entry : streamFotoPreparazione) {
			listaImmPreparazioneURL.add("file:/" + entry.toString().replace("\\", "/"));
		}

	}

	public int getID_RICETTA() {
		return ID_RICETTA;
	}

	public void setID_RICETTA(int iD_RICETTA) {
		ID_RICETTA = iD_RICETTA;
	}

	public String getNOME_RICETTA() {
		return NOME_RICETTA;
	}

	public void setNOME_RICETTA(String nOME_RICETTA) {
		NOME_RICETTA = nOME_RICETTA;
	}

	public String getPORTATA() {
		return PORTATA;
	}

	public void setPORTATA(String pORTATA) {
		PORTATA = pORTATA;
	}

	public int getPORZIONI() {
		return PORZIONI;
	}

	public void setPORZIONI(int pORZIONI) {
		PORZIONI = pORZIONI;
	}

	public boolean isIS_VEGETARIANA() {
		return IS_VEGETARIANA;
	}

	public void setIS_VEGETARIANA(boolean iS_VEGETARIANA) {
		IS_VEGETARIANA = iS_VEGETARIANA;
	}

	public boolean isIS_SENZA_GLUTINE() {
		return IS_SENZA_GLUTINE;
	}

	public void setIS_SENZA_GLUTINE(boolean iS_SENZA_GLUTINE) {
		IS_SENZA_GLUTINE = iS_SENZA_GLUTINE;
	}

	public List<String> getIngredientiList() {
		return ingredientiList;
	}

	public void setIngredientiList(List<String> ingredientiList) {
		this.ingredientiList = ingredientiList;
	}

	public List<String> getListaImmPreparazioneURL() {
		return listaImmPreparazioneURL;
	}

	public void setListaImmPreparazioneURL(List<String> listaImmPreparazioneURL) {
		this.listaImmPreparazioneURL = listaImmPreparazioneURL;
	}

	public Optional<String> getImmPiattoURL() {
		return immPiattoURL;
	}

	public void setImmPiattoURL(Optional<String> immPiattoURL) {
		this.immPiattoURL = immPiattoURL;
	}

	public String getPREPARAZIONE() {
		return PREPARAZIONE;
	}

	public void setPREPARAZIONE(String pREPARAZIONE) {
		PREPARAZIONE = pREPARAZIONE;
	}

	public int getTEMPO_PREPARAZIONE() {
		return TEMPO_PREPARAZIONE;
	}

	public void setTEMPO_PREPARAZIONE(int tEMPO_PREPARAZIONE) {
		TEMPO_PREPARAZIONE = tEMPO_PREPARAZIONE;
	}

	public boolean isCOTTURA() {
		return COTTURA;
	}

	public void setCOTTURA(boolean cOTTURA) {
		COTTURA = cOTTURA;
	}

}
