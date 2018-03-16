package com.xrbpowered.uitest;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;

import com.xrbpowered.zoomui.BasePanel;
import com.xrbpowered.zoomui.TextUtils;
import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.UIElement;
import com.xrbpowered.zoomui.icons.SvgIcon;
import com.xrbpowered.zoomui.std.StdPainter;
import com.xrbpowered.zoomui.std.UIButton;
import com.xrbpowered.zoomui.std.UIButtonBase;
import com.xrbpowered.zoomui.std.UIScrollContainer;
import com.xrbpowered.zoomui.std.UITextBox;
import com.xrbpowered.zoomui.std.UIToolButton;

public class FileBrowser extends UIContainer {

	private static final SimpleDateFormat dateFmt = new SimpleDateFormat("d MMM yyyy, HH:mm");
	
	private static final SvgIcon fileIcon = new SvgIcon("svg/file.svg", 160, StdPainter.instance.iconPalette);
	private static final SvgIcon folderIcon = new SvgIcon("svg/folder.svg", 160, StdPainter.instance.iconPalette);
	private static final SvgIcon diskIcon = new SvgIcon("svg/disk.svg", 160, StdPainter.instance.iconPalette);
	
	private static final int LIST_ITEM_WIDTH = 256;
	private static final int LIST_ITEM_HEIGHT = 48;
	
	private static String formatFileSize(long size) {
		String[] prefs = {"bytes", "KB", "MB", "GB", "TB"};
		double s = size;
		for(int d=0; d<prefs.length; d++) {
			if(d>0 && s<10.0)
				return String.format("%.2f %s", s, prefs[d]);
			else if(d>0 && s<100.0)
				return String.format("%.1f %s", s, prefs[d]);
			else if(s<1000.0)
				return String.format("%.0f %s", s, prefs[d]);
			else
				s = s/1024.0;
		}
		return "";
	}
	
	private static boolean startsWithSymbol(String s) {
		if(s.isEmpty())
			return false;
		char ch = s.charAt(0);
		return !(Character.isLetter(ch) || Character.isDigit(ch)) && ch!='_';
	}
	
	public static class FileListItem extends UIElement {
		protected File file;
		public final FileBrowser fileBrowser;
		protected boolean hover = false;
		
		private String info;
		private boolean isSystem;
		private int textWidth = -1;
		private int textHeight = -1;
		
		public FileListItem(UIContainer parent, FileBrowser fileBrowser, File file) {
			super(parent);
			this.fileBrowser = fileBrowser;
			this.file = file;
			if(file.isFile())
				info = dateFmt.format(file.lastModified()) + ", "+formatFileSize(file.length());
			else if(file.getName().isEmpty())
				info = formatFileSize(file.getFreeSpace()) + " free, " + formatFileSize(file.getTotalSpace()) + " total";
			else
				info = null;
			isSystem = startsWithSymbol(file.getName()) || file.isHidden() && !file.getName().isEmpty();
		}
		
		@Override
		public void paint(Graphics2D g2) {
			int w = (int)getWidth();
			int h = (int)getHeight();
			Rectangle clip = g2.getClipBounds();
			if(clip.y>h || clip.y+clip.height<0)
				return;
			
			StdPainter painter = StdPainter.instance;
			boolean sel = (file==fileBrowser.view.getSelectedFile());
			Color bgColor = sel ? painter.colorSelection : hover ? painter.colorHighlight : painter.colorTextBg;
			g2.setColor(bgColor);
			g2.fillRect(0, 0, w, h);

			String fileName = file.getName();
			boolean disk = false;
			if(fileName.isEmpty()) {
				fileName = file.getAbsolutePath();
				disk = true;
			}

			int style = sel ? 1 : 0;
			if(isSystem) style += 2;
			(disk ? diskIcon : file.isFile() ? fileIcon : folderIcon).paint(g2, style, 20, 8, 32, getPixelScale(), true);

			g2.setFont(painter.font);
			g2.setColor(sel ? painter.colorSelectionFg : painter.colorFg);
			if(textWidth<0) {
				FontMetrics fm = g2.getFontMetrics();
				textWidth = fm.stringWidth(fileName);
				textHeight = fm.getAscent() - fm.getDescent();
			}
			float y = info==null ? (h/2f + textHeight/2f) : (h/2f-3f);
			if(textWidth+60>=w-8) {
				Rectangle r = new Rectangle(0, 0, w-8, h);
				if(r.intersects(clip)) {
					r = r.intersection(clip);
					g2.setClip(r);
					g2.drawString(fileName, 60, y);
					g2.setClip(clip);
					g2.setPaint(new GradientPaint(w-64, 0, new Color(bgColor.getRGB()&0xffffff, true), w-8, 0, bgColor));
					g2.fillRect(w-64, 0, 56, h);
				}
			}
			else {
				g2.drawString(fileName, 60, y);
			}
			if(info!=null) {
				g2.setColor(sel ? painter.colorSelectionFgDisabled : painter.colorFgDisabled);
				g2.drawString(info, 60, (int)(h/2f+3f+textHeight));
			}
		}
		
