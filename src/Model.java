import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.prefs.Preferences;

import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;


class Model {
    private final JFXPanel fxPanel = new JFXPanel();
    private MediaPlayer mediaPlayer;
    private int lastSongIndex;
    private boolean fileIsPrepared;
    private ArrayList<String> playlistPaths = new ArrayList<>();

    void prepareToPlay(int songIndex) {
        lastSongIndex = songIndex;
        String path = playlistPaths.get(songIndex);
        Media hit = new Media(new File(path).toURI().toString());
        mediaPlayer = new MediaPlayer(hit);
        fileIsPrepared = true;
    }

    void clearPlaylistPaths() {
        playlistPaths.clear();
    }

    void exportPlaylistPaths(String playlistText) {
        Preferences pref = Preferences.userNodeForPackage(Model.class);
        for (int i = 0; i < playlistPaths.size(); i++)
            pref.put(playlistText + i, playlistPaths.get(i));
        pref.put(playlistText + "Size", String.valueOf(playlistPaths.size()));
    }

    void dispose() {
        if (fileIsPrepared) {
            pause();
            mediaPlayer.dispose();
        }
    }

    boolean songExists(String absolutePath) {
        Path path = Paths.get(absolutePath);
        return Files.exists(path);
    }

    String getSongPathForIndex(int index) {
        return playlistPaths.get(index);
    }

    void swapPathsList(int a, int b) {
        Collections.swap(playlistPaths, a, b);
    }

    boolean pathsListContains(int index) {
        return index < playlistPaths.size();
    }

    void addToPlaylistPaths(String absolutePath) {
        playlistPaths.add(absolutePath);
    }

    void removeFromPlaylistPaths(int index) {
        playlistPaths.remove(index);
    }

    void setVolume(double value) {
        mediaPlayer.setVolume(value);
    }

    boolean isSongFinished() {
        return (mediaPlayer.getCurrentTime().toMillis() >= mediaPlayer.getStopTime().toMillis());
    }

    void pause() {
        if (mediaPlayer != null)
            mediaPlayer.pause();
    }

    void stop() {
        if (mediaPlayer != null)
            mediaPlayer.stop();
    }

    void play() {
        if (isSongFinished())
            prepareToPlay(lastSongIndex);
        mediaPlayer.play();
    }

    String getMusicLength() {
        while (mediaPlayer.getStopTime() == Duration.UNKNOWN) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        int minutes = (int) mediaPlayer.getStopTime().toMinutes();
        int seconds = (int) (mediaPlayer.getStopTime().toSeconds() % 60);
        return String.format("%d:%02d%n", minutes, seconds);

    }

    String getMusicTime() {
        int minutes = (int) mediaPlayer.getCurrentTime().toMinutes();
        int seconds = (int) (mediaPlayer.getCurrentTime().toSeconds() % 60);
        return String.format("%d:%02d%n", minutes, seconds);
    }

    String getMusicStartTime() {
        int minutes = (int) mediaPlayer.getStartTime().toMinutes();
        int seconds = (int) (mediaPlayer.getStartTime().toSeconds() % 60);
        return String.format("%d:%02d%n", minutes, seconds);
    }

    void setMusicStartTime(double time) {
        mediaPlayer.setStartTime(Duration.seconds(time * mediaPlayer.getStopTime().toSeconds()));
    }

    boolean isFilePrepared() {
        return fileIsPrepared;
    }

    double getCurrentMusicPosition() {
        return mediaPlayer.getCurrentTime().toMillis() / mediaPlayer.getStopTime().toMillis();
    }

}
