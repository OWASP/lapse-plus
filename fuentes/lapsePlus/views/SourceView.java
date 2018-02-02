package lapsePlus.views;

/*
* SourceView.java,version 2.8, 2010
*/
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.Collator;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;

import lapsePlus.CallerFinder;
import lapsePlus.LapsePlugin;
import lapsePlus.Utils;
import lapsePlus.XMLConfig;
import lapsePlus.MethodSearchRequestor.MethodDeclarationsSearchRequestor;
import lapsePlus.Utils.MethodDeclarationUnitPair;
import lapsePlus.XMLConfig.SourceDescription;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.ITextEditor;

public class SourceView extends ViewPart {
	
	
	class ViewMatch {
		
		private String message;
		private ASTNode ast;
		private CompilationUnit unit;
		private IResource resource;
		private String type;
		private IMember member;
		private boolean error;
		private boolean nonWeb;
		private String category;

		ViewMatch(String message, ASTNode ast, CompilationUnit unit, IResource resource, String type, String category, IMember member, boolean error, boolean nonWeb){
			this.message = message;
			this.ast = ast;
			this.unit = unit;
			this.resource = resource;
			this.type = type;
			this.category = category;
			this.member = member;
			this.error = error;
			this.nonWeb = nonWeb;
		}
		
		public String toString() {
			return message;
		}
		
		public String getMessage() {
			return message;
		}		
		public String getType() {
			return type;
		}
		public CompilationUnit getUnit() {
			return unit;
		}
		public IResource getResource() {
			return resource;
		}
		public ASTNode getAST() {
			return ast;
		}
		public String getCategory() {
			return this.category;
		}
		public IMember getMember() {
			return member;
		}
		public boolean isSource() {
			return unit != null;	
		}
		public int getLineNumber() {
			return isSource() ?
					unit.getLineNumber(ast.getStartPosition()) :
					-1; 
		}
		public String getFullFileName() {
			return resource.getFullPath().toString();
		}
		public String getFileName() {
			return resource.getName();
		}
		public IProject getProject() {
			return resource.getProject();
		}
		public boolean isError() {
			return error;
		}

		public String toLongString() {
			return 
				cutto(this.getAST().toString(), 65) + "\t" +
				cutto(this.getType(), 20) + "\t" +
				cutto(this.getProject().getProject().getName(), 12) + "\t" +
				this.getResource().getFullPath().toString() + ":" +
				this.getLineNumber();
		}

		public boolean isNonWeb() {
			return this.nonWeb;
		}
	}
	
	static TableViewer viewer;
	ViewContentProvider contentProvider;
	
	Action runAction;
	Action doubleClickAction;
	Action hideNonWebAction;
	Action hideNoSourceAction;
	
	IAction copyToClipboardAction;
	Clipboard fClipboard;
	
	// Constants
	static final String ANT_TASK_TYPE = "org.apache.tools.ant.Task";
	static final String ANT_TASK_METHODS = "set*";	
	
	class ViewContentProvider implements IStructuredContentProvider {
		
		Vector<ViewMatch> matches = new Vector<ViewMatch>();
		
		public void addMatch(ViewMatch match) {
			matches.add(match);
		}
		
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
		
		public void dispose() {
		}
		
		public Object[] getElements(Object parent) {
			return matches.toArray();
		}

		public int getMatchCount() {
			return matches.size();
		}

