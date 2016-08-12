package kr.dja;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Image;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class LogicCore
{
	public static final Resource RES = new Resource();
	public static void main(String[] args)
	{
		new UI();
	}
	public static Resource getResource()
	{
		return RES;
	}
}
class Resource
{
	public Font NORMAL_FONT;
	public Font PIXEL_FONT;
	public Font BAR_FONT;
	public int ASD;
	Properties config = new Properties();
	public Resource()
	{
		
		try
		{
			NORMAL_FONT = Font.createFont(Font.TRUETYPE_FONT, getClass().getClassLoader().getResourceAsStream("font/SeoulNamsanM.ttf"));
			BAR_FONT = Font.createFont(Font.TRUETYPE_FONT,getClass().getClassLoader().getResourceAsStream("font/gulim.ttf"));
			PIXEL_FONT = Font.createFont(Font.TRUETYPE_FONT,getClass().getClassLoader().getResourceAsStream("font/HOOG0557.ttf"));
		}
		catch(FontFormatException | IOException e)
		{
			e.printStackTrace();
		}
		
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(getClass().getClassLoader().getResource("config.properties").getFile()));
			config.load(reader);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		config.getOrDefault("SaveLocation", "");
		config.getOrDefault("Cycle", "200");
		config.getOrDefault("OpenFile", "");
		
		
	}
}