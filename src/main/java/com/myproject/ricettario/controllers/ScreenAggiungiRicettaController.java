package com.myproject.ricettario.controllers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.controlsfx.control.ToggleSwitch;

import com.myproject.ricettario.modelli.AlertPanel;
import com.myproject.ricettario.modelli.Portata;
import com.myproject.ricettario.modelli.Ricetta;
import com.myproject.ricettario.persister.RicettarioPersister;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ScreenAggiungiRicettaController {

	private static final Integer MAX_PORZIONI = 99;
	private static final Integer MAX_ORE = 99;

	RicettarioPersister persister;

	/* ---- Costruttore --------------------------------------------------------------------------------------- */

	public ScreenAggiungiRicettaController(RicettarioPersister persister) {
		this.persister = persister;
	}

	/* ---- initialize() --------------------------------------------------------------------------------------- */
	@FXML
	public void initialize() {

		// Settaggio valori minimo, massimo e iniziale dello spinner porzioni
		porzioniAR.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, MAX_PORZIONI));
		porzioniAR.getValueFactory().setValue(1);

		// Settaggio valori minimo, massimo e iniziale dello spinner oreScelte
		oreScelte.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(3, MAX_ORE));
		oreScelte.setOpacity(0.5);
	}

	/* ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 */
	// INSERIMENTO FINALE

	@FXML
	void inserimentoFinale(ActionEvent event) {

		// disabilito il pulsante per evitare casini
		inserimentoFinaleAR.setDisable(true);

		// se i dati sono corretti...
		if (datiCorretti()) {

			// salvo
			try {

				persister.salvaRicetta(nomeRicettaAR.getText(), portataScelta, porzioniAR.getValue(), vegAR.isSelected(), glutineAR.isSelected(), strIngredienti.toString(), preparazioneAR.getText(),
							tempoPreparazioneScelto, cotturaAR.isSelected(), immPiattoURL, immPreparazioneURLList);

				// SE SEI QUI ALLORA IL SALVATAGGIO E' ANDATO A BUON FINE

				// resetta la schermata
				resetUI();

				// Segnala il corretto inserimento
				AlertPanel.says(AlertType.INFORMATION, "Ricetta Aggiunta con successo!", "");

			} catch (Exception e) {
				AlertPanel.says(AlertType.ERROR, "ERRORE. La ricetta potrebbe non essere stata salvata", Optional.of(e));
			}
		}

		// riabilito il pulsante
		inserimentoFinaleAR.setDisable(false);
	}

	/* ---------------------------------------------------------------------------------------------------- */
	/**
	 * Resetta lo screen e i dati alla situazione iniziale.
	 */
	private void resetUI() {

		// nome ricetta
		nomeRicettaAR.clear();

		// portate
		if (buttonGroup_PORTATE.getSelectedToggle() != null) {
			buttonGroup_PORTATE.getSelectedToggle().setSelected(false);
		}
		portataScelta = null;

		// veg
		vegAR.setSelected(false);

		// glutine
		glutineAR.setSelected(false);

		// tolgo dal vbox_ingredienti tutte le hbox a parte la prima. La prima la svuoto solo
		vBoxScrollPane.getChildren().subList(1, vBoxScrollPane.getChildren().size()).clear();
		((TextField) ((HBox) vBoxScrollPane.getChildren().get(0)).getChildren().get(0)).clear();
		// libero la stringa ingredienti
		strIngredienti.setLength(0);

		// tempo cottura
		if (buttonGroup_ORESCELTE.getSelectedToggle() != null) {
			buttonGroup_ORESCELTE.getSelectedToggle().setSelected(false);
		}
		oreScelte.getValueFactory().setValue(3);
		oreScelte.setOpacity(0.5);
		tempoPreparazioneScelto = null;

		// porzioni
		porzioniAR.getValueFactory().setValue(1);

		// cottura
		cotturaAR.setSelected(false);

		// preparazione
		preparazioneAR.clear();

		// immagine piatto
		immPiattoURL = "";

		// immagini preparazione
		immPreparazioneURLList.clear();
	}

	/* ---------------------------------------------------------------------------------------------------- */
	/**
	 * Controlla che i dati inseriti siano tutti corretti. In caso contrario, lancia un alert descittivo e return false;
	 */
	private boolean datiCorretti() {

		// NOME RICETTA
		if (nomeRicettaAR.getText() == null || nomeRicettaAR.getText().equals("") || nomeRicettaAR.getText().strip().equals("")) {

			// TODO fare meglio, magari senza il popup
			nomeRicettaAR.setStyle("-fx-effect: dropshadow(three-pass-box, red ,10, 0, 0, 0)");
			nomeRicettaAR.setOnKeyTyped(e -> {
				nomeRicettaAR.setStyle(null);
			});

			AlertPanel.says(AlertType.ERROR, "NOME RICETTA mancante", "Devi inserire il nome della ricetta.");
			return false;
		}
		if (nomeRicettaAR.getText().length() >= 254) {
			AlertPanel.says(AlertType.ERROR, "NOME RICETTA troppo lungo", "Il nome della ricetta deve essere di MASSIMO 253 caratteri.");
			return false;
		}

		// PORTATA
		if (portataScelta == null) {
			AlertPanel.says(AlertType.ERROR, "PORTATA mancante", "Devi selezionare la portata della ricetta: capisco che può essere una scelta difficile, ma è obbligatorio..");
			return false;
		}

		// PORZIONI
		if (porzioniAR.getValue() == 0) {
			AlertPanel.says(AlertType.ERROR, "PORZIONI mancante", "Devi inserire il numero di porzioni della ricetta.");
			return false;
		}

		// INGREDIENTI
		// prima di tutto azzero la stringa degli ingredienti, per essere riempita tra poco
		strIngredienti.setLength(0);

		// prendo gli ingredienti: lo scroll pane contiene solo un vbox; il vbox contiene solo una serie di hbox;
		// VIENE INCLUSO ANCHE L'INGREDIENTE DELLA RIGA DI INGRESSO
		for (Node hbox : vBoxScrollPane.getChildren()) {

			// per ogni hbox prendo il primo elemento che è sicuramente un textfield;
			TextField testo = (TextField) ((HBox) hbox).getChildren().get(0);

			// se il testo è non vuoto...
			if (testo.getText() != null && !testo.getText().equals("") && !testo.getText().replace(";", "").strip().equals("")) {

				// lo aggiungo alla stringa degli ingredienti:
				strIngredienti.append(testo.getText() + ";");
			}
		}
		if (strIngredienti.length() <= 0) {
			AlertPanel.says(AlertType.ERROR, "INGREDIENTI mancanti", "Devi inserire almeno un ingrediente, sennò che ricetta è.");
			return false;
		}
		if (strIngredienti.length() >= 500) {
			AlertPanel.says(AlertType.ERROR, "INGREDIENTI troppo lunghi",
						"Gli ingredienti devono essere nel complesso sotto i 500 caratteri. \nSei fuori di " + (strIngredienti.length() - 500) + " caratteri.");
			return false;
		}

		// PREPARAZIONE
		if (preparazioneAR.getText().equals(null) || preparazioneAR.getText().equals("")) {
			AlertPanel.says(AlertType.ERROR, "PREPARAZIONE mancante", "Devi inserire la preparazione della ricetta.");
			return false;
		}
		if (preparazioneAR.getText().length() >= 4000) {
			AlertPanel.says(AlertType.ERROR, "PREPARAZIONE troppo lunga",
						"La preparazione deve essere lunga meno di 4000 caratteri. \nSei fuori di " + (preparazioneAR.getText().length() - 4000) + " caratteri.");
			return false;
		}

		// TEMPO PREPARAZIONE
		if (tempoPreparazioneScelto == null) {
			AlertPanel.says(AlertType.ERROR, "TEMPO PREPARAZIONE mancante", "Devi inserire il tempo di preparazione.");
			return false;
		}

		// IMMAGINI
		if (immPreparazioneURLList.size() > 4) {
			AlertPanel.says(AlertType.ERROR, "TROPPE IMMAGINI PREPARAZIONE", "Il massimo è 4 immagini. Eliminane alcune");
			return false;
		}

		return true;

	}

	/* ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 */
	// INREDIENTI

	private StringBuilder strIngredienti = new StringBuilder();

	/**
	 * Aggiunge l'ingrediente all'elenco. Possibilità di modificare l'ingrediente dopo averlo scritto, possibilità di cancellare l'intero ingrediente. L'inserimento è non è valido se è vuoto o solo
	 * spazi.
	 * 
	 * @param event
	 */
	@FXML
	void ingredienteAggiungi(ActionEvent event) {
		// se il testo dell'ingrediente è nullo, è uguale a "", o se contiene solo spazi...
		if (nomeIngredienteAR.getText() != null && !nomeIngredienteAR.getText().equals("") && !nomeIngredienteAR.getText().replace(";", "").strip().equals("")) {

			// creo una nuova HBox che contiene un TextField con l'ingrediente e il bottone di cancellazione
			HBox newHbox = new HBox();

			// creo il TextField che contiene il l'ingrediente strippato (senza spazi in testa e in coda) e senza punti e virgole
			TextField newTextField = new TextField(nomeIngredienteAR.getText().replace(";", "").strip());
			newTextField.setEditable(false);
			newTextField.setPrefWidth(250);
			newTextField.setPrefHeight(60);
			newTextField.setStyle("-fx-font-size: 15px");
			HBox.setMargin(newTextField, new Insets(0, 0, 0, 17));

			// creo il bottone di cancellazione ingrediente
			Button cancellaIngrediente = new Button("x");
			cancellaIngrediente.setStyle("-fx-background-radius: 2; -fx-background-color: tomato;");
			cancellaIngrediente.setOpacity(0);
			HBox.setMargin(cancellaIngrediente, new Insets(0, 0, 0, 5));

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
		nomeIngredienteAR.clear();

	}

	/**
	 * Evento aggiunta ingrediente: quando si scive un ingrediente e si clicca ENTER, viene invocato il metodo ingredienteAggiungi che aggiunge l'ingrediente
	 * 
	 */
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
	// PORTATE

	private Portata portataScelta;

	@FXML
	void antipastoHandler(ActionEvent event) {
		portataScelta = Portata.ANTIPASTO;
	}

	@FXML
	void primoHandler(ActionEvent event) {
		portataScelta = Portata.PRIMO;
	}

	@FXML
	void secondoHandler(ActionEvent event) {
		portataScelta = Portata.SECONDO;
	}

	@FXML
	void contornoHandler(ActionEvent event) {
		portataScelta = Portata.CONTORNO;
	}

	@FXML
	void dolceHandler(ActionEvent event) {
		portataScelta = Portata.DOLCE;
	}

	/* ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 */
	// TEMPO DI PREPARAZIONE

	/**
	 * E' calcolato sempre in minuti
	 */
	private Integer tempoPreparazioneScelto = null;

	/**
	 * Serve solo per resettare lo spinner nel caso in cui viene prima cliccato lo spinner, poi viene cliccato un bottone
	 */
	private boolean molteOreScelte = false;

	@FXML
	void cinqueMinHandler(ActionEvent event) {
		tempoPreparazioneScelto = 5;
		reset_oreScelte();
	}

	@FXML
	void quindiciMinHandler(ActionEvent event) {
		tempoPreparazioneScelto = 15;
		reset_oreScelte();
	}

	@FXML
	void trentaMinHandler(ActionEvent event) {
		tempoPreparazioneScelto = 30;
		reset_oreScelte();
	}

	@FXML
	void quarantacinqueMinHandler(ActionEvent event) {
		tempoPreparazioneScelto = 45;
		reset_oreScelte();
	}

	@FXML
	void unaOraHandler(ActionEvent event) {
		tempoPreparazioneScelto = 60;
		reset_oreScelte();
	}

	@FXML
	void dueOreHandler(ActionEvent event) {
		tempoPreparazioneScelto = 120;
		reset_oreScelte();
	}

	private void reset_oreScelte() {
		if (molteOreScelte == true) {
			oreScelte.setOpacity(0.5);
			oreScelte.getValueFactory().setValue(3);
			molteOreScelte = false;
		}
	}

	/**
	 * Quando viene cliccato lo spinner...
	 */
	@FXML
	void piuDueOreHandler(MouseEvent event) {
		// ristabilisco l'opacità
		oreScelte.setOpacity(1);

		// deseleziono il bottone delle ore attualmente selezionato (se è stato selezionato)
		if (buttonGroup_ORESCELTE.getSelectedToggle() != null) {
			buttonGroup_ORESCELTE.getSelectedToggle().setSelected(false);
		}

		molteOreScelte = true;
		tempoPreparazioneScelto = oreScelte.getValue() * 60;
	}

	/* ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 */
	// IMMAGINI

	// Importante. Il metodo (File).toURI().toString() ritorna:<file:/C:/Users/evang.HOMEEVANGELISTI/Downloads/nomeImmagine.jpg>

	/**
	 * E' una lista che contiene le 4 immagini della preparazione. Ogni elemento deve essere sempre nella forma:<file:/C:/Users/evang.HOMEEVANGELISTI/Downloads/nomeImmagine.jpg>
	 */
	private List<String> immPreparazioneURLList = new ArrayList<>();

	/**
	 * E' l'unica immagine del piatto. Viene inglobato da un Optional. Deve essere sempre nella forma:<file:/C:/Users/evang.HOMEEVANGELISTI/Downloads/nomeImmagine.jpg>
	 */
	private String immPiattoURL = "";

	/**
	 * Quando si clicca il pulsante per inserire l'immagine del piatto.
	 */
	@FXML
	void aggiungiImmaginePiattoHandler(ActionEvent event) {

		// resetto lo stile
		aggiungiImmPiatto.setStyle("-fx-effect: null");

		// apro un fileChooser che filtra solo JPG, PNG.
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Seleziona una foto del piatto");
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Downloads"));
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("IMMAGINI", "*.png;*.jpg"));

		// devo salvare in un File
		File fileImmagine = fileChooser.showOpenDialog(null);

		// salvo il file come immagine se qualcosa è stato selezionato
		if (fileImmagine != null) {
			immPiattoURL = fileImmagine.toURI().toString();
		}

	}

	/* --------------------------------------------------------------------------------------------------------------------------- */
	/**
	 * Quando si clicca il pulsante per inserire le immagini di preparazione.
	 */
	@FXML
	void aggiungiImmaginiPreparazioneHandler(ActionEvent event) {

		// resetto lo stile
		aggiungiImmPreparazione.setStyle("-fx-effect: null");

		// apro un fileChooser che filtra solo JPG, PNG.
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Seleziona fino a QUATTRO immagini");
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Downloads"));
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("IMMAGINI", "*.png;*.jpg"));

		// devo salvare l'uscita in una lista di file
		List<File> fileImmagine = fileChooser.showOpenMultipleDialog(null);

		// se la lista di file non è vuota...
		if (fileImmagine != null && !fileImmagine.isEmpty()) {

			// per ogni file, aggiungo alla lista il suo URL
			fileImmagine.stream().limit(4).forEach(fileImm -> {
				immPreparazioneURLList.add(fileImm.toURI().toString());
			});

			// se sono state inserite più di quattro immagini, si avvisa con un alert e si apre la visualizzazione delle immagini
			if (immPreparazioneURLList.size() > 4) {
				Alert alert = new Alert(AlertType.WARNING);
				alert.setHeaderText("Inserite più di 4 immagini");
				alert.setContentText("Sono state salvate solo le prime quattro...");
				alert.showAndWait();
				vediImmaginiPreparazioneHandler(event);
			}
		}

	}

	/* --------------------------------------------------------------------------------------------------------------------------- */
	/**
	 * Pannello per visualizzare l'immagine selezionata.
	 */
	@FXML
	void vediImmaginePiattoHandler(ActionEvent event) {
		// se l'immagine del piatto è presente...
		if (!immPiattoURL.equals("")) {

			Stage stagePiatto = new Stage();

			// creo uno scrollPane con dentro una vBox
			ScrollPane pane = new ScrollPane();
			pane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

			VBox vbox = new VBox();
			vbox.getChildren().add(new Label("Tasto destro per eliminare l'immagine"));

			// creo una imageView
			ImageView imageView = new ImageView(immPiattoURL);
			imageView.setFitWidth(600);
			imageView.setPreserveRatio(true);
			imageView.setSmooth(true);
			imageView.setCache(true);

			// aggiungo la imageView alla vBox
			vbox.getChildren().add(imageView);
			vbox.setPadding(new Insets(20, 20, 20, 20));
			vbox.setAlignment(Pos.TOP_CENTER);
			VBox.setMargin(imageView, new Insets(20, 20, 20, 20));

			// quando clicchi tasto destro, parte un alert che chiede conferma di voler eliminare l'immagine
			imageView.setOnContextMenuRequested(c -> {
				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.setTitle("");
				alert.setHeaderText("Conferma di voler cancellare l'immagine");
				ButtonType buttonTypeYes = new ButtonType("Si");
				ButtonType buttonTypeAnnulla = new ButtonType("Annulla", ButtonData.CANCEL_CLOSE);
				alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeAnnulla);
				Optional<ButtonType> result = alert.showAndWait();

				// se sì, si rimuove l'immagine rendendola vuota
				if (result.get() == buttonTypeYes) {
					immPiattoURL = "";
					stagePiatto.close();
				}
			});

			// setto scena e stage. Lo stage è unico per tutta l'applicazione.
			pane.setContent(vbox);
			Scene scene = new Scene(pane, 700, 600);
			stagePiatto.setScene(scene);
			stagePiatto.initModality(Modality.APPLICATION_MODAL);
			stagePiatto.getIcons().add(new Image("images/bowlCOL.png"));
			stagePiatto.show();

		}

		// altrimenti se non c'è nessuna immagine...
		else {

			aggiungiImmPiatto.setStyle("-fx-effect: dropshadow(three-pass-box, red,10, 0, 0, 0)");
			Timeline timeline = new Timeline(new KeyFrame(Duration.millis(1500), new KeyValue(aggiungiImmPiatto.styleProperty(), null)));
			timeline.play();
		}
	}

	/* --------------------------------------------------------------------------------------------------------------------------- */
	/**
	 * Pannello per visualizzare le immagini selezionate
	 */
	@FXML
	void vediImmaginiPreparazioneHandler(ActionEvent event) {

		// se la lista delle immagini non è vuota...
		if (!immPreparazioneURLList.isEmpty()) {

			Stage stagePrep = new Stage();

			// creo uno scrollPane con dentro una vBox
			ScrollPane pane = new ScrollPane();
			pane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

			VBox vbox = new VBox();
			vbox.getChildren().add(new Label("Tasto destro per rimuovere l'immagine"));

			// per ogni immagine dentro la lista...
			immPreparazioneURLList.stream().forEach(immagineURL -> {

				// creo una imageView
				ImageView imageView = new ImageView(immagineURL);
				imageView.setFitWidth(600);
				imageView.setPreserveRatio(true);
				imageView.setSmooth(true);
				imageView.setCache(true);

				// aggiungo la imageView alla vBox
				vbox.getChildren().add(imageView);
				vbox.setPadding(new Insets(20, 20, 20, 20));
				vbox.setAlignment(Pos.TOP_CENTER);
				VBox.setMargin(imageView, new Insets(20, 20, 20, 20));

				// quando clicchi tasto destro, parte un alert che chiede conferma di voler eliminare l'immagine
				imageView.setOnContextMenuRequested(c -> {
					Alert alert = new Alert(AlertType.CONFIRMATION);
					alert.setTitle("");
					alert.setHeaderText("Conferma di voler rimuovere l'immagine");
					ButtonType buttonTypeYes = new ButtonType("Si");
					ButtonType buttonTypeAnnulla = new ButtonType("Annulla", ButtonData.CANCEL_CLOSE);
					alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeAnnulla);
					Optional<ButtonType> result = alert.showAndWait();

					// se sì, si rimuove l'immagine dalla lista
					if (result.get() == buttonTypeYes) {
						immPreparazioneURLList.remove(immagineURL);
						vbox.getChildren().remove(imageView);

						// se la lista è vuota, chiudi lo stage
						if (immPreparazioneURLList.isEmpty()) {
							stagePrep.close();
						}
					}
				});
			}); // fine forEach

			// setto scena e stage. Lo stage è unico per tutta l'applicazione.
			pane.setContent(vbox);
			Scene scene = new Scene(pane, 700, 600);
			stagePrep.setScene(scene);
			stagePrep.initModality(Modality.APPLICATION_MODAL);
			stagePrep.getIcons().add(new Image("images/bowlCOL.png"));
			stagePrep.show();

		}

		// altrimenti se la lista delle immagini è vuota...
		else {

			aggiungiImmPreparazione.setStyle("-fx-effect: dropshadow(three-pass-box, red,10, 0, 0, 0)");
			Timeline timeline = new Timeline(new KeyFrame(Duration.millis(1500), new KeyValue(aggiungiImmPreparazione.styleProperty(), null)));
			timeline.play();
		}

	}

	/* ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 */
	// MODIFICA RICETTA

	int ID_ricettaDaModificare = -1;

	/**
	 * La funzione modificaRicetta imposta tutti i field, bottoni, .., alle impostazioni della ricetta da modificare.
	 * 
	 * @param ricetta
	 */
	public void modificaRicetta(Ricetta ricetta) {

		// abilito il pulsante di aggiorna
		aggiornaRicettaAR.setVisible(true);
		aggiornaRicettaAR.setDisable(false);

		// disabilito il pulsante inserisci
		inserimentoFinaleAR.setVisible(false);
		inserimentoFinaleAR.setDisable(true);

		// resetto la grafica da un possibile inserimento in corso
		resetUI();

		// prendo l'ID
		ID_ricettaDaModificare = ricetta.getID_RICETTA();

		// riempio i vari campi con i dati della ricetta

		nomeRicettaAR.setText(ricetta.getNOME_RICETTA());

		if (ricetta.getPORTATA().equals("ANTIPASTO")) {
			buttonGroup_PORTATE.selectToggle(antipastoButton);
			portataScelta = Portata.of("ANTIPASTO");
		} else if (ricetta.getPORTATA().equals("PRIMO")) {
			buttonGroup_PORTATE.selectToggle(primoButton);
			portataScelta = Portata.of("PRIMO");
		} else if (ricetta.getPORTATA().equals("SECONDO")) {
			buttonGroup_PORTATE.selectToggle(secondoButton);
			portataScelta = Portata.of("SECONDO");
		} else if (ricetta.getPORTATA().equals("CONTORNO")) {
			buttonGroup_PORTATE.selectToggle(contornoButton);
			portataScelta = Portata.of("CONTORNO");
		} else if (ricetta.getPORTATA().equals("DOLCE")) {
			buttonGroup_PORTATE.selectToggle(dolceButton);
			portataScelta = Portata.of("DOLCE");
		}

		vegAR.setSelected(ricetta.isIS_VEGETARIANA());

		glutineAR.setSelected(ricetta.isIS_SENZA_GLUTINE());

		for (String ingrediente : ricetta.getIngredientiList()) {

			// metto l'ingrediente nel field
			nomeIngredienteAR.setText(ingrediente);

			// invoco la funzione che me lo aggiunge
			ingredienteAggiungi(null);
		}

		if (ricetta.getTEMPO_PREPARAZIONE() > 120) {
			oreScelte.getValueFactory().setValue(ricetta.getTEMPO_PREPARAZIONE() / 60);
			tempoPreparazioneScelto = ricetta.getTEMPO_PREPARAZIONE() / 60;
		} else {

			if (ricetta.getTEMPO_PREPARAZIONE() == 5) {
				buttonGroup_ORESCELTE.selectToggle(cinqueMin);
				tempoPreparazioneScelto = 5;
			} else if (ricetta.getTEMPO_PREPARAZIONE() == 15) {
				buttonGroup_ORESCELTE.selectToggle(quindiciMin);
				tempoPreparazioneScelto = 15;
			} else if (ricetta.getTEMPO_PREPARAZIONE() == 30) {
				buttonGroup_ORESCELTE.selectToggle(trentaMin);
				tempoPreparazioneScelto = 30;
			} else if (ricetta.getTEMPO_PREPARAZIONE() == 45) {
				buttonGroup_ORESCELTE.selectToggle(quarantacinqueMin);
				tempoPreparazioneScelto = 45;
			} else if (ricetta.getTEMPO_PREPARAZIONE() == 60) {
				buttonGroup_ORESCELTE.selectToggle(unaOra);
				tempoPreparazioneScelto = 60;
			} else if (ricetta.getTEMPO_PREPARAZIONE() == 120) {
				buttonGroup_ORESCELTE.selectToggle(dueOre);
				tempoPreparazioneScelto = 120;
			}

		}

		cotturaAR.setSelected(ricetta.isCOTTURA());

		porzioniAR.getValueFactory().setValue(ricetta.getPORZIONI());

		preparazioneAR.setText(ricetta.getPREPARAZIONE());

		// IMMAGINI
		immPiattoURL = ricetta.getImmPiattoURL().orElse("");
		immPreparazioneURLList = ricetta.getListaImmPreparazioneURL();
	}

	/* --------------------------------------------------------------------------------------------------------------------------- */
	/**
	 * Handler del bottone che aggiorna la ricetta che si vuole modificare.
	 */
	@FXML
	void aggiornaRicettaHandler(ActionEvent event) {

		boolean aggiornata_con_successo = false;

		// disabilito il pulsante per evitare casini
		aggiornaRicettaAR.setDisable(true);

		// se i dati sono corretti...
		if (datiCorretti()) {

			// salvo
			try {

				persister.updateRicetta(ID_ricettaDaModificare, nomeRicettaAR.getText(), portataScelta, porzioniAR.getValue(), vegAR.isSelected(), glutineAR.isSelected(), strIngredienti.toString(),
							preparazioneAR.getText(), tempoPreparazioneScelto, cotturaAR.isSelected(), immPiattoURL, immPreparazioneURLList);

				// SE SEI QUI ALLORA IL SALVATAGGIO E' ANDATO A BUON FINE

				// resetta la schermata
				resetUI();

				// resetta l'ID per sicurezza
				ID_ricettaDaModificare = -1;

				// Segnala il corretto inserimento
				AlertPanel.says(AlertType.INFORMATION, "Ricetta Modificata con successo!", "");

				aggiornata_con_successo = true;

			} catch (Exception e) {
				AlertPanel.says(AlertType.ERROR, "ERRORE. La ricetta potrebbe non essere stata modificata", Optional.of(e));
			}
		}

		// riabilito il pulsante
		aggiornaRicettaAR.setDisable(false);

		// se è stata aggiornata con successo, allora metto a posto i pulsanti
		if (aggiornata_con_successo) {
			// disabilito il pulsante aggiorna
			aggiornaRicettaAR.setVisible(false);
			aggiornaRicettaAR.setDisable(true);

			// abilito il pulsante inserisci
			inserimentoFinaleAR.setVisible(true);
			inserimentoFinaleAR.setDisable(false);
		}

	}

	/* ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 */
	@FXML
	private TextArea preparazioneAR;
	@FXML
	private ToggleSwitch glutineAR;
	@FXML
	private ToggleSwitch vegAR;
	@FXML
	private ToggleSwitch cotturaAR;
	@FXML
	private TextField nomeRicettaAR;
	@FXML
	private Spinner<Integer> porzioniAR;
	@FXML
	private VBox vBoxScrollPane;
	@FXML
	private TextField nomeIngredienteAR;
	@FXML
	private ToggleButton antipastoButton;
	@FXML
	private ToggleButton primoButton;
	@FXML
	private ToggleButton secondoButton;
	@FXML
	private ToggleButton contornoButton;
	@FXML
	private ToggleButton dolceButton;
	@FXML
	private ToggleButton cinqueMin;
	@FXML
	private ToggleButton quindiciMin;
	@FXML
	private ToggleButton trentaMin;
	@FXML
	private ToggleButton quarantacinqueMin;
	@FXML
	private ToggleButton unaOra;
	@FXML
	private ToggleButton dueOre;
	@FXML
	private Spinner<Integer> oreScelte;
	@FXML
	private Button aggiungiImmPreparazione;
	@FXML
	private Button aggiungiImmPiatto;
	@FXML
	private ToggleGroup buttonGroup_ORESCELTE;
	@FXML
	private ToggleGroup buttonGroup_PORTATE;
	@FXML
	private Button aggiornaRicettaAR;
	@FXML
	private Button inserimentoFinaleAR;

}
