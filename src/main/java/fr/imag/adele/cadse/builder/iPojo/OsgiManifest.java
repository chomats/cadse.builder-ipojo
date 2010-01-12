package fr.imag.adele.cadse.builder.iPojo;

import java.io.IOException;
import java.util.Enumeration;

import org.eclipse.osgi.util.ManifestElement;
import org.osgi.framework.BundleException;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.core.internal.utils.FileUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.osgi.framework.util.Headers;
import org.eclipse.osgi.util.ManifestElement;
import org.osgi.framework.BundleException;



class OsgiManifestElement extends ManifestElement {

	
	
	/* (non-Javadoc)
	 * @see org.eclipse.osgi.util.ManifestElement#addAttribute(java.lang.String, java.lang.String)
	 */
	@Override
	public void addAttribute(String key, String value) {
		super.addAttribute(key, value);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.osgi.util.ManifestElement#addDirective(java.lang.String, java.lang.String)
	 */
	@Override
	public void addDirective(String key, String value) {
		super.addDirective(key, value);
	}
	
	/**
	 * Attribute.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * 
	 * @return the osgi manifest element
	 */
	public OsgiManifestElement attribute(String key, String value) {
		super.addAttribute(key, value);
		return this;
	}
	
	/**
	 * Directive.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * 
	 * @return the osgi manifest element
	 */
	public OsgiManifestElement directive(String key, String value) {
		super.addDirective(key, value);
		return this;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return toString(this);
	}
	
	/**
	 * To string.
	 * 
	 * @param me
	 *            the me
	 * 
	 * @return the string
	 */
	public static String toString(ManifestElement me)  {
		StringBuilder sb = new StringBuilder();
		toString(sb,me);
		return sb.toString();
	}
		
	/**
	 * To string.
	 * 
	 * @param sb
	 *            the sb
	 * @param me
	 *            the me
	 */
	public static void toString(Appendable sb ,ManifestElement me)  {
		try {
			String[] vc = me.getValueComponents();
			char c = 0;
			for (String v : vc) {
				if (c != 0) sb.append(c);
				sb.append(v);
				c = ';';
			}
			c = 0;
			Enumeration ak = me.getKeys();
			if (ak != null) {
				while (ak.hasMoreElements()) {
					String k = (String) ak.nextElement();
					String[] vs = me.getAttributes(k);
					for (String v : vs) {
						sb.append(';').append(k).append("=\"").append(v).append("\""); //$NON-NLS-1$
					}
					
				}
			}
			Enumeration dk = me.getDirectiveKeys();
			if (dk != null) {
				while (dk.hasMoreElements()) {
					String k = (String) dk.nextElement();
					String[] vs = me.getDirectives(k);
					for (String v : vs) {
						sb.append(";\n  ").append(k).append(":=\"");
						//sb.append(v);
						String[] vv = v.split(",");
						boolean newLine = false;
						for (String vinvv : vv) {
							if (newLine) {
								sb.append(",\n   ");
							}
							sb.append(vinvv.trim());
							
							newLine = true;
						};
						sb.append("\""); //$NON-NLS-1$
					}
					
				}
			}
			
		} catch (IOException e) {
			// ignored
		}
		
	}
	
}

/**
 * Cette classe permet de lire et d'ecrire un manifest OSGI.
 * 
 * @author chomats
 */
public class OsgiManifest {

	/** The Constant ECLIPSE_LAZY_START. */
	public static final String	ECLIPSE_LAZY_START					= "Eclipse-LazyStart";

	/** Bundle manifest header constants from the OSGi R4 framework constants. */
	public static final String	BUNDLE_CATEGORY						= "Bundle-Category";

	/** The Constant BUNDLE_CLASSPATH. */
	public static final String	BUNDLE_CLASSPATH					= "Bundle-ClassPath";

	/** The Constant BUNDLE_COPYRIGHT. */
	public static final String	BUNDLE_COPYRIGHT					= "Bundle-Copyright";

	/** The Constant BUNDLE_DESCRIPTION. */
	public static final String	BUNDLE_DESCRIPTION					= "Bundle-Description";

	/** The Constant BUNDLE_NAME. */
	public static final String	BUNDLE_NAME							= "Bundle-Name";

	/** The Constant BUNDLE_NATIVECODE. */
	public static final String	BUNDLE_NATIVECODE					= "Bundle-NativeCode";

	/** The Constant EXPORT_PACKAGE. */
	public static final String	EXPORT_PACKAGE						= "Export-Package";

	/** The Constant EXPORT_SERVICE. */
	public static final String	EXPORT_SERVICE						= "Export-Service";

	/** The Constant IMPORT_PACKAGE. */
	public static final String	IMPORT_PACKAGE						= "Import-Package";

	/** The Constant DYNAMICIMPORT_PACKAGE. */
	public static final String	DYNAMICIMPORT_PACKAGE				= "DynamicImport-Package";

	/** The Constant IMPORT_SERVICE. */
	public static final String	IMPORT_SERVICE						= "Import-Service";

