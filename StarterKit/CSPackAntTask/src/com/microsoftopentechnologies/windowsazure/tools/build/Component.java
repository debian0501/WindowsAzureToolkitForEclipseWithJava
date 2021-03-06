/*
 Copyright 2013 Microsoft Open Technologies, Inc.
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.microsoftopentechnologies.windowsazure.tools.build;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.tools.ant.BuildException;

/**
 * 
 * A Class representing component element within workerrole element
 * 
 */
public class Component {
	private String importSrc;
	private String importAs;
	private ImportMethod importMethod = ImportMethod.NONE;
	private DeployMethod deployMethod = DeployMethod.NONE;
	private DeployMethod cloudMethod = DeployMethod.NONE;
	private String deployDir;
	private String cloudSrc;
	private String cloudKey;
	private CloudUpload cloudUpload = CloudUpload.NEVER;
	private WorkerRole role;
	
	/**
	 * Constructor
	 */
	public Component(WorkerRole role) {
		this.role = role;
	}

	/**
	 * Returns WorkerRole that contains this component
	 * @return
	 */
	public WorkerRole getRole() {
		return role;
	}
	
	/**
	 * Sets/gets the cloud upload setting
	 * @param cloudUpload
	 */
	public void setCloudUpload(String cloudUpload) {
		if(cloudUpload == null) {
			throw new BuildException("Missing cloud deploy setting");
		}
		
		if(cloudUpload.equalsIgnoreCase(CloudUpload.NEVER.toString())) {
			this.cloudUpload = CloudUpload.NEVER;
		} else if(cloudUpload.equalsIgnoreCase(CloudUpload.ALWAYS.toString())) {
			this.cloudUpload = CloudUpload.ALWAYS;
		} else if(cloudUpload.equalsIgnoreCase(CloudUpload.AUTO.toString())) {
			this.cloudUpload = CloudUpload.AUTO;
		} else {
			throw new BuildException("Unsupported cloud upload setting: " + cloudUpload);
		}
	}
	public CloudUpload getCloudUpload() {
		return this.cloudUpload;
	}
	
	/**
	 * Sets/gets the deployment method in the cloud if different from deploymethod
	 * @param cloudMethod
	 */
	public void setCloudMethod(String cloudMethod) {
		if (cloudMethod == null) {
			throw new BuildException("Missing cloud method");
		}

		if(cloudMethod.equalsIgnoreCase("exec")) {
			this.cloudMethod = DeployMethod.EXEC;
		} else if(cloudMethod.equalsIgnoreCase("copy")) {
			this.cloudMethod = DeployMethod.COPY;
		} else if(cloudMethod.equalsIgnoreCase("unzip")) {
			this.cloudMethod = DeployMethod.UNZIP;
		} else if(cloudMethod.equalsIgnoreCase("none")) {
			this.cloudMethod = DeployMethod.NONE;
		} else {
			throw new BuildException("Unsupported cloud method: " + cloudMethod);			
		}
	}
	public DeployMethod getCloudMethod() {
		if(this.cloudMethod == DeployMethod.NONE) {
			return this.deployMethod;
		} else {
			return this.cloudMethod;
		}
	}
	
	/**
	 * Sets/gets the storage account access key to use when deploying in the cloud if the download comes from a private blob
	 * @param key
	 */
	public void setCloudKey(String key) {
		this.cloudKey = key;
	}
	public String getCloudKey() {
		return this.cloudKey;
	}
	
	/**
	 * Sets/gets the URL to download component from when deploying in cloud (not local)
	 * @param url
	 */
	public void setCloudSrc(String url) {
		this.cloudSrc = url;
	}
	public URL getCloudSrc() {
		if(this.cloudSrc == null || 0==this.cloudSrc.compareToIgnoreCase("auto")) {
			return null;
		}
		try {
			URI uri = new URI(this.cloudSrc);
			return uri.toURL();
		} catch (URISyntaxException e) {
			throw new BuildException("Cloud source URL not valid: " + this.cloudSrc);
		} catch (MalformedURLException e) {
			throw new BuildException("Cloud source URL not valid: " + this.cloudSrc);
		}
	}
	
