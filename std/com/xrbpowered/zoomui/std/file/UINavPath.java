package com.xrbpowered.zoomui.std.file;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.io.File;

import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.UIElement;
import com.xrbpowered.zoomui.std.UIArrowButton;
import com.xrbpowered.zoomui.std.UIButton;
import com.xrbpowered.zoomui.std.UIListItem;
import com.xrbpowered.zoomui.std.text.UITextBox;

public class UINavPath extends UIContainer {

	public static Font font = UIButton.font;
	
	public static Color colorBackground = UITextBox.colorBackground;
	public static Color colorText = UITextBox.colorText;
	public static Color colorBorder = UITextBox.colorBorder;
	public static Color colorHighlight = UIListItem.colorHighlight;

	protected class PathItem extends UIElement {
		public final int index;
		public final String item;
		
		public PathItem(UIContainer parent, int index, String item) {
			super(parent);
			this.index = index;
			this.item = item;
		}
		
		@Override
		public boolean repaintOnHover() {
			return true;
		}

		@Override
		public void paint(GraphAssist g) {
			Color bgColor = isHover() ? colorHighlight : colorBackground;
			g.fill(this, bgColor);
			
			g.setFont(font);
			g.setColor(colorText);
			g.drawString(item, 14, getHeight()/2, GraphAssist.LEFT, GraphAssist.CENTER);
			UIArrowButton.drawRightArrow(g, 6, (int)(getHeight()/2));
		}
		
		@Override
		public boolean onMouseDown(float x, float y, Button button, int mods) {
			if(button==Button.left) {
				onItemClicked(index, item);
				return true;
			}
			else
				return false;
		}
		
		public int measureSize(GraphAssist g) {
			g.setFont(font);
			FontMetrics fm = g.getFontMetrics();
			int w = fm.stringWidth(item)+20;
			setSize(w, UINavPath.this.getHeight());
			return w;
		}
	}
	
	protected PathItem[] items = null;
	
	private boolean measure = false;
	private boolean oversize = false;
	
	public UINavPath(UIContainer parent) {
		super(parent);
		setSize(UITextBox.defaultWidth, UITextBox.defaultHeight);
	}
	
	@Override
	public boolean repaintOnHover() {
		return true;
	}

	public void setPath(String[] items) {
		this.items = new PathItem[items.length];
		removeAllChildren();
		for(int i=0; i<items.length; i++) {
			this.items[i] = new PathItem(this, i, items[i]);
		}
		measure = true;
	}

	public void setPath(String path) {
		String[] items = path.split("[\\\\\\/]");
		setPath(items);
	}

	public void setPath(File path) {
		setPath(path.getAbsolutePath());
	}
	
	public void onItemClicked(int index, String item) {
	}
	
	@Override
	public void layout() {
		measure = true;
	}
	
	@Override
	protected void paintBackground(GraphAssist g) {
		g.fill(this, colorBackground);
	}
	
	@Override
	protected void paintChildren(GraphAssist g) {
		if(measure) {
			if(items!=null) {
				int wtotal = 0;
				oversize = false;
				for(int i=items.length-1; i>=0; i--) {
					PathItem item = items[i];
					int w = item.measureSize(g);
					if(!oversize && w+wtotal<getWidth()-16) {
						wtotal += w;
						item.setVisible(true);
					}
					else {
						oversize = true;
						item.setVisible(false);
					}
				}
				float x = oversize ? 16 : 0;
				for(UIElement c : children) {
					if(c.isVisible()) {
						c.setPosition(x, 0);
						x += c.getWidth();
					}
				}
			}
			measure = false;
		}
		if(oversize) {
			g.setFont(font);
			g.setColor(colorText);
			g.drawString("...", 8, getHeight()/2, GraphAssist.CENTER, GraphAssist.CENTER);
		}
		super.paintChildren(g);
		g.border(this, isHover() ? colorText : colorBorder);
	}
}
