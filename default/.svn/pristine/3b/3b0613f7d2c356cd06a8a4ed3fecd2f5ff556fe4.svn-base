package edu.asu.jmars.ui.looknfeel;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import edu.asu.jmars.Main;

@XmlRootElement(name = "font")
@XmlAccessorType(XmlAccessType.FIELD)
public class ThemeFont  {
	
	private static String PATH_TO_MATERIAL_FONTS = "resources/material/fonts/";			
	private static final Font ITALIC = getFont("Roboto-Italic.ttf");	
	private static final Font LIGHT = getFont("Roboto-Light.ttf");
	private static final Font BOLD = getFont("Roboto-Bold.ttf");
	private static final Font MEDIUM = getFont("Roboto-Medium.ttf");
	private static final Font REGULAR = getFont("Roboto-Regular.ttf");
	private static final Font THIN = getFont("Roboto-Thin.ttf");
	private static final Font THIN_ITALIC = getFont("RobotoCondensed-LightItalic.ttf");
	private static final Font BOLD_ITALIC = getFont("Roboto-BoldItalic.ttf");
	
	public enum FONTS {
		ROBOTO(14f), ROBOTO_TEXT(18f), ROBOTO_TABLE(13f), ROBOTO_TABLE_HEADER(13f), 
		ROBOTO_TABLE_COLUMN(13f), ROBOTO_TABLE_ROW(13f), ROBOTO_TAB(16f),
		ROBOTO_CHART_XL(18f), ROBOTO_CHART_LARGE(15f), ROBOTO_CHART_REGULAR(14f),
		ROBOTO_CHART_SMALL(12f),
		NOTO(14f), UNKNOWN(14f);

		private float fontsize;

		FONTS(float size) {
			this.fontsize = size;
		}

		public float fontSize() {
			return fontsize;
		}
	}	
	
	public static Font getFont(String fileName) {
		try {
			Font customFont = Font
					.createFont(Font.PLAIN, Main.getResourceAsStream(PATH_TO_MATERIAL_FONTS + fileName))
					.deriveFont(FONTS.ROBOTO.fontSize());
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			ge.registerFont(customFont);
			return customFont;
		} catch (IOException e) {
			return null;
		} catch (FontFormatException e) {
			return null;
		}
	}
	
	public static String getFontName() {
		return "Roboto";
	}
	
	public static float getRegularFontSize() {
		return 14f;
	}
	
	public static float getTextFontSize() {
		return 18f;
	}

	public static Font getItalic() {
		return ITALIC;
	}

	public static Font getLight() {
		return LIGHT;
	}

	public static Font getBold() {
		return BOLD;
	}

	public static Font getRegular() {
		return REGULAR;
	}

	public static Font getMedium() {
		return MEDIUM;
	}

	public static Font getThin() {
		return THIN;
	}

	public static Font getThinItalic() {
		return THIN_ITALIC;
	}
	
	public static Font getBoldItalic() {
		return BOLD_ITALIC;
	}
	
}
