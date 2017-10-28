import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

class View extends JFrame {

    private final JButton playButton, removeSongButton, addSongButton, moveSongUpButton, moveSongDownButton,
            goToNextSongButton, goToPreviousSongButton, stopButton, volumeButton;
    private final JMenuItem addSongItem, removeSongItem, exitProgramItem, clearPlaylistItem, goToNextSongItem,
            goToPreviousSongItem, stopItem, increaseVolumeItem, lowerVolumeItem, muteItem, addCustomPlaylistItem;
    private final JMenu choosePlaylistMenu;
    private final ArrayList<JMenuItem> customPlaylists;
    private final JSlider musicSlider, volumeSlider;
    private final DefaultListModel<String> playlistModel;
    private final DefaultListModel<String> playlistModelBackup;
    private final JList<String> playlist;
    private final JCheckBox autoPlayBox, randomPlayBox;
    private final JLabel songLength, songTime, playlistName;
    private ImageIcon playIcon, pauseIcon, soundOffIcon, soundOnIcon, soundLessIcon, soundEvenLLessIcon;
    private int selectedPlaylistIndex;

    View() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        int width = 400;
        int height = 300;
        setSize(width, height);
        setLayout(null);
        setVisible(false);
        setResizable(false);
        setTitle("Music Player");
        selectedPlaylistIndex = 0;

        addSongButton = new JButton(loadIcon("images/add.png"));
        addSongButton.setBounds(width - 47, 40, 35, 35);
        addSongButton.setToolTipText("Add new songs to the playlist");
        addSongButton.setFocusable(false);
        add(addSongButton);

        removeSongButton = new JButton(loadIcon("images/remove.png"));
        removeSongButton.setBounds(width - 47, addSongButton.getY() + 35, 35, 35);
        removeSongButton.setToolTipText("Remove selected song from the playlist");
        removeSongButton.setFocusable(false);
        add(removeSongButton);

        moveSongUpButton = new JButton(loadIcon("images/up.png"));
        moveSongUpButton.setBounds(width - 47, removeSongButton.getY() + 35, 35, 35);
        moveSongUpButton.setToolTipText("Move selected song upwards on the playlist");
        moveSongUpButton.setFocusable(false);
        add(moveSongUpButton);

        moveSongDownButton = new JButton(loadIcon("images/down.png"));
        moveSongDownButton.setBounds(width - 47, moveSongUpButton.getY() + 35, 35, 35);
        moveSongDownButton.setToolTipText("Move selected song downwards on the playlist");
        moveSongDownButton.setFocusable(false);
        add(moveSongDownButton);

        stopButton = new JButton(loadIcon("images/stop.png"));
        stopButton.setBounds(40, height - 65, 35, 35);
        stopButton.setContentAreaFilled(false);
        stopButton.setBorderPainted(false);
        stopButton.setFocusable(false);
        add(stopButton);

        pauseIcon = loadIcon("images/pause.png");
        playIcon = loadIcon("images/play.png");

        playButton = new JButton(playIcon);
        playButton.setBounds(10, height - 100, 50, 50);
        playButton.setContentAreaFilled(false);
        playButton.setBorderPainted(false);
        playButton.setFocusable(false);
        add(playButton);

        musicSlider = new JSlider();
        musicSlider.setBounds(70, height - 80, width - 100, 25);
        musicSlider.setMinimum(0);
        musicSlider.setMaximum(10000);
        musicSlider.setFocusable(false);
        add(musicSlider);

        volumeSlider = new JSlider();
        volumeSlider.setBounds(90, height - 100, width / 4, 25);
        volumeSlider.setMinimum(0);
        volumeSlider.setMaximum(100);
        volumeSlider.setFocusable(false);
        add(volumeSlider);

        soundOnIcon = loadIcon("images/sound_on.png");
        soundOffIcon = loadIcon("images/sound_off.png");
        soundLessIcon = loadIcon("images/sound_less.png");
        soundEvenLLessIcon = loadIcon("images/sound_even_less.png");

        volumeButton = new JButton(soundOnIcon);
        volumeButton.setBounds(volumeSlider.getX() - 25, volumeSlider.getY() + 2, 20, 20);
        volumeButton.setContentAreaFilled(false);
        volumeButton.setBorderPainted(false);
        volumeButton.setFocusable(false);
        add(volumeButton);

        goToPreviousSongButton = new JButton(loadIcon("images/previous_song.png"));
        goToPreviousSongButton.setBounds(80, musicSlider.getY() + 20, 32, 32);
        goToPreviousSongButton.setContentAreaFilled(false);
        goToPreviousSongButton.setBorderPainted(false);
        goToPreviousSongButton.setFocusable(false);
        add(goToPreviousSongButton);