		@Override
		protected void onMouseIn() {
			hover = true;
			requestRepaint();
		}
		
		@Override
		protected void onMouseOut() {
			hover = false;
			requestRepaint();
		}
		
		@Override
		protected boolean onMouseDown(float x, float y, int buttons) {
			if(buttons==mouseLeftMask) {
				FileViewPane fileView = fileBrowser.view;
				if(fileView.getSelectedFile()==file)
					fileView.onClickSelected();
				else {
					fileView.setSelectedFile(file);
					fileView.onSelect(file);
				}
				requestRepaint();
				return true;
			}
			else
				return false;
		}
	}

	public static class FileGroupBoxHeader extends UIElement {
		protected boolean hover = false;

		public FileGroupBoxHeader(FileGroupBox parent) {
			super(parent);
		}
		
		@Override
		public void paint(Graphics2D g2) {
			StdPainter painter = StdPainter.instance;
			Color bgColor = hover ? painter.colorHighlight : painter.colorTextBg;
			g2.setColor(bgColor);
			g2.fillRect(0, 0, (int)getWidth(), (int)getHeight());
			
			FileGroupBox grp = (FileGroupBox) getParent();
			boolean open = grp.isViewOpen();
			
			g2.setColor(open ? painter.colorSelection : painter.colorFgDisabled);
			String str = String.format("%s (%d)", grp.title, grp.getNumFiles());
			FontMetrics fm = g2.getFontMetrics();
			int textWidth = fm.stringWidth(str);
			g2.drawString(str, 20, 2+painter.fontSize);
			
			Stroke stroke = g2.getStroke();
			g2.setStroke(new BasicStroke(2f));
			g2.setColor(painter.colorFg);
			int w = (int)(getHeight()/2f);
			if(open)
				g2.drawPolyline(new int[] {6, 10, 14}, new int[] {w-2, w+2, w-2}, 3);
			else
				g2.drawPolyline(new int[] {8, 12, 8}, new int[] {w-4, w, w+4}, 3);
			
			g2.setStroke(stroke);
			g2.setColor(painter.colorBorderLight);
			g2.drawLine(textWidth+28, w, (int)getWidth()-8, w);
		}
	
		@Override
		protected void onMouseIn() {
			hover = true;
			requestRepaint();
		}
		
		@Override
		protected void onMouseOut() {
			hover = false;
			requestRepaint();
		}
		
		@Override
		protected boolean onMouseDown(float x, float y, int buttons) {
			if(buttons==mouseLeftMask) {
				FileGroupBox grp = (FileGroupBox) getParent();
				grp.toggleView();
				requestRepaint();
				return true;
			}
			else
				return false;
		}
	}

	public static class FileGroupBoxBody extends UIContainer {
		
		public FileGroupBoxBody(FileGroupBox parent) {
			super(parent);
		}
		
		@Override
		protected void layout() {
			float w = LIST_ITEM_WIDTH;
			float h = LIST_ITEM_HEIGHT;
			float maxw = getWidth();
			float y = 0f;
			float x = 0f; 
			for(UIElement e : children) {
				if(x+w>maxw) {
					x = 0f;
					y += h;
				}
				e.setLocation(x, y);
				e.setSize(w, h);
				x += w;
			}
			setSize(getWidth(), y+h);
		}
		
	}

