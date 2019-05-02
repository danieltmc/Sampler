import javax.sound.midi.*;
import javax.sound.sampled.*;
import java.util.ArrayList;
import java.util.List;
import java.io.*;

public class SampleTest
{
	/*
	public File samples[] = new File[1];// = {new File("summer.wav")};
	public AudioInputStream streams[] = new AudioInputStream[1];// = {AudioSystem.getAudioInputStream(samples[0])};
	public AudioFormat formats[] = new AudioFormat[1];// = {streams[0].getFormat()};
	public DataLine.Info infos[] = new DataLine.Info[1];// = {new DataLine.Info(Clip.class, formats[0])};
	public Clip clips[] = new Clip[1];// = {(Clip) AudioSystem.getLine(infos[0])};
	*/
	public File sample;
	public AudioInputStream stream;
	public AudioFormat format;
	public DataLine.Info info;
	public Clip clip;
	
	public SampleTest()
	{
		MidiDevice device;
		MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
		
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
				Transmitter trans = device.getTransmitter();
				trans.setReceiver(new MidiInputReceiver(device.getDeviceInfo().toString()));
				device.open();
				System.out.println("MIDI Device Initialized: " + device.getDeviceInfo() + " was opened");
				
				//clips[0].open(streams[0]);
			}
			catch (MidiUnavailableException e)
			{
				// Do nothing because we are iterating through all options until we find one that works
			}
		}
		try
		{
		/*
			samples[0] = new File("summer.wav");
			streams[0] = AudioSystem.getAudioInputStream(samples[0]);
			formats[0] = streams[0].getFormat();
			infos[0] = new DataLine.Info(Clip.class, formats[0]);
			clips[0] = (Clip) AudioSystem.getLine(infos[0]);
		*/
		sample = new File("summer.wav");
		stream = AudioSystem.getAudioInputStream(sample);
		format = stream.getFormat();
		info = new DataLine.Info(Clip.class, format);
		clip = (Clip) AudioSystem.getLine(info);
		clip.open(stream);
		}
		catch (Exception e)
		{
			System.out.println(e.toString());
		}
	}
	
	public static void main(String[] args)
	{
		SampleTest handler = new SampleTest();
		//samples = new File[args.length];
		
		try
		{
		/*
			for (int i = 0; i < args.length; i++)
			{
				samples[i] = new File(args[i]);
				streams[i] = AudioSystem.getAudioInputStream(samples[i]);
				formats[i] = streams[i].getFormat();
				infos[i] = new DataLine.Info(Clip.class, formats[i]);
				clips[i] = (Clip) AudioSystem.getLine(infos[i]);
				clips[i].open(streams[i]);
			}
		*/
			//clips[0].open(streams[0]);
		}
		catch (Exception e)
		{
			System.out.println("Exception occurred during initialization: " + e.toString());
		}
		return;
	}
}

class MidiInputReceiver implements Receiver
{
	// Name of MIDI device
	public String name;
	public SampleTest sampler;
	// Midi Status values
	private Integer note_off = new Integer(0x8);
	private Integer note_on = new Integer(0x9);
	private Integer poly_press = new Integer(0xA);
	private Integer control_change = new Integer(0xB);
	private Integer program_change = new Integer(0xC);
	private Integer channel_press = new Integer(0xD);
	private Integer pitch_bend = new Integer(0xE);
	private Integer sysex = new Integer(0xF);
	
	public MidiInputReceiver(String name)
	{
		this.name = name;
	}
	
	public setSampler(SampleTest sampler)
	{
		this.sampler = sampler;
	}
	
	public void send(MidiMessage msg, long timestamp)
	{
		//StringBuilder output = new StringBuilder( Integer.toBinaryString(msg.getMessage()[0]) + Integer.toBinaryString(msg.getMessage()[1]) + Integer.toBinaryString(msg.getMessage()[2]) );
		//System.out.println(msg.getMessage());
		System.out.println("Status: " + Integer.toBinaryString(msg.getMessage()[0]));
		Integer status = new Integer(msg.getMessage()[0]);
		if (status.equals(note_on))
		{
			// clips[i].start();
			clip.start();
		}
		else if (status.equals(note_off))
		{
			// clips[i].stop();
			clip.stop();
		}
	}
	
	public void close()
	{
		return;
	}
}