	/**
	 * Sets importas attribute
	 * 
	 * @param importAs
	 */
	public void setImportAs(String importAs) {
		this.importAs = importAs;
	}

	/**
	 * Returns the name of the file or directory that the component will be
	 * imported as, relative to approot
	 * 
	 * @return
	 */
	public String getImportAs() {
		if(this.importAs != null && !this.importAs.isEmpty()) {
			// If import destination has been specified, then return it; treat empty string as null
			return this.importAs;
		} else if(this.importMethod == ImportMethod.NONE) {
			// If no import method and no import destination, then return import source
			return this.importSrc;
		} else if(this.importMethod == ImportMethod.COPY) {
			// If import method is Copy and no import destination, then assume the import source's file name 
			File srcPath = new File(this.importSrc);
			return srcPath.getName();
		} else if(this.importMethod == ImportMethod.ZIP) {
			// If import method is Zip and no import destination, then assume the import souce's file name plus .zip
			File srcPath = new File(this.importSrc);
			return srcPath.getName() + ".zip";
		} else {
			return null;
		}
	}

	/**
	 * Gets the effective file name of the downloaded component
	 * @return
	 */
	public String getCloudDownloadAs() {
		
		final URI uri;
		try {
			if(null == (uri = this.getCloudSrc().toURI())) {
				return null;
			}
		} catch (URISyntaxException e) {
			return null;
		}

		final String path = uri.getPath().substring(1);
		final String[] pathParts = path.split("/");
		return pathParts[pathParts.length-1];
	}
	
	/**
	 * Sets deploymethod attribute
	 * 
	 * @param deployMethod
	 */
	public void setDeployMethod(String deployMethod) {
		if (deployMethod == null) {
			throw new BuildException("Missing deployment method");
		}

		if(deployMethod.equalsIgnoreCase("exec")) {
			this.deployMethod = DeployMethod.EXEC;
		} else if(deployMethod.equalsIgnoreCase("copy")) {
			this.deployMethod = DeployMethod.COPY;
		} else if(deployMethod.equalsIgnoreCase("unzip")) {
			this.deployMethod = DeployMethod.UNZIP;
		} else if(deployMethod.equalsIgnoreCase("none")) {
			this.deployMethod = DeployMethod.NONE;
		} else {
			throw new BuildException("Unsupported deployment method: " + deployMethod);			
		}
	}

	/**
	 * Gets deploymethod setting
	 * 
	 * @return
	 */
	public DeployMethod getDeployMethod() {
		return this.deployMethod;
	}

	/**
	 * Sets importmethod attribute
	 * 
	 * @param importMethod
	 */
	public void setImportMethod(String importMethod) {
		if (importMethod == null) {
			throw new BuildException("Missing import method");
		} else if(importMethod.equalsIgnoreCase("none")) {
			this.importMethod = ImportMethod.NONE;
		} else if(importMethod.equalsIgnoreCase("copy")) {
			this.importMethod = ImportMethod.COPY;
		} else if(importMethod.equalsIgnoreCase("zip")) {
			this.importMethod = ImportMethod.ZIP;
		} else if(importMethod.equalsIgnoreCase("auto")) {
			this.importMethod = ImportMethod.AUTO;
		} else {
			throw new BuildException("Unsupported import method: " + importMethod);
		}
	}

	/**
	 * Gets importmethod setting
	 * 
	 * @return
	 */
	public ImportMethod getImportMethod() {
		return this.importMethod;
	}

	/**
	 * Sets importsrc attribute
	 * 
	 * @param srcpath
	 */
	public void setImportSrc(String srcPath) {
		this.importSrc = srcPath;
	}

	/**
	 * Gets the importsrc attribute
	 * 
	 * @return
	 */
	public String getImportSrc() {
		return this.importSrc;
	}

	/**
	 * Sets deployto attribute
	 * 
	 * @param srcpath
	 */
	public void setDeployDir(String deployPath) {
		if(deployPath.isEmpty())
			deployPath = null;
		this.deployDir = deployPath;
	}

