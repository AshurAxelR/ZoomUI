package com.xrbpowered.zoomui.swing;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import com.xrbpowered.zoomui.UIPopupWindow;
import com.xrbpowered.zoomui.UIWindow;

public class SwingPopup extends UIPopupWindow {

	public final JPopupMenu popup;
	public final BasePanel panel;

	public SwingPopup(SwingWindowFactory factory) {
		super(factory);
		// consume click that caused popup to close so it doesn't act on other components below
		UIManager.put("PopupMenu.consumeEventOnClose", Boolean.TRUE);
		
		popup = new JPopupMenu();
		popup.setBorder(BorderFactory.createEmptyBorder());
		popup.setLayout(new BorderLayout());
		
		panel = new BasePanel(this);
		popup.add(panel, BorderLayout.CENTER);
		
		popup.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
			}
			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				onClose();
			}
			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
			}
		});
	}

	@Override
	public int getClientWidth() {
		return panel.getWidth();
	}

	@Override
	public int getClientHeight() {
		return panel.getHeight();
	}

	@Override
	public void setClientSize(int width, int height) {
		panel.resize(width, height);
	}
	
	@Override
	public int getX() {
		return popup.getX();
	}
	
	@Override
	public int getY() {
		return popup.getY();
	}
	
	@Override
	public void moveTo(int x, int y) {
		popup.setLocation(x, y);
	}

	@Override
	public void center() {
	}

	@Override
	public void show(UIWindow invoker, float x, float y) {
		BasePanel panel = SwingWindowFactory.getBasePanel(invoker);
		
		if(panel==null) {
			// required to catch non-client clicks if invoker is null
			JFrame f = new JFrame();
			f.setType(JFrame.Type.UTILITY);
			f.setUndecorated(true);
			f.setOpacity(0.0f);
			f.addFocusListener(new FocusListener() {
				@Override
				public void focusGained(FocusEvent e) {
				}
				@Override
				public void focusLost(FocusEvent e) {
					if(popup.isVisible())
						SwingPopup.this.close();
					f.dispose();
				}
			});
			f.setVisible(true);
		}
		popup.show(panel, (int)x, (int)y);
	}

	@Override
	public void repaint() {
		panel.repaint();
	}

	@Override
	public void setCursor(Cursor cursor) {
		panel.setCursor(cursor);
	}
	
	@Override
	public void close() {
		popup.setVisible(false);
	}

	@Override
	public int baseToScreenX(float x) {
		return panel.baseToScreenX(x);
	}

	@Override
	public int baseToScreenY(float y) {
		return panel.baseToScreenY(y);
	}

	@Override
	public float screenToBaseX(int x) {
		return panel.screenToBaseX(x);
	}

	@Override
	public float screenToBaseY(int y) {
		return panel.screenToBaseY(y);
	}

	@Override
	public FontMetrics getFontMetrics(Font font) {
		return panel.getFontMetrics(font);
	}
	
	public boolean isVisible() {
		return popup.isVisible();
	}

}
