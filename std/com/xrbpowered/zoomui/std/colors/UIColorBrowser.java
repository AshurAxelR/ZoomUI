package com.xrbpowered.zoomui.std.colors;

import java.awt.Color;
import java.awt.Font;
import java.math.RoundingMode;
import java.text.NumberFormat;

import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.HotKeyMap;
import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.UIModalWindow;
import com.xrbpowered.zoomui.UIModalWindow.ResultHandler;
import com.xrbpowered.zoomui.UIWindowFactory;
import com.xrbpowered.zoomui.base.UIButtonBase;
import com.xrbpowered.zoomui.std.UIButton;
import com.xrbpowered.zoomui.std.UIOptionBox;
import com.xrbpowered.zoomui.std.text.UITextBox;

public class UIColorBrowser extends UIContainer {

	public static int defaultWidth = 16+256+UIColorSlider.defaultWidth+24+144;
	public static int defaultHeight = 24+256;
	public static int defaultHeightAlpha = defaultHeight+UIColorSlider.defaultWidth-8;
	public static int floatPrecision = 2;
	
	private static final String[][] chanLabels = {{"H:", "S:", "B:", "A:"}, {"R:", "G:", "B:", "A:"}};
	private static final String[][] chanMetrics = {{"\u00b0", "%", "%", "%"}, {null, null, null, null}};
	
	public static Font font = UIButton.font;
	public static Color colorText = UIButton.colorText;
	
	public final boolean pickAlpha;
	
	public boolean intMode = true;
	
	public final UIColorView view;
	public final UIColorSlider alphaSlider;
	public final UIColorBox colorBox;
	public final UIButtonBase btnOk, btnCancel;
	public final UIOptionBox<Boolean> optMode;
	public final UITextBox txtColor;
	public final UITextBox[][] txtChan;
	
	public UIColorBrowser(UIContainer parent, boolean alpha, final ResultHandler<Color> resultHandler) {
		super(parent);
		this.pickAlpha = alpha;
		
		view = new UIColorView(this) {
			@Override
			public void onColorChanged() {
				updateText();
			}
		};
		
		colorBox = new UIColorBox(this);
		colorBox.setSize(64, 64);
		
		if(alpha) {
			alphaSlider = new UIColorSlider(this, false) {
				@Override
				public Color getSliderColorAt(float sz) {
					return UIColorBox.colorWithAlpha(view.getColor(), sz);
				}
				@Override
				public void onChanged() {
					updateText();
				}
			};
			alphaSlider.setValue(1f);
		}
		else {
			alphaSlider = null;
		}
		
		optMode = new UIOptionBox<Boolean>(this, new Boolean[] {true, false}) {
			@Override
			protected String formatOption(Boolean value) {
				return value ? "Integer" : "Float";
			}
			@Override
			protected void onOptionSelected(Boolean value) {
				intMode = value;
				updateText();
			}
		};
		
		txtColor = new UITextBox(this) {
			@Override
			public boolean onEnter() {
				try {
					if(intMode) {
						long rgb;
						String s = txtColor.editor.getText();
						if(pickAlpha && s.length()>6) {
							rgb = Long.parseLong(s, 16) & 0xffffffffL;
							if(rgb<0L) rgb = 0L;
							if(rgb>0xffffffffL) rgb = 0xffffffffL;
						}
						else {
							rgb = Long.parseLong(s, 16) & 0xffffffL;
							if(rgb<0L) rgb = 0L;
							if(rgb>0xffffffL) rgb = 0xffffffL;
							rgb |= 0xff000000;
						}
						setColor(new Color((int)rgb, true));
					}
					else {
						String[] ss = txtColor.editor.getText().split(",\\s*", 5);
						float[] vs = new float[4];
						float last = 0f;
						for(int i=0; i<4; i++) {
							float v = i>=ss.length ? (i==3 ? 1f : last) : Float.parseFloat(ss[i]);
							if(v<0f) v = 0f;
							if(v>1f) v = 1f;
							vs[i] = v;
							last = v;
						}
						if(!pickAlpha)
							vs[3] = 1f;
						setColor(new Color(vs[0], vs[1], vs[2], vs[3]));
					}
					updateText();
					return true;
				}
				catch (NumberFormatException e) {
					return false;
				}
			}
			@Override
			public boolean onEscape() {
				updateText();
				return true;
			}
		};
		
		txtChan = new UITextBox[2][numChan()];
		for(int i=0; i<2; i++)
			for(int j=0; j<numChan(); j++) {
				final int mode = i;
				txtChan[i][j] = new UITextBox(this) {
					@Override
					public boolean onEnter() {
						boolean res;
						if(mode==0)
							res = enterHSB();
						else
							res = enterRGB();
						if(res)
							view.onColorChanged();
						return res;
					}
					@Override
					public boolean onEscape() {
						updateText();
						return true;
					}
				};
			}
		
		btnOk = new UIButton(this, "OK") {
			@Override
			public void onAction() {
				if(resultHandler!=null) {
					resultHandler.onResult(view.getColor());
				}
			}
		};
		btnCancel = new UIButton(this, "Cancel") {
			@Override
			public void onAction() {
				if(resultHandler!=null)
					resultHandler.onCancel();
			}
		};
		getRoot().hotKeys = new HotKeyMap()
				.addOk(btnOk)
				.addCancel(btnCancel);
		
		view.onColorChanged();
	}
	
