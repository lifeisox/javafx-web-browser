package browser;

import java.awt.Desktop;
import java.io.File;
import java.util.Optional;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.stage.Stage;
import javafx.util.Duration;

public class DownloadTask {
	public static Stage downloadWindow;
	public VBox centerVbox;
	private TextArea statusTextArea;

	public DownloadTask() {

		downloadWindow = new Stage();
		downloadWindow.setOnCloseRequest(winEvent -> downloadWindow = null);
		downloadWindow.setTitle("Downloading...");
		downloadWindow.getIcons().add(new Image("/assignment3/BigHero.png"));
		downloadWindow.setScene(new Scene(createWindow()));
		downloadWindow.show();
	}

	private Parent createWindow() {
		BorderPane root = new BorderPane();

		statusTextArea = new TextArea();
		statusTextArea.setEditable(false);
		statusTextArea.setWrapText(true);
		statusTextArea.setPrefHeight(300.0);

		centerVbox = new VBox();

		VBox tempVbox = new VBox(centerVbox, statusTextArea);
		root.setCenter(tempVbox);

		Button closeButton = new Button("Close");
		closeButton.setOnAction(eh -> { 
			downloadWindow.close();
			downloadWindow = null;
		});
		closeButton.setPrefWidth(80.0);

		root.setBottom(closeButton);
		BorderPane.setAlignment(closeButton, Pos.CENTER_RIGHT);
		BorderPane.setMargin(closeButton, new Insets(10, 10, 10, 10));
		return root;
	}

	public void addDownloadTask(String sourceUrl, String downloadPath) {
		DownloadTasks downTasks = new DownloadTasks(sourceUrl, downloadPath);
		centerVbox.getChildren().add(downTasks);

		downTasks.getDownloadTask().setOnRunning(eh -> {
			statusTextArea.appendText("Downloading " + sourceUrl + ".\n");
		});

		downTasks.getDownloadTask().setOnSucceeded(eh -> {
			statusTextArea.appendText(sourceUrl + " was successfully downloaded.\n");
			String file = downTasks.getSaveFilePath();
			closeAnimation(downTasks);

			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Confirm...");
			alert.setHeaderText(null);
			alert.setContentText("Are you want to launch a default application for this file?");

			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == ButtonType.OK){
				try { Desktop.getDesktop().open(new File(file)); } catch (Exception err) {}
			} 
		});

		downTasks.getDownloadTask().setOnFailed(eh -> {
			statusTextArea.appendText("Downloading of " + sourceUrl + " failed because unknown error occured.\n" );
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Downloading failed");
			alert.setHeaderText(null);
			alert.setContentText(sourceUrl + " failed.\n");
			alert.showAndWait();
			closeAnimation(downTasks);
		});

		downTasks.getDownloadTask().setOnCancelled(eh -> {
			statusTextArea.appendText(sourceUrl + " download was cancelled.\n" );
			closeAnimation(downTasks);
		});
	}
	
	private void closeAnimation(DownloadTasks downTasks) {
		TranslateTransition tt1 = new TranslateTransition(Duration.millis(10.0));
		tt1.setFromY(0.0);
		tt1.setToY(10.0);
		tt1.setCycleCount(50);
		tt1.setAutoReverse(true);

		FadeTransition ft = new FadeTransition(Duration.millis(500.0));
		ft.setByValue(0.3);
		ft.setCycleCount(2);
		ft.setAutoReverse(true);

		TranslateTransition tt2 = new TranslateTransition(Duration.millis(500.0));
		tt2.setFromY(0.0);
		tt2.setToY(500.0);
		tt2.setCycleCount(1);

		ParallelTransition pt = new ParallelTransition(ft,  tt2);

		SequentialTransition st = new SequentialTransition (downTasks, tt1, pt);
		st.setOnFinished(event -> {
			AudioClip plonkSound = new AudioClip("http://www.flashkit.com/imagesvr_ce/flashkit/soundfx/Interfaces/Click_So-S_Bainbr-7969/Click_So-S_Bainbr-7969_hifi.mp3");
			plonkSound.play();
			centerVbox.getChildren().remove(downTasks);
		});

		st.play();
	}
}
