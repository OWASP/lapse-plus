package lapsePlus;

/*
* CallerFinder.java, version 2.8, 2010
*/

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.search.JavaSearchScope;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.PlatformUI;

public class CallerFinder {
	    
    private static IJavaSearchScope fSearchScope;

    public static Collection/*<Util.ExprUn...>*/ findCallers(final String methodName, final JavaProject project, final boolean isConstructor) {
    	class FindingOp implements IRunnableWithProgress { 
    		Collection c = null;
    		public void run(IProgressMonitor monitor) {
    			c = findCallers(monitor, methodName, project, isConstructor);
    			if(c == null) {
    				JavaPlugin.logErrorMessage("Collection is null");
    			}
//    			notify();
    		}
			public Collection/*<MethodDeclarationUnitPair>*/ getCollection() {return c;}
    	}			
					
    	try {
    		FindingOp operation = new FindingOp();
			PlatformUI.getWorkbench().getProgressService().run(false, true, operation);
//			operation.wait();
			Collection c = operation.getCollection();
			if(c == null) {
				JavaPlugin.logErrorMessage("No collection is computed");
			}
			return c;
		} catch (InvocationTargetException e) {
			JavaPlugin.log(e);
		} catch (InterruptedException e) {
			// canceled
		}
		return null;
    }
    
    public static Collection/*<Util.ExprUn...>*/ findCallers(final IMethod method, final IJavaProject project) {
		// TODO Auto-generated method stub
		return null;
	}
    
    public static Collection/*<MethodDeclarationUnitPair>*/ findMethods(final SimpleName methodName, final JavaProject project, final boolean isConstructor) {
    	class FindingOp implements IRunnableWithProgress { 
    		Collection c = null;
    		public void run(IProgressMonitor monitor) {
    			c = findCallees(monitor, methodName.toString(), project, isConstructor);
    			if(c == null) {
    				JavaPlugin.logErrorMessage("Collection is null");
    			}
//    			notify();
    		}
			public Collection/*<MethodDeclarationUnitPair>*/ getCollection() {return c;}
    	}
    	try {
    		FindingOp operation = new FindingOp();
			PlatformUI.getWorkbench().getProgressService().run(false, true, operation);
//			operation.wait();
			Collection c = operation.getCollection();
			if(c == null) {
				JavaPlugin.logErrorMessage("No collection is computed");
			}
			return c;
		} catch (InvocationTargetException e) {
			JavaPlugin.log(e);
		} catch (InterruptedException e) {
			// canceled
		}
		return null;
    }
    
