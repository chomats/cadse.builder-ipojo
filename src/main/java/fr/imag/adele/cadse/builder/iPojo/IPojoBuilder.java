/* 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package fr.imag.adele.cadse.builder.iPojo;

import java.util.Iterator;
import java.util.Map;


import org.apache.felix.ipojo.manipulator.Pojoization;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import fr.imag.adele.cadse.builder.Activator;

public class IPojoBuilder extends IncrementalProjectBuilder {

	
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
				metadataFile = null;
			}
		}
		try {
			IContainer outClasses = (IContainer) ResourcesPlugin.getWorkspace().getRoot().findMember(jp.getOutputLocation());
			EclipsePojoization p = new EclipsePojoization();
			p.setOutClasses(outClasses);
			p.monitor = monitor;
			MarkerIpojoProblem.unmark(getProject());
		
		
			p.directoryPojoization(outClasses.getLocation().toFile(), 
					metadataFile == null ? null : metadataFile.getLocation().toFile(), manifestFile.getLocation().toFile());
		
			
			showErrors(getProject(), p);
			
			getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
		
		} catch (Throwable e) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(),e));
		}
		return new IProject[0];
	}

	
	
	private static void showErrors(IProject p, Pojoization pojo) {
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
