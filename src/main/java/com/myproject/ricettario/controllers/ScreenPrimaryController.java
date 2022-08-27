package com.myproject.ricettario.controllers;

import java.io.IOException;

import com.myproject.ricettario.persister.RicettarioPersister;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;

public class ScreenPrimaryController {

	public final static String Screen_AggiungiRicettaURL = "/view/Screen_AggiungiRicetta.fxml";
	public final static String Screen_VediRicetteURL = "/view/Screen_VediRicette.fxml";

	AnchorPane screen_AggiungiRicetta;
	ScreenAggiungiRicettaController screenAggiungiRicettaController;
	AnchorPane screen_VediRicette;
	ScreenVediRicetteController screenVediRicetteController;

	RicettarioPersister persister;

	/* ---- Costruttore -------------------------------------------------------------------------- */

	public ScreenPrimaryController(RicettarioPersister persister) {

		this.persister = persister;
	}

	/**
	 * Viene invocato automaticamente dopo il comando FXML.load() dello ScreenPrimary.fxml, che avviene nel main. <br>
	 * E' differente dal costruttore: qui devono essere inserite tutte quelle operazioni di inizializzazione della classe che utilizzano variabili/funzioni con tag "@FXML"
	 * 
	 * @throws IOException
	 */
	@FXML
	public void initialize() throws IOException {

		/*
		 * Carica screen aggiungi ricetta
		 */
		FXMLLoader loader_AggiungiRicetta = new FXMLLoader(getClass().getResource(Screen_AggiungiRicettaURL), null, null, e -> {
			return new ScreenAggiungiRicettaController(persister);
		});
		screen_AggiungiRicetta = (AnchorPane) loader_AggiungiRicetta.load();
		screenAggiungiRicettaController = (ScreenAggiungiRicettaController) loader_AggiungiRicetta.getController();

		/*
		 * Carica screen vedi ricetta
		 */
		FXMLLoader loader_VediRicette = new FXMLLoader(getClass().getResource(Screen_VediRicetteURL), null, null, e -> {
			return new ScreenVediRicetteController(persister, this);
		});
		screen_VediRicette = (AnchorPane) loader_VediRicette.load();
		screenVediRicetteController = (ScreenVediRicetteController) loader_VediRicette.getController();

		/*
		 * Altro metodo per il carcamento:
		 */
		/*screen_AggiungiRicetta = FXMLLoader.<AnchorPane>load(getClass().getResource(Screen_AggiungiRicettaURL), null, null, e -> {
			return new ScreenAggiungiRicettaController(persister);
		});
		screen_VediRicette = FXMLLoader.<AnchorPane>load(getClass().getResource(Screen_VediRicetteURL), null, null, e -> {
			return new ScreenVediRicetteController(persister);
		});*/

	}

	/* ---- Handlers bottoni -------------------------------------------------------------------------- */

	@FXML
	public void aggiungiRicettaHandler(ActionEvent event) {

		anchorPaneSubSP.getChildren().clear();
		anchorPaneSubSP.getChildren().add(screen_AggiungiRicetta);
	}

	@FXML
	public void vediRicetteHandler(ActionEvent event) {

		anchorPaneSubSP.getChildren().clear();
		anchorPaneSubSP.getChildren().add(screen_VediRicette);
	}

	/* ---- FX elements -------------------------------------------------------------------------- */

	public ScreenAggiungiRicettaController getScreenAggiungiRicettaController() {
		return screenAggiungiRicettaController;
	}

	public ScreenVediRicetteController getScreenVediRicetteController() {
		return screenVediRicetteController;
	}

	public RicettarioPersister getPersister() {
		return persister;
	}

	/* ---- FX elements -------------------------------------------------------------------------- */

	/**
	 * E' la zona centrale dove vengono visualizzati lo Screen_AggiungiRicetta, Screen_VediRicette. Deve contenere al massimo uno screen.
	 */
	@FXML
	private AnchorPane anchorPaneSubSP;

}
