package browser;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import javafx.util.Duration;

public class Browser extends Region {
	private final double HISTORYLIST_WIDTH = 200.0; 
	private String defaultURL;
	private String downloadPath;
	private WebView browserView;
	protected WebEngine engine;
	private WebHistory history;
	private TextField urlText;
	private ListView<WebHistory.Entry> historyList;
	private Button backButton, bookmarkButton, nextButton;
	private boolean isHistoryListOpened;
	private BorderPane root;
	private List<BookmarkActionListener> bookmarkListeners;
    
    public Browser(String defaultURL, String downloadPath) {
    	this.defaultURL = defaultURL;
    	this.downloadPath = downloadPath;
    	
		browserView = new WebView();
		engine = browserView.getEngine();
		history = engine.getHistory();
		bookmarkListeners = new ArrayList<BookmarkActionListener>();

		engine.load(this.defaultURL);
		createScene();	// The method for composition of a scene
		processForLoading(); // The method for processing when a web page is loaded
		processForHistory(); // The method for control of web history
    }

	/**
	 * The createScene() method is for composition of a scene. 
	 * Most controls and layouts is created in the method. 
	 * 
	 * if the browser history is at the first page, back button should be disabled.
	 * if the browser history is at the last page, next button should be disabled.
	 * 
	 */
	private void createScene() {
		// Browser Bar
		MouseHandler myHandler = new MouseHandler();

		final Tooltip backButtonTooltip = new Tooltip();
		backButtonTooltip.setText(
		    "If you click this button,\n" +
		    "it will be show previous webpage."
		);
		backButton = new Button("Back");
		backButton.disableProperty().bind(history.currentIndexProperty().lessThanOrEqualTo(0)); // Disable Condition
		backButton.setOnMouseClicked(myHandler);
		backButton.setTooltip(backButtonTooltip);
		urlText = new TextField();
		urlText.setOnMouseClicked(myHandler);
		urlTextEvents();

		final Tooltip bookmarkButtonTooltip = new Tooltip();
		bookmarkButtonTooltip.setText(
		    "If you click this button,\n" +
		    "it will save current webpage to\n" +
		    "bookmark."
		);
		bookmarkButton = new Button("Add Bookmark");
		bookmarkButton.setDisable(true);
		bookmarkButton.setTooltip(bookmarkButtonTooltip);
		bookmarkButton.setOnMouseClicked(myHandler);
		
		final Tooltip nextButtonTooltip = new Tooltip();
		nextButtonTooltip.setText(
		    "If you click this button,\n" +
		    "it will be show next webpage."
		);
		nextButton = new Button("Next");
		nextButton.disableProperty().bind(
                history.currentIndexProperty().greaterThanOrEqualTo(Bindings.size(history.getEntries()).subtract(1)));
		nextButton.setOnMouseClicked(myHandler);
		nextButton.setTooltip(nextButtonTooltip);
		HBox browserBar = new HBox(backButton, urlText, bookmarkButton, nextButton);
		HBox.setHgrow(urlText, Priority.ALWAYS);

		// Main Window
		historyList = new ListView<>();
		historyList.setItems(history.getEntries());
		historyList.setMaxWidth(0.0);
		isHistoryListOpened = false;

		// WebView KeyPress Event 
		browserView.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.isControlDown()) {
					if (ke.getCode() == KeyCode.LEFT && !backButton.isDisable()) { // history back
						goBack();
					} else if (ke.getCode() == KeyCode.RIGHT && !nextButton.isDisable()) { // history forward
						goForward();
					}
				}
			}
		});
		
		root = new BorderPane();
		root.setPadding(new Insets(5, 2, 2, 2));
		root.setTop(browserBar);
		root.setCenter(browserView);
		root.setRight(historyList);

		getChildren().add(root);
	}
	
	@Override
    protected void layoutChildren() {
        layoutInArea(root, 0.0, 0.0, getWidth(), getHeight(), 0.0, HPos.CENTER, VPos.CENTER);
    }
	/**
	 * <p>The processForLoading() method consists of two module. 
	 * The one is for listening for changes of state for the web page loader,
	 * and the other is for file download. Most of them are the code of my professor, 
	 * and it is an example of a 3-parameter Lambda function.</p>
	 * <p> When the web page was loaded normally,</p>
	 * <ol><li>Change the title of the stage into the title of current web page</li>
	 * <li>Change the text of the browser bar to the URL of current web page</li>
	 * <li>if this page is at the bookmark menu, bookmark button should be disabled.</li></ol>
	 */
	private synchronized void processForLoading() {
		
		engine.getLoadWorker().stateProperty().addListener((ov, oldState, newState) -> {
			// This if statement gets run if the new page load succeeded.
			if (newState == State.SUCCEEDED) {
				TabPane tabs = (TabPane) getParent().getParent();
				Tab tab = tabs.getTabs().get(tabs.getSelectionModel().getSelectedIndex());
				tab.setText(engine.getTitle());
				tab.setGraphic(loadFavicon(engine.getDocument().getBaseURI()));
				String url = engine.getLocation();
				urlText.setText(url);
				fireBookmarkEvent(1);
			}
		});
		
		// 1) Monitor the location url, and 2) If it is a pdf file, then create a pdf viewer for it, 3) if it is downloadable, then download it.
		engine.locationProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {

				String downloadableFile = null;  
			    String[] downloadableFiles = { ".doc", ".docx", ".xls", ".xlsx", ".iso", 
			    		".zip", ".tgz", ".jar", ".pdf", ".exe", ".img", ".dmg", ".tar" };
			    for (String extension : downloadableFiles) {
			    	if (newValue.endsWith(extension)) {
			    		downloadableFile = extension;
			    		break;
			    	}
			    }
			    if (downloadableFile != null) {  
			    	int filenameIndex = newValue.lastIndexOf("/") + 1; // if file name is existed?
			      	if (filenameIndex != 0) {
			      		try { new DownloadBar(newValue, downloadPath); } catch (Exception e) {};
			      	}
			    }
			}
		});
		
		engine.setOnAlert(ev -> {
			Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Alert");
            alert.setHeaderText("JavaScript Alert Message");
            alert.setContentText(engine.getTitle());
            alert.showAndWait();
        });
	}

	/**
	 * <p>The processForHistory() method is for control of web history.</p> 
	 * <ul><li>If there are some changes in web history, 
	 * the method adds or removes the same data in ListView control for saving of the history</li>
	 * <li>If a user click the row of ListView control, the web page will show in this browser.</li></ul>
	 */
	private void processForHistory() {

		historyList.setCellFactory(lv -> new ListCell<WebHistory.Entry>() {
            @Override
            public void updateItem(WebHistory.Entry entry, boolean empty) {
                super.updateItem(entry, empty);

                if (empty) {
                	setText(null);
                	setGraphic(null);
                } else {
                	textProperty().set((entry.getTitle() == null ) ? entry.getUrl() : entry.titleProperty().get());
                	graphicProperty().setValue(loadFavicon(entry.getUrl()));
                }
            }
        });

        history.currentIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            if (newIndex.intValue() != historyList.getSelectionModel().getSelectedIndex()) {
                historyList.getSelectionModel().clearAndSelect(newIndex.intValue());
            }
        });

        historyList.getSelectionModel().selectedIndexProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue.intValue() != history.getCurrentIndex()) {
                history.go(newValue.intValue() - history.getCurrentIndex());
            }
        });
	}

	/**
	 * It is the inner class of MyGUI class for handling of Mouse events.
	 * Back button, Bookmark button, Next button and Text field for URL's entry
	 * use the handle method of this class.
	 *
	 */
	private class MouseHandler implements EventHandler<MouseEvent> {
		/**
		 * <p>The handle() method is for handling of mouse click events</p>
		 * <ul><li>If navigation back button is pressed, the web page is moved to the previous page if it exists.</li>
		 * <li>If navigation next button is pressed, the web page is moved to the next page if it exists.</li>
		 * <li>If bookmark button is pressed, the web page is saved in the bookmark menu.</li>
		 * <li>If the text field for URL is single clicked, all texts in text field is selected.</li>
		 * <li>If the text field for URL is double clicked, all texts in text field is erased.</li></ul>
		 * 
		 * @param e It is the object of the MouseEvent class.
		 */
		@Override
		public void handle(MouseEvent e) {
			if (e.getSource().equals(backButton)) {
				if (e.getClickCount() == 1)	goBack();
			} else if (e.getSource().equals(urlText)) {
				if (e.getClickCount() == 2)
					urlText.setText("");
				else if (e.getClickCount() == 1) 
					urlText.selectAll(); 
			} else if (e.getSource().equals(bookmarkButton)) {
				if (e.getClickCount() == 1) {
					addBookmarks();
				}
			} else if (e.getSource().equals(nextButton)) {
				if (e.getClickCount() == 1)	goForward();
			}
			e.consume();
		}
	}

	/**
	 * The search() method shows the TextInputDialog control for searching something relate to Java in Google .
	 */
	public void search() {
		TextInputDialog dialog = new TextInputDialog("type here");
		dialog.setTitle("Find help for Java class");
		dialog.setHeaderText("Search for Java Class Documentation");
		dialog.setContentText("Witch Java class do you want to research?");

		Optional<String> result = dialog.showAndWait();
		result.ifPresent(name -> {
			String query = "https://www.google.ca/search?q=java+" + 
					result.get() + "&sourceid=chrome&ie=UTF-8";
			engine.load(query);
		});
	}

	/**
	 * The animationList() method shows a small part of examples of JavaFX animation when "Show History" of Help menu is clicked.
	 */
	public void animationListDoor() {
		if (!isHistoryListOpened) {
			historyList.setMaxWidth(HISTORYLIST_WIDTH);
			
			FadeTransition ft = new FadeTransition(Duration.millis(500.0));
			ft.setByValue(0.3);
			ft.setCycleCount(2);
			ft.setAutoReverse(true);

			TranslateTransition tt = new TranslateTransition(Duration.millis(10.0));
			tt.setFromX(0.0);
			tt.setToX(20.0);
			tt.setCycleCount(50);
			tt.setAutoReverse(true);
			
			ParallelTransition pt = new ParallelTransition(historyList, ft,  tt);
			pt.play();
			isHistoryListOpened = true;
		} else {
			TranslateTransition t1 = new TranslateTransition(Duration.millis(10.0));
			t1.setByX(0.0);
			t1.setCycleCount(1);
			t1.setAutoReverse(true);
			
			TranslateTransition t2 = new TranslateTransition(Duration.millis(10.0));
			t2.setFromX(0.0);
			t2.setToX(20.0);
			t2.setCycleCount(50);
			t2.setAutoReverse(true);
			
			TranslateTransition t3 = new TranslateTransition(Duration.millis(500.0));
			t3.setByX(HISTORYLIST_WIDTH);
			t3.setCycleCount(1);
			t3.setAutoReverse(true);

			SequentialTransition st = new SequentialTransition (historyList, t1, t2, t3);
			st.setOnFinished(event -> {
				historyList.setMaxWidth(0.0);
				isHistoryListOpened = false;
			});
			st.play();
		}
	}

	public boolean getIsHistoryListOpened() { return isHistoryListOpened; }

	/**
	 * The urlTextEvents() controls some processes when a user presses Enter key.
	 * If the string a user inputs is a URL, the method loads new web page.
	 * If not, the method call Google with the string.
	 */
	private void urlTextEvents() {
		urlText.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.ENTER) {
				String url;
				if (isURL(urlText.getText()))
					url = urlText.getText();
				else 
					url = "https://www.google.ca/search?q=" + 
							urlText.getText() + "&ie=UTF-8";
				engine.load(url);
				e.consume();
			}
		});
	}

	/**
	 * Tell the engine to go back 1 page in the history
	 */
	public void goBack() { history.go(-1); }

	/**
	 * Tell the engine to go forward 1 page in the history
	 */
	public void goForward() { history.go(1); }

	public void goURL(String URL) { engine.load(URL); }
	
	public void setDefaultURL(String URL) { defaultURL = URL; }
	
	public void setDownloadPath(String Path) { downloadPath = Path; }
	
	/**
	 * The isURL() function is judge whether parameter is URL or not.
	 * @param url the string that is waiting for judge 
	 * @return If url is URL, return true
	 */
	private boolean isURL(String url) {
		int idx = url.indexOf("://");
		if (idx == -1) return false;
		else return true;
	}
	
	/**
	 * The loadFavicon function is for searching the favicon of some website.
	 * The function is using google favicon search application.
	 * @param location URL for taking a favicon
	 * @return return ImageView node for a favicon if success
	 */
	public ImageView loadFavicon(String location) {
		ImageView favicon = null;
		try {
		    String faviconUrl = String.format("http://www.google.com/s2/favicons?domain_url=%s", URLEncoder.encode(location, "UTF-8"));
		    favicon = new ImageView(new Image(faviconUrl, true));
		} catch (UnsupportedEncodingException ex) {
		}
		return favicon;
	}
	
	public void executeJavaScript(String command) {
		engine.executeScript(command);
	}
	
	private synchronized void addBookmarks() {
		fireBookmarkEvent(0);
		bookmarkButton.setDisable(true);
	}
	
	public synchronized void addBookmarkListener(BookmarkActionListener event) {
        bookmarkListeners.add(event);
    }
    
    public synchronized void removeBookmarkListener(BookmarkActionListener event) {
    	bookmarkListeners.remove(event);
    }
     
    private synchronized void fireBookmarkEvent(int whichEvent) {
    	String title = engine.getTitle();
		String location = engine.getLocation();
		if (title == null || title == "") title = location;
		ImageView icon = loadFavicon(engine.getDocument().getBaseURI());
        BookmarkActionEvent event = new BookmarkActionEvent(this, title, location, icon);
        Iterator<BookmarkActionListener> listeners = bookmarkListeners.iterator();
        while( listeners.hasNext() ) {
            if (whichEvent == 0) 
            	(listeners.next()).onBookmarkButtonClick(event);
            else bookmarkButton.setDisable((listeners.next()).isBookmarkThere(event));
        }
    }
}
