package edu.asu.jmars.ui.image.factory;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Optional;
import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.transcoder.image.TIFFTranscoder;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import edu.asu.jmars.Main;


public enum SvgConverter {

	PNG {
		@Override
		public BufferedImage convert(ImageDescriptor desc) {
			Transcoder trc = new PNGTranscoder();
			return convertSvg(desc, trc);
		}			
	},

	JPG {
		@Override
		public BufferedImage convert(ImageDescriptor desc) {			
			Transcoder trc = new JPEGTranscoder();
			return convertSvg(desc, trc);
		}
	},

	TIFF {
		@Override
		public BufferedImage convert(ImageDescriptor desc) {	
			Transcoder trc = new TIFFTranscoder();
			return convertSvg(desc, trc);		
		}
	};

	
	public abstract BufferedImage convert(ImageDescriptor desc);
	
	private static BufferedImage convertSvg(ImageDescriptor desc, Transcoder trc)
	{
		ByteArrayOutputStream resultByteStream = new ByteArrayOutputStream();
		BufferedImage img = null;
		InputStream svg = Main.getResourceAsStream(desc.getImageFilePath());
		TranscoderInput input = new TranscoderInput();
		TranscoderOutput transcoderOutput = new TranscoderOutput(resultByteStream);

		try {
			Reader in = new StringReader(updateSVGColors(svg, desc).toString());
			input.setReader(in);
			trc.transcode(input, transcoderOutput);
			resultByteStream.flush();
			img = ImageIO.read(new ByteArrayInputStream(resultByteStream.toByteArray()));
			resultByteStream.close();
		} catch (TranscoderException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return img;
	}
	

	private static StringBuffer updateSVGColors(InputStream svg, ImageDescriptor desc) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(svg));
		StringWriter sout = new StringWriter();
		PrintWriter out = new PrintWriter(sout);		
		Document doc;
		
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(svg);			
			XPath xpath = XPathFactory.newInstance().newXPath();
			
			Optional<Color> fill = desc.getDisplayColor();			
			fill.ifPresent(fillcolor -> {		
				try {
					NodeList nodes = (NodeList) xpath.evaluate("//*[contains(@fill, '#')]", doc, XPathConstants.NODESET);
					if (nodes.getLength() > 0)
					{
					    Node value = nodes.item(0).getAttributes().getNamedItem("fill");
					    if (value != null)
					        value.setNodeValue(getColorAsBrowserHex(fillcolor));
					}
				} catch (XPathExpressionException e) {	
					e.printStackTrace();
				}				
			});		
			
			Optional<Color> strokecolor = desc.getStrokeColor();			
			strokecolor.ifPresent(stroke -> {
				NodeList nodes;
				try {
					nodes = (NodeList) xpath.evaluate("//*[contains(@stroke, '#')]", doc, XPathConstants.NODESET);
					if (nodes.getLength() > 0)
					{
					    Node value = nodes.item(0).getAttributes().getNamedItem("stroke");
					    if (value !=null)					    
					        value.setNodeValue(getColorAsBrowserHex(stroke));
					}
				} catch (XPathExpressionException e) {					
					e.printStackTrace();
				}				
			});			
		
			Transformer xformer;
			xformer = TransformerFactory.newInstance().newTransformer();
			xformer.transform(new DOMSource(doc), new StreamResult(out));

			out.close();
			in.close();

		} catch (SAXException e2) {			
			e2.printStackTrace();
		} catch (ParserConfigurationException e2) {			
			e2.printStackTrace();
		} catch (TransformerConfigurationException e) {			
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {			
			e.printStackTrace();
		} catch (TransformerException e) {		
			e.printStackTrace();
		}

		return sout.getBuffer();
	}
	
	private static String getColorAsBrowserHex(Color color) {
		 String rgb = Integer.toHexString(color.getRGB());		 
	     return "#" + rgb.substring(2, rgb.length());	     
	}
}
