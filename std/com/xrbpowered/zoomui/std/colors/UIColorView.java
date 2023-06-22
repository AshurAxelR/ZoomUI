package com.xrbpowered.zoomui.std.colors;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import com.xrbpowered.zoomui.DragActor;
import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.UIElement;
import com.xrbpowered.zoomui.base.DragPointActor;
import com.xrbpowered.zoomui.std.UIListBox;

public class UIColorView extends UIContainer {

	public static Color colorBorder = UIListBox.colorBorder;
	public static int colorDotRadius = 8;
	
	private class ColorBox extends UIElement {
		private int hoverx, hovery;
		private BufferedImage buffer = null;
		private float bufferHue = -1f;
		
		private DragActor dragActor = new DragPointActor(this) {
			@Override
			public boolean notifyMouseMove(float dx, float dy) {
				super.notifyMouseMove(dx, dy);
				updateHover(posx, posy, pixelScale);
				pickColor(pixelScale);
				repaint();
				return true;
			}
			
			@Override
			public boolean notifyMouseUp(float x, float y, Button button, int mods, UIElement target) {
				super.notifyMouseUp(x, y, button, mods, target);
				if(!isInside(posx, posy)) {
					hoverx = -1;
					hovery = -1;
					repaint();
				}
				return true;
			}
		};
		
		public ColorBox() {
			super(UIColorView.this);
			this.hoverx = -1;
			this.hovery = -1;
		}

		public int getBoxSize(float pix) {
			return (int)(getWidth()/pix);
		}
		
		public int getRgbAt(int x, int y, float hue, int size) {
			float s = x/(float)size;
			float v = 1f - y/(float)size;
			return Color.HSBtoRGB(hue, s, v);
		}
		
		private void updateBuffer(int size) {
			buffer = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
			bufferHue = getHue();
			Graphics2D g = (Graphics2D) buffer.getGraphics();
			for(int x=0; x<size; x++)
				for(int y=0; y<size; y++) {
					g.setColor(new Color(getRgbAt(x, y, bufferHue, size)));
					g.fillRect(x, y, 1, 1);
				}
		}
		
		@Override
		public void paint(GraphAssist g) {
			float pix = g.startPixelMode(this);
			int size = getBoxSize(pix);
			if(buffer==null || bufferHue!=getHue() || buffer.getWidth()!=size)
				updateBuffer(size);
			g.graph.drawImage(buffer, 0, 0, null);
			if(this.hoverx>=0 && hovery>=0) {
				g.pushClip(0, 0, size, size);
				g.pushAntialiasing(true);
				g.pushPureStroke(true);
				int r = (int)(colorDotRadius/pix);
				g.setStroke(3f/pix);
				g.setColor(new Color(0x77000000, true));
				g.graph.drawOval(hoverx-r, hovery-r, r*2, r*2);
				g.setColor(new Color(getRgbAt(hoverx, hovery, getHue(), size)));
				g.graph.fillOval(hoverx-r, hovery-r, r*2, r*2);
				g.resetStroke();
				g.setColor(Color.WHITE);
				g.graph.drawOval(hoverx-r, hovery-r, r*2, r*2);
				g.popAntialiasing();
				g.popPureStroke();
				g.popClip();
			}
			g.finishPixelMode();
			g.resetStroke();
			g.drawRect(0, 0, size*pix, size*pix, colorBorder);
		}
		
		private void updateHover(float x, float y, float pix) {
			int size = getBoxSize(pix);
			hoverx = (int)(x/pix);
			if(hoverx<0)
				hoverx = 0;
			else if(hoverx>size)
				hoverx = size;
			hovery = (int)(y/pix);
			if(hovery<0)
				hovery = 0;
			else if(hovery>size)
				hovery = size;
		}
		
		private void pickColor(float pix) {
			int size = getBoxSize(pix);
			float s = hoverx/(float)size;
			float v = 1f - hovery/(float)size;
			int prev = getRGB();
			int next = setColor(getHue(), s, v);
			if(prev!=next)
				onColorChange(next);
		}
		
		@Override
		public DragActor acceptDrag(float x, float y, Button button, int mods) {
			if(dragActor.notifyMouseDown(x, y, button, mods))
				return dragActor;
			else
				return null;
		}
		
		@Override
		public boolean onMouseDown(float x, float y, Button button, int mods) {
			float pix = getPixelScale();
			updateHover(x, y, pix);
			pickColor(pix);
			return true;
		}
		
		@Override
		public void onMouseMoved(float x, float y, int mods) {
			updateHover(x, y, getPixelScale());
			repaint();
		}
		
		@Override
		public void onMouseIn() {
			getBase().getWindow().setCursor(null);
		}
		
		@Override
		public void onMouseOut() {
			getBase().getWindow().setCursor(Cursor.getDefaultCursor());
			hoverx = -1;
			hovery = -1;
			repaint();
		}
	}
	
	private final ColorBox box;
	private float[] hsv = {0, 0, 0};
	private int rgb;
	
	public UIColorView(UIContainer parent) {
		super(parent);
		box = new ColorBox();
	}

	public float getHue() {
		return hsv[0];
	}
	
	public float getSaturation() {
		return hsv[1];
	}
	
	public float getValue() {
		return hsv[2];
	}
	
	public int getRGB() {
		return rgb;
	}
	
	public int setColor(float h, float s, float v) {
		this.hsv[0] = h;
		this.hsv[1] = s;
		this.hsv[2] = v;
		this.rgb = Color.HSBtoRGB(h, s, v) & 0xffffff;
		return this.rgb;
	}
	
	public void setColor(int rgb) {
		this.rgb = rgb;
		Color c = new Color(rgb);
		Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), this.hsv);
	}
	
	public void onColorChange(int rgb) {
		System.out.printf("#%06x\n", rgb);
	}
	
	@Override
	public void layout() {
		box.setLocation(0, 0);
		float boxSize = Math.min(getWidth(), getHeight());
		box.setSize(boxSize, boxSize);
		super.layout();
	}
	
	@Override
	protected void paintSelf(GraphAssist g) {
		g.fill(this, Color.WHITE);
	}
	
}
