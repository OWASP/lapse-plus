package lapsePlus;

/*
 * XMLConfig.java, version 2.8, 2010
*/

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jface.util.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class XMLConfig {
	private static boolean TRACE = false;
	static Map<IMethod, SinkDescription> method2desc = new HashMap<IMethod, SinkDescription>();

	public static class SinkDescription {
		public int getVulnerableParameter() {
			return vulnerableParameter;
		}
		String id, typeName, methodName, categoryName;
		private int vulnerableParameter;		
		
		protected SinkDescription(String id, String typeName, String methodName, String categoryName, 
				int parameterCount, int vulnerableParameter) 
		{
			this.id = id;
			this.typeName = typeName;
			this.methodName = methodName;
			this.categoryName = categoryName;
			this.vulnerableParameter = vulnerableParameter; 
		}

		public SinkDescription(String id, String categoryName, int vulnerableParameter){
			this.id = id;
			
			int right_bracker_idx = id.lastIndexOf(')');
			int left_bracker_idx = id.lastIndexOf('(');
			String param = id.substring(left_bracker_idx+1, right_bracker_idx);
			int comma_count = 0;
			for(int i=0; i < param.length(); i++) {
			    char c = param.charAt(i);
			    if(c == ',') {
			        comma_count++;
			    }
			}
			int dot_index = id.lastIndexOf('.'); 
			
			this.typeName = id.substring(0, dot_index);
			this.methodName = id.substring(0, left_bracker_idx);
			this.categoryName = categoryName;
			this.vulnerableParameter = vulnerableParameter; 
		}

		public String toString(){
			return "Sink: " + (typeName + ", " + methodName + ", " + categoryName);
		}
		public String getCategoryName() {
			return categoryName;
		}
		public String getMethodName() {
			return methodName;
		}
		public String getTypeName() {
			return typeName;
		}
		public String getID() {
			return id;
		}
	}
	
	public static class SourceDescription {
		String typeName, methodName, categoryName, id;
		
		public String getCategoryName() {
			return categoryName;
		}
		public String getMethodName() {
			return methodName;
		}
		public String getTypeName() {
			return typeName;
		}
		protected SourceDescription(String id, String typeName, String methodName, String categoryName) {
			this.id = id;
			this.typeName = typeName;
			this.methodName = methodName;
			this.categoryName = categoryName;
		}
		
		public SourceDescription(String id, String categoryName){
			this.id = id;
			
			//int right_bracker_idx = id.lastIndexOf(')');
			int left_bracker_idx = id.lastIndexOf('(');
			//String param = id.substring(left_bracker_idx+1, right_bracker_idx);
			int dot_index = id.lastIndexOf('.'); 
			
			this.typeName = id.substring(0, dot_index);
			this.methodName = id.substring(0, left_bracker_idx);
			this.categoryName = categoryName; 
		}

		public String toString(){
			return "Source: " + (typeName + ", " + methodName + ", " + categoryName);
		}
		public String getID() {
			return this.id;
		}		
	}

	public static class SafeDescription {
		String typeName, methodName, id;
		
		public String getMethodName() {
			return methodName;
		}
		public String getTypeName() {
			return typeName;
		}
		public SafeDescription(String id, String typeName, String methodName) {
			this.id = id;
			this.typeName = typeName;
			this.methodName = methodName;
		}

		public String toString(){
			return "Safe: " + (typeName + ", " + methodName);
		}
		public String getID() {
			return this.id;
		}
	}
    public static class DerivationDescription {
        String typeName, methodName, id;
        
   
      public String getMethodName() {
            return methodName;
        }
        public String getTypeName() {
            return typeName;
        }
        public DerivationDescription(String id, String typeName, String methodName){
            this.id = id;
            this.typeName = typeName;
            this.methodName = methodName;
        }

        public String toString(){
            return "Derivator: " + (typeName + ", " + methodName);
        }
        public String getID() {
            return this.id;
        }       
    }
    public static class ReflectorDescription {
        String typeName, methodName, categoryName, id;
        
        public String getCategoryName() {
            return categoryName;
        }
        public String getMethodName() {
            return methodName;
        }
        public String getTypeName() {
            return typeName;
        }
        public ReflectorDescription(String id, String typeName, String methodName, String categoryName) {
            this.id = id;
            this.typeName = typeName;
            this.methodName = methodName;
            this.categoryName = categoryName;
        }

        public String toString(){
            return "Reflector: " + (typeName + ", " + methodName + ", " + categoryName);
        }
        public String getID() {
            return this.id;
        }       
    }
	
    public static void main(String argv []){
        TRACE = true;
    	Collection<SourceDescription> sources           = readSources("sources.xml", "");
    	Collection<SinkDescription> sinks 	            = readSinks("sinks.xml", "");
    	Collection<DerivationDescription> derived 	    = readDerivators("derived.xml", "");
    	
        if(TRACE) {
            System.out.println(sources);
            System.out.println(sinks);
            System.out.println(derived);
        }
    }

	public static Collection<SinkDescription> readSinks(String fileName, String base) {
		LinkedList<SinkDescription> result = new LinkedList<SinkDescription>(); 
		
		try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new File(base + fileName));

            // normalize text representation
            doc.getDocumentElement ().normalize ();
            if(TRACE) System.out.println ("Root element of the doc is " + doc.getDocumentElement().getNodeName());

            NodeList sinks = doc.getElementsByTagName("sink");
            if(TRACE) System.out.println("Total no of sinks: " + sinks.getLength());

            for(int s=0; s < sinks.getLength() ; s++){                
                if(sinks.item(s).getNodeType() == Node.ELEMENT_NODE){
                	Element sinkNode = (Element) sinks.item(s);
                	Assert.isNotNull(sinkNode);
                	String id = sinkNode.getAttribute("id");
                	Assert.isNotNull(id);                	
                	
                    //String typeName 	= sinkNode.getElementsByTagName("type").item(0).getChildNodes().item(0).getNodeValue().trim();
                    //String methodName 	= sinkNode.getElementsByTagName("method").item(0).getChildNodes().item(0).getNodeValue().trim();
                    String categoryName = sinkNode.getElementsByTagName("category").item(0).getChildNodes().item(0).getNodeValue().trim();
                    int parameterCount  = new Integer(sinkNode.getElementsByTagName("paramCount").item(0).getChildNodes().item(0).getNodeValue().trim()).intValue();
                    int vulnerableParameter = new Integer(sinkNode.getElementsByTagName("vulnParam").item(0).getChildNodes().item(0).getNodeValue().trim()).intValue();
                    
                    SinkDescription desc = new SinkDescription(id, categoryName, vulnerableParameter);
                    result.add(desc);
                    if(TRACE) System.out.println(desc);
                }//end of if clause
            }//end of for loop with s var
        } catch (SAXParseException err) {
	        System.out.println ("** Parsing error" + ", line " + err.getLineNumber () + ", uri " + err.getSystemId ());
	        System.out.println(" " + err.getMessage ());
	        return null;
        }catch (SAXException e) {
	        Exception x = e.getException ();
	        ((x == null) ? e : x).printStackTrace ();
	        return null;
        }catch (Throwable t) {
        	t.printStackTrace ();
        	return null;  
        }
        
        return result;
	}
	
	public static Collection<SinkDescription> readSinks(String fileName) {
		return readSinks("sinks.xml", getPlugingBasePath());
	}
	
	public static Collection<SourceDescription> readSources(String fileName, String base) {
		LinkedList<SourceDescription> result = new LinkedList<SourceDescription>(); 
		try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new File(base + fileName));

            // normalize text representation
            doc.getDocumentElement ().normalize ();
            if(TRACE) System.out.println ("Root element of the doc is " + doc.getDocumentElement().getNodeName());

            NodeList sources = doc.getElementsByTagName("source");
            if(TRACE) System.out.println("Total no of sources: " + sources.getLength());

            for (int s = 0; s < sources.getLength(); s++) {                
                if(sources.item(s).getNodeType() == Node.ELEMENT_NODE){
                	Element sourceNode = (Element) sources.item(s);
                	Assert.isNotNull(sourceNode);

                	String id 			= sourceNode.getAttribute("id");                	
                    //String typeName 	= sourceNode.getElementsByTagName("type").item(0).getChildNodes().item(0).getNodeValue().trim();
                    //String methodName 	= sourceNode.getElementsByTagName("method").item(0).getChildNodes().item(0).getNodeValue().trim();
                    String categoryName = sourceNode.getElementsByTagName("category").item(0).getChildNodes().item(0).getNodeValue().trim();
                    
                    SourceDescription desc = new SourceDescription(id, categoryName);
                    result.add(desc);
                    if(TRACE) System.out.println(desc);
                }//end of if clause
            }//end of for loop with s var
        } catch (SAXParseException err) {
	        System.out.println ("** Parsing error" + ", line " + err.getLineNumber () + ", uri " + err.getSystemId ());
	        System.out.println(" " + err.getMessage ());
	        return null;
        }catch (SAXException e) {
	        Exception x = e.getException ();
	        ((x == null) ? e : x).printStackTrace ();
	        return null;
        }catch (Throwable t) {
        	t.printStackTrace ();
        	return null;  
        }
        
        return result;
	}
    
	public static Collection<SafeDescription> readSafes(String fileName, String base) {
		LinkedList<SafeDescription> result = new LinkedList<SafeDescription>(); 
		try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new File(base + fileName));

            // normalize text representation
            doc.getDocumentElement ().normalize ();
            if(TRACE) System.out.println ("Root element of the doc is " + doc.getDocumentElement().getNodeName());

            NodeList sources = doc.getElementsByTagName("safe");
            if(TRACE) System.out.println("Total no of safes: " + sources.getLength());

            for(int s=0; s<sources.getLength() ; s++){                
                if(sources.item(s).getNodeType() == Node.ELEMENT_NODE){
                	Element sourceNode = (Element) sources.item(s);
                	Assert.isNotNull(sourceNode);

                	String id 			= sourceNode.getAttribute("id");                	
                    String typeName 	= sourceNode.getElementsByTagName("type").item(0).getChildNodes().item(0).getNodeValue().trim();
                    String methodName 	= sourceNode.getElementsByTagName("method").item(0).getChildNodes().item(0).getNodeValue().trim();
                    
                    SafeDescription desc = new SafeDescription(id, typeName, methodName);
                    result.add(desc);
                    if(TRACE) System.out.println(desc);
                }//end of if clause
            }//end of for loop with s var
        } catch (SAXParseException err) {
	        System.out.println ("** Parsing error" + ", line " + err.getLineNumber () + ", uri " + err.getSystemId ());
	        System.out.println(" " + err.getMessage ());
	        return null;
        }catch (SAXException e) {
	        Exception x = e.getException ();
	        ((x == null) ? e : x).printStackTrace ();
	        return null;
        }catch (Throwable t) {
        	t.printStackTrace ();
        	return null;  
        }
        
        return result;
	}
	public static Collection<DerivationDescription> readDerivators(String fileName) {
		return readDerivators(fileName, getPlugingBasePath());
	}
	
	 public static Collection<DerivationDescription> readDerivators(String fileName, String base) {
	        LinkedList<DerivationDescription> result = new LinkedList<DerivationDescription>(); 
	        
	            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
	            Document doc = null;
	            try {
		            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		            doc = docBuilder.parse(new File(base + fileName));
	            }catch (SAXParseException err) {
	                System.out.println ("** Parsing error" + ", line " + err.getLineNumber () + ", uri " + err.getSystemId ());
	                System.out.println(" " + err.getMessage ());
	                return null;
	            }catch (SAXException e) {
	                Exception x = e.getException ();
	                ((x == null) ? e : x).printStackTrace ();
	                return null;
	            } catch (IOException e) {
	                e.printStackTrace();
	                return null;
	            } catch (ParserConfigurationException e) {
	                e.printStackTrace();
	                return null;
	            }

	            // normalize text representation
	            doc.getDocumentElement ().normalize ();
	            if(TRACE) System.out.println ("Root element of the doc is " + doc.getDocumentElement().getNodeName());

	            NodeList derivators = doc.getElementsByTagName("derivation");
	            if(TRACE) System.out.println("Total no of reflectors: " + derivators.getLength());

	            for(int s=0; s<derivators.getLength() ; s++){                
	                if(derivators.item(s).getNodeType() == Node.ELEMENT_NODE){
	                    try {
		                    Element reflectorNode = (Element) derivators.item(s);
		                    Assert.isNotNull(reflectorNode);
		
		                    String id           = reflectorNode.getAttribute("id");                    
		                    String typeName     = reflectorNode.getElementsByTagName("type").item(0).getChildNodes().item(0).getNodeValue().trim();
		                    String methodName   = reflectorNode.getElementsByTagName("method").item(0).getChildNodes().item(0).getNodeValue().trim();
		                   
		                    
		                    DerivationDescription desc = new DerivationDescription(id, typeName, methodName);
		                    result.add(desc);
		                    if (TRACE) System.out.println(desc);
	                    } catch (Throwable t) {
	                        t.printStackTrace ();
	                        return null;  
	                    }
	                }//end of if clause
	            }//end of for loop with s var
	            
	        return result;
	    }
    public static Collection<ReflectorDescription> readReflectors(String fileName, String base) {
        LinkedList<ReflectorDescription> result = new LinkedList<ReflectorDescription>(); 
        
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            Document doc = null;
            try {
	            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
	            doc = docBuilder.parse(new File(base + fileName));
            }catch (SAXParseException err) {
                System.out.println ("** Parsing error" + ", line " + err.getLineNumber () + ", uri " + err.getSystemId ());
                System.out.println(" " + err.getMessage ());
                return null;
            }catch (SAXException e) {
                Exception x = e.getException ();
                ((x == null) ? e : x).printStackTrace ();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
                return null;
            }

            // normalize text representation
            doc.getDocumentElement ().normalize ();
            if(TRACE) System.out.println ("Root element of the doc is " + doc.getDocumentElement().getNodeName());

            NodeList sources = doc.getElementsByTagName("reflector");
            if(TRACE) System.out.println("Total no of reflectors: " + sources.getLength());

            for(int s=0; s<sources.getLength() ; s++){                
                if(sources.item(s).getNodeType() == Node.ELEMENT_NODE){
                    try {
	                    Element reflectorNode = (Element) sources.item(s);
	                    Assert.isNotNull(reflectorNode);
	
	                    String id           = reflectorNode.getAttribute("id");                    
	                    String typeName     = reflectorNode.getElementsByTagName("type").item(0).getChildNodes().item(0).getNodeValue().trim();
	                    String methodName   = reflectorNode.getElementsByTagName("method").item(0).getChildNodes().item(0).getNodeValue().trim();
	                    String categoryName = reflectorNode.getElementsByTagName("category").item(0).getChildNodes().item(0).getNodeValue().trim();
	                    
	                    ReflectorDescription desc = new ReflectorDescription(id, typeName, methodName, categoryName);
	                    result.add(desc);
	                    if (TRACE) System.out.println(desc);
                    } catch (Throwable t) {
                        t.printStackTrace ();
                        return null;  
                    }
                }//end of if clause
            }//end of for loop with s var
            
        return result;
    }
	
	public static Collection<SourceDescription> readSources(String fileName) {
		return readSources(fileName, getPlugingBasePath());
	}
	
	public static Collection<SafeDescription> readSafes(String fileName) {
		return readSafes(fileName, getPlugingBasePath());
	}
    
    public static Collection<ReflectorDescription> readReflectors(String fileName) {
        return readReflectors(fileName, getPlugingBasePath());
    }
	
	public static String getPlugingBasePath(){
		LapsePlugin plugin = LapsePlugin.getDefault();
		URL base = plugin.getDescriptor().getInstallURL();
		
		try {
			return FileLocator.toFileURL(base).getFile() + "/";
		} catch (IOException e) {		
			e.printStackTrace();
			return "";
		}	
	}
	
//	private static void saveDescription(IMethod method, SinkDescription desc) {
//		method2desc.put(method, desc);		
//	}
	
	public static SinkDescription getDescription(IMethod method) {
		return method2desc.get(method);
	}
}