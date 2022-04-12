package application;

import java.awt.Toolkit;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

import controllers.ScreenPrimaryController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import modelli.AlertPanel;
import persister.RicettarioPersister;

public class MainRicettario extends Application {

	/**
	 * 1.0.01 : Tolta la possibilità di aggiungere qualsiasi file. <br>
	 * 1.2: Cambiato tutto. <br>
	 * 1.2.0.1: fix ritorno pulsante inserisci dopo aggiorna <br>
	 * 1.2.0.2: fix spinner tempo preparazione in aggiungi ricetta<br>
	 */
	public final static String VERSION = "1.2.0.2";

	public final static Integer MIN_WIDTH = 950;
	public final static Integer MIN_HIGHT = 450;

	public final static String ScreenPrimaryURL = "/view/ScreenPrimary.fxml";
	public final static String CSSbootstrap3URL = "/view/cssFiles/bootstrap3.css";

	public final static boolean CSS_ABILITATO = true;

	RicettarioPersister ricettePersister;

	@Override
	public void start(Stage primaryStage) {
		try {
			/*
			 * Creazione istanza del persister
			 */
			ricettePersister = new RicettarioPersister();

		} catch (Exception e) {
			AlertPanel.says(AlertType.ERROR, "ERRORE: nella connessione col database\nControllare se sono state rimosse delle ricette e NON aggiungerne altre", Optional.of(e));
		}

		/*
		 * Load del ScreenPrimary con la chiamata al costruttore personalizzata.
		 * Lo ScreenPrimary è un BorderPane con al centro un AnchorPane con tutti i nodi.
		 * Vengono caricati anche lo screen di aggiungi ricetta e di vedi ricetta.
		 */
		BorderPane screenPrimary = null;
		try {
			screenPrimary = FXMLLoader.<BorderPane>load(getClass().getResource(ScreenPrimaryURL), null, null, e -> {
				return new ScreenPrimaryController(ricettePersister);
			});
		} catch (IOException e) {
			AlertPanel.says(AlertType.ERROR, "ERRORE: nella load di screenPrimary o screen_AggiungiRicetta o screen_VediRicette", Optional.of(e));
		}

		/*
		 * Creazione della Scene principale con all'interno lo ScreenPrimary, ossia tutta la grafica
		 */
		Scene primaryScene = new Scene(screenPrimary);

		/*
		 * Inserimento del CSS all'interno di primaryScene
		 */
		if (CSS_ABILITATO) {
			primaryScene.getStylesheets().add(getClass().getResource(CSSbootstrap3URL).toExternalForm());
		}

		/*
		 * Impostazioni dello stage primario (primaryStage).
		 * Tale stage appare al centro dello schermo, non massimizzato e con dimensione massima pari alla dimensione massima dello schermo dell'user.
		 */
		primaryStage.setTitle("Ricettario");

		primaryStage.getIcons().add(new Image("images/boil.png"));

		primaryStage.setMinWidth(MIN_WIDTH);
		primaryStage.setMinHeight(MIN_HIGHT);
		primaryStage.setMaxHeight(Toolkit.getDefaultToolkit().getScreenSize().getHeight());
		primaryStage.setMaxWidth(Toolkit.getDefaultToolkit().getScreenSize().getWidth());
		primaryStage.setMaximized(false);

		primaryStage.centerOnScreen();

		primaryStage.setScene(primaryScene);

		// Alla chiusura del primaryStage, viene invocato il metodo di chiusura del database
		primaryStage.setOnCloseRequest(event -> {
			try {
				ricettePersister.closeDatabase();
			} catch (SQLException e1) {
				AlertPanel.says(AlertType.ERROR, "ERRORE: in chiusura del database", Optional.of(e1));
			}
		});

		primaryStage.show();

	}

	public static void main(String[] args) {
		launch(args);
	}

}
