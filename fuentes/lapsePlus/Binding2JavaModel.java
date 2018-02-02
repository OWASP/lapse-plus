package lapsePlus;

/*
* Binding2JavaModel.java, version 2.8, 2010
*/

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

/**
 * A helper class to convert compiler bindings into corresponding 
 * Java elements.
 */
public class Binding2JavaModel {
    
    private Binding2JavaModel(){}


    public static ICompilationUnit findCompilationUnit(ITypeBinding typeBinding, IJavaProject project) throws JavaModelException {
        if (!typeBinding.isFromSource()) {
            return null;
        }
        while (typeBinding != null && !typeBinding.isTopLevel()) {
            typeBinding= typeBinding.getDeclaringClass();
        }
        if (typeBinding != null) {
            IPackageBinding pack= typeBinding.getPackage();
            String packageName= pack.isUnnamed() ? "" : pack.getName(); //$NON-NLS-1$
            IType type= project.findType(packageName, typeBinding.getName());
            if (type != null) {
                return type.getCompilationUnit();
            }
        }
        return null;
    }


    /**
     * Converts the given <code>IVariableBinding</code> into a <code>IField</code>
     * using the classpath defined by the given Java project. Returns <code>null</code>
     * if the conversion isn't possible.
     */
    public static IField find(IVariableBinding field, IJavaProject in) throws JavaModelException {
        IType declaringClass = find(field.getDeclaringClass(), in);
        if (declaringClass == null)
            return null;
        IField foundField= declaringClass.getField(field.getName());
        if (! foundField.exists())
            return null;
        return foundField;
    }
    
    /**
     * Converts the given <code>ITypeBinding</code> into a <code>IType</code>
     * using the classpath defined by the given Java project. Returns <code>null</code>
     * if the conversion isn't possible.
     */
    public static IType find(ITypeBinding type, IJavaProject scope) throws JavaModelException {
        if (type.isPrimitive())
            return null;
        String[] typeElements= getNameComponents(type);
        IJavaElement element= scope.findElement(getPathToCompilationUnit(type.getPackage(), typeElements[0]));
        IType candidate= null;
        if (element instanceof ICompilationUnit) {
            candidate= ((ICompilationUnit)element).getType(typeElements[0]);
        } else if (element instanceof IClassFile) {
            candidate= ((IClassFile)element).getType();
        } else if (element == null){
            if (type.isMember())
                candidate= findType(scope, getFullyQualifiedImportName(type.getDeclaringClass()));
            else
                candidate= findType(scope, getFullyQualifiedImportName(type));
        }
        
        if (candidate == null || typeElements.length == 1)
            return candidate;
            
        return findTypeInType(typeElements, candidate);
    }

    /** 
     * Finds a type by its qualified type name (dot separated).
     * @param jproject The java project to search in
     * @param str The fully qualified name (type name with enclosing type names and package (all separated by dots))
     * @return The type found, or null if not existing
     */ 
    public static IType findType(IJavaProject jproject, String fullyQualifiedName) throws JavaModelException {
        //workaround for bug 22883
        IType type= jproject.findType(fullyQualifiedName);
        if (type != null)
            return type;
        IPackageFragmentRoot[] roots= jproject.getPackageFragmentRoots();
        for (int i= 0; i < roots.length; i++) {
            IPackageFragmentRoot root= roots[i];
            type= findType(root, fullyQualifiedName);
            if (type != null && type.exists())
                return type;
        }   
        return null;
    }
    
    private static IType findType(IPackageFragmentRoot root, String fullyQualifiedName) throws JavaModelException{
        IJavaElement[] children= root.getChildren();
        for (int i= 0; i < children.length; i++) {
            IJavaElement element= children[i];
            if (element.getElementType() == IJavaElement.PACKAGE_FRAGMENT){
                IPackageFragment pack= (IPackageFragment)element;
                if (! fullyQualifiedName.startsWith(pack.getElementName()))
                    continue;
                IType type= findType(pack, fullyQualifiedName);
                if (type != null && type.exists())
                    return type;
            }
        }       
        return null;
    }
    
    private static IType findType(IPackageFragment pack, String fullyQualifiedName) throws JavaModelException{
        ICompilationUnit[] cus= pack.getCompilationUnits();
        for (int i= 0; i < cus.length; i++) {
            ICompilationUnit unit= cus[i];
            IType type= findType(unit, fullyQualifiedName);
            if (type != null && type.exists())
                return type;
        }
        return null;
    }
    
