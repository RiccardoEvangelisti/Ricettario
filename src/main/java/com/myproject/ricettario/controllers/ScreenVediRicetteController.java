package com.myproject.ricettario.controllers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.jfoenix.controls.JFXListView;
import com.myproject.ricettario.modelli.AlertPanel;
import com.myproject.ricettario.modelli.Portata;
import com.myproject.ricettario.modelli.Ricetta;
import com.myproject.ricettario.persister.RicettarioPersister;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;

public class ScreenVediRicetteController {

	private static final Integer MAX_PORZIONI = 99;

	private RicettarioPersister persister;
	private ScreenPrimaryController screenPrimaryController;

	/* ---- Costruttore --------------------------------------------------------------------------------------- */
	public ScreenVediRicetteController(RicettarioPersister persister, ScreenPrimaryController screenPrimaryController) {
		this.persister = persister;
		this.screenPrimaryController = screenPrimaryController;
	}

	/* ---- initialize() --------------------------------------------------------------------------------------- */
	@FXML
	public void initialize() {

		/*
		 * PORZIONI: Settaggio spinner porzioni, da 0 a MAX_PORZIONI. Ogni volta che si clicca sullo spinner, il radioButton tutteRicette si disattiva.
		 */
		spinnerPorzioni.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, MAX_PORZIONI));
		spinnerPorzioni.setOnMousePressed(e -> {
			spinnerPorzioni.setOpacity(1);
			tuttePorzioni.setSelected(false);
		});

		/*
		 * PORZIONI: RadioButton tuttePorzioni disattiva lo spinner accanto e lo setta a 1.
		 */
		tuttePorzioni.setOnMousePressed(p -> {
			spinnerPorzioni.getValueFactory().setValue(1);
			spinnerPorzioni.setOpacity(0.5);
		});

		/*
		 *  MOSTRO subito le ricette
		 */
		// mostraRicette(null);

	}

	/* ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 */
	// MOSTRA RICETTE
	/**
	 * Viene eseguita cliccando sul pulsante "Cerca ricette" e NON all'avvio del programma. <br>
	 * 1) Viene costruita la stringa che costituisce la query.<br>
	 * 2) Esecuzione della select chiamando il persister. <br>
	 * 3) Ciclaggio del set in uscita dalla select. Per ogni riga trovata, si crea un RigaBox con la ricetta all'interno.<br>
	 * 4) Visualizzazione del risultato mediante una JFXListView dentro il tabPanel.<br>
	 */
	@FXML
	public void mostraRicette(ActionEvent e) {

		/*
		 *  1) Viene costruita la stringa che costituisce la query
		 */
		String selectQuery = selectQuery();

		/*
		 * 2) Esecuzione della select chiamando il persister
		 */
		ResultSet setFiltrate = null;
		try {
			setFiltrate = persister.selectRicette(selectQuery);
		} catch (SQLException e1) {
			AlertPanel.says(AlertType.ERROR, "ERRORE: nella select", Optional.of(e1));
		}

		/*
		 * 3) Ciclaggio del set in uscita dalla select. Per ogni riga trovata, si crea un RigaBox con la ricetta all'interno.
		 */
		// Lista delle RigaBox che entrerà nella listaJFXListView.
		List<AnchorPane> listaRigaBox = new ArrayList<>();

		// A ResultSet cursor is initially positioned before the first row
		// Prelevo i dati da ogni riga e li metto il costruttore di un nuovo RigaBox, il quale viene aggiunto alla listaRigaBox
		AnchorPane root = null;
		try {
			while (setFiltrate.next()) {

				Ricetta ricetta = new Ricetta(setFiltrate.getInt(1), setFiltrate.getString(2), setFiltrate.getString(3), setFiltrate.getInt(4), setFiltrate.getBoolean(5), setFiltrate.getBoolean(6),
							setFiltrate.getString(7), setFiltrate.getString(8), setFiltrate.getInt(9), setFiltrate.getBoolean(10));

				root = FXMLLoader.<AnchorPane>load(getClass().getResource("/view/RigaBox.fxml"), null, null, callback -> {
					return new ControllerRigaBox(ricetta, tabPaneListaRisultati, initialScreenTabElencoRicette, screenPrimaryController);
				});

				listaRigaBox.add(root);
			}
		} catch (Exception e2) {
			AlertPanel.says(AlertType.ERROR, "ERRORE: nel percorrere il set del database, oppure nel creare un RigaBox", Optional.of(e2));
		}

		/*
		 * 4) Visualizzazione del risultato mediante una JFXListView dentro il tabPanel.
		 */

		// se la listaRigaBox non è vuota...
		if (!listaRigaBox.isEmpty()) {

			// Lista di tutte le RigaBox da mettere in un nuovo tab.
			JFXListView<AnchorPane> listaJFXListView = new JFXListView<>();
			listaJFXListView.setStyle("-fx-background-color:#f5f5f5");
			listaJFXListView.setExpanded(true);
			listaJFXListView.depthProperty().set(1);

			// metto la lista di rigaBox dentro la listaJFXListView
			listaJFXListView.setItems(FXCollections.observableArrayList(listaRigaBox));

			// se vengono selezionate tutte le ricette, si mostra nel tab iniziale
			if (selectQuery.equals("SELECT * FROM RICETTE_TABLE WHERE ID_RICETTA != 0  ")) {

				tutteRicetteTab.setContent(listaJFXListView);
				tabPaneListaRisultati.getSelectionModel().select(tutteRicetteTab);
			}

			// altrimenti si fa un nuovo tab col risultato
			else {

				// Tab che contiene la lista dei risultati della ricerca sottoforma di RigaBox.
				Tab tabListaRisultati = new Tab(); // TODO estetica: il Tab è proprio brutto

				// riempio il tab
				tabListaRisultati.setContent(listaJFXListView);
				tabListaRisultati.setText("Filtrate");

				tabPaneListaRisultati.getTabs().add(tabListaRisultati);
				tabPaneListaRisultati.getSelectionModel().select(tabListaRisultati);
			}
		}
		// se non ci sono risultati...
		else {
			// label col messaggio d'errore
			Label label = new Label("\n\tNessuna Ricetta corrisponde ai parametri di ricerca.\n\tAggiungine una nuova buonissima!");
			label.setStyle("-fx-font-size: 18;");
			label.setPadding(new Insets(30, 20, 15, 15));

			// riempio il tab
			Tab tabListaRisultati = new Tab();
			tabListaRisultati.setContent(label);
			tabListaRisultati.setText("trovato niente...");
			tabPaneListaRisultati.getTabs().add(tabListaRisultati);
			tabPaneListaRisultati.getSelectionModel().select(tabListaRisultati);
		}
	}

	/* --------------------------------------------------------------------------------------------------------------------------- */
	/**
	 * Funzione che costruisce man mano la Stringa che costituisce la query SELECT. La prima Stringa contiene un "WHERE ..." (che seleziona sempre tutte le ricette) e le stringhe dopo un "AND ..."
	 * oppure "". <br>
	 * In questo modo se non vi sono inseriti filtri, verrà eseguita solo la prima stringa (che seleziona tutte le ricette), altrimenti verrà eseguita una somma di tutte le stringhe filtrate (dove
	 * ciascuna contiene un AND)
	 */
	private String selectQuery() {

		// Stringa iniziale: è l'unico where, tutti gli altri sono o AND o niente
		String initial_String = "SELECT * FROM RICETTE_TABLE WHERE ID_RICETTA != 0  ";

		// nomeRicetta: AND NOME_RICETTA LIKE '%pizza%'
		String nomeRicetta_String = "";
		if (nomeRicettaVR.getText() != null && !nomeRicettaVR.getText().equals("") && !nomeRicettaVR.getText().strip().equals("")) {
			nomeRicetta_String = "AND NOME_RICETTA LIKE '%" + nomeRicettaVR.getText() + "%'  ";
		}

		// portata: AND PORTATA = ANTIPASTO
		String portata_String = "";
		if (PORTATA.isPresent()) {
			portata_String = "AND PORTATA = '" + PORTATA.get().toString() + "'  ";
		}

		// porzioni: AND PORZIONI = 10
		String porzioni_String = "";
		if (!tuttePorzioni.isSelected()) {
			porzioni_String = "AND PORZIONI = " + spinnerPorzioni.getValue() + "  ";
		}

		// veg: AND IS_VEGETARIANA = TRUE
		String veg_String = "";
		if (siVeg.isSelected()) {
			veg_String = "AND IS_VEGETARIANA = TRUE  ";
		} else if (noVeg.isSelected()) {
			veg_String = "AND IS_VEGETARIANA = FALSE  ";
		}

		// glutine: AND IS_SENZA_GLUTINE = TRUE
		String glutine_String = "";
		if (siGlutine.isSelected()) {
			glutine_String = "AND IS_SENZA_GLUTINE = TRUE  ";
		} else if (noGlutine.isSelected()) {
			glutine_String = "AND IS_SENZA_GLUTINE = FALSE  ";
		}

		// ingredienti: AND INGREDIENTI LIKE %farina%
		StringBuilder ingredientiString = new StringBuilder("");

		// per ogni ingrediente inserito
		vBoxScrollPane.getChildren().forEach(hbox -> {

			// per ogni hbox prendo il primo elemento che è sicuramente un textfield;
			TextField testo = (TextField) ((HBox) hbox).getChildren().get(0);

			// se il testo è non vuoto...
			if (testo.getText() != null && !testo.getText().equals("") && !testo.getText().replace(";", "").strip().equals("")) {

				ingredientiString.append("AND INGREDIENTI LIKE '%" + testo.getText().replace(";", "").strip() + "%'  ");
			}
		});// end forEach

		// tempo preparazione: AND TEMPO_PREPARAZIONE <= 60
		String tempoPrep_String = "";
		if (TEMPO_PREPARAZIONE.isPresent()) {
			tempoPrep_String = "AND TEMPO_PREPARAZIONE <= " + TEMPO_PREPARAZIONE.get() + "  ";
		}

		// cottura: AND COTTURA = FALSE
		String cottura_String = "";
		if (siCottura.isSelected()) {
			cottura_String = "AND COTTURA = TRUE  ";
		} else if (noCottura.isSelected()) {
			cottura_String = "AND COTTURA = FALSE  ";
		}

		return initial_String + nomeRicetta_String + portata_String + porzioni_String + veg_String + glutine_String + ingredientiString.toString() + tempoPrep_String + cottura_String;
	}

	// --------------------------------------------------------------------------------------------------------------------------------
	/**
	 * Reset di tutti i filtri: vengono azzerati sia i nodi sia le variabili di questa classe.
	 * 
	 * @param event
	 */
	@FXML
	void resetFiltri(ActionEvent event) {

		// nomeRicetta
		nomeRicettaVR.clear();

		// porzioni
		tuttePorzioni.setSelected(true);
		spinnerPorzioni.getValueFactory().setValue(1);
		spinnerPorzioni.setOpacity(0.5);

		// veg
		buttonGroup_VEG.selectToggle(tuttiVeg);

		// glutine
		buttonGroup_SENZAGLUTINE.selectToggle(tuttiGlutine);

		// cottura
		buttonGroup_COTTURA.selectToggle(tuttiCottura);

		// portata
		panePortataHandler(null);
		panePortata.setExpanded(false);

		// tempo preparazione
		paneTempoPreparazioneHandler(null);
		paneTempoPreparazione.setExpanded(false);

		// ingredienti
		vBoxScrollPane.getChildren().subList(1, vBoxScrollPane.getChildren().size()).clear();
		((TextField) ((HBox) vBoxScrollPane.getChildren().get(0)).getChildren().get(0)).clear();
		paneIngredienti.setExpanded(false);

	}

	/* ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 */
	// PORTATA
	/**
	 * La portata può anche essere empty, a significare che sono considerate tutte.
	 */
	private Optional<Portata> PORTATA = Optional.empty();

	/**
	 * Ogni volta che viene cliccato Portata, il testo e il colore tornano predefiniti. Inoltre i toggle buttons interni si azzerano.
	 */
	@FXML
	void panePortataHandler(MouseEvent event) {
		panePortata.setText("Portata");
		panePortata.setTextFill(Paint.valueOf("#000000"));
		PORTATA = Optional.empty();

		buttonGroup_PORTATE.selectToggle(null);
	}

	@FXML
	void antipastoHandler(ActionEvent event) {
		setButtonPortata("ANTIPASTI", Portata.ANTIPASTO);
	}

	@FXML
	void primoHandler(ActionEvent event) {
		setButtonPortata("PRIMI", Portata.PRIMO);
	}

	@FXML
	void secondoHandler(ActionEvent event) {
		setButtonPortata("SECONDI", Portata.SECONDO);
	}

	@FXML
	void contrornoHandler(ActionEvent event) {
		setButtonPortata("CONTORNI", Portata.CONTORNO);
	}

	@FXML
	void dolceHandler(ActionEvent event) {
		setButtonPortata("DOLCI", Portata.DOLCE);
	}

	/**
	 * Quando viene selezionato un bottone, la portata viene associata al contenuto di quel bottone, il nome e il colore del pannello cambia e il pannello torna chiuso. Viene usata questa funzione di
	 * supporto per sintetizzare il codice.
	 */
	private void setButtonPortata(String text, Portata portata) {
		panePortata.setText("Portata: " + text);
		panePortata.setTextFill(Paint.valueOf("#ff0000"));
		PORTATA = Optional.of(portata);
		panePortata.setExpanded(false);
	}

	/* ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 */
	// TEMPO DI PREPARAZIONE

	/**
	 * Il tempoPreparazione può anche non esserci: se non c'è significa che ci sono tutte. 5 = 5 minuti 15 = 15 minuti 30 = 30 minuti 45 = 45 minuti 60 = 1 ora 120 = due ore.
	 */
	private Optional<Integer> TEMPO_PREPARAZIONE = Optional.empty();

	/**
	 * Ogni volta che viene cliccato Tempo di Cottura, il testo e il colore tornano predefiniti. Inoltre i toggle buttons interni si azzerano.
	 */
	@FXML
	void paneTempoPreparazioneHandler(MouseEvent event) {
		paneTempoPreparazione.setText("Tempo di Preparazione");
		paneTempoPreparazione.setTextFill(Paint.valueOf("#000000"));
		TEMPO_PREPARAZIONE = Optional.empty();

		buttonGroup_TEMPOPREPRARAZIONE.selectToggle(null);
	}

	@FXML
	void cinqueMinHandler(ActionEvent event) {
		setButtonTempoPreparazione("5 min", 5);
	}

	@FXML
	void quindiciMinHandler(ActionEvent event) {
		setButtonTempoPreparazione("15 min", 15);
	}

	@FXML
	void trentaMinHandler(ActionEvent event) {
		setButtonTempoPreparazione("30 min", 30);
	}

	@FXML
	void quarantacinqueMinHandler(ActionEvent event) {
		setButtonTempoPreparazione("45 min", 45);
	}

	@FXML
	void unaoraMinHandler(ActionEvent event) {
		setButtonTempoPreparazione("1 ora", 60);
	}

	@FXML
	void dueoreMinHandler(ActionEvent event) {
		setButtonTempoPreparazione("2 ore", 120);
	}

	/**
	 * Quando viene selezionato un bottone, il tempodipreparazione viene associato al contenuto di quel bottone; il nome e il colore del pannello cambia e il pannello torna chiuso. Viene usata questa
	 * funzione di supporto per sintetizzare il codice.
	 */
	private void setButtonTempoPreparazione(String text, Integer number) {
		paneTempoPreparazione.setText("Tempo di Preparazione\nentro " + text);
		paneTempoPreparazione.setTextFill(Paint.valueOf("#ff0000"));
		TEMPO_PREPARAZIONE = Optional.of(number);
		paneTempoPreparazione.setExpanded(false);
	}

	/* ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 */
	// INGREDIENTI

	@FXML
	void ingredienteAggiungi(ActionEvent event) {
		if (nomeIngredienteCerca.getText() != null && !nomeIngredienteCerca.getText().equals("") && !nomeIngredienteCerca.getText().replace(";", "").strip().equals("")) {

			// creo una nuova HBox che contiene un TextField con l'ingrediente e il bottone di cancellazione
			HBox newHbox = new HBox();

			// creo il TextField che contiene il l'ingrediente strippato (senza spazi in testa e in coda) e senza punti e virgole
			TextField newTextField = new TextField(nomeIngredienteCerca.getText().replace(";", "").strip());
			newTextField.setPrefWidth(250);
			newTextField.setPrefHeight(60);
			newTextField.setStyle("-fx-font-size: 15px");
			newTextField.setEditable(false);
			HBox.setMargin(newTextField, new Insets(0, 0, 0, 5));

			// creo il bottone di cancellazione ingrediente
			Button cancellaIngrediente = new Button("x");
			cancellaIngrediente.setStyle("-fx-background-radius: 2; -fx-background-color: tomato;");
			cancellaIngrediente.setOpacity(0);
			HBox.setMargin(cancellaIngrediente, new Insets(0, 5, 0, 5));

			// aggiungo il textfield e il bottone alla hbox; aggiungo la hbox allo scrollPane
			newHbox.getChildren().addAll(newTextField, cancellaIngrediente);
			vBoxScrollPane.getChildren().addAll(newHbox);

			// Quando si clicca il bottone di cancellazione, viene rimosso lo specifico hbox
			cancellaIngrediente.setOnAction(e -> {
				vBoxScrollPane.getChildren().remove(newHbox);
			});

			// Animazione bottone di cancellazione: appare e scompare quando il mouse ci passa sopra
			cancellaIngrediente.setOnMouseEntered(e -> {
				cancellaIngrediente.setOpacity(100);
			});
			cancellaIngrediente.setOnMouseExited(e -> {
				cancellaIngrediente.setOpacity(0);
			});

		} // end if

		// Libero il field iniziale di immissione, sia che ho aggiunto l'ingrediente sia che no.
		nomeIngredienteCerca.clear();

	}

	@FXML
	void ingredienteAggiungiENTER(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			ingredienteAggiungi(new ActionEvent());
		}

	}

	/* ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 */
	@FXML
	private TitledPane panePortata;
	@FXML
	private TitledPane paneTempoPreparazione;
	@FXML
	private RadioButton siVeg;
	@FXML
	private RadioButton noVeg;
	@FXML
	private RadioButton tuttiVeg;
	@FXML
	private RadioButton siGlutine;
	@FXML
	private RadioButton noGlutine;
	@FXML
	private RadioButton tuttiGlutine;
	@FXML
	private Spinner<Integer> spinnerPorzioni;
	@FXML
	private RadioButton tuttePorzioni;
	@FXML
	private TabPane tabPaneListaRisultati;
	@FXML
	private RadioButton siCottura;
	@FXML
	private RadioButton noCottura;
	@FXML
	private RadioButton tuttiCottura;
	@FXML
	private Tab tutteRicetteTab;
	@FXML
	private VBox vBoxScrollPane;
	@FXML
	private TextField nomeIngredienteCerca;
	@FXML
	private TitledPane paneIngredienti;
	@FXML
	private ToggleGroup buttonGroup_VEG;
	@FXML
	private ToggleGroup buttonGroup_SENZAGLUTINE;
	@FXML
	private ToggleGroup buttonGroup_COTTURA;
	@FXML
	private ToggleGroup buttonGroup_PORTATE;
	@FXML
	private ToggleGroup buttonGroup_TEMPOPREPRARAZIONE;
	@FXML
	private TextField nomeRicettaVR;
	@FXML
	private AnchorPane initialScreenTabElencoRicette;
}