	public static class FileGroupBox extends UIContainer implements Comparable<FileGroupBox> {
		public final FileBrowser fileBrowser;
		public int order;
		public String title;
		private final FileGroupBoxHeader header;
		private final FileGroupBoxBody body;
		private int numFiles = 0;
		
		public FileGroupBox(FileBrowser fileBrowser, int order, String title) {
			super(fileBrowser.view.getView());
			this.fileBrowser = fileBrowser;
			this.title = title;
			this.order = order;
			this.header = new FileGroupBoxHeader(this);
			this.body = new FileGroupBoxBody(this);
		}
		
		public void addFile(File file) {
			numFiles++;
			new FileListItem(body, fileBrowser, file);
		}
		
		public int getNumFiles() {
			return numFiles;
		}
		
		public boolean isViewOpen() {
			return body.isVisible();
		}
		
		public void toggleView() {
			body.setVisible(!body.isVisible());
			invalidateLayout();
		}
		
		@Override
		protected void layout() {
			float w = getWidth();
			header.setLocation(0, 0);
			header.setSize(w, StdPainter.instance.fontSize+8);
			if(body.isVisible()) {
				body.setLocation(0, header.getHeight());
				body.setSize(w, 0);
				body.layout();
				setSize(w, body.getHeight()+header.getHeight()+8);
			}
			else {
				setSize(w, header.getHeight()+8);
			}
		}
		
		@Override
		public int compareTo(FileGroupBox o) {
			int res = Integer.compare(order, o.order);
			if(res==0) {
				res = title.compareToIgnoreCase(o.title);
			}
			return res;
		}
	}
	
	public static class FileViewPane extends UIScrollContainer {
		private File directory;
		private File selectedFile = null;
		private List<FileGroupBox> groups = new ArrayList<>();
		private String[] groupTypes = null;
		public boolean autoTypes = false;
		
		public FileViewPane(FileBrowser fileBrowser, String[] groupTypes, boolean autoTypes) {
			super(fileBrowser);
			this.groupTypes = autoTypes ? null : groupTypes;
			this.autoTypes = autoTypes;
		}
		
		public boolean setDirectory(File directory) {
			FileBrowser fileBrowser = (FileBrowser) getParent();
			File[] files = null;
			if(directory==null) {
				fileBrowser.btnUp.disable();
				files = File.listRoots();
			}
			else {
				fileBrowser.btnUp.enable();
				directory = Paths.get(directory.toURI()).normalize().toFile();
				files = directory.listFiles();
			}
			if(files==null)
				return false;
			this.directory = directory;
			fileBrowser.txtPath.text = directory==null ? "This computer" : directory.getAbsolutePath();
			
			Arrays.sort(files, new Comparator<File>() {
				@Override
				public int compare(File o1, File o2) {
					int res = Boolean.compare(!o1.isDirectory(), !o2.isDirectory());
					if(res==0) {
						res = o1.getName().compareToIgnoreCase(o2.getName());
					}
					return res;
				}
			});
			
			getView().removeAllChildren();
			groups.clear();
			FileGroupBox dirGroup = null;
			FileGroupBox rootGroup = null;
			FileGroupBox allGroup = null;
			HashMap<String, FileGroupBox> groupMap = new HashMap<>();
			for(File file : files) {
				if(file.isDirectory()) {
					if(file.getName().isEmpty()) {
						if(rootGroup==null) {
							rootGroup = new FileGroupBox(fileBrowser, -1, "File systems");
							groups.add(rootGroup);
						}
						rootGroup.addFile(file);
					}
					else {
						if(dirGroup==null) {
							dirGroup = new FileGroupBox(fileBrowser, 0, "Folders");
							groups.add(dirGroup);
						}
						dirGroup.addFile(file);
					}
				}
				else {
					String type = null;
					if(autoTypes || groupTypes!=null) {
						String fileName = file.getName();
						int dotIndex = fileName.lastIndexOf('.');
						if(!startsWithSymbol(fileName) && dotIndex>0) {
							String ext = fileName.substring(dotIndex+1);
							if(autoTypes) {
								type = ext.toLowerCase();
							}
							else {
								for(String t : groupTypes) {
									if(t.equalsIgnoreCase(ext)) {
										type = t;
										break;
									}
								}
							}
						}
					}
					if(type==null) {
						if(allGroup==null) {
							allGroup = new FileGroupBox(fileBrowser, 2, groupTypes==null && !autoTypes ? "All files" : "All other files");
							groups.add(allGroup);
						}
						allGroup.addFile(file);
					}
					else {
						FileGroupBox grp = groupMap.get(type);
						if(grp==null) {
							grp = new FileGroupBox(fileBrowser, 1, type.toUpperCase()+" files");
							groupMap.put(type, grp);
							groups.add(grp);
						}
						grp.addFile(file);
					}
				}
			}
			Collections.sort(groups);
			
			selectedFile = null;
			onSelect(selectedFile);
			return true;
		}
		
