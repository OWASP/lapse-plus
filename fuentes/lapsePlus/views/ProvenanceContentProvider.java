package lapsePlus.views;

/*
* ProvenanceContentProvider.java, version 2.8, 2010
*/

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import lapsePlus.HistoryDefinitionLocation;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;


public interface ProvenanceContentProvider extends ITreeContentProvider {
	public abstract void addElement(HistoryDefinitionLocation defLoc);
	public abstract void setCurrentInput(HistoryDefinitionLocation input);
	public abstract HistoryDefinitionLocation getCurrentInput();
	public abstract Object[] getAllElements();
	public abstract void clearElements();
	public abstract ProvenanceContentProvider switchType();
	public abstract ISelection getFirstElement();
	public abstract int getDepth();

	//----------------------------------  Methods for providing statistics ---------------------------------- 
	public abstract int getTotalElementCount();
	public abstract int getLeafElementCount();
	public abstract int getFileCount();
	public abstract int getTruncatedElementCount();

	public class HierarchicalViewContentProvider implements ProvenanceContentProvider {
		HistoryDefinitionLocation element = null;
		public HierarchicalViewContentProvider() {}
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
		public void dispose() {
		}
		public Object[] getElements(Object parent) {
			return element != null ? 
					new Object[] { element } :
					new Object[] {};
		}
		public void setElement(HistoryDefinitionLocation defLoc) {
			element = defLoc;
		}
		public void clearElements() {
			element = null;
		}
		public ProvenanceContentProvider switchType() {
			FlatViewContentProvider result = new FlatViewContentProvider();
			if(this.element != null) {
				result.addElement(this.element);
			}
	
			return result;
		}
		
