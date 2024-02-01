package com.xrbpowered.zoomui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.util.Deque;
import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * Convenience wrapper for Java2D graphics.
 * 
 * <p>This class implements the following property stacks to improve robustness during recursive operations:</p>
 * <ul>
 * <li>Affine transforms via {@link #pushTx()} and {@link #popTx()}.</li>
 * <li>Clipping area via {@link #pushClip(float, float, float, float)} and {@link #popClip()}.</li>
 * <li>Stroke control rendering hint via {@link #pushPureStroke(boolean)} and {@link #popPureStroke()}.</li>
 * </ul>
 *
 * <p>It also provides API wrappers for floating point drawing.
 * This is just a syntax sugar, as internally these are cast to integers for Java2D.</p>
 *
 */
public class GraphAssist {

	public static final int LEFT = 0;
	public static final int CENTER = 1;
	public static final int RIGHT = 2;
	public static final int TOP = 0;
	public static final int BOTTOM = 2;

	protected static final Stroke DEFAULT_STROKE = new BasicStroke(1f);

	/**
	 * Reference to the wrapped Java 2D graphics object.
	 */
	public final Graphics2D graph;

	private Deque<AffineTransform> txStack = new LinkedList<>();
	private Deque<Rectangle> clipStack = new LinkedList<>();
	private Deque<Boolean> aaStack = new LinkedList<>();
	private Deque<Boolean> pureStrokeStack = new LinkedList<>();

	public GraphAssist(Graphics2D graph) {
		this.graph = graph;
	}

	/**
	 * Returns the currently set affine transform.
	 * @return transform matrix
	 */
	public AffineTransform getTransform() {
		return graph.getTransform();
	}

	/**
	 * Replaces current affine transform with a new matrix.
	 * Does not modify transform stack.
	 * @param t transform matrix
	 */
	public void setTransform(AffineTransform t) {
		graph.setTransform(t);
	}

	/**
	 * Returns the top element from the transform stack without changing the stack or modifying transform settings. 
	 * @return transform matrix
	 * @throws NoSuchElementException if the transform stack is empty
	 */
	public AffineTransform peekTx() {
		return txStack.getFirst();
	}

	/**
	 * Pushes the current affine transform to the transform stack.
	 */
	public void pushTx() {
		txStack.addFirst(getTransform());
	}

	/**
	 * Reverts affine transform setting from the stack.
	 * @throws NoSuchElementException if the transform stack is empty
	 */
	public void popTx() {
		setTransform(txStack.removeFirst());
	}

	/**
	 * Sets current affine transform to an identity matrix.
	 */
	public void clearTransform() {
		graph.setTransform(new AffineTransform());
	}

	/**
	 * Concatenates current affine transform with translation.
	 * @param dx horizontal translation delta
	 * @param dy vertical translation delta
	 */
	public void translate(double dx, double dy) {
		graph.translate(dx, dy);
	}

	/**
	 * Concatenates current affine transform with scaling.
	 * Same scaling factor is used for both horizontal and vertical scaling.
	 * @param scale scaling factor delta
	 */
	public void scale(double scale) {
		graph.scale(scale, scale);
	}

	/**
	 * Returns current clip area.
	 * @return clip bounds rectangle
	 */
	public Rectangle getClip() {
		return graph.getClipBounds();
	}

	/**
	 * Overrides current clip area without using clip stack.
	 * @param r new clip rectangle
	 * @see #pushClip(float, float, float, float)
	 */
	public void setClip(Rectangle r) {
		graph.setClip(r);
	}

	/**
	 * Sets new clip area and pushes the old clip to the stack if they overlap, returns <code>false</code> otherwise.
	 * 
	 * <p>The new clip area is calculated as an intersection between the requested (<code>x</code>, <code>y</code>, <code>w</code>, <code>h</code>)
	 * clip rectangle and the current clip rectangle. If these areas do not overlap, no drawing should occur at all,
	 * and the method returns <code>false</code> to indicate that. When this happens, the current clip area in {@link #graph} is not
	 * modified, and the clip stack is not changed. Do not call {@link #popClip()} after <code>pushClip</code>
	 * returns <code>false</code>!</p>
	 * 
	 * <p>When the intersection results in a non-empty rectangle, the method pushes the old clip value onto the stack,
	 * sets the intersection as a new clip area in {@link #graph} and returns <code>true</code>. In this case, the caller should
	 * proceed to drawing, which will be graphically hard-clipped within the intersection bounds, and after that, restore the clip area
	 * using {@link #popClip()}.</p>
	 * 
	 * <p>Recommended usage pattern:</p>
	 *<pre>
	 *if (g.pushClip(x, y, w, h)) {
	 *    // draw operations
	 *    g.popClip();
	 *}
	 *</pre>
	 * 
	 * @param x left coordinate of the requested clip area 
	 * @param y top coordinate of the requested clip area
	 * @param w width of the requested clip area
	 * @param h height of the requested clip area
	 * @return <code>true</code> if the clip area is pushed onto the stack and updated,
	 *		<code>false</code> if nothing is pushed 
	 * 
	 * @see #popClip()
	 */
	public boolean pushClip(float x, float y, float w, float h) {
		Rectangle clip = getClip();
		Rectangle r = new Rectangle((int) x, (int) y, (int) w, (int) h);
		if(clip==null) {
			clipStack.addFirst(null);
			setClip(r);
			return true;
		}
		else if(r.intersects(clip)) {
			clipStack.addFirst(clip);
			setClip(r.intersection(clip));
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Sets new clip area to UI element's bounds in it's local space and pushes the old clip to the stack if they overlap,
	 * returns <code>false</code> otherwise. This is a shorthand for <code>pushClip(0, 0, e.getWidth(), e.getHeight())</code>.
	 * 
	 * @param e UI element to use for clip area
	 * @return <code>true</code> if the clip area is pushed onto the stack and updated,
	 *		<code>false</code> if nothing is pushed
	 * 
	 * @see #pushClip(float, float, float, float)
	 * @see #popClip()
	 */
	public boolean pushClip(UIElement e) {
		return pushClip(0, 0, e.getWidth(), e.getHeight());
	}

	/**
	 * Reverts clip area settings from the clip stack.
	 * Do not call this method if the respective <code>pushClip</code> returned <code>false</code>.
	 * 
	 * @see #pushClip(float, float, float, float)
	 * 
	 * @throws NoSuchElementException if the clip stack is empty, indicating the mismatch between the number of pushes and pops.
	 */
	public void popClip() {
		setClip(clipStack.removeFirst());
	}

	public boolean isAntialisingOn() {
		return graph.getRenderingHint(RenderingHints.KEY_ANTIALIASING)==RenderingHints.VALUE_ANTIALIAS_ON;
	}

	public void pushAntialiasing(boolean aa) {
		aaStack.addFirst(isAntialisingOn());
		graph.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				aa ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
	}

	public void popAntialiasing() {
		boolean aa = aaStack.removeFirst();
		graph.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				aa ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
	}

	/**
	 * Returns the current rendering hint setting for the stroke control.
	 * See {@link #pushPureStroke(boolean)} for the explanation of stroke control settings.
	 * 
	 * @return <code>true</code> if the stroke control is set to {@link RenderingHints#VALUE_STROKE_PURE},
	 *		<code>false</code> if it is set to {@link RenderingHints#VALUE_STROKE_NORMALIZE}
	 */
	public boolean isPureStrokeOn() {
		return graph.getRenderingHint(RenderingHints.KEY_STROKE_CONTROL)==RenderingHints.VALUE_STROKE_PURE;
	}

	/**
	 * Sets the stroke control rendering hint and pushes the old value to the stack.
	 * 
	 * <p>Java 2D rendering hint for stroke control {@link RenderingHints#KEY_STROKE_CONTROL} can take one
	 * of the following values:</p>
	 * <ul>
	 * <li>{@link RenderingHints#VALUE_STROKE_NORMALIZE} normalises coordinates to the nearest pixel.
	 * Should be used for background filling and drawing pixel-perfect rectangular frames and straight lines.
	 * Can cause ugly distortions for curved shapes, especially circles.</li>
	 * <li>{@link RenderingHints#VALUE_STROKE_PURE} renders in unmodified ("pure") sub-pixel coordinates.
	 * Preserves accurate shapes, but may cause uneven apparent width for straight axis-parallel lines and rectangles.</li>
	 * </ul>
	 * 
	 * @param pure <code>true</code> sets the stroke control to <code>VALUE_STROKE_PURE</code>,
	 *		<code>false</code> sets it to <code>VALUE_STROKE_NORMALIZE</code>
	 * 
	 * @see #isPureStrokeOn()
	 * @see #popPureStroke()
	 */
	public void pushPureStroke(boolean pure) {
		pureStrokeStack.addFirst(isPureStrokeOn());
		graph.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
				pure ? RenderingHints.VALUE_STROKE_PURE : RenderingHints.VALUE_STROKE_NORMALIZE);
	}

	/**
	 * Reverts stroke control setting from the stroke control stack.
	 * See {@link #pushPureStroke(boolean)} for the explanation of stroke control settings.
	 * 
	 * @throws NoSuchElementException if the stroke control stack is empty, indicating the mismatch between the number of pushes and pops.
	 */
	public void popPureStroke() {
		boolean pure = pureStrokeStack.removeFirst();
		graph.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
				pure ? RenderingHints.VALUE_STROKE_PURE : RenderingHints.VALUE_STROKE_NORMALIZE);
	}

	public void setColor(Color c) {
		graph.setColor(c);
	}

	public void setFont(Font f) {
		graph.setFont(f);
	}

	public FontMetrics getFontMetrics() {
		return graph.getFontMetrics();
	}

	public void setPaint(Paint p) {
		graph.setPaint(p);
	}

	public void setStroke(float width) {
		graph.setStroke(new BasicStroke(width));
	}

	public void resetStroke() {
		graph.setStroke(DEFAULT_STROKE);
	}

	public void fillRect(float x, float y, float w, float h) {
		graph.fillRect((int) x, (int) y, (int) w, (int) h);
	}

	public void fillRect(float x, float y, float w, float h, Color c) {
		setColor(c);
		fillRect(x, y, w, h);
	}

	public void drawRect(float x, float y, float w, float h) {
		graph.drawRect((int) x, (int) y, (int) w, (int) h);
	}

	public void drawRect(float x, float y, float w, float h, Color c) {
		setColor(c);
		drawRect(x, y, w, h);
	}

	public void fill(UIElement e) {
		fillRect(0, 0, e.getWidth(), e.getHeight());
	}

	public void fill(UIElement e, Color c) {
		fillRect(0, 0, e.getWidth(), e.getHeight(), c);
	}

	public void border(UIElement e) {
		drawRect(0, 0, e.getWidth(), e.getHeight());
	}

	public void border(UIElement e, Color c) {
		drawRect(0, 0, e.getWidth(), e.getHeight(), c);
	}

	public void line(float x1, float y1, float x2, float y2) {
		graph.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
	}

	public void line(float x1, float y1, float x2, float y2, Color c) {
		setColor(c);
		line(x1, y1, x2, y2);
	}

	public void vborder(UIElement e, int halign) {
		float x = align(e.getWidth(), halign);
		line(x, 0, x, e.getHeight());
	}

	public void vborder(UIElement e, int halign, Color c) {
		setColor(c);
		vborder(e, halign);
	}

	public void hborder(UIElement e, int valign) {
		float y = align(e.getHeight(), valign);
		line(0, y, e.getWidth(), y);
	}

	public void hborder(UIElement e, int valign, Color c) {
		setColor(c);
		hborder(e, valign);
	}

	public void fillCircle(float cx, float cy, float r) {
		graph.fillOval((int) (cx - r), (int) (cy - r), (int) (r * 2f), (int) (r * 2f));
	}

	public void fillCircle(float cx, float cy, float r, Color c) {
		setColor(c);
		fillCircle(cx, cy, r);
	}

	public void drawCircle(float cx, float cy, float r) {
		graph.drawOval((int) (cx - r), (int) (cy - r), (int) (r * 2f), (int) (r * 2f));
	}

	public void drawCircle(float cx, float cy, float r, Color c) {
		setColor(c);
		drawCircle(cx, cy, r);
	}

	public float startPixelMode(UIElement e, boolean antialias) {
		pushAntialiasing(antialias);
		pushTx();
		clearTransform();
		translate(peekTx().getTranslateX(), peekTx().getTranslateY());
		return e.getPixelSize();
	}

	public float startPixelMode(UIElement e) {
		return startPixelMode(e, false);
	}

	public void finishPixelMode() {
		popTx();
		popAntialiasing();
	}

	public void pixelBorder(UIElement e, int thickness, Color fill, Color stroke) {
		float pix = startPixelMode(e);
		int w = (int) Math.ceil(e.getWidth() / pix);
		int h = (int) Math.ceil(e.getHeight() / pix);

		if(fill!=null) {
			setColor(fill);
			graph.fillRect(0, 0, w, h);
		}
		if(stroke!=null) {
			pixelRect(graph, 0, 0, w, h, thickness, stroke);
		}

		finishPixelMode();
	}

	public void drawString(String str, float x, float y) {
		graph.drawString(str, x, y);
	}

	public float drawString(String str, float x, float y, int halign, int valign) {
		FontMetrics fm = graph.getFontMetrics();
		float w = fm.stringWidth(str);
		float h = fm.getAscent() - fm.getDescent();
		float tx = x - align(w, halign);
		float ty = y + h - align(h, valign);
		graph.drawString(str, tx, ty);
		return y + fm.getHeight();
	}

	public static int ptToPixels(float pt) {
		return Math.round(96f * pt / 72f);
	}

	public static float align(float span, int align) {
		return span * (float) align / 2f;
	}

	public static void pixelRect(Graphics2D graph, int left, int top, int width, int height,
			int thickness, Color color) {

		graph.setStroke(DEFAULT_STROKE);
		graph.setColor(color);
		for(int i = 0; i<thickness; i++)
			graph.drawRect(left + i, top + i, width - i * 2 - 1, height - i * 2 - 1);
	}

}
