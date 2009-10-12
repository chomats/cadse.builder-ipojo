package fr.imag.adele.cadse.builder.iPojo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Manifest;

import org.apache.felix.ipojo.manipulator.Pojoization;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.BundleException;

public class EclipsePojoization extends Pojoization {

	IContainer outClasses;
	
	public void begin(IFile metadataFile, IProgressMonitor monitor) throws CoreException {
		parseXMLMetadata(metadataFile.getLocation().toFile());
		
		computeDeclaredComponents();
	}
	
	
	public void finish(IFile manifestFile) {
		m_manifest = manifestFile.getLocation().toFile();
		m_referredPackages = getReferredPackages();
        
		Manifest mf = doManifest(); // Compute the manifest
        if (mf == null) {
            error("Cannot found input manifest");
            return;
        }
        writeManifest(mf);
	}
	
	public void manipulate() {
		for (Iterator iterator = m_components.iterator(); iterator.hasNext();) {
			ComponentInfo ci = (ComponentInfo) iterator.next();
			if (ci.m_classname != null)
				try {
					manipulate(ci);
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	
	public void manipulate(ComponentInfo ci) throws CoreException, IOException {
		IFile origF = outClasses.getFile(new Path(ci.m_classname));
		if (!origF.exists()) {
			error("The component " + ci.m_classname + " is declared but not in directory "+outClasses);
			return;
		}
			byte[] outClazz = manipulateComponent(readClass(origF), ci);
            if (outClazz != null && outClazz.length !=0) {
            	// not manipulate
            	m_classes.put(ci.m_classname, outClazz);
	
	           
	            // Manipulate inner classes ?
	            if (!ci.m_inners.isEmpty()) {
	                for (int k = 0; k < ci.m_inners.size(); k++) {
	                    String innerCN = (String) ci.m_inners.get(k)
	                            + ".class";
	                    InputStream innerStream = outClasses.getFile(new Path(innerCN)).getContents();
	                    manipulateInnerClass(innerStream, innerCN, ci);
	                }
	            }
            }
		
	}
	
	public void writeClasses(IProgressMonitor monitor) {
		// Write every manipulated file.
        Iterator it = m_classes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String classname = (String) entry.getKey();
            byte[] clazz = (byte[]) entry.getValue();
            writeClass(classname, clazz, monitor);
        }
	}
	
	
	public void writeClass(String classname, byte[] clazz, IProgressMonitor monitor)  {
		IFile ipojoF = this.outClasses.getFile(new Path(classname));
		
         try {
        	 //mkdirs(ipojoF.getParent(), monitor);
     		 ipojoF.setContents(new ByteArrayInputStream(clazz), true, false, monitor);
         } catch (CoreException e) {
        	 error("Cannot manipulate the file : cannot write class " +  classname + " !");
             return;
		}
	}
	
	private void mkdirs(IContainer parentC, IProgressMonitor monitor) throws CoreException {
		if (parentC instanceof IFolder && !parentC.exists()) {
			mkdirs(parentC.getParent(), monitor);
			((IFolder)parentC).create(true, true, monitor);
		}
	}


	public void manipulateIfNeed(IFile origF) {
		String nameclass = 
			origF.getFullPath().removeFirstSegments(outClasses.getFullPath().segmentCount()).toPortableString();
		for (Iterator iterator = m_components.iterator(); iterator.hasNext();) {
			ComponentInfo ci = (ComponentInfo) iterator.next();
			if (ci.m_classname != null && ci.m_classname.equals(nameclass)) {
				try {
					manipulate(ci);
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return;
			}
		}
		
	}
	
	
	


	byte[] readClass(IFile f) throws CoreException, IOException {
		 InputStream inputStream = f.getContents();
         byte [] bytes =  new byte[inputStream.available()];
         inputStream.read(bytes);
         inputStream.close();
         return bytes;
	}
	
	protected void writeManifest(Manifest mf) {
		if (m_manifest == null) {
            m_manifest = new File(m_dir, "META-INF/MANIFEST.MF");
            if (! m_manifest.exists()) {
                error("Cannot find the manifest file : " + m_manifest.getAbsolutePath());
                return;
            }
        } else {
            if (! m_manifest.exists()) {
                error("Cannot find the manifest file : " + m_manifest.getAbsolutePath());
                return;
            }
        }
        try {
        	ByteArrayOutputStream baos = new ByteArrayOutputStream();
            mf.write(baos);
    		System.out.println("ipojo-builder : write "+m_manifest);
    		OsgiManifest om  = new OsgiManifest(new ByteArrayInputStream(baos.toByteArray()));
    		om.save(m_manifest);
        } catch (IOException e) {
            error("Cannot write the manifest file : " + e.getMessage());
        } catch (BundleException e) {
            error("Cannot write the manifest file : " + e.getMessage());
        } catch (CoreException e) {
            error("Cannot write the manifest file : " + e.getMessage());
        }
	}
	
	
	
	public void setOutClasses(IContainer outClasses) {
		this.outClasses = outClasses;
	}


	public List<String> getComponentsClasses() {
		ArrayList<String> classes = new ArrayList<String>();
		for (Iterator iterator = m_components.iterator(); iterator.hasNext();) {
			ComponentInfo ci = (ComponentInfo) iterator.next();
			if (ci.m_classname != null) {
				classes.add(ci.m_classname);
			}
		}
		return classes;
	}
}