		@Override
		protected float layoutView() {
			float w = getWidth();
			float y = 0f;
			for(FileGroupBox grp : groups) {
				grp.setLocation(0, y);
				grp.setSize(w, 0);
				grp.layout();
				y += grp.getHeight();
			}
			return y;
		}
		
		public File getDirectory() {
			return directory;
		}
		
		public void refresh() {
			setDirectory(directory);
		}
		
		public boolean upDirectory() {
			if(directory==null)
				return false;
			Path path = Paths.get(directory.toURI());
			Path parent = path.getParent();
			if(parent!=null)
				return setDirectory(parent.toFile());
			else
				return setDirectory(null);
		}
		
		public File getSelectedFile() {
			return selectedFile;
		}
		
		public void setSelectedFile(File file) {
			this.selectedFile = file;
		}
		
		public void onSelect(File file) {
			((FileBrowser) getParent()).txtFileName.text = file==null ? "" : file.getName();
			requestRepaint();
		}
		
		public void onClickSelected() {
			if(selectedFile!=null) {
				if(selectedFile.isDirectory()) {
					if(setDirectory(selectedFile)) {
						FileBrowser fileBrowser = (FileBrowser) getParent();
						fileBrowser.pushHistory();
					}
				}
			}
		}
		
		@Override
		protected void paintSelf(Graphics2D g2) {
			g2.setColor(StdPainter.instance.colorTextBg);
			g2.fillRect(0, 0, (int)getWidth(), (int)getHeight());
		}
		
		@Override
		protected void paintChildren(Graphics2D g2) {
			super.paintChildren(g2);
			StdPainter painter = StdPainter.instance;
			g2.setColor(painter.colorBorder);
			g2.drawRect(0, 0, (int)getWidth(), (int)getHeight());
		}
	}

	public final FileViewPane view;
	public final UITextBox txtFileName, txtPath;
	private UIButtonBase btnBack, btnFwd, btnRefresh, btnUp, btnHome, btnRoots, btnOk, btnCancel;
	public LinkedList<File> history = new LinkedList<>();
	public int historyIndex = -1;
	
	public FileBrowser(UIContainer parent) {
		super(parent);
		view = new FileViewPane(this, null, true);
		txtPath = new UITextBox(this);
		txtFileName = new UITextBox(this);
		btnBack = new UIToolButton(this, new SvgIcon("svg/back.svg", 160, StdPainter.instance.iconPalette), 16, 2) {
			public void onAction() {
				if(historyIndex>0) {
					historyIndex--;
					view.setDirectory(history.get(historyIndex));
					if(historyIndex==0)
						btnBack.disable();
					if(historyIndex<history.size()-1)
						btnFwd.enable();
				}
				requestRepaint();
			}
		}.disable();
		btnFwd = new UIToolButton(this, new SvgIcon("svg/forward.svg", 160, StdPainter.instance.iconPalette), 16, 2) {
			public void onAction() {
				if(historyIndex<history.size()-1) {
					historyIndex++;
					view.setDirectory(history.get(historyIndex));
					if(historyIndex>=history.size()-1)
						btnFwd.disable();
					if(historyIndex>0)
						btnBack.enable();
				}
				requestRepaint();
			}
		}.disable();
		btnRefresh = new UIToolButton(this, new SvgIcon("svg/refresh.svg", 160, StdPainter.instance.iconPalette), 16, 2) {
			public void onAction() {
				view.refresh();				
				requestRepaint();
			}
		};
		btnUp = new UIToolButton(this, new SvgIcon("svg/up.svg", 160, StdPainter.instance.iconPalette), 32, 8) {
			public void onAction() {
				if(view.upDirectory())
					pushHistory();
				requestRepaint();
			}
		};
		btnHome = new UIToolButton(this, new SvgIcon("svg/home.svg", 160, StdPainter.instance.iconPalette), 32, 8) {
			public void onAction() {
				if(view.setDirectory(new File(System.getProperty("user.home"))))
					pushHistory();
				requestRepaint();
			}
		};
		btnRoots = new UIToolButton(this, new SvgIcon("svg/roots.svg", 160, StdPainter.instance.iconPalette), 32, 8) {
			public void onAction() {
				if(view.setDirectory(null))
					pushHistory();
				requestRepaint();
			}
		};
		btnOk = new UIButton(this, "OK");
		btnCancel = new UIButton(this, "Cancel") {
			@Override
			public void onAction() {
				System.exit(0);
			}
		};
		view.setDirectory(new File("."));
		pushHistory();
	}
	
