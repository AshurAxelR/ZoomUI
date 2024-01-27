package com.xrbpowered.zoomui.base;

import com.xrbpowered.zoomui.DragActor;
import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.MouseInfo;
import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.UIElement;

/**
 * UI container that allows the user to pan the view using a mouse-drag action.
 * Child elements are graphically clipped to this container (overflow hidden).
 * 
 * <p>Pan view introduces another layer of coordinates called <i>transformed</i> local space,
 * which applies an affine transform (e.g., translation for panning) to the normal (untransformed) local space.
 * Subclasses can override {@link #applyTransform(GraphAssist)} to perform other types of transform,
 * for instance, {@link UIZoomView} uses this method to apply both scaling and translation.</p>
 * 
 * <p>Background and foreground content of the view are drawn in the untransformed local space, and
 * child elements are drawn in the transformed local space. The drawing order is as follows:</p>
 * <ul>
 * <li>{@link #paintBackground(GraphAssist)} in untransformed local space.</li>
 * <li>{@link UIContainer#paintChildren(GraphAssist)} in transformed local space.</li>
 * <li>{@link #paintForeground(GraphAssist)} in untransformed local space.</li>
 * </ul> 
 * 
 * <p>Panning limits can be set using {@link #setPanRange(int, int)} or {@link #setPanRangeForClient(float, float)}.
 * The {@link #DISABLED} constant can be used to create a vertical-only or horizontal-only pan view, or to temporarily disable panning.</p>
 * 
 * <p>Default mouse button for panning is the right mouse button. Subclasses can override {@link #isPanTrigger(MouseEvent)}
 * to change the button or add modifier keys.</p>
 * 
 * @see UIZoomView
 * 
 */
public class UIPanView extends UIContainer {

	/**
	 * Panning limit is turned off, used for {@link #setPanRange(int, int)}.
	 */
	public static final int UNLIMITED = -1;

	/**
	 * Panning is disabled (locked at zero), used for {@link #setPanRange(int, int)}.
	 */
	public static final int DISABLED = 0;

	/**
	 * Drag action handler for panning.
	 */
	private DragActor panActor = new DragActor() {
		@Override
		public boolean notifyMouseDown(float x, float y, MouseInfo mouse) {
			if(isPanTrigger(mouse)) {
				return true;
			}
			return false;
		}

		@Override
		public boolean notifyMouseMove(float rx, float ry, float drx, float dry, MouseInfo mouse) {
			float pix = getPixelSize();
			pan(drx * pix, dry * pix);
			repaint();
			return true;
		}

		@Override
		public void notifyMouseUp(float rx, float ry, MouseInfo mouse, UIElement target) {
			// do nothing
		}
	};

	/**
	 * Current horizontal pan. Visible to subclasses for convenience.
	 * @see #getPanX()
	 */
	protected float panX = 0;

	/**
	 * Current vertical pan. Visible to subclasses for convenience.
	 * @see #getPanY()
	 */
	protected float panY = 0;

	/**
	 * Horizontal pan limit. See {@link #getMaxPanX()}
	 */
	protected int maxPanX = UNLIMITED;

	/**
	* Vertical pan limit. See {@link #getMaxPanY()}
	*/
	protected int maxPanY = UNLIMITED;

	/**
	* Constructor, see {@link UIElement#UIElement(UIContainer)}.
	* @param parent parent container
	*/
	public UIPanView(UIContainer parent) {
		super(parent);
	}

	/**
	 * Determines if the mouse-down event is a trigger for panning in terms of pressed buttons and key modifiers.
	 * @param button mouse button of the related mouse-down event
	 * @param mods status of the modifier keys
	 * @return <code>true</code> if the event is a pan trigger, <code>false</code> if it is not
	 */
	protected boolean isPanTrigger(MouseInfo mouse) {
		return (mouse.eventButton==MouseInfo.RIGHT);
	}

	private void applyPanLimits() {
		if(maxPanX!=DISABLED) {
			if(maxPanX>0) {
				if(panX<0)
					panX = 0f;
				if(panX>maxPanX)
					panX = maxPanX;
			}
		}
		else {
			panX = 0;
		}
		if(maxPanY!=DISABLED) {
			if(maxPanY>0) {
				if(panY<0)
					panY = 0f;
				if(panY>maxPanY)
					panY = maxPanY;
			}
		}
		else {
			panY = 0;
		}
	}

	/**
	 * Pans to a specific location.
	 * @param x new horizontal pan value
	 * @param y new vertical pan value
	 * @see #pan(float, float)
	 * @see #resetPan()
	 */
	public void setPan(float x, float y) {
		panX = x;
		panY = y;
		applyPanLimits();
	}

	/**
	 * Pans by a given offset amount.
	 * @param dx change in the horizontal pan value
	 * @param dy change in the vertical pan value
	 * @see #setPan(float, float)
	 * @see #resetPan()
	 */
	public void pan(float dx, float dy) {
		panX -= dx;
		panY -= dy;
		applyPanLimits();
	}

