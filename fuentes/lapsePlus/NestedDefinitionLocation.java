package lapsePlus;

/*
* NestedDefinitionLocation.java, version 2.8, 2010
*/

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.dom.ASTNode;

public class NestedDefinitionLocation extends DefinitionLocation {
	NestedDefinitionLocation parent;
	private Collection<DefinitionLocation> children;
	boolean recursive = false;
	
	public NestedDefinitionLocation(String name, IResource resource, int lineNumber, ASTNode ast, NestedDefinitionLocation parent){
		super(name, resource, lineNumber, ast);
		
		this.parent = parent;
		
		if(parent != null) {
			// register children
			parent.addChild(this);
		}
	}
	
	public NestedDefinitionLocation(String name, IResource resource, int lineNumber, ASTNode ast){
		this(name, resource, lineNumber, ast, null);
	}
	
	private void addChild(DefinitionLocation location) {
		if(children == null) {
			children = new ArrayList<DefinitionLocation>();
		}
		children.add(location);			
	}
	
	public NestedDefinitionLocation getParent() {
		return parent;
	}
	
	/**
	 * @return Returns the children.
	 */
	public Collection getChildren() {
		return children;
	}
	
	public boolean hasChildren() {
		return children != null && children.size() > 0;
	}
	
	public boolean containsAncestor(ASTNode name) {
//		System.out.println(
//				"Checking " + getASTNode() + ", " + getASTNode().hashCode() + " vs " + 
//				name + ", " + name.hashCode());
		if(parent == null) {
			return false;	
		}
		
		if(same(parent.getASTNode(), name)) {
			logError(			
				getParent().getASTNode() + " at " + parent.getASTNode().getStartPosition() + " and " +
				name + " at " + name.getStartPosition() + 
				" matched."
				);
			return true;
		} else {
			return getParent().containsAncestor(name);
		}
	}

	/**
	 * Fast and, hopefully, correct comparator of AST nodes.
	 * */
	private boolean same(ASTNode node1, ASTNode node2) {
		if(!node1.toString().equals(node2.toString())) return false;
		if(node1.getStartPosition() != node2.getStartPosition()) return false;
		if(node1.getLength() != node2.getLength()) return false;
		
		return true;		
	}

	public int getDepth() {
		if(getParent() == null) return 1;
		return getParent().getDepth() + 1;
	}
	
	public boolean isRecursive() {
		return recursive;
	}
	public void setRecursive(boolean recursive) {
		this.recursive = recursive;
	}

    private static void log(String message, Throwable e) {
        LapsePlugin.trace(LapsePlugin.RECURSION, "NestedDefinitionLocation: " + message, e);
    }
    
    @SuppressWarnings("unused")
	private static void log(String message) {
        log(message, null);
    }
    
    private static void logError(String message) {
        log(message, new Throwable());
    }
}