        goToNextSongButton = new JButton(loadIcon("images/next_song.png"));
        goToNextSongButton.setBounds(goToPreviousSongButton.getX() + 32, goToPreviousSongButton.getY(), 32, 32);
        goToNextSongButton.setContentAreaFilled(false);
        goToNextSongButton.setBorderPainted(false);
        goToNextSongButton.setFocusable(false);
        add(goToNextSongButton);

        playlistName = new JLabel("Default playlist:");
        playlistName.setBounds(22, 5, 300, 50);
        add(playlistName);

        playlistModel = new DefaultListModel<>();
        playlist = new JList<>(playlistModel);
        playlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        playlist.setLayoutOrientation(JList.VERTICAL);
        JScrollPane scrollPaneList = new JScrollPane(playlist);
        scrollPaneList.setBounds(20, 40, width - 70, height / 2);
        scrollPaneList.setVisible(true);
        add(scrollPaneList);

        playlistModelBackup = new DefaultListModel<>();

        JMenuBar menuBar = new JMenuBar();
        menuBar.setBounds(0, 0, width, 20);
        menuBar.setVisible(true);
        add(menuBar);

        JMenu playerMenu = new JMenu("Player");
        playerMenu.setVisible(true);
        menuBar.add(playerMenu);

        exitProgramItem = new JMenuItem("Exit");
        exitProgramItem.setAccelerator(KeyStroke.getKeyStroke("ctrl X"));
        playerMenu.add(exitProgramItem);

        JMenu playlistMenu = new JMenu("Playlist");
        menuBar.add(playlistMenu);

        addCustomPlaylistItem = new JMenuItem("Add custom playlist");
        playlistMenu.add(addCustomPlaylistItem);

        choosePlaylistMenu = new JMenu("Choose playlist");
        playlistMenu.add(choosePlaylistMenu);

        customPlaylists = new ArrayList<>();
        customPlaylists.add(new JMenuItem());
        customPlaylists.get(0).setText("Default playlist");
        choosePlaylistMenu.add(customPlaylists.get(0));

        addSongItem = new JMenuItem("Add new songs");
        addSongItem.setBounds(0, 0, 50, 20);
        addSongItem.setAccelerator(KeyStroke.getKeyStroke("ctrl A"));
        playlistMenu.add(addSongItem);

        removeSongItem = new JMenuItem("Remove selected song");
        removeSongItem.setAccelerator(KeyStroke.getKeyStroke("ctrl R"));
        playlistMenu.add(removeSongItem);

        clearPlaylistItem = new JMenuItem("Clear all playlists");
        playlistMenu.add(clearPlaylistItem);

        JMenu songControlMenu = new JMenu("Song control");
        menuBar.add(songControlMenu);

        goToPreviousSongItem = new JMenuItem("Play previous song");
        goToPreviousSongItem.setAccelerator(KeyStroke.getKeyStroke("Q"));
        songControlMenu.add(goToPreviousSongItem);

        goToNextSongItem = new JMenuItem("Play next song");
        goToNextSongItem.setAccelerator(KeyStroke.getKeyStroke("E"));
        songControlMenu.add(goToNextSongItem);

        muteItem = new JMenuItem("Mute");
        muteItem.setAccelerator(KeyStroke.getKeyStroke("M"));
        songControlMenu.add(muteItem);

        lowerVolumeItem = new JMenuItem("Lower volume");
        lowerVolumeItem.setAccelerator(KeyStroke.getKeyStroke("S"));
        songControlMenu.add(lowerVolumeItem);

        increaseVolumeItem = new JMenuItem("Increase volume");
        increaseVolumeItem.setAccelerator(KeyStroke.getKeyStroke("W"));
        songControlMenu.add(increaseVolumeItem);

        stopItem = new JMenuItem("Stop playing");
        stopItem.setAccelerator(KeyStroke.getKeyStroke("ctrl S"));
        songControlMenu.add(stopItem);

        autoPlayBox = new JCheckBox("Autoplay");
        autoPlayBox.setFocusable(false);
        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(autoPlayBox);

        randomPlayBox = new JCheckBox("Random play");
        randomPlayBox.setFocusable(false);
        randomPlayBox.setEnabled(false);
        menuBar.add(randomPlayBox);

        songLength = new JLabel();
        songLength.setVisible(true);
        songLength.setBounds(340, height - 60, 100, 10);
        add(songLength);

