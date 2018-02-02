package lapsePlus.views;

/*
 * LapseMultiActionGroup.java, version 2.8, 2010
 */

import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.actions.ActionGroup;

abstract public class LapseMultiActionGroup extends ActionGroup {
	
	IAction[] fActions;
	MenuItem[] fItems;
	boolean[] fStatus;

	/**
	 * Creates a new action group with a given set of actions.
	 * 
	 * @param actions
	 *            the actions for this multi group
	 * @param currentSelection
	 *            decides which action is selected in the menu on start up.
	 *            Denotes the location in the actions array of the current
	 *            selected state. It cannot be null.
	 */
	public LapseMultiActionGroup(IAction[] actions) {
		super();

		fActions = actions;
	}

	/**
	 * Add the actions to the given menu manager.
	 */
	abstract protected void addActions(IMenuManager viewMenu);
}

class LapseRadioActionGroup extends LapseMultiActionGroup {
	private int fCurrentSelection;

	public LapseRadioActionGroup(IAction[] actions, int currentSelection, boolean toggle) {
		super(actions);

		fCurrentSelection = currentSelection;
		fActions = actions;
	}

	public void setEnabled(boolean enabled) {
		for (int i = 0; i < fItems.length; i++) {
			MenuItem e = fItems[i];
			if (e != null) {
				e.setEnabled(enabled);
			}
		}
	}

	/**
	 * Add the actions to the given menu manager.
	 */
	protected void addActions(IMenuManager viewMenu) {
		
		viewMenu.add(new Separator());//we begin adding a Separator
		
		fItems = new MenuItem[fActions.length];
		fStatus = new boolean[fActions.length];

		//We go all over the actions
		for (int i = 0; i < fActions.length; i++) {
			
			final int j = i;

			//We create the menu
			viewMenu.add(new ContributionItem() {//Contribution item in a menu is a button or a separator. A contribution item in a menu bar is a menu
				
				public void fill(Menu menu, int index) {
					// System.err.println("Filling the menu");
					int style = SWT.CHECK;
					
					if ((fActions[j].getStyle() & IAction.AS_RADIO_BUTTON) != 0)
						style = SWT.RADIO;

					//Initializing the menu and the images
					//The MenuItem receives a menu, the style of check or radio button
					MenuItem mi = new MenuItem(menu, style, index);
					ImageDescriptor d = fActions[j].getImageDescriptor();
					mi.setImage(JavaPlugin.getImageDescriptorRegistry().get(d));
					fItems[j] = mi;

					mi.setEnabled(true);
					mi.setText(fActions[j].getText());
					mi.setSelection(fCurrentSelection == j);
					fStatus[j] = (fCurrentSelection == j);

					//To know if the menu is selected
					mi.addSelectionListener(new SelectionAdapter() {
						public void widgetSelected(SelectionEvent e) {
							if (fCurrentSelection == j) {
								// already selected
								fItems[fCurrentSelection].setSelection(true);
								fStatus[fCurrentSelection] = true;
								return;
							}
							
							fActions[j].run();

							// Update checked state
							fItems[fCurrentSelection].setSelection(false);
							fStatus[fCurrentSelection] = false;
							fCurrentSelection = j;
							fItems[fCurrentSelection].setSelection(true);
							fStatus[fCurrentSelection] = true;
						}
					});
				}
			});
		}
	}
}

class LapseCheckboxActionGroup extends LapseMultiActionGroup {
	boolean fInitial[]; // initial values

	public LapseCheckboxActionGroup(IAction[] actions, boolean[] initial) {
		super(actions);

		fActions = actions;
		this.fInitial = initial;
	}

	public void setEnabled(boolean enabled) {
		for (int i = 0; i < fItems.length; i++) {
			MenuItem e = fItems[i];
			if (e != null) {
				e.setEnabled(enabled);
			}
		}
	}

	/**
	 * Add the actions to the given menu manager.
	 */
	protected void addActions(IMenuManager viewMenu) {
		viewMenu.add(new Separator());
		fItems = new MenuItem[fActions.length];
		fStatus = new boolean[fActions.length];

		for (int i = 0; i < fActions.length; i++) {
			final int j = i;

			if ((fInitial != null) && fInitial[j]) {
				fActions[j].run();
			}

			viewMenu.add(new ContributionItem() {
				public void fill(Menu menu, int index) {
					// System.err.println("Filling the menu");
					int style = SWT.CHECK;

					MenuItem mi = new MenuItem(menu, style, index);
					ImageDescriptor d = fActions[j].getImageDescriptor();
					mi.setImage(JavaPlugin.getImageDescriptorRegistry().get(d));
					fItems[j] = mi;

					mi.setEnabled(true);
					mi.setText(fActions[j].getText());

					mi.setSelection((fInitial != null) && fInitial[j]);
					fStatus[j] = (fInitial != null) && fInitial[j];

					mi.addSelectionListener(new SelectionAdapter() {
						public void widgetSelected(SelectionEvent e) {
							MenuItem item = fItems[j];
							// System.err.println("Old value: " +
							// item.getSelection());
							item.setSelection(!fStatus[j]);
							fStatus[j] = !fStatus[j];
							// System.err.println("New value: " +
							// item.getSelection());

							fActions[j].run();

						}
					});
				}
			});
		}
	}
}
