package com.myproject.ricettario.modelli;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;

public class AlertPanel {

	private static Alert alert;
	private static TextArea textArea;

	public static void says(AlertType type, String headerText, Optional<Exception> exception) {
		alert = new Alert(type);
		textArea = new TextArea();
		alert.setResizable(true);

		alert.setTitle("Ricettario");
		alert.setHeaderText(headerText);

		if (exception.isPresent()) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			exception.get().printStackTrace(pw);
			textArea.setText(sw.toString());
			textArea.setWrapText(true);
			textArea.setEditable(false);
		}

		alert.getDialogPane().setContent(textArea);

		alert.showAndWait();
	}

	public static void says(AlertType type, String headerText, String contentText) {
		alert = new Alert(type);
		alert.setResizable(true);
		textArea = new TextArea();

		alert.setTitle("Ricettario");
		alert.setHeaderText(headerText);

		textArea.setText(contentText);
		textArea.setWrapText(true);
		textArea.setEditable(false);

		alert.getDialogPane().setContent(textArea);

		alert.showAndWait();
	}

	public static boolean says(AlertType type, String headerText, String contentText, ButtonType buttonTypeYes, ButtonType buttonTypeAnnulla) {
		alert = new Alert(type);
		alert.setResizable(true);
		textArea = new TextArea();

		alert.setTitle("Ricettario");
		alert.setHeaderText(headerText);

		textArea.setText(contentText);
		textArea.setWrapText(true);
		textArea.setEditable(false);

		alert.getDialogPane().setContent(textArea);
		alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeAnnulla);

		// se sì...
		if (alert.showAndWait().get() == buttonTypeYes) {
			return true;
		} else {
			return false;
		}
	}

}