	/**
	 * Pans to <code>(0, 0)</code>.
	 * @see #setPan(float, float)
	 */
	public void resetPan() {
		panX = 0f;
		panY = 0f;
	}

	/**
	 * Returns current horizontal pan value.
	 * @return horizontal pan
	 */
	public float getPanX() {
		return panX;
	}

	/**
	 * Returns current vertical pan value.
	 * @return vertical pan
	 */
	public float getPanY() {
		return panY;
	}

	/**
	 * Returns the current horizontal pan limit. 
	 * @return maximum allowed pan value if the limit is set, {@link #DISABLED} if the panning is blocked,
	 * or {@link #UNLIMITED} if the limit is not set
	 * @see #setPanRange(int, int)
	 */
	public int getMaxPanX() {
		return maxPanX;
	}

	/**
	 * Returns the current vertical pan limit. 
	 * @return maximum allowed pan value if the limit is set, {@link #DISABLED} if the panning is blocked,
	 * or {@link #UNLIMITED} if the limit is not set
	 * @see #setPanRange(int, int)
	 */
	public int getMaxPanY() {
		return maxPanY;
	}

	/**
	 * Sets the range limit for panning.
	 * <p>Horizontal and vertical limits are configured independently using one of the following values:</p>
	 * <ul>
	 * <li>{@link #UNLIMITED} or any negative value removes the limit so the user can pan indefinitely in that direction.</li>
	 * <li>{@link #DISABLED} or 0 blocks panning in that direction (locks at zero).</li>
	 * <li>any positive value: panning is allowed in the range from 0 to the given value.</li>
	 * </ul>
	 * 
	 * @param h horizontal pan limit
	 * @param v vertical pan limit
	 * 
	 * @see #getMaxPanX()
	 * @see #getMaxPanY()
	 */
	public void setPanRange(int h, int v) {
		this.maxPanX = h;
		this.maxPanY = v;
		applyPanLimits();
	}

	/**
	 * Sets the pan range to provide panning for a client area of a given size based on the current size of this container.
	 * 
	 * <p>Pan range is not updated automatically when the container is resized. Therefore,
	 * this method may need to be called from {@link #layout()} to ensure that the range is always up to date.</p>
	 * 
	 * <p>The top-left corner of the client area is assumed to be at <code>(0, 0)</code>.
	 * If the size of the client area is smaller than this view in one or both of the dimensions,
	 * the panning will be respectively blocked in a required direction.</p>
	 * 
	 * 
	 * @param width client area width
	 * @param height client area height
	 * 
	 * @see #setPanRange(int, int)
	 */
	public void setPanRangeForClient(float width, float height) {
		int h = (int) (width - getWidth());
		if(h<0)
			h = DISABLED;
		int v = (int) (height - getHeight());
		if(v<0)
			v = DISABLED;
		setPanRange(h, v);
	}

	@Override
	protected float parentToLocalX(float px) {
		return super.parentToLocalX(px) + panX;
	}

	@Override
	protected float parentToLocalY(float py) {
		return super.parentToLocalY(py) + panY;
	}

	@Override
	protected float localToParentX(float x) {
		return super.localToParentX(x - panX);
	}

	@Override
	protected float localToParentY(float y) {
		return super.localToParentY(y - panY);
	}

	/**
	 * Updates the transform in <code>g</code> to convert to the transformed local space.
	 * This method does not need to save the old transform.
	 * @param g wrapper for {@link Graphics2D} canvas in local coordinates
	 */
	protected void applyTransform(GraphAssist g) {
		g.translate(-panX, -panY);
	}

	@Override
	protected void paintChildren(GraphAssist g) {
		if(g.pushClip(0, 0, getWidth(), getHeight())) {
			g.pushTx();
			applyTransform(g);
			super.paintChildren(g);
			g.popTx();
			g.popClip();
		}
	}

	@Override
	public UIElement getElementAt(float px, float py) {
		if(isInside(px, py))
			return super.getElementAt(px, py);
		else
			return null;
	}

	@Override
	public UIElement notifyMouseDown(float px, float py, MouseInfo mouse) {
		if(isInside(px, py))
			return super.notifyMouseDown(px, py, mouse);
		else
			return null;
	}

	@Override
	public UIElement notifyMouseUp(float px, float py, MouseInfo mouse, UIElement initiator) {
		if(isInside(px, py))
			return super.notifyMouseUp(px, py, mouse, initiator);
		else
			return null;
	}

	@Override
	public UIElement notifyMouseScroll(float px, float py, float delta, MouseInfo mouse) {
		if(isInside(px, py))
			return super.notifyMouseScroll(px, py, delta, mouse);
		else
			return null;
	}

	@Override
	public DragActor acceptDrag(float x, float y, MouseInfo mouse) {
		if(panActor.notifyMouseDown(x, y, mouse))
			return panActor;
		else
			return null;
	}

	@Override
	public boolean onMouseDown(float x, float y, MouseInfo mouse) {
		if(isPanTrigger(mouse))
			return true;
		return false;
	}

}
