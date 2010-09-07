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
 *
 * Copyright (C) 2006-2010 Adele Team/LIG/Grenoble University, France
 */
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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.apache.felix.ipojo.manipulation.InnerClassManipulator;
import org.apache.felix.ipojo.manipulation.Manipulator;
import org.apache.felix.ipojo.manipulation.annotations.MetadataCollector;
import org.apache.felix.ipojo.manipulator.Pojoization;
import org.apache.felix.ipojo.manipulator.QuotedTokenizer;
import org.apache.felix.ipojo.metadata.Attribute;
import org.apache.felix.ipojo.metadata.Element;
import org.apache.felix.ipojo.xml.parser.ParseException;
import org.apache.felix.ipojo.xml.parser.SchemaResolver;
import org.apache.felix.ipojo.xml.parser.XMLMetadataParser;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.objectweb.asm.ClassReader;
import org.osgi.framework.BundleException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

class EclipsePojoization extends Pojoization {

	IContainer outClasses;
	
	
	/**
     * Manipulate the input directory.
     * @throws CoreException 
     */
    protected void manipulateDirectory() {
        manipulateComponents(); // Manipulate classes
        m_referredPackages = getReferredPackages();
        Manifest mf = doManifest(); // Compute the manifest
        if (mf == null) {
            error("Cannot found input manifest");
            return;
        }

        // Write every manipulated file.
        Iterator it = m_classes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String classname = (String) entry.getKey();
            byte[] clazz = (byte[]) entry.getValue();
            if (clazz == null|| clazz.length == 0) continue;
            // The class name is already a path
            
            writeClass(classname, clazz);
        }

        // Write manifest
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
            mf.write(new FileOutputStream(m_manifest));
        } catch (IOException e) {
            error("Cannot write the manifest file : " + e.getMessage());
        }

    }
    
	IProgressMonitor monitor;
	
	protected void writeClass(String classname, byte[] clazz)  {
		IFile ipojoF = this.outClasses.getFile(new Path(classname));
		
         try {
        	 //mkdirs(ipojoF.getParent(), monitor);
     		 ipojoF.setContents(new ByteArrayInputStream(clazz), true, false, monitor);
         } catch (CoreException e) {
        	 error("Cannot manipulate the file : cannot write class " +  classname + " !");
             return;
		}
	}
	
	
	/**
     * Return a byte array that contains the bytecode of the given classname.
     * @param classname name of a class to be read
     * @return a byte array
     * @throws IOException if the classname cannot be read
     */
    protected byte[] getBytecode(final String classname) throws IOException {

    	IFile origF = outClasses.getFile(new Path(classname));
		if (!origF.exists()) {
			error("The component " + classname + " is declared but not in directory "+outClasses);
			throw new IOException();
		}
		InputStream inputStream = null;
		try{
			inputStream = origF.getContents();
			 byte [] bytes =  new byte[inputStream.available()];
			 if (bytes.length == 0)
		         	throw new IOException("Empty class");
	         inputStream.read(bytes);
	         return bytes;
		} catch (CoreException e) {
			throw new IOException(e.getMessage());
		} finally {
        	try {
				if (inputStream != null)
					inputStream.close();
			} catch (Exception e) {
			}
		}
    }
    
	public void setOutClasses(IContainer outClasses) {
		this.outClasses = outClasses;
	}

}

