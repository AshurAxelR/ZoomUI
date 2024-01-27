package com.xrbpowered.zoomui.swing;

import static com.xrbpowered.zoomui.MouseInfo.LEFT;
import static com.xrbpowered.zoomui.MouseInfo.RIGHT;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import com.xrbpowered.zoomui.MouseInfo;
import com.xrbpowered.zoomui.UIWindow;
import com.xrbpowered.zoomui.icons.PngIcon;

public class SwingTrayIcon {

	public final TrayIcon trayIcon;
	public final int iconSize;
	
	public UIWindow window;
	public SwingPopup popup;
	
	private PngIcon icon;
	
	public SwingTrayIcon(PngIcon icon, String tooltip, UIWindow window, SwingPopup popup) {
		if(!SystemTray.isSupported())
			throw new UnsupportedOperationException("SystemTray is not supported");
		
		this.window = window;
		this.popup = popup;
		
		this.icon = icon;
		Image img = icon.createImage();
		iconSize = new TrayIcon(img).getSize().width;
		img = icon.createImage(iconSize, -1);
		trayIcon = new TrayIcon(img, tooltip);
		
		trayIcon.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				onMouseDown(BasePanel.getMouseInfo(e), e.getX(), e.getY());
			}
		});

		try {
			SystemTray.getSystemTray().add(trayIcon);
		}
		catch (AWTException e) {
			throw new UnsupportedOperationException("Tray icon could not be added.");
		}
	}
	
	public PngIcon getIcon() {
		return icon;
	}
	
	public void setIcon(PngIcon icon) {
		this.icon = icon;
		trayIcon.setImage(icon.createImage(iconSize, -1));
	}
	
	public void onMouseDown(MouseInfo mouse, int x, int y) {
		switch(mouse.eventButton) {
			case LEFT:
				if(window!=null)
					window.show();
				break;
			case RIGHT:
				if(popup!=null)
					popup.show(null, x, y); // TODO show direction
				break;
			default:
				break;
		}
	}

}