    private static IType findType(ICompilationUnit cu, String fullyQualifiedName) throws JavaModelException{
        IType[] types= cu.getAllTypes();
        for (int i= 0; i < types.length; i++) {
            IType type= types[i];
            if (getFullyQualifiedName(type).equals(fullyQualifiedName))
                return type;
        }
        return null;
    }
    
    /**
     * Returns the fully qualified name of the given type using '.' as separators.
     * This is a replace for IType.getFullyQualifiedTypeName
     * which uses '$' as separators. As '$' is also a valid character in an id
     * this is ambiguous. JavaCore PR: 1GCFUNT
     */
    public static String getFullyQualifiedName(IType type) {
        return type.getFullyQualifiedName('.');
    }
    

    /**
     * Finds the given <code>IMethodBinding</code> in the given <code>IType</code>. Returns
     * <code>null</code> if the type doesn't contain a corresponding method.
     */
    public static IMethod find(IMethodBinding method, IType type) throws JavaModelException {
        IMethod[] candidates= type.getMethods();
        for (int i= 0; i < candidates.length; i++) {
            IMethod candidate= candidates[i];
            if (candidate.getElementName().equals(method.getName()) && sameParameters(method, candidate)) {
                return candidate;
            }
        }
        return null;
    }

    public static IMethod findIncludingSupertypes(IMethodBinding method, IType type, IProgressMonitor pm) throws JavaModelException {
        IMethod inThisType= find(method, type);
        if (inThisType != null)
            return inThisType;
        IType[] superTypes= getAllSuperTypes(type, pm);
        for (int i= 0; i < superTypes.length; i++) {
            IMethod m= find(method, superTypes[i]);
            if (m != null)
                return m;
        }
        return null;
    }

    public static IMethod find(IMethodBinding method, IJavaProject scope) throws JavaModelException {
        IType type= find(method.getDeclaringClass(), scope);
        if (type == null)
            return null;
        return find(method, type);  
    }
    
    //---- Helper methods to convert a type --------------------------------------------
    
    private static IPath getPathToCompilationUnit(IPackageBinding packageBinding, String topLevelTypeName) {
        IPath result= new Path(""); //$NON-NLS-1$
        String[] packageNames= packageBinding.getNameComponents();
        for (int i= 0; i < packageNames.length; i++) {
            result= result.append(packageNames[i]);
        }
        return result.append(topLevelTypeName + ".java"); //$NON-NLS-1$
    }
    
    private static IType findTypeInType(String[] typeElements, IType jmType) {
        IType result= jmType;
        for (int i= 1; i < typeElements.length; i++) {
            result= result.getType(typeElements[i]);
            if (!result.exists())
                return null;
        }
        return result == jmType ? null : result;
    }
    
    //---- Helper methods to convert a method ---------------------------------------------
    
    private static boolean sameParameters(IMethodBinding method, IMethod candidate) throws JavaModelException {
        ITypeBinding[] methodParamters= method.getParameterTypes();
        String[] candidateParameters= candidate.getParameterTypes();
        if (methodParamters.length != candidateParameters.length)
            return false;
        IType scope= candidate.getDeclaringType();
        for (int i= 0; i < methodParamters.length; i++) {
            ITypeBinding methodParameter= methodParamters[i];
            String candidateParameter= candidateParameters[i];
            if (!sameParameter(methodParameter, candidateParameter, scope))
                return false;
        }
        return true;
    }
    
    private static boolean sameParameter(ITypeBinding type, String candidate, IType scope) throws JavaModelException {
        if (type.getDimensions() != Signature.getArrayCount(candidate))
            return false;
            
        // Normalizes types
        if (type.isArray())
            type= type.getElementType();
        candidate= Signature.getElementType(candidate);
        
        if (isPrimitiveType(candidate) || type.isPrimitive()) {
            return type.getName().equals(Signature.toString(candidate));
        } else {
            if (isResolvedType(candidate)) {
                return Signature.toString(candidate).equals(getFullyQualifiedName(type));
            } else {
                String[][] qualifiedCandidates= scope.resolveType(Signature.toString(candidate));
                if (qualifiedCandidates == null || qualifiedCandidates.length == 0)
                    return false;
                String packageName= type.getPackage().isUnnamed() ? "" : type.getPackage().getName(); //$NON-NLS-1$
                String typeName= getTypeQualifiedName(type);
                for (int i= 0; i < qualifiedCandidates.length; i++) {
                    String[] qualifiedCandidate= qualifiedCandidates[i];
                    if (qualifiedCandidate[0].equals(packageName) &&
                            qualifiedCandidate[1].equals(typeName)) {
                        return true;
                            }
                }
            }
        }
        return false;
    }
    
