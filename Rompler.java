import javax.sound.midi.*;
import javax.sound.sampled.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.io.*;

public class Rompler
{
	public File sample;
	public FileInputStream fis;
	public byte[] bytearray;
	public byte[] basearray;
	public AudioInputStream stream;
	public AudioInputStream[] streams;
	public ByteArrayInputStream bytestream;
	public Clip[] clips;
	public Transmitter trans;
	private MidiInputReceiver temp;
	int fis_count = 0;
	int fis_reader;
	
	SourceDataLine new_line;
	public Rompler()
	{
		MidiDevice device;
		MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
		
		try
		{
			sample = new File("sounds/wow3.wav");
			fis = new FileInputStream(sample);
			stream = AudioSystem.getAudioInputStream(sample);
			AudioFormat orig_format = stream.getFormat();
			DataLine.Info dinfo = new DataLine.Info(SourceDataLine.class, orig_format);			
			streams = new AudioInputStream[25];
			clips = new Clip[25];
			// AudioInputStream <- TargetDataLine.open <- AudioFormat(float sampleRate, int sampleSizeInBits, int channels, boolean signed, boolean bigEndian)
			// Relative frequencies of notes in a 2 Octave range (25 total semitones)
			// Assuming freqs[13] is the base frequency, multiply by these values to get the frequency of notes relative to the base frequency
			float[] freqs = {(float) 0.5, (float) 0.5295, (float) 0.561, (float) 0.5945, (float) 0.63, (float) 0.6675, (float) 0.707, (float) 0.749, (float) 0.7935, (float) 0.841, (float) 0.891, (float) 0.944, (float) 1.0, (float) 1.0595, (float) 1.225, (float) 1.189, (float) 1.26, (float) 1.335, (float) 1.414, (float) 1.4985, (float) 1.5875, (float) 1.667, (float) 1.782, (float) 1.8875, (float) 2.0};
			basearray = new byte[fis.available()];
			fis.read(basearray);
			
			for (int i = 0; i < 25; i++)
			{
				float floattemp = (float) basearray.length;
				int inttemp = (int) (floattemp / freqs[i]);
				//System.out.println(inttemp); // Use to debug NullPointerExceptions caused by exceeding the array's length
			}
			for (int i = 0; i < 25; i++)
			{
				int arraylen = (int) ((float) basearray.length / freqs[i]);
				bytearray = new byte[arraylen];
				Arrays.fill(bytearray, (byte) 0);
				for (int j = 0; j < basearray.length; j++)
				{
					//bytearray[(int) ((float) j / (float) freqs[i])] = 0;
					if ((int) ((float) j / (float) freqs[i]) < bytearray.length)
					{
						bytearray[(int) ((float) j / (float) freqs[i])] = basearray[j];
					}
					else { break; } // Will lose a few samples on the end in the worst-case scenario
				}
				//ad_streams[i] = new AudioDataStream(new AudioData(bytearray));
				bytestream = new ByteArrayInputStream(bytearray);
				streams[i] = new AudioInputStream(bytestream, orig_format, (long) bytearray.length);
				clips[i] = (Clip) AudioSystem.getLine(new DataLine.Info(Clip.class, orig_format));
				clips[i].open(streams[i]);
			}
			System.out.println("All shifts completed");
		}
		catch (Exception e) { System.out.println(e.toString()); }
		
		for (int i = 0; i < infos.length; i++)
		{
			try
			{
				device = MidiSystem.getMidiDevice(infos[i]);
				List<Transmitter> transmitters = device.getTransmitters();
				for (int j = 0; j < transmitters.size(); j++)
				{
					transmitters.get(j).setReceiver(new MidiInputReceiver(device.getDeviceInfo().toString()));
				}
				temp = new MidiInputReceiver(device.getDeviceInfo().toString());
				temp.setClipArray(clips);
				trans = device.getTransmitter();
				trans.setReceiver(temp);
				device.open();
				System.out.println("MIDI Device Initialized: " + device.getDeviceInfo() + " was opened");
			}
			catch (MidiUnavailableException e) { /* Do nothing because we are iterating through all options until we find one input and one output that work */ }
		}
		System.out.println("\nOnly works with C3 through C5 given the current sample");
	}
	
	public static void main(String[] args) { Rompler handler = new Rompler(); }
}
