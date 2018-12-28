# xjflac-codec
Just a bit change from JFLAC library 1.5.2

This project was copied from a forked jflac project at <https://github.com/nguillaumin/jflac>, the original project site was <http://jflac.sourceforge.net/>

I need the jflac work together with java sound api, but the original project seems not be updated for a long time, so I copied this project.

I copied this project just for my own purposes, but I had no knowledge about FLAC codec nor java sound api, so this project is most likely not updated, please don't depends on this project, thanks!

# changes
1.  change package path to win.zqxu.xjflac
2.  all source code file re-formatted
3.  change compile warning code, using Java 8 generic style
4.  add frame length support to get music duration

# sample code
```
  public static void main(String[] args) throws Exception {
    File file = new File("test.flac");
    // determine music duration using AudioFileFormat
    AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(file);
    outputDuration("duration from AudioFileFormat: ",
        fileFormat.getFormat(), fileFormat.getFrameLength());
    try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(file)) {
      // determine music duration using original AudioFileFormat
      outputDuration("duration from original AudioFormat: ",
          audioStream.getFormat(), audioStream.getFrameLength());
      int channels = audioStream.getFormat().getChannels();
      float rate = audioStream.getFormat().getSampleRate();
      AudioFormat playFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
          rate, 16, channels, channels * 2, rate, false);
      DataLine.Info lineInfo = new DataLine.Info(SourceDataLine.class, playFormat);
      SourceDataLine audioLine = (SourceDataLine) AudioSystem.getLine(lineInfo);
      AudioInputStream playStream = AudioSystem.getAudioInputStream(playFormat, audioStream);
      // determine music duration using converted AudioFileFormat
      outputDuration("duration from converted AudioFormat: ",
          playStream.getFormat(), playStream.getFrameLength());
      // skip thirty seconds
      byte[] buffer = new byte[65536];
      float bytesPerSecond = playFormat.getFrameSize() * playFormat.getFrameRate();
      int bytesRead = 0, bytesToSkip = (int) (bytesPerSecond * 30);
      while (bytesToSkip > 0 && bytesRead != -1) {
        bytesToSkip -= bytesRead = (int) playStream.read(buffer);
      }
      // Start play
      audioLine.open();
      audioLine.start();
      while ((bytesRead = playStream.read(buffer)) != -1) {
        audioLine.write(buffer, 0, bytesRead);
      }
      // Waiting for play to complete
      audioLine.drain();
      audioLine.close();
    }
  }

  private static void outputDuration(String message,
      AudioFormat format, long frameLength) {
    float bytesPerSecond = format.getFrameSize() * format.getFrameRate();
    System.out.println(message + (int) (frameLength / bytesPerSecond));
  }
```