        songTime = new JLabel();
        songTime.setBounds(150, height - 60, 100, 10);
        add(songTime);
    }

    void setRandomPlayBoxEnabled(boolean arg) {
        randomPlayBox.setEnabled(arg);
    }

    void removeRandomPlayBoxSelection() {
        randomPlayBox.setSelected(false);
    }

    void clearCustomPlaylists() {
        selectedPlaylistIndex = 0;
        Preferences preferences = Preferences.userNodeForPackage(View.class);
        try {
            preferences.clear();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
        for (int i = customPlaylists.size() - 1; i > 0; i--)
            removeCustomPlaylist(i);
        updatePlaylistName();
    }


    String getSelectedPlaylistText() {
        return getSelectedCustomPlaylist().getText();
    }

    int getAmountOfCustomPlaylists() {
        return customPlaylists.size();
    }

    void exportCustomPlaylists() {
        Preferences preferences = Preferences.userNodeForPackage(View.class);
        for (int i = 1; i < customPlaylists.size(); i++) {
            preferences.put("customPlaylists" + i, customPlaylists.get(i).getText());
        }
        preferences.put("amountOfCustomPlaylists", String.valueOf(customPlaylists.size()));
    }

    void importCustomPlaylists(ActionListener actionListener) throws BackingStoreException {
        Preferences preferences = Preferences.userNodeForPackage(View.class);
        int size = Integer.parseInt(preferences.get("amountOfCustomPlaylists", "1"));
        for (int i = 1; i < size; i++) {
            String playlist = preferences.get("customPlaylists" + i, null);
            addCustomPlaylist(playlist, actionListener);
        }
    }

    void addCustomPlaylist(String text, ActionListener actionListener) {
        customPlaylists.add(new JMenuItem());
        int lastIndex = getLastPlaylistIndex();
        customPlaylists.get(lastIndex).setText(text);
        customPlaylists.get(lastIndex).addActionListener(actionListener);
        choosePlaylistMenu.add(customPlaylists.get(lastIndex));
    }

    private void removeCustomPlaylist(int index) {
        customPlaylists.remove(index);
        choosePlaylistMenu.remove(index);
    }

    void updatePlaylistName() {
        playlistName.setText(getSelectedPlaylistText() + ":");
    }

    void setSelectedPlaylistIndex(int index) {
        selectedPlaylistIndex = index;
    }

    private JMenuItem getSelectedCustomPlaylist() {
        return customPlaylists.get(selectedPlaylistIndex);
    }

    int getSelectedPlaylistIndex() {
        return selectedPlaylistIndex;
    }

    private int getLastPlaylistIndex() {
        return customPlaylists.size() - 1;
    }

    private void addNumbersToPlaylist() {
        String text;
        for (int i = 0; i < playlistModelBackup.size(); i++) {
            text = String.format("%02d", (i + 1)) + " - " + playlistModelBackup.get(i);
            playlistModel.set(i, text);
        }
    }

    void clearSongsInCurrentPlaylist() {
        playlistModel.clear();
        playlistModelBackup.clear();
    }

    int getSelectedSongIndex() {
        return playlist.getSelectedIndex();
    }

    private ImageIcon loadIcon(String path) {
        BufferedImage icon = null;
        try
        {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            InputStream input = classLoader.getResourceAsStream(path);
            icon = ImageIO.read(input);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        assert icon != null;
        return new ImageIcon(icon);
    }

    int getPlaylistLastIndex() {
        return playlistModel.size() - 1;
    }

    int getPlaylistFirstIndex() {
        return 0;
    }

    void selectPlaylistIndex(int index) {
        playlist.setSelectedIndex(index);
    }

    boolean isAutoPlayEnabled() {
        return autoPlayBox.isSelected();
    }

    boolean isRandomPlayBoxSelected() {
        return randomPlayBox.isSelected();
    }

    boolean isPlaylistSelectionEmpty() {
        return playlist.isSelectionEmpty();
    }

    boolean playlistContainsMoreThanOneSong() {
        return getPlaylistLastIndex() > getPlaylistFirstIndex();
    }

    boolean playlistContainsExaclyOneSong() {
        return getPlaylistFirstIndex() == getPlaylistLastIndex();
    }

    void setPlayButtonIcon() {
        playButton.setIcon(playIcon);
    }

    void setPauseButtonIcon() {
        playButton.setIcon(pauseIcon);
    }

    void setMuteButtonIcon() {
        volumeButton.setIcon(soundOffIcon);
    }

    void setVolumeButtonIcon() {
        volumeButton.setIcon(soundOnIcon);
    }

    void setLessSoundIcon() {
        volumeButton.setIcon(soundLessIcon);
    }

    void setEvenLessSoundIcon() {
        volumeButton.setIcon(soundEvenLLessIcon);
    }

    void addToPlaylist(String fileName) {
        playlistModel.addElement(fileName);
        playlistModelBackup.addElement(fileName);
        addNumbersToPlaylist();
    }

    void addToPlaylistAndSelect(String fileName) {
        addToPlaylist(fileName);
        playlist.setSelectedIndex(getPlaylistLastIndex());
    }

    void removeFromPlaylist(int index) {
        playlistModel.remove(index);
        playlistModelBackup.remove(index);
        addNumbersToPlaylist();
    }

    void swapElements(int a, int b) {
        String line1 = playlistModel.get(a);
        String line2 = playlistModel.get(b);
        String line1Backup = playlistModelBackup.get(a);
        String line2Backup = playlistModelBackup.get(b);
        playlistModel.set(a, line2);
        playlistModel.set(b, line1);
        playlistModelBackup.set(a, line2Backup);
        playlistModelBackup.set(b, line1Backup);
        playlist.setSelectedIndex(a);
        playlist.updateUI();
        addNumbersToPlaylist();

    }

    void addMusicSliderMouseListener(MouseListener mouseListener) {
        musicSlider.addMouseListener(mouseListener);
    }

    void addListSelectionListener(ListSelectionListener listSelectionListener) {
        playlist.addListSelectionListener(listSelectionListener);
    }

    void addActionListeners(ActionListener actionListener) {
        removeSongButton.addActionListener(actionListener);
        addSongItem.addActionListener(actionListener);
        addSongButton.addActionListener(actionListener);
        moveSongUpButton.addActionListener(actionListener);
        moveSongDownButton.addActionListener(actionListener);
        playButton.addActionListener(actionListener);
        exitProgramItem.addActionListener(actionListener);
        clearPlaylistItem.addActionListener(actionListener);
        removeSongItem.addActionListener(actionListener);
        goToNextSongButton.addActionListener(actionListener);
        goToPreviousSongButton.addActionListener(actionListener);
        stopButton.addActionListener(actionListener);
        volumeButton.addActionListener(actionListener);
        goToNextSongItem.addActionListener(actionListener);
        goToPreviousSongItem.addActionListener(actionListener);
        stopItem.addActionListener(actionListener);
        increaseVolumeItem.addActionListener(actionListener);
        lowerVolumeItem.addActionListener(actionListener);
        muteItem.addActionListener(actionListener);
        addCustomPlaylistItem.addActionListener(actionListener);
        customPlaylists.get(0).addActionListener(actionListener);
        autoPlayBox.addActionListener(actionListener);
    }

    void addChangeListeners(ChangeListener changeListener) {
        musicSlider.addChangeListener(changeListener);
        volumeSlider.addChangeListener(changeListener);
    }

    void setSongLengthLabel(String text) {
        songLength.setText(text);
    }

    void setSongTimeLabel(String text) {
        songTime.setText(text);
    }

    JMenuItem getAddSongItem() {
        return addSongItem;
    }

    JMenuItem getExitProgramItem() {
        return exitProgramItem;
    }

    JMenuItem getRemoveSongItem() {
        return removeSongItem;
    }

    JList<String> getPlaylist() {
        return playlist;
    }

    JButton getRemoveSongButton() { return removeSongButton; }

    JButton getAddSongButton() { return addSongButton; }

    JButton getPlayButton() {
        return playButton;
    }

    JButton getMoveSongUpButton() { return moveSongUpButton; }

    JButton getMoveSongDownButton() { return moveSongDownButton; }

    JButton getGoToNextSongButton() {
        return goToNextSongButton;
    }

    JButton getGoToPreviousSongButton() {
        return goToPreviousSongButton;
    }

    JButton getStopButton() {
        return stopButton;
    }

    JButton getVolumeButton() {
        return volumeButton;
    }

    JMenuItem getGoToNextSongItem() {
        return goToNextSongItem;
    }

    JMenuItem getGoToPreviousSongItem() {
        return goToPreviousSongItem;
    }

    JMenuItem getStopItem() {
        return stopItem;
    }

    JMenuItem getIncreaseVolumeItem() {
        return increaseVolumeItem;
    }

    JMenuItem getLowerVolumeItem() {
        return lowerVolumeItem;
    }

    JMenuItem getMuteItem() {
        return muteItem;
    }

    JMenuItem getClearPlaylistItem() {
        return clearPlaylistItem;
    }

    JMenuItem getAddCustomPlaylistItem() {
        return addCustomPlaylistItem;
    }

    JMenuItem getCustomPlaylist(int index) {
        return customPlaylists.get(index);
    }

    JCheckBox getAutoPlayBox() {
        return autoPlayBox;
    }

    JSlider getMusicSlider() {
        return musicSlider;
    }

    JSlider getVolumeSlider() {
        return volumeSlider;
    }

}