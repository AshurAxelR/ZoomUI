package com.xrbpowered.zoomui;

import java.awt.Color;
import java.awt.RenderingHints;

import com.xrbpowered.zoomui.base.UILayersContainer;

public class RootContainer extends UILayersContainer implements Measurable {

	public static class ModalRootContainer<A> extends RootContainer {
		protected ModalRootContainer(UIModalWindow<A> window, float scale) {
			super(window, scale);
		}
		@SuppressWarnings("unchecked")
		@Override
		public UIModalWindow<A> getWindow() {
			return (UIModalWindow<A>) super.getWindow();
		}
	}
	
	public KeyInputHandler hotKeys = null;
	
	private float baseScale;
	private UIWindow window;
	private TabIndex tabIndex;
	
	protected RootContainer(UIWindow window, float scale) {
		super(null);
		this.baseScale = scale;
		this.window = window;
		this.tabIndex = new TabIndex(this);
	}

	public RootContainer replaceTabIndex(TabIndex tabIndex) {
		tabIndex.copyState(this.tabIndex);
		this.tabIndex = tabIndex;
		return this;
	}
	
	@Override
	public RootContainer getRoot() {
		return this;
	}
	
	public UIWindow getWindow() {
		return window;
	}
	
	public TabIndex tabIndex() {
		return tabIndex;
	}
	
	private UIElement uiUnderMouse = null;
	
	private DragActor drag = null;
	private UIElement uiInitiator = null;
	private MouseInfo initiatorInfo = null;
	private int prevMouseX = 0;
	private int prevMouseY = 0;
	
	protected boolean invalidLayout = true;

	private int clientBorderWidth = 0;
	private Color clientBorderColor = null;
	
	public void setClientBorder(int thickness, Color color) {
		this.clientBorderWidth = thickness;
		this.clientBorderColor = color;
	}
	
	public void removeClientBorder() {
		this.clientBorderWidth = 0;
		this.clientBorderColor = null;
	}
	
	@Override
	public void repaint() {
		window.repaint();
	}
	
	public void invalidateLayout() {
		invalidLayout = true;
	}

    /**
     * Determines if there is an active mouse-drag activity.
     * @return <code>true</code> if a drag action is in progress, otherwise <code>false</code>.
     */
    public boolean isDragActive() {
        return drag != null;
    }
    
	public boolean onKeyPressed(char c, int code, InputInfo input) {
		tabIndex.validate();
		if(tabIndex.hasFocus() && tabIndex.getFocus().onKeyPressed(c, code, input))
			return true;
		else if(hotKeys!=null && hotKeys.onKeyPressed(c, code, input))
			return true;
		else
			return tabIndex.onKeyPressed(c, code, input);
	}
	
	@Override
	public UIElement notifyMouseDown(float px, float py, MouseInfo mouse) {
		if(!isDragActive()) {
			prevMouseX = getWindow().rootToScreenX(px);
			prevMouseY = getWindow().rootToScreenY(py);
			UIElement ui = super.notifyMouseDown(px, py, mouse);
			if(ui!=uiInitiator && uiInitiator!=null)
				uiInitiator.onMouseReleased();
			uiInitiator = ui;
			initiatorInfo = mouse;
			//if(uiFocused!=null && uiFocused!=uiInitiator) // FIXME better strategy for losing focus?
			//	resetFocus();
		}
		return this;
	}
	
	@Override
	public UIElement notifyMouseUp(float px, float py, MouseInfo mouse, UIElement initiator) {
		if(isDragActive()) {
			UIElement ui = getElementAt(px, py);
			drag.notifyMouseUp(px, py, mouse, ui);
			drag = null;
		}
		else {
			if(super.notifyMouseUp(px, py, mouse, uiInitiator)!=uiInitiator && uiInitiator!=null)
				uiInitiator.onMouseReleased();
		}
		initiatorInfo = null;
		return this;
	}
	
	@Override
	public void onMouseOut() {
		if(!isDragActive() && uiUnderMouse!=null) {
			if(uiUnderMouse!=this)
				uiUnderMouse.onMouseOut();
			uiUnderMouse = null;
		}
	}
	
