package lapsePlus.views;

/* 
 * DLTreeLabelProvider.java, version 2.8, 2010
*/

import lapsePlus.HistoryDefinitionLocation;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.callhierarchy.CallHierarchyImageDescriptor;
import org.eclipse.jdt.internal.ui.viewsupport.ImageImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.internal.dialogs.ViewLabelProvider;

public class DLTreeLabelProvider /*implements ILabelProvider*/ extends ColorDecoratingLabelProvider {
	public DLTreeLabelProvider() {
		super(new ViewLabelProvider(null, null), null);
	}
	
	public Color getForeground(Object element) {
		Display display = Display.getCurrent();
		HistoryDefinitionLocation loc = (HistoryDefinitionLocation) element;

		if(loc.isConstant()) {
			return display.getSystemColor(SWT.COLOR_DARK_BLUE);
		} else {
		    String fullCalleeName = null;
			if(loc.getASTNode() instanceof MethodInvocation) {
				MethodInvocation mi = (MethodInvocation) loc.getASTNode();
				Expression s=mi.getExpression();
				fullCalleeName = mi.getName().getFullyQualifiedName();
			}else
		    if(loc.getASTNode() instanceof ClassInstanceCreation) {
		        ClassInstanceCreation mi = (ClassInstanceCreation) loc.getASTNode();
				fullCalleeName = mi.getType().toString();
			}
		    
		    if(fullCalleeName != null) {
				if(SourceView.isSourceName(fullCalleeName)) {
					return display.getSystemColor(SWT.COLOR_DARK_RED);
				} else
			    if(SourceView.isSafeName(fullCalleeName)) {
					return display.getSystemColor(SWT.COLOR_DARK_GREEN);
				}
			}
		}
		
		return display.getSystemColor(SWT.COLOR_LIST_FOREGROUND);
	}

	public Image getImage(Object element) {
		if(!(element instanceof HistoryDefinitionLocation)) return null;
		HistoryDefinitionLocation hdl = (HistoryDefinitionLocation)element;
		Image result = null;
		switch(hdl.getType()) {
			case HistoryDefinitionLocation.CALL_ARG 		:
				result = JavaPluginImages.get(JavaPluginImages.IMG_MISC_PRIVATE);			// actual parameter of a call
				break;
			case HistoryDefinitionLocation.FORMAL_PARAMETER	:
				result = JavaPluginImages.get(JavaPluginImages.IMG_MISC_PROTECTED);			// formal parameter of a method
				break;
			case HistoryDefinitionLocation.RETURN	:
				result = JavaPluginImages.get(JavaPluginImages.IMG_OBJS_INTERFACE_DEFAULT);	// return result of a method 
				break;
			case HistoryDefinitionLocation.DECLARATION 		:
				result = JavaPluginImages.get(JavaPluginImages.IMG_MISC_PUBLIC);			// local declaration
				break;
			case HistoryDefinitionLocation.UNDEFINED   		:
				result = JavaPluginImages.get(JavaPluginImages.IMG_FIELD_PRIVATE);			// no definition found for some reason
				break;
			case HistoryDefinitionLocation.INITIAL     		:
				result = JavaPluginImages.get(JavaPluginImages.IMG_MISC_DEFAULT);			// initial query
				break;
			default:			
				result = JavaPluginImages.get(JavaPluginImages.IMG_MISC_PROTECTED);			
		}
		int flags = 0;
		if (hdl.isRecursive()) {
			flags |= CallHierarchyImageDescriptor.RECURSIVE;
		}
		if (hdl.isMaxLevel()) {
			flags |= CallHierarchyImageDescriptor.MAX_LEVEL;
		}
		// adorn the image with a small arrow
		ImageDescriptor baseImage= new ImageImageDescriptor(result);
        Rectangle bounds= result.getBounds();
        return JavaPlugin.getImageDescriptorRegistry().get(
        		new CallHierarchyImageDescriptor(
        				baseImage, 
        				flags, 
						new Point(bounds.width, bounds.height)));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		HistoryDefinitionLocation curElem = (HistoryDefinitionLocation) element;
		if (curElem == null) {
			return "(error)";
		}
		return curElem.toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void addListener(ILabelProviderListener listener) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
	 */
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void removeListener(ILabelProviderListener listener) {
	}
}