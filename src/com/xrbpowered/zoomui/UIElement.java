package com.xrbpowered.zoomui;

import java.awt.Rectangle;
import java.security.InvalidParameterException;

/**
 * Parent class for all ZoomUI components.
 * 
 * <p>Each instance of <code>UIElement</code> represents an UI component with position, size, and toggled visibility.
 * Additionally, the hover state keeps track of mouse-in and mouse-out events.</p>
 * 
 * <p><code>UIElement</code> cannot have child elements. For elements with children use {@link UIContainer} instead.</p>
 * 
 * <p>Parent container is assigned in the constructor and cannot be changed once the element is created.
 * For re-parenting, the element needs to be destroyed and recreated in another container.</p>
 * 
 * <p>Event handling is done by overriding a respective method:</p>
 * <ul>
 * <li>{@link #onMouseIn()} - mouse enters the element</li>
 * <li>{@link #onMouseOut()} - mouse leaves the element</li>
 * <li>{@link #onMouseMoved(float, float, MouseInfo)} - mouse moved within the element bounds</li>
 * <li>{@link #onMouseDown(float, float, MouseInfo)} - mouse button pressed</li>
 * <li>{@link #onMouseUp(float, float, MouseInfo, UIElement)} - mouse button released.
 *      The respective mouse-down event may have happened on a different element (initiator).</li>
 * <li>{@link #onMouseReleased()} - mouse button released elsewhere.
 *      This event is sent to the initiator element that had the respective mouse-down event.</li>
 * <li>{@link #onMouseScroll(float, float, float, MouseInfo)} - mouse wheel scrolled over the element</li>
 * </ul>
 * 
 * <p>All event handler methods are called from the UI thread, therefore can request {@link UIElement#repaint()}
 * if needed.</p>
 */
public abstract class UIElement {

	/**
	 * Reference to the parent UI container, can be <code>null</code>
	 */
	private final UIContainer parent;

	/**
	 * Reference to the root container. Can be <code>null</code>; however,
	 * {@link #getRoot()} must not return <code>null</code>.
	 * 
	 * @see #getRoot()
	 */
	private final RootContainer root;

	/**
	 * Element's visibility flag, see {@link #isVisible()}.
	 */
	private boolean visible = true;

	/**
	 * Element's left position in parent space.
	 */
	private float x;

	/**
	 * Element's top position in parent space.
	 */
	private float y;

	/**
	 * Element's horizontal size.
	 */
	private float width;

	/**
	 * Element's vertical size.
	 */
	private float height;

	/**
	 * Element's hover status. Updated automatically in the default
	 * mouse-in and mouse-out handlers.
	 */
	private boolean hover = false;

	/**
	 * Element constructor. Automatically registers the element in <code>parent</code>.
	 * @param parent parent container
	 * @throws InvalidParameterException if a non-null root container cannot be determined, see {@link #getRoot()}
	 */
	public UIElement(UIContainer parent) {
		this.parent = parent;
		this.root = (parent!=null) ? parent.getRoot() : null;

		if(getRoot()==null) {
			// subclasses that allow parent to be null (e.g., RootContainer)
			// must override getRoot to return some non-null value
			throw new InvalidParameterException("root container not found");
		}

		if(parent!=null)
			parent.addChild(this);
	}

	/**
	 * Removes this element from its parent.
	 */
	public void remove() {
		getParent().removeChild(this);
	}

	/**
	 * Determines whether the element automatically fires {@link #repaint()} on every mouse-in and mouse-out event.
	 * Returns <code>false</code> by default; child classes may override this method.
	 * @return <code>true</code> if repaint is fired automatically
	 */
	public boolean repaintOnHover() {
		return false;
	}

	/**
	 * Returns parent container.
	 * @return parent container
	 */
	public UIContainer getParent() {
		return parent;
	}

	/**
	 * Returns root container in element hierarchy.
	 * This method is fast as each element keeps reference to the root container.
	 * 
	 * <p>Root container is expected to exist. Subclasses that allow parent container to be
	 * <code>null</code> (like {@link RootContainer}) must override this method to return some non-null value.</p>
	 * 
	 * @return root container
	 * 
	 * @see RootContainer
	 */
	public RootContainer getRoot() {
		return root;
	}

	public void invalidateTabIndex() {
		getRoot().tabIndex.invalidate();
	}

