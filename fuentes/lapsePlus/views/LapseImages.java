package lapsePlus.views;

/*
* LapseImages.java, version 2.8, 2010
*/

import java.net.MalformedURLException;
import java.net.URL;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;

public class LapseImages {
	private static URL fgIconBaseURL = null;
	
	static {
		fgIconBaseURL= JavaPlugin.getDefault().getBundle().getEntry("/icons/"); //$NON-NLS-1$
		System.err.println("URL: " + fgIconBaseURL);
	}
	
	public static final String COLLAPSE= "collapseall.gif"; //$NON-NLS-1$
	public static final String EXPAND= "expandall.gif"; //$NON-NLS-1$
	public static final String LINK_WITH_EDITOR= "synced.gif"; //$NON-NLS-1$

	public static final String SETFOCUS= "setfocus.gif"; //$NON-NLS-1$
	public static final String REFRESH= "refresh.gif"; //$NON-NLS-1$

	public static final String STATISTICS= "statistics.gif"; //$NON-NLS-1$
	
	//---- Helper methods to access icons on the file system --------------------------------------
	public static void setImageDescriptors(IAction action, String type) {
		try {
			ImageDescriptor id = ImageDescriptor.createFromURL(makeIconFileURL("d", type)); //$NON-NLS-1$
			if (id != null)
				action.setDisabledImageDescriptor(id);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		try {
			ImageDescriptor id = ImageDescriptor.createFromURL(makeIconFileURL("c", type)); //$NON-NLS-1$
			if (id != null)
				action.setHoverImageDescriptor(id);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		action.setImageDescriptor(create("e", type)); //$NON-NLS-1$
	}
	
	
	private static ImageDescriptor create(String path, String name) {
		try {
			return ImageDescriptor.createFromURL(makeIconFileURL(path, name));
		} catch (MalformedURLException e) {
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}
	
	private static URL makeIconFileURL(String path, String name) throws MalformedURLException {
		if (fgIconBaseURL == null) {
			throw new MalformedURLException();
		}
			
		StringBuffer buffer= new StringBuffer(path);
		buffer.append('/');
		buffer.append(name);
		return new URL(fgIconBaseURL, buffer.toString());
	}	
}