package view;
	
import java.util.List;
import java.util.Optional;

import controller.MusicOrganizerController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.Album;
import model.SoundClip;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;


public class MusicOrganizerWindow extends Application {
	
	private BorderPane bord;
	private static MusicOrganizerController controller;
	private TreeItem<Album> rootNode;
	private TreeView<Album> tree;
	private ButtonPaneHBox buttons;
	private SoundClipListView soundClipTable;
	private TextArea messages;
	
	
	public static void main(String[] args) {
		controller = new MusicOrganizerController();
		if (args.length == 0) {
			controller.loadSoundClips("sample-sound");
		} else if (args.length == 1) {
			controller.loadSoundClips(args[0]);
		} else {
			System.err.println("too many command-line arguments");
			System.exit(0);
		}
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) {
		try {
			controller.registerView(this);
			primaryStage.setTitle("Music Organizer");
			
			bord = new BorderPane();
			Menu menuFile = new Menu("File");

			MenuBar menuBar = new MenuBar();

			MenuItem saveAs = new MenuItem("Save As...");
			saveAs.setOnAction(e -> {
				controller.saveAs(primaryStage);
			});

			MenuItem Load = new MenuItem("Load Hierarchy");
			Load.setOnAction(e -> {
				controller.loadHierarchy();
			});

			menuFile.getItems().add(Load);
			menuFile.getItems().add(saveAs);

			menuBar.getMenus().addAll(menuFile);

			// Create buttons in the top of the GUI
			buttons = new ButtonPaneHBox(controller, this);
			
			VBox topContainer = new VBox();
			menuBar.setUseSystemMenuBar(true);
			topContainer.getChildren().addAll(menuBar, buttons);

			bord.setTop(topContainer);



			// Create the tree in the left of the GUI
			tree = createTreeView();
			bord.setLeft(tree);
			
			// Create the list in the right of the GUI
			soundClipTable = createSoundClipListView();
			bord.setCenter(soundClipTable);
						
			// Create the text area in the bottom of the GUI
			bord.setBottom(createBottomTextArea());
			
			Scene scene = new Scene(bord);
			
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.sizeToScene();
			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {

				@Override
				public void handle(WindowEvent arg0) {
					Platform.exit();
					System.exit(0);
					
				}
				
			});

			
			primaryStage.show();
			
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private TreeView<Album> createTreeView(){
		rootNode = new TreeItem<>(controller.getRootAlbum());
		System.out.println(controller.getRootAlbum() + " is the root album with: " + controller.getRootAlbum().getSubAlbums().size() + " subalbums");
		TreeView<Album> v = new TreeView<>(rootNode);
		
		v.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent e) {
				if(e.getClickCount()==2) {
					Album selectedAlbum = getSelectedAlbum();
					soundClipTable.display(selectedAlbum);
					AlbumWindow albumWindow = new AlbumWindow();
					albumWindow.createWindow(selectedAlbum, controller);
				}
			}
		});
		return v;
	}
	
	private SoundClipListView createSoundClipListView() {
		SoundClipListView v = new SoundClipListView();
		v.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		v.display(controller.getRootAlbum());
		
		v.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				if(e.getClickCount() == 2) {
					controller.playSoundClips(v.getSelectedClips());
				}
			}
		});
		return v;
	}
	
	private ScrollPane createBottomTextArea() {
		messages = new TextArea();
		messages.setPrefRowCount(3);
		messages.setWrapText(true);
		messages.prefWidthProperty().bind(bord.widthProperty());
		messages.setEditable(false); // don't allow user to edit this area
		
		// Wrap the TextArea in a ScrollPane, so that the user can scroll the 
		// text area up and down
		ScrollPane sp = new ScrollPane(messages);
		sp.setHbarPolicy(ScrollBarPolicy.NEVER);
		sp.setVbarPolicy(ScrollBarPolicy.ALWAYS);
		
		return sp;
	}
	
	/**
	 * Displays the message in the text area at the bottom of the GUI
	 * @param message the message to display
	 */
	public void displayMessage(String message) {
		messages.appendText(message + "\n");
	}
	
	public Album getSelectedAlbum() {
		TreeItem<Album> selectedItem = getSelectedTreeItem();
		return selectedItem == null ? null : selectedItem.getValue();
	}
	
	private TreeItem<Album> getSelectedTreeItem(){
		return tree.getSelectionModel().getSelectedItem();
	}

	/**
	 * Updates the tree view with a new root album when you load a new hierarchy
	 * @param newRoot
	 */
	public void updateTreeView(Album newRoot) {
		rootNode = new TreeItem<>(newRoot);
		buildTreeItems(rootNode);
		tree.setRoot(rootNode);
		tree.setShowRoot(true); // Ensure the root is shown
		tree.getSelectionModel().select(rootNode); // Select the root node
	}
	
	/**
	 * Recursively builds the tree view from the given root album
	 * @param parentItem
	 */
	private void buildTreeItems(TreeItem<Album> parentItem) {
		Album parentAlbum = parentItem.getValue();
		for (Album subAlbum : parentAlbum.getSubAlbums()) {
			TreeItem<Album> childItem = new TreeItem<>(subAlbum);
			parentItem.getChildren().add(childItem);
			buildTreeItems(childItem); // Recursive call to build the full tree
		}
	}
	
	
	
	
	/**
	 * Pop up a dialog box prompting the user for a name for a new album.
	 * Returns the name, or null if the user pressed Cancel
	 */
	public String promptForAlbumName() {
		TextInputDialog dialog = new TextInputDialog();
		
		dialog.setTitle("Enter album name");
		dialog.setHeaderText(null);
		dialog.setContentText("Please enter the name for the album");
		Optional<String> result = dialog.showAndWait();
		if(result.isPresent()) {
			return result.get();
		} else {
			return null;
		}
	}
	
	/**
	 * Return all the sound clips currently selected in the clip table.
	 */
	public List<SoundClip> getSelectedSoundClips(){
		return soundClipTable.getSelectedClips();
	}


	
	
	
	/**
	 * *****************************************************************
	 * Methods to be called in response to events in the Music Organizer
	 * *****************************************************************
	 */	
	
	
	
	/**
	 * Updates the album hierarchy with a new album
	 * @param newAlbum
	 */
	public void onAlbumAdded(Album parent, Album newAlbum){
	
		TreeItem<Album> root = tree.getRoot();
		TreeItem<Album> parentNode = findAlbumNode(parent, root);
			
		parentNode.getChildren().add(new TreeItem<>(newAlbum));
		parentNode.setExpanded(true); // automatically expand the parent node in the tree	
			
	}
		
		
	
	public void onAlbumRemoved(Album toRemove){
			
		TreeItem<Album> root = tree.getRoot();
			
		TreeItem<Album> nodeToRemove = findAlbumNode(toRemove, root);
		nodeToRemove.getParent().getChildren().remove(nodeToRemove);
			
	}
		
	private TreeItem<Album> findAlbumNode(Album albumToFind, TreeItem<Album> root) {
			
		// recursive method to locate a node that contains a specific album in the TreeView
			
		if(root.getValue().equals(albumToFind)) {
			return root;
		}
			
		for(TreeItem<Album> node : root.getChildren()) {
			TreeItem<Album> item = findAlbumNode(albumToFind, node);
			if(item != null)
				return item;
		}
		
		return null;
	}
	
	/**
	 * Refreshes the clipTable in response to the event that clips have
	 * been modified in an album
	 */
	public void onClipsUpdated(){
		Album a = getSelectedAlbum();
		soundClipTable.display(a);
	}

	
}
