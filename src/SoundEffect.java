import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class SoundEffect {
    private Clip clip;

    public SoundEffect(String filePath) {
        try {
            File soundFile = new File(filePath);
            if (soundFile.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(soundFile);
                clip = AudioSystem.getClip();
                clip.open(audioInput);
            } else {
                System.out.println("Sound file not found: " + filePath);
            }
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public void play() {
        if (clip != null) {
            if (clip.isRunning()) {
                clip.stop(); // Stop the clip if it's already playing
            }
            clip.setFramePosition(0); // Rewind to the beginning
            clip.start();
        }
    }
}