	/**
	 * Gets the deployto attribute
	 * 
	 * @return
	 */
	public String getDeployDir() {
		if(this.deployDir != null && !this.deployDir.isEmpty()) {
			return this.deployDir;
		} else {
			return "."; // Return current directory by default
		}
	}
	
	/** Allows the use of an arbitrary type attribute setting on a component by external tools, but the Ant extension currently 
	 * has no functionality associated with it
	 * 
	 * @param type
	 */
	public void setType(String type) {
		return;
	}

	/**
	 * Returns the container name based on the URL, if this is a private Windows Azure Blob 
	 * @return
	 */
	private String getCloudContainer() {
		final URL url;
		final URI uri;
		final String path;
		final String[] pathParts;
		
		try {
			if(null == (url = this.getCloudSrc()) || this.getCloudKey() == null) {
				return null;
			} else if(null == (uri = url.toURI())) {
				return null;
			} else if(null == (path = uri.getPath()) || path.isEmpty()) {
				return null;
			} else if(null == (pathParts = path.substring(1).split("/"))) {
				return null;
			} else if(pathParts.length < 1) {
				return null;
			} else {
				return pathParts[0];
			}
		} catch (URISyntaxException e) {
			return null;
		}
	}
	
	/**
	 * Returns the blob name based on the URL, if this isa private Windows Azure Blob
	 * @return
	 */
	private String getCloudBlob() {
		final URL url;
		final URI uri;
		final String path, containerName;
		
		try {
			if(null == (url = this.getCloudSrc()) || this.getCloudKey() == null) {
				return null;
			} else if(null == (uri = url.toURI())) {
				return null;
			} else if(null == (path = uri.getPath()) || path.isEmpty()) {
				return null;
			} else if(null == (containerName = getCloudContainer())) {
				return null;
			} else {
				return path.substring(containerName.length()+2);
			}
		} catch (URISyntaxException e) {
			return null;
		}
	}
	
	/** Returns the name of the storage account based on the URL, if this is a private Windows Azure Blob
	 * @return
	 */
	private String getCloudStorage() {
		final URL url;
		final String hostName;
		final String[] hostNameParts;
		
		if(null == (url = this.getCloudSrc()) || this.getCloudKey() == null) {
			return null;
		} else if(null == (hostName = url.getHost())) {
			return null;
		} else if(null == (hostNameParts = hostName.split("\\."))) {
			return null;
		} else if(hostNameParts.length < 1) {
			return null;
		} else {
			return hostNameParts[0];
		}
	}
	
	/** Returns the base URL of the storage service based on the URL, if this is a private Windows Azure Blob
	 * @return
	 */
	private String getCloudStorageEndpoint() {
		final URL url;
		final String hostName;
		final String[] hostNameParts;
		if(null == (url = this.getCloudSrc()) || this.getCloudKey() == null) {
			return null;
		} else if(null == (hostName = url.getHost())) {
			return null;
		} else if(null == (hostNameParts = hostName.split("\\."))) {
			return null;
		} else if(hostNameParts.length < 4) {
			return null;
		} else {
			StringBuilder baseURL = new StringBuilder();
			for(int i=2; i<hostNameParts.length; i++) {
				baseURL.append(hostNameParts[i]);
				if(i<hostNameParts.length-1) {
					baseURL.append('.');
				}
			}
			return url.getProtocol() + "://" + baseURL.toString();
		}		
	}
	
	/**
	 * Verifies whether the download indicated by cloudsrc exists
	 * @return
	 */
	private void verifyDownloadPublic() {
		final WindowsAzurePackage waPackage = role.getPackage();
		final URL cloudSrc = getCloudSrc();
		if(!waPackage.getVerifyDownloads()) {
			return;
		}
		
		waPackage.log("Verifying download availability (" + cloudSrc.toExternalForm() + ")...");
		
		try {
			final HttpURLConnection connection = (HttpURLConnection) this.getCloudSrc().openConnection();
			connection.setRequestMethod("HEAD");
			if(200 != connection.getResponseCode()) {
				waPackage.log("warning: Failed to confirm download availability! Make sure the URL is correct (" + cloudSrc.toExternalForm() + ").", 1);
			}
			
		} catch (IOException e) {
			waPackage.log("warning: Failed to confirm download availability! Make sure the URL is correct (" + cloudSrc.toExternalForm() + ").", 1);							
		}
	}
	
