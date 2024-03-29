package com.xrbpowered.zoomui.std.file;

import static com.xrbpowered.zoomui.MouseInfo.LEFT;

import java.awt.Color;
import java.awt.Font;
import java.io.File;

import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.MouseInfo;
import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.UIElement;
import com.xrbpowered.zoomui.UIModalWindow;
import com.xrbpowered.zoomui.UIModalWindow.ResultHandler;
import com.xrbpowered.zoomui.UIWindowFactory;
import com.xrbpowered.zoomui.base.History;
import com.xrbpowered.zoomui.base.UIButtonBase;
import com.xrbpowered.zoomui.std.UIButton;
import com.xrbpowered.zoomui.std.UIMessageBox;
import com.xrbpowered.zoomui.std.UIMessageBox.MessageResult;
import com.xrbpowered.zoomui.std.UIToolButton;
import com.xrbpowered.zoomui.std.text.UITextBox;

public class UIFileBrowser extends UIContainer {

	public static int defaultWidth = 840;
	public static int defaultHeight = 480;
	
	public static String rootPathLabel = "This computer";
	
	public static Font font = UIButton.font;
	public static Color colorText = UIButton.colorText;

	public final UIFileView view;
	public final UITextBox txtFileName, txtPath;
	public final UINavPath navPath;
	public final UIButtonBase btnBack, btnFwd, btnRefresh, btnUp, btnHome, btnRoots, btnOk, btnCancel;
	
	public final History<File> history = new History<File>(64) {
		@Override
		protected void apply(File item) {
			view.setDirectory(item);
		}
		@Override
		public void push() {
			push(view.getDirectory());
		}
		@Override
		protected void onUpdate() {
			btnBack.setEnabled(canUndo());
			btnFwd.setEnabled(canRedo());
		}
	};
	
