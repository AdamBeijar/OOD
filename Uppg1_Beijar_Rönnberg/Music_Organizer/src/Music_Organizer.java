public class Music_Organizer {
    private Album rootAlbum;
    public Music_Organizer() {
        this.rootAlbum = new Album("All Sound Content");
    }

    /**
     * @return the root album of the music organizer.
     */
    public Album getRootAlbum() {
        return rootAlbum;
    }

    /**
     * Add a new sub album to the parent album.
     * @param name
     * @param parentAlbum
     */
    public void AddNewSubAlbum(String name, Album parentAlbum) {
        Album newAlbum = new Album(name, parentAlbum);
    }

    /**
     * Check if the sound clip is found in the album.
     * @param soundClip
     * @param album
     * @return true if the sound clip is found in the album.
     */
    public Boolean isSoundClipFound(SoundClip soundClip, Album album) {
        if (album.getSoundClips().isEmpty() && album.getSubAlbums().isEmpty()){
            return false;
        }
        if (!album.getSubAlbums().isEmpty()) {
            for (Album subAlbum: album.getSubAlbums()) {
                if (isSoundClipFound(soundClip, subAlbum)) {
                    return true;
                }
            }   
        }
        return album.getSoundClips().contains(soundClip);
    }

    /**
     * Remove the sound clip from the album.
     * @param soundClip
     * @param album
     */
    public void removeSoundClip(SoundClip soundClip, Album album) {
        if (album.isRootAlbum()) {
           soundClip.remove(); 
        }
        if (album.getSoundClips().contains(soundClip)) {
            album.removeSoundClip(soundClip);
        }
    }

    /**
     * Remove the sub album from the parent album.
     * @param album
     */
    public void removeSubAlbum(Album album) {
        album.getParentAlbum().removeSubAlbum(album);
    }

    /**
     * Add the sound clip to the album.
     * @param soundClip
     * @param album
     */
    public void addSoundClip(SoundClip soundClip, Album album) {
        Album rootAlbum = getRootAlbum();
        if (!rootAlbum.getSoundClips().contains(soundClip)) {
            rootAlbum.getSoundClips().add(soundClip);
        } else {
            album.addSoundClip(soundClip);
        }
    }
}
