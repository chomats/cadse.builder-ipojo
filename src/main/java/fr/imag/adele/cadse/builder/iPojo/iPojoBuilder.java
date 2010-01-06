package fr.imag.adele.cadse.builder.iPojo;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

public class iPojoBuilder extends IncrementalProjectBuilder {

	
	protected static final String	IPOJO_CLASSES	= "ipojo-classes";
	public static final String BUILDER_ID = "fr.imag.adele.cadse.builder.iPojoBuilder";

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int,
	 *      java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
		IJavaProject jp =JavaCore.create(getProject());
		
		IProject directoryProject = getProject();
		
		IFile manifestFile = directoryProject.getFile(new Path("META-INF/MANIFEST.MF"));
		if (!manifestFile.exists()) {
			manifestFile = directoryProject.getFile(new Path("src/main/resources/META-INF/MANIFEST.MF"));
			if (!manifestFile.exists()) {
				manifestFile = directoryProject.getFile("sources/main/resources/META-INF/MANIFEST.MF");
			}
			if (!manifestFile.exists()) {
				return new IProject[0];
			}
		}
		IFile metadataFile = directoryProject.getFile("metadata.xml");
		if (!metadataFile.exists()) {
			metadataFile = directoryProject.getFile(new Path( 
					"src/main/resources/metadata.xml"));
			if (!metadataFile.exists()) {
				metadataFile = directoryProject.getFile(new Path( 
						"sources/main/resources/metadata.xml"));
			}
			if (!metadataFile.exists()) {
				return new IProject[0];
			}
		}
		IContainer outClasses = (IContainer) ResourcesPlugin.getWorkspace().getRoot().findMember(jp.getOutputLocation());
		// this code to resolve problem with manifest add ipojo
//		if (kind != FULL_BUILD) {
//			IResourceDelta delta = getDelta(getProject());
//			if (delta != null) {
//				if (delta.findMember(metadataFile.getProjectRelativePath())!= null) {
//					EclipsePojoization p = new EclipsePojoization();
//					p.begin(metadataFile, monitor);
//					for (String cn : p.getComponentsClasses()) {
//						IResource classR = outClasses.findMember(new Path(cn.replace('.', '/')+".class"));
//						if (classR != null && classR.exists()) {
//							classR.delete(true, monitor);
//						}
//					}
//					return new IProject[0];
//				}
//			}
//		}
		EclipsePojoization p = new EclipsePojoization();
		MarkerIpojoProblem.unmark(getProject());
		
		
		
		p.setOutClasses(outClasses);
		p.begin(metadataFile, monitor);
		p.manipulate();
		p.writeClasses(monitor);
		
		showErrors(getProject(), p);	
		p.finish(manifestFile);
		
		getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
		
		
		return new IProject[0];
	}

	
	
	private static void showErrors(IProject p, EclipsePojoization pojo) {
		for (Iterator iterator = pojo.getErrors().iterator(); iterator.hasNext();) {
			String m = (String) iterator.next();
		
			try {
				IMarker marker = MarkerIpojoProblem.mark(p);
				MarkerIpojoProblem.setMessage(marker, m);
				MarkerIpojoProblem.setSeverity(marker, IMarker.SEVERITY_ERROR);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		super.clean(monitor);
		MarkerIpojoProblem.unmark(getProject());
	}
	
	
}
