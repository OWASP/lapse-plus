package lapsePlus.views;

/*
* MethodFinder.java, version 2.8, 2010
*/

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;

public class MethodFinder {
	IFile resource;
	//MethodFinderVisitor visitor = new MethodFinderVisitor();

	public MethodFinder(IFile resource){
		this.resource = resource;
	}
	public IMethod convertMethodDecl2IMethod(MethodDeclaration methodDecl){
		SimpleName methodName = methodDecl.getName();
		//cu.accept(visitor);
		
		try {
			ICompilationUnit iCompilationUnit = JavaCore.createCompilationUnitFrom((IFile) resource);
			
			int startPos = methodDecl.getStartPosition();
			IJavaElement element = iCompilationUnit.getElementAt(startPos);
			if(element instanceof IMethod) {
				return (IMethod) element;
			}
			return null;
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return null;
	}
	/*
	class MethodFinderVisitor extends ASTVisitor {
		public boolean visit(ITypeBinding node) {
			System.out.println(">>> Visiting " + node);
			return true;
		}
		public boolean visit(MethodDeclaration node) {
			System.out.println(">>> Visiting " + node);
			return true;
		}
	}
	*/
}
