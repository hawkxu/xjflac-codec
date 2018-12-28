package win.zqxu.xjflac.sound.spi;

/**
 * libFLAC - Free Lossless Audio Codec library
 * Copyright (C) 2001,2002,2003  Josh Coalson
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 */

import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import win.zqxu.xjflac.FLACDecoder;
import win.zqxu.xjflac.PCMProcessor;
import win.zqxu.xjflac.frame.Frame;
import win.zqxu.xjflac.metadata.Metadata;
import win.zqxu.xjflac.metadata.StreamInfo;
import win.zqxu.xjflac.util.ByteData;

/**
 * Converts an Flac bitstream into a PCM 16bits/sample audio stream.
 * 
 * @author Marc Gimpel, Wimba S.A. (marc@wimba.com)
 * @author Florian Bomers
 * @version $Revision: 1.6 $
 */
public class Flac2PcmAudioInputStream extends RingedAudioInputStream implements
    PCMProcessor {

  /** Flac Decoder. */
  private FLACDecoder decoder;

  private ByteData pcmData;

  /** StreamInfo MetaData. */
  private StreamInfo streamInfo;

  /** the meta data from the stream */
  private Metadata[] metaData;

  /**
   * Constructor.
   * 
   * @param in
   *          the underlying input stream.
   * @param format
   *          the target format of this stream's audio data.
   * @param length
   *          the length in sample frames of the data in this stream.
   */
  public Flac2PcmAudioInputStream(InputStream in, AudioFormat format,
      long length) {
    this(in, format, length, DEFAULT_BUFFER_SIZE);
  }

  /**
   * Constructor.
   * 
   * @param in
   *          the underlying input stream.
   * @param format
   *          the target format of this stream's audio data.
   * @param length
   *          the length in sample frames of the data in this stream.
   * @param size
   *          the buffer size.
   */
  public Flac2PcmAudioInputStream(InputStream in, AudioFormat format,
      long length, int size) {
    super(in, format, length, size);
  }

  /**
   * called from the super class whenever more PCM data is needed.
   */
  protected void fill() throws IOException {
    if (decoder == null) {
      initDecoder();
    }
    if (decoder.isEOF()) {
      buffer.setEOF(true);
    } else {
      Frame frame = decoder.readNextFrame();
      if (frame != null) {
        pcmData = decoder.decodeFrame(frame, pcmData);
        processPCM(pcmData);
      }
    }
  }

  /**
   * Initialize the Flac Decoder after reading the Header.
   * 
   * @exception IOException
   */
  protected void initDecoder() throws IOException {
    decoder = new FLACDecoder(in);
    decoder.addPCMProcessor(this);
    metaData = decoder.readMetadata();
  }

  /**
   * Process the StreamInfo block.
   * 
   * @param streamInfo
   *          the StreamInfo block
   * @see win.zqxu.xjflac.PCMProcessor#processStreamInfo(win.zqxu.xjflac.metadata.StreamInfo)
   */
  public void processStreamInfo(StreamInfo streamInfo) {
    this.streamInfo = streamInfo;
  }

  /**
   * Process the decoded PCM bytes. This is called synchronously from the fill()
   * method.
   * 
   * @param pcm
   *          The decoded PCM data
   * @see win.zqxu.xjflac.PCMProcessor#processPCM(ByteData)
   */
  public void processPCM(ByteData pcm) {
    buffer.resize(pcm.getLen() * 2);
    buffer.put(pcm.getData(), 0, pcm.getLen());
  }

  /**
   * @return the streamInfo
   */
  public StreamInfo getStreamInfo() {
    return streamInfo;
  }

  /**
   * @return the metaData
   */
  public Metadata[] getMetaData() {
    return metaData;
  }

  @Override
  public long getFrameLength() {
    return ((AudioInputStream) in).getFrameLength();
  }
}
