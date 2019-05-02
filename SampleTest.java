import javax.sound.midi.*;
import javax.sound.sampled.*;
import java.util.ArrayList;
import java.util.List;
import java.io.*;

public class SampleTest
{
	public File sample;
	public AudioInputStream stream;
	public AudioFormat format;
	public DataLine.Info info;
	public Clip clip;
	public Transmitter trans;
	private MidiInputReceiver temp;
	
	public SampleTest()
	{
		MidiDevice device;
		MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
		
		try
		{
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
				temp.setClip(clip);
				temp.setStream(stream);
				temp.setFormat(format);
				trans = device.getTransmitter();
				//trans.setReceiver(new MidiInputReceiver(device.getDeviceInfo().toString()));
				trans.setReceiver(temp);
				device.open();
				System.out.println("MIDI Device Initialized: " + device.getDeviceInfo() + " was opened");
				
				//clips[0].open(streams[0]);
			}
			catch (MidiUnavailableException e) { /* Do nothing because we are iterating through all options until we find one that works */ }
		}
	}
	
	public static void main(String[] args)
	{
		SampleTest handler = new SampleTest();
		return;
	}
}

class MidiInputReceiver implements Receiver
{
	// Name of MIDI device
	public String name;
	//public SampleTest sampler;
	public Clip clip;
	public AudioInputStream stream;
	public AudioFormat format;
	public long pause_point = 0;
	// Midi Status values
	/*
	private Integer note_off = new Integer(0x8);
	private Integer note_on = new Integer(0x9);
	private Integer poly_press = new Integer(0xA);
	private Integer control_change = new Integer(0xB);
	private Integer program_change = new Integer(0xC);
	private Integer channel_press = new Integer(0xD);
	private Integer pitch_bend = new Integer(0xE);
	private Integer sysex = new Integer(0xF);
	*/	
	private Integer note_off = new Integer(0xffffff80);
	private Integer note_on = new Integer(0xffffff90);
	private Integer poly_press = new Integer(0xffffffa0);
	private Integer control_change = new Integer(0xffffffb0);
	private Integer program_change = new Integer(0xffffffc0);
	private Integer channel_press = new Integer(0xffffffd0);
	private Integer pitch_bend = new Integer(0xffffffe0);
	private Integer sysex = new Integer(0xfffffff0);
	
	public MidiInputReceiver(String name)
	{
		this.name = name;
	}
	
	public void setClip(Clip clip)
	{
		this.clip = clip;
	}
	public void setStream(AudioInputStream stream)
	{
		this.stream = stream;
	}
	public void setFormat(AudioFormat format)
	{
		this.format = format;
	}
	
	public void send(MidiMessage msg, long timestamp)
	{
		// Print Status in binary
		//System.out.println("Status: " + Integer.toBinaryString(msg.getMessage()[0]));
		// Print Status in hex
		System.out.println("Status: " + Integer.toHexString(msg.getMessage()[0]));
		///*
		for (int i = 1; i < msg.getMessage().length; i++)
		{
			System.out.println("Data: " + Integer.toHexString(msg.getMessage()[i]));
		}
		//*/
		Integer status = new Integer(msg.getMessage()[0]);
		
		if (status.equals(note_on))
		{
			System.out.println("Note on");
			/*try
			{
				clip.open(stream);
				clip.start();
			}
			catch (Exception e) { System.out.println(e.toString()); }*/
			// Necessary to resume clip from same point
			//clip.setMicrosecondPosition(pause_point);
			clip.setMicrosecondPosition(0);
			clip.start();
		}
		else if (status.equals(note_off))
		{
			System.out.println("Note off");
			// Necessary to resume clip from same point
			//pause_point = clip.getMicrosecondPosition();
			clip.stop();
			//clip.close();
			//clip.stop();
		}
	}
	
	public void close()
	{
		return;
	}
}
