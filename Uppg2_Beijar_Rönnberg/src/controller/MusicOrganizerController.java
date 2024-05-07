package controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import model.Album;
import model.SoundClip;
import model.SoundClipBlockingQueue;
import model.SoundClipLoader;
import model.SoundClipPlayer;
import view.MusicOrganizerWindow;
import java.io.File;

public class MusicOrganizerController {

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
		Album newAlbum = new Album(name, parent);
		view.onAlbumAdded(newAlbum);

		
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
		view.onAlbumRemoved();
		album.markAsChanged();
		album.notifyObservers("albumRemoved");
		System.out.println("Album removed");
	}
	
	/**
	 * Adds sound clips to an album
	 */
	public void addSoundClips(){ 
		Album album = view.getSelectedAlbum();
		if(album == null) {
			view.displayMessage("Please select an album to add sound clips to.");
			return;
		}
		List<SoundClip> clips = view.getSelectedSoundClips();
			Album parentAblum = album.getParentAlbum();
			while (parentAblum != null) {
				for(SoundClip clip : clips) {
					if (!parentAblum.getSoundClips().contains(clip)){
						album.addSoundClip(clip);
					}
				}
				parentAblum = parentAblum.getParentAlbum();
			}
		album.markAsChanged();
		album.notifyObservers("clipsUpdated");
		System.out.println("Clips updated");
		view.onClipsUpdated();
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
		System.out.println("Clips updated");
		view.onClipsUpdated();
	}
	
	/**
	 * Puts the selected sound clips on the queue and lets
	 * the sound clip player thread play them. Essentially, when
	 * this method is called, the selected sound clips in the 
	 * SoundClipTable are played.
	 */
	public void playSoundClips(){
		List<SoundClip> l = view.getSelectedSoundClips();
		queue.enqueue(l);
		for(int i=0;i<l.size();i++) {
			view.displayMessage("Playing " + l.get(i));
		}
		// play the soundclip
		Media media = new Media(l.get(0).getFile().toURI().toString());
		MediaPlayer mediaPlayer = new MediaPlayer(media);
		mediaPlayer.play();
	}
}