	/**
	 * Requests to recalculate layout of the entire UI before the next repaint.
	 * Use this method to request layout instead of calling {@link #layout()} directly.
	 * 
	 * <p>This method is also called automatically when the root panel is resized or elements are added or removed in the hierarchy.</p> 
	 * 
	 * @see #layout()
	 */
	public void invalidateLayout() {
		getRoot().invalidateLayout();
	}

	/**
	 * Recalculates the layout of this element's interior.
	 * Does nothing by default; child classes may override this method.
	 * 
	 * <p>Typical use for this method is for a container to layout its child elements using {@link #setPosition(float, float)} and {@link #setSize(float, float)}.
	 * The implementation of {@link UIContainer#layout()} also invokes <code>layout()</code> for its children,
	 * therefore it is recommended to call <code>super.layout()</code> for classes extending <code>UIContainer</code>.</p>
	 * 
	 * @see #invalidateLayout()
	 */
	public void layout() {
		// nothing to layout, override when needed
	}

	/**
	 * Changes element's visibility. Invisible elements are not painted and do not react to UI events.
	 * @param visible new visibility flag
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	/**
	 * Determines whether the element is visible.
	 * @return current visibility flag
	 * @see #setVisible(boolean)
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * Recursively determines whether all parent containers in the UI hierarchy have their visibility set to <code>true</code>.
	 * Returns <code>true</code> if parent is <code>null</code>. 
	 * @return <code>true</code> if all parent containers are visible, otherwise <code>false</code>
	 * @see #isVisible()
	 * @see #setVisible(boolean)
	 */
	public boolean isParentVisible() {
		if(parent!=null)
			return parent.isVisible() && parent.isParentVisible();
		else
			return true;
	}

	/**
	 * Determines whether the element is visible and overlaps the specified clipping area.
	 * If clipping area is <code>null</code>, the overlap is always true, and the result depends only on the visibility flag {@link #isVisible()}.
	 * @param clip clipping rectangle in parent space or <code>null</code>
	 * @return <code>true</code> if the element is visible within the clip rectangle, otherwise <code>false</code>
	 */
	public boolean isVisible(Rectangle clip) {
		return isVisible() && (clip==null ||
				!(clip.x - x>getWidth() || clip.x - x + clip.width<0 || clip.y - y>getHeight()
						|| clip.y - y + clip.height<0));
	}

