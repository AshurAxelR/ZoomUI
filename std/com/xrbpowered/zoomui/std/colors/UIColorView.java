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
		
		protected DragActor dragActor = new DragPointActor(this) {
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

		protected Color getColorAtHover(float size) {
			return getBoxColorAt(hoverx/size, hovery/size);
		}
		
		protected void updateBuffer(int size) {
			if(size<=0)
				return;
			buffer = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
			bufferHue = getHue();
			Graphics2D g = (Graphics2D) buffer.getGraphics();
			for(int x=0; x<size; x++)
				for(int y=0; y<size; y++) {
					g.setColor(getBoxColorAt(x/(float)size, y/(float)size));
					g.fillRect(x, y, 1, 1);
				}
		}
		
		protected int getBoxSize(float pix) {
			return (int)(getWidth()/pix);
		}
		
		@Override
		public void paint(GraphAssist g) {
			float pix = g.startPixelMode(this);
			int size = getBoxSize(pix);
			if(buffer==null || bufferHue!=getHue() || buffer.getWidth()!=size)
				updateBuffer(size);
			if(buffer!=null)
				g.graph.drawImage(buffer, 0, 0, null);
			if(this.hoverx>=0 && hovery>=0) {
				g.pushClip(0, 0, size, size);
				g.pushAntialiasing(true);
				g.pushPureStroke(true);
				int r = (int)(colorDotRadius/pix);
				g.setStroke(3f/pix);
				g.setColor(new Color(0x77000000, true));
				g.graph.drawOval(hoverx-r, hovery-r, r*2, r*2);
				g.setColor(getColorAtHover(size));
				g.graph.fillOval(hoverx-r, hovery-r, r*2, r*2);
				g.resetStroke();
				g.setColor(Color.WHITE);
				g.graph.drawOval(hoverx-r, hovery-r, r*2, r*2);
				g.popAntialiasing();
				g.popPureStroke();
				g.popClip();
			}
			g.resetStroke();
			g.drawRect(0, 0, size-1, size-1, colorBorder);
			g.finishPixelMode();
		}
		
		protected void updateHover(float x, float y, float pix) {
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
		
		protected void pickColor(float pix) {
			int size = getBoxSize(pix);
			if(setColor(getColorAtHover(size)))
				onColorChanged();
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
	
	public final ColorBox box;
	public final UIColorSlider slider;
	
	private float[] hsb = {0, 0, 0};
	private Color color = Color.BLACK;
	
	public UIColorView(UIContainer parent) {
		super(parent);
		box = new ColorBox();
		slider = new UIColorSlider(this, true) {
			@Override
			public Color getSliderColorAt(float sz) {
				return UIColorView.this.getSliderColorAt(sz);
			}
			@Override
			public void onChanged() {
				hsb[0] = getValue();
				if(setColor(new Color(Color.HSBtoRGB(getHue(), getSaturation(), getBrightness()))))
					onColorChanged();
				repaint();
			}
		};
	}

	public float getHue() {
		return hsb[0];
	}
	
	public float getSaturation() {
		return hsb[1];
	}
	
	public float getBrightness() {
		return hsb[2];
	}
	
	public Color getBoxColorAt(float sx, float sy) {
		float s = sx;
		float v = 1f - sy;
		return new Color(Color.HSBtoRGB(getHue(), s, v));
	}

	public Color getSliderColorAt(float sz) {
		return new Color(Color.HSBtoRGB(sz, 1f, 1f));
	}

	public Color getColor() {
		return color;
	}
	
	public boolean setColor(Color color) {
		if(color.equals(this.color))
			return false;
		this.color = color;
		Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), this.hsb);
		return true;
	}
	
	public void onColorChanged() {
		System.out.printf("#%06x\n", color.getRGB()&0xffffff);
	}
	
	@Override
	public void layout() {
		box.setLocation(0, 0);
		float boxSize = Math.min(getWidth()-UIColorSlider.defaultWidth, getHeight());
		box.setSize(boxSize, boxSize);
		slider.setLocation(boxSize, 0);
		slider.setSize(UIColorSlider.defaultWidth, boxSize);
		super.layout();
	}
	
	@Override
	protected void paintSelf(GraphAssist g) {
		g.fill(this, Color.WHITE);
	}
	
}