	public UIFileBrowser(UIContainer parent, final ResultHandler<File> resultHandler) {
		super(parent);
		
		view = new UIFileView(this, null, true) {
			@Override
			public void onDirectorySet() {
				File dir = view.getDirectory();
				btnUp.setEnabled(dir!=null);
				txtPath.editor.setText(dir!=null ? dir.getAbsolutePath() : rootPathLabel);
				txtPath.editor.selectAll();
				navPath.setPath(dir);
			}
			@Override
			public void onBrowse() {
				history.push();
			}
			@Override
			public void onFileSelected(File file) {
				if(file.isFile()) {
					txtFileName.editor.setText(file.getName());
					txtFileName.editor.deselect();
					txtFileName.editor.setCursor(txtFileName.editor.getText().length());
				}
			}
		};
		
		// top pane
		txtPath = new UITextBox(this) {
			@Override
			public boolean onEnter() {
				return setPath(editor.getText());
			}
			@Override
			public void onFocusLost() {
				navPath.setVisible(true);
				this.setVisible(false);
				super.onFocusLost();
			}
		};
		txtPath.setVisible(false);
		navPath = new UINavPath(this) {
			@Override
			public void setPath(File path) {
				super.setPath(path==null ? rootPathLabel : path.getAbsolutePath());
			}
			@Override
			public boolean onMouseDown(float x, float y, MouseInfo mouse) {
				if(mouse.eventButton==LEFT) {
					this.setVisible(false);
					txtPath.setVisible(true);
					getRoot().setFocus(txtPath.editor);
					txtPath.editor.selectAll();
					repaint();
				}
				return true;
			}
			@Override
			public void onItemClicked(int index, String item) {
				if(item.equals(rootPathLabel)) {
					UIFileBrowser.this.setPath(null);
				}
				else {
					StringBuilder sb = new StringBuilder();
					for(int i=0; i<=index; i++) {
						sb.append(items[i].item);
						sb.append("/");
					}
					UIFileBrowser.this.setPath(sb.toString());
				}
				repaint();
			}
		};
		btnBack = new UIToolButton(this, UIToolButton.iconPath+"left.svg", 16, 2) {
			public void onAction() {
				if(history.undo())
					repaint();
			}
		}.disable();
		btnFwd = new UIToolButton(this, UIToolButton.iconPath+"right.svg", 16, 2) {
			public void onAction() {
				if(history.redo())
					repaint();
			}
		}.disable();
		btnRefresh = new UIToolButton(this, UIToolButton.iconPath+"refresh.svg", 16, 2) {
			public void onAction() {
				view.refresh();				
				repaint();
			}
		};
		
		// left pane
		btnUp = new UIToolButton(this, UIToolButton.iconPath+"o_up.svg", 32, 8) {
			public void onAction() {
				if(view.upDirectory())
					history.push();
				repaint();
			}
		};
		btnHome = new UIToolButton(this, UIToolButton.iconPath+"home.svg", 32, 8) {
			public void onAction() {
				if(view.setDirectory(new File(System.getProperty("user.home"))))
					history.push();
				repaint();
			}
		};
		btnRoots = new UIToolButton(this, UIToolButton.iconPath+"computer.svg", 32, 8) {
			public void onAction() {
				if(view.setDirectory(null))
					history.push();
				repaint();
			}
		};
		
		// bottom pane
		txtFileName = new UITextBox(this);
		btnOk = new UIButton(this, "OK") {
			@Override
			public void onAction() {
				if(resultHandler!=null) {
					String name = txtFileName.editor.getText();
					File result = name.isEmpty() ? view.getDirectory() : new File(view.getDirectory(), name);
					resultHandler.onResult(result);
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
		getRoot().hotKeys
				.addOk(btnOk)
				.addCancel(btnCancel);
		
		setDirectory(null, false);
	}
	
	public void setDirectory(File dir, boolean clearHistory) {
		if(clearHistory)
			history.clear();
		
		File prev = view.getDirectory();
		view.setDirectory(dir);

		if(clearHistory ||
				!(prev==null && dir==null) &&
				!(prev!=null && dir!=null && dir.equals(prev)))
			history.push(); // push after clear history or if dir has changed
	}
	
	private boolean setPath(String path) {
		if(view.setDirectory(path==null || path.equals(rootPathLabel) ? null : new File(path))) {
			history.push();
			return true;
		}
		else {
			UIMessageBox.show("Error", "Cannot open directory "+path,
					UIMessageBox.iconError, new MessageResult[] {MessageResult.ok}, null);
			txtPath.editor.selectAll();
			return false;
		}
	}
	
	@Override
	public void layout() {
		float w = getWidth();
		float h = getHeight();
		float top = txtFileName.getHeight()+16;
		float viewh = h-24-UIButton.defaultHeight*2-top;
		view.setPosition(56, top);
		view.setSize(w-56, viewh);
		txtFileName.setPosition(56, h-UIButton.defaultHeight*2-16);
		txtFileName.setSize(w-56-8, txtFileName.getHeight());
		txtPath.setPosition(56, 8);
		txtPath.setSize(w-56-4-28, txtFileName.getHeight());
		navPath.setPosition(txtPath.getX(), txtPath.getY());
		navPath.setSize(txtPath.getWidth(), txtPath.getHeight());
		btnBack.setPosition(28-22, 8);
		btnFwd.setPosition(28+2, 8);
		btnRefresh.setPosition(w-28, 8);
		btnUp.setPosition(4, top+4);
		btnHome.setPosition(4, top+viewh-48*2-4);
		btnRoots.setPosition(4, top+viewh-48-4);
		btnOk.setPosition(w-UIButton.defaultWidth*2-12, h-UIButton.defaultHeight-8);
		btnCancel.setPosition(w-UIButton.defaultWidth-8, h-UIButton.defaultHeight-8);
		super.layout();
	}
	
	@Override
	protected void paintBackground(GraphAssist g) {
		int w = (int)getWidth();
		int h = (int)getHeight();
		int top = (int)(txtFileName.getHeight()+16);
		
		g.fillRect(0, 0, w, top, Color.WHITE);
		g.fillRect(0, top, w, h-top, new Color(0xf2f2f2));
		g.setColor(new Color(0xcccccc));
		g.line(0, top, w, top);
		
		g.setFont(font);
		g.setColor(colorText);
		g.drawString("File:", 52, txtFileName.getY()+txtFileName.getHeight()/2f,
				GraphAssist.RIGHT, GraphAssist.CENTER);
	}
	
	public static UIFileBrowser fromDialog(UIModalWindow<?> dlg) {
		for(UIElement e : dlg.getContainer().getChildren()) {
			if(e instanceof UIFileBrowser)
				return (UIFileBrowser) e;
		}
		return null;
	}
	
	public static UIModalWindow<File> createDialog(String title, File dir, int w, int h, boolean canResize, ResultHandler<File> onResult) {
		if(w<=0)
			w = defaultWidth;
		if(h<=0)
			h = defaultHeight;
		UIModalWindow<File> dlg = UIWindowFactory.instance.createModal(title, w, h, canResize, onResult);
		new UIFileBrowser(dlg.getContainer(), dlg.wrapInResultHandler()).setDirectory(dir, true);
		return dlg;
	}

	public static UIModalWindow<File> createDialog(String title, File dir, ResultHandler<File> onResult) {
		return createDialog(title, dir, 0, 0, true, onResult);
	}
	
	public static UIModalWindow<File> createDialog(String title, ResultHandler<File> onResult) {
		return createDialog(title, new File("."), 0, 0, true, onResult);
	}
}
