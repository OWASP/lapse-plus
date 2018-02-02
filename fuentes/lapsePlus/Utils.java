package lapsePlus;

/*
* Utils.java, version 2.8, 2010
*/

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;

/**
 * Some strongly typed pairs... 
 * */
public class Utils {
	public static class ExprUnitResource {
		Expression expr;
		CompilationUnit cu;
		IResource resource;
		
		public ExprUnitResource(Expression expr, CompilationUnit cu, IResource resource) {			
			this.expr = expr;
			this.cu = cu;
			this.resource = resource;
		}
		public CompilationUnit getCompilationUnit() {
			return cu;
		}
		
		/**
		 * This is either a MethodInvocation or a ClassInstanceCreation. 
		 * */
		public Expression getExpression() {
			return expr;
		}
		public IResource getResource() {
			return resource;
		}
	}
	public static class ExprUnitResourceMember extends ExprUnitResource {
		private IMember member;

		public ExprUnitResourceMember(Expression expr, CompilationUnit cu, IResource resource, IMember member) {			
			super(expr, cu, resource);
			this.member = member;
		}

		public IMember getMember() {
			return member;
		}
	}
	
	public static class MethodDeclarationUnitPair {
		MethodDeclaration method;
		CompilationUnit cu;
		IResource resource;
		IMember member;
		
		public MethodDeclarationUnitPair(MethodDeclaration method, CompilationUnit cu, IResource resource, IMember member) {
			this.method = method;
			this.cu = cu;
			this.resource = resource;
			this.member = member;
		}
		public CompilationUnit getCompilationUnit() {
			return cu;
		}
		public MethodDeclaration getMethod() {
			return method;
		}
		public IResource getResource() {
			return resource;
		}
		public IMember getMember() {
			return member;
		}
	}

	public static class ExpressionUnitPair {
		Expression expr;
		CompilationUnit cu;
		IResource resource;
				
		public ExpressionUnitPair(Expression expr, CompilationUnit cu, IResource resource) {
			this.expr = expr;
			this.cu = cu;
			this.resource = resource;
		}
		public CompilationUnit getCompilationUnit() {
			return cu;
		}
		public Expression getExpression() {
			return expr;
		}
		public IResource getResource() {
			return resource;
		}
	}
}