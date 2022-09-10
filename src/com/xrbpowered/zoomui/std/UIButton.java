package com.xrbpowered.zoomui.std;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;

import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.base.UIButtonBase;

public class UIButton extends UIButtonBase {

	public static Font font = new Font("Tahoma", Font.PLAIN, GraphAssist.ptToPixels(9f));
	
	public static Color colorDown = new Color(0xd4d4d4);
	public static Color colorDisabled = new Color(0xe7e7e7);
	public static Color colorBorder = new Color(0x888888);
	public static Color colorText = Color.BLACK;
	public static Color colorTextDisabled = new Color(0x888888);
	public static Color colorGradTop = new Color(0xeeeeee);
	public static Color colorGradBottom = new Color(0xcccccc);

	public static int defaultWidth = 88;
	public static int defaultHeight = 20;

	public String label;
	
	public UIButton(UIContainer parent, String label) {
		super(parent);
		this.label = label;
		setSize(defaultWidth, defaultHeight);
	}
	
	@Override
	public void paint(GraphAssist g) {
		g.setPaint(isEnabled() ? (down ? colorDown : new GradientPaint(0, 0, colorGradTop, 0, getHeight(), colorGradBottom)) : colorDisabled);
		g.fill(this);
		g.border(this, hover ? colorText : colorBorder);
		g.setColor(isEnabled() ? colorText : colorTextDisabled);
		g.setFont(font);
		g.drawString(label, getWidth()/2f, getHeight()/2f, GraphAssist.CENTER, GraphAssist.CENTER);
	}
	
}