	public void setColor(Color color) {
		if(color==null)
			return;
		view.setColor(color);
		if(pickAlpha)
			alphaSlider.setValue(color.getAlpha()/255f);
	}
	
	protected int numChan() {
		return pickAlpha ? 4 : 3;
	}
	
	public float getAlpha() {
		return pickAlpha ? alphaSlider.getValue() : 1f;
	}
	
	public int getRGB() {
		return UIColorBox.colorWithAlpha(view.getColor().getRGB(), getAlpha());
	}
	
	protected boolean enterHSB() {
		try {
			float h, s, b, a;
			if(intMode) {
				h = Integer.parseInt(txtChan[0][0].editor.getText()) / 360f;
				s = Integer.parseInt(txtChan[0][1].editor.getText()) / 100f;
				b = Integer.parseInt(txtChan[0][2].editor.getText()) / 100f;
				a = pickAlpha ? Integer.parseInt(txtChan[0][3].editor.getText()) / 100f : 1f;
			}
			else {
				h = Float.parseFloat(txtChan[0][0].editor.getText());
				s = Float.parseFloat(txtChan[0][1].editor.getText());
				b = Float.parseFloat(txtChan[0][2].editor.getText());
				a = pickAlpha ? Float.parseFloat(txtChan[0][3].editor.getText()) : 1f;
			}
			if(h<0f) h = 0f;
			if(h>1f) h = 1f;
			if(s<0f) s = 0f;
			if(s>1f) s = 1f;
			if(b<0f) b = 0f;
			if(b>1f) b = 1f;
			if(a<0f) a = 0f;
			if(a>1f) a = 1f;
			view.setColor(h, s, b);
			if(pickAlpha)
				alphaSlider.setValue(a);
			return true;
		}
		catch (NumberFormatException e) {
			return false;
		}
	}
	
