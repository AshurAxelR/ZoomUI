package com.xrbpowered.zoomui.std;

import java.awt.Color;
import java.awt.Font;

import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.UIElement;
import com.xrbpowered.zoomui.std.text.UITextBox;

public class UIListItem extends UIElement {
	
	public static float defaultHeight = 20f;
	
	public static Font font = UIButton.font;

	public static Color colorText = UITextBox.colorText;
	public static Color colorHighlight = new Color(0xe8f0ff);
	public static Color colorSelection = UITextBox.colorSelection;
	public static Color colorSelectedText = UITextBox.colorSelectedText;

	public final int index;
	public final Object object;
	
	public final UIListBox list;
	protected boolean hover = false;
	
	public UIListItem(UIListBox list, int index, Object object) {
		super(list.getView());
		this.index = index;
		this.object = object;
		this.list = list;
		setSize(0, defaultHeight);
	}
	
	@Override
	public void paint(GraphAssist g) {
		boolean sel = (index==list.getSelectedIndex());
		g.fill(this, sel ? colorSelection : hover ? colorHighlight : Color.WHITE);
		g.setColor(sel ? colorSelectedText : colorText);
		g.setFont(font);
		g.drawString(getLabel(), 8, getHeight()/2f, GraphAssist.LEFT, GraphAssist.CENTER);
	}
	
	protected String getLabel() {
		return object.toString();
	}
	
	@Override
	public void onMouseIn() {
		hover = true;
		repaint();
	}
	
	@Override
	public void onMouseOut() {
		hover = false;
		repaint();
	}
	
	@Override
	public boolean onMouseDown(float px, float py, Button button, int mods) {
		if(button==Button.left) {
			if(list.getSelectedIndex()==index)
				list.onClickSelected();
			else
				list.select(index);
			repaint();
			return true;
		}
		else
			return false;
	}
}