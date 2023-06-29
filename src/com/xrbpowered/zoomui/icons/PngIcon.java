package com.xrbpowered.zoomui.icons;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

public class PngIcon {

	public final String uri;
	
	private BufferedImage buffer = null;
	
	public PngIcon(String uri) {
		this.uri = uri;
	}
	
	public PngIcon load() {
		BufferedImage img = null;
		try {
			InputStream in = ClassLoader.getSystemResourceAsStream(uri);
			if(in==null)
				in = new FileInputStream(new File(uri));
			img = ImageIO.read(in);
			in.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		buffer = img;
		return this;
	}
	
	public BufferedImage createImage() {
		if(buffer==null)
			load();
		return buffer;
	}
	
	public Image createImage(int width, int height) {
		if(buffer==null)
			load();
		return buffer.getScaledInstance(width, height, Image.SCALE_SMOOTH);
	}

}