	private void updateMouseMove(float x, float y, MouseInfo mouse) {
		UIElement ui = getElementAt(x, y);
		if(ui!=uiUnderMouse) {
			if(uiUnderMouse!=null && uiUnderMouse!=this)
				uiUnderMouse.onMouseOut();
			uiUnderMouse = ui;
			if(uiUnderMouse!=null && uiUnderMouse!=this)
				uiUnderMouse.onMouseIn();
		}
		if(!isDragActive() && uiUnderMouse!=null && uiUnderMouse!=this)
			uiUnderMouse.onMouseMoved(uiUnderMouse.rootToLocalX(x), uiUnderMouse.rootToLocalY(y), mouse);
	}
	
	@Override
	public void onMouseMoved(float x, float y, MouseInfo mouse) {
		if(!isDragActive())
			updateMouseMove(x, y, mouse);
	}
	
	public void onMouseDragged(float x, float y, MouseInfo mouse) {
		if(!isDragActive() && uiInitiator!=null && initiatorInfo!=null) {
			float rx = getWindow().screenToRootX(prevMouseX);
			float ry = getWindow().screenToRootY(prevMouseY);
			drag = uiInitiator.acceptDrag(uiInitiator.rootToLocalX(rx), uiInitiator.rootToLocalY(ry), initiatorInfo);
		}
		if(isDragActive()) {
			int px = prevMouseX;
			int py = prevMouseY;
			prevMouseX = getWindow().rootToScreenX(x);
			prevMouseY = getWindow().rootToScreenY(y);

			// (prevMouseX, prevMouseY) contains the current mouse position at this point;
			// (px, py) is the previous position
			if(!drag.notifyMouseMove(x, y, prevMouseX - px, prevMouseY - py, mouse))
				drag = null;
		}
		updateMouseMove(x, y, mouse);
	}
	
	public void resetFocus() {
		tabIndex.resetFocus();
	}

	public void setFocus(KeyInputHandler handler) {
		tabIndex.setFocus(handler);
	}
	
	public KeyInputHandler getFocus() {
		return tabIndex.getFocus();
	}

	public float getBaseScale() {
		return baseScale;
	}
	
	public void setBaseScale(float scale) {
		this.baseScale = (scale>0f) ? scale : getWindow().getFactory().getBaseScale();
		invalidateLayout();
	}
	
	@Override
	public float getPixelSize() {
		return getWindow().getFactory().globalPixelScale() / baseScale;
	}
	
	@Override
	protected float parentToLocalX(float px) {
		return px / baseScale;
	}
	
	@Override
	protected float parentToLocalY(float py) {
		return py / baseScale;
	}
	
	@Override
	protected float localToParentX(float x) {
		return x * baseScale;
	}

	@Override
	protected float localToParentY(float y) {
		return y * baseScale;
	}

	@Override
	public void layout() {
		super.layout();
		invalidLayout = false;
	}
	
	@Override
	public float measureWidth() {
		float max = 0;
		for(UIElement c : children) {
			if(c instanceof Measurable) {
				float w = ((Measurable) c).measureWidth();
				if(w>max)
					max = w;
			}
		}
		return max;
	}
	
	@Override
	public float measureHeight() {
		float max = 0;
		for(UIElement c : children) {
			if(c instanceof Measurable) {
				float h = ((Measurable) c).measureHeight();
				if(h>max)
					max = h;
			}
		}
		return max;
	}
	
	@Override
	public float getWidth() {
		return getWindow().getClientWidth() / baseScale;
	}
	
	@Override
	public float getHeight() {
		return getWindow().getClientHeight() / baseScale;
	}
	
	@Override
	public float getX() {
		return 0;
	}
	
	@Override
	public float getY() {
		return 0;
	}
	
	@Override
	public boolean isInside(float px, float py) {
		return true;
	}
	
	@Override
	public void paint(GraphAssist g) {
		if(invalidLayout)
			layout();
		if(g.graph!=null) {
			g.graph.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
			g.graph.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.graph.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
		}
		super.paint(g);
		
		if(clientBorderWidth>0 && clientBorderColor!=null && g.graph!=null) {
			g.graph.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			g.graph.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
			GraphAssist.pixelRect(g.graph, 0, 0, window.getClientWidth(), window.getClientHeight(), clientBorderWidth, clientBorderColor);
		}
	}
	
	@Override
	protected void paintChildren(GraphAssist g) {
		g.pushTx();
		g.scale(baseScale);
		super.paintChildren(g);
		g.popTx();
	}
	
}
