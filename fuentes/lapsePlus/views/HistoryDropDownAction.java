package lapsePlus.views;

/*
* HistoryDropDownAction.java, version 2.8, 2010
*/

import lapsePlus.HistoryDefinitionLocation;

import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;


/**
 * This file provides support for a pull-down history menu.
 * */
public class HistoryDropDownAction extends Action implements IMenuCreator {
	public static final int RESULTS_IN_DROP_DOWN = 10;

	private LapseView fView;
	private Menu fMenu;
	
	public HistoryDropDownAction(LapseView view) {
		fView = view;
		fMenu= null;
		//setToolTipText(TypeHierarchyMessages.getString("HistoryDropDownAction.tooltip")); //$NON-NLS-1$
		JavaPluginImages.setLocalImageDescriptors(this, "history_list.gif"); //$NON-NLS-1$
		setMenuCreator(this);
	}

	public void dispose() {
		// action is reused, can be called several times.
		if (fMenu != null) {
			fMenu.dispose();
			fMenu= null;
		}
	}

	public Menu getMenu(Menu parent) {
		return null;
	}

	public Menu getMenu(Control parent) {
		if (fMenu != null) {
			fMenu.dispose();
		}
		fMenu= new Menu(parent);
		HistoryDefinitionLocation[] elements= fView.getHistoryEntries();
		boolean checked = addEntries(fMenu, elements);
		if (elements.length > RESULTS_IN_DROP_DOWN) {
			new MenuItem(fMenu, SWT.SEPARATOR);
//			Action others= new HistoryListAction(fView);
//			others.setChecked(checked);
//			addActionToMenu(fMenu, others);
		}
		return fMenu;
	}
	
	private boolean addEntries(Menu menu, HistoryDefinitionLocation[] elements) {
		boolean checked = false;
		
		int min = Math.min(elements.length, RESULTS_IN_DROP_DOWN);
		for (int i= 0; i < min; i++) {
			HistoryAction action = new HistoryAction(fView, elements[i]);
			action.setChecked(elements[i].equals(fView.getCurrentInput()));
			checked= checked || action.isChecked();
			addActionToMenu(menu, action);
		}	
		return checked;
	}
	

	protected void addActionToMenu(Menu parent, Action action) {
		ActionContributionItem item= new ActionContributionItem(action);
		item.fill(parent, -1);
	}
}

class HistoryAction extends Action {
	private LapseView fViewPart;
	private HistoryDefinitionLocation fElement;
	
	public HistoryAction(LapseView viewPart, HistoryDefinitionLocation element) {
		super();
		fViewPart= viewPart;
		fElement= element;		
		
		setText(element.getName() + " at " + element.getResource().getName() + ":" + element.getLineNumber());
		setImageDescriptor(getImageDescriptor(element));
				
		setDescription(element.getName() + " at " + element.getResource().getName() + ":" + element.getLineNumber());
		setToolTipText(element.toString());		
	}
	
	private ImageDescriptor getImageDescriptor(HistoryDefinitionLocation elem) {
		JavaElementImageProvider imageProvider= new JavaElementImageProvider();
		ImageDescriptor desc = JavaPluginImages.DESC_FIELD_DEFAULT;
		imageProvider.dispose();
		return desc;
	}
	
	/*
	 * @see Action#run()
	 */
	public void run() {
		fViewPart.gotoHistoryEntry(fElement);
	}	
}