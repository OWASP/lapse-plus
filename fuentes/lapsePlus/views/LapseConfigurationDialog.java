package lapsePlus.views;

/*
* LapseConfigurationDialog.java,version 2.8, 2010
*/

import lapsePlus.LapsePlugin;

import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

class LapseConfigurationDialog extends StatusDialog {
    private Text fMaxCallDepth;
    private Text fMaxCallers;
    // checkboxes
    
    /*private Button fInitialUse;
    private Button fFilterOnNames;
    private Button fFormalParameters;
    private Button fLocalDeclarations;*/
    private Button fFollowCalls;
    private Button fLimitPropDepth;

    protected LapseConfigurationDialog(Shell parentShell) {
        super(parentShell);
    }

    /*
     * (non-Javadoc) Method declared on Window.
     */
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Provenance tracker properties");
    }

    protected Control createDialogArea(Composite parent) {
        Composite superComposite = (Composite) super.createDialogArea(parent);
        Composite composite = new Composite(superComposite, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout layout = new GridLayout();
        layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
        layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
        layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
        layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
        composite.setLayout(layout);
        //createFiltersArea(composite);
        //new Label(composite, SWT.NONE); // Filler
        createMaxCallDepthArea(composite);
        new Label(composite, SWT.NONE); // Filler
        applyDialogFont(parent);
        updateUIFromFilter();
        return composite;
    }

    private void createMaxCallDepthArea(Composite parent) {
        GridLayout l = new GridLayout();
        l.numColumns = 2;
        {
            Group group = new Group(parent, SWT.SHADOW_ETCHED_IN | SWT.FILL);
            group.setText("Slicer settings");
            group.setLayout(l);
            group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
            fLimitPropDepth = createCheckbox(group, "&Limit propagation depth", true);
            fLimitPropDepth.addSelectionListener(new SelectionListener() {
                public void widgetSelected(SelectionEvent e) {
                    fMaxCallDepth.setEnabled(fLimitPropDepth.getSelection());
                }

                public void widgetDefaultSelected(SelectionEvent e) {
                    // TODO Auto-generated method stub
                }
            });
            fLimitPropDepth.setSelection(true);
            new Label(group, SWT.NONE);
            new Label(group, SWT.NONE).setText("&Maximum call depth");
            fMaxCallDepth = new Text(group, SWT.SINGLE | SWT.BORDER);
            fMaxCallDepth.setTextLimit(4);
            fMaxCallDepth.addModifyListener(new ModifyListener() {
                public void modifyText(ModifyEvent e) {
                    validateInput();
                }
            });
        }
        {
            Group group = new Group(parent, SWT.SHADOW_ETCHED_IN | SWT.FILL);
            group.setText("Traversal through return results");
            group.setLayout(l);
            group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
            fFollowCalls = createCheckbox(group, "Follow t&hrough calls backwards", true);
            fFollowCalls.addSelectionListener(new SelectionListener() {
                public void widgetSelected(SelectionEvent e) {
                    fMaxCallers.setEnabled(fFollowCalls.getSelection());
                }

                public void widgetDefaultSelected(SelectionEvent e) {
                    // TODO Auto-generated method stub
                }
            });
            fFollowCalls.setSelection(true);
            new Label(group, SWT.NONE);
            new Label(group, SWT.NONE).setText("Maximum callers to &process");
            fMaxCallers = new Text(group, SWT.SINGLE | SWT.BORDER);
            fMaxCallers.setTextLimit(4);
            fMaxCallers.addModifyListener(new ModifyListener() {
                public void modifyText(ModifyEvent e) {
                    validateInput();
                }
            });
            {
                GridData gridData = new GridData(GridData.GRAB_HORIZONTAL | GridData.END);
                gridData.widthHint = convertWidthInCharsToPixels(2);
                fMaxCallDepth.setLayoutData(gridData);
            }
        }
        {
            GridData gridData = new GridData(GridData.GRAB_HORIZONTAL | GridData.END);
            gridData.widthHint = convertWidthInCharsToPixels(2);
            fMaxCallers.setLayoutData(gridData);
        }
    }

    /*private void createFiltersArea(Composite parent) {
        Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
        group.setText("Filter settings");
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        group.setLayout(layout);
        fInitialUse = createCheckbox(group, "Include &inital use", true);
        fInitialUse.setSelection(true);
        fFilterOnNames = createCheckbox(group, "Include &call arguments", true);
        fFilterOnNames.setSelection(true);
        fFormalParameters = createCheckbox(group, "Include &formal parameters", true);
        fFormalParameters.setSelection(true);
        fLocalDeclarations = createCheckbox(group, "Include &local declarations", true);
        fLocalDeclarations.setSelection(true);
        // GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL |
        // GridData.GRAB_HORIZONTAL);
        // gridData.widthHint = convertWidthInCharsToPixels(60);
    }*/

    /**
     * Creates a check box button with the given parent and text.
     * 
     * @param parent
     *            the parent composite
     * @param text
     *            the text for the check box
     * @param grabRow
     *            <code>true</code>to grab the remaining horizontal space,
     *            <code>false</code> otherwise
     * 
     * @return the check box button
     */
    private Button createCheckbox(Composite parent, String text, boolean grabRow) {
        Button button = new Button(parent, SWT.CHECK);
        if (grabRow) {
            GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
            button.setLayoutData(gridData);
        }
        button.setText(text);
        return button;
    }

    private void updateFilterFromUI() {
        int maxCallDepth = Integer.parseInt(this.fMaxCallDepth.getText());
        int maxCallers = Integer.parseInt(this.fMaxCallers.getText());
        LapsePlugin.setMaxCallDepth(maxCallDepth);
        LapsePlugin.setMaxCallers(maxCallers);
        LapsePlugin.FOLLOW_INTO_FUNCTIONS = fFollowCalls.getSelection();
        LapsePlugin.LIMIT_PROP_DEPTH = fLimitPropDepth.getSelection();
        // LapsePlugin.getDefault().getPreferenceStore().setValue("includeInitial",
        // fInitialUse.getEnabled());
    }

    /**
     * Updates the UI state from the given filter.
     * 
     * @param filter
     *            the filter to use
     */
    private void updateUIFromFilter() {
        fMaxCallDepth.setText(String.valueOf(LapsePlugin.getMaxCallDepth()));
        fMaxCallers.setText(String.valueOf(LapsePlugin.getMaxCallers()));
        fFollowCalls.setSelection(LapsePlugin.FOLLOW_INTO_FUNCTIONS);
        fLimitPropDepth.setSelection(LapsePlugin.LIMIT_PROP_DEPTH);
    }

    /**
     * Updates the filter from the UI state. Must be done here rather than by
     * extending open() because after super.open() is called, the widgetry is
     * disposed.
     * 
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    protected void okPressed() {
        if (!isIntValid(fMaxCallDepth.getText())) {
            if (fMaxCallDepth.forceFocus()) {
                fMaxCallDepth.setSelection(0, fMaxCallDepth.getCharCount());
                fMaxCallDepth.showSelection();
            }
        }
        if (!isIntValid(fMaxCallers.getText())) {
            if (fMaxCallers.forceFocus()) {
                fMaxCallers.setSelection(0, fMaxCallers.getCharCount());
                fMaxCallers.showSelection();
            }
        }
        updateFilterFromUI();
        super.okPressed();
    }

    private boolean isIntValid(String text) {
        if (text.length() == 0) return false;
        try {
            int num = Integer.parseInt(text);
            return (num >= 1 && num <= 99);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void validateInput() {
        StatusInfo status = new StatusInfo();
        if (!isIntValid(fMaxCallDepth.getText())) {
            status.setError("Invalid input: expecting a number between 1 and 99");
        }
        if (!isIntValid(fMaxCallers.getText())) {
            status.setError("Invalid input: expecting a number between 1 and 99");
        }
        updateStatus(status);
    }
}
