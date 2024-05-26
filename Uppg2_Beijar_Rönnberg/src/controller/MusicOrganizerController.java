package controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import model.Album;
import model.SoundClip;
import model.SoundClipBlockingQueue;
import model.SoundClipLoader;
import model.SoundClipPlayer;
import view.MusicOrganizerWindow;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javafx.stage.Stage;

public class MusicOrganizerController implements Serializable{

	private MusicOrganizerWindow view;
	private SoundClipBlockingQueue queue;
	private Album root;
	
	public MusicOrganizerController() {
		root = new Album("All Sound Clips");
		
		// Create the blocking queue
		queue = new SoundClipBlockingQueue();
				
		// Create a separate thread for the sound clip player and start it
		
		(new Thread(new SoundClipPlayer(queue))).start();
	}
	
	/**
	 * Load the sound clips found in all subfolders of a path on disk. If path is not
	 * an actual folder on disk, has no effect.
	 */
	public Set<SoundClip> loadSoundClips(String path) {
		Set<SoundClip> clips = SoundClipLoader.loadSoundClips(path);
		for(SoundClip clip : clips) {
			root.addSoundClip(clip);
		}
		return clips;
	}
	
	public void registerView(MusicOrganizerWindow view) {
		this.view = view;
	}
	
	/**
	 * Returns the root album
	 */
	public Album getRootAlbum(){
		return root;
	}
	
	/**
	 * Adds an album to the Music Organizer
	 */
	public void addNewAlbum(){ 
		Album parent = view.getSelectedAlbum();
		if(parent == null) {
			view.displayMessage("Please select an album to add a new album to.");
			return;
		}
		String name = view.promptForAlbumName();
		if(name == null) {
			view.displayMessage("No album name entered.");
			return;
		}
		System.out.println(getRootAlbum().toString());
		for (Album a : getRootAlbum().getSubAlbums()) {
			System.out.println(a.toString());
		}
		Album newAlbum = new Album(name, parent);
		view.onAlbumAdded(parent, newAlbum);
	}
	
	/**
	 * Removes an album from the Music Organizer
	 */
	public void deleteAlbum(){ 
		Album album = view.getSelectedAlbum();
		if(album == null) {
			view.displayMessage("Please select an album to delete.");
			return;
		}
		Album parent = album.getParentAlbum();
		if(parent == null) {
			view.displayMessage("Cannot delete the root album.");
			return;
		}
		parent.removeSubAlbum(album);
		view.onAlbumRemoved(album);
		album.markAsChanged();
		album.notifyObservers("albumRemoved");
	}
	
	/**
	 * Adds sound clips to an album
	 */
	public void addSoundClips() { 
		Album album = view.getSelectedAlbum();
		if (album == null) {
			view.displayMessage("Please select an album to add sound clips to.");
			return;
		}
		List<SoundClip> clips = view.getSelectedSoundClips();
		
		for (SoundClip clip : clips) {
			addClipToAlbumAndParents(album, clip); // Add the clip to the album and all its parent albums
		}
		
		album.markAsChanged();
		album.notifyObservers("clipsUpdated");
		view.onClipsUpdated();
	}
	
	/**
	 * Helper method to add a sound clip to an album and all its parent albums
	 */
	private void addClipToAlbumAndParents(Album album, SoundClip clip) {
		if (album == null) return;
		
		// Add the clip to the current album
		album.addSoundClip(clip);
		
		// Recurse up to add the clip to parent albums
		addClipToAlbumAndParents(album.getParentAlbum(), clip);
	}
	
	/**
	 * Removes sound clips from an album
	 */
	public void removeSoundClips(){ 
		Album album = view.getSelectedAlbum();
		if(album == null) {
			view.displayMessage("Please select an album to remove sound clips from.");
			return;
		}
		List<SoundClip> clips = view.getSelectedSoundClips();
		HashSet<Album> subAlbums = album.getSubAlbums();
		for (Album subAlbum : subAlbums) {
			for(SoundClip clip : clips) {
				subAlbum.removeSoundClip(clip);
			}
		}
		album.markAsChanged();
		album.notifyObservers("clipsUpdated");
		view.onClipsUpdated();
	}
	
