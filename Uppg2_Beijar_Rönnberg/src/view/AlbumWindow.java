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

public class AlbumWindow {

    public void createWindow(Album album) {
        Stage window = new Stage();
        Scene scene = new Scene(new Group(), 800, 600);
        window.setScene(scene);
        window.setTitle(album.toString());

        ListView<SoundClip> soundClipListView = new ListView<>();
        ObservableList<SoundClip> soundClips = FXCollections.observableArrayList(new HashSet<>(album.getSoundClips()));
        soundClipListView.setItems(soundClips);

        Observer observer = new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                System.out.println("AlbumWindow update");
                System.out.println(arg);
                if (arg instanceof String && arg.equals("albumRemoved")) {
                    System.out.println("Album removed");
                    window.close();
                } else {
                    soundClips.clear();
                    soundClips.addAll(new HashSet<>(album.getSoundClips()));
                }
            }
        };

        album.addObserver(observer);

        ((Group) scene.getRoot()).getChildren().add(soundClipListView);
        window.show();
    }

}
