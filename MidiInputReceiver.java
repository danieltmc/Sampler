import javax.sound.midi.*;
import javax.sound.sampled.*;
import java.util.ArrayList;
import java.util.List;
import java.io.*;

public class MidiInputReceiver implements Receiver
{
	// Name of MIDI device
	public String name;
	public Clip clip;
	public long pause_point = 0;
	
	public Clip a;
	public Clip b;
	public Clip c;
	public Clip d;
	public Clip[] clips;
	
	public FloatControl fca;
	public FloatControl fcb;
	
	public int num_playing = 0;
	public int[] playing_indexes = {0, 0};
	
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
	
	public void setClip(Clip clip, Clip clipb)
	{
		this.clip = clip;
		this.a = clipb;
		//this.fca = (FloatControl) this.clip.getControl(FloatControl.Type.MASTER_GAIN);
		//this.fcb = (FloatControl) this.a.getControl(FloatControl.Type.MASTER_GAIN);
	}
	public void setClipArray(Clip[] clips)
	{
		this.clips = clips;
	}
	
	public void send(MidiMessage msg, long timestamp)
	{	
		// Status messages are 32 bits, we don't care about the 24 MSB or the 4 LSB, so we isolate the 4bits we care about
		Integer status = new Integer(((msg.getMessage()[0] >> 4) & (0xf)));
		Integer input_channel = new Integer(msg.getMessage()[1]);
		Integer input_val = new Integer(msg.getMessage()[2]);
		System.out.println("\nStatus: " + Integer.toHexString(status));
		for (int i = 1; i < msg.getMessage().length; i++)
		{
			//System.out.println("Data #" + i + ": " + Integer.toBinaryString(msg.getMessage()[i]));
			//System.out.println("Data #" + i + ": " + Integer.toHexString(msg.getMessage()[i]));
			System.out.println("Data #" + i + ": " + Integer.toString(msg.getMessage()[i]));
		}
		//System.out.println("Data: " + Integer.toHexString(msg.getMessage()[0]) + Integer.toHexString(msg.getMessage()[1]) + Integer.toHexString(msg.getMessage()[2]));
		
		// Lowest note on MPK Mini: 48, Highest note on MPK Mini: 72
		if (status.equals(note_on))
		{
			System.out.println("Note on");
			
			// Ensure that users can't cause NullPointerExceptions by going outside of the pre-defined range
			if (input_channel < 48) { input_channel = 48; }
			else if (input_channel > 72) { input_channel = 72; }
			// Necessary to resume clip from its previous stopping point - we don't want this, but it's interesting
			//clip.setMicrosecondPosition(pause_point);
			//clip.setMicrosecondPosition(0);
			//clip.start();
			this.clips[input_channel - 48].setMicrosecondPosition(0);
			//this.clips[input_channel - 48].start();
			this.clips[input_channel - 48].loop(100);
			if (this.num_playing == 1) { }
			this.num_playing++;
		}
		else if (status.equals(note_off))
		{
			System.out.println("Note off");
			// Necessary to resume clip from same point
			//pause_point = clip.getMicrosecondPosition();
			//clip.stop();
			this.clips[input_channel - 48].stop();
			this.num_playing--;
		}
		else if (status.equals(poly_press))
		{
			// TODO: Add Aftertouch (changing sound by pressing harder on the key while it is already pressed)
			// Akai MPK Mini does not support aftertouch
			System.out.println("Polyphonic Pressure");
		}
		else if (status.equals(control_change))
		{
			// TODO: Add Control Change (see https://www.midi.org/specifications-old/item/table-3-control-change-messages-data-bytes-2 for info)
			System.out.println("Control Change");
			//fca.setValue(1.0f / ((float) input_val + 1));
		}
		else if (status.equals(program_change))
		{
			// TODO: Add Program Change (patch change - allow user to change samples)
			System.out.println("Program Change");
			//fcb.setValue(1.0f / ((float) input_val + 1));
		}
		else if (status.equals(channel_press))
		{
			// TODO: Add Channel Pressure (Like aftertouch, but averaged across all of the keys of the controller)
			// Akai MPK Mini does not support channel pressure
			System.out.println("Channel Pressure");
		}
		else if (status.equals(pitch_bend))
		{
			// TODO: Add Pitch Bend (push the pitch higher or lower)
			System.out.println("Pitch Bend");
		}
		else if (status.equals(sysex))
		{
			// Will not be interpreted by this program, all necessary features from Sysex messages are implemented elsewhere
			System.out.println("System Exclusive Message");
		}
	}
	
	public void close()
	{
		for (int i = 0; i < this.clips.length; i++) { this.clips[i].close(); }
	}
}
