package com.xrbpowered.zoomui.std.colors;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.UIElement;
import com.xrbpowered.zoomui.std.UIListBox;

public class UIColorBox extends UIElement {

	public static Color colorBorder = UIListBox.colorBorder;
	public static Color[] colorChecker = new Color[] { new Color(0xf7f7f7), new Color(0xeeeeee) };
	
	public static int defaultWidth = 40;
	public static int defaultHeight = 20;

	public Color color = Color.BLACK;
	
	public UIColorBox(UIContainer parent) {
		super(parent);
		setSize(defaultWidth, defaultHeight);
	}

	public Color getColor() {
		return color;
	}
	
	@Override
	public void paint(GraphAssist g) {
		float pix = g.startPixelMode(this);
		int w = (int)(getWidth()/pix);
		int h = (int)(getHeight()/pix);
		g.resetStroke();
		g.setPaint(getTransparencyPaint(pix));
		g.fillRect(0, 0, w, h);
		g.fillRect(0, 0, w, h, getColor());
		g.drawRect(0, 0, w-1, h-1, colorBorder);
		g.finishPixelMode();
	}
	
	private static TexturePaint transparencyPaint = null;
	
	public static Paint getTransparencyPaint(float pix) {
		int tile = (int)(8/pix);
		int size = tile*2;
		if(transparencyPaint==null || transparencyPaint.getImage().getWidth()!=size) {
			BufferedImage buffer = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = (Graphics2D) buffer.getGraphics();
			for(int x=0; x<2; x++)
				for(int y=0; y<2; y++) {
					g.setColor(colorChecker[(x+y)&1]);
					g.fillRect(x*tile, y*tile, tile, tile);
				}
			Rectangle2D r = new Rectangle2D.Float(0, 0, size, size);
			transparencyPaint = new TexturePaint(buffer, r);
		}
		return transparencyPaint;
	}
	
	public static int colorWithAlpha(int rgb, float alpha) {
		rgb &= 0xffffff;
		rgb |= Math.round(alpha*255f) << 24;
		return rgb;
	}
	
	public static Color colorWithAlpha(Color color, float alpha) {
		return new Color(colorWithAlpha(color.getRGB(), alpha), true);
	}

}