	protected boolean enterRGB() {
		try {
			int r, g, b;
			float a;
			if(intMode) {
				r = Integer.parseInt(txtChan[1][0].editor.getText());
				g = Integer.parseInt(txtChan[1][1].editor.getText());
				b = Integer.parseInt(txtChan[1][2].editor.getText());
				a = pickAlpha ? Integer.parseInt(txtChan[1][3].editor.getText()) / 255f : 1f;
			}
			else {
				r = Math.round(Float.parseFloat(txtChan[1][0].editor.getText()) * 255f);
				g = Math.round(Float.parseFloat(txtChan[1][1].editor.getText()) * 255f);
				b = Math.round(Float.parseFloat(txtChan[1][2].editor.getText()) * 255f);
				a = pickAlpha ? Float.parseFloat(txtChan[1][3].editor.getText()) : 1f;
			}
			if(r<0) r = 0;
			if(r>255) r = 255;
			if(g<0) g = 0;
			if(g>255) g = 255;
			if(b<0) b = 0;
			if(b>255) b = 255;
			if(a<0f) a = 0f;
			if(a>1f) a = 1f;
			view.setColor(new Color(r, g, b));
			if(pickAlpha)
				alphaSlider.setValue(a);
			return true;
		}
		catch (NumberFormatException e) {
			return false;
		}
	}
	
	protected void updateText() {
		Color c = view.getColor();
		if(intMode) {
			txtColor.editor.setText(String.format(pickAlpha ? "%08x" : "%06x", getRGB()));
			
			txtChan[0][0].editor.setText(Integer.toString(Math.round(view.getHue()*360f)));
			txtChan[0][1].editor.setText(Integer.toString(Math.round(view.getSaturation()*100f)));
			txtChan[0][2].editor.setText(Integer.toString(Math.round(view.getBrightness()*100f)));
			txtChan[1][0].editor.setText(Integer.toString(c.getRed()));
			txtChan[1][1].editor.setText(Integer.toString(c.getGreen()));
			txtChan[1][2].editor.setText(Integer.toString(c.getBlue()));
			
			if(pickAlpha) {
				txtChan[0][3].editor.setText(Integer.toString(Math.round(getAlpha()*100f)));
				txtChan[1][3].editor.setText(Integer.toString(Math.round(getAlpha()*255f)));
			}
		}
		else {
			int p = floatPrecision;
			float r = c.getRed()/255f;
			float g = c.getGreen()/255f;
			float b = c.getBlue()/255f;
			if(pickAlpha)
				txtColor.editor.setText(prettyFloats(p, r, g, b, getAlpha()));
			else
				txtColor.editor.setText(prettyFloats(p, r, g, b));
			
			txtChan[0][0].editor.setText(prettyFloat(p, view.getHue()));
			txtChan[0][1].editor.setText(prettyFloat(p, view.getSaturation()));
			txtChan[0][2].editor.setText(prettyFloat(p, view.getBrightness()));
			txtChan[1][0].editor.setText(prettyFloat(p, r));
			txtChan[1][1].editor.setText(prettyFloat(p, g));
			txtChan[1][2].editor.setText(prettyFloat(p, b));
			
			if(pickAlpha) {
				String a = prettyFloat(p, getAlpha());
				txtChan[0][3].editor.setText(a);
				txtChan[1][3].editor.setText(a);
			}
		}
		getRoot().resetFocus();
		colorBox.color = new Color(getRGB(), pickAlpha);
		if(pickAlpha)
			alphaSlider.resetBuffer();
	}

	public UIColorBrowser(UIContainer parent, final ResultHandler<Color> resultHandler) {
		this(parent, false, resultHandler);
	}

	@Override
	public void layout() {
		view.setPosition(8, 8);
		view.setSize(
			Math.min(256+UIColorSlider.defaultWidth, getWidth()-btnOk.getWidth()-96),
			Math.min(256, pickAlpha ? getHeight()-16-UIColorSlider.defaultWidth : getHeight()-24)
		);
		view.layout();
		if(pickAlpha) {
			alphaSlider.setPosition(8, view.getY()+view.getHeight());
			alphaSlider.setSize(view.box.getWidth(), UIColorSlider.defaultWidth);
		}
		colorBox.setPosition(view.getX()+view.getWidth(), 8);
		
		float th = txtColor.getHeight();
		float x0 =view.getX()+view.getWidth()+24;
		float y0 = view.getY()+view.getHeight()+4-3*(th+4);
		float w = getWidth()-8-x0;
		float cw = (w+40)/2;
		optMode.setSize(w, th);
		optMode.setPosition(x0, y0-th*2-20);
		txtColor.setSize(w, th);
		txtColor.setPosition(x0, y0-th-16);
		for(int i=0; i<2; i++)
			for(int j=0; j<numChan(); j++) {
				UITextBox txt = txtChan[i][j];
				txt.setSize(cw-40, txtColor.getHeight());
				float y = y0+j*(th+4);
				if(j==3) y += 4;
				txt.setPosition(x0+i*cw, y);
			}
		
		btnOk.setPosition(getWidth()-btnOk.getWidth()-8, 8);
		btnCancel.setPosition(btnOk.getX(), 12+btnOk.getHeight());
	}
	