		public void clearMatches() {
			matches.clear();
		}
	}
	
	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider, IColorProvider {
		
		public String getColumnText(Object obj, int index) {
			
			ViewMatch match = (ViewMatch) obj;
			
			if(index == 0) {
				return match.getMessage();
			} else
			if (index == 1) {
				return match.getMember() != null ? match.getMember().getElementName() : null;
			} else
			if (index == 2) {
				return match.getType();
			} else
			if (index == 3) {
					return match.getCategory();
				} else
			if (index == 4) {
				return match.getProject() != null ? match.getProject().getName() : null;
			} else
			if (index == 5) {
				return match.getFileName();
			} else
			if (index == 6) {
				return match.isSource() ? "" + match.getLineNumber() : "";
			} else {
				return null;
			}
		}
		
		public Image getColumnImage(Object obj, int index) {
			ViewMatch match = (ViewMatch) obj;
			
			if(index == 0) {
				return match.isError() ?
						JavaPluginImages.get(JavaPluginImages.IMG_OBJS_ERROR) :
						JavaPluginImages.get(JavaPluginImages.IMG_FIELD_PUBLIC);
			}
			return null;
		}
		
		public Color getForeground(Object element) {
			Display display = Display.getCurrent();
			ViewMatch match = (ViewMatch) element;
			
			return match.isSource() ?
					display.getSystemColor(SWT.COLOR_LIST_FOREGROUND) :
					display.getSystemColor(SWT.COLOR_GRAY);
		}
		
		public Color getBackground(Object element) {
			return null;
		}
		
	}

	static class ColumnBasedSorter extends ViewerSorter {
		private int columnNum;
		private int orientation = 1;
		
		ColumnBasedSorter(int columnNum, int orientation){
			super ( Collator.getInstance(Locale.getDefault()) );
			
			this.columnNum = columnNum;
			this.orientation = orientation;
		}
		
		ColumnBasedSorter(int columnNum){
			this(columnNum, 1);
		}
		
		public int category(Object element) {
			ViewMatch match  = (ViewMatch) element;
			if(match.isError()) {
				return 1; 
			} else {
				return 0;
			}
		}
		
		public int compare(Viewer viewer, Object e1, Object e2) {
			ViewMatch match1 = (ViewMatch)e1;
			ViewMatch match2 = (ViewMatch)e2;
			int result = Integer.MAX_VALUE; 
			String s1, s2;
		
			
			//{ "Suspicious call", "Method", "Type", "Category", "Project", "File", "Line" };
			if(columnNum == 0) {
				s1 = match1.getMessage();
				s2 = match2.getMessage();															
			} else
			if(columnNum == 1) {
				s1 = match1.getMember() != null ? match1.getMember().getElementName() : "";
				s2 = match2.getMember() != null ? match2.getMember().getElementName() : "";
			} else
			if(columnNum == 2) {
				s1 = match1.getType();
				s2 = match2.getType();
			} else
			if(columnNum == 3) {
				s1 = match1.getCategory();
				s2 = match2.getCategory();
			} else 
			if(columnNum == 4) {
				s1 = match1.getProject() != null ? match1.getProject().getName() : "";
				s2 = match2.getProject() != null ? match2.getProject().getName() : "";
			} else
			if(columnNum == 5) {
				s1 = "" + match1.getFileName();
				s2 = "" + match2.getFileName();
			} else
			if(columnNum == 6) {
				s1 = "" + match1.getLineNumber();
				s2 = "" + match2.getLineNumber();
			} else {
				logError("Unknown column: " + columnNum);
				return 0;
			}
			
			result = orientation*s1.compareToIgnoreCase(s2);
			
			return result;
		}
		
		public void toggle() {
			orientation = orientation * -1;
		}
		
		public int getColumn() {
			return this.columnNum;
		}
		
		public int getOrientation() {
			return orientation;
		}
	}

	public SourceView() {}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		
		fClipboard= new Clipboard(parent.getDisplay());
		
