package lapsePlus.views;

/*
* LapseView.java,version 2.8, 2010
*/

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import lapsePlus.CallerFinder;
import lapsePlus.DefinitionLocation;
import lapsePlus.HistoryDefinitionLocation;
import lapsePlus.LapsePlugin;
import lapsePlus.NodeFinder;
import lapsePlus.Utils;
import lapsePlus.views.SinkView;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**JAVA MODEL**/
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
//import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;


import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.IProgressConstants;
import org.eclipse.ui.texteditor.IEditorStatusLine;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Main view class that calls most of the computational logic 
 * behind the slicer.
 * 
 * It extends from ViewPart: an abstract base implementation of all workbench views.
 * 
 * */
public class LapseView extends ViewPart{
	
	/********************************* Members *********************************/
	LocationViewer 		fViewer; //Tree Viewer
	ProvenanceContentProvider fContentProvider;
	IJavaSearchScope 	fSearchScope;	
	ITextEditor 		fEditor;
	IOpenable	 		fOpenable;
	CompilationUnit 	fRoot;
	IDocument 			fCurrentDocument;
	SuperListener 		fSuperListener;
	ASTParser 			fParser;	
	Clipboard 			fClipboard;
	LapseLayoutActionGroup fPullDownActions;
	SlicingJob 			fSlicingJob;
	SlicingFromSinkJob  fSlicingFromSinkJob;	
	
	/********************************* Actions *********************************/	
	private Action updateAction;
	private Action doubleClickAction;
	private Action prefAction;
	private Action historyAction;
	private Action selectAllAction;
	private LapseCopyAction copyAction;
	
	// Expansion/collapsing
	private Action collapseAction;
	private Action expandAction;
	
	private Action expandAllAction;
	private Action collapseAllAction;
	protected int fExpansionLevel;
	
	protected static final boolean TRACE = true;
	protected static final int MIN_EXPANSION_LEVEL = 0;

	
	public LapseView() {
		LapsePlugin.getDefault().setLapseView(this);	
	}

