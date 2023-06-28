package com.xrbpowered.zoomui.std.text;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;

import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.KeyInputHandler;
import com.xrbpowered.zoomui.TabIndex;
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
		UITextEdit e = new UITextEdit(this, true) {
			public boolean onKeyPressed(char c, int code, int modifiers) {
				switch(code) {
					case KeyEvent.VK_TAB: {
							KeyInputHandler tab = getBase().tabIndex.selectTab(TabIndex.getDir(modifiers));
							if(tab!=this) {
								if(!onEnter())
									onEscape();
								getBase().setFocus(tab);
								repaint();
							}
						}
						return true;
					case KeyEvent.VK_ENTER:
						if(onEnter())
							getBase().resetFocus();
						repaint();
						return true;
					case KeyEvent.VK_ESCAPE:
						if(onEscape())
							getBase().resetFocus();
						repaint();
						return true;
					default:
						return super.onKeyPressed(c, code, modifiers);
				}
			}
			@Override
			public void onFocusGained() {
				UITextBox.this.onFocusGained();
				super.onFocusGained();
			}
			@Override
			public void onFocusLost() {
				UITextBox.this.onFocusLost();
				super.onFocusLost();
			}
		};
		getBase().tabIndex.add(e);
		return e;
	}

	public void onFocusGained() {
	}

	public void onFocusLost() {
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
