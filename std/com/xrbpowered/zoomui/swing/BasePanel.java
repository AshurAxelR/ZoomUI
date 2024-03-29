package com.xrbpowered.zoomui.swing;

import static com.xrbpowered.zoomui.InputInfo.*;
import static com.xrbpowered.zoomui.MouseInfo.*;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.InputInfo;
import com.xrbpowered.zoomui.MouseInfo;
import com.xrbpowered.zoomui.UIWindow;

public class BasePanel extends JPanel {

	private static Cursor blankCursor = null;
	
	public final UIWindow window;
	
	public BasePanel(final UIWindow window) {
		this.window = window;
		setFocusable(true);
		setFocusTraversalKeysEnabled(false);
		
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				window.notifyResized();
			}
		});
			
		addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
			}
			
			@Override
			public void focusLost(FocusEvent e) {
				// window.getContainer().resetFocus();
			}
		});
		
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(window.getContainer().onKeyPressed(e.getKeyChar(), e.getKeyCode(), getInputInfo(e)))
					e.consume();
			}
		});
		
		addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				window.getContainer().notifyMouseScroll(e.getX(), e.getY(), (float)e.getPreciseWheelRotation(), getMouseInfo(e));
			}
		});
		
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				window.getContainer().notifyMouseDown(e.getX(), e.getY(), getMouseInfo(e));
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				window.getContainer().notifyMouseUp(e.getX(), e.getY(), getMouseInfo(e), null);
			}
			@Override
			public void mouseEntered(MouseEvent e) {
				window.getContainer().onMouseIn();
			}
			@Override
			public void mouseExited(MouseEvent e) {
				window.getContainer().onMouseOut();
			}
		});
		
		addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				window.getContainer().onMouseDragged(e.getX(), e.getY(), getMouseInfo(e));
			}
			
			@Override
			public void mouseMoved(MouseEvent e) {
				window.getContainer().onMouseMoved(e.getX(), e.getY(), getMouseInfo(e));
			}
		});
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		window.getContainer().paint(new GraphAssist((Graphics2D) g));
	}
	
	@Override
	public void resize(int width, int height) {
		float scale = window.getContainer().getBaseScale();
		setPreferredSize(new Dimension((int)(width*scale), (int)(height*scale)));
		Window javaWindow = SwingUtilities.getWindowAncestor(this);
		if(javaWindow!=null)
			javaWindow.pack();
		window.notifyResized();
	}
	
	private Point pt = new Point();
	
	public int baseToScreenX(float x) {
		pt.setLocation(x, 0);
		SwingUtilities.convertPointToScreen(pt, this);
		return pt.x;
	}
	
	public int baseToScreenY(float y) {
		pt.setLocation(0, y);
		SwingUtilities.convertPointToScreen(pt, this);
		return pt.y;
	}
	
	public float screenToBaseX(int x) {
		pt.setLocation(x, 0);
		SwingUtilities.convertPointFromScreen(pt, this);
		return pt.x;
	}
	
	public float screenToBaseY(int y) {
		pt.setLocation(0, y);
		SwingUtilities.convertPointFromScreen(pt, this);
		return pt.y;
	}
	
	@Override
	public void setCursor(Cursor cursor) {
		if(cursor==null) {
			if(blankCursor==null) {
				blankCursor = getToolkit().createCustomCursor(
					new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB),
					new Point(),
					null
				);
			}
			cursor = blankCursor;
		}
		super.setCursor(cursor);
	}
	
	private static int getMods(InputEvent e) {
		int m = e.getModifiersEx();
		int mods = NONE;
		if((m & InputEvent.CTRL_DOWN_MASK) != 0)
			mods |= CTRL;
		if((m & InputEvent.ALT_DOWN_MASK) != 0)
			mods |= ALT;
		if((m & InputEvent.SHIFT_DOWN_MASK) != 0)
			mods |= SHIFT;
		return mods;
	}

	public static InputInfo getInputInfo(InputEvent e) {
		return new InputInfo(getMods(e));
	}

	public static MouseInfo getMouseInfo(MouseEvent e) {
		int eventButton;
		switch(e.getButton()) {
			case MouseEvent.NOBUTTON:
				eventButton = NONE;
				break;
			case MouseEvent.BUTTON1:
				eventButton = LEFT;
				break;
			case MouseEvent.BUTTON2:
				eventButton = MIDDLE;
				break;
			case MouseEvent.BUTTON3:
				eventButton = RIGHT;
				break;
			case 4:
				eventButton = BUTTON4;
				break;
			case 5:
				eventButton = BUTTON5;
				break;
			default:
				eventButton = UNKNOWN;
				break;
		}
		
		int m = e.getModifiersEx();
		int buttons = NONE;
		if((m & InputEvent.BUTTON1_DOWN_MASK) != 0)
			buttons |= LEFT;
		if((m & InputEvent.BUTTON2_DOWN_MASK) != 0)
			buttons |= MIDDLE;
		if((m & InputEvent.BUTTON3_DOWN_MASK) != 0)
			buttons |= RIGHT;
		if((m & InputEvent.getMaskForButton(4)) != 0)
			buttons |= BUTTON4;
		if((m & InputEvent.getMaskForButton(5)) != 0)
			buttons |= BUTTON4;
		
		return new MouseInfo(eventButton, buttons, getMods(e), e.getClickCount());
	}
}