	@Override
	protected void paintBackground(GraphAssist g) {
		g.fill(this, new Color(0xf2f2f2));
		if(getParent()==getRoot())
			g.hborder(this, GraphAssist.TOP, UIColorView.colorBorder);
		
		g.setFont(font);
		g.setColor(colorText);
		g.drawString(intMode ? "#" : "Vec", txtColor.getX()-4, txtColor.getY()+txtColor.getHeight()/2f,
				GraphAssist.RIGHT, GraphAssist.CENTER);
		for(int i=0; i<2; i++)
			for(int j=0; j<numChan(); j++) {
				UITextBox txt = txtChan[i][j];
				g.drawString(chanLabels[i][j], txt.getX()-4, txt.getY()+txt.getHeight()/2f,
						GraphAssist.RIGHT, GraphAssist.CENTER);
				if(intMode && chanMetrics[i][j]!=null)
					g.drawString(chanMetrics[i][j], txt.getX()+txt.getWidth()+2, txt.getY()+txt.getHeight()/2f,
							GraphAssist.LEFT, GraphAssist.CENTER);
			}
	}
	
	public static NumberFormat getPrettyFloatFormat(int prec) {
		NumberFormat f = NumberFormat.getInstance();
		f.setMaximumFractionDigits(prec);
		f.setMinimumFractionDigits(1);
		f.setRoundingMode(RoundingMode.HALF_UP); 
		return f;
	}
	
	public static String prettyFloat(int prec, float x) {
		return getPrettyFloatFormat(prec).format(x);
	}

	public static String prettyFloats(int prec, float... xs) {
		NumberFormat f = getPrettyFloatFormat(prec);
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<xs.length; i++) {
			if(i>0) sb.append(", ");
			sb.append(f.format(xs[i]));
		}
		return sb.toString();
	}

	public static UIModalWindow<Color> createDialog(String title, Color color, boolean alpha, int w, int h, boolean canResize, ResultHandler<Color> onResult) {
		if(title==null)
			title = "Select color";
		if(w<=0)
			w = defaultWidth;
		if(h<=0)
			h = alpha ? defaultHeightAlpha : defaultHeight;
		UIModalWindow<Color> dlg = UIWindowFactory.instance.createModal(title, w, h, canResize, onResult);
		new UIColorBrowser(dlg.getContainer(), alpha, dlg.wrapInResultHandler()).setColor(color);
		return dlg;
	}

	public static UIModalWindow<Color> createDialog(String title, Color color, boolean alpha, ResultHandler<Color> onResult) {
		return createDialog(title, color, alpha, 0, 0, false, onResult);
	}

	public static UIModalWindow<Color> createDialog(String title, Color color, ResultHandler<Color> onResult) {
		return createDialog(title, color, false, 0, 0, false, onResult);
	}

	public static UIModalWindow<Color> createDialog(Color color, boolean alpha, ResultHandler<Color> onResult) {
		return createDialog(null, color, alpha, 0, 0, false, onResult);
	}

	public static UIModalWindow<Color> createDialog(Color color, ResultHandler<Color> onResult) {
		return createDialog(null, color, false, 0, 0, false, onResult);
	}

}