	/**
	 * Initializes this view
	 */
	public void init(IViewSite site) throws PartInitException {
		
		log("In init(...)");
		
		super.setSite(site);
		
		if (fSuperListener == null) {
			
			
			fSuperListener = new SuperListener(this);
			
			//showMessage("Registering the plugin");
			
			//To track if the plugin is selected
			ISelectionService service = site.getWorkbenchWindow().getSelectionService();
			
			//We add a listener to notify if the selection has changed
			service.addPostSelectionListener(fSuperListener);
			
			//A file that can be edited by more than one client
			FileBuffers.getTextFileBufferManager().addFileBufferListener(fSuperListener);
			
		}
		
		//We create the parser for the Abstract Syntax Tree
		fParser = ASTParser.newParser(AST.JLS3);//Java Language Specifitacion 3
		
		fParser.setResolveBindings(true);//The compiler have to provide binding information for the AST nodes
		
		//Backward slicer from sinks
		fSlicingJob = new SlicingJob("Backward data slicer");
		fSlicingFromSinkJob = new SlicingFromSinkJob("Backward slicer from a sink", this);
		
	}
    
	
	public void createPartControl(Composite parent) {
		
		log("In createPartControl(...)");
		//viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		fViewer = new LocationViewer(parent);
		
		fContentProvider = new ProvenanceContentProvider.HierarchicalViewContentProvider();
		fViewer.setContentProvider(fContentProvider);
		fViewer.setLabelProvider((IBaseLabelProvider) new DLTreeLabelProvider());
		//viewer.setSorter(new NameSorter());
		fViewer.setInput(getViewSite());
		fClipboard = new Clipboard(parent.getDisplay());

		makeActions();
		hookContextMenu();
		hookViewerEvents();
		
		fViewer.getControl().addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent event) {
				if ((event.stateMask & SWT.CTRL) != 0) {
		            switch (event.keyCode) {
		            	case '+' :
			            	if(expandAction.isEnabled()) {
			            		expandAction.run();
			            	}
		            	return;
		            	
		            	case '-' :
			            	if(collapseAction.isEnabled()) {
			            		collapseAction.run();
			            	}
			            return;
			            
		            	case 'L' :
			            	if((event.stateMask & SWT.SHIFT) != 0 && updateAction.isEnabled()) {
			            		updateAction.run();
			            	}
			            	return;
		            }		        
				}
			}
			public void keyReleased(KeyEvent e) {}
		});
				
		// obtain focus
		setFocus();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(fViewer.getControl());
		fViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, fViewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		//fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
		
		fPullDownActions = new LapseLayoutActionGroup(this);
		fPullDownActions.fillActionBars(bars);	
	}

	protected void fillContextMenu(IMenuManager manager) {
		//super.fillContextMenu(manager);
		//manager.add(updateAction);
		manager.add(selectAllAction);
		manager.add(copyAction);
		
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(updateAction);
		//manager.add(expandAction);
		manager.add(expandAllAction);
		
		//manager.add(collapseAction);
		manager.add(collapseAllAction);
		
		manager.add(prefAction);
		manager.add(historyAction);
		manager.add(copyAction);
	}

	class SlicingJob extends Job {
		public SlicingJob(String name) {
			super(name);
			
//			setProperty(IProgressConstants.ACTION_PROPERTY, 
//					new Action("Pop up a dialog") { 
//						public void run() {
//							MessageDialog.openInformation(getSite().getShell(), 
//									"Goto Action", 
//									"The job can have an action associated with it"
//							);
//						}
//			});
		}

		//Receives the monitor of the progress of the activity
		protected IStatus run(IProgressMonitor monitor) {
			
			IRunnableWithProgress runnable = new IRunnableWithProgress() {
				
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException{				
					
					try {
						if(fRoot == null || fOpenable == null) {
							logError("Uninitialized inputs");
							throw new RuntimeException();
						}
						
						updateSlice(fRoot, fOpenable.getBuffer().getUnderlyingResource(), monitor);
						
					} catch (InvalidSlicingSelectionException e) {
						throw new RuntimeException(e);
					} catch (JavaModelException e) {
						throw new RuntimeException(e);
					}
				}	
				
			};
			
			
			try {
				
				runnable.run(monitor);				
				
				if(fContentProvider.getTotalElementCount() == 0) {							
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							setContentDescription("Error creating a slice");
						}
					});
					return Status.CANCEL_STATUS;
					
				} else {
				    Display.getDefault().syncExec(new Runnable() {
				    	
						public void run() {
							// now enable the actions
							collapseAction.setEnabled(true);
							expandAction.setEnabled(true);
							
							collapseAllAction.setEnabled(true);
							expandAllAction.setEnabled(true);
							
							//System.err.println("Setting enabled to true");
							// enable actions in the menu
							fPullDownActions.setEnabled(true);
							refresh();
						}
						
				    });
				}
				
				return Status.OK_STATUS;
			} catch (InvocationTargetException e) {
				log(e.getMessage(), e);
			} catch (InterruptedException e) {
                log(e.getMessage(), e);			
			} catch (RuntimeException e) {
                log(e.getMessage(), e);
			}
			
			return Status.CANCEL_STATUS;
		}
	}
	
	class SlicingFromSinkJob extends Job {
		
		ASTNode sink;
		CompilationUnit unit;
		IResource resource;
		private LapseView view;

		public SlicingFromSinkJob(String name, LapseView view) {
			super(name);
			this.view = view;
			
			setProperty(IProgressConstants.ACTION_PROPERTY, 
					new Action("Pop up a dialog") { 
						public void run() {
							MessageDialog.openInformation(getSite().getShell(), "Goto Action", "The job can have an action associated with it");
						}
			});
		}
			
		public void setSink(ASTNode sink) {
			this.sink = sink;
		}
		
		public ASTNode getSink() {
			return sink;
		}
		
		public IResource getResource() {
			return resource;
		}
		
		public void setResource(IResource resource) {
			
			if(resource == null) {
				throw new RuntimeException("Setting the resource to NULL");
			}
			
			this.resource = resource;
		}
		
		public CompilationUnit getUnit() {
			return unit;
		}
		
		public void setUnit(CompilationUnit unit) {
			if(unit == null) {
				throw new RuntimeException("Setting the unit to NULL");
			}
			this.unit = unit;
		}

		protected IStatus run(IProgressMonitor monitor) {
			IRunnableWithProgress runnable = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {					
					
					try {						
						updateSlice(sink, unit, resource, monitor);
					} catch (InvalidSlicingSelectionException e) {
						throw new RuntimeException(e);
					}
				}				
			};
			
			try {
				try {
					runnable.run(monitor);
				} catch ( RuntimeException e) {					
					return Status.CANCEL_STATUS;
				}
				
				if(fContentProvider.getTotalElementCount() == 0) {							
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							setContentDescription("Error creating a slice");
						}
					});
					return Status.CANCEL_STATUS;
				} else {
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							// now enable the actions
							collapseAction.setEnabled(true);
							expandAction.setEnabled(true);
							collapseAllAction.setEnabled(true);
							expandAllAction.setEnabled(true);
							//System.err.println("Setting enabled to true");
							// enable actions in the menu
							fPullDownActions.setEnabled(true);
							view.getViewSite().getWorkbenchWindow().getActivePage().activate(view);
						}});
				}
				
				return Status.OK_STATUS;
			} catch (InvocationTargetException e) {
                log(e.getMessage(), e);
			} catch (InterruptedException e) {
                log(e.getMessage(), e);		
			} catch (RuntimeException e) {
                log(e.getMessage(), e);
				//showMessage("Invalid input state: " + e.getMessage());
			}
			
			return Status.CANCEL_STATUS;
		}
	}
	
	private void makeActions() {
		{
			updateAction = new Action() {
				public void run() {
					log("Pressed the update slice button");
					fSlicingJob.schedule();					
				}
			};
			updateAction.setText("Compute backward slice");
			updateAction.setToolTipText("Compute backward slice");
			ImageDescriptor desc = JavaPluginImages.DESC_OBJS_SEARCH_READACCESS;
			updateAction.setImageDescriptor(desc);
			updateAction.setHoverImageDescriptor(desc);
			updateAction.setAccelerator(SWT.CTRL | SWT.ALT | 'L');
		}
		{
			prefAction = new Action() {
				public void run() {
					LapseConfigurationDialog dialog = 
						new LapseConfigurationDialog(fViewer.getControl().getShell());
					//dialog.create();
					dialog.open();
					dialog.getReturnCode();
				}
			};
			prefAction.setText("Set slicing preferences");
			prefAction.setToolTipText("Set slicing preferences");
			ImageDescriptor desc = JavaPluginImages.DESC_ELCL_FILTER;
			prefAction.setImageDescriptor(desc);
			prefAction.setHoverImageDescriptor(desc);
		}
		
		{
			historyAction = new HistoryDropDownAction(this);
			historyAction.setText("Slicing history");
			historyAction.setToolTipText("Slicing history");
			ImageDescriptor desc = JavaPluginImages.DESC_OBJS_SEARCH_REF;
			historyAction.setImageDescriptor(desc);
			historyAction.setHoverImageDescriptor(desc);
		}
		
		{
			collapseAction = new Action() {
				public void run() {
					if(fExpansionLevel == MIN_EXPANSION_LEVEL) {
						beep();
					}else {
						fExpansionLevel--;
						fViewer.collapseAll();
						fViewer.expandToLevel(fExpansionLevel);
						fViewer.getTree().setRedraw(true);
						fViewer.refresh();
						//System.out.println("Expansion level: " + fExpansionLevel);
					}					
				}
			};
			collapseAction.setText("Collapse one level");
			collapseAction.setToolTipText("Collapse one level (Ctrl -)");
			//updateAction.setAccelerator(SWT.CTRL | '-');
			collapseAction.setEnabled(false);
			
			JavaPluginImages.setLocalImageDescriptors(collapseAction, "collapseall.gif");
		}
		
		{
			expandAction = new Action() {
				public void run() {
					if(fExpansionLevel == fContentProvider.getDepth()) {
						beep();
					}else {
						fExpansionLevel++;
						fViewer.expandToLevel(fExpansionLevel);
						fViewer.getTree().setRedraw(true);
						fViewer.refresh();
						//System.out.println("Expansion level: " + fExpansionLevel);
					}
				}
			};
			expandAction.setText("Expand one level");
			expandAction.setToolTipText("Expand one level  (Ctrl +)");
			//updateAction.setAccelerator(SWT.CTRL | '+');
			expandAction.setEnabled(false);
	
			JavaPluginImages.setLocalImageDescriptors(expandAction, "pack_empty_co.gif");
		}	
		
		{
			collapseAllAction = new Action() {
				public void run() {
					if(fExpansionLevel != MIN_EXPANSION_LEVEL) {
						fExpansionLevel = MIN_EXPANSION_LEVEL;	
						fViewer.collapseAll();
						fViewer.getTree().setRedraw(true);
						fViewer.refresh();
						//System.out.println("Expansion level: " + fExpansionLevel);
					} else {
						beep();
					}					
				}
			};
			collapseAllAction.setText("Collapse all");
			collapseAllAction.setToolTipText("Collapse all");
			collapseAllAction.setEnabled(false);
			
			JavaPluginImages.setLocalImageDescriptors(collapseAllAction, "collapseall.gif");
		}
		
		{
			expandAllAction = new Action() {
				public void run() {
					if(fExpansionLevel != fContentProvider.getDepth()) {
						fExpansionLevel = fContentProvider.getDepth();
						fViewer.expandAll();
						fViewer.getTree().setRedraw(true);
						fViewer.refresh();
						//System.out.println("Expansion level: " + fExpansionLevel);
					} else {
						beep();	
					}					
				}
			};
			expandAllAction.setText("Expand all");
			expandAllAction.setToolTipText("Expand all");
			expandAllAction.setEnabled(false);
			
			JavaPluginImages.setLocalImageDescriptors(expandAllAction, "pack_empty_co.gif");
		}	
		{
			copyAction = new LapseCopyAction(this, fClipboard, fViewer);
			copyAction.setText("Copy to clipboard");
			copyAction.setToolTipText("Copy to clipboard");
			copyAction.setEnabled(true);
			copyAction.setImageDescriptor(JavaPluginImages.DESC_DLCL_COPY_QUALIFIED_NAME);
		}
		{
			selectAllAction = new Action() {
				public void run() {
					fViewer.setSelection(
							new StructuredSelection(
									fContentProvider.getAllElements()));
				}
			};
			selectAllAction.setText("Select all");
			selectAllAction.setToolTipText("Select all");
			selectAllAction.setEnabled(true);
			ISharedImages workbenchImages = JavaPlugin.getDefault().getWorkbench().getSharedImages();
			selectAllAction.setImageDescriptor(workbenchImages.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));
		}
		log("Done making actions. Adding them to the bars.");
		
		contributeToActionBars();
	}
	
	private void setStatusBarMessage(String message, boolean isError) {
		IEditorStatusLine statusLine= (IEditorStatusLine) fEditor.getAdapter(IEditorStatusLine.class);
		if (statusLine != null) {
			statusLine.setMessage(false, message, JavaPluginImages.get(JavaPluginImages.IMG_MISC_PRIVATE));
		}else {
			logError("No status line!");
		}
		if(isError) {
			beep();
		}
	}	

	private void setStatusBarMessage(String message) {
		setStatusBarMessage(message, false);
	}
	
	private void beep() {
		if(fEditor != null) {
			fEditor.getSite().getShell().getDisplay().beep();
		}
	}

	private void hookViewerEvents()  {
		// double-click
		fViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				// handle double-click in the pane
				if(doubleClickAction == null) {
					doubleClickAction = new Action() {
						public void run() {
							//System.err.println("Detected a double-click");
							ISelection selection = fViewer.getSelection();
							HistoryDefinitionLocation dl = (HistoryDefinitionLocation) 
										((IStructuredSelection) selection).getFirstElement();							
							if(dl.getLineNumber() == DefinitionLocation.INVALID_SOURCE_LINE) {
								setStatusBarMessage("Invalid location", true);
								// invalid location -- get out of here
								return;
							}
							
							ITextEditor editor = null;
							try {
								editor = (ITextEditor) EditorUtility.openInEditor(dl.getResource());
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							if(editor == null) {
								JavaPlugin.logErrorMessage("Can't open an editor for " + dl.getASTNode());
								return;
							}
							
							editor.selectAndReveal(
									dl.getASTNode().getStartPosition(), 
									dl.getASTNode().getLength());							
							
							// activate the new window if necessary
							if(editor != fEditor) {
								try {									
									// reset the input
									setInput(editor);
									
									// reactivate the editor
									editor.getSite().getPage().activate(editor);
									
									log("Activated " + editor.getTitle());
								} catch (CoreException e) {
                                    log(e.getMessage(), e);
									return;
								}
							}
							setStatusBarMessage("Opened " + 
									dl.getResource().getName() + ":" + dl.getLineNumber() +
									" -- " + dl.toString());
						}
					};
				}

				doubleClickAction.run();
			}
		});
		
		fViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				HistoryDefinitionLocation match = (HistoryDefinitionLocation) sel.getFirstElement();
				if(match != null) {
					IStatusLineManager slManager = getViewSite().getActionBars().getStatusLineManager();
					slManager.setMessage(match.toString());
				}
			}
		});
	}
	private void showMessage(String message) {
		MessageDialog.openInformation(fViewer.getControl().getShell(),
				"Provenance Tracker", message);
	}

	public void setFocus() {
		fViewer.getControl().forceFocus();
	}
	
	public static IOpenable getJavaInput(IEditorPart part) {
	    IEditorInput editorInput= part.getEditorInput();
	    if (editorInput != null) {
	      IJavaElement input= (IJavaElement)editorInput.getAdapter(IJavaElement.class);
	      if (input instanceof IOpenable) {
	        return (IOpenable) input;
	      }
	    }
	    return null;  
	}

	static VariableDeclaration name2decl(SimpleName sn, CompilationUnit cu, IResource resource){
		DeclarationInfoManager.DeclarationInfo info = DeclarationInfoManager.retrieveDeclarationInfo(cu);
		//System.out.println(info);
		VariableDeclaration decl = info.getVariableDeclaration(sn);
		if(decl == null) {
			logError("decl == null for " + sn);	
		}
		return decl;
	}
	
	static boolean isFinal(SimpleName sn, CompilationUnit cu, IResource resource){
		
		DeclarationInfoManager.DeclarationInfo info = DeclarationInfoManager.retrieveDeclarationInfo(cu);
		return info.isFinal(sn);
	}
	
	/**
	 * Tests whether a given expression is a String contant.
	 * */
	public static boolean isStringContant(Expression arg, CompilationUnit cu, IResource resource) {
		if(arg instanceof StringLiteral) {		
			return true;
		} else
//		if(arg instanceof SimpleName) {
//			// find out if the name is final...
//		} else
		if(arg instanceof InfixExpression) {
			InfixExpression infixExpr = (InfixExpression) arg;
			if(!isStringContant(infixExpr.getLeftOperand(), cu, resource)) return false; 
			if(!isStringContant(infixExpr.getRightOperand(), cu, resource)) return false;
						
			for(Iterator iter2 = infixExpr.extendedOperands().iterator(); iter2.hasNext(); ) {
				if(!isStringContant((Expression) iter2.next(), cu, resource)) {
					return false;
				}
			}
			return true;
		} 
		// TODO: add final/const
		
		return false;	
	}

	void processNodeSelection(SimpleName sn, HistoryDefinitionLocation defl, CompilationUnit cu, IResource resource, IProgressMonitor monitor){
		//_cp.addElement("Visited node " + sn.toString());		
		if(name2decl(sn, cu, resource) == null) {
			// only deal with variables
			logError("No definition for " + sn + " is found");
			return;
		}
		
		//Name otherName = decl.getName();
		//String var = Name.getFullyQualifiedName();
		
		//fContentProvider.clearElements();
		// make the root of the slicing tree
		HistoryDefinitionLocation dl = new HistoryDefinitionLocation(
				sn.toString(),
				(IFile)resource, 
				cu.getLineNumber(sn.getStartPosition()), 
				sn, defl, HistoryDefinitionLocation.INITIAL);
		if(defl==null){
		setCurrentInput(dl);
		addHistoryEntry(dl);
		fContentProvider.addElement(dl);
		}
		processDecl(sn, cu, resource, dl, new LinkedList<MethodInvocation>(), monitor);
			
		//showMessage("Got " + _cp.getElementsCount() + " in the list");			
		//fViewer.setSelection(new StructuredSelection(covering));
	}
		
	private void processDecl(SimpleName name, CompilationUnit cu, IResource resource, 
			HistoryDefinitionLocation parent, LinkedList<MethodInvocation> stack, 
			IProgressMonitor monitor) 
	{		
		VariableDeclaration decl = name2decl(name, cu, resource);
//		if(TRACE) System.out.print('.');
		if(monitor.isCanceled()) {
			// check if the search has been cancelled 
			return;
		}
		//monitor.setTaskName("Processing " + name + " in " + resource.getName/*getFullPath*/());
		monitor.subTask("Processing " + name + " in " + resource.getName/*getFullPath*/());
				
		if(decl == null) {
			logError(
					"decl: " + decl + " on line " + (decl == null ? DefinitionLocation.INVALID_SOURCE_LINE : fRoot.getLineNumber(decl.getStartPosition())) + 
					" and name: " + name + "(" + name.hashCode() + ") on line " + fRoot.getLineNumber(name.getStartPosition()));
			
			// the case of no declaration found -- add a question mark
			/*HistoryDefinitionLocation dl = */new HistoryDefinitionLocation(
					name.toString(), 
					null,	// this will be ignored 
					DefinitionLocation.INVALID_SOURCE_LINE, 
					name, parent, HistoryDefinitionLocation.UNDEFINED);
			//System.out.println("Case 1");
		} else
		if(decl.getParent() instanceof MethodDeclaration){
			// the case of a parameter -- add the actuals and recurse
			//showMessage(decl.toString() + " is a parameter of " + parent.getClass());
			MethodDeclaration methodDecl = (MethodDeclaration) decl.getParent();
			IMethod method = new MethodFinder((IFile) resource).convertMethodDecl2IMethod(methodDecl);
			if(method == null) {
				JavaPlugin.logErrorMessage("Internal error: No method found for " + methodDecl);
				return;
			}

			HistoryDefinitionLocation paramDL = new HistoryDefinitionLocation(
					decl.toString(), 
					resource, 
					cu.getLineNumber(decl.getStartPosition()), 
					decl, parent, HistoryDefinitionLocation.FORMAL_PARAMETER);
			if(!registerExpansion(paramDL)) {
				// recursion detected here
				return;
			}

			Expression onlyCall = (Expression) (stack.isEmpty() ? null : stack.getLast());
			log("Looking for calls from " + onlyCall);
			Collection/*<ExpressionUnitPair>*/ c = CallerFinder.getActualsForFormal(method, name, onlyCall, monitor, null);
			if(c.isEmpty()){
				logError(
						"No suitable actual arguments for formal argument " + 
						name + " of " + method.getElementName() + " at " + 
						resource.getName() + " found");
			} else
			for (Iterator iter = c.iterator(); iter.hasNext();) {
				Utils.ExpressionUnitPair eup = (Utils.ExpressionUnitPair) iter.next();
				Expression 		e 			 = eup.getExpression();
				CompilationUnit nextCU		 = eup.getCompilationUnit();
				IResource 		nextResource = eup.getResource();
								
				processExpression(paramDL, e, nextCU, nextResource, stack, monitor, HistoryDefinitionLocation.CALL_ARG,false);
			}
			//System.out.println("Case 2");
		} else {
			/**
			 * The case of a declaration -- look at the right hand side.
			 * */
			Object obj = (decl.getParent() instanceof VariableDeclaration) ?
					decl.getParent() :
					decl;					
			String desc = obj.toString();
			int type = (decl.getParent() instanceof FieldDeclaration) ? 
						HistoryDefinitionLocation.FIELD :
						HistoryDefinitionLocation.DECLARATION;
			// the case of a regilar declaration being found 
			HistoryDefinitionLocation dl = new HistoryDefinitionLocation(
					desc, 
					resource, 
					cu.getLineNumber(decl.getStartPosition()), 
					decl, parent, type);
			Expression e = decl.getInitializer();
			
			if(e != null) {
				// TODO: add processing of the RHS of the declaration
				processExpression(dl, e, cu, resource, stack, monitor, HistoryDefinitionLocation.COPY,false);
			}
			//System.out.println("Case 3");
		}
	}

	protected void processExpression(HistoryDefinitionLocation parent, 
			Expression e, CompilationUnit cu, IResource resource, LinkedList<MethodInvocation> stack,
			IProgressMonitor monitor, int defaultType, boolean first) 
	{
		log("In processExpression with expr=" + e);
		
		monitor.subTask("Processing " + e.toString());
		SimpleName nextName = CallerFinder.SlicingUtils.getVariable(e);
		if(nextName != null) {
			/**
			 * Follow a simple assignment.
			 * */
			HistoryDefinitionLocation actualDL = new HistoryDefinitionLocation(
					nextName/*.getParent()*/.toString(), 
					resource,
					cu.getLineNumber(nextName.getStartPosition()), 
					nextName,
					parent, defaultType);
			//showMessage("Type " + nextName.getParent().getClass());					
			
			// marks actualDL as recursive if necessary
			if(registerExpansion(actualDL)) {
				processDecl(nextName, cu, resource, actualDL, stack, monitor);
			}
		} else 
		if(e instanceof MethodInvocation) {
			/**
			 * Go back through a method invocation.
			 * */
			MethodInvocation mi = (MethodInvocation) e;
			SimpleName methodName = mi.getName();
			log("Going back through callee " + methodName.toString());
			HistoryDefinitionLocation callDL; 
			if(!first){
			callDL= new HistoryDefinitionLocation(
					mi.toString(), 
					resource,
					cu.getLineNumber(mi.getStartPosition()), 
					mi,
					parent, HistoryDefinitionLocation.CALL);
			}
			else{
				callDL=parent;
			}
			
			if(registerExpansion(callDL)) {
				Collection/*<MethodDeclarationUnitPair>*/ callees = CallerFinder.findCallees(monitor, methodName.toString(), null, false);
				if(callees.isEmpty()) {
					if(SinkView.isDerivationName(methodName.getFullyQualifiedName()))
					{
							HistoryDefinitionLocation dl = new HistoryDefinitionLocation(
									mi.getExpression().toString(), 
									resource,
									cu.getLineNumber(mi.getStartPosition()), 
									mi.getExpression(), callDL, HistoryDefinitionLocation.DERIVATION);
							if(registerExpansion(dl)) {
								Expression expr = mi.getExpression();
								if(expr != null) {
									// Recurse on the returned expression
									processExpression(dl, expr, cu, resource, stack, monitor, HistoryDefinitionLocation.DERIVATION,false);
									
								}
							}
					}
					else{
					logError("No suitable callees for " + methodName + " in " + resource + " found");
					System.out.println("No suitable callees for " + methodName + " in " + resource + " found");
					}
				} else
				for (Iterator iter = callees.iterator(); iter.hasNext();) {
					Utils.MethodDeclarationUnitPair element 
														= (Utils.MethodDeclarationUnitPair) iter.next();
					MethodDeclaration methodDeclaration = element.getMethod();
					CompilationUnit nextCU 				= element.getCompilationUnit();
					IResource nextResource 				= element.getResource();				
				
					if(methodDeclaration  != null){
						if(!LapsePlugin.FOLLOW_INTO_FUNCTIONS) {
							/*HistoryDefinitionLocation dl = */new HistoryDefinitionLocation(
									"Method " + methodDeclaration.getName().getFullyQualifiedName(), 
									nextResource,
									nextCU.getLineNumber(e.getStartPosition()), 
									methodDeclaration, callDL, HistoryDefinitionLocation.RETURN);
						} else {
							Collection/*<ReturnStatement>*/ returns = CallerFinder.findReturns(monitor, methodDeclaration, null);
							for (Iterator iter2 = returns.iterator(); iter2.hasNext();) {
								ReturnStatement returnStmt = (ReturnStatement) iter2.next();
							
								HistoryDefinitionLocation dl = new HistoryDefinitionLocation(
										returnStmt.toString(), 
										nextResource,
										nextCU.getLineNumber(returnStmt.getStartPosition()), 
										returnStmt, callDL, HistoryDefinitionLocation.RETURN);
								if(registerExpansion(dl)) {
									Expression expr = returnStmt.getExpression();
									if(expr != null) {
										// Recurse on the returned expression
										stack.addLast(mi);
										processExpression(dl, expr, nextCU, nextResource, stack, monitor, HistoryDefinitionLocation.COPY,false);
										stack.removeLast();
									}
								}
							}
						}
					}
					else{
						//check if it is a derivtion expression
						if(SinkView.isDerivationName(methodName.getFullyQualifiedName()))
						{
				
										processExpression(callDL, mi.getExpression(), cu, resource, stack, monitor, HistoryDefinitionLocation.DERIVATION,false);
								
								break;
						}
					}	
				}
			}
		} else
		if ( e instanceof InfixExpression && ( ((InfixExpression)e).getOperator() == InfixExpression.Operator.PLUS ) ) {
			/**
			 * Follow arguments of a string concatenation.
			 * */
			InfixExpression ie   = (InfixExpression) e;
			Expression leftExpr  = ie.getLeftOperand();
			Expression rightExpr = ie.getRightOperand();
			HistoryDefinitionLocation concatDL;
			if(!first){
			concatDL = new HistoryDefinitionLocation(
					e.toString(), 
					resource,
					cu.getLineNumber(e.getStartPosition()), 
					e,
					parent, HistoryDefinitionLocation.STRING_CONCAT);
			}
			else
			concatDL=parent;
			
			if(registerExpansion(concatDL)) {
				processExpression(concatDL, leftExpr,  cu, resource, stack, monitor, HistoryDefinitionLocation.COPY,false);
				processExpression(concatDL, rightExpr, cu, resource, stack, monitor, HistoryDefinitionLocation.COPY,false);
				if(ie.extendedOperands() != null) {
				    for(Iterator iter = ie.extendedOperands().iterator(); iter.hasNext(); ) {
				        Expression ext_e = (Expression) iter.next();
				        
				        processExpression(concatDL, ext_e, cu, resource, stack, monitor, HistoryDefinitionLocation.COPY,false);      				        
				    }
				}
			}
		}
		else if(e instanceof ParenthesizedExpression){
			ParenthesizedExpression ex=(ParenthesizedExpression)e;
			processExpression(parent, ex.getExpression(), cu, resource, stack, monitor, HistoryDefinitionLocation.UNDEFINED,false);
		}
		else if(e instanceof CastExpression){
			CastExpression cex=(CastExpression)e;
			processExpression(parent, cex.getExpression(), cu, resource, stack, monitor, HistoryDefinitionLocation.UNDEFINED,false);
		}
		else if(e instanceof ArrayAccess) {
			ArrayAccess ae=(ArrayAccess)e;
			
			HistoryDefinitionLocation arrAccess;
			if(!first){
				arrAccess= new HistoryDefinitionLocation(
					e.toString(), 
					resource,
					cu.getLineNumber(e.getStartPosition()), 
					e,
					parent, HistoryDefinitionLocation.ARRAYACCESS);
			}
			else arrAccess=parent;
			if(registerExpansion(arrAccess)) 
				processExpression(arrAccess, ae.getArray(),  cu, resource, stack, monitor, HistoryDefinitionLocation.ARRAYACCESS,false);
			
		}
		else if(e instanceof ClassInstanceCreation)
		{
			ClassInstanceCreation c=(ClassInstanceCreation)e;
			HistoryDefinitionLocation cc;
			if(!first){
			cc= new HistoryDefinitionLocation(
					e.toString(), 
					resource,
					cu.getLineNumber(e.getStartPosition()), 
					e,
					parent, HistoryDefinitionLocation.CLASS_INSTANCE_CREATION);
			}else
				cc=parent;
			String aux=(c.getType()).toString();
			if(SinkView.isDerivationName(aux)){
				for(Object arg:c.arguments()){
					if(registerExpansion(cc)) 
					processExpression(cc,(Expression)arg,cu,resource,stack,monitor,HistoryDefinitionLocation.DERIVATION,false);
				}
		}
			}
		else {
			/** 
			 * Some other expression.
			 */
			/*HistoryDefinitionLocation dl = */new HistoryDefinitionLocation(
					e.toString(), 
					resource, 
					cu.getLineNumber(e.getStartPosition()), 
					e, parent, getExpressionType(e, cu, resource));						
		}
	}
		
	private int getExpressionType(Expression expr, CompilationUnit cu, IResource resource) {
		if(isStringContant(expr, cu, resource)) {
			return HistoryDefinitionLocation.STRING_CONSTANT;
		}else if(expr instanceof NullLiteral){
			return HistoryDefinitionLocation.NULL;
		}
		else {
			return HistoryDefinitionLocation.UNDEFINED;
		}
	}

	/**
	 * @return false means that propagation should be stopped.
	 * */
	private boolean registerExpansion(HistoryDefinitionLocation dl) {
		/**
		 * Recursion point is here. Before we recurse, we want to make sure we are not entering a 
		 * potentially infinite loop, so, here goes an occurs check.
		 * */
		if(dl.containsAncestor(dl.getASTNode())){
			dl.setRecursive(true);
			logError("Recursion detected for " + dl.getASTNode());
			return false;
		}
		if ( dl.getDepth() >= LapsePlugin.getMaxCallDepth()) {
			dl.setMaxLevel(true);
			logError("Depth of  " +  LapsePlugin.getMaxCallDepth() + " exceeded");
			return false;
		}
		//System.out.println("Recursing on " + dl.getASTNode() + " depth = " + dl.getDepth());
		return true;
	}

	protected void refresh() {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				setContentDescription("Created a slice with " +
						fContentProvider.getLeafElementCount() + " leaf element(s) and " +
						fContentProvider.getTotalElementCount() + " element(s) " +
						"located in " + fContentProvider.getFileCount() + " file(s) with " +
						fContentProvider.getTruncatedElementCount() + " element(s) truncated " +
						"with a maximum depth of  " + fContentProvider.getDepth() + ". " +
						"Using a " + 
							(fContentProvider instanceof ProvenanceContentProvider.FlatViewContentProvider ? 
									"flat" : 
										"hierarchical") +  
						" viewer.");
				fViewer.refresh();
				fViewer.expandAll();
				fExpansionLevel = fContentProvider.getDepth();
				
				ISelection selection = fContentProvider.getFirstElement();
				if(selection != null) {
					fViewer.setSelection(selection, true);
				}
			}
		});
	}

	public void setInput(ITextEditor editor) throws CoreException {
		if (fEditor != null) {
			uninstallModificationListener();
		}
	
		fEditor = null;
		fRoot   = null;
		if(editor == null) {
			logError("editor is set to null");
			return;
		}
	
		IOpenable openable = getJavaInput(editor);
		if (openable == null) {
			throw new RuntimeException("Editor not showing a classfile");
		}
		// reset the fields
		fOpenable = openable;
		fRoot 	  = internalSetInput(openable);
		fEditor	  = editor;
		
		installModificationListener();
	}	
	
	private CompilationUnit internalSetInput(IOpenable input) throws CoreException {
		IBuffer buffer = input.getBuffer();
		if (buffer == null) {
			JavaPlugin.logErrorMessage("Input has no buffer"); //$NON-NLS-1$
		}
		if (input instanceof ICompilationUnit) {
			fParser.setSource((ICompilationUnit) input);
		} else {
			fParser.setSource((IClassFile) input);
		}

		try {
			CompilationUnit root = (CompilationUnit) fParser.createAST(null);
			log("Recomputed the AST for " + buffer.getUnderlyingResource().getName());
							
			if (root == null) {
				JavaPlugin.logErrorMessage("Could not create AST"); //$NON-NLS-1$
			}
	
			return root;
		} catch (RuntimeException e) {
			JavaPlugin.logErrorMessage("Could not create AST:\n" + e.getMessage()); //$NON-NLS-1$
			return null;
		}
	}

	//--------------------------- Utilities --------------------------- 
	private void installModificationListener() {
		fCurrentDocument = fEditor.getDocumentProvider().getDocument(fEditor.getEditorInput());
		fCurrentDocument.addDocumentListener(fSuperListener);
	}

	void uninstallModificationListener() {
		if (fCurrentDocument != null) {
			fCurrentDocument.removeDocumentListener(fSuperListener);
			fCurrentDocument = null;
		}
	}

	void updateSlice(CompilationUnit unit, IResource resource, IProgressMonitor monitor) throws InvalidSlicingSelectionException {
		final NodeFinder finder = new NodeFinder();
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				try {
					if(fEditor == null) {
						JavaPlugin.logErrorMessage("fEditor == null, can't compute the slice");
						return;
					}
					ISelectionProvider selProv = fEditor.getSelectionProvider();
					if(selProv == null) {
						throw new InvalidSlicingSelectionException("No selection provider. Something is wrong.");
					}
					ISelection selection = selProv.getSelection();
					if(selection == null) {
						throw new InvalidSlicingSelectionException("No selection detected. Please select an identifier.");
					}
					if(! (selection instanceof ITextSelection) ) {
						throw new InvalidSlicingSelectionException("Selection type is not valid. Please select an identifier in a Java text editor.");
					}
					ITextSelection textSelection = (ITextSelection) selection;
					log("Working with " + textSelection.getText() + " in " + fEditor.getTitle());
					
					/*if(textSelection.getLength() == 0) {
						throw new InvalidSlicingSelectionException("The selection is empty. Please select an identifier.");
					}*/
					if(fRoot == null) {
						throw new InvalidSlicingSelectionException("No current document for the plugin. Something is wrong.");	
					}
					
					finder.setOffset(textSelection.getOffset());
					finder.setLength(textSelection.getLength());

					fRoot.accept(finder);
				} catch (InvalidSlicingSelectionException e) {
					throw new RuntimeException(e);
				}
			}
		});
		
		// found the relevant ASTNode
		ASTNode covering = finder.getCoveringNode();
		updateSlice(covering, unit, resource, monitor);		
		
		// restore editor position
		// TODO:
		//fEditor.setHighlightRange(textSelection.getOffset(), textSelection.getLength(), true);
	}
	
	void updateSlice(final ASTNode covering, CompilationUnit unit, IResource resource, IProgressMonitor monitor) throws InvalidSlicingSelectionException {
		log("Working with " + covering);
		if(covering instanceof SimpleName) {
			String initialLocation = resource.getFullPath().toOSString() + ":" + unit.getLineNumber(covering.getStartPosition());
			monitor.setTaskName("Computing a backwards slice for " + covering + " at " + 
					(initialLocation != null? initialLocation : "") );
			monitor.beginTask("Started the computation", 100);
			
			processNodeSelection((SimpleName)covering,null,unit, resource, monitor);
					
			//String line = "sn: " + sn + ", var: " + var + ", line: " + fRoot.lineNumber(decl.getStartPosition());
			// refresh the screen
			
			refresh();
			
			// finish the job				
			monitor.done();
			}
		else if(covering instanceof InfixExpression){
		  InfixExpression infixExpr = (InfixExpression) covering;
		  HistoryDefinitionLocation dl = new HistoryDefinitionLocation(
					infixExpr.toString(),
					(IFile)resource, 
					unit.getLineNumber(0), 
					covering,null, HistoryDefinitionLocation.STRING_CONCAT);
		  
			processExpression(dl, infixExpr, unit, resource, new LinkedList<MethodInvocation>(), monitor, HistoryDefinitionLocation.INITIAL,true);
			setCurrentInput(dl);
			addHistoryEntry(dl);
			fContentProvider.addElement(dl);
			refresh();
		  }
		 else if(covering instanceof ArrayAccess) {
			 ArrayAccess ae=(ArrayAccess)covering;
			 HistoryDefinitionLocation dl = new HistoryDefinitionLocation(
			ae.toString(),
						(IFile)resource, 
						unit.getLineNumber(0), 
						covering,null, HistoryDefinitionLocation.ARRAYACCESS);
				processExpression(dl, ae, unit, resource, new LinkedList<MethodInvocation>(), monitor, HistoryDefinitionLocation.INITIAL,true);
				setCurrentInput(dl);
				addHistoryEntry(dl);		
				fContentProvider.addElement(dl);
				
				refresh();
				
			}
			 else if(covering instanceof CastExpression) {
				 CastExpression ae=(CastExpression)covering;
				 HistoryDefinitionLocation dl = new HistoryDefinitionLocation(
							ae.toString(),
							(IFile)resource, 
							unit.getLineNumber(0), 
							covering,null, HistoryDefinitionLocation.INITIAL);
					processExpression(dl, ae, unit, resource, new LinkedList<MethodInvocation>(), monitor, HistoryDefinitionLocation.CALL,true);
					setCurrentInput(dl);
					addHistoryEntry(dl);
					fContentProvider.addElement(dl);
					refresh();
				}
			 else if(covering instanceof ClassInstanceCreation) {
				 ClassInstanceCreation ae=(ClassInstanceCreation)covering;
				 HistoryDefinitionLocation dl = new HistoryDefinitionLocation(
							ae.toString(),
							(IFile)resource, 
							unit.getLineNumber(0), 
							covering,null, HistoryDefinitionLocation.CLASS_INSTANCE_CREATION);
					processExpression(dl, ae, unit, resource, new LinkedList<MethodInvocation>(), monitor, HistoryDefinitionLocation.CALL,true);
					setCurrentInput(dl);
					addHistoryEntry(dl);
					fContentProvider.addElement(dl);
					refresh();
				}
			 else if(covering instanceof MethodInvocation) {
				MethodInvocation mi=(MethodInvocation)covering;
				HistoryDefinitionLocation dl = new HistoryDefinitionLocation(
							mi.toString(),
							(IFile)resource, 
							unit.getLineNumber(0), 
							covering,null, HistoryDefinitionLocation.CALL);
					processExpression(dl, mi, unit, resource, new LinkedList<MethodInvocation>(), monitor, HistoryDefinitionLocation.CALL,true);
					setCurrentInput(dl);
					addHistoryEntry(dl);
					fContentProvider.addElement(dl);
					refresh();	
				}
		else{
			// do nothing...
			//throw new InvalidSlicingSelectionException
			Display.getDefault().syncExec(new Runnable() { 
				public void run() {
					showMessage("No valid identifier corresponds to " + covering);
				}}
			);
			
		}
		
	}

	public boolean isFlatLayout() {
		return fContentProvider instanceof ProvenanceContentProvider.FlatViewContentProvider;
	}
	void toggleViewer() {
		if(fContentProvider == null) {
			JavaPlugin.logErrorMessage("In switchViewer with null");
			return;
		}
		// do the switch
		fContentProvider = fContentProvider.switchType();
		fViewer.setContentProvider(fContentProvider);
		
		//System.out.println("Switched the viewer type to " + fContentProvider);
		refresh();
	}
	
	//-------------------------------------------- History ---------------------------------------------
	private LinkedList<HistoryDefinitionLocation> fInputHistory = new LinkedList<HistoryDefinitionLocation>();
