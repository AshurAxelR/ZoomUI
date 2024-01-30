package com.xrbpowered.zoomui.base;

import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.MouseInfo;
import com.xrbpowered.zoomui.UIContainer;

/**
 * UI container that allows the user to zoom in and out. Also inherits all panning functionality from {@link UIPanView}.
 * Child elements are graphically clipped to this container (overflow hidden).
 * 
 * <p>Panning functions {@link #setPan(float, float)}, {@link #pan(float, float)}, {@link #getPanX()}, and {@link #getPanY()}
 * operate in the current scale, while pan range functions {@link #setPanRange(int, int)}, {@link #setPanRangeForClient(float, float)},
 * {@link #getMaxPanX()}, and {@link #getMaxPanY()} are independent of the scale  and always use the identity scale of 1.</p>
 *
 * <p>By default, the zooming is done using Ctrl + mouse-scroll. Subclasses can override {@link #isScrollTrigger(MouseInfo)}
 * to change the modifier key. Zooming by mouse-dragging is not implemented in this class.</p>
 */
public class UIZoomView extends UIPanView {

	/**
	 * Current scaling factor (zoom). Visible to subclasses for convenience.
	 * @see #getScale()
	 */
	protected float scale = 1f; // TODO integer zoom steps

	private float minScale = 0.1f;
	private float maxScale = 3.0f;

	/**
	 * Constructor, see {@link UIElement#UIElement(UIContainer)}.
	 * @param parent parent container
	 */
	public UIZoomView(UIContainer parent) {
		super(parent);
	}

	/**
	 * Determines if the mouse-scroll event is a trigger for zooming based on the modifier keys.
	 * <p>By default, the zoom is triggered if the Control key is down. Subclasses can override this behaviour.</p> 
	 * @param mouse mouse button state and modifier key information of the related mouse-scroll event
	 * @return <code>true</code> if the event is a pan trigger, <code>false</code> if it is not
	 */
	protected boolean isScrollTrigger(MouseInfo mouse) {
		return mouse.mods==MouseInfo.CTRL;
	}

	private void applyScaleLimits() {
		if(scale<minScale)
			scale = minScale;
		if(scale>maxScale)
			scale = maxScale;
	}

	/**
	 * Sets the minimum and maximum allowed zoom levels.
	 * The value for <code>min</code> should be less or equal to 1, and <code>max</code> should be greater or equal to 1.
	 * Invalid values are clamped, and no exception is thrown.
	 * 
	 * @param min minimum scaling factor
	 * @param max maximum scaling factor
	 */
	public void setScaleRange(float min, float max) {
		this.minScale = (min>1f) ? 1f : min;
		this.maxScale = (max<1f) ? 1f : max;
		applyScaleLimits();
	}

	/**
	 * Resets scaling factor to 1.
	 */
	public void resetScale() {
		scale = 1f;
		applyScaleLimits();
	}

	/**
	 * Sets the current zoom level.
	 * The scaling factor is automatically clamped between the minimum and maximum levels.
	 * @param s new scaling factor
	 * @see #setScaleRange(float, float)
	 */
	public void setScale(float s) {
		scale = s;
		applyScaleLimits();
	}

	/**
	 * Changes current zoom level by multiplying scaling factor with a given value;
	 * the origin is the centre of this view.
	 * @param ds zoom multiplier
	 * @see #rescale(float, float, float)
	 */
	public void rescale(float ds) {
		rescale(ds, parentToLocalX(getWidth() / 2f), parentToLocalY(getHeight() / 2f));
	}

	/**
	 * Changes current zoom level by multiplying scaling factor with a given value;
	 * the origin point is specified.
	 * @param ds zoom multiplier
	 * @param x coordinate of the origin
	 * @param y coordinate of the origin
	 * @see #rescale(float)
	 */
	public void rescale(float ds, float x, float y) {
		float s = scale;
		scale *= ds;
		applyScaleLimits();
		super.pan((s - scale) * x, (s - scale) * y);
	}

	/**
	 * Returns current zoom (scaling factor).
	 * @return scaling factor
	 */
	public float getScale() {
		return scale;
	}

	/**
	 * Returns the minimum allowed scaling factor.
	 * @return scaling limit
	 * @see #setScaleRange(float, float)
	 */
	public float getMinScale() {
		return minScale;
	}

	/**
	 * Returns the maximum allowed scaling factor.
	 * @return scaling limit
	 * @see #setScaleRange(float, float)
	 */
	public float getMaxScale() {
		return maxScale;
	}

	@Override
	public void setPan(float x, float y) {
		super.setPan(x * scale, y * scale);
	}

	@Override
	public void pan(float dx, float dy) {
		super.pan(dx * scale, dy * scale);
	}

	@Override
	public float getPanX() {
		return super.getPanX() / scale;
	}

	@Override
	public float getPanY() {
		return super.getPanY() / scale;
	}

	@Override
	public float getPixelSize() {
		return super.getPixelSize() / scale;
	}

	@Override
	protected float parentToLocalX(float px) {
		return super.parentToLocalX(px) / scale;
	}

	@Override
	protected float parentToLocalY(float py) {
		return super.parentToLocalY(py) / scale;
	}

	@Override
	protected float localToParentX(float x) {
		return super.localToParentX(x * scale);
	}

	@Override
	protected float localToParentY(float y) {
		return super.localToParentY(y * scale);
	}

	@Override
	protected void applyTransform(GraphAssist g) {
		super.applyTransform(g);
		g.scale(scale);
	}

	@Override
	public boolean onMouseScroll(float x, float y, float delta, MouseInfo mouse) {
		if(isScrollTrigger(mouse)) {
			rescale(1.0f + delta * 0.2f, x, y);
			repaint();
			return true;
		}
		else
			return false;
	}
}