	/** The Constant BUNDLE_VENDOR. */
	public static final String	BUNDLE_VENDOR						= "Bundle-Vendor";

	/** The Constant BUNDLE_VERSION. */
	public static final String	BUNDLE_VERSION						= "Bundle-Version";

	/** The Constant BUNDLE_DOCURL. */
	public static final String	BUNDLE_DOCURL						= "Bundle-DocURL";

	/** The Constant BUNDLE_CONTACTADDRESS. */
	public static final String	BUNDLE_CONTACTADDRESS				= "Bundle-ContactAddress";

	/** The Constant BUNDLE_ACTIVATOR. */
	public static final String	BUNDLE_ACTIVATOR					= "Bundle-Activator";

	/** The Constant BUNDLE_UPDATELOCATION. */
	public static final String	BUNDLE_UPDATELOCATION				= "Bundle-UpdateLocation";

	/** The Constant BUNDLE_REQUIREDEXECUTIONENVIRONMENT. */
	public static final String	BUNDLE_REQUIREDEXECUTIONENVIRONMENT	= "Bundle-RequiredExecutionEnvironment";

	/** The Constant BUNDLE_SYMBOLICNAME. */
	public static final String	BUNDLE_SYMBOLICNAME					= "Bundle-SymbolicName";

	/** The Constant BUNDLE_LOCALIZATION. */
	public static final String	BUNDLE_LOCALIZATION					= "Bundle-Localization";

	/** The Constant REQUIRE_BUNDLE. */
	public static final String	REQUIRE_BUNDLE						= "Require-Bundle";

	/** The Constant FRAGMENT_HOST. */
	public static final String	FRAGMENT_HOST						= "Fragment-Host";

	/** The Constant BUNDLE_MANIFESTVERSION. */
	public static final String	BUNDLE_MANIFESTVERSION				= "Bundle-ManifestVersion";

	/** The Constant BUNDLE_URL. */
	public static final String	BUNDLE_URL							= "Bundle-URL";

	/** The Constant BUNDLE_SOURCE. */
	public static final String	BUNDLE_SOURCE						= "Bundle-Source";

	/** The Constant BUNDLE_DATE. */
	public static final String	BUNDLE_DATE							= "Bundle-Date";

	/** The Constant METADATA_LOCATION. */
	public static final String	METADATA_LOCATION					= "Metadata-Location";

	/** The Constant SERVICE_COMPONENT. */
	public static final String	SERVICE_COMPONENT					= "Service-Component";

	/** The Constant MANIFEST_VERSION. */
	public static final String	MANIFEST_VERSION					= "Manifest-Version";

	/** The mf. */
	protected Headers			mf;

	/** The write. */
	protected Appendable		write;

