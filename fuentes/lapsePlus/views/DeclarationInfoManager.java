package lapsePlus.views;

/*
* DeclarationInfoManager.java,version 2.8, 2010
*/

import java.util.HashMap;
import java.util.Map;

import lapsePlus.LapsePlugin;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class DeclarationInfoManager {
	
	static Map<CompilationUnit, DeclarationInfo> unit2info = new HashMap<CompilationUnit, DeclarationInfo>();
	//static Map<CompilationUnit, DeclarationInfo> unit2info;
	
	public static DeclarationInfo retrieveDeclarationInfo(CompilationUnit unit) {
		DeclarationInfo info  = (DeclarationInfo) unit2info.get(unit);
		if(info == null) {
			DeclarationFinderVisitor visitor = new DeclarationFinderVisitor();
			unit.accept(visitor);
			info = visitor.getInfo();
			// save for future use
			unit2info.put(unit, info);
			visitor = null;
		}
		
		return info;
	}
	
	/*public static void initializeDeclarationMap(){
		
		unit2info = new HashMap<CompilationUnit, DeclarationInfo>();
		
	}*/
	
	public static class DeclarationInfo {
		
		Map<String, VariableDeclaration> name2node = new HashMap<String, VariableDeclaration>();
		private HashMap<SimpleName, Boolean> finalMap = new HashMap<SimpleName, Boolean>();
		
		public void setVariableDeclaration(String name, VariableDeclaration node) {
			log("Entering " + name + " -> " + node);
			name2node.put(name, node);
		}
		
		public VariableDeclaration getVariableDeclaration(String name) {
			log("Requesting " + name);
			return (VariableDeclaration) name2node.get(name);
		}
		
		public void setFinal(SimpleName name) {
			log("Making " + name + " final");
			finalMap.put(name, null);
		}
		
		public boolean isFinal(SimpleName name) {
			log("Requesting final status for " + name);
			return finalMap.get(name) != null;
		}
		
		public VariableDeclaration getVariableDeclaration(SimpleName name) {
			ASTNode node = name;
			do {
				node = node.getParent();
			} while ( node != null && !(node instanceof MethodDeclaration) );
			
			String key = null;
			
			if(node != null) {
				key = node.toString() + "/" + name.getFullyQualifiedName();
			}else {
				key = "GLOBAL" + name.getFullyQualifiedName();	
			}
			
			logError("Trying " + key);
			VariableDeclaration var = getVariableDeclaration(key);
			if(var == null && node != null) {
				key = "GLOBAL" + name.getFullyQualifiedName();
				log("Trying " + key);
				var = getVariableDeclaration(key);
			}
			
			return var;
		}
	
		public String toString() {
			return "[VariableDeclaration for " + name2node.size() + "] name(s)"; 
		}
	}
	
	public static class DeclarationFinderVisitor extends ASTVisitor {
		
		DeclarationInfo info = new DeclarationInfo();
		MethodDeclaration fCurrentMethod = null;
	
		//////////////////////////////// VISITORS START //////////////////////////////// 
		public boolean visit(MethodDeclaration node) {
			fCurrentMethod = node;
			return true;
		}
		
		public boolean visit(SingleVariableDeclaration node) {
			SimpleName name = node.getName();
			boolean isFinal = Modifier.isFinal(node.getModifiers());
			if(isFinal) {
				info.setFinal(name);
			}
			
			info.setVariableDeclaration(getKey(name), node);
			log("1. Encountered " + node);
			
			return true;		
		}
		
		/*public boolean visit(VariableDeclarationExpression node) {
			for (Iterator iter = node.fragments().iterator(); iter.hasNext();) {
				VariableDeclarationFragment fragment = (VariableDeclarationFragment) iter.next();
				visit(fragment);
			}
			if(TRACE) System.out.println("2. Encountered " + node);
			return true;
		}*/
		
//		public boolean visit(FieldDeclaration node) {
//			for (Iterator iter = node.fragments().iterator(); iter.hasNext();) {
//				VariableDeclarationFragment fragment = (VariableDeclarationFragment) iter.next();
//				visit(fragment);
//			}
//			if(TRACE) System.out.println("3. Encountered " + node);
//			return true;
//		}
//		
		public boolean visit(VariableDeclarationFragment node) {
			info.setVariableDeclaration(getKey(node.getName()), node);
			log("4. Encountered " + node.getName());
			return true;
		}
		
		public void endVisit(MethodDeclaration node) {
			fCurrentMethod = null;
		}
		//////////////////////////////// END OF VISITORS ////////////////////////////////  
		
		private String getKey(SimpleName name) {
			String result = null;
			if(fCurrentMethod != null) {
				result = fCurrentMethod.toString() + "/" + name.getFullyQualifiedName();
			}else {
				result = "GLOBAL" + name.getFullyQualifiedName();
			}
			log("Creating key " + result);
			
			return result;
		}
		
//		public boolean visit(VariableDeclarationStatement node) {
//			if(TRACE) System.err.println("5. Encountered " + node);
//			return true;
//		}
		
		public DeclarationInfo getInfo() {
			return info;
		}
	}
    
     private static void log(String message, Throwable e) {
        LapsePlugin.trace(LapsePlugin.AST_PARSING, message, e);
    }
    
    private static void log(String message) {
        log("Source view: " + message, null);
    }
    
    private static void logError(String message) {
        log(message, new Throwable());
    }
}