	/** Verifies whether the blob pointed to by cloudSrc actually exists
	 * @return
	 */
	private void ensurePrivateDownload(WindowsAzureManager waManager) {
		final WindowsAzurePackage waPackage = role.getPackage();
		final URL cloudSrc = getCloudSrc();
		String containerName, blobName, storageName;
		final boolean isNetworkAvailable;
		
		if(!waPackage.getVerifyDownloads() && getCloudUpload() == CloudUpload.NEVER) {
			return; // Nothing to do because download verification and upload are off
		} else if(!(isNetworkAvailable = WindowsAzurePackage.isNetworkAvailable()) && getCloudUpload() == CloudUpload.NEVER) {
			return; // Nothing to do because network not available and no upload requested
		} else if(!isNetworkAvailable) {
			waPackage.log("warning: Failed to ensure blob availability (" + cloudSrc.toExternalForm() + ") because there is no network connectivity", 1);
			return;
		}
		
		waPackage.log("Verifying blob availability (" + cloudSrc.toExternalForm() + ")...");
		
		if(null == waManager) {
			waPackage.log("warning: Failed to verify blob availability (" + cloudSrc.toExternalForm() + ") due to an internal error", 1);
		} else if(null == (containerName = getCloudContainer()) || null == (blobName = getCloudBlob()) || null == (storageName = getCloudStorage())) {
			waPackage.log("warning: Failed to verify blob availability (" + cloudSrc.toExternalForm() + ") because the URL does not appear to point to a Windows Azure blob", 1);
		} else if(null == waManager.execute(new WindowsAzureManager.Commands.Container.Create(
				containerName, 
				storageName, 
				this.getCloudKey(), 
				this.getCloudStorageEndpoint()))) {
			waPackage.log("warning: Failed to ensure the blob's availability because the specified container could not be created (" + containerName + ")", 1);
		} else if(getCloudUpload() == CloudUpload.ALWAYS) {
			uploadBlob(waManager, blobName, containerName, storageName, this.getCloudKey());
		} else if(null != waManager.execute(new WindowsAzureManager.Commands.Blob.Use(
				blobName, 
				containerName, 
				storageName, 
				this.getCloudKey(), 
				this.getCloudStorageEndpoint()))) {
			; // Blob existence confirmed, nothing else to do
		} else if(getCloudUpload() == CloudUpload.NEVER) {
			waPackage.log("warning: Failed to verify blob availability! Make sure the URL and/or the access key is correct (" + cloudSrc.toExternalForm() + ")", 1);
		} else if(getCloudUpload() == CloudUpload.AUTO) {
			uploadBlob(waManager, blobName, containerName, storageName, this.getCloudKey());
		} 
	}
	

	private void uploadBlob(WindowsAzureManager waManager, String blobName, String containerName, String storageName, String accessKey) {
		final WindowsAzurePackage waPackage = role.getPackage();
		final File approotDir = role.getAppRootDir();
		File srcFile;
		
		// Figure out where to get the source component, the source location or approot
		if(getImportSrc() != null) {
			srcFile = new File(getImportSrc());
		} else if(getImportAs() != null) {
			srcFile = new File(getImportAs());
		} else {
			waPackage.log("warning: Skipping a component that cannot be uploaded to blob storage automatically", 1);
			return;
		}
		
		if (!srcFile.isAbsolute()) {
			srcFile = new File(approotDir, srcFile.getPath());
		}

		// If directory then zip it
		final File uploadedFile;
		if (!srcFile.exists()) {
			throw new BuildException(String.format("Failed to find component \"%s\"", srcFile.getPath()));
		} else if(srcFile.isDirectory()) {
			// Put the zipped file to upload in deploy directory
			uploadedFile = new File(waPackage.getPackageDir(), getCloudDownloadAs());
			waPackage.zipFile(srcFile, uploadedFile);
		} else {
			// File to upload is the existing source file
			uploadedFile = srcFile;
		}
			
		// Upload
		waPackage.log("Please wait for blob upload to complete (" + this.getCloudSrc() + ")...");
		if(waManager == null) {
			return;
		} else if(null == (waManager.execute(new WindowsAzureManager.Commands.Blob.Upload(
				uploadedFile.getAbsolutePath(), 
				blobName, 
				containerName, 
				storageName, 
				this.getCloudKey(), 
				this.getCloudStorageEndpoint())))) {
			waPackage.log("warning: Failed to upload blob " + this.getCloudSrc() + ". The deployment might not work correctly in the cloud", 1);
		} else {
			waPackage.log("Uploaded blob " + this.getCloudSrc());			
		}
	}
	
