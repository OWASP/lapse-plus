package lapsePlus;

/*
 * MethodSearchRequestor.java, version 2.8, 2010
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.corext.refactoring.structure.ASTNodeSearchUtil;

public class MethodSearchRequestor{
	
	//static Map<ICompilationUnit, CompilationUnit> parserMap = new HashMap<ICompilationUnit, CompilationUnit>();
	
	static Map<ICompilationUnit, CompilationUnit> parserMap;

	// Settings
	final static boolean SKIP_NOT_SOURCE = false;
	final static boolean SKIP_BINARY = false;

	static CompilationUnit retrieveCompilationUnit(ICompilationUnit unit) {
		CompilationUnit cu = (CompilationUnit) parserMap.get(unit);
		if (cu == null) {
			ASTParser parser = ASTParser.newParser(AST.JLS3);
			parser.setSource(unit);
			cu = (CompilationUnit) parser.createAST(null);

			parserMap.put(unit, cu);
		}

		return cu;
	}
	
	static void initializeParserMap(){
		
		parserMap = new HashMap<ICompilationUnit, CompilationUnit>();
		
	}

	public static class MethodReferencesSearchRequestor extends SearchRequestor {
		
		public class MethodCallResultCollector {
			ArrayList<Utils.ExprUnitResourceMember> _methodUnitPairs;

			MethodCallResultCollector() {
				_methodUnitPairs = new ArrayList<Utils.ExprUnitResourceMember>();
			}

			public void addCaller(Expression expr, CompilationUnit cu,
					IResource resource, IMember member) {
				_methodUnitPairs.add(new Utils.ExprUnitResourceMember(expr, cu,
						resource, member));
			}

			public Collection/* <MethodUnitPair> */getMethodUnitPairs() {
				return _methodUnitPairs;
			}
		}

		private MethodCallResultCollector fSearchResults;
		private boolean fRequireExactMatch = true;

		MethodReferencesSearchRequestor() {
			fSearchResults = new MethodCallResultCollector();
		}

		public void acceptSearchMatch(SearchMatch match) {
			log("Got match: " + match);
			if (fRequireExactMatch
					&& (match.getAccuracy() != SearchMatch.A_ACCURATE)) {
				logError("Skipping unaccurate match " + match);
				return;
			}

			if (match.isInsideDocComment()) {
				log("Is inside doc " + match);
				return;
			}

			if (match.getElement() != null
					&& match.getElement() instanceof IMember) {
				IMember member = (IMember) match.getElement();
				if (SKIP_BINARY && member.isBinary()) {
					log("Skipping binary member " + member);
					return;
				}
				// match.get
				if (member.getCompilationUnit() == null) {
					logError("No compilation unit for " + member);
					if (!SKIP_NOT_SOURCE) {
						fSearchResults.addCaller(null, null,
								match.getResource(), member);
					}
					return;
				}
				CompilationUnit cuNode = retrieveCompilationUnit(member.getCompilationUnit());
				ASTNode node = ASTNodeSearchUtil.getAstNode(match, cuNode);
				Expression expr = null;

				if (node != null) {

					if (node instanceof MethodInvocation) {
						expr = (MethodInvocation) node;
					} else if (node.getParent() instanceof MethodInvocation) {
						expr = (MethodInvocation) node.getParent();
					} else if (node instanceof ClassInstanceCreation) {
						expr = (ClassInstanceCreation) node;
					} else if (node.getParent() instanceof ClassInstanceCreation) {
						expr = (ClassInstanceCreation) node.getParent();
					} else {
						System.err.println("Unknown match type: " + node
								+ " of type " + node.getClass());
						try {
							System.err
									.println("MethodReferencesSearchRequestor: Skipping node that appears in the search: "
											+ node
											+ " of type "
											+ node.getClass()
											+ " at line "
											+ member.getCorrespondingResource()
											+ ":"
											+ cuNode.getLineNumber(node
													.getStartPosition()));
						} catch (JavaModelException e) {
							log(e.getMessage(), e);
							return;
						}
						return;
					}
				}

				IResource resource = match.getResource();
				if (resource == null) {
					System.err.println("No resource for " + match);
					return;
				}

				if (expr == null)
					System.err.println("expr is null");

				fSearchResults.addCaller(expr, cuNode, resource, member);
				// System.err.println("Matched " + mi + " of type " +
				// mi.getClass());
			} else {
				System.err.println("Skipping match: " + match);
			}
		}

		public Collection/* <MethodUnitPair> */getMethodUnitPairs() {
			return fSearchResults.getMethodUnitPairs();
		}
	}

	public static class MethodDeclarationsSearchRequestor extends
			SearchRequestor {
		public class MethodDeclarationlResultCollector {
			ArrayList<Utils.MethodDeclarationUnitPair> _methodDeclarationUnitPairs;

			MethodDeclarationlResultCollector() {
				_methodDeclarationUnitPairs = new ArrayList<Utils.MethodDeclarationUnitPair>();
			}

			public void addCaller(MethodDeclaration mi, CompilationUnit cu,
					IResource resource, IMember member) {
				_methodDeclarationUnitPairs
						.add(new Utils.MethodDeclarationUnitPair(mi, cu,
								resource, member));
			}

			public Collection/* <MethodDeclarationUnitPair> */getMethodDeclarationUnitPairs() {
				return _methodDeclarationUnitPairs;
			}
		}

		private MethodDeclarationlResultCollector fSearchResults;
		private boolean fRequireExactMatch = true;

		public MethodDeclarationsSearchRequestor() {
			fSearchResults = new MethodDeclarationlResultCollector();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jdt.core.search.SearchRequestor#acceptSearchMatch(org
		 * .eclipse.jdt.core.search.SearchMatch)
		 */
		public void acceptSearchMatch(SearchMatch match) {
			if (fRequireExactMatch
					&& (match.getAccuracy() != SearchMatch.A_ACCURATE)) {
				return;
			}

			if (match.isInsideDocComment()) {
				return;
			}

			if (match.getElement() != null
					&& match.getElement() instanceof IMember) {
				IMember member = (IMember) match.getElement();
				if (SKIP_BINARY && member.isBinary()) {
					log("Skipping binary member " + member);
					return;
				}

				if (member.getCompilationUnit() == null) {
					logError("No compilation unit for " + member);
					if (!SKIP_NOT_SOURCE) {
						fSearchResults.addCaller(null, null,
								match.getResource(), member);
					}
					return;
				}

				CompilationUnit cu = retrieveCompilationUnit(member
						.getCompilationUnit());
				ASTNode node = ASTNodeSearchUtil.getAstNode(match, cu);

				MethodDeclaration md = null;

				if (node != null) {
					if (node instanceof MethodDeclaration) {
						md = (MethodDeclaration) node;
					} else if (node.getParent() instanceof MethodDeclaration) {
						md = (MethodDeclaration) node.getParent();
					} else {
						try {
							System.err
									.println("MethodDeclarationsSearchRequestor: Skipping node that appears in the search: "
											+ node
											+ " of type "
											+ node.getClass()
											+ " at line "
											+ member.getCorrespondingResource()
											+ ":"
											+ cu.getLineNumber(node
													.getStartPosition()));
						} catch (JavaModelException e) {
							log(e.getMessage(), e);
							return;
						}
						return;
					}
				}

				IResource resource = match.getResource();
				if (resource == null) {
					System.err.println("No resource for " + match);
					return;
				}
				fSearchResults.addCaller(md, cu, resource, member);
				log("Matched " + md + " in " + resource);
			}
		}

		public Collection/* <MethodDeclarationUnitPair> */getMethodUnitPairs() {
			return fSearchResults.getMethodDeclarationUnitPairs();
		}
	}

	private static void log(String message, Throwable e) {
		LapsePlugin.trace(LapsePlugin.SEARCH, "Method search: " + message, e);
	}

	private static void log(String message) {
		log(message, null);
	}

	private static void logError(String message) {
		log(message, new Throwable());
	}
}