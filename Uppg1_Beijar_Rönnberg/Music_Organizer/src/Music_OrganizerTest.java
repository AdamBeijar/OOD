import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

public class Music_OrganizerTest {
    @Test
    public void testAddNewSubAlbum() {
        Music_Organizer organizer = new Music_Organizer();
        Album rootAlbum = organizer.getRootAlbum();
        organizer.AddNewSubAlbum("Sub Album", rootAlbum);
        assertTrue(rootAlbum.hasSubAlbums());
    }

    @Test
    public void testAddSoundClip() {
        Music_Organizer organizer = new Music_Organizer();
        Album rootAlbum = organizer.getRootAlbum();
        SoundClip soundClip = new SoundClip(new File("../testFiles/test.txt"));
        organizer.addSoundClip(soundClip, rootAlbum);
        assertTrue(rootAlbum.getSoundClips().contains(soundClip));
    }

    @Test
    public void testGetRootAlbum() {
        Music_Organizer organizer = new Music_Organizer();
        assertTrue(organizer.getRootAlbum().isRootAlbum());
    }

    @Test
    public void testIsSoundClipFound() {
        Music_Organizer organizer = new Music_Organizer();
        Album rootAlbum = organizer.getRootAlbum();
        SoundClip soundClip = new SoundClip(new File("../testFiles/test.txt"));
        organizer.addSoundClip(soundClip, rootAlbum);
        assertTrue(organizer.isSoundClipFound(soundClip, rootAlbum));
    }

    @Test
    public void testRemoveSoundClip() {
        Music_Organizer organizer = new Music_Organizer();
        Album rootAlbum = organizer.getRootAlbum();
        SoundClip soundClip = new SoundClip(new File("../testFiles/test.txt"));
        System.out.println(rootAlbum.size());
        organizer.addSoundClip(soundClip, rootAlbum);
        System.out.println(rootAlbum.size());
        organizer.removeSoundClip(soundClip, rootAlbum);
        System.out.println(rootAlbum.size());
        assertTrue(!rootAlbum.getSoundClips().contains(soundClip));
    }

    @Test
    public void testRemoveSubAlbum() {
        Music_Organizer organizer = new Music_Organizer();
        Album rootAlbum = organizer.getRootAlbum();
        Album subAlbum = new Album("Sub Album", rootAlbum);
        organizer.removeSubAlbum(subAlbum);
        assertTrue(!rootAlbum.hasSubAlbums());
    }
}