		////////////////////////////////////////////////////////////////////////
		// 					Tree support
		//////////////////////////////////////////////////////////////////////// 
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof HistoryDefinitionLocation) {
	            HistoryDefinitionLocation loc = (HistoryDefinitionLocation) parentElement;
	            if(loc.getChildren() != null) {
	            	return loc.getChildren().toArray();	
	            }	            
	        }
	        return null;
		}
		public Object[] getAllElements() {
			ArrayList<HistoryDefinitionLocation> result = new ArrayList<HistoryDefinitionLocation>();
			if(getCurrentInput() != null) {
				addElementsUnder(getCurrentInput(), result);
			}
			return result.toArray();
		}
		
		private void addElementsUnder(HistoryDefinitionLocation currentInput, ArrayList<HistoryDefinitionLocation> result) {
			if(currentInput == null) {
				JavaPlugin.logErrorMessage("Called addElementsUnder with currentInput==null");
				return;
			} else {
				result.add(currentInput);		// the only place where elements are added
				if ( currentInput.hasChildren() ) {
					for (Iterator iter = currentInput.getChildren().iterator(); iter.hasNext();) {
						HistoryDefinitionLocation element = (HistoryDefinitionLocation) iter.next();
						addElementsUnder(element, result);
					}
				}
			}
		}
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
		 */
		public Object getParent(Object element) {
			if (element instanceof HistoryDefinitionLocation) {
	            return ((HistoryDefinitionLocation) element).getParent();
	        }
	
	        return null;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
		 */
		public boolean hasChildren(Object element) {
			if (element instanceof HistoryDefinitionLocation) {
				if(element == null) return false;
				
				Collection children = ((HistoryDefinitionLocation) element).getChildren();
				return children != null && !children.isEmpty();
			}
			return false;
		}
		
		/////////////////////////////////////// Methods for providing statistics /////////////////////////////////////// 
		public int getTotalElementCount() {		
			return getElementCount(element);
		}
		private int getElementCount(HistoryDefinitionLocation element) {
			if(element == null) return 0;
			if(element.getChildren() == null || element.getChildren().isEmpty()) return 1;
			int result = 1;
			for(Iterator iter = element.getChildren().iterator(); iter.hasNext();) {
				HistoryDefinitionLocation child = (HistoryDefinitionLocation) iter.next();
				result += getElementCount(child);
			}
			
			return result;
		}
		public int getLeafElementCount() {
			return getLeafElementCount(element);
		}
		private int getLeafElementCount(HistoryDefinitionLocation element) {
			if(element == null) return 0;
			if(element.getChildren() == null || element.getChildren().isEmpty()) return 1;
			int result = 0;
			for(Iterator iter = element.getChildren().iterator(); iter.hasNext();) {
				HistoryDefinitionLocation child = (HistoryDefinitionLocation) iter.next();
				result += getLeafElementCount(child);
			}
			
			return result;
		}
		
		public int getFileCount() {
			HashSet<IResource> files = new HashSet<IResource>();
			getFiles(element, files);
			return files.size();
		}
		private void getFiles(HistoryDefinitionLocation element, HashSet<IResource> files) {
			if(element == null) return;
			
			files.add(element.getResource());
			
			if(element.getChildren() == null || element.getChildren().isEmpty()) return;
			for(Iterator iter = element.getChildren().iterator(); iter.hasNext();) {
				HistoryDefinitionLocation child = (HistoryDefinitionLocation) iter.next();
				getFiles(child, files);
			}
		}
		
		public int getTruncatedElementCount() {
			return getTruncatedElementCount(element);
		}
		private int getTruncatedElementCount(HistoryDefinitionLocation element) {
			if(element == null) return 0;
			int result = element.isMaxLevel() ? 1 : 0;
	
			if(element.getChildren() == null || element.getChildren().isEmpty()) return result;
			
			for(Iterator iter = element.getChildren().iterator(); iter.hasNext();) {
				HistoryDefinitionLocation child = (HistoryDefinitionLocation) iter.next();
				result += getTruncatedElementCount(child);
			}
			
			return result;
		}
		/* (non-Javadoc)
		 * @see lapse.views.ProvenanceResultsContentProvider#addElement(lapse.HistoryDefinitionLocation)
		 */
		public void addElement(HistoryDefinitionLocation defLoc) {
			if(element == null) {
				setElement(defLoc);
			} else 
			if(defLoc != element){
				JavaPlugin.logErrorMessage("Error in addElement(...): calling addElement with " + defLoc + 
						" old value: " + element);
			} else {
				// just setting it twice, it's benign, I guess
			}
		}
		public void setCurrentInput(HistoryDefinitionLocation input) {
			setElement(input);
		}
		public HistoryDefinitionLocation getCurrentInput() {
			return this.element;
		}
		/* (non-Javadoc)
		 * @see lapse.views.ProvenanceContentProvider#getFirstElement()
		 */
		public ISelection getFirstElement() {
			if(element == null) return null;
			return new StructuredSelection(new Object[] {element});
		}
		/* (non-Javadoc)
		 * @see lapse.views.ProvenanceContentProvider#getDepth()
		 */
		public int getDepth() {
			return getDepth(element);
		}
		
		private int getDepth(HistoryDefinitionLocation element) {
			if(element == null) return 0;
			if(!element.hasChildren()) {
				return 1;
			}
			int maxLevel = 0;
			for (Iterator iter = element.getChildren().iterator(); iter.hasNext();) {
				HistoryDefinitionLocation e = (HistoryDefinitionLocation) iter.next();
				int level = getDepth(e);
				if(maxLevel < level) {
					maxLevel = level;
				}				
			}
			return maxLevel + 1;
		}
	}
	
	public class FlatViewContentProvider implements ProvenanceContentProvider {
		private Vector<HistoryDefinitionLocation> elements = new Vector<HistoryDefinitionLocation>();
		private static final Object[] EMPTY_ARRAY = new Object[] {};
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
		public void dispose() {
		}
		public Object[] getElements(Object parent) {
			return elements.toArray();
		}
		public Object[] getAllElements() {
			return elements.toArray();
		}
		public void addElement(HistoryDefinitionLocation defLoc) {
			if(!defLoc.hasChildren()) {
				// terminal node
				elements.add(defLoc);
			}else {
				for (Iterator iter = defLoc.getChildren().iterator(); iter.hasNext(); ) {
					HistoryDefinitionLocation element = (HistoryDefinitionLocation) iter.next();
					addElement(element);
				}
			}
		}
		public void clearElements() {
			elements.clear();
		}
		
		//
		// Tree support
		// 
		public Object[] getChildren(Object parentElement) {
			return EMPTY_ARRAY;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
		 */
		public Object getParent(Object element) {
			return null;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
		 */
		public boolean hasChildren(Object element) {		
			return false;
		}
		
		/////////////////////////////////////// Methods for providing statistics /////////////////////////////////////// 
		public int getTotalElementCount() {
			return elements.size();
		}
		public int getLeafElementCount() {
			return getTotalElementCount();
		}
		public int getFileCount() {
			HashSet<IResource> files = new HashSet<IResource>();
			for (Iterator iter = elements.iterator(); iter.hasNext();) {
				HistoryDefinitionLocation element = (HistoryDefinitionLocation) iter.next();
				files.add(element.getResource());
			}
			return files.size();
		}
		public int getTruncatedElementCount() {
			int result = 0;
			for (Iterator iter = elements.iterator(); iter.hasNext();) {
				HistoryDefinitionLocation element = (HistoryDefinitionLocation) iter.next();
				if(element.isMaxLevel()) {
					result++;
				}
			}
			return result;
		}
		public ProvenanceContentProvider switchType() {
			HierarchicalViewContentProvider result = new HierarchicalViewContentProvider();
			// need to find the topmost element
			if(this.getTotalElementCount() == 0) {
				result.element = null; 
			} else {
				HistoryDefinitionLocation dl = (HistoryDefinitionLocation) this.getAllElements()[0];
				do {
					if(dl.getParent() != null) {
						dl = (HistoryDefinitionLocation)dl.getParent();
					} else {
						break;
					}
				} while(true);
				// topmost element
				result.element = dl;
			}
			
			return result;
		}
		/* (non-Javadoc)
		 * @see lapsePlus.views.ProvenanceContentProvider#getFirstElement()
		 */
		public ISelection getFirstElement() {
			if(elements == null || elements.size() == 0) return null;
			return new StructuredSelection(new Object[] {elements.firstElement()});			
		}
		/* (non-Javadoc)
		 * @see lapsePlus.views.ProvenanceContentProvider#getDepth()
		 */
		public int getDepth() {
			return 1;
		}
		/* (non-Javadoc)
		 * @see lapsePlus.views.ProvenanceContentProvider#setCurrentInput(lapse.HistoryDefinitionLocation)
		 */
		public void setCurrentInput(HistoryDefinitionLocation input) {
			// TODO: fix this			
		}
		public HistoryDefinitionLocation getCurrentInput() {
			// TODO: fix this
			return null;
		}
	}
}