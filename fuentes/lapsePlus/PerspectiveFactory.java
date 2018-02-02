package lapsePlus;

/*
* PerspectiveFactory.java, version 2.8, 2010
*/

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class PerspectiveFactory implements IPerspectiveFactory {
	private static final String PROVENANCE_TRACKER_VIEW_ID = "lapsePlus.views.LapseView";
	private static final String SOURCE_VIEW_ID = "lapsePlus.views.SourceView";
	private static final String SINK_VIEW_ID = "lapsePlus.views.SinkView";

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
	 */
	public void createInitialLayout(IPageLayout layout) {
		 // Get the editor area.
		  String editorArea = layout.getEditorArea();
		  
		  IFolderLayout bottom = layout.createFolder("bottom", IPageLayout.BOTTOM, 0.75f, editorArea);
		  bottom.addView(PROVENANCE_TRACKER_VIEW_ID);
		  bottom.addView(SOURCE_VIEW_ID);
		  bottom.addView(SINK_VIEW_ID);
		  //bottom.addView(MARKER_VIEW_ID);
		  bottom.addView(IPageLayout.ID_TASK_LIST);
		  bottom.addPlaceholder(IPageLayout.ID_PROBLEM_VIEW);
	     
		  // Add the favorites action set
		  //layout.addActionSet(FAVORITES_ACTION_ID);	
		  
		  layout.addView(IPageLayout.ID_OUTLINE, IPageLayout.RIGHT, 0.85f, editorArea);
	}
}