    public static String getTypeQualifiedName(ITypeBinding type) {
        StringBuffer buffer= new StringBuffer();
        createName(type, false, buffer);
        return buffer.toString();
    }
    
    private static boolean isPrimitiveType(String s) {
        char c= s.charAt(0);
        return c != Signature.C_RESOLVED && c != Signature.C_UNRESOLVED;
    }
    
    private static boolean isResolvedType(String s) {
        int arrayCount= Signature.getArrayCount(s);
        return s.charAt(arrayCount) == Signature.C_RESOLVED;
    }
    
    public static String[] getNameComponents(ITypeBinding type) {
        List<String> result= new ArrayList<String>(5);
        createName(type, false, result);
        return (String[]) result.toArray(new String[result.size()]);
    }
    
    private static void createName(ITypeBinding type, boolean includePackage, StringBuffer buffer) {
        ITypeBinding baseType= type;
        if (type.isArray()) {
            baseType= type.getElementType();
        }
        if (!baseType.isPrimitive() && !baseType.isNullType()) {
            ITypeBinding declaringType= baseType.getDeclaringClass();
            if (declaringType != null) {
                createName(declaringType, includePackage, buffer);
                buffer.append('.');
            } else if (includePackage && !baseType.getPackage().isUnnamed()) {
                buffer.append(baseType.getPackage().getName());
                buffer.append('.');
            }
        }
        if (!baseType.isAnonymous()) {
            buffer.append(type.getName());
        } else {
            buffer.append("$local$"); //$NON-NLS-1$
        }
    }
    
    private static void createName(ITypeBinding type, boolean includePackage, List<String> list) {
        ITypeBinding baseType= type;
        if (type.isArray()) {
            baseType= type.getElementType();
        }
        if (!baseType.isPrimitive() && !baseType.isNullType()) {
            ITypeBinding declaringType= baseType.getDeclaringClass();
            if (declaringType != null) {
                createName(declaringType, includePackage, list);
            } else if (includePackage && !baseType.getPackage().isUnnamed()) {
                String[] components= baseType.getPackage().getNameComponents();
                for (int i= 0; i < components.length; i++) {
                    list.add(components[i]);
                }
            }
        }
        if (!baseType.isAnonymous()) {
            list.add(type.getName());
        } else {
            list.add("$local$"); //$NON-NLS-1$
        }       
    }   
    
    
    public static String getFullyQualifiedImportName(ITypeBinding type) {
        if (type.isArray()) {
            return getFullyQualifiedName(type.getElementType());
        } else if (type.isAnonymous()) {
            return getFullyQualifiedImportName(type.getSuperclass());
        } else {
            return getFullyQualifiedName(type);
        }
    }
    
    public static String getFullyQualifiedName(ITypeBinding type) {
        StringBuffer buffer= new StringBuffer();
        createName(type, true, buffer);
        return buffer.toString();       
    }   

    public static IType[] getAllSuperTypes(IType type, IProgressMonitor pm) throws JavaModelException {
        //workaround for bugs 23644 and 23656
        try{
            pm.beginTask("", 3); //$NON-NLS-1$
            ITypeHierarchy hierarchy= type.newSupertypeHierarchy(new SubProgressMonitor(pm, 1));
            
            IProgressMonitor subPm= new SubProgressMonitor(pm, 2);
            List<IType> typeList= Arrays.asList(hierarchy.getAllSupertypes(type));
            subPm.beginTask("", typeList.size()); //$NON-NLS-1$
            Set<IType> types= new HashSet<IType>(typeList);
            for (Iterator iter= typeList.iterator(); iter.hasNext();) {
                IType superType= (IType)iter.next();
                IType[] superTypes= getAllSuperTypes(superType, new SubProgressMonitor(subPm, 1));
                types.addAll(Arrays.asList(superTypes));
            }
            types.add(type.getJavaProject().findType("java.lang.Object"));//$NON-NLS-1$
            subPm.done();
            return (IType[]) types.toArray(new IType[types.size()]);
        } finally {
            pm.done();
        }   
    }
    

}

