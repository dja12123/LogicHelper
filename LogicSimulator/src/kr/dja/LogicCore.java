package kr.dja;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Image;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class LogicCore
{
	public static final Resource RES = new Resource();
	public static void main(String[] args)
	{
		new UI();
	}
}
class Resource
{
	public Font NORMAL_FONT;
	public Font PIXEL_FONT;
	public Font BAR_FONT;
	Resource()
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
	}
}