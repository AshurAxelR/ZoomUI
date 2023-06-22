package com.xrbpowered.zoomui.examples;

import com.xrbpowered.zoomui.UIWindow;
import com.xrbpowered.zoomui.std.colors.UIColorView;
import com.xrbpowered.zoomui.swing.SwingWindowFactory;

public class ColorBrowser {
	public static void main(String[] args) {
		UIWindow frame = SwingWindowFactory.use().createFrame("ColorBrowser", 500, 400);
		new UIColorView(frame.getContainer());
		frame.show();
	}
}