	public void pushHistory() {
		historyIndex++;
		while(history.size()>historyIndex)
			history.removeLast();
		history.add(view.getDirectory());
		if(historyIndex>0)
			btnBack.enable();
		btnFwd.disable();
	}
	
	@Override
	protected void layout() {
		StdPainter painter = StdPainter.instance;
		float w = getWidth();
		float h = getHeight();
		float top = txtFileName.getHeight()+16;
		float viewh = h-24-painter.buttonHeight*2-top;
		view.setLocation(56, top);
		view.setSize(w-56, viewh);
		txtFileName.setLocation(56, h-painter.buttonHeight*2-16);
		txtFileName.setSize(w-56-8, txtFileName.getHeight());
		txtPath.setLocation(56, 8);
		txtPath.setSize(w-56-4-28, txtFileName.getHeight());
		btnBack.setLocation(28-22, 8);
		btnFwd.setLocation(28+2, 8);
		btnRefresh.setLocation(w-28, 8);
		btnUp.setLocation(4, top+4);
		btnHome.setLocation(4, top+viewh-48*2-4);
		btnRoots.setLocation(4, top+viewh-48-4);
		btnOk.setLocation(w-painter.buttonWidth*2-12, h-painter.buttonHeight-8);
		btnCancel.setLocation(w-painter.buttonWidth-8, h-painter.buttonHeight-8);
		super.layout();
	}
	
	@Override
	protected void paintSelf(Graphics2D g2) {
		StdPainter painter = StdPainter.instance;
		int w = (int)getWidth();
		int h = (int)getHeight();
		int top = (int)(txtFileName.getHeight()+16);
		int viewh = h-24-painter.buttonHeight*2-top;
		
		g2.setColor(painter.colorBgLight);
		g2.fillRect(0, 0, w, top);
		g2.setColor(painter.colorBg);
		g2.fillRect(0, top, w, h-top);
		
		g2.setColor(painter.colorBgDark);
		g2.fillRect(0, top, 56, viewh);
		g2.setColor(painter.colorBorderLight);
		g2.drawLine(0, top, w, top);
		g2.drawLine(0, top+viewh, 56, top+viewh);
		
		g2.setFont(painter.font);
		g2.setColor(painter.colorFg);
		TextUtils.drawString(g2, "File:", 52, (int)(txtFileName.getY()+txtFileName.getHeight()/2f), TextUtils.RIGHT, TextUtils.CENTER);
	}
	
	/*@Override
	public void paint(Graphics2D g2) {
		long t = System.currentTimeMillis();
		super.paint(g2);
		System.out.printf("Paint time: %d ms\n", System.currentTimeMillis()-t);
	}*/

	public static void main(String[] args) {
		JFrame frame = new JFrame("Open file");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		BasePanel base = new BasePanel();
		base.setPreferredSize(new Dimension(1040, 600));
		// base.getBaseContainer().setBaseScale(1f);
		
		new FileBrowser(base.getBaseContainer());
		
		frame.setContentPane(base);
		frame.pack();
		frame.setVisible(true);

	}
}