		viewer = new LocationViewer(parent);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new ColumnBasedSorter(2));
		viewer.setInput(getViewSite());
		
		makeActions();
		
		hookContextMenu();
		hookDoubleClickAction();
		
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				if(sel != null) {
					int size = sel.toArray().length;
					if(size > 0) {
						IStatusLineManager slManager = getViewSite().getActionBars().getStatusLineManager();
						slManager.setMessage("Selected " + size + (size > 1 ? " entries." : "entry."));
					}
				}
			}
		});
		
		contributeToActionBars();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				SourceView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		
		IActionBars bars = getViewSite().getActionBars();
		fillLocalToolBar(bars.getToolBarManager());
		
		LapseMultiActionGroup group = 
			new LapseCheckboxActionGroup(
					new IAction[] {hideNonWebAction, hideNoSourceAction},
					new boolean[] {true		   , 	 true});
		group.addActions(bars.getMenuManager());
	}

	//Add actions to the plugin
	private void fillContextMenu(IMenuManager manager) {
		manager.add(runAction);
		manager.add(copyToClipboardAction);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(runAction);
		manager.add(copyToClipboardAction);
	}
	
	private void computeSources() {
		
		(new Job("Computing Sources") {
		protected IStatus run(IProgressMonitor monitor) {
			
			
			//Clear matches
			contentProvider.clearMatches();
			
			//((ViewContentProvider) viewer.getContentProvider()).clearMatches();
			
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					viewer.refresh();
				}
			});
			
			IJavaModel model = JavaModelManager.getJavaModelManager().getJavaModel();
			IJavaProject[] projects;
			
			try {
				projects = model.getJavaProjects();
			} catch (JavaModelException e) {
				log(e.getMessage(), e);
				return Status.CANCEL_STATUS;
			}
			
			for (int i = 0; i < projects.length; i++) {
				final JavaProject project = (JavaProject) projects[i];
				
				log(
						"------------------ Project " + 
						cutto(project.getProject().getName(), 20) + 
						"------------------ ");
				
//				if(!project.isOpen()) {
//					System.out.println("Skipping " + project);
//					continue;
//				}
				
				Collection/*<SourceDescription>*/ sources = XMLConfig.readSources("sources.xml");
				
				if(sources == null || sources.size() == 0) {
					logError("No interesting methods in " + project.getResource().getName());
					continue;
				}
				
				int matches = 0, old_matches = 0;
				
				log("\tProcessing " + sources.size() + " methods");
				
				for (Iterator descIter = sources.iterator(); descIter.hasNext(); ){
					XMLConfig.SourceDescription desc = (SourceDescription) descIter.next();				
					
					log(
							"Project " + project.getProject().getName() + 
							": processing method " + desc.getID() + "...\t");
					monitor.subTask("Project " + project.getProject().getName() + 
							": processing method " + desc.getID() + "...\t");
					
					int index=desc.getMethodName().lastIndexOf('.');
                    char aux=(desc.getMethodName().charAt(index+1));
                    boolean isConstructor = aux<='Z';
                    
                    if(isConstructor)
                    	System.out.println(desc.getMethodName()+" is constrcutor");
                    
                   // boolean isContructor = desc.getTypeName().endsWith(desc.getMethodName());
                    
					Collection callers/*<MethodUnitPair>*/ = CallerFinder.findCallers(monitor, desc.getID(), project, isConstructor);
					
					for (Iterator iter = callers.iterator(); iter.hasNext();) {
						Utils.ExprUnitResourceMember element = (Utils.ExprUnitResourceMember) iter.next();
						Expression expr = element.getExpression();
						if(expr == null) {
							log("Unexpected NULL in one of the callers");
							continue;
						}
						// do a case on the expression:
						
						if(!(expr instanceof MethodInvocation)) {
							logError("Can't match " + expr + " of type " + expr.getClass());
							continue;
						}
						MethodInvocation mi = (MethodInvocation) expr;
	
						ViewMatch match = new ViewMatch(
								expr.toString(), 
								expr, 
								element.getCompilationUnit(), 
								element.getResource(),																
								desc.getID(),
								desc.getCategoryName(),
								element.getMember(),
								false,
								false);
						contentProvider.addMatch(match);
						matches++;
					}
					
					if(matches > old_matches){
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								viewer.refresh();
							}
						});
					}
					old_matches = matches;					
				}
				
