package model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Observable;

/**
 * Album is a class representing a collection of
 * sound clips and sub-albums.
 */
public class Album extends Observable implements Serializable{
    private final String title;
    private final Album parentAblum;
    private HashSet<Album> subAlbums = new HashSet<>();
    private HashSet<SoundClip> soundClips = new HashSet<>();

    public Album(String title) {
        this.title = title;
        this.parentAblum = null;
    }

    /**
     * Make a new album with a title and parent album.
     *
     * @param title
     * @param parentAblum
     */
    public Album(String title, Album parentAblum) {
        this.title = title;
        this.parentAblum = parentAblum;
        // add the album to the parent album
        parentAblum.getSubAlbums().add(this);
    }

    /**
     * Add a new sub album to the parent album.
     *
     * @param soundClip
     */
    public void addSoundClip(SoundClip soundClip) {
        soundClips.add(soundClip);
    }

    /**
     * Check if the sound clip is found in the album.
     *
     * @param soundClip
     * @return true if the sound clip is found in the album.
     */
    public void removeSoundClip(SoundClip soundClip) {
        soundClips.remove(soundClip);
    }

    /**
     * Remove the sub album from the parent album.
     *
     * @param subAlbum
     */
    public void removeSubAlbum(Album subAlbum) {
        subAlbums.remove(subAlbum);
    }

    /**
     * @return the title of the album.
     */
    public Boolean isRootAlbum() {
        return parentAblum == null;
    }

    /**
     * Add the sound clip to the album.
     *
     * @param soundClip
     */
    public Boolean hasSubAlbums() {
        return !subAlbums.isEmpty();
    }

    /**
     * @return the number of sound clips and sub-albums in the album.
     */
    public int size() {
        return soundClips.size() + subAlbums.size();
    }

    /**
     * @return the title of the album.
     */
    public HashSet<Album> getSubAlbums() {
        return subAlbums;
    }

    /**
     * @return the sound clips in the album.
     */
    public HashSet<SoundClip> getSoundClips() {
        return soundClips;
    }

    public void markAsChanged() {
        setChanged();
    }

    /**
     * @return the parent album of the album.
     */
    public Album getParentAlbum() {
        return parentAblum;
    }

    @Override
    public String toString() {
        return title;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Album && ((Album) obj).title.equals(title);
    }
}
