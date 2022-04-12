package controllers;

import java.io.IOException;
import java.util.Optional;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import modelli.AlertPanel;
import modelli.Ricetta;

public class ControllerRigaBox {

	private TabPane tabPane;
	private Ricetta ricetta;
	private String IMAGE_PATH = null;
	private ScreenPrimaryController screenPrimaryController;
	private AnchorPane initialScreenTabElencoRicette;

	/* ---- Costruttore --------------------------------------------------------------------------------------- */
	public ControllerRigaBox(Ricetta ricetta, TabPane tabPaneListaRisultati, AnchorPane initialScreenTabElencoRicette, ScreenPrimaryController screenPrimaryController) {
		this.ricetta = ricetta;
		this.tabPane = tabPaneListaRisultati;
		this.screenPrimaryController = screenPrimaryController;
		this.initialScreenTabElencoRicette = initialScreenTabElencoRicette;
	}

	/* ---- initialize() --------------------------------------------------------------------------------------- */
	/**
	 * 1) Si inizializzano degli elementi che servono per il bouncing effect.<br>
	 * 2) Si riempiono i campi.
	 */
	@FXML
	public void initialize() {
		double widthInitial = 200;
		double heightInitial = 200;
		clipRect = new Rectangle();
		clipRect.setWidth(widthInitial);
		clipRect.setHeight(0);
		clipRect.translateYProperty().set(heightInitial);
		extendablePane.setClip(clipRect);
		extendablePane.translateYProperty().set(-heightInitial);
		extendablePane.prefHeightProperty().set(0);

		areaPreparazione.setText(ricetta.getPREPARAZIONE());

		listIngredienti.setItems(FXCollections.observableArrayList(ricetta.getIngredientiList()));
		listIngredienti.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				mouseEvent.consume();
			}
		});
		listIngredienti.setStyle("-fx-font-family:Arial; -fx-font-size:17; ");

		nomeRicetta.setText(ricetta.getNOME_RICETTA());

		if (ricetta.getPORTATA().equals("ANTIPASTO"))
			IMAGE_PATH = "/images/starter.png";

		if (ricetta.getPORTATA().equals("PRIMO"))
			IMAGE_PATH = "/images/spaghetti.png";

		if (ricetta.getPORTATA().equals("SECONDO"))
			IMAGE_PATH = "/images/curry.png";

		if (ricetta.getPORTATA().equals("CONTORNO"))
			IMAGE_PATH = "/images/salad.png";

		if (ricetta.getPORTATA().equals("DOLCE"))
			IMAGE_PATH = "/images/peceOfcake.png";

		immPortata.setImage(new Image(IMAGE_PATH));

		if (ricetta.getImmPiattoURL().isPresent()) {
			immPiatto.setImage(new Image(ricetta.getImmPiattoURL().get()));
		} else {
			immPiatto.setImage(new Image("/images/piatto-smaltato-ho-ancora-fame.jpg"));
		}

		tempoPreparazione.setText(java.time.Duration.ofMinutes(ricetta.getTEMPO_PREPARAZIONE()).toString().replaceAll("PT", "").replaceAll("H", " h").replaceAll("M", " min"));
	}

	/* ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 */
	// PULSANTE DI ESPANSIONE

	@FXML
	void toggleExtendable(ActionEvent event) {
		clipRect.setWidth(extendablePane.getWidth());

		if (clipRect.heightProperty().get() != 0) {

			// Animation for scroll up.
			Timeline timelineUp = new Timeline();

			// Animation of sliding the search pane up, implemented via clipping.
			final KeyValue kvUp1 = new KeyValue(clipRect.heightProperty(), 0);
			final KeyValue kvUp2 = new KeyValue(clipRect.translateYProperty(), extendablePane.getHeight());

			// The actual movement of the search pane. This makes the table grow.
			final KeyValue kvUp4 = new KeyValue(extendablePane.prefHeightProperty(), 0);
			final KeyValue kvUp3 = new KeyValue(extendablePane.translateYProperty(), -extendablePane.getHeight());

			final KeyValue kvButton = new KeyValue(collapse.rotateProperty(), 0);

			final KeyValue kvPane = new KeyValue(rigaPane.prefHeightProperty(), 65);

			final KeyFrame kfUp = new KeyFrame(Duration.millis(200), kvUp1, kvUp2, kvUp3, kvUp4, kvButton, kvPane);
			timelineUp.getKeyFrames().add(kfUp);
			timelineUp.play();
		} else {

			// Animation for scroll down.
			Timeline timelineDown = new Timeline();

			// Animation for sliding the search pane down. No change in size, just making the visible part of the pane bigger.
			final KeyValue kvDwn1 = new KeyValue(clipRect.heightProperty(), extendablePane.getHeight());
			final KeyValue kvDwn2 = new KeyValue(clipRect.translateYProperty(), 0);

			// Growth of the pane.
			final KeyValue kvDwn4 = new KeyValue(extendablePane.prefHeightProperty(), extendablePane.getHeight());
			final KeyValue kvDwn3 = new KeyValue(extendablePane.translateYProperty(), 0);

			final KeyValue kvButton = new KeyValue(collapse.rotateProperty(), 180);

			final KeyValue kvPane = new KeyValue(rigaPane.prefHeightProperty(), 260);

			final KeyFrame kfDwn = new KeyFrame(Duration.millis(200), createBouncingEffect(extendablePane.getHeight()), kvDwn1, kvDwn2, kvDwn3, kvDwn4, kvButton, kvPane);
			timelineDown.getKeyFrames().add(kfDwn);

			timelineDown.play();
		}
	}

	private EventHandler<ActionEvent> createBouncingEffect(double height) {
		final Timeline timelineBounce = new Timeline();
		timelineBounce.setCycleCount(2);
		timelineBounce.setAutoReverse(true);
		final KeyValue kv1 = new KeyValue(clipRect.heightProperty(), (height - 15));
		final KeyValue kv2 = new KeyValue(clipRect.translateYProperty(), 15);
		final KeyValue kv3 = new KeyValue(extendablePane.translateYProperty(), -15);
		final KeyFrame kf1 = new KeyFrame(Duration.millis(100), kv1, kv2, kv3);
		timelineBounce.getKeyFrames().add(kf1);

		// Event handler to call bouncing effect after the scroll down is finished.
		EventHandler<ActionEvent> handler = new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				timelineBounce.play();
			}
		};
		return handler;
	}

	/* ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 */
	// PULSANTE APERTURA RICETTA
	/**
	 * Quando viene cliccato il pulsante per aprire per intero la ricetta.
	 */
	@FXML
	void openRicetta(ActionEvent event) {

		// Creo un nuovo tab e come nome inserisco il nome della ricetta
		Tab tabRicetta = new Tab();
		tabRicetta.setText(ricetta.getNOME_RICETTA());

		// Creo un nuovo pane di ScreenSingolaRicetta e lo inserisco dentro il tab appena creato
		ScrollPane root;
		try {
			root = FXMLLoader.<ScrollPane>load(getClass().getResource("/view/ScreenSingolaRicetta.fxml"), null, null, callback -> {
				return new ScreenSingolaRicettaController(ricetta, IMAGE_PATH, tabPane, initialScreenTabElencoRicette, screenPrimaryController);
			});
			tabRicetta.setContent(root);
		} catch (IOException e) {
			AlertPanel.says(AlertType.ERROR, "ERRORE: nel caricamento della ricetta", Optional.of(e));
		}

		// aggiungo il tab al tabPane principale
		tabPane.getTabs().add(tabRicetta);
		tabPane.getSelectionModel().select(tabRicetta);
	}

	/* ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 */

	@FXML
	private AnchorPane rigaPane;
	@FXML
	private AnchorPane extendablePane;
	@FXML
	private Button collapse;
	@FXML
	private ImageView immPortata;
	@FXML
	private ImageView immPiatto;
	@FXML
	private Label nomeRicetta;
	@FXML
	private TextArea areaPreparazione;
	@FXML
	private Label tempoPreparazione;
	private Rectangle clipRect;
	@FXML
	private ListView<String> listIngredienti;
}
