package com.xrbpowered.zoomui.std.menu;

import static com.xrbpowered.zoomui.MouseInfo.LEFT;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;

import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.MouseInfo;
import com.xrbpowered.zoomui.UIElement;
import com.xrbpowered.zoomui.base.UIButtonBase;
import com.xrbpowered.zoomui.std.UIButton;
import com.xrbpowered.zoomui.std.text.UITextBox;

public class UIMenuItem extends UIButtonBase {

	public static Font font = UIButton.font;
	
	public static Color colorText = Color.BLACK;
	public static Color colorDisabled = new Color(0x999999);
	public static Color colorHover = UITextBox.colorSelection;
	public static Color colorHoverText = UITextBox.colorSelectedText;

	public static int defaultHeight = 24;
	public static int marginLeft = 16;
	public static int marginRight = 32;
	
	public String label;
	
	public UIMenuItem(UIMenu parent, String label) {
		super(parent);
		this.label = label;
		setSize(0, defaultHeight);
	}
	
	public float getMinWidth() {
		FontMetrics fm = getRoot().getWindow().getFontMetrics(font, font.getSize(), getPixelSize());
		return fm.stringWidth(label)+getTotalMargins();
	}

	public float getMarginLeft() {
		return marginLeft;
	}

	public float getTotalMargins() {
		return marginLeft+marginRight;
	}

	public String getLabel() {
		return label;
	}
	
	public boolean isActive() {
		return isHover();
	}
	
	@Override
	public void paint(GraphAssist g) {
		if(!isEnabled()) {
			g.setColor(colorDisabled);
		}
		else if(isActive()) {
			g.fill(this, colorHover);
			g.setColor(colorHoverText);
		}
		else {
			g.setColor(colorText);
		}
		g.setFont(font);
		g.drawString(getLabel(), getMarginLeft(), getHeight()/2f, GraphAssist.LEFT, GraphAssist.CENTER);
	}
	
	@Override
	public boolean onMouseDown(float x, float y, MouseInfo mouse) {
		if(mouse.eventButton==LEFT) {
			if(isEnabled()) {
				onAction();
				((UIMenu) getParent()).onItemAction();
			}
			repaint();
		}
		return true;
	}
	
	@Override
	public boolean onMouseUp(float x, float y, MouseInfo mouse, UIElement initiator) {
		if(initiator!=this)
			return false;
		return true;
	}
}
