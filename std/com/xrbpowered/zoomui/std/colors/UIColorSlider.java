package com.xrbpowered.zoomui.std.colors;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import com.xrbpowered.zoomui.DragActor;
import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.UIElement;
import com.xrbpowered.zoomui.base.DragPointActor;
import com.xrbpowered.zoomui.std.UIArrowButton;
import com.xrbpowered.zoomui.std.UIListBox;

public abstract class UIColorSlider extends UIElement {

	public static int defaultWidth = 32;
	public static int margin = 8;
	
	public static float floatThreshold = 0.001f;

	public static Color colorBorder = UIListBox.colorBorder;

	public final boolean vertical;
	
	protected float value = 0f;
	
	private BufferedImage buffer = null;

	protected DragActor dragActor = new DragPointActor(this) {
		@Override
		public boolean notifyMouseMove(float dx, float dy) {
			super.notifyMouseMove(dx, dy);
			pickValue(posx, posy, pixelScale);
			repaint();
			return true;
		}
	};
	
	public UIColorSlider(UIContainer parent, boolean vertical) {
		super(parent);
		this.vertical = vertical;
	}
	
	private void checkRange() {
		if(value<0f)
			value = 0f;
		if(value>1f)
			value = 1f;
	}
	
	public float getValue() {
		return value;
	}
	
	public boolean setValue(float v) {
		float old = this.value;
		this.value = v;
		checkRange();
		return Math.abs(old-this.value)>floatThreshold;
	}

	public abstract Color getSliderColorAt(float sz);
	
	protected void updateBuffer(int w, int h) {
		if(w<=0 || h<=0)
			return;
		buffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D) buffer.getGraphics();
		for(int x=0; x<w; x++)
			for(int y=0; y<h; y++) {
				float sz = vertical ? y/(float)h : x/(float)w;
				g.setColor(getSliderColorAt(sz));
				g.fillRect(x, y, 1, 1);
			}
	}
	
	protected int getBoxWidth(float pix) {
		int w = (int)(getWidth()/pix);
		if(vertical)
			w -= margin*2;
		return w;
	}

	protected int getBoxHeight(float pix) {
		int h = (int)(getHeight()/pix);
		if(!vertical)
			h -= margin*2;
		return h;
	}

	@Override
	public void paint(GraphAssist g) {
		float pix = g.startPixelMode(this);
		int w = getBoxWidth(pix);
		int h = getBoxHeight(pix);
		int mx = vertical ? margin : 0;
		int my = !vertical ? margin : 0;
		if(buffer==null || buffer.getWidth()!=w || buffer.getHeight()!=h)
			updateBuffer(w, h);
		if(buffer!=null)
			g.graph.drawImage(buffer, mx, my, null);
		g.resetStroke();
		g.drawRect(mx, my, w-1, h-1, colorBorder);
		g.finishPixelMode();
		
		g.setColor(Color.BLACK);
		if(vertical) {
			int y = (int)(getValue()*h*pix);
			UIArrowButton.drawRightArrow(g, mx/2, y);
			UIArrowButton.drawLeftArrow(g, (int)(getWidth()-mx/2), y);
		}
		else {
			int x = (int)(getValue()*w*pix);
			UIArrowButton.drawDownArrow(g, x, my/2);
			UIArrowButton.drawDownArrow(g, x, (int)(getHeight()-my/2));
		}
	}

	protected void pickValue(float x, float y, float pix) {
		if(vertical) {
			int h = getBoxHeight(pix);
			if(setValue(y/(float)h/pix))
				onChanged();
		}
		else {
			int w = getBoxWidth(pix);
			if(setValue(x/(float)w/pix))
				onChanged();
		}
	}
	
	public void onChanged() {
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
		pickValue(x, y, pix);
		return true;
	}
}
