import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

class BackgroundMusic {
    private Clip clip;
    private FloatControl volumeControl;

    public BackgroundMusic(String filePath) {
        try {
            File musicPath = new File(filePath);
            if (musicPath.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicPath);
                clip = AudioSystem.getClip();
                clip.open(audioInput);

                // Get the volume control object
                volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                setVolume(-20.0f);

                clip.start();
                clip.loop(Clip.LOOP_CONTINUOUSLY);
                System.out.println("Playing music: " + filePath); // Debugging information
            } else {
                System.out.println("File not found: " + filePath);
            }
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
            System.out.println("Unsupported audio file: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error reading file: " + filePath);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            System.out.println("Line unavailable: " + filePath);
        }
    }

    public void setVolume(float volume) {
        if (volumeControl != null) {
            volumeControl.setValue(volume);
        }
    }

}