//	private HistoryDefinitionLocation fCurrentInput;
	/**
	 * Adds the entry if new. Inserted at the beginning of the history entries list.
	 */		
	private void addHistoryEntry(HistoryDefinitionLocation entry) {
		HistoryDefinitionLocation oldEntry = getHistoryEntry(entry);
		if (oldEntry != null) {
			fInputHistory.remove(oldEntry);
		}
		
		fInputHistory.add(0, entry);
		
		historyAction.setEnabled(true);
	}
	
	private HistoryDefinitionLocation getHistoryEntry(HistoryDefinitionLocation entry) {
		for (Iterator iter = fInputHistory.iterator(); iter.hasNext();) {
			HistoryDefinitionLocation element = (HistoryDefinitionLocation) iter.next();
			if(element.getASTNode() == entry.getASTNode()) {
				return element;
			}
		}
		return null;
	}
	
	private void updateHistoryEntries() {
        for (int i = fInputHistory.size() - 1; i >= 0; i--) {
            HistoryDefinitionLocation type = fInputHistory.get(i);
        }
        historyAction.setEnabled(!fInputHistory.isEmpty());
    }
	
	/**
	 * Goes to the selected entry, without updating the order of history entries.
	 */	
	public void gotoHistoryEntry(HistoryDefinitionLocation entry) {
		if (fInputHistory.contains(entry)) {
			setCurrentInput(entry);		// TODO
			refresh();
		}
	}
	
	/**
	 * Gets all history entries.
	 */
	public HistoryDefinitionLocation[] getHistoryEntries() {
		if (fInputHistory.size() > 0) {
			updateHistoryEntries();
		}
		return (HistoryDefinitionLocation[]) fInputHistory.toArray(
				new HistoryDefinitionLocation[fInputHistory.size()]);
	}
	
	/**
	 * Sets the history entries
	 */
	public void setHistoryEntries(HistoryDefinitionLocation[] elems) {
		fInputHistory.clear();
		for (int i= 0; i < elems.length; i++) {
			fInputHistory.add(elems[i]);
		}
		updateHistoryEntries();
	}

	public HistoryDefinitionLocation getCurrentInput() {
		return fContentProvider.getCurrentInput();
	}
	public void setCurrentInput(HistoryDefinitionLocation currentInput) {
		fContentProvider.setCurrentInput(currentInput);
		//if(lastVariable)
		refresh();
	}

	public SlicingFromSinkJob getSinkSlicingJob() {
		return this.fSlicingFromSinkJob;
	}
    
     private static void log(String message, Throwable e) {
        LapsePlugin.trace(LapsePlugin.ALL_DEBUG, "Lapse view: " + message, e);
    }
    
    private static void logError(String message) {
        log(message, new Throwable());
    }
    
    private static void log(String message) {
        log(message, null);
    }
}



