/*
 * Created on Jun 3, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package lapsePlus;

/*
 * HistoryDefinitionLocation.java, version 2.8, 2010
*/

import java.util.Iterator;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.dom.ASTNode;

public class HistoryDefinitionLocation extends NestedDefinitionLocation {
	/**
	 * Type contants. Make sure to update the string descriptions below if you change
	 * any of these constants. 
	 * */
	public final static int NO_TYPE 			= 0;	// default type
	public final static int CALL_ARG 			= 1;	// actual parameter of a call
	public final static int CALL     			= 2;	// call expression
	public final static int FORMAL_PARAMETER	= 3;	// formal parameter of a method
	public final static int RETURN				= 4;	// formal parameter of a method
	public final static int DECLARATION 		= 5;	// local declaration
	public final static int FIELD		 		= 6;	// field declaration
	public final static int STRING_CONSTANT		= 7;	// string constant or a result of their concatenations
	public final static int STRING_CONCAT		= 8;	// string constant or a result of their concatenations
	public final static int UNDEFINED   		= 9;	// no definition found for some reason
	public final static int INITIAL     		= 10;	// initial query
	public final static int RECURSION			= 11;   // recursive invocation
	public final static int COPY				= 12;   // assignment
	public final static int ARRAYACCESS			= 13;   // array access
	public final static int NULL				= 14;   // array access
	public final static int DERIVATION			= 15;   // array access
	public final static int CLASS_INSTANCE_CREATION		= 16;   // array access
	
	/*final static String[] TYPE_NAMES = {
		"",
        "CALL ARGUMENT",
        "CALL EXPRESSION",
        "FORMAL PARAMETER",
		"RETURN",
        "LOCAL DECLARATION",
        "FIELD DECLARATION",
        "STRING CONSTANT",
        "STRING CONCATENATION",
        "UNDEFINED",
        "INITIAL",
		"RECURSION",
		"COPY"
	};*/
	
    final static String[] TYPE_NAMES = {
        "",
        "argument of a call",
        "call expression",
        "formal argument",
        "return value",
        "local variable declaration",
        "field declaration",
        "string constant",
        "string concatenation operation",
        "undefined value or constant",
        "initial",
        "recursion",
        "assignment operation",
        "array access",
        "null literal",
        "derivation method",
        "class instance creation"
    };
    
	private int type = NO_TYPE;
	boolean maxLevel = false;	

	public HistoryDefinitionLocation(String name, IResource resource, int lineNumber, ASTNode ast, NestedDefinitionLocation parent, int type) {
		super(name, resource, lineNumber, ast, parent);
	
		this.type = type;
	}
	
	public HistoryDefinitionLocation(String name, IResource resource, int lineNumber, ASTNode ast, NestedDefinitionLocation parent) {
		this(name, resource, lineNumber, ast, parent, NO_TYPE);
	}
	
	public int getType() {return type;}
	
	String getTypeName() {return TYPE_NAMES[type];}
	
	public boolean isMaxLevel() {
		return maxLevel;
	}
	public void setMaxLevel(boolean maxLevel) {
		this.maxLevel = maxLevel;		
	}
	
	public boolean isConstant() {
		if(!hasChildren()) {
			return type == STRING_CONSTANT;	
		} else {
			for(Iterator iter = getChildren().iterator(); iter.hasNext();) {
				HistoryDefinitionLocation child = (HistoryDefinitionLocation) iter.next();
				
				if(!child.isConstant()) {
					return false;
				}
			}
			return true;
		}
	}
	
	public String toString() {
		return super.toString() + " [" + getTypeName() + "] " +
			(isMaxLevel()  ? " exceeding the maximum depth" : "") +
			(isRecursive() ? " terminated because of recursion" : "");
	}
}
