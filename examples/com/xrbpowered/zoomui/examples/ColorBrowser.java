package com.xrbpowered.zoomui.examples;

import java.awt.Color;

import com.xrbpowered.zoomui.UIModalWindow;
import com.xrbpowered.zoomui.UIModalWindow.ResultHandler;
import com.xrbpowered.zoomui.std.colors.UIColorBrowser;
import com.xrbpowered.zoomui.swing.SwingWindowFactory;

public class ColorBrowser {
	public static void main(String[] args) {
		UIModalWindow<Color> frame = SwingWindowFactory.use().createModal("Pick color", UIColorBrowser.defaultWidth, UIColorBrowser.defaultHeight, false, null);
		frame.onResult = new ResultHandler<Color>() {
			@Override
			public void onResult(Color result) {
				System.out.printf("#%08x\n", result.getRGB());
				System.exit(0);
			}
			@Override
			public void onCancel() {
				System.out.println("Cancelled");
				System.exit(1);
			}
		};
		new UIColorBrowser(frame.getContainer(), frame.wrapInResultHandler());
		frame.show();
	}
}