//Tree Viewer for Provenance Tracker
class LocationViewer extends TreeViewer {
    
	LocationViewer(Composite parent) {
    	super(new Tree(parent, SWT.MULTI));//For Multiselection behaviour in lists or text fields
//    	setAutoExpandLevel(ALL_LEVELS);
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
}


class InvalidSlicingSelectionException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	InvalidSlicingSelectionException(String message){
		super(message);
	}
}


class LapseCopyAction extends Action {
    private static final char INDENTATION= '\t';
    
    private LapseView fView;
    private LocationViewer fViewer; 
    private final Clipboard fClipboard;

    public LapseCopyAction(LapseView view, Clipboard clipboard, LocationViewer viewer) {
        
    	super("LapseCopyAction");//we create the copy action with the name given
    	
        Assert.isNotNull(clipboard);
        
        //WorkbenchHelp.setHelp(this, IJavaHelpContextIds.CALL_HIERARCHY_COPY_ACTION);
        
        fView= view;
        fClipboard= clipboard;
        fViewer= viewer;
    }

    public void run() {
        StringBuffer buf= new StringBuffer();
        
        addCalls(fViewer.getTree().getSelection()[0], 0, buf);//we get the node selected in the tree

        TextTransfer plainTextTransfer = TextTransfer.getInstance();//for converting plain text in a String into Platform specific representation
        
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
    
    private void addCalls(TreeItem item, int indent, StringBuffer buf) {
        for (int i= 0; i < indent; i++) {
            buf.append(INDENTATION);
        }

        buf.append(item.getText());
        buf.append('\n');
        
        if (item.getExpanded()) {
            TreeItem[] items= item.getItems();
            for (int i= 0; i < items.length; i++) {
                addCalls(items[i], indent + 1, buf);
            }
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