package browser;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * DownloadBar class is for a downloading file on web site.
 * 
 * @author ByungSeon
 *
 */
public class DownloadBar {
	private static Stage downloadStage = null;
	private static VBox downloadVBox;
	private static TextArea statusTextArea;
	
	private Label filenameLabel;
	private ProgressBar progBar;
	private ProgressIndicator progIndicator;
	private Button cancelButton;
	private HBox downloadRoot;
	private String sourceURL;
	private String destFullName;
	private String destFileName;
	private DownloadTask downloadTask;
	
	
	/** Calling this function will guarantee that the downloadTasks VBox is created and visible.
	 * @return A Stage that will show each downloadTask's progress
	 */
	public Stage getDownloadWindow() {
		if(downloadStage == null) {
			BorderPane root = new BorderPane();

			statusTextArea = new TextArea();
			statusTextArea.setEditable(false);
			statusTextArea.setWrapText(true);
			statusTextArea.setPrefHeight(300.0);

			downloadVBox = new VBox();

			root.setCenter(new VBox(downloadVBox, statusTextArea));

			Button closeButton = new Button("Close");
			closeButton.setOnAction(e -> downloadStage.close());
			closeButton.setPrefWidth(80.0);

			root.setBottom(closeButton);
			BorderPane.setAlignment(closeButton, Pos.CENTER_RIGHT);
			BorderPane.setMargin(closeButton, new Insets(10, 10, 10, 10));
			
			downloadStage = new Stage();
			downloadStage.setOnCloseRequest(e -> downloadStage = null);
			downloadStage.setTitle("Downloading...");
			downloadStage.getIcons().add(new Image("/assignment3/BigHero.png"));
			downloadStage.setScene(new Scene(root));
		}
		return downloadStage;
	}
	
	/** Create a window for download process
	 * @return return HBox
	 */
	private HBox getDownloadTasksWindow() {
		filenameLabel = new Label(destFileName);
		filenameLabel.setPrefWidth(100.0);
		filenameLabel.setAlignment(Pos.CENTER_RIGHT);
		
		progBar = new ProgressBar();
		progBar.setPrefWidth(200.0);
		
		progIndicator = new ProgressIndicator();
		progIndicator.setPrefHeight(100.0);
		
		cancelButton = new Button("Cancel");
		cancelButton.setPrefWidth(70.0);
		cancelButton.setOnAction(eh -> {
			Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Confirm...");
            alert.setHeaderText(null);
            alert.setContentText("Are you really cancel this download?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK){
            	downloadTask.cancel();
            } 
		});
		
		downloadRoot = new HBox(filenameLabel, progBar, progIndicator, cancelButton);
		downloadRoot.setPrefHeight(HBox.USE_COMPUTED_SIZE);
		downloadRoot.setPrefWidth(HBox.USE_COMPUTED_SIZE);
		downloadRoot.setAlignment(Pos.CENTER);
		downloadRoot.setStyle("-fx-border-width: 1px; -fx-border-color: darkgrey;");
		downloadRoot.setPadding(new Insets(3, 3, 3, 3));
		downloadRoot.setSpacing(10.0);
		HBox.setHgrow(progBar, Priority.ALWAYS);
		
		return downloadRoot;
	}
	