	/**
	 * Changes element position.
	 * @param x new left coordinate in parent space
	 * @param y new top coordinate in parent space
	 */
	public void setPosition(float x, float y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Changes element size. Negative values are clamped to 0.
	 * @param width new element width
	 * @param height new element height
	 */
	public void setSize(float width, float height) {
		this.width = (width<0f) ? 0f : width;
		this.height = (height<0f) ? 0f : height;
	}

	/**
	 * Returns element's <i>x</i> (left) position.
	 * @return left coordinate in parent space 
	 */
	public float getX() {
		return x;
	}

	/**
	 * Returns element's <i>y</i> (top) position.
	 * @return top coordinate in parent space
	 */
	public float getY() {
		return y;
	}

	/**
	 * Returns element's width.
	 * @return current width
	 */
	public float getWidth() {
		return width;
	}

	/**
	 * Returns element's height
	 * @return current height
	 */
	public float getHeight() {
		return height;
	}

	/**
	 * Determines if the point is visible within element's bounds; always <code>false</code> for invisible elements.
	 * This method is primarily used for selecting mouse event targets.
	 * 
	 * <p>Element's bounds are in parent space and determined by {@link #getX()}, {@link #getY()}, {@link #getWidth()} and {@link #getHeight()} of the element.
	 * Child classes may override this method to create "unbounded" elements.</p>
	 * 
	 * @param px horizontal coordinate of the point in parent space
	 * @param py vertical coordinate of the point in parent space
	 * @return <code>true</code> if the element is visible and the point lies within its bounds, otherwise <code>false</code>.
	 * 
	 * @see #isVisible()
	 */
	public boolean isInside(float px, float py) {
		return isVisible() && px>=getX() && py>=getY() && px<=(getX() + getWidth()) && py<=(getY() + getHeight());
	}

	/**
	 * Converts <i>x</i> coordinate from parent space to this element's local space.
	 * @param px horizontal coordinate in parent space
	 * @return horizontal coordinate in local space
	 */
	protected float parentToLocalX(float px) {
		return px - this.x;
	}

	/**
	 * Converts <i>y</i> coordinate from parent space to this element's local space.
	 * @param py vertical coordinate in parent space
	 * @return vertical coordinate in local space
	 */
	protected float parentToLocalY(float py) {
		return py - this.y;
	}

	/**
	 * Converts <i>x</i> coordinate from the element's local space to parent space.
	 * @param x horizontal coordinate in local space
	 * @return horizontal coordinate in parent space
	 */
	protected float localToParentX(float x) {
		return x + this.x;
	}

	/**
	 * Converts <i>y</i> coordinate from the element's local space to parent space.
	 * @param y vertical coordinate in local space
	 * @return vertical coordinate in parent space
	 */
	protected float localToParentY(float y) {
		return y + this.y;
	}

	/**
	 * Recursively converts <i>x</i> coordinate from root (pixel) space to this element's local space.
	 * @param rx horizontal coordinate in root space
	 * @return horizontal coordinate in local space
	 * @see #getRoot()
	 * @see RootContainer
	 */
	public float rootToLocalX(float rx) {
		return parentToLocalX(parent==null ? rx : parent.rootToLocalX(rx));
	}

	/**
	 * Recursively converts <i>y</i> coordinate from root (pixel) space to this element's local space.
	 * @param ry vertical coordinate in root space
	 * @return vertical coordinate in local space
	 * @see #getRoot()
	 * @see RootContainer
	 */
	public float rootToLocalY(float ry) {
		return parentToLocalY(parent==null ? ry : parent.rootToLocalY(ry));
	}

	/**
	 * Recursively converts <i>x</i> coordinate from the element's local space to root (pixel) space.
	 * @param x horizontal coordinate in local space
	 * @return horizontal coordinate in root space
	 * @see #getRoot()
	 * @see RootContainer
	 */
	public float localToRootX(float x) {
		return parent==null ? localToParentX(x) : parent.localToRootX(localToParentX(x));
	}

	/**
	 * Recursively converts <i>y</i> coordinate from the element's local space to root (pixel) space.
	 * @param y vertical coordinate in local space
	 * @return vertical coordinate in root space
	 * @see #getRoot()
	 * @see RootContainer
	 */
	public float localToRootY(float y) {
		return parent==null ? localToParentY(y) : parent.localToRootY(localToParentY(y));
	}

	/**
	 * Recursively determines the size of one screen pixel in local coordinates.
	 * 
	 * <p>Can be used to calculate pixel-perfect dimensions based on the current UI scale and affine transforms:</p>
	 *<pre>
	 *sizeInPixels = size / getPixelSize();
	 *</pre>
	 * 
	 * <p>The calculation may be relatively slow, so cache the result if it needs to be used multiple times within a function.
	 * Note that the pixel scale may change at any point, e.g., by user zooming in and out, but it is reasonable to assume
	 * that it remains constant within one function call on the UI (Swing) thread. Typical use case: {@link #paint(GraphAssist)}
	 * function, which should always be called from the UI thread.</p>
	 * 
	 * @return pixel scale
	 */
	public float getPixelSize() {
		if(parent!=null)
			return parent.getPixelSize();
		else
			return 1f;
	}

	/**
	 * Requests repainting of the UI tree or subtree containing this element. Must be called instead of directly invoking {@link #paint(GraphAssist)}.
	 * 
	 * <p>The request is recursively propagated to the parent. Depending on the container implementation, UI tree may be repainted partially or fully.</p>
	 * 
	 * <p>When using with Swing, this method must be called from the UI (Swing) thread; use <code>SwingUtilities#invokeLater(Runnable)</code> if needed.
	 * Mouse event handlers are called from the UI thread too, therefore, it is allowed to call <code>repaint</code> directly as a reaction to mouse events.</p>
	 */
	public void repaint() {
		if(parent!=null)
			parent.repaint();
	}

	/**
	 * Resets hover status of the element.
	 * @see #isHover()
	 */
	public void resetHover() {
		hover = false;
	}

	/**
	 * Determines whether the hover status is set. Hover status is managed by the
	 * default mouse-in and mouse-out event handlers.
	 * @return current hover status
	 * @see #resetHover()
	 */
	public boolean isHover() {
		return hover;
	}

	/**
	 * Draws the contents of this element.
	 * 
	 * <p>The method is called from the {@link UIWindow} and recursively
	 * traverses the tree of UI elements via {@link UIContainer#paint(GraphAssist)}.
	 * Do not call this method directly to repaint the element, use {@link #repaint()} instead.</p>
	 * 
	 * <p>The drawing is done in the local space where the element bounds are mapped between 0 and {@link #getWidth()} horizontally
	 * and between 0 and {@link #getHeight()} vertically; top-left corner is the origin.
	 * 
	 * <p>Child classes of <code>UIElement</code> must implement this method;
	 * however, child classes of {@link UIContainer} should instead override:</p>
	 * <ul>
	 * <li>{@link UIContainer#paintBackground(GraphAssist)},</li>
	 * <li>{@link UIContainer#paintForeground(GraphAssist)},</li>
	 * <li>or {@link UIContainer#paintChildren(GraphAssist)} if needed.</li>
	 * </ul>
	 * 
	 * @param g wrapper for {@link Graphics2D} canvas in local coordinates
	 * 
	 * @see GraphAssist
	 * @see #repaint()
	 */
	public abstract void paint(GraphAssist g);

	/**
	 * Determines whether the user's mouse-drag started a drag action.
	 * Returns {@link DragActor} event handler on success or <code>null</code> to cancel drag.
	 * If the drag action is cancelled, all future drag events will appear as simple mouse-move
	 * events until the next mouse-down event.
	 * 
	 * <p>This method is called as a reaction to Swing's mouse-dragged event
	 * {@link MouseMotionListener#mouseDragged(MouseEvent)}, which means that it
	 * happens after the mouse is moved while a button is held down. For convenience,
	 * the mouse position reported in <code>x</code> and <code>y</code> arguments are the position
	 * of the initial mouse-down event before the drag. If the method returns a drag actor,
	 * it will immediately receive {@link DragActor#onMouseDrag(float, float, float, float, MouseInfo)} with the updated
	 * mouse position.</p>
	 * 
	 * <p>It is not required to create a new instance of <code>DragActor</code> every time.
	 * The method can return the same drag handler instance for every drag action.
	 * Typically, the <code>acceptDrag</code> method should also call {@link DragActor#startDrag(float, float, MouseInfo)}
	 * to make sure the drag handler is accepting the drag as well.</p>
	 * 
	 * @param x mouse-down coordinate in local space (horizontal)
	 * @param y mouse-down coordinate in local space (vertical)
	 * @param mouse mouse button and modifier key information of the related mouse-down event
	 * @return drag handler if the drag action started, or <code>null</code> to refuse drag action 
	 * 
	 * @see DragActor#startDrag(float, float, MouseInfo)
	 */
	public DragActor acceptDrag(float x, float y, MouseInfo mouse) {
		return null;
	}

	/**
	 * Recursively determines the top visible UI element at a specified location.
	 * The function traverses this element and, for containers, its children.
	 * 
	 * <p>The method is primarily used to find a UI element under mouse.</p>
	 * 
	 * <p>Z order of elements is fixed. Children are considered "above" their parent,
	 * and go bottom to top in their order or creation. In other words, if two elements overlap, the one created later
	 * will be considered "above" in the hierarchy.</p>
	 * 
	 * <p>Default implementation for <code>UIElement</code> checks if the point lies within element's bounds
	 * via {@link #isInside(float, float)} method and returns <code>null</code> if it doesn't.</p>
	 * 
	 * @param px horizontal coordinate in parent space
	 * @param py vertical coordinate in parent space
	 * @return top visible UI element or <code>null</code> if there is no UI element in this location.
	 */
	public UIElement getElementAt(float px, float py) {
		if(isInside(px, py))
			return this;
		else
			return null;
	}

	/**
	 * Implements propagation of a mouse-down event through UI hierarchy. This function is internal to ZoomUI.
	 * @param px cursor coordinate in parent space (horizontal)
	 * @param py cursor coordinate in parent space (vertical)
	 * @param mouse mouse button and modifier key information of the related mouse-down event
	 * @return UI element in the tree that handled the event, or <code>null</code> if the event was not handled
	 */
	UIElement notifyMouseDown(float px, float py, MouseInfo mouse) {
		if(isInside(px, py) && onMouseDown(parentToLocalX(px), parentToLocalY(py), mouse))
			return this;
		else
			return null;
	}

	/**
	 * Implements propagation of a mouse-up event through UI hierarchy. This function is internal to ZoomUI.
	 * @param px cursor coordinate in parent space (horizontal)
	 * @param py cursor coordinate in parent space (vertical)
	 * @param mouse mouse button and modifier key information of the related mouse-up event
	 * @param initiator initiator element: the UI element that had received the respective mouse-down event
	 * @return UI element in the tree that handled the event, or <code>null</code> if the event was not handled
	 */
	UIElement notifyMouseUp(float px, float py, MouseInfo mouse, UIElement initiator) {
		if(isInside(px, py) && onMouseUp(parentToLocalX(px), parentToLocalY(py), mouse, initiator))
			return this;
		else
			return null;
	}

	/**
	 * Implements propagation of a mouse-scroll event through UI hierarchy. This function is internal to ZoomUI.
	 * @param px cursor coordinate in parent space (horizontal)
	 * @param py cursor coordinate in parent space (vertical)
	 * @param delta scroll amount
	 * @param mouse mouse button state and modifier key information of the related mouse-scroll event
	 * @return UI element in the tree that handled the event, or <code>null</code> if the event was not handled
	 */
	UIElement notifyMouseScroll(float px, float py, float delta, MouseInfo mouse) {
		if(isInside(px, py) && onMouseScroll(parentToLocalX(px), parentToLocalY(py), delta, mouse))
			return this;
		else
			return null;
	}

	/**
	 * Mouse-in event handler: mouse entered the element.
	 */
	public void onMouseIn() {
		hover = true;
		if(repaintOnHover())
			repaint();
	}

	/**
	 * Mouse-out event handler: mouse left the element.
	 */
	public void onMouseOut() {
		hover = false;
		if(repaintOnHover())
			repaint();
	}

	/**
	 * Mouse-moved event handler: mouse moved within the element bounds.
	 * @param x new cursor coordinate in local space (horizontal)
	 * @param y new cursor coordinate in local space (vertical)
	 * @param mouse mouse button state and modifier key information of the related mouse-move event
	 */
	public void onMouseMoved(float x, float y, MouseInfo mouse) {
		// no action
	}

	/**
	 * Mouse-down event handler: a mouse button was pressed.
	 * @param x cursor coordinate in local space (horizontal)
	 * @param y cursor coordinate in local space (vertical)
	 * @param mouse mouse button and modifier key information of the related mouse-down event
	 * @return <code>true</code> if the element handles the event,
	 *     or <code>false</code> if the event should propagate to the parent container (fall through).
	 */
	public boolean onMouseDown(float x, float y, MouseInfo mouse) {
		// no action, fall through
		return false;
	}

	/**
	 * Mouse-up event handler: a mouse button was released.
	 * The respective mouse-down event may have happened on a different UI element (the initiator).
	 * 
	 * <p><code>initiator</code> always refers to the latest mouse-down event within this zoomable UI tree,
	 * regardless of which mouse button is pressed or released. In most cases it is enough to test
	 * that the initiator is the same element as the one receivng this mouse-up event:</p>
	 *<pre>
	 *if (initiator == this) {
	 *    // do event handling
	 *    return true;
	 *}
	 *else
	 *    return false; // or true if the event needs to be consumed anyway
	 *</pre>
	 * 
	 * @param x cursor coordinate in local space (horizontal)
	 * @param y cursor coordinate in local space (vertical)
	 * @param mouse mouse button and modifier key information of the related mouse-up event
	 * @param initiator initiator element: the UI element that had received the latest mouse-down event
	 * @return <code>true</code> if the element handles the event,
	 *     or <code>false</code> if the event should propagate to the parent container (fall through).
	 */
	public boolean onMouseUp(float x, float y, MouseInfo mouse, UIElement initiator) {
		// no action, fall through
		return false;
	}

	/**
	 * Mouse-released event handler: mouse button released elsewhere.
	 * This event is sent to the initiator element that had received the respective mouse-down event.
	 */
	public void onMouseReleased() {
		// no action
	}

	/**
	 * Mouse-scroll event handler: mouse wheel scrolled over the element.
	 * @param x cursor coordinate in local space (horizontal)
	 * @param y cursor coordinate in local space (vertical)
	 * @param delta scroll amount
	 * @param mouse mouse button state and modifier key information of the related mouse-scroll event
	 * @return <code>true</code> if the element handles the event,
	 *     or <code>false</code> if the event should propagate to the parent container (fall through).
	 */
	public boolean onMouseScroll(float x, float y, float delta, MouseInfo mouse) {
		return false;
	}

	public void onFocusGained() {
	}

	public void onFocusLost() {
	}

	public boolean isFocused() {
		return getRoot().getFocus()==this;
	}

}