	/**
	 * Returns the component deployment commandline
	 * @param destFile
	 * @param deployMethod
	 * @param deployPath
	 * @return
	 */
	public String createComponentDeployCommandLine() {
		final String importedPath;
		String cmdLineTemplate; 
		final String deployPath = getDeployDir();
		final DeployMethod method;
		final WindowsAzurePackage wapackage = role.getPackage();
		
		// If building for the cloud, let cloudmethod override deploymethod if specified
		if(wapackage.getPackageType() == PackageType.local) {
			method = getDeployMethod();
			importedPath = getImportAs();	
		} else {
			method = getCloudMethod();
			if(getCloudSrc() == null) {
				importedPath = getImportAs();					
			} else {
				importedPath = getCloudDownloadAs();
			}
		} 
		
		final File destFile = new File(importedPath);

		switch(method)
		{
			case COPY:
				// Support for deploy method: copy - ensuring non-existent target directories get created as needed
				cmdLineTemplate = "if not \"%SERVER_APPS_LOCATION%\" == \"\\%ROLENAME%\" if exist \"$destName\"\\* (echo d | xcopy /y /e /q \"$destName\" \"$deployPath\\$destName\" 1>nul) else (echo f | xcopy /y /q \"$destName\" \"$deployPath\\$destName\" 1>nul)";
				return cmdLineTemplate
						.replace("$destName", destFile.getName())
						.replace("$deployPath", deployPath);
			case UNZIP:
				// Support for deploy method: unzip return
				cmdLineTemplate = "cscript /NoLogo $utilSubdir\\$unzipFilename \"$destName\" $deployPath";
				//cmdLineTemplate = "cmd /c $utilSubdir\\$unzipFilename file unzip \"$destName\" $deployPath"; // TODO
				cmdLineTemplate = cmdLineTemplate
						.replace("$utilSubdir", WindowsAzurePackage.DEFAULT_UTIL_SUBDIR)
						.replace("$unzipFilename", WindowsAzurePackage.UTIL_UNZIP_FILENAME)
						//.replace("$unzipFilename", WindowsAzurePackage.UTIL_WASH_FILENAME) //TODO
						.replace("$destName", destFile.getName())
						.replace("$deployPath", deployPath);
				
				// Delete the ZIP to make room, but only if downloaded
				if(null != getCloudSrc()) {
					cmdLineTemplate += System.getProperty("line.separator") + "del /Q /F \"" + destFile.getName() + "\"";
				}
				
				return cmdLineTemplate;
				
			case EXEC:
				// Support for deploy method: exec
				StringBuilder s = new StringBuilder("start \"Windows Azure\" ");
				
				// If deploy dir specified, treat it as a change directory request
				if(deployPath != null) {
					s.append("/D\"");
					s.append(deployPath);
					s.append("\" ");
				}
				s.append(importedPath);
				return s.toString();
			case NONE:
				// Ignore if deploymethod is NONE
				return null;
			default:
				throw new BuildException("Unsupported deployment method");
		}
	}
	
	/**
	 * Returns command line for downloading from public location
	 * @return
	 */
	private String createPublicDownloadCommandLine() {
		if(getCloudSrc() == null) {
			return null;
		} 

		final String cmd = String.format("cmd /c %s%s%s file download \"%s\" \"%s\"", 
				WindowsAzurePackage.DEFAULT_UTIL_SUBDIR, 
				File.separator, 
				WindowsAzurePackage.UTIL_WASH_FILENAME, 
				getCloudSrc(),
				this.getCloudDownloadAs());
		return cmd;
	}
	
