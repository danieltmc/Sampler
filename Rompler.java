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
	public int nyquist = 44100; // Remove?
	public int filter_freq; // Remove?
	public float sqrttwo = (float) Math.sqrt(2); // Remove?
	// Relative frequencies of notes in a 2 Octave range (25 total semitones)
	// Assuming freqs[13] is the base frequency, multiply by these values to get the frequency of notes relative to the base frequency
	float[] freqs = {(float) 0.5, (float) 0.5295, (float) 0.561, (float) 0.5945, (float) 0.63, (float) 0.6675, (float) 0.707, (float) 0.749, (float) 0.7935, (float) 0.841, (float) 0.891, (float) 0.944, (float) 1.0, (float) 1.0595, (float) 1.225, (float) 1.189, (float) 1.26, (float) 1.335, (float) 1.414, (float) 1.4985, (float) 1.5875, (float) 1.667, (float) 1.782, (float) 1.8875, (float) 4.0};
	// C3 = 1:2, Db3 = 8:15, D3 = 5:9, Eb3 = 3:5, E3 = 5:8, F3 = 2:3, Gb3 = 32:45, G3 = 3:4, Ab3 = 4:5, A3 = 5:6, Bb3 = 8:9, B3 = 15:16
	// C4 = 1:1, Db4 = 16:15, D4 = 9:8, Eb4 = 6:5, E4 = 5:4, F4 = 4:3, Gb4 = 45:32, G4 = 3:2, Ab4 = 8:5, A4 = 5:3, Bb4 = 9:5, B4 = 15:8, C5 = 2:1
	float[] upshifts = {(float) 1.0, (float) 8.0, (float) 5.0, (float) 3.0, (float) 5.0, (float) 2.0, (float) 32.0, (float) 3.0, (float) 4.0, (float) 5.0, (float) 8.0, (float) 15.0, (float) 1.0, (float) 16.0, (float) 9.0, (float) 6.0, (float) 5.0, (float) 4.0, (float) 45.0, (float) 3.0, (float) 8.0, (float) 5.0, (float) 9.0, (float) 15.0, (float) 2.0};
	float[] downshifts = {(float) 2.0, (float) 15.0, (float) 9.0, (float) 5.0, (float) 8.0, (float) 3.0, (float) 45.0, (float) 4.0, (float) 5.0, (float) 6.0, (float) 9.0, (float) 16.0, (float) 1.0, (float) 15.0, (float) 8.0, (float) 5.0, (float) 4.0, (float) 3.0, (float) 32.0, (float) 2.0, (float) 5.0, (float) 3.0, (float) 5.0, (float) 8.0, (float) 1.0};
	
	SourceDataLine new_line;
	public Rompler(String samplename)
	{
		MidiDevice device;
		MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
		try
		{
			sample = new File(System.getProperty("user.dir") + "/sounds/" + samplename);
			fis = new FileInputStream(sample);
			stream = AudioSystem.getAudioInputStream(sample);
			AudioFormat orig_format = stream.getFormat();
			DataLine.Info dinfo = new DataLine.Info(SourceDataLine.class, orig_format);			
			streams = new AudioInputStream[25];
			clips = new Clip[25];
			
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
				int max = 0;
				int min = 0;
				bytearray = new byte[arraylen];
				// Values range from -128 to 127, so 128 is out of bounds
				Arrays.fill(bytearray, (byte) 0);
				if (i > 13)
				{
					filter_freq = (int) ((float) nyquist / freqs[i]);
					//float value
				}
				//bytearray = shift_by_x(freqs[i], basearray); // Only works for the unused version
				bytearray = shift_by_x(i, basearray);
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
		System.out.println("\nAssume that the base sample's relative pitch is C4.\nThis rompler only plays back a relative pitch of C3 through C5.");
	}
	
	public Rompler() { this("short_summer.wav");/*this("wow3.wav");*/ }
	
	public byte[] shift_by_x(float degree, byte[] input)
	{
		int new_length = (int) (degree * (float) input.length);
		byte[] output = new byte[new_length];
		Arrays.fill(output, (byte) 128);
		for (int i = 0; i < input.length; i++)
		{
			//output[i] = input[(int) ((float) i / degree)]; // Too much static
			if ((int) ((float) i / degree) < output.length)
			{
				output[(int) ((float) i / degree)] = input[i];
			}
			else { break; }
		}
		return output;
	}
	
	public byte[] shift_by_interval(int interval_index, byte[]input)
	{
		byte[] output = shift_by_x(this.upshifts[interval_index], input);
		output = shift_by_x(this.downshifts[interval_index], output);
		return output;
	}
	
	public static void main(String[] args)
	{
		if (args.length == 0) { Rompler handler = new Rompler(); }
		else { Rompler handler = new Rompler(args[0]); }
	}
}