	/**
	 * The constructor for a DownloadBar
	 * @param sourceURL The String URL of a file to download
	 * @param destinationPath The default download path
	 * @throws Exception Pass exception
	 */
	public DownloadBar(String sourceURL, String destinationPath) throws Exception {
		
		this.sourceURL = sourceURL;
		
		getDownloadWindow().show();
		
		URL url = new URL(sourceURL);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        int responseCode = httpConn.getResponseCode();
        int contentLength = -1;
 
        // always check HTTP response code first
        if (responseCode == HttpURLConnection.HTTP_OK) {
            String fileName = sourceURL.substring(sourceURL.lastIndexOf("/") + 1,
                    sourceURL.length());
            /* The Content-Disposition response-header field has been proposed as a means 
             * for the origin server to suggest a default filename if the user requests 
             * that the content is saved to a file. This usage is derived from the definition 
             * of Content-Disposition in RFC 1806
             */
            String disposition = httpConn.getHeaderField("Content-Disposition");
            contentLength = httpConn.getContentLength();
 
            if (disposition != null) {
                // extracts file name from header field
                int index = disposition.indexOf("filename=");
                if (index > 0) {
                    fileName = disposition.substring(index + 10, disposition.length() - 1);
                }
            } 

            // divide filename and extension
	      	String fileExtension = fileName.substring(fileName.lastIndexOf("."));
	      	if (fileExtension == null) fileExtension = "";
	      	fileName = fileName.substring(0, fileName.lastIndexOf("."));

	      	int count = 0;
	      	while (true) {
	      		String numFileName = fileName + ((count == 0) ? "" : "(" + count + ")");
	      		String fullFileName = destinationPath + File.separator + numFileName + fileExtension;
	      		File file = new File(fullFileName);
	      		if (!file.isFile()) {
	      			fileName = numFileName;
	      			break;
	      		}
	      		count++;
	      	}
	      	destFileName = fileName + fileExtension;
	      	destFullName = destinationPath + File.separator + destFileName;
        }
        
        httpConn.disconnect();
	      	
        downloadVBox.getChildren().add( getDownloadTasksWindow() );
	      	
        downloadTask = new DownloadTask( contentLength );
		
        downloadTask.setOnRunning(eh -> statusTextArea.appendText("Downloading " + sourceURL + ".\n"));

        downloadTask.setOnSucceeded(eh -> {
			statusTextArea.appendText(sourceURL + " was successfully downloaded.\n");
			closeAnimation();

			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Confirm...");
			alert.setHeaderText(null);
			alert.setContentText("Are you want to launch a default application for this file?");

			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == ButtonType.OK){
				try { Desktop.getDesktop().open(new File(destFullName)); } catch (Exception err) {}
			} 
		});

        downloadTask.setOnFailed(eh -> {
			statusTextArea.appendText("Downloading of " + sourceURL + " failed because unknown error occured.\n" );
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Downloading failed");
			alert.setHeaderText(null);
			alert.setContentText(sourceURL + " failed.\n");
			alert.showAndWait();
			closeAnimation();
			File delFile = new File(destFullName);
			delFile.delete();
		});

        downloadTask.setOnCancelled(eh -> {
			statusTextArea.appendText(sourceURL + " download was cancelled.\n" );
			closeAnimation();
			File delFile = new File(destFullName);
			delFile.delete();
		});
		
        progBar.progressProperty().bind(downloadTask.progressProperty());
        progIndicator.progressProperty().bind(downloadTask.progressProperty());

        Thread thread = new Thread(downloadTask);
        thread.setDaemon(true);
        thread.start();
	}

	/**
	 * When downloading is finished, the closeAnimation() shows some animation.
	 */
	private void closeAnimation() {
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

		SequentialTransition st = new SequentialTransition (downloadRoot, tt1, pt);
		st.setOnFinished(event -> {
			AudioClip plonkSound = new AudioClip("http://www.flashkit.com/imagesvr_ce/flashkit/soundfx/Interfaces/Click_So-S_Bainbr-7969/Click_So-S_Bainbr-7969_hifi.mp3");
			plonkSound.play();
			downloadVBox.getChildren().remove(downloadRoot);
		});

		st.play();
	}
	
	/**This class represents a task that will be run in a separate thread. It will run call(), 
	 *  and then call succeeded, cancelled, or failed depending on whether the task was cancelled
	 *  or failed. If it was not, then it will call succeeded() after call() finishes.
	 */
	private class DownloadTask extends Task<String> {
		private static final int BUFFER_SIZE = 4096;
		private int contentLength;
		
		private DownloadTask( int contentLength ) {
			this.contentLength = contentLength;
		}
		
		@Override
		protected String call() throws Exception {
			
			URL url = new URL(sourceURL);
	        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
	        int responseCode = httpConn.getResponseCode();
	        
	        if (responseCode == HttpURLConnection.HTTP_OK) {
	            InputStream inputStream = httpConn.getInputStream();
	            
	            FileOutputStream outputStream = new FileOutputStream(destFullName);
	 
	            long nread = 0L;
	            int bytesRead = -1;
	            byte[] buffer = new byte[BUFFER_SIZE];
	            while ((bytesRead = inputStream.read(buffer)) != -1) {
	                outputStream.write(buffer, 0, bytesRead);
	                nread += bytesRead;
	                updateProgress(nread, contentLength);
	                if (isCancelled()) {
	                    break;
	                }
	            }
	            outputStream.close();
	            inputStream.close();
	        } 
	        httpConn.disconnect();
			return "Finished";
		}
		
		//Write the code here to handle a successful completion of the call() function.
		@Override
		protected void succeeded() {
			super.succeeded();	
		}
		
		//Write the code here to handle the task being cancelled before call() finishes.
		@Override
		protected void cancelled() {
			super.cancelled();
		}
		
		@Override
		protected void failed() {		
			super.failed();	
		}
	}		
}
