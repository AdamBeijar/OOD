package view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import model.Album;
import model.SoundClip;

import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;

import controller.MusicOrganizerController;
import javafx.scene.input.MouseEvent;

public class AlbumWindow {

    private MusicOrganizerController controller;

    /**
     * Create a new window for the album.
     * @param album
     */
    public void createWindow(Album album, MusicOrganizerController InputController) {
        Stage window = new Stage();
        Scene scene = new Scene(new Group(), 300, 600);
        window.setScene(scene);
        window.setTitle(album.toString());

        controller = InputController;

        // Create a list view for the sound clips
        SoundClipListView soundClipListView = createSoundClipListView(album);
        soundClipListView.setPrefSize(300, 600);
        // Create an observable list for the sound clips
        ObservableList<SoundClip> soundClips = FXCollections.observableArrayList(new HashSet<>(album.getSoundClips()));
        soundClipListView.setItems(soundClips);

        /* 
         * Create an observer for the album. The observer will update the list view
         */
        Observer observer = new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                // Update the list view
                if (arg instanceof String && arg.equals("albumRemoved")) {
                    // if the album is removed, close the window
                    window.close();
                } else {
                    // Else update the list view
                    soundClips.clear();
                    soundClips.addAll(new HashSet<>(album.getSoundClips()));
                }
            }
        };

        album.addObserver(observer);

        ((Group) scene.getRoot()).getChildren().add(soundClipListView);
        window.show();
    }

    /**
     * Create a list view for the sound clips.
     * @param album
     * @return
     */
    private SoundClipListView createSoundClipListView(Album album) {
		SoundClipListView v = new SoundClipListView();
        v.setOnMouseClicked((MouseEvent e) -> {
            if (e.getClickCount() == 2) {
                SoundClip selectedClip = v.getSelectionModel().getSelectedItem();
                controller.playSoundClips(v.getSelectedClips());
            }
        });
        return v;
	}

}
