package lapsePlus.views;

/*
* LapseLayoutActionGroup.java, version 2.8, 2010
*/

import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IActionBars;

class LapseLayoutActionGroup extends LapseRadioActionGroup {
	
	public LapseLayoutActionGroup(LapseView packageExplorer) {
		super(
				//creates hierarchical and leaves only options in Provenance Tracker View
				createActions(packageExplorer), 
				packageExplorer.isFlatLayout() ? 1 : 0,
				false
			);
	}

	private static LayoutAction hierarchicalLayoutAction;
	private static LayoutAction flatLayoutAction;
	
	static int getSelectedState(LapseView packageExplorer) {
		if (packageExplorer.isFlatLayout())
			return 1;
		else
			return 0;
	}
	
	public void fillActionBars(IActionBars actionBars){
		addActions(actionBars.getMenuManager());
		setEnabled(false);
	}
	
	/**Provenance Tracker - Hierarchichal Layout, Leaves Only Layout*/
	
	static IAction[] createActions(LapseView packageExplorer) {
		hierarchicalLayoutAction = new LayoutAction(packageExplorer, false);
		hierarchicalLayoutAction.setText("Hierarchical layout");
		JavaPluginImages.setLocalImageDescriptors(hierarchicalLayoutAction, "hierarchicalLayout.gif");
		
		flatLayoutAction = new LayoutAction(packageExplorer, true);
		flatLayoutAction.setText("Leaves only");
		JavaPluginImages.setLocalImageDescriptors(flatLayoutAction, "flatLayout.gif");
		
		return new IAction[]{hierarchicalLayoutAction, flatLayoutAction};
	}
}


class LayoutAction extends Action implements IAction {
	private boolean fIsFlatLayout;
	private LapseView fViewer;

	public LayoutAction(LapseView viewer, boolean flat) {
		super("", AS_PUSH_BUTTON);

		fIsFlatLayout = flat;
		fViewer = viewer;
	}

	public void run() {
		if (fViewer.isFlatLayout() != fIsFlatLayout) {
			fViewer.toggleViewer();
		}
	}
}
