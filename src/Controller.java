import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class Controller implements ActionListener, ChangeListener, ListSelectionListener, MouseListener, WindowListener {
    private final Model model;
    private final View view;
    private final Timer timer;
    private boolean playAtNextMouseRelease, swappingSongs, addingSongs;
    private int previousVolume;

    Controller(View view, Model model) {
        this.view = view;
        this.model = model;
        playAtNextMouseRelease = false;
        swappingSongs = false;
        addingSongs = false;
        previousVolume = 0;
        timer = new Timer(100, this);

        view.addMusicSliderMouseListener(this);
        view.addListSelectionListener(this);
        view.addWindowListener(this);
        view.addActionListeners(this);
        view.addChangeListeners(this);

        try {
            view.importCustomPlaylists(this);
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }

        importSongs(view.getSelectedPlaylistText());

        view.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object z = e.getSource();
        if (z == view.getAddSongItem() || z == view.getAddSongButton())
            addNewSongs();
        if (z == view.getPlayButton() && model.isFilePrepared() && !view.isPlaylistSelectionEmpty())
            pauseOrPlay();
        if ((z == view.getStopButton() || z == view.getStopItem()) && model.isFilePrepared() && !view.isPlaylistSelectionEmpty())
            stopMusic();
        if ((z == view.getRemoveSongButton() || z == view.getRemoveSongItem()) && !view.isPlaylistSelectionEmpty())
            removeSelectedSongFromPlaylist();
        if (z == view.getMoveSongUpButton() && view.getSelectedSongIndex() != view.getPlaylistFirstIndex() && !view.isPlaylistSelectionEmpty())
            swapSelectedSongWithIndex(-1);
        if (z == view.getMoveSongDownButton() && view.getSelectedSongIndex() != view.getPlaylistLastIndex() && !view.isPlaylistSelectionEmpty())
            swapSelectedSongWithIndex(1);
        if (z == view.getGoToNextSongButton() || z == view.getGoToNextSongItem())
            switchToNextSong();
        if (z == view.getGoToPreviousSongButton() || z == view.getGoToPreviousSongItem())
            switchToPreviousSong();
        if (z == view.getExitProgramItem())
            exitPlayer();
        if (z == view.getClearPlaylistItem())
            clearAllPlaylists();
        if (z == view.getVolumeButton() || z == view.getMuteItem())
            muteOrUnmute();
        if (z == view.getIncreaseVolumeItem())
            changeVolumeBy(20);
        if (z == view.getLowerVolumeItem())
            changeVolumeBy(-20);
        if (z == view.getAddCustomPlaylistItem())
            addCustomPlaylist();
        if (z == view.getAutoPlayBox())
            switchRandomPlayBoxState();
        if (z == timer)
            progressThroughSong();
        selectCustomPlaylistAtClick(z);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        Object z = e.getSource();
        if (z == view.getMusicSlider() && model.isFilePrepared() && !timer.isRunning() && !view.isPlaylistSelectionEmpty()) {
            convertSliderPositionIntoMusicTime();
        }
        if (z == view.getVolumeSlider()) {
            switchVolumeIcon();
            updateVolumeFromSlider();
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        Object z = e.getSource();
        if (z == view.getPlaylist()) {
            removeNotExistingSong();
            if ((!swappingSongs && !addingSongs) || view.playlistContainsExaclyOneSong())
                songStateRestart();
            swappingSongs = false;
            addingSongs = false;
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        Object z = e.getSource();
        if (z == view.getMusicSlider() && model.isFilePrepared() && timer.isRunning()) {
            pauseMusic();
            convertSliderPositionIntoMusicTime();
            playMusic();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Object z = e.getSource();
        if (z == view.getMusicSlider() && model.isFilePrepared() && timer.isRunning()) {
            pauseMusic();
            playAtNextMouseRelease = true;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        Object z = e.getSource();
        if (z == view.getMusicSlider() && model.isFilePrepared() && playAtNextMouseRelease) {
            convertSliderPositionIntoMusicTime();
            playMusic();
            playAtNextMouseRelease = false;
        }
    }

    @Override
    public void windowClosing(WindowEvent e) {
        String text = view.getSelectedPlaylistText();
        model.exportPlaylistPaths(text);
        view.exportCustomPlaylists();
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosed(WindowEvent e) {

    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }

    private void progressThroughSong() {
        convertMusicTimeIntoSliderPosition();
        proceedWhenSongFinished();
    }

    private void addNewSongs() {
        JFileChooser menuChooser = new JFileChooser();
        menuChooser.setMultiSelectionEnabled(true);
        menuChooser.setFileFilter(new FileNameExtensionFilter("MP3, MP4, WAV", "MP3", "WAV", "MP4"));
        if (menuChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            addingSongs = true;
            addSongsToPlaylist(menuChooser.getSelectedFiles());
        }
    }

    private void addCustomPlaylist() {
        String text = JOptionPane.showInputDialog(view,
                "Type the name of the new playlist",
                "Add custom playlist",
                JOptionPane.PLAIN_MESSAGE);
        if (text != null && !text.isEmpty())
            view.addCustomPlaylist(text, this);
    }

    private void exitPlayer() {
        stopMusic();
        model.dispose();
        view.dispose();
        System.exit(0);
    }

    private void pauseOrPlay() {
        if (timer.isRunning())
            pauseMusic();
        else
            playMusic();
    }

    private void muteOrUnmute() {
        if (view.getVolumeSlider().getValue() == 0)
            restoreSound();
        else
            mute();
    }

    private void playRandomSong() {
        stopMusic();
        Random rand = new Random();
        int bond = view.getPlaylistLastIndex() + 1;
        int songIndex;
        do {
            songIndex = rand.nextInt(bond);
        } while (songIndex == view.getSelectedSongIndex());
        model.prepareToPlay(songIndex);
        view.selectPlaylistIndex(songIndex);
    }

    private void importSongs(String playlistText) {
        Preferences pref = Preferences.userNodeForPackage(Model.class);
        int playlistSize = Integer.parseInt(pref.get(playlistText + "Size", "0"));
        if (playlistSize > 0) {
            for (int i = 0; i < playlistSize; i++)
                addSongToPlaylist(pref.get(playlistText + i, null));
            view.selectPlaylistIndex(playlistSize - 1);
        }
    }

    private void switchRandomPlayBoxState() {
        if (view.isAutoPlayEnabled())
            view.setRandomPlayBoxEnabled(true);
        else {
            view.setRandomPlayBoxEnabled(false);
            view.removeRandomPlayBoxSelection();
        }
    }

    private void selectCustomPlaylist(int index) {
        stopMusic();
        String text = view.getSelectedPlaylistText();
        model.exportPlaylistPaths(text);
        clearCurrentPlaylist();
        view.setSelectedPlaylistIndex(index);
        text = view.getSelectedPlaylistText();
        importSongs(text);
        view.updatePlaylistName();
    }

    private void clearCurrentPlaylist() {
        view.clearSongsInCurrentPlaylist();
        model.clearPlaylistPaths();
    }

    private void clearAllPlaylists() {
        view.clearCustomPlaylists();
        view.clearSongsInCurrentPlaylist();
        model.clearPlaylistPaths();
        stopMusic();
    }

    private void selectCustomPlaylistAtClick(Object z) {
        int size = view.getAmountOfCustomPlaylists();
        for (int i = 0; i < size; i++) {
            if (z == view.getCustomPlaylist(i) && i != view.getSelectedPlaylistIndex()) {
                selectCustomPlaylist(i);
            }
        }
    }

    private void proceedWhenSongFinished() {
        if (model.isSongFinished()) {
            pauseMusic();
            if (view.isAutoPlayEnabled()) {
                if (view.isRandomPlayBoxSelected())
                    playRandomSong();
                else
                    switchToNextSong();
            }
            else {
                model.prepareToPlay(view.getSelectedSongIndex());
                updateVolumeFromSlider();
            }
        }
    }

    private void clearLabels() {
        view.setSongLengthLabel("");
        view.setSongTimeLabel("");
    }

    private void changeVolumeBy(int difference) {
        int previousValue = view.getVolumeSlider().getValue();
        view.getVolumeSlider().setValue(previousValue + difference);
    }

    private void mute() {
        previousVolume = view.getVolumeSlider().getValue();
        view.getVolumeSlider().setValue(0);
    }

    private void restoreSound() {
        if (previousVolume != 0) {
            view.getVolumeSlider().setValue(previousVolume);
            previousVolume = 0;
        }
    }

    private void swapSelectedSongWithIndex(int difference) {
        swappingSongs = true;
        int selectedIndex = view.getSelectedSongIndex();
        int goalIndex = view.getSelectedSongIndex() + difference;
        model.swapPathsList(goalIndex, selectedIndex);
        view.swapElements(goalIndex, selectedIndex);
    }

    private void removeSelectedSongFromPlaylist() {
        int index = view.getSelectedSongIndex();
        removeFromPlaylist(index);
    }

    private void removeFromPlaylist(int index) {
        model.removeFromPlaylistPaths(index);
        view.removeFromPlaylist(index);
        if (model.pathsListContains(index))
            view.getPlaylist().setSelectedIndex(index);
        else
            view.getPlaylist().setSelectedIndex(index-1);
        if (view.isPlaylistSelectionEmpty())
            stopMusic();
    }

    private void addSongsToPlaylist(File[] songs) {
        for (File song : songs)
            addSongToPlaylist(song.getAbsolutePath());
    }

    private void addSongToPlaylist(String absolutePath) {
        Path path = Paths.get(absolutePath);
        if (Files.exists(path)) {
            model.addToPlaylistPaths(absolutePath);
            String fileName = path.getFileName().toString();
            addFileNameToView(fileName);
        }
    }

    private void addFileNameToView(String fileName) {
        if (!addingSongs || view.isPlaylistSelectionEmpty()) {
            view.addToPlaylistAndSelect(fileName);
            view.getMusicSlider().setValue(0);
        }
        else
            view.addToPlaylist(fileName);
    }

    private void switchToNextSong() {
        if (view.playlistContainsMoreThanOneSong()) {
            stopMusic();
            int songIndex;
            if (view.getSelectedSongIndex() == view.getPlaylistLastIndex())
                songIndex = view.getPlaylistFirstIndex();
            else
                songIndex = view.getSelectedSongIndex() + 1;
            model.prepareToPlay(songIndex);
            view.selectPlaylistIndex(songIndex);
        }
        else if (view.playlistContainsExaclyOneSong())
            replayMusic();
    }

    private void switchToPreviousSong() {
        if (!view.isPlaylistSelectionEmpty()) {
            stopMusic();
            int songIndex;
            if (view.getSelectedSongIndex() == view.getPlaylistFirstIndex())
                songIndex = view.getPlaylistLastIndex();
            else
                songIndex = view.getSelectedSongIndex() - 1;
            view.selectPlaylistIndex(songIndex);
        }
    }

    private void pauseMusic() {
        if (timer.isRunning()) {
            timer.stop();
            model.pause();
            view.setPlayButtonIcon();
            view.setTitle("Music Player by nickt");
        }
    }

    private void playMusic() {
        if (!timer.isRunning()) {
            timer.start();
            model.play();
            view.setPauseButtonIcon();
            view.setTitle("Music Player by nickt - now playing " + view.getPlaylist().getSelectedValue());
        }
    }

    private void stopMusic() {
        pauseMusic();
        model.stop();
        view.getMusicSlider().setValue(0);
        view.setSongTimeLabel("0:00");
        if (view.isPlaylistSelectionEmpty()) {
            clearLabels();
            model.dispose();
        }
    }

    private void replayMusic() {
        stopMusic();
        playMusic();
        updateVolumeFromSlider();
    }

    private void convertSliderPositionIntoMusicTime() {
        model.setMusicStartTime((double) view.getMusicSlider().getValue() / (view.getMusicSlider().getMaximum()));
        view.setSongTimeLabel(model.getMusicStartTime());
    }

    private void convertMusicTimeIntoSliderPosition() {
        view.getMusicSlider().setValue((int) (model.getCurrentMusicPosition() * view.getMusicSlider().getMaximum()));
        view.setSongTimeLabel(model.getMusicTime());
    }

    private void switchVolumeIcon() {
        int volume = view.getVolumeSlider().getValue();
        if (volume == 0)
            view.setMuteButtonIcon();
        else if (volume > 55)
            view.setVolumeButtonIcon();
        else if (volume > 33)
            view.setLessSoundIcon();
        else
            view.setEvenLessSoundIcon();
    }

    private void updateVolumeFromSlider() {
        if (model.isFilePrepared())
            model.setVolume((double) view.getVolumeSlider().getValue() / 100);
    }

    private void removeNotExistingSong() {
        if (!view.isPlaylistSelectionEmpty()) {
            int selectedSongIndex = view.getSelectedSongIndex();
            String selectedSongPath = model.getSongPathForIndex(selectedSongIndex);
            if (!model.songExists(selectedSongPath))
                removeFromPlaylist(selectedSongIndex);
        }
    }

    private void songStateRestart() {
        pauseMusic();
        clearLabels();
        if (!view.isPlaylistSelectionEmpty()) {
            int songIndex = view.getSelectedSongIndex();
            view.getMusicSlider().setValue(0);
            model.prepareToPlay(songIndex);
            updateVolumeFromSlider();
            view.setSongLengthLabel(model.getMusicLength());
            view.setSongTimeLabel("0:00");
            if (view.isAutoPlayEnabled())
                replayMusic();
        }
    }
}