	/**
	 * Instantiates a new osgi manifest.
	 * 
	 * @param p
	 *            the p
	 * 
	 * @throws BundleException
	 *             the bundle exception
	 */
	public OsgiManifest(IProject p) throws BundleException {
		IFile manifest = p.getFile(new Path("META-INF/MANIFEST.MF"));
		mf = null;
		if (manifest.exists()) {
			try {
				mf = Headers.parseManifest(new FileInputStream(manifest.getLocation().toFile()));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (BundleException e) {
				e.printStackTrace();
			}
		}

		/*
		 * 
		 */
		if (mf == null) {
			mf = new Headers(20);
		} else {
			mf = new Headers(mf); // because mf is read only, il faut le
			// dupliquer
		}
	}

	/**
	 * It's readOnly.
	 * 
	 * @param manifest
	 *            the manifest
	 * 
	 * @throws BundleException
	 *             the bundle exception
	 */
	public OsgiManifest(InputStream manifest) throws BundleException {
		mf = Headers.parseManifest(manifest);
	}

	/**
	 * Construct a default manifest.
	 */
	public OsgiManifest() {
		mf = new Headers(20);
	}

	/**
	 * Instantiates a new osgi manifest.
	 * 
	 * @param m
	 *            the m
	 */
	public OsgiManifest(OsgiManifest m) {
		mf = new Headers(m.mf);
	}

	/**
	 * Get the manifest value, null if no value.
	 * 
	 * @param key
	 *            the manifest key
	 * 
	 * @return the manifest value
	 * 
	 * @throws BundleException
	 *             the bundle exception
	 */
	public ManifestElement getAttribute(String key) throws BundleException {
		String v = (String) mf.get(key);
		if (v == null || v.length() == 0) {
			return null;
		}
		ManifestElement[] e = ManifestElement.parseHeader(key, v);
		if (e != null && e.length == 1) {
			return e[0];
		}
		return null;
	}

	/**
	 * Gets the attribute string.
	 * 
	 * @param key
	 *            the key
	 * 
	 * @return the attribute string
	 * 
	 * @throws BundleException
	 *             the bundle exception
	 */
	public String getAttributeString(String key) throws BundleException {
		String v = (String) mf.get(key);
		if (v == null || v.length() == 0) {
			return null;
		}
		ManifestElement[] e = ManifestElement.parseHeader(key, v);
		if (e != null && e.length == 1) {
			return e[0].getValue();
		}
		return null;
	}

	/**
	 * Return an array of manifest element for an entry 'key'. Return null if
	 * the value is null or empty
	 * 
	 * @param key
	 *            the property
	 * 
	 * @return the value for key or null
	 * 
	 * @throws BundleException
	 *             if the header value is invalid
	 */
	public ManifestElement[] getAttributes(String key) throws BundleException {
		String v = (String) mf.get(key);
		if (v == null || v.length() == 0) {
			return null;
		}
		ManifestElement[] e = ManifestElement.parseHeader(key, v);
		return e;
	}


	public void save(File f) throws IOException, BundleException, CoreException {
		StringBuilder sb = new StringBuilder();
		write(sb);
		
		FileUtil.transferStreams(new ByteArrayInputStream(sb.toString().getBytes()), 
				new FileOutputStream(f), f.getAbsolutePath(), null);
	}

	/**
	 * Write a manifest.
	 * 
	 * @param a
	 *            the a
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws BundleException
	 *             the bundle exception
	 */
	public void write(Appendable a) throws IOException, BundleException {
		this.write = a;
		w(MANIFEST_VERSION);
		w(BUNDLE_MANIFESTVERSION);
		w(BUNDLE_NAME);
		w(BUNDLE_SYMBOLICNAME);
		w(BUNDLE_VENDOR);
		w(BUNDLE_ACTIVATOR);
		w(BUNDLE_VERSION);
		w(BUNDLE_LOCALIZATION);
		w(BUNDLE_CLASSPATH);
		w(BUNDLE_DESCRIPTION);
		w(BUNDLE_DOCURL);
		w(ECLIPSE_LAZY_START);
		mw(EXPORT_PACKAGE);
		mw(IMPORT_PACKAGE);
		mw(REQUIRE_BUNDLE);
		wo(MANIFEST_VERSION, BUNDLE_MANIFESTVERSION, BUNDLE_MANIFESTVERSION, BUNDLE_NAME, BUNDLE_SYMBOLICNAME,
				BUNDLE_ACTIVATOR, BUNDLE_VERSION, BUNDLE_LOCALIZATION, BUNDLE_CLASSPATH, ECLIPSE_LAZY_START,
				EXPORT_PACKAGE, IMPORT_PACKAGE, REQUIRE_BUNDLE, BUNDLE_VENDOR, BUNDLE_DESCRIPTION, BUNDLE_DOCURL);
		write.append("\n");
	}

	/**
	 * Write a key if need.
	 * 
	 * @param key
	 *            a key to write in manifest
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void w(String key) throws IOException {
		String value = (String) mf.get(key);
		if (value == null || value.length() == 0) {
			return;
		}
		write.append(key).append(": ").append(value).append("\n");
	}

	/**
	 * Write a key of type list if need.
	 * 
	 * @param key
	 *            the key
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws BundleException
	 *             the bundle exception
	 */
	public void mw(String key) throws IOException, BundleException {
		String value = (String) mf.get(key);
		if (value == null || value.length() == 0) {
			return;
		}
		ManifestElement[] elements = ManifestElement.parseHeader(key, value);
		Arrays.sort(elements, new Comparator<ManifestElement>() {

			public int compare(ManifestElement me1, ManifestElement me2) {
				return me1.getValue().compareTo(me2.getValue());
			}
		});
		write.append(key).append(":");
		for (int i = 0; i < elements.length; i++) {
			if (i != 0) {
				write.append(",\n");
			}

			write.append(" ");
			OsgiManifestElement.toString(write, elements[i]);
		}
		write.append("\n");
	}

	/**
	 * Write others key.
	 * 
	 * @param keys
	 *            an array of keys allready writen and must be excluded
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void wo(String... keys) throws IOException {
		HashSet<String> hkeys = new HashSet<String>(Arrays.asList(keys));
		TreeSet<String> keysToWrite = new TreeSet<String>();
		Enumeration<String> mfkeys = mf.keys();
		while (mfkeys.hasMoreElements()) {
			String k = mfkeys.nextElement();
			if (hkeys.contains(k)) {
				continue;
			}
			keysToWrite.add(k);
		}
		for (String k : keysToWrite) {
			String value = (String) mf.get(k);
			if (value == null | value.length() == 0) {
				continue;
			}
			write.append(k).append(": ");
			writeBlock(k, value);
			write.append("\n");
		}
	}

	/** used in wo
	 * 
	 * @param key the key
	 * @param value the value
	 * @throws IOException
	 */
	private void writeBlock(String key, String value) throws IOException {
		int line = 70 - 1 - key.length();
		int pos = 0;
		while (pos < value.length()) {
			int l = Math.min(line, value.length() - pos);

			if (pos != 0) {
				write.append("\n ");
			} else {
				line = 70;
			}

			write.append(value.substring(pos, pos + l));
			pos = pos + l;
		}
	}

}