	public static Collection/*<MethodDeclarationUnitPair>*/ 
			findCallees(IProgressMonitor progressMonitor, String methodName, IJavaProject project, boolean isConstructor) 
	{
		try {
            MethodSearchRequestor.MethodDeclarationsSearchRequestor searchRequestor = 
            	new MethodSearchRequestor.MethodDeclarationsSearchRequestor();
            SearchEngine searchEngine = new SearchEngine();

            IProgressMonitor monitor = new SubProgressMonitor(
            		progressMonitor, 5, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL);
            monitor.beginTask("Searching for declaration of " + methodName +
            		(project != null ? " in " + project.getProject().getName() : ""), 100);
            IJavaSearchScope searchScope = getSearchScope(project);
            int matchType = !isConstructor ? IJavaSearchConstants.METHOD : IJavaSearchConstants.CONSTRUCTOR;
            SearchPattern pattern = SearchPattern.createPattern(
            		methodName, 
					matchType,
					IJavaSearchConstants.DECLARATIONS, 
					SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE );
            
            searchEngine.search(
            		pattern, 
					new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() },
                    searchScope, 
					searchRequestor, 
					monitor
					);
            monitor.done();

            return searchRequestor.getMethodUnitPairs();
        } catch (CoreException e) {
            JavaPlugin.log(e);

            return new LinkedList();
        }
	}
	
	/**
	 * Returns a collection of return statements withing @param methodDeclaration.
	 * */
	public static Collection<ReturnStatement> findReturns(IProgressMonitor progressMonitor, MethodDeclaration methodDeclaration, JavaProject project) {
		progressMonitor.setTaskName("Looking for returns in " + methodDeclaration.getName());
		final Collection<ReturnStatement> returns = new ArrayList<ReturnStatement>();
		ASTVisitor finder = new ASTVisitor() {			
			public boolean visit(ReturnStatement node) {
				return returns.add(node);
			}
		};
		
		methodDeclaration.accept(finder);
		return returns;
	}	
    
	public static String callersToString(IMethod method) {
		Collection/*<MethodUnitPair>*/ callers = findCallers(method, null);
		String result = "[ ";
		for (Iterator iter = callers.iterator(); iter.hasNext();) {
			Object element = (Object) iter.next();
			result += element + " ";			
		}
		return result + "]";
	}
	
	public static Collection<Utils.ExpressionUnitPair> getActualsForFormal(IMethod method, Name name, Expression onlyCall, IProgressMonitor monitor, IJavaProject project) {
		Collection<Utils.ExpressionUnitPair> result = new ArrayList<Utils.ExpressionUnitPair>();
		Collection/*<MethodUnitPair>*/ c = findCallers(monitor, method, project);
		if (c.isEmpty()) {
			log("No callers for " + method);
		} else {
			monitor.beginTask("Getting actuals for " + name + " in " + method.getElementName(), c.size());
			int i = 0;
			for (Iterator iter = c.iterator(); iter.hasNext();) {
				Utils.ExprUnitResource mi = (Utils.ExprUnitResource) iter.next();
				int pos = SlicingUtils.getFormalArgumentPos(method, name);
				if (pos == SlicingUtils.NO_FORMAL_ARGUMENT) {
					log("No parameter " + name + " found in " + method);
					continue;
				}
				if (mi.getExpression() == null) {
					logError("unexpected error: mi.getExpression() == null when looking at " + name + " and " + method);
					continue;
				}
				String s1 = mi.getExpression().toString();
				//String s2 = onlyCall.toString();
				if ( (onlyCall != null) && (!s1.equals(onlyCall.toString())) ) {
					log("Skipping " + mi.getExpression() + "while looking for " + onlyCall + " instead");
					continue;
				}
				Expression expr = SlicingUtils.mapFormal2Actual(mi.getExpression(), pos);
				if(expr != null) {
					result.add(new Utils.ExpressionUnitPair(expr, mi.getCompilationUnit(), mi.getResource()));	
				}			
				monitor.worked(++i);
			}
			monitor.done();
		}
		
		return result;
	}
	
	public static Collection/*<ExpressionUnitPair>*/ getActualsForFormal(final IMethod method, final Name name, final Expression onlyCall, final JavaProject project) {
		class FindingOp implements IRunnableWithProgress { 
			Collection c = null;
			public void run(IProgressMonitor monitor) {
				c = getActualsForFormal(method, name, onlyCall, monitor, project);
			}
			public Collection getCollection() {return c;}
		}
		try {
    		FindingOp operation = new FindingOp();
			PlatformUI.getWorkbench().getProgressService().run(false, true, operation);
			
			return operation.getCollection();
		} catch (InvocationTargetException e) {
			JavaPlugin.log(e);
		} catch (InterruptedException e) {
			// canceled
		}
		return null;		
	}

	public static Collection/*<MethodUnitPair>*/ findCallers(IProgressMonitor progressMonitor, IMethod member, IJavaProject project) {
        boolean isConstructor = false;
        try {
            isConstructor = member.isConstructor(); // interrogate the member itself
        } catch (JavaModelException e) {
            JavaPlugin.log(e);
        }
        String methodName = isConstructor ?
            member.getDeclaringType().getElementName() :
            member.getDeclaringType().getElementName() + "." + member.getElementName();
		return findCallers(progressMonitor, methodName, project, isConstructor);
    }
	    
	public static Collection/*<MethodUnitPair>*/ findCallers(IProgressMonitor progressMonitor, String methodName, IJavaProject project, boolean isConstructor) {
        
		try {
			
			MethodSearchRequestor.initializeParserMap();
			
        	SearchRequestor searchRequestor =  (SearchRequestor)new MethodSearchRequestor.MethodReferencesSearchRequestor();
			
            SearchEngine searchEngine = new SearchEngine();

            IProgressMonitor monitor = new SubProgressMonitor(progressMonitor, 5, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
            
            monitor.beginTask("Searching for calls to " + methodName + (project != null ? " in " + project.getProject().getName() : ""), 100);
            
            IJavaSearchScope searchScope = getSearchScope(project);
            
            // This is kind of hacky: we need to make up a string name for the search to work right
            
            log("Looking for calls to " + methodName);
            
            int matchType = !isConstructor ? IJavaSearchConstants.METHOD : IJavaSearchConstants.CONSTRUCTOR;
            
            SearchPattern pattern = SearchPattern.createPattern(
            		methodName, 
					matchType,
					IJavaSearchConstants.REFERENCES,
					SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE);
            
            searchEngine.search(
            		pattern, 
					new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
                    searchScope, 
					searchRequestor, 
					monitor
			);

            if(searchRequestor instanceof MethodSearchRequestor.MethodDeclarationsSearchRequestor){                
                return ((MethodSearchRequestor.MethodDeclarationsSearchRequestor)searchRequestor).getMethodUnitPairs();
            }else{
                return ((MethodSearchRequestor.MethodReferencesSearchRequestor)searchRequestor).getMethodUnitPairs();
            }
            
        } catch (CoreException e) {
            JavaPlugin.log(e);

            return new LinkedList();
        }
    }
    
    public static Collection/*<MethodUnitPair>*/ findDeclarations(IProgressMonitor progressMonitor, String methodName, IJavaProject project, boolean isConstructor) {
        try {
            SearchRequestor searchRequestor = new MethodSearchRequestor.MethodDeclarationsSearchRequestor(); 
            SearchEngine searchEngine = new SearchEngine();

            IProgressMonitor monitor = new SubProgressMonitor(
                    progressMonitor, 5, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
            monitor.beginTask("Searching for calls to " + 
                    methodName + (project != null ? " in " + project.getProject().getName() : ""), 100);            
            IJavaSearchScope searchScope = getSearchScope(project);
            // This is kind of hacky: we need to make up a string name for the search to work right
            log("Looking for " + methodName);
            int matchType = !isConstructor ? IJavaSearchConstants.METHOD : IJavaSearchConstants.CONSTRUCTOR;
            SearchPattern pattern = SearchPattern.createPattern(
                    methodName, 
                    matchType,
                    IJavaSearchConstants.DECLARATIONS,
                    SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE );
            
            searchEngine.search(
                    pattern, 
                    new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() },
                    searchScope, 
                    searchRequestor, 
                    monitor
                    );

            if(searchRequestor instanceof MethodSearchRequestor.MethodDeclarationsSearchRequestor){                
                return ((MethodSearchRequestor.MethodDeclarationsSearchRequestor)searchRequestor).getMethodUnitPairs();
            }else{
                return ((MethodSearchRequestor.MethodReferencesSearchRequestor)searchRequestor).getMethodUnitPairs();
            }
        } catch (CoreException e) {
            JavaPlugin.log(e);

            return new LinkedList();
        }
    }
    
    public static IJavaSearchScope getSearchScope(IJavaProject project) {
    	if (fSearchScope == null) {
            fSearchScope = SearchEngine.createWorkspaceScope();
        	//fSearchScope = SearchEngine.createJavaSearchScope(new IResource[] {method.getResource()});
        }
//    	return fSearchScope;

    	if(project == null) {	        
	        return fSearchScope;
    	} else {
    		JavaSearchScope js = new JavaSearchScope();
    		try {
    			int includeMask = 
    				JavaSearchScope.SOURCES | 
					JavaSearchScope.APPLICATION_LIBRARIES | 
					JavaSearchScope.SYSTEM_LIBRARIES ;
				js.add((JavaProject) project, includeMask, new HashSet());
			} catch (JavaModelException e) {
				log(e.getMessage(), e);
				return fSearchScope;
			}
    		return js;
    	} 
    }    	
        
    public static class SlicingUtils {
		public static final int NO_FORMAL_ARGUMENT = -1;

		public static Expression mapFormal2Actual(Expression mi, int pos){
			//if(mi == null) return null;
			List args = null;
			if (mi instanceof ClassInstanceCreation) {
				args = ((ClassInstanceCreation)mi).arguments();
			} else if(mi instanceof MethodInvocation) {
				args = ((MethodInvocation)mi).arguments();
			} else {
				JavaPlugin.logErrorMessage("Unexpected type in mapFormal2Actual for " + mi);
				return null;
			}
	    	
	    	return (Expression) args.get(pos);
		}
		
		public static SimpleName getVariable(Expression expr){
			if (expr instanceof SimpleName) {
				return (SimpleName) expr;
			} else {
				return null;
			}			
		}
		
		/**
		 * @return NO_FORMAL_ARGUMENT signals that there's no matching position in the method declaration.
		 * */
		
		public static int getFormalArgumentPos(IMethod method, Name arg){
			String[] names;
			try {
				names = method.getParameterNames();
			} catch (JavaModelException e) {
				return NO_FORMAL_ARGUMENT; 
			}
			for (int i = 0; i < names.length; i++) {
				if(names[i].equals(arg.toString())) {
					return i;
				}
			}
			return NO_FORMAL_ARGUMENT;
		}
    }

    private static void log(String message, Throwable e) {
        LapsePlugin.trace(LapsePlugin.SEARCH, "Call finder: " + message, e);
    }
    
    private static void logError(String message) {
        log(message, new Throwable());
    }
    
    private static void log(String message) {
        log(message, null);
    }
}

