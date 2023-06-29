package com.xrbpowered.zoomui.examples;

import com.xrbpowered.zoomui.icons.PngIcon;
import com.xrbpowered.zoomui.std.UIMessageBox;
import com.xrbpowered.zoomui.std.UIMessageBox.MessageResult;
import com.xrbpowered.zoomui.std.menu.UIMenu;
import com.xrbpowered.zoomui.std.menu.UIMenuItem;
import com.xrbpowered.zoomui.std.menu.UIMenuSeparator;
import com.xrbpowered.zoomui.swing.SwingFrame;
import com.xrbpowered.zoomui.swing.SwingPopup;
import com.xrbpowered.zoomui.swing.SwingTrayIcon;
import com.xrbpowered.zoomui.swing.SwingWindowFactory;

public class TrayApp {

	public static void main(String[] args) {
		SwingWindowFactory swing = SwingWindowFactory.use();
		
		SwingFrame frame = swing.createFrame("TrayApp", 1200, 600);
		frame.exitOnClose(false);
		
		final SwingPopup popup = new SwingPopup(swing);
		UIMenu menu = new UIMenu(popup.getContainer());
		popup.getContainer().setClientBorder(1, UIMenu.colorBorder);
		new UIMenuItem(menu, "About") {
			@Override
			public void onAction() {
				popup.close();
				UIMessageBox.show("About", "TrayApp version 1.0<br>TrayApp puts an icon to the system tray.",
						UIMessageBox.iconOk, new MessageResult[] {MessageResult.ok},
						null);
			}
		};
		new UIMenuItem(menu, "Disabled item").setEnabled(false);
		new UIMenuSeparator(menu);
		new UIMenuItem(menu, "Exit") {
			@Override
			public void onAction() {
				System.exit(0);
			}
		};
		popup.setClientSizeToContent();
		
		new SwingTrayIcon(new PngIcon("icons/berry.png"), "TrayApp", frame, popup);
		frame.show();
	}

}
