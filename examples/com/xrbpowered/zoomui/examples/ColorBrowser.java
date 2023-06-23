package com.xrbpowered.zoomui.examples;

import java.awt.Color;

import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.UIWindow;
import com.xrbpowered.zoomui.std.colors.UIColorView;
import com.xrbpowered.zoomui.swing.SwingWindowFactory;

public class ColorBrowser {
	public static void main(String[] args) {
		UIWindow frame = SwingWindowFactory.use().createFrame("ColorBrowser", 500, 400);
		new UIColorView(frame.getContainer()) {
			@Override
			protected void paintSelf(GraphAssist g) {
				g.fill(this, new Color(0xf2f2f2));
				g.hborder(this, GraphAssist.TOP, UIColorView.colorBorder);
			}
		};
		frame.show();
	}
}