	/**
	 * Puts the selected sound clips on the queue and lets
	 * the sound clip player thread play them. Essentially, when
	 * this method is called, the selected sound clips in the 
	 * SoundClipTable are played.
	 */
	public void playSoundClips(List<SoundClip> l){
		queue.enqueue(l);
		for(int i=0;i<l.size();i++) {
			view.displayMessage("Playing " + l.get(i));
		}
		// play the soundclip
		Media media = new Media(l.get(0).getFile().toURI().toString());
		MediaPlayer mediaPlayer = new MediaPlayer(media);
		mediaPlayer.play();
	}

	/**
	 * Saves the current state of the Music Organizer to a file
	 */
	public void saveAs(Stage primaryStage) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save As");
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("HTML", "*.html"), new FileChooser.ExtensionFilter("Searialize", "*.ser")); // only allow saving as HTML or .ser files
		File file = fileChooser.showSaveDialog(primaryStage);
		String fileExtension = file.getName().substring(file.getName().lastIndexOf("."));
		if (file != null) {
			if (fileExtension.equals(".html")) {
				file = createHtml(root, file);
				view.displayMessage("Successfully saved hierarchy to " + file.getName());
			} else if (fileExtension.equals(".ser")) {
				saveHierarchy(file);
				view.displayMessage("Successfully saved hierarchy to " + file.getName());
			} else {
				view.displayMessage("Invalid file type. Please save as HTML or JSON.");
			}
		}
	}

	/**
	 * Saves the Music Organizer hierarchy to a .ser file
	 * @param file
	 */
	public void saveHierarchy(File file) {
		try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
			out.writeObject(root);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Loads a Music Organizer hierarchy from a .ser file
	 */
	public void loadHierarchy() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Load Hierarchy");
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Searialize", "*.ser")); // only allow loading .ser files 
		File file = fileChooser.showOpenDialog(null);
		if (file != null) {
			view.displayMessage("Loading hierarchy from " + file.getName());
			try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
				root = (Album) in.readObject();
				view.updateTreeView(root);
				view.onClipsUpdated();
				view.displayMessage("Successfully loaded hierarchy from " + file.getName());
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Creates an HTML file from the Music Organizer
	 * @param rootAlbum
	 * @param file
	 * @return the created HTML file
	 */
	public File createHtml(Album rootAlbum, File file){
		String html = createAlbumHTML(rootAlbum);
		File htmlFile = new File("htmlTemplate/template.html"); // template file
		String htmlString = getHtmlString(htmlFile); // get the template as a string
		String title = "Music Organizer";
		htmlString = htmlString.replace("$title", title);
		htmlString = htmlString.replace("$list", html); // replace the list with the album hierarchy
		file.setWritable(true);
		try {
			Files.write(file.toPath(), htmlString.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file;
	}

	/**
	 * Gets the contents of an HTML file as a string
	 * @param html
	 * @return the contents of the HTML file as a string
	 */
	public String getHtmlString(File html){
		String htmlString = "";
		try {
			htmlString = new String(Files.readAllBytes(html.toPath()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return htmlString;
	}

	/**
	 * Creates an HTML representation of the album hierarchy
	 * @param album
	 * @return the HTML representation of the album hierarchy
	 */
	public String createAlbumHTML(Album album){
		String html = "<ul>"; // start the list
		for (Album subAlbum : album.getSubAlbums()){
			html += "<li><b>" + subAlbum.toString() + "</b></li>"; // for every subalbum, add a list item
			html += createAlbumHTML(subAlbum); // recursively add the subalbum's subalbums
		}
		for (SoundClip clip : album.getSoundClips()){
			html += "<li>" + clip.toString() + "</li>"; // for every soundclip, add a list item
		}
		html += "</ul>"; // end the list
		return html;
	}
}
