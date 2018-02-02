package lapsePlus;

/*
* DefinitionLocation.java, version 2.8, 2010
*/

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.dom.ASTNode;

public class DefinitionLocation {
	public static final int INVALID_SOURCE_LINE = -1;
		
	String name;
	int lineNumber;
	ASTNode ast;
	IResource resource;
	
	DefinitionLocation(String name, IResource resource, int lineNumber, ASTNode ast){
		this.name = name;
        if (this.name.endsWith("\n")) this.name = this.name.substring(0, this.name.length() - 1);
        if (this.name.endsWith(";")) this.name = this.name.substring(0, this.name.length() - 1);
        this.lineNumber = lineNumber;
		this.ast = ast;
		this.resource = resource;
	}
	public int getLineNumber() {
		return lineNumber;
	}
	public String getName() {
		return name;
	}
	public IResource getResource() {
		return resource;
	}
	public String toString() {		
		return /*ast.toString() +*/ 
			name + 
                (lineNumber == INVALID_SOURCE_LINE ? 
					"" :
						resource != null ? 
							(" (" + resource.getName() + ":" + lineNumber + ")") :
					    	"<unknown>");
	}
	public ASTNode getASTNode() {
		return ast;
	}	
}