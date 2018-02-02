package lapsePlus.views;

/*
* SuperListener.java,version 2.8, 2010
*/

import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.filebuffers.IFileBufferListener;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import org.eclipse.jdt.internal.ui.JavaPlugin;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.ITextEditor;


class SuperListener implements ISelectionListener, IFileBufferListener, IDocumentListener
{
	private final LapseView view;
	
	/**
	 * @param LapseView
	 */
	SuperListener(LapseView view) {
		this.view = view;
	}
	
	//Listener to see the changes in the selection of the text editor view
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		
		if (part != this.view.fEditor && part instanceof ITextEditor && (LapseView.getJavaInput((ITextEditor) part) != null)) 
		{
			try {
				if(LapseView.TRACE) {
				    System.out.println("In selectionChanged: setting the input");
				}

				this.view.setInput((ITextEditor) part);
				
				if(this.view.fEditor == null) {
					throw new RuntimeException("Couldn't set the editor properly");
				}
				
			} catch (CoreException e) {
				JavaPlugin.logErrorMessage("Caught exception: " + e.toString());
				return;
			}				
		}
		
	}
	
	public void bufferCreated(IFileBuffer buffer) {
		//System.out.println("Buffer created for " + buffer.getLocation());
	}
	
	public void bufferDisposed(IFileBuffer buffer) {
		if (buffer instanceof ITextFileBuffer) {
			this.view.uninstallModificationListener();
		}
	}
	
	public void bufferContentAboutToBeReplaced(IFileBuffer buffer) {}
	public void bufferContentReplaced(IFileBuffer buffer) {}
	public void stateChanging(IFileBuffer buffer) {}
	public void dirtyStateChanged(IFileBuffer buffer, boolean isDirty) {}
	public void stateValidationChanged(IFileBuffer buffer, boolean isStateValidated) {}
	public void underlyingFileMoved(IFileBuffer buffer, IPath path) {}
	public void underlyingFileDeleted(IFileBuffer buffer) {}
	public void stateChangeFailed(IFileBuffer buffer) {}
	
	
	public void documentChanged(DocumentEvent event) {
		try {
			this.view.setInput(this.view.fEditor);	
		} catch (CoreException e) {
			JavaPlugin.log(e);
		}			
	}
	public void documentAboutToBeChanged(DocumentEvent event) {}		
}