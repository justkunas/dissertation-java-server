package util;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * Class used to read and write to the machine config file
 * 
 * @author Justin Jankunas
 *
 */
public class XMLHandler {
	
	public static enum Dimensions { 
		HEIGHT("height"),
		WIDTH("width"),
		LENGTH("length"),
		WEIGHT("weight");
		
		public String tag;
		
		private Dimensions(String tag){
			this.tag = tag;
		}
		
	};
	
	public static String getDimensions(Dimensions tagName,String path){
		String returnValue="";
		try {
			File inputFile = new File(path);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = dbf.newDocumentBuilder();
			Document doc = builder.parse(inputFile);
			NodeList nodeListLvl1 = doc.getElementsByTagName("book");
			Node node = nodeListLvl1.item(0);
			Node nodeLvl2 = ((Element)node).getElementsByTagName("dimensions").item(0);
			Node nodeLvl3 = ((Element)nodeLvl2).getElementsByTagName(tagName.tag).item(0);
						
			return nodeLvl3.getTextContent();
			
		}catch(Exception err){
			//err.printStackTrace();
		}
		return returnValue;
	}
	
	public static String getElement(String tagName,File path){
		String[] toReturn;
		String returnValue="";
		try {
			File inputFile = path;
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = dbf.newDocumentBuilder();
			Document doc = builder.parse(inputFile);
			NodeList nodeListLvl1 = doc.getElementsByTagName("book");
			Node node = nodeListLvl1.item(0);
			Node nodeLvl2 = ((Element)node).getElementsByTagName(tagName).item(0);
			
			if(nodeLvl2.hasChildNodes()){
				NodeList NodeListLvl2 = nodeLvl2.getChildNodes(); 
				toReturn = new String[NodeListLvl2.getLength()];
				
				for(int i = 0; i < NodeListLvl2.getLength(); i++){
					toReturn[i] = NodeListLvl2.item(i).getTextContent();
				}				
				
			}else{
				
				toReturn = new String[1];
				toReturn[1] = nodeLvl2.getTextContent();
				
				System.out.println(toReturn);
				
			}
			
			for(int i = 0; i < toReturn.length; i++){
				if(i == toReturn.length-1){
					returnValue += toReturn[i];
				}else{
					returnValue += toReturn[i] + ",";
				}
			}
			
		}catch(Exception err){
			//err.printStackTrace();
		}
		return returnValue;
	}
	
	public static String getElement(String tagName,String path){
		String[] toReturn;
		String returnValue="";
		try {
			File inputFile = new File(path);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = dbf.newDocumentBuilder();
			Document doc = builder.parse(inputFile);
			NodeList nodeListLvl1 = doc.getElementsByTagName("book");
			Node node = nodeListLvl1.item(0);
			Node nodeLvl2 = ((Element)node).getElementsByTagName(tagName).item(0);
			
			if(nodeLvl2.hasChildNodes()){
				NodeList NodeListLvl2 = nodeLvl2.getChildNodes(); 
				toReturn = new String[NodeListLvl2.getLength()];
				
				for(int i = 0; i < NodeListLvl2.getLength(); i++){
					toReturn[i] = NodeListLvl2.item(i).getTextContent();
				}				
				
			}else{
				
				toReturn = new String[1];
				toReturn[1] = nodeLvl2.getTextContent();
				
				System.out.println(toReturn);
				
			}
			
			for(int i = 0; i < toReturn.length; i++){
				if(i == toReturn.length-1){
					returnValue += toReturn[i];
				}else{
					returnValue += toReturn[i] + ",";
				}
			}
			
		}catch(Exception err){
			//err.printStackTrace();
		}
		return returnValue;
	}
	/**
	 * Sets a specific tag within the machine config file for a specific machine
	 * @param machine Machine to edit
	 * @param tag Tag to edit
	 * @param value New value
	 */
	public static void setElement(String machine, String tag, String value) {
		try {
			File inputFile = new File("Machines.xml");
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = dbf.newDocumentBuilder();
			Document doc = builder.parse(inputFile);
			NodeList nl = doc.getElementsByTagName("book");
			
			System.err.println(inputFile.getAbsolutePath());

			for (int i = 0; i < nl.getLength(); i++) {
				Node nNode = nl.item(i);
				NodeList nodeList = nNode.getChildNodes();
				for (int j = 0; j < nodeList.getLength(); j++) {
					Node jNode = nodeList.item(j);
					if (jNode.getNodeType() == Node.ELEMENT_NODE) {
						Element xEle = (Element) jNode;
						if (xEle.getElementsByTagName("Machine_Name").item(0).getTextContent().equals(machine)) {
							xEle.getElementsByTagName(tag).item(0).setTextContent(value);

						}
					}

				}

			}

			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			Result output = new StreamResult(new File("Machines.xml"));
			Source input = new DOMSource(doc);

			transformer.transform(input, output);
			
		} catch (Exception err) {
			err.printStackTrace();
		}
	}

}