//				System.out.println("Found " + matches + " matche(s).");
				// find all main methods
				log("Looking for 'main' arguments");
				monitor.subTask("Looking for 'main' arguments");
				matches += addMethodsByName("main", "'main' declaration", "main argument", project, monitor, true);
				
				// find Ant entry points
				log("Looking for Ant task entry points");
				monitor.subTask("Looking for Ant task entry points");
				matches += addMethodsByName(ANT_TASK_TYPE + "." + ANT_TASK_METHODS, "Ant task entry point", "ANT", project, monitor, true);
				
				logError(project.getProject().getName() + "\t:\t" + matches + " matche(s)");
			}
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					viewer.refresh();
				}
			});
			logError("There are " + contentProvider.getMatchCount() + " matches");
			
			return Status.OK_STATUS;
		}}).schedule();
	}
	
	int addMethodsByName(String methodName, String type, String category, JavaProject project, IProgressMonitor monitor, boolean nonWeb) {
		int matches = 0;
		ViewContentProvider cp = ((ViewContentProvider)viewer.getContentProvider());
		try {
			MethodDeclarationsSearchRequestor requestor = new MethodDeclarationsSearchRequestor();
            SearchEngine searchEngine = new SearchEngine();

            IJavaSearchScope searchScope = CallerFinder.getSearchScope(project);
            SearchPattern pattern = SearchPattern.createPattern(
            		methodName, 
					IJavaSearchConstants.METHOD,
					IJavaSearchConstants.DECLARATIONS, 
					SearchPattern.R_PATTERN_MATCH | SearchPattern.R_CASE_SENSITIVE
					);
     
			searchEngine.search(
					pattern, 
					new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() },
			        searchScope, 
			        requestor, 
					monitor
					);
			Collection pairs =  requestor.getMethodUnitPairs();
			for(Iterator iter = pairs.iterator(); iter.hasNext();) {
				Utils.MethodDeclarationUnitPair pair = (MethodDeclarationUnitPair) iter.next();
				ViewMatch match = new ViewMatch(
						pair.getMember().getDeclaringType().getElementName() + "." + pair.getMember().getElementName(),  
						pair.getMethod() != null ? pair.getMethod().getName() : null, 
						pair.getCompilationUnit(), 
						pair.getResource(), 
						type,
						category,
						pair.getMember(),
						false,
						nonWeb);
				cp.addMatch(match);
				monitor.subTask("Found " + matches + " matches");
				matches++;
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			log(e.getMessage(), e);
		}
		
		return matches;
	}

	/**
	 * Tests whether a given expression is a String contant.
	 * */
	boolean isStringContant(Expression arg) {
		if(arg instanceof StringLiteral) {
			return true;
		} else
		if(arg instanceof InfixExpression) {
			InfixExpression infixExpr = (InfixExpression) arg;
			if(!isStringContant(infixExpr.getLeftOperand())) return false; 
			if(!isStringContant(infixExpr.getRightOperand())) return false;
						
			for(Iterator iter2 = infixExpr.extendedOperands().iterator(); iter2.hasNext(); ) {
				if(!isStringContant((Expression) iter2.next())) {
					return false;
				}
			}
			return true;
		}
		
		return false;	
	}

    /*
	private Type findLocallyDeclaredType(String argName, CompilationUnit compilationUnit) {
		final Map<String, Type> var2type = new HashMap<String, Type>();
		
		ASTVisitor visitor = new ASTVisitor() {
			public boolean visit(VariableDeclarationStatement node) {
				Type type = node.getType();
				//node.getType()
				for(Iterator iter = node.fragments().iterator(); iter.hasNext();) {
					VariableDeclarationFragment frag = (VariableDeclarationFragment)iter.next(); 
					SimpleName var = frag.getName();
					
					//System.out.println("Storing " + var.getFullyQualifiedName() +  " of type " + type);
					var2type.put(var.getFullyQualifiedName(), type);
				}
				
				return false;
			}
		};
		compilationUnit.accept(visitor);
		//System.out.println("There are " + var2type.size() + " elements in the map");
		
		return (Type) var2type.get(argName);
	}*/
	
	private void makeActions() {
		
		contentProvider = ((ViewContentProvider)viewer.getContentProvider());
		
		runAction = new Action() {
			public void run() {
				computeSources();
			}			
		};
		
		copyToClipboardAction = new CopyMatchViewAction(this, fClipboard);
		copyToClipboardAction.setText("Copy selection to clipboard");
		copyToClipboardAction.setToolTipText("Copy selection to clipboard");
		copyToClipboardAction.setImageDescriptor(JavaPluginImages.DESC_DLCL_COPY_QUALIFIED_NAME);
				
		doubleClickAction = new Action() {
			public void run() {
				IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
				ViewMatch match = (ViewMatch) sel.getFirstElement();
				
				try {
//					System.out.println("Double-clicked on " + match.getMember().getClass());
					EditorUtility.openInEditor(match.getMember(), true);
					if(match.getUnit() != null) {
						ITextEditor editor = (ITextEditor) EditorUtility.openInEditor(match.getMember());
						editor.selectAndReveal(
								match.getAST().getStartPosition(), 
								match.getAST().getLength());
					}
				} catch (PartInitException e) {
					log(e.getMessage(), e);
				} catch (Exception e) {
                    log(e.getMessage(), e);
				}
			}				
		};
		
		runAction.setText("Find sources");
		runAction.setToolTipText("Find sources");
		//Find Sources image
		runAction.setImageDescriptor(JavaPluginImages.DESC_OBJS_JSEARCH);
		
		hideNonWebAction = new Action("Hide non-Web sources", IAction.AS_CHECK_BOX) {
			
			boolean hasFilter = false;
			
			ViewerFilter filter = new ViewerFilter() {
				public boolean select(Viewer viewer, Object parentElement, Object element) {
					ViewMatch match = (ViewMatch) element;
					
					return !match.isNonWeb();
				}	
			};
			
			public void run() {
				if(!hasFilter) {
					viewer.addFilter(filter);
					hasFilter = true;
				} else {
					viewer.removeFilter(filter);
					hasFilter = false;
				}
			}
		};
		
		hideNonWebAction.setImageDescriptor(JavaPluginImages.DESC_ELCL_FILTER);
		
		hideNoSourceAction = new Action("Hide vulnerability sources with no source code", IAction.AS_CHECK_BOX) {
			boolean hasFilter = false;
			ViewerFilter filter = new ViewerFilter() {
				public boolean select(Viewer viewer, Object parentElement, Object element) {
					ViewMatch match = (ViewMatch) element;
					
					return match.isSource();
				}									
			};
			public void run() {
				if(!hasFilter) {
					viewer.addFilter(filter);
					hasFilter = true;
				} else {
					viewer.removeFilter(filter);
					hasFilter = false;
				}
			}
		};
		
		hideNoSourceAction.setImageDescriptor(JavaPluginImages.DESC_ELCL_FILTER);
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}
	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}		

	static class LocationViewer extends TableViewer {
	    private final String columnHeaders[] = { "Suspicious call", "Method", "Type", "Category", "Project", "File", "Line" }; 
	    private ColumnLayoutData columnLayouts[] = {
	        new ColumnPixelData(300),
	        new ColumnWeightData(100),
	        new ColumnWeightData(100),
	        new ColumnWeightData(100),
	        new ColumnWeightData(100),
	        new ColumnWeightData(100),
	        new ColumnWeightData(20)};	    

	    LocationViewer(Composite parent) {
	        super(createTable(parent));

	        createColumns();
	        
//	        JavaUIHelp.setHelp(this, IJavaHelpContextIds.CALL_HIERARCHY_VIEW);
	    }

	    /**
	     * Creates the table control.
	     */
	    private static Table createTable(Composite parent) {
	        Table table = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
	        table.setLinesVisible(true);
	        return table;
	    }
	    
	    private void createColumns() {
	        TableLayout layout = new TableLayout();
	        getTable().setLayout(layout);
	        getTable().setHeaderVisible(true);
	        for (int i = 0; i < columnHeaders.length; i++) {
	            layout.addColumnData(columnLayouts[i]);
	            TableColumn tc = new TableColumn(getTable(), SWT.NONE,i);
	            tc.setResizable(columnLayouts[i].resizable);
	            tc.setText(columnHeaders[i]);
	            final int j = i;
	            tc.addSelectionListener(new SelectionAdapter() {           	
                	public void widgetSelected(SelectionEvent e) {
                		ViewerSorter oldSorter = viewer.getSorter();
                		if(oldSorter instanceof ColumnBasedSorter) {
                			ColumnBasedSorter sorter = (ColumnBasedSorter) oldSorter;	                			 
                			if(sorter.getColumn() == j) {
                				sorter.toggle();
                				viewer.refresh();
//                				System.err.println("Resorting column " + j + " in order " + sorter.getOrientation());
                				return;
                			}
                		}
                		
                		viewer.setSorter(new ColumnBasedSorter(j));
//                		System.err.println("Sorting column " + j + " in order " + 1);	                		
                		viewer.refresh();
                    }	                
                });
	        }
	    }

	    /**
	     * Attaches a contextmenu listener to the tree
	     */	   
	    void initContextMenu(IMenuListener menuListener, String popupId, IWorkbenchPartSite viewSite) {
	        MenuManager menuMgr= new MenuManager();
	        menuMgr.setRemoveAllWhenShown(true);
	        menuMgr.addMenuListener(menuListener);
	        Menu menu= menuMgr.createContextMenu(getControl());
	        getControl().setMenu(menu);
	        viewSite.registerContextMenu(popupId, menuMgr, this);
	    }
	
	    void clearViewer() {
	        setInput(""); //$NON-NLS-1$
	    }
	}
	
	private static String cutto(String str, int to) {
		if(str.length() < to-3) {
			return str + repeat(" ", to - str.length());
		} else {
			return str.substring(0, to-3) + "...";
		}
	}
	
	private static String repeat(String str, int times) {
		StringBuffer buf = new StringBuffer(); 
		for (int i = 0; i < times; i++) {
			buf.append(str);
		}
		
		return buf.toString();
	}
	
	class CopyMatchViewAction extends Action {
	    //private static final char INDENTATION= '\t';  //$NON-NLS-1$
	    
	    private SourceView fView;
		private final Clipboard fClipboard;

		public CopyMatchViewAction(SourceView view, Clipboard clipboard) {
			super("Copy matches to clipboard"); 
			Assert.isNotNull(clipboard);
			fView= view;
			fClipboard= clipboard;
		}

		public void run() {
	        StringBuffer buf = new StringBuffer();
	        addCalls(viewer.getTable().getSelection(), buf);

			TextTransfer plainTextTransfer = TextTransfer.getInstance();
			try{
				fClipboard.setContents(
					new String[]{ convertLineTerminators(buf.toString()) }, 
					new Transfer[]{ plainTextTransfer });
			}  catch (SWTError e){
				if (e.code != DND.ERROR_CANNOT_SET_CLIPBOARD) 
					throw e;
				if (MessageDialog.openQuestion(fView.getViewSite().getShell(), 
						("CopyCallHierarchyAction.problem"), ("CopyCallHierarchyAction.clipboard_busy"))
				) 
				{
					run();
				}
			}
		}
		
		private void addCalls(TableItem[] items, StringBuffer buf) {
			for (int i = 0; i < items.length; i++) {
				TableItem item = items[i];
				SourceView.ViewMatch match = (SourceView.ViewMatch) item.getData();
				
				buf.append(match.toLongString());
		        buf.append('\n');			
			}                
	    }

	    private String convertLineTerminators(String in) {
			StringWriter stringWriter = new StringWriter();
			PrintWriter printWriter = new PrintWriter(stringWriter);
			StringReader stringReader = new StringReader(in);
			BufferedReader bufferedReader = new BufferedReader(stringReader);		
			String line;
			try {
				while ((line= bufferedReader.readLine()) != null) {
					printWriter.println(line);
				}
			} catch (IOException e) {
				return in; // return the call hierarchy unfiltered
			}
			return stringWriter.toString();
		}
	}

	public static boolean isSourceName(String identifier) {
		Collection sources = XMLConfig.readSources("sources.xml");
		for(Iterator iter = sources.iterator(); iter.hasNext(); ){
			XMLConfig.SourceDescription sourceDesc = (XMLConfig.SourceDescription) iter.next();
			int i=sourceDesc.getMethodName().lastIndexOf('.');
			String sub=sourceDesc.getMethodName().substring(i+1);
			if(sub.equals(identifier)){
				return true;
			}
		}
	
		// none matched
		return false;
	}

    public static boolean isSafeName(String identifier) {
		Collection safes = XMLConfig.readSafes("safes.xml");
		for(Iterator iter = safes.iterator(); iter.hasNext(); ){
			XMLConfig.SafeDescription safeDesc = (XMLConfig.SafeDescription) iter.next();
			if(safeDesc.getMethodName().equals(identifier)){
				return true;
			}
		}
	
		// none matched
		return false;
    }

    private static void log(String message, Throwable e) {
        LapsePlugin.trace(LapsePlugin.SOURCE_DEBUG, "Source view: " + message, e);
    }
    
    private static void log(String message) {
        log(message, null);
    }
    
    private static void logError(String message) {
        log(message, new Throwable());
    }
    /**
     * Tests whether a given expression is a String contant.
     * 
     * @param arg -- argument that we want to test
     * 
     * This method does pattern-matching to find constant strings. If none of 
     * the patterns match, false is returned. 
     */
    public static boolean isStringContant(Expression arg, CompilationUnit unit, IResource resource) {
        if (arg instanceof StringLiteral) {
            return true;
        } else if (arg instanceof InfixExpression) {
            InfixExpression infixExpr = (InfixExpression) arg;
            if (!isStringContant(infixExpr.getLeftOperand(), unit, resource)) return false;
            if (!isStringContant(infixExpr.getRightOperand(), unit, resource)) return false;
            for (Iterator iter2 = infixExpr.extendedOperands().iterator(); iter2.hasNext();) {
                if (!isStringContant((Expression) iter2.next(), unit, resource)) {
                    return false;
                }
            }
            return true;
        } else if (arg instanceof SimpleName) {
            SimpleName name = (SimpleName) arg;
            // System.err.println("TODO -> Name: " + name);
            VariableDeclaration varDecl = LapseView.name2decl(name, unit, resource);
            if (varDecl instanceof SingleVariableDeclaration) {
                SingleVariableDeclaration decl = (SingleVariableDeclaration) varDecl;
                if (decl.getInitializer() != null && decl.getInitializer() instanceof StringLiteral) {
                    StringLiteral l = (StringLiteral) decl.getInitializer();
                    return true;
                }
            } else {
                VariableDeclarationFragment decl = (VariableDeclarationFragment) varDecl;
                if (decl.getInitializer() != null) {
                    return isStringContant(decl.getInitializer(), unit, resource);
                }
            }
        } else if (arg instanceof MethodInvocation) {
            MethodInvocation inv = (MethodInvocation) arg;
            if (inv.getName().getIdentifier().equals("toString")) {
                // TODO: StringBuffer.toString() return result
                Expression target = inv.getExpression();
                // System.err.println("TODO -> methodInv: " + inv);
            }
        }
        // TODO: add final/const
        return false;       // this is a conservative return value
    }
}


