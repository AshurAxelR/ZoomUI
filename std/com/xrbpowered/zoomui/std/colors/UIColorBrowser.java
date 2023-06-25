package com.xrbpowered.zoomui.std.colors;

import java.awt.Color;
import java.awt.Font;

import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.UIModalWindow.ResultHandler;
import com.xrbpowered.zoomui.base.UIButtonBase;
import com.xrbpowered.zoomui.std.UIButton;
import com.xrbpowered.zoomui.std.text.UITextBox;

public class UIColorBrowser extends UIContainer {

	public static int defaultWidth = 16+256+UIColorSlider.defaultWidth+24+144;
	public static int defaultHeight = 24+256;
	
	private static final String[][] chanLabels = {{"H:", "S:", "B:"}, {"R:", "G:", "B:"}};
	
	public static Font font = UIButton.font;
	public static Color colorText = UIButton.colorText;
	
	public final boolean alpha;
	
	public boolean intMode = true;
	
	public final UIColorView view;
	public final UIButtonBase btnOk, btnCancel;
	public final UITextBox txtColor;
	public final UITextBox[][] txtChan = new UITextBox[2][3];
	
	public UIColorBrowser(UIContainer parent, boolean alpha, final ResultHandler<Color> resultHandler) {
		super(parent);
		this.alpha = alpha;
		
		view = new UIColorView(this) {
			@Override
			public void onColorChanged() {
				updateText();
			}
		};
		
		txtColor = new UITextBox(this) {
			@Override
			public boolean onEnter() {
				try {
					int rgb = Integer.parseInt(txtColor.editor.getText(), 16);
					view.setColor(new Color(rgb, alpha));
					view.onColorChanged();
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
		for(int i=0; i<2; i++)
			for(int j=0; j<3; j++) {
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
		
		view.onColorChanged();
	}
	
	protected boolean enterHSB() {
		try {
			float h = Integer.parseInt(txtChan[0][0].editor.getText()) / 360f;
			if(h<0f) h = 0f;
			if(h>1f) h = 1f;
			float s = Integer.parseInt(txtChan[0][1].editor.getText()) / 100f;
			if(s<0f) s = 0f;
			if(s>1f) s = 1f;
			float b = Integer.parseInt(txtChan[0][2].editor.getText()) / 100f;
			if(b<0f) b = 0f;
			if(b>1f) b = 1f;
			view.setColor(h, s, b);
			return true;
		}
		catch (NumberFormatException e) {
			return false;
		}
	}
	
	protected boolean enterRGB() {
		try {
			int r = Integer.parseInt(txtChan[1][0].editor.getText());
			if(r<0) r = 0;
			if(r>255) r = 255;
			int g = Integer.parseInt(txtChan[1][1].editor.getText());
			if(g<0) g = 0;
			if(g>255) g = 255;
			int b = Integer.parseInt(txtChan[1][2].editor.getText());
			if(b<0) b = 0;
			if(b>255) b = 255;
			view.setColor(new Color(r, g, b));
			return true;
		}
		catch (NumberFormatException e) {
			return false;
		}
	}
	
	protected void updateText() {
		if(intMode) {
			Color c = view.getColor();
			if(alpha)
				txtColor.editor.setText(String.format("%08x", c.getRGB()));
			else
				txtColor.editor.setText(String.format("%06x", c.getRGB()&0xffffff));
			
			txtChan[0][0].editor.setText(Integer.toString(Math.round(view.getHue()*360f)));
			txtChan[0][1].editor.setText(Integer.toString(Math.round(view.getSaturation()*100f)));
			txtChan[0][2].editor.setText(Integer.toString(Math.round(view.getBrightness()*100f)));
			txtChan[1][0].editor.setText(Integer.toString(c.getRed()));
			txtChan[1][1].editor.setText(Integer.toString(c.getGreen()));
			txtChan[1][2].editor.setText(Integer.toString(c.getBlue()));
		}
		getBase().resetFocus();
	}

	public UIColorBrowser(UIContainer parent, final ResultHandler<Color> resultHandler) {
		this(parent, false, resultHandler);
	}

	@Override
	public void layout() {
		view.setLocation(8, 8);
		view.setSize(
			Math.min(256+UIColorSlider.defaultWidth, getWidth()-btnOk.getWidth()-96),
			Math.min(256, getHeight()-24)
		);
		
		float th = txtColor.getHeight();
		float x0 =view.getX()+view.getWidth()+24;
		float y = view.getY()+view.getHeight()+4-3*(th+4);
		float w = getWidth()-8-x0;
		float cw = (w+32)/2;
		txtColor.setSize(w, th);
		txtColor.setLocation(x0, y-th-16);
		for(int i=0; i<2; i++)
			for(int j=0; j<3; j++) {
				UITextBox txt = txtChan[i][j];
				txt.setSize(cw-32, txtColor.getHeight());
				txt.setLocation(x0+i*cw, y+j*(th+4));
			}
		
		btnOk.setLocation(getWidth()-btnOk.getWidth()-8, 8);
		btnCancel.setLocation(btnOk.getX(), 12+btnOk.getHeight());
		super.layout();
	}
	
	@Override
	protected void paintSelf(GraphAssist g) {
		g.fill(this, new Color(0xf2f2f2));
		if(getParent()==getBase())
			g.hborder(this, GraphAssist.TOP, UIColorView.colorBorder);
		
		float x = view.getX()+view.getWidth();
		float pix = g.startPixelMode(this);
		g.resetStroke();
		g.fillRect(x/pix, 8/pix, 64/pix, 64/pix, view.getColor());
		g.drawRect(x/pix, 8/pix, 64/pix, 64/pix, UIColorView.colorBorder);
		g.finishPixelMode();
		
		g.setFont(font);
		g.setColor(colorText);
		g.drawString("#", txtColor.getX()-4, txtColor.getY()+txtColor.getHeight()/2f,
				GraphAssist.RIGHT, GraphAssist.CENTER);
		for(int i=0; i<2; i++)
			for(int j=0; j<3; j++) {
				UITextBox txt = txtChan[i][j];
				g.drawString(chanLabels[i][j], txt.getX()-4, txt.getY()+txt.getHeight()/2f,
						GraphAssist.RIGHT, GraphAssist.CENTER);
			}
	}
	
}
