package browser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class DownloadTasks extends HBox {
	private String sourceUrl;
	private String downloadPath;
	private Label filenameLabel;
	private ProgressBar progBar;
	private ProgressIndicator progIndicator;
	private Button cancelButton;
	private Task<Integer> downloadTask;
	private String saveFilePath;
	private HBox root;

	public DownloadTasks(String sourceUrl, String downloadPath) {
		this.sourceUrl = sourceUrl;
		this.downloadPath = downloadPath;
		createWindowPiece();
		try { downloadFile(); } catch(Exception err) { err.printStackTrace(); }
	}
	
	public String getSourceUrl() { return sourceUrl; }
	public String getDownloadPath() { return downloadPath; }
	public Task<Integer> getDownloadTask() { return downloadTask; }
	public String getSaveFilePath() { return saveFilePath; }
	
	private void createWindowPiece() {
		
		filenameLabel = new Label();
		filenameLabel.setPrefWidth(100.0);
		filenameLabel.setAlignment(Pos.CENTER_RIGHT);
		
		progBar = new ProgressBar();
		progBar.setPrefWidth(200.0);
		
		progIndicator = new ProgressIndicator();
		
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
		
		root = new HBox(filenameLabel, progBar, progIndicator, cancelButton);
		root.setPrefHeight(USE_COMPUTED_SIZE);
		root.setPrefWidth(USE_COMPUTED_SIZE);
		root.setAlignment(Pos.CENTER);
		root.setStyle("-fx-border-width: 1px; -fx-border-color: darkgrey;");
		root.setPadding(new Insets(3, 3, 3, 3));
		root.setSpacing(10.0);
		HBox.setHgrow(progBar, Priority.ALWAYS);
		
		this.getChildren().addAll(root);
	}
	
	private void downloadFile() throws IOException {

		downloadTask = new HttpDownloadUtility(getSourceUrl(), getDownloadPath());
		
        progBar.progressProperty().bind(downloadTask.progressProperty());
        progIndicator.progressProperty().bind(downloadTask.progressProperty());

        Thread thread = new Thread(downloadTask);
        thread.setDaemon(true);
        thread.start();
	}

	/**
	 * A utility that downloads a file from a URL.
	 * @author beginanew.life
	 *
	 */
	public class HttpDownloadUtility extends Task<Integer> {
		
		private static final int BUFFER_SIZE = 4096;
		private String sourceURL, downloadPath;
  
		public HttpDownloadUtility(String sourceURL, String downloadPath) {
			this.sourceURL = sourceURL;
			this.downloadPath = downloadPath;
		}
		
		@Override
		protected Integer call() throws Exception {
			
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
		      		String fullFileName = getDownloadPath() + File.separator + numFileName + fileExtension;
		      		File file = new File(fullFileName);
		      		if (!file.isFile()) {
		      			fileName = numFileName;
		      			break;
		      		}
		      		count++;
		      	}
		      	
		      	fileName = fileName + fileExtension;
		      	final String fname = fileName;
		      	Platform.runLater(new Runnable() {
	                  @Override public void run() {
	                    filenameLabel.setText(fname);    
	                  }
	            });
	 
	            // opens input stream from the HTTP connection
	            InputStream inputStream = httpConn.getInputStream();
	            saveFilePath = downloadPath + File.separator + fileName;
	            
	            // opens an output stream to save into file
	            FileOutputStream outputStream = new FileOutputStream(saveFilePath);
	 
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
	        
	        return contentLength;
		}
		
		@Override
		protected void succeeded() {
			super.succeeded();
		};
		
		@Override
		protected void cancelled() {
			super.cancelled();
			File delFile = new File(saveFilePath);
			delFile.delete();
		}

		@Override
		protected void failed() {
			super.failed();
			File delFile = new File(saveFilePath);
			delFile.delete();
		}
	}

}
