package browser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Optional;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class FireChromE extends Application {
	private final String ICON_PATH = "/assignment3/BigHero.png";
	private final String DEFAULT_FILE_PATH = "default.txt";
	private final String BOOKMARK_FILE_PATH = "bookmark.txt";
	private double stageX, stageY, stageWidth, stageHeight;
	private String downloadPath, defaultURL;
	private Scene scene; 
	private TabPane tabPane; // Foundation of tabs
	private Menu bookmarkMenu; // Bookmark menu
	private ArrayList<Bookmark> bookmarks; // ArrayList for Bookmarks

	/**
	 * <p>The main() method is not required for JavaFX applications when the JAR file 
	 * for the application is created with the JavaFX Packager tool, 
	 * which embeds the JavaFX Launcher in the JAR file.</p> 
	 * <p>However, it is useful to include the main() method so you can run JAR files 
	 * that were created without the JavaFX Launcher, such as when using an IDE 
	 * in which the JavaFX tools are not fully integrated. </p>
	 * 
	 * @param args In Java args contains the supplied command-line arguments as an array of String objects.
	 * 
	 */
	public static void main(String[] args) { launch(args); }
	/**
	 * the start() is the start point of all JavaFX program.
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {
		bookmarks = new ArrayList<Bookmark>();

		readSettings();
		createMainWindow();	
		createNewBrowserTab();
		makeBookmarkMenu();

		primaryStage.setScene(scene);
		primaryStage.setTitle("My Web Browser - made by Byungseon");
		primaryStage.getIcons().add(new Image(ICON_PATH));
		primaryStage.setX(stageX);
		primaryStage.setY(stageY);
		primaryStage.setOnCloseRequest(ev -> saveSettings());
		primaryStage.show();
	}
	/**
	 * The readSettings() reads default data and bookmarks saved.
	 */
	private void readSettings() {
		ReadDefaultFile(); // Read saved default value from default.txt file
		ReadBookmarkFile(); // Read saved bookmark from bookmark.txt file
	}
	/**
	 * The ReadDefaultFile reads data from default.txt text file by streams of characters,
	 * and saves them to instance variables.
	 */
	private void ReadDefaultFile() {
		String line, attribute, value;
		File file = null;
		FileReader fileReader = null;
		BufferedReader buffReader = null;

		stageX = 50.0;
		stageY = 50.0;
		stageWidth = 1024.0;
		stageHeight = 768.0;
		defaultURL = "http://www.google.ca";
		downloadPath = System.getProperty("user.home") + File.separator + "Download";

		try {
			file = new File(DEFAULT_FILE_PATH);
			if (!file.isFile()) file.createNewFile();
		} catch (Exception error) { error.printStackTrace();}

		try {
			fileReader = new FileReader(DEFAULT_FILE_PATH);
			buffReader = new BufferedReader(fileReader);
			while((line = buffReader.readLine()) != null) {
				attribute = line.substring(0, line.indexOf("="));
				value = line.substring(line.indexOf("=") + 1);
				if (value != null) {
					if (attribute.equals("screenX")) stageX = Double.parseDouble(value);
					else if (attribute.equals("screenY")) stageY = Double.parseDouble(value);
					else if (attribute.equals("width")) stageWidth = Double.parseDouble(value);
					else if (attribute.equals("height")) stageHeight = Double.parseDouble(value);
					else if (attribute.equals("homepage")) defaultURL = value;
					else if (attribute.equals("downloadDirectory")) downloadPath = value;
				}
			}
		} catch (Exception error) {
			error.printStackTrace();
		} finally {
			if (buffReader != null) try { buffReader.close(); } catch (IOException e) {}
			if (fileReader != null) try { fileReader.close(); } catch (IOException e) {}
		}
	}
	/**
	 * The ReadBookmarkFile() reads bookmark data from bookmark.txt text file by streams of characters,
	 * and saves them to bookmarks ArrayList.
	 */
	private void ReadBookmarkFile() {
		File file = null;
		FileInputStream fileInputStream = null;
		ObjectInputStream objInputStream = null;
		Bookmark line = null;

		try {
			file = new File(BOOKMARK_FILE_PATH);
			if (!file.isFile()) file.createNewFile();
		} catch (Exception error) { error.printStackTrace();}

		try {
			fileInputStream = new FileInputStream(BOOKMARK_FILE_PATH);
			objInputStream = new ObjectInputStream(fileInputStream);
			while((line = (Bookmark) objInputStream.readObject()) != null) {
				bookmarks.add(line);
			}
		} catch (EOFException error) {
		} catch (Exception error) {	
			error.printStackTrace();
		} finally {
			if (objInputStream != null) try { objInputStream.close(); } catch (IOException e) {}
			if (fileInputStream != null) try { fileInputStream.close(); } catch (IOException e) {}
		}
	}
	/**
	 * The saveSettings() saves default data and bookmark data.
	 */
	private void saveSettings() {
		writeDefaultFile();
		writeBookmarkFile();
	}
	/**
	 * The writeDefaultFile() saves data of saved instance variables to default.txt text file. 
	 */
	private void writeDefaultFile() {
		FileWriter fileWriter = null;
		BufferedWriter buffWriter = null;

		try {
			fileWriter = new FileWriter(DEFAULT_FILE_PATH, false);
			buffWriter = new BufferedWriter(fileWriter);

			buffWriter.write("screenX=" + scene.getWindow().getX());
			buffWriter.newLine();
			buffWriter.write("screenY=" + scene.getWindow().getY());
			buffWriter.newLine();
			buffWriter.write("width=" + scene.getWidth());
			buffWriter.newLine();
			buffWriter.write("height=" + scene.getHeight());
			buffWriter.newLine();
			buffWriter.write("homepage=" + defaultURL);
			buffWriter.newLine();
			buffWriter.write("downloadDirectory=" + downloadPath);
			buffWriter.newLine();
		} catch (Exception error) {
			error.printStackTrace();
		} finally {
			if (buffWriter != null) try { buffWriter.close(); } catch (IOException e) {}
			if (fileWriter != null) try { fileWriter.close(); } catch (IOException e) {}
		}
	}
	/**
	 * The writeBookmarkFile() saves data of bookmarks ArrayLists to bookmark.txt text file. 
	 */
	private void writeBookmarkFile() {
		FileOutputStream fileOutputStream = null;
		ObjectOutputStream objOutputStream = null;

		try {
			fileOutputStream = new FileOutputStream(BOOKMARK_FILE_PATH);
			objOutputStream = new ObjectOutputStream(fileOutputStream);
			for (Bookmark bookmark : bookmarks) {
				objOutputStream.writeObject(bookmark);
			}
		} catch (Exception error) {
			error.printStackTrace();
		} finally {
			if (objOutputStream != null) try { objOutputStream.close(); } catch (IOException e) {}
			if (fileOutputStream != null) try { fileOutputStream.close(); } catch (IOException e) {}
		}
	}
	/**
	 * The createMainWindow() method is for composition of a scene. 
	 * Most controls and layouts is created in the method. 
	 */
	private void createMainWindow() {
		// File sub menu
		final MenuItem newTab = new MenuItem("New Tab");
		newTab.setAccelerator(new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN));
		newTab.setOnAction(e -> createNewBrowserTab());
		final MenuItem quitMenu = new MenuItem("Quit");	// Quit Menu
		quitMenu.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));
		quitMenu.setOnAction(e -> Platform.exit()); 
		// Help sub menu
		final MenuItem helpJavaMenu = new MenuItem("Get help for Java class"); // Get help for Java Class
		helpJavaMenu.setAccelerator(new KeyCodeCombination(KeyCode.H, KeyCombination.CONTROL_DOWN));
		helpJavaMenu.setOnAction(e -> search());
		final CheckMenuItem showHistoryMenu = new CheckMenuItem("Show History"); //  Show History
		showHistoryMenu.setAccelerator(new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN));
		showHistoryMenu.setOnAction(e -> historyListOpenAndClose());
		final MenuItem help3Menu = new MenuItem("About"); // About
		help3Menu.setAccelerator(new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN));
		help3Menu.setOnAction(e -> about());
		// Javascript Menu
		final MenuItem jsExeMenu = new MenuItem("Execute Code");
		jsExeMenu.setAccelerator(new KeyCodeCombination(KeyCode.J, KeyCombination.CONTROL_DOWN));
		jsExeMenu.setOnAction(e -> executeJavaScript());
		// Setting sub menu
		final MenuItem homeMenu = new MenuItem("Homepage");	// Set Homepage URL
		homeMenu.setAccelerator(new KeyCodeCombination(KeyCode.M, KeyCombination.CONTROL_DOWN));
		homeMenu.setOnAction(e -> setHomepageURL());
		final MenuItem downloadPathMenu = new MenuItem("Download Path"); // Set Download Path
		downloadPathMenu.setAccelerator(new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN));
		downloadPathMenu.setOnAction(e -> setDownloadPath());

		// Menu Bar
		final Menu fileMenu = new Menu("File");
		fileMenu.getItems().addAll(newTab, quitMenu);
		bookmarkMenu = new Menu("Bookmarks");
		final Menu helpMenu = new Menu("Help");
		helpMenu.getItems().addAll(helpJavaMenu, showHistoryMenu, help3Menu);
		final Menu jsMenu = new Menu("JavaScript");
		jsMenu.getItems().addAll(jsExeMenu);
		final Menu settingMenu = new Menu("Settings");
		settingMenu.getItems().addAll(homeMenu, downloadPathMenu);
		final MenuBar menubar = new MenuBar(fileMenu, bookmarkMenu, helpMenu, jsMenu, settingMenu);

		// TabPane for WebViews
		tabPane = new TabPane();
		tabPane.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
			Tab tab = tabPane.getTabs().get((int) newValue);
			Browser browser = (Browser) tab.getContent();
			showHistoryMenu.setSelected(browser.getIsHistoryListOpened());
		});

		final BorderPane root = new BorderPane();
		root.setTop(menubar);
		root.setCenter(tabPane);

		scene = new Scene(root, stageWidth, stageHeight);
	}
	/**
	 * The createNewBrowserTab() method creates new tab for other web browser.
	 */
	private void createNewBrowserTab() {
		Tab tab = new Tab("New Window");
		if (tabPane.getTabs().size() == 0) tab.setClosable(false);
		Browser browser = new Browser(defaultURL, downloadPath);
		browser.addBookmarkListener(new BookmarkActionListener() {
			@Override
			public void onBookmarkButtonClick(BookmarkActionEvent actionEvent) {
				bookmarks.add(new Bookmark(actionEvent.getTitle(), actionEvent.getUrl()));
				MenuItem bookmarkMenuItem = new MenuItem(actionEvent.getTitle(), actionEvent.getIcon());
				bookmarkMenuItem.setOnAction(action -> loadBookmark(actionEvent.getUrl()));
				bookmarkMenu.getItems().add(bookmarkMenuItem);
			}
			@Override
			public boolean isBookmarkThere(BookmarkActionEvent actionEvent) {
				for (Bookmark bookmark : bookmarks) {
					if (actionEvent.getUrl().equals(bookmark.getUrl())) {
						return true;
					}
				}
				return false;
			}
		});

		tab.setContent(browser);
		tabPane.getTabs().add(tab);
		tabPane.getSelectionModel().select(tab);
	}
	/**
	 * The makeBookmarkMenu() adds the bookmark when user clicks the bookmark button on web browser in a tab.
	 */
	private void makeBookmarkMenu() {
		Browser browser = (Browser) tabPane.getTabs().get(tabPane.getSelectionModel().getSelectedIndex()).getContent();
		for (Bookmark bookmark : bookmarks) {
			MenuItem bookmarkMenuItem = new MenuItem(bookmark.getTitle(), browser.loadFavicon(bookmark.getUrl()));
			bookmarkMenuItem.setOnAction(action -> loadBookmark(bookmark.getUrl()));
			bookmarkMenu.getItems().add(bookmarkMenuItem);
		}
	}
	/**
	 * The search() method shows the TextInputDialog control for searching something relate to Java in Google .
	 */
	private void search() {
		Tab tab = tabPane.getTabs().get(tabPane.getSelectionModel().getSelectedIndex());
		Browser browser = (Browser) tab.getContent();
		browser.search();
	}
	/**
	 * The historyListOpenAnsClose() opens and closes the history list on right side of window.
	 */
	private void historyListOpenAndClose() {
		Tab tab = tabPane.getTabs().get(tabPane.getSelectionModel().getSelectedIndex());
		Browser browser = (Browser) tab.getContent();
		browser.animationListDoor();
	}
	/**
	 * The about() method shows a simple Alert dialog box for showing information of this program.
	 */
	private void about() {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Information Dialog");
		alert.setHeaderText("About");
		alert.setContentText("Byungseon's browser, v1.0. April. 17, 2016");
		// Put an icon in alert dialog
		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image(ICON_PATH));
		alert.showAndWait();
	}
	/**
	 * The executeJavaScript() shows a dialog box for executing JavaScript code.
	 */
	private void executeJavaScript() {
		TextInputDialog dialog = new TextInputDialog("alert(window.location)");
		dialog.setTitle("Execute JavaScript");
		dialog.setHeaderText("Please enter your JavaScript code");
		Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image(ICON_PATH));
		
		Optional<String> result = dialog.showAndWait();

		result.ifPresent(name -> {
			Tab tab = tabPane.getTabs().get(tabPane.getSelectionModel().getSelectedIndex());
			Browser browser = (Browser) tab.getContent();
			browser.executeJavaScript(result.get());
		});
	}
	/**
	 * The setHomepageURL() shows a dialog box for setting the default homepage.
	 */
	private void setHomepageURL() {
		TextInputDialog dialog = new TextInputDialog(defaultURL);
		dialog.setTitle("Default URL");
		dialog.setHeaderText("Please enter your default URL");
		Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image(ICON_PATH));

		Optional<String> result = dialog.showAndWait();

		result.ifPresent(name -> {
			defaultURL = result.get();
			for (Tab tab : tabPane.getTabs()) {
				Browser browser = (Browser) tab.getContent();
				browser.setDefaultURL(defaultURL);
			}
		});
	}
	/**
	 * The setDownloadPath() shows a dalog box for setting default download path.
	 */
	private void setDownloadPath() {
		// Create the custom dialog.
		Dialog<String> dialog = new Dialog<>();
		dialog.setTitle("Default Download Path");
		dialog.setHeaderText("Please enter default download path");
		Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image(ICON_PATH));

		// Set the button types.
		ButtonType selectButtonType = new ButtonType("Select", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(selectButtonType, ButtonType.CANCEL);

		// Create the username and password labels and fields.
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(30, 30, 30, 30));

		TextField pathTextField = new TextField();
		pathTextField.setPrefWidth(300.0);
		pathTextField.setPromptText("download locaton");
		pathTextField.setText(downloadPath);

		File tmpdir = new File(downloadPath);
		if (!tmpdir.isDirectory()) downloadPath = "";
		
		Button changeButton = new Button("Change...");
		changeButton.setPrefWidth(100.0);
		changeButton.setOnAction(e -> {
			DirectoryChooser chooser = new DirectoryChooser();
			if (downloadPath != null && downloadPath.length() != 0) {
				try {
					chooser.setInitialDirectory(new File(this.downloadPath));
				} catch (Exception er) {
					chooser.setInitialDirectory(new File(System.getProperty("user.home")));
				}
			} else {
				chooser.setInitialDirectory(new File(System.getProperty("user.home")));
			}

			File initDir = chooser.showDialog(scene.getWindow());
			if (initDir != null)  	pathTextField.setText(initDir.getAbsolutePath()); 
		});

		grid.add(new Label("Download Location:"), 0, 0);
		grid.add(pathTextField, 1, 0);
		grid.add(changeButton, 2, 0);

		dialog.getDialogPane().setContent(grid);

		// Request focus on the path field by default.
		Platform.runLater(() -> pathTextField.requestFocus());

		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == selectButtonType) {
				return pathTextField.getText();
			}
			return null;
		});

		Optional<String> result = dialog.showAndWait();

		result.ifPresent(name -> {
			File dir = new File(result.get());
			if (!dir.exists()) {
				Alert alert = new Alert(AlertType.CONFIRMATION);
	            alert.setTitle("New Directory");
	            alert.setHeaderText("Create New Directory");
	            alert.setContentText("Directory is not existed. Do you make it?");

	            Optional<ButtonType> resultAlert = alert.showAndWait();
	            if (resultAlert.get() == ButtonType.OK){
	            	Alert alertsub = new Alert(AlertType.ERROR);
	            	try { dir.mkdirs(); } catch(Exception err) { 
	    				alertsub.setTitle("Error!");
	    				alertsub.setHeaderText("Directory Creation Error");
	    				alertsub.setContentText("An error occurs while the directory was maden. You had better choose another directory.");
	    				alertsub.showAndWait();
	    				return;
	            	}
	            } else { return; }
			}
			if (!dir.canWrite()) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error!");
				alert.setHeaderText("Directory Permission Error");
				alert.setContentText("You have to select the another directory that can be written.");
				alert.showAndWait();
				return;
			}
			downloadPath = result.get();
			for (Tab tab : tabPane.getTabs()) {
				Browser browser = (Browser) tab.getContent();
				browser.setDownloadPath(downloadPath);
			}
		});
	}
	/**
	 * The loadBookmark(String URL) calls the method that shows the web page 
	 * that user clicks on the bookmark menu.
	 * 
	 * @param URL it is the Url of the bookmark menu a user clicks.
	 */
	private void loadBookmark(String URL) {
		Browser browser = (Browser) tabPane.getTabs().get(tabPane.getSelectionModel().getSelectedIndex()).getContent();
		browser.goURL(URL);
	}
}
