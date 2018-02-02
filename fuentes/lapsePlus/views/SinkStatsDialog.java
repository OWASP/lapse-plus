package lapsePlus.views;

/*
 * SinkStatsDialog.java, version 2.8, 2010
 */
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

class SinkStatsDialog extends StatusDialog {
    // private Text fMaxCallDepth;
    // private Text fMaxCallers;
    // // checkboxes
    // private Button fInitialUse;
    // private Button fFilterOnNames;
    // private Button fFormalParameters;
    // private Button fLocalDeclarations;
    // private Button fFollowCalls;
    // private Button fLimitPropDepth;
    private SinkView view;

    protected SinkStatsDialog(Shell parentShell, SinkView view) {
        super(parentShell);
        this.view = view;
    }

    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Sink statistics properties");
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
        Text outputArea = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.READ_ONLY | SWT.V_SCROLL);
        outputArea.setSize(500, 300);
        outputArea.setText(view.getStatisticsManager().getStatistics());
        outputArea.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLUE));
        return composite;
    }
}
