package com.xrbpowered.zoomui;

/**
 * An interface for receiving mouse-drag events.
 * 
 * <p>All drag events are sent to the UI element, who returned this interface instance from
 * {@link UIElement#acceptDrag(float, float, MouseInfo)} method.</p>
 * 
 * <p>It is important to note that <code>DragActor</code> works in root container (pixel) coordinates,
 * except for {@link #startDrag(float, float, MouseInfo)} method, which is in local space for practical reasons.</p>
 * <ul>
 * <li>In order to convert to local, use {@link UIElement#rootToLocalX(float)} and {@link UIElement#rootToLocalY(float)}.</li>
 * <li>In order to convert deltas to local deltas, multiply by {@link UIElement#getPixelSize()}.</li>
 * </ul>
 * 
 * <p>All event handler methods are called from the UI thread, therefore can request {@link UIElement#repaint()}
 * if needed.</p>
 * 
 * @see UIElement#acceptDrag(float, float, MouseInfo)
 *
 */
public interface DragActor {

	/**
	 * Requests to start a new drag action. The method should return <code>true</code> if it accepts the drag action.
	 * 
	 * <p>Mouse position is in the local space of the sender UI element.</p>
	 * 
	 * <p>The function is not called automatically. A typical use is to call from
	 * {@link UIElement#acceptDrag(float, float, MouseInfo)} assuming you have an instance of <code>DragActor</code>
	 * called <code>dragActor</code>:</p>
	 *<pre>
	 *{@literal @}Override
	 *public DragActor acceptDrag(float x, float y, MouseInfo mouse) {
	 *    if(dragActor.startDrag(x, y, mouse))
	 *        return dragActor;
	 *    else
	 *        return null;
	 *}
	 *</pre>
	 * 
	 * @param x horizontal mouse position of the initial mouse-down in local space of the sender element
	 * @param y vertical mouse position of the initial mouse-down in local space of the sender element
	 * @param mouse mouse button and modifier key information of the related mouse-down event
	 * @return <code>true</code> if the drag action is started, <code>false</code> if it has been rejected
	 * 
	 * @see UIElement#acceptDrag(float, float, MouseInfo)
	 */
	public boolean startDrag(float x, float y, MouseInfo mouse);

	/**
	 * Implements mouse-drag event handler.
	 * 
	 * <p>The method can return <code>false</code> to cancel drag action. This does not generate a mouse-up
	 * or mouse-release event: those will happen in due course with the same initiator element.</p>
	 * 
	 * <p>Mouse position change is reported in root (pixel) coordinates and can be converted using
	 * {@link UIElement#getPixelSize()}:</p>
	 *<pre>
	 *float pix = getPixelSize();
	 *float localDx = drx * pix;
	 *float localDy = dry * pix;
	 *</pre>
	 * <p>Pixel scale may change between mouse events, so should not be cached between function calls.</p>
	 * 
	 * <p>Mouse position is reported in root coordinates and can be converted to local using
	 * {@link UIElement#rootToLocalX(float)} and {@link UIElement#rootToLocalY(float)}.
	 * The position is reported in addition to the change in position to avoid "drift" effect.</p>
	 * 
	 * @param rx current mouse horizontal position in root coordinates
	 * @param ry current mouse vertical position in root coordinates
	 * @param drx change in horizontal mouse position since the previous mouse-move event in root (pixel) coordinates 
	 * @param dry change in vertical mouse position since the previous mouse-move event in root (pixel) coordinates 
	 * @param mouse mouse button state and modifier key information of the related mouse-drag event
	 * 
	 * @return <code>false</code> if the drag action has been cancelled, <code>true</code> if the drag should continue 
	 */
	public boolean onMouseDrag(float rx, float ry, float drx, float dry, MouseInfo mouse);

	/**
	 * Implements mouse-up (drag finished) event handler. 
	 * 
	 * <p>This handler is called for any button, not just the one that started the drag action,
	 * and it always ends the drag action. Typically, the released button <code>e.getButton()</code> can be used to
	 * determine how to handle the end of this drag action: apply or cancel.</p> 
	 * 
	 * <p>Mouse position is reported in root coordinates and can be converted to local using
	 * {@link UIElement#rootToLocalX(float)} and {@link UIElement#rootToLocalY(float)}.</p>
	 * 
	 * <p>The method should return <code>true</code> to complete drag action. In the current version,
	 * returning <code>false</code> is not fully supported; it is reserved for multi-button dragging support in the future.</p>
	 * 
	 * @param rx mouse-up horizontal position in root coordinates
	 * @param ry mouse-up vertical position in root coordinates
	 * @param mouse mouse button and modifier key information of the related mouse-up event
	 * @param target UI element under mouse, which may be different from the drag initiator 
	 */
	public default void onDragFinish(float rx, float ry, MouseInfo mouse, UIElement target) {
		// does nothing by default
	}

	/**
	 * Implements cancel event handler.
	 * 
	 * <p>This handler is called when the drag is cancelled programmatically without a mouse event trigger.
	 * Mouse position <code>rx, ry</code> corresponds to the previous mouse-drag event position.</p>
	 * 
	 * <p>Default implementation does nothing.</p>
	 * 
	 * @param rx latest horizontal mouse position in root coordinates
	 * @param ry latest vertical mouse position in root coordinates
	 */
	public default void onDragCancel(float rx, float ry) {
		// does nothing by default
	}

}
