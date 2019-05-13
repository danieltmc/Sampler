import javax.sound.midi.*;
import javax.sound.sampled.*;
import java.util.Arrays;
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
	// C3 = 1:2, Db3 = 8:15, D3 = 5:9, Eb3 = 3:5, E3 = 5:8, F3 = 2:3, Gb3 = 32:45, G3 = 3:4, Ab3 = 4:5, A3 = 5:6, Bb3 = 8:9, B3 = 15:16
	// C4 = 1:1, Db4 = 16:15, D4 = 9:8, Eb4 = 6:5, E4 = 5:4, F4 = 4:3, Gb4 = 45:32, G4 = 3:2, Ab4 = 8:5, A4 = 5:3, Bb4 = 9:5, B4 = 15:8, C5 = 2:1
	// Using alternate ratios because some of the larger pairs (like Gb) don't sound good unless they're rounded a little bit
	//int[] upsamples = {1, 8, 5, 3, 5, 2, 32, 3, 4, 5, 8, 15, 1, 16, 9, 6, 5, 4, 45, 3, 8, 5, 9, 15, 2};
	int[] upsamples = {1, 8, 5, 3, 5, 2, 7, 3, 4, 5, 8, 15, 1, 16, 9, 6, 5, 4, 10, 3, 8, 5, 9, 15, 2};
	//int[] downsamples = {2, 15, 9, 5, 8, 3, 45, 4, 5, 6, 9, 16, 1, 15, 8, 5, 4, 3, 32, 2, 5, 3, 5, 8, 1};
	int[] downsamples = {2, 15, 9, 5, 8, 3, 10, 4, 5, 6, 9, 16, 1, 15, 8, 5, 4, 3, 7, 2, 5, 3, 5, 8, 1};
	
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
				int max = 0;
				int min = 0;
				bytearray = shift_by_interval(i, basearray);
				streams[i] = new AudioInputStream(new ByteArrayInputStream(bytearray), orig_format, (long) bytearray.length);
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
	
	public byte[] shift_by_interval(int interval_index, byte[] input)
	{
		byte[] output = shift_up_by_x(interval_index, input);
		output = shift_down_by_x(interval_index, output);
		return output;
	}
	
	public byte[] shift_up_by_x(int interval_index, byte[] input)
	{
		//System.out.println("Up");
		int degree = this.upsamples[interval_index];
		int new_length = degree * input.length;
		byte[] output = new byte[new_length];
		// 128 is outside of the range, so these are effectively tagged to be replaced
		Arrays.fill(output, (byte) 128);
		for (int i = 0; i < input.length; i++)
		{
			if ((i / degree) < output.length)
			{
				output[i / degree] = input[i];
			}
			else { break; }
		}
		// Interpolation of stuffed values
		for (int i = 0; i < output.length; i++)
		{
			// Replace the stuffed values with the average of the point before and after
			if ((output[i] == (byte) 128) && (i < output.length - 1) && (i > 0))
			{
				output[i] = (byte) (((int) output[i-1] + (int) output[i+1]) / 2);
			}
			// Sinc interpolation - it works, but it is significantly slower to process
			/*if (output[i] == (byte) 128)
			{
				output[i] = (byte) ((double) 127 * (Math.sin(Math.PI * (double) i) / (Math.PI * (double) i)));
			}*/
		}
		return output;
	}
	
	public byte[] shift_down_by_x(int interval_index, byte[] input)
	{
		//System.out.println("Down");
		int degree = this.downsamples[interval_index];
		int new_length = input.length / degree;
		byte[] output = new byte[new_length];
		Arrays.fill(output, (byte) 0);
		for (int i = 0; i < input.length; i++)
		{
			if ((i * degree) < output.length)
			{
				output[i * degree] = input[i];
			}
			else { break; }
		}
		//return LPF(interval_index, output); // Kills the sound
		return output;
	}
	
	public byte[] LPF(int interval_index, byte[] input)
	{
		float normalized;
		float temp;
		byte val;
		float q = (float) Math.sqrt(0.5);
		float new_freq = (float) 1.0 / (float) this.downsamples[interval_index];
		for (int i = 0; i < input.length; i++)
		{
			normalized = ((float) (input[i] + 128)) / ((float) (127 + 128));
			temp = (float) 1 / ((((float) i * (float) i)/(new_freq * new_freq)) + ((float) i / (new_freq * q)) + (float) 1);
			if (temp > (float) 0.5) { input[i] = (byte) (temp * (float) 127 * (float) 2); }
			else if (temp < (float) 0.5) { input[i] = (byte) (temp * (float) -128 * (float) 2); }
			else { input[i] = (byte) 0; }
		}
		return input;
	}
	
	// Creates too much static to be useful
	public byte[] gain(byte[] input)
	{
		float normalized;
		for (int i = 0; i < input.length; i++)
		{
			normalized = ((float) (input[i] + 128)) / ((float) (127 + 128));
			if (normalized > (float) 0.5) { input[i] = (byte) (normalized * (float) 127); }
			else if (normalized < (float) 0.5) { input[i] = (byte) (normalized * (float) -128);	}
			else { input[i] = (byte) 0; }
		}
		return input;
	}
	
	public static void main(String[] args)
	{
		if (args.length == 0) { Rompler handler = new Rompler(); }
		else { Rompler handler = new Rompler(args[0]); }
	}
}
