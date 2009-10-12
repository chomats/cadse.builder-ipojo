package fr.imag.adele.cadse.builder.iPojo;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.eclipse.pde.core.build.IBuildEntry;
import org.eclipse.pde.internal.core.build.BuildModel;

public class FileBuildModel extends BuildModel {
	File			_underlyingResource;
	private String	_defaultSourceFolder;

	public FileBuildModel(String path) {
		_underlyingResource = new File(path);
	}

	/**
	 * test is the build model exists
	 * 
	 * @return true if the file where is stored the build model exists
	 */
	public boolean exists() {
		return _underlyingResource.exists();
	}

	@Override
	public void load() {
		if (_underlyingResource.exists()) {
			try {
				InputStream stream = new FileInputStream(_underlyingResource);
				load(stream, false);
				stream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
		}
	}

	@Override
	protected void updateTimeStamp() {
	}

	public String getInstallLocation() {
		return _underlyingResource.getAbsolutePath();
	}

	public boolean isInSync() {
		return true;
	}

	public boolean isEditable() {
		return false;
	}

	/**
	 * Returns an array of tokens of the build entry with the specified name.
	 * 
	 * @param name
	 *            name of the desired entry
	 * @return array of tokens, or <samp>null</samp> if not found.
	 */
	public String[] getTokensEntry(String name) {
		if (!exists()) {
			if (name.equals("source..")) {
				return new String[] { _defaultSourceFolder };
			}
			return null;
		}
		IBuildEntry e = getBuild().getEntry(name);
		if (e == null) {
			if (name.equals("source..")) {
				return new String[] { _defaultSourceFolder };
			}
			return null;
		}
		return e.getTokens();
	}

	public void setDefaultSourceFolder(String src) {
		_defaultSourceFolder = src;
	}

	
	public String getDefaultSourceFolder() {
		return _defaultSourceFolder;
	}
}
