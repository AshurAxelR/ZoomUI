package com.xrbpowered.zoomui;

import java.awt.Color;
import java.awt.RenderingHints;

import com.xrbpowered.zoomui.base.UILayersContainer;

/**
 * Root UI container for a zoomable UI hierarchy. Root container can be placed inside a Swing window using {@link RootPanel}.
 * 
 * <p>Root container is automatically created when a window is created, and it cannot be replaced.
 * Instead, one should add or remove children of the root container.
 * Root container layouts its immediate children to fill its client area as {@link UILayersContainer}.</p>
 * 
 * <p><i>Base scale</i> is the UI scaling factor that determines the ratio between the logical (scaled) UI coordinates
 * and pixel coordinates in the parent (e.g., Swing) window. Root container's local space is in logical (scaled) units,
 * so {@link #getWidth()} and {@link #getHeight()} can be used to determine child elements' sizes.
 * This is different from the so-called <i>root space</i>, which corresponds to one-to-one pixel ratio.
 * Root space is what {@link #localToRootX(float)}, {@link #localToRootY(float)}, {@link #rootToLocalX(float)}, and
 * {@link #rootToLocalY(float)} work with. Root space can be considered a parent to root container's local space.</p>
 * 
 * @see UIWindow
 * @see UIContainer
 *
 */
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

	/**
	 * Reference to the parent window.
	 */
	private final UIWindow window;

	public final HotKeyMap hotKeys;

	public final TabIndex tabIndex;

	/**
	 * UI scaling factor.
	 * @see #getBaseScale()
	 */
	private float baseScale;

	private UIElement uiUnderMouse = null;

	private DragActor drag = null;
	private UIElement uiInitiator = null;
	private MouseInfo initiatorInfo = null;

	/**
	 * Last seen mouse position in screen space. Used for drag calculation.
	 * Screen space is more persistent than root space as the root container may change position during drag.
	 */
	private int prevMouseX = 0;
	private int prevMouseY = 0;

	/**
	 * Layout has been invalidated and needs to be recalculated.
	 * Recalculation using {@link #layout()} is automatically called before {@link #paint(GraphAssist)} 
	 * if this flag is set.
	 */
	protected boolean invalidLayout = true;

	private int clientBorderWidth = 0;

	private Color clientBorderColor = null;

	protected RootContainer(UIWindow window, float scale) {
		super(null);
		this.window = window;
		this.hotKeys = new HotKeyMap();
		this.tabIndex = new TabIndex(this);
		this.baseScale = scale;
	}

	@Override
	public RootContainer getRoot() {
		return this;
	}

	/**
	 * Returns parent window.
	 * @return parent window
	 */
	public UIWindow getWindow() {
		return window;
	}

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
		return drag!=null;
	}

	/**
	 * Cancels any active drag actions.
	 * Calls {@link DragActor#onDragCancel(float, float)} for the active drag action.
	 * Does nothing if there is no active drag action.
	 */
	public void cancelDrag() {
		if(isDragActive()) {
			float x = getWindow().screenToRootX(prevMouseX);
			float y = getWindow().screenToRootY(prevMouseY);
			drag.onDragCancel(x, y);
			drag = null;
		}
		initiatorInfo = null;
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
			drag.onDragFinish(px, py, mouse, ui);
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
			if(!drag.onMouseDrag(x, y, prevMouseX - px, prevMouseY - py, mouse))
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

	/**
	 * Returns the UI scaling factor. Base scale is the ratio between logical UI sizes and pixel sizes.
	 * It is reciprocal to root container's {@link #getPixelSize()}.
	 * @return base scale
	 */
	public float getBaseScale() {
		return baseScale;
	}

	/**
	 * Sets a new UI scaling factor for this UI tree.
	 * <p>Calls {@link #invalidateLayout()} automatically.</p>
	 * @param scale new base scale if greater than zero, otherwise the default from {@link UIWindowFactory#getBaseScale()} will be used 
	 */
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
	public void setSize(float width, float height) {
		// the size of root container is determined by the client size of its parent window
		throw new UnsupportedOperationException();
	}

	@Override
	public void setPosition(float x, float y) {
		// the position of root container is fixed at (0, 0)
		throw new UnsupportedOperationException();
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
			g.graph.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
			g.graph.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.graph.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
		}
		super.paint(g);

		if(clientBorderWidth>0 && clientBorderColor!=null && g.graph!=null) {
			g.graph.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			g.graph.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
			GraphAssist.pixelRect(g.graph, 0, 0, window.getClientWidth(), window.getClientHeight(), clientBorderWidth,
					clientBorderColor);
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