	/**
	 * Returns command line for downloading from private blob
	 * @return
	 */
	private String createBlobDownloadCommandLine() {
		if(getCloudSrc() == null || getCloudKey() == null) {
			return null;
		}
		
		try {
			// Extract storage account, container, blob and file names
			final String storeName = getCloudSrc().getHost().split("\\.")[0];
			final URI uri = getCloudSrc().toURI();
			final String path = uri.getPath().substring(1);
			final String[] pathParts = path.split("/");
			final String containerName = pathParts[0];
			final String blobName = path.substring(containerName.length()+1);
			final String fileName = this.getCloudDownloadAs();
			if(pathParts.length < 2 || containerName.isEmpty() || blobName.isEmpty() || storeName.isEmpty() || fileName.isEmpty()) {
				throw new BuildException("\tNot a valid blob URL: " + getCloudSrc().toExternalForm());
			} 

			return String.format("cmd /c %s%s%s blob download \"%s\" \"%s\" %s %s \"%s\" \"%s\"", 
					WindowsAzurePackage.DEFAULT_UTIL_SUBDIR, 
					File.separator, 
					WindowsAzurePackage.UTIL_WASH_FILENAME, 
					fileName, 
					blobName, 
					containerName, 
					storeName, 
					getCloudKey(),
					getCloudStorageEndpoint());
			
		} catch (URISyntaxException e) {
			throw new BuildException("\tNot valid component URL: " + getCloudSrc().toExternalForm());
		}
	}

	/**
	 * Returns component download command line
	 * @return
	 */
	public String createDownloadCommandLine() {
		if(role.getPackage().getPackageType() == PackageType.local || getCloudSrc() == null) {
			// Nothing to download if building for emulator or no cloudsrc URL
			return null;
		
		} else if(getCloudKey() == null) {
			// Download from public location
			return createPublicDownloadCommandLine();

		} else {
			// Download from private blob
			return createBlobDownloadCommandLine();
		}
	}

	/**
	 * Ensure availability of the download
	 * @return
	 */
	public void ensureDownload(WindowsAzureManager waManager) {
		final URL cloudSrc = getCloudSrc();
		
		if(cloudSrc == null) {
			return;
		} else if(this.getCloudKey() == null) {
			// Verify public download
			verifyDownloadPublic();
		} else {
			// Ensure private download
			ensurePrivateDownload(waManager);
		}
	}

	
	/**
	 * Validates component import settings
	 */
	public void verifyImportSettings() {
		// Validate parameters
		if (getImportSrc() == null && getImportMethod() != ImportMethod.NONE) {
			// Missing import source
			throw new BuildException("Missing import source");
		} else if (getImportAs() == null) {
			// Missing importAs name
			throw new BuildException(String.format("Missing import destination for component '%s'", getImportSrc()));
		} else if (getImportMethod() == null) {
			// Missing import method
			throw new BuildException(String.format("Missing import method for component '%s'", getImportAs()));
		}
	}

	/**
	 * Ensures the component's deployment settings are ok and it is ready to be deployed
	 */
	public void verifyDeploySettings() {
		if (role.getAppRootDir() == null) {
			throw new BuildException("Missing component or approot due to an unknown internal error");
		}

		final WindowsAzurePackage wapackage = role.getPackage();
		
		// Determine deploy method depending on cloud vs emulator
		final DeployMethod deployMethod;
		if(wapackage.getPackageType() == PackageType.local) {
			deployMethod = getDeployMethod();
		} else { 
			deployMethod = getCloudMethod();
		}
		
		final ImportMethod importMethod = getImportMethod();
		final File deployFile = new File(role.getAppRootDir(), getImportAs());

		// Ensure default value for deploy method
		if (importMethod == ImportMethod.ZIP && deployMethod == DeployMethod.EXEC) {
			// It doesn't make sense to call exec on a zip
			throw new BuildException(String.format("Deployment method '%s' cannot be used with the import method '%s' for component '%s'", deployMethod.toString().toLowerCase(), importMethod.toString().toLowerCase(), deployFile));

		} else if (deployMethod == null) {
			// Missing deploy method is a problem
			throw new BuildException(String.format("Missing deployment method for component '%s'", deployFile));

		} else if (!deployFile.exists() && deployMethod != DeployMethod.EXEC && (wapackage.getPackageType() == PackageType.local || getCloudSrc() == null)) {
			// Validate that deployment already exists in approot, unless its deployment method is EXEC, in which case skip this check, since it could be an arbitrary commandline
			throw new BuildException(String.format("Cannot find component '%s'", deployFile));

		} else if (getDeployDir() == null && (deployMethod == DeployMethod.COPY || deployMethod ==  DeployMethod.UNZIP)) {
			// Missing deploy directory for COPY or UNZIP (not required for EXEC and NONE)
			throw new BuildException(String.format("Missing deployment directory for component '%s'", getImportAs()));
		}
	}
	
