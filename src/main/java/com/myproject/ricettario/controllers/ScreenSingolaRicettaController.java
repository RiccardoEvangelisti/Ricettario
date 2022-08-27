package com.myproject.ricettario.controllers;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

import com.myproject.ricettario.modelli.AlertPanel;
import com.myproject.ricettario.modelli.Ricetta;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

public class ScreenSingolaRicettaController {

	private Ricetta ricetta;
	private String IMAGE_PATH;
	private ScreenPrimaryController screenPrimaryController;
	private TabPane tabPane;
	private AnchorPane initialScreenTabElencoRicette;

	/* ---- Costruttore --------------------------------------------------------------------------------------- */

	public ScreenSingolaRicettaController(Ricetta ricetta, String IMAGE_PATH, TabPane tabPane, AnchorPane initialScreenTabElencoRicette, ScreenPrimaryController screenPrimaryController) {
		this.ricetta = ricetta;
		this.IMAGE_PATH = IMAGE_PATH;
		this.screenPrimaryController = screenPrimaryController;
		this.tabPane = tabPane;
		this.initialScreenTabElencoRicette = initialScreenTabElencoRicette;
	}

	/* ---- initialize() --------------------------------------------------------------------------------------- */
	/**
	 * Riepimento di tutti i campi grafici.
	 */
	@FXML
	public void initialize() {

		labelNomeRicetta.setText(ricetta.getNOME_RICETTA());

		immPortata.setImage(new Image(IMAGE_PATH));
		labelPortata.setText(ricetta.getPORTATA().toString());

		ivPiatto.setImage(new Image(ricetta.getImmPiattoURL().orElse("/images/piatto-smaltato-ho-ancora-fame.jpg")));

		labelTempo.setText(java.time.Duration.ofMinutes(ricetta.getTEMPO_PREPARAZIONE()).toString().replaceAll("PT", "").replaceAll("H", " h").replaceAll("M", " min"));

		labelPorzioni.setText(String.valueOf(ricetta.getPORZIONI()));

		if (!ricetta.isIS_VEGETARIANA()) {
			vegIcon.setVisible(false);
			labelVeg.setVisible(false);
		}

		if (!ricetta.isIS_SENZA_GLUTINE()) {
			senzaGlutineIcon.setVisible(false);
			labelSenzaGlutine.setVisible(false);
		}

		txtArea.setText(ricetta.getPREPARAZIONE());

		txtArea.setTooltip(new Tooltip("Click tasto destro per espandere"));

		listIngredienti.setItems(FXCollections.observableArrayList(ricetta.getIngredientiList()));
		listIngredienti.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				mouseEvent.consume();
			}
		});
		listIngredienti.setStyle("-fx-font-family:Arial; -fx-font-size:17; ");

		vBoxMain.setPrefHeight(785 + txtArea.getMaxHeight());
		txtArea.setOnContextMenuRequested(e -> {
			txtArea.setMaxHeight(txtArea.getHeight() + 1200);
			vBoxMain.setPrefHeight(785 + txtArea.getMaxHeight());
		});

		if (ricetta.getListaImmPreparazioneURL().isEmpty()) {
			immPreparazioneButton.setDisable(true);
		}
	}

	/* ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 */
	// PULSANTE APRI IMMAGINI PREPARAZIONE
	/**
	 * La funzione apre le immagini di preparazione utilizzando l'applicazione predefinita.
	 */
	@FXML
	void openImmPreparazione(ActionEvent event) {

		// per ogni immagine dentro la lista...
		ricetta.getListaImmPreparazioneURL().stream().forEach(immagine -> {

			try {
				Desktop.getDesktop().open(new File(immagine.substring(6)));
			} catch (IOException e) {
				AlertPanel.says(AlertType.ERROR, "ERRORE: nell'apertura delle immagini", Optional.of(e));
			}
		});

		/*
		 * VECCHIO APPROCCIO: aprivo un pannello apposito.
		 */
		/*
		// se la lista delle immagini non è vuota...
		if (!ricetta.getListaImmPreparazioneURL().isEmpty()) {
		
			Stage stagePrep = new Stage();
		
			// creo uno scrollPane con dentro una vBox
			ScrollPane pane = new ScrollPane();
			pane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		
			VBox vbox = new VBox();
		
			// per ogni immagine dentro la lista...
			ricetta.getListaImmPreparazioneURL().stream().forEach(immagine -> {
		
				// creo una imageView
				ImageView imageView = new ImageView(immagine);
				imageView.setFitWidth(600);
				imageView.setPreserveRatio(true);
				imageView.setSmooth(true);
				imageView.setCache(true);
		
				// aggiungo la imageView alla vBox
				vbox.getChildren().add(imageView);
				vbox.setPadding(new Insets(20, 20, 20, 20));
				vbox.setAlignment(Pos.TOP_CENTER);
				VBox.setMargin(imageView, new Insets(20, 20, 20, 20));
			}); // fine forEach
		
			// setto scena e stage. Lo stage è unico per tutta l'applicazione.
			pane.setContent(vbox);
			//pane.setFitToWidth(true);
			//pane.setFitToHeight(true);
		
			Scene scene = new Scene(pane, 700, 600);
			stagePrep.setScene(scene);
			//stagePrep.setMaximized(true);
			stagePrep.centerOnScreen();
			stagePrep.initModality(Modality.APPLICATION_MODAL);
			stagePrep.getIcons().add(new Image("images/bowlCOL.png"));
		
			stagePrep.show();
		
		}*/

	}

	/* ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 */
	// PULSANTE MODIFICA RICETTA

	@FXML
	void modificaRicettaHandler(ActionEvent event) {

		// chiedo per sicurezza
		if (AlertPanel.says(AlertType.CONFIRMATION, "Confermi di voler MODIFICARE la ricetta?", "Verranno chiuse le attuali ricette aperte", new ButtonType("Si"),
					new ButtonType("Annulla", ButtonData.CANCEL_CLOSE))) {

			// prima di tutto, onde evitare che nello screen vedi ricetta vi rimangano delle ricette già cancellate o modificate, chiudo tutti i tab a parte il primo
			tabPane.getTabs().subList(1, tabPane.getTabs().size()).clear();
			// il primo lo svuoto
			tabPane.getTabs().get(0).setContent(initialScreenTabElencoRicette);

			// passo la ricetta allo screen aggiungi ricetta attraverso il metodo opportuno
			screenPrimaryController.getScreenAggiungiRicettaController().modificaRicetta(ricetta);

			// visualizzo lo screen aggiungi ricetta
			screenPrimaryController.aggiungiRicettaHandler(null);
		}
	}

	/* ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 */
	// PULSANTE ELIMINA RICETTA

	@FXML
	void eliminaRicettaHandler(ActionEvent event) {

		// chiedo per sicurezza
		if (AlertPanel.says(AlertType.CONFIRMATION, "Confermi di voler ELIMINARE la ricetta?", "Non sarà possibile recuperarla. No, neanche nel cestino", new ButtonType("Si"),
					new ButtonType("Annulla", ButtonData.CANCEL_CLOSE))) {

			// prima di tutto, onde evitare che nello screen vedi ricetta vi rimangano delle ricette già cancellate o modificate, chiudo tutti i tab a parte il primo
			tabPane.getTabs().subList(1, tabPane.getTabs().size()).clear();
			// il primo lo svuoto
			tabPane.getTabs().get(0).setContent(initialScreenTabElencoRicette);

			try {
				screenPrimaryController.getPersister().eliminaRicetta(ricetta.getID_RICETTA());
			} catch (Exception e) {
				AlertPanel.says(AlertType.ERROR, "ERRORE: nella eliminazione della ricetta",
							"Controlla cosa è stato eliminato e cosa no.\nControlla se le immagini di piatto e preparazione sono ancora presenti.");
			}
		}
	}

	// ----------------------------------------------------------------------------------------------------

	@FXML
	private Label labelNomeRicetta;
	@FXML
	private Label labelTempo;
	@FXML
	private Label labelPorzioni;
	@FXML
	private ImageView immPortata;
	@FXML
	private Label labelPortata;
	@FXML
	private ImageView vegIcon;
	@FXML
	private ImageView senzaGlutineIcon;
	@FXML
	private Label labelVeg;
	@FXML
	private Label labelSenzaGlutine;
	@FXML
	private VBox vBoxMain;
	@FXML
	private ImageView ivPiatto;
	@FXML
	private TextArea txtArea;
	@FXML
	private ListView<String> listIngredienti;
	@FXML
	private Button immPreparazioneButton;
}
