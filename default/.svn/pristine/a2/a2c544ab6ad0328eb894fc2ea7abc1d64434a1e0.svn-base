package edu.asu.jmars.swing;

import edu.asu.jmars.ui.looknfeel.GUITheme;
import edu.asu.jmars.ui.looknfeel.ThemeFont;
import edu.asu.jmars.ui.looknfeel.theme.component.ThemeText;
import edu.asu.jmars.util.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.net.*;
import javax.swing.*;
import static  edu.asu.jmars.ui.looknfeel.Utilities.getColorAsBrowserHex;

public class UrlLabel extends JLabel
 {
	private static DebugLog log = DebugLog.instance();

	private String url;
	private String plain;
	private String under;

	/**
	 * Creates a label that uses the url as the text
	 * for the label, and uses the default color
	 * (theme.getHyperlinkTextColor())
	 * 
	 * Uses an underline state when the cursor enters the
	 * label. (To simulate a hyperlink)
	 * 
	 * Has no icon.
	 * 
	 * @param url  The url for the label to use
	 */
	public UrlLabel(URL url)
	 {
		this(url.toString(), null);
	 }
	/**
	 * Creates a label that uses the url as the text
	 * for the label, and uses the default color
	 * (theme.getHyperlinkTextColor())
	 * 
	 * Uses an underline state when the cursor enters the
	 * label. (To simulate a hyperlink)
	 * 
	 * Has no icon.
	 * 
	 * @param url	The url for the label to use
	 */
	public UrlLabel(String url){
		this(url, null);
	}
	
	/**
	 * Creates a label that uses the url as the text
	 * for the label, and uses the color specified. 
	 * 
	 * Uses an underline state when the cursor enters the
	 * label. (To simulate a hyperlink)
	 * 
	 * Has no icon.
	 * 
	 * @param url	The url for the label
	 * @param color	Color to display as the label foreground
	 * (if color is null, then theme.getHyperlinkTextColor() 
	 * will be used)
	 */
	public UrlLabel(String url, String color)
	 {
		this(url, url, color, null);
	 }
	
	/**
	 * Creates a label with the name given, which will open a 
	 * browser to the specified URL.  Sets the label to the 
	 * specified color.  If no color is passed in then the 
	 * color is the default hyperLink text color from the 
	 * GUITheme.
	 * Uses an underline state when the cursor enters the
	 * label. (To simulate a hyperlink)
	 * Also sets an icon to the RIGHT of the text, and sets
	 * an icon text gap of 10 pixels.
	 * 
	 * @param name	Text displayed in label
	 * @param url	The url which the browser will go to
	 * @param color	Color of the label
	 * @param icon	Icon that is displayed to the right of the text
	 */
	public UrlLabel(String name, String url, String color, Icon icon){
		this.url = url;
		
		String fontFace = ThemeFont.getFontName();
		Color hyperlink = ((ThemeText)GUITheme.get("text")).getHyperlink();
		if(color==null){
			color = getColorAsBrowserHex(hyperlink);
		}
		
		plain = "<html><pre><font color="+color+"><font face="+fontFace+">"+name+"</font></color></pre>";
		under = "<html><pre><font color="+color+"><u><font face="+fontFace+">"+name+"</font></u></color></pre>";		
		
		setText(plain);
		if(icon != null){
			setIcon(icon);
			setHorizontalTextPosition(SwingConstants.LEFT);
			setIconTextGap(10);
		}
		url = url.trim(); //make sure there's no white space in the url (most likely preceeding or following)
		addMouseListener(madapter);
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	}
	

	
	private MouseAdapter madapter = new MouseAdapter() {
		public void mouseClicked(MouseEvent e) {
					if(SwingUtilities.isLeftMouseButton(e)){
						try{
							Util.launchBrowser(url);
						}catch(Exception ex){
							log.aprintln(ex);
							log.aprintln(url);
							JOptionPane.showMessageDialog(
								UrlLabel.this,
								"Unable to open browser due to:\n" + ex,
								"JMARS",
								JOptionPane.ERROR_MESSAGE);
						}
					}
					if(SwingUtilities.isRightMouseButton(e)){
						//Show right-click popup menu (has copy item)
						showMenu(e.getX(), e.getY());
					}
				 }
		public void mouseEntered(MouseEvent ev) {
					setText(under);
				 }
		public void mouseExited(MouseEvent e) {
					setText(plain);
				 }
	};
	
	//Builds and displays the copy popup menu
	private void showMenu(int x, int y){
		JPopupMenu rcMenu = new JPopupMenu();
		JMenuItem copyItem = new JMenuItem(copyAct);
		rcMenu.add(copyItem);
		
		rcMenu.show(this, x, y);
	}
	//Copy url string to clipboard
	private Action copyAct = new AbstractAction("Copy url text"){
		public void actionPerformed(ActionEvent e) {
			StringSelection copyString = new StringSelection(url);
			Clipboard cboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			cboard.setContents(copyString, null);
		}
	};
 }