	/**
	 * Verifies component import into the approot
	 * @param component
	 * @param approot
	 */
	public void verifyImportSucceeded() {
		final WindowsAzurePackage wapackage = role.getPackage();
		if (role.getAppRootDir() == null) {
			throw new BuildException("Internal failure for unknown reason");
		} else if (getImportAs() == null) {
			wapackage.log(String.format("\tNothing to import for component '%s'", getImportSrc()));
		} else if(getCloudSrc() != null && wapackage.getPackageType() == PackageType.cloud) {
			// Don't verify component when building for the cloud if it has a cloud deployment url specified
			wapackage.log(String.format("\tNot importing component '%s' because it will be downloaded during deployment in the cloud", getImportAs()));
		} else if (getImportMethod() != ImportMethod.NONE) {
			// Confirm that the file actually got imported into the approot, unless import method is NONE
			String fileName = getImportAs();
			
			// Strip out command line parameters if any, but for deploymethod=EXEC only
			if(fileName != null && getDeployMethod() == DeployMethod.EXEC) {
				fileName = fileName.split(" ")[0]; 
			}
			
			final File destFile = new File(role.getAppRootDir(), fileName);
			if (destFile.exists()) {
				wapackage.log(String.format("\tImported as '%s' from \"%s\"", fileName, getImportSrc()));
			} else {
				throw new BuildException(String.format("Failed importing component '%s' as '%s' into 'approot\\%s'", getImportSrc(), getImportMethod(), fileName));
			}
		}
	}
	
	/**
	 * Imports a component into the role's approot
	 * @param component
	 * @param approotDir
	 */
	public void doImport() {
		final File approotDir = role.getAppRootDir();
		final WindowsAzurePackage wapackage = role.getPackage();
		
		// Ignore no import method
		final ImportMethod importMethod = getImportMethod();
		if(importMethod == ImportMethod.NONE) {
			return;
		}

		String fileName = getImportAs();

        // Strip out command line parameters if any, but only for deploymethod=EXEC
		if(getDeployMethod() == DeployMethod.EXEC && fileName != null) {
			fileName = fileName.split(" ")[0];
		}
        
		final File destFile = new File(approotDir, fileName);
        
		// When building for the cloud if cloud source is specified, delete the component if it exists and import method isn't none; and don't import
		if(getCloudSrc() != null && wapackage.getPackageType() == PackageType.cloud && getImportMethod() != ImportMethod.NONE) {
			if(destFile.exists()) {
				WindowsAzurePackage.deleteDirectory(destFile);
			}
			return;
		}
		
		File srcFile = new File(getImportSrc());

		// If relative path, make it relative to approot
		if (!srcFile.isAbsolute()) {
			srcFile = new File(approotDir, srcFile.getPath());
		}

		if (!srcFile.exists())
			throw new BuildException(String.format("Failed to find component \"%s\"", srcFile.getPath()));

		if (importMethod == ImportMethod.COPY) {
			// Component import method: copy
			wapackage.copyFile(srcFile, destFile);

		} else if (importMethod == ImportMethod.ZIP) {
			// Component import method: zip
			wapackage.zipFile(srcFile, destFile);
		}
	}
}