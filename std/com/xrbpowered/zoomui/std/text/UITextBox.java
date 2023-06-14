package com.xrbpowered.zoomui.std.text;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;

import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.base.UIPanView;
import com.xrbpowered.zoomui.base.UITextEdit;
import com.xrbpowered.zoomui.std.UIButton;

public class UITextBox extends UIPanView {

	public static Font font = UIButton.font;

	public static Color colorBackground = Color.WHITE;
	public static Color colorText = Color.BLACK;
	public static Color colorSelection = new Color(0x0077dd);
	public static Color colorSelectedText = Color.WHITE;
	public static Color colorBorder = new Color(0x888888);

	public static int defaultWidth = 120;
	public static int defaultHeight = 20;

	public final UITextEdit editor;
	
	public UITextBox(UIContainer parent) {
		super(parent);
		editor = createEditor();
		setSize(defaultWidth, defaultHeight);
	}
	
	protected UITextEdit createEditor() {
		return new UITextEdit(this, true) {
			public boolean onKeyPressed(char c, int code, int modifiers) {
				switch(code) {
					case KeyEvent.VK_ENTER:
						checkPushHistory(HistoryAction.unspecified);
						if(onEnter())
							getBase().resetFocus();
						repaint();
						return true;
					case KeyEvent.VK_ESCAPE:
						checkPushHistory(HistoryAction.unspecified);
						if(onEscape())
							getBase().resetFocus();
						repaint();
						return true;
					default:
						return super.onKeyPressed(c, code, modifiers);
				}
			}
		};
	}

	public boolean onEnter() {
		return true;
	}

	public boolean onEscape() {
		return true;
	}
	
	@Override
	public void layout() {
		editor.setLocation(0, 0);
		editor.updateSize();
	}
	
	@Override
	protected void paintChildren(GraphAssist g) {
		super.paintChildren(g);
		g.border(this, editor.isFocused() ? colorSelection : editor.hover ? colorText : colorBorder);
	}
	
}
