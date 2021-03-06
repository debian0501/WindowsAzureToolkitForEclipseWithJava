/**
 * Copyright 2013 Persistent Systems Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	 http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.persistent.util;

import java.io.File;
import java.util.Arrays;

import waeclipseplugin.Activator;

import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.microsoftopentechnologies.wacommon.storageregistry.StorageRegistryUtilMethods;
import com.persistent.winazureroles.Messages;
/**
 * Class has utility methods
 * required for UI components listeners of
 * JDK, Server and Application tabs
 * for windows azure deployment project creation wizard
 * and server configuration property page.
 */
public class JdkSrvConfigListener extends JdkSrvConfig {
	/**
	 * Method is used when JDK check box is checked.
	 * @return
	 */
	public static String jdkChkBoxChecked() {
		// Pre-populate with auto-discovered JDK if any
		String jdkDefaultDir =
				WAEclipseHelper.jdkDefaultDirectory(null);
		getTxtJdk().setText(jdkDefaultDir);
		setEnableJDK(true);
		enableJdkRdButtons(getDlRdLocBtn());
		getSerCheckBtn().setEnabled(true);
		return jdkDefaultDir;
	}
	/**
	 * Method is used when JDK check box is unchecked.
	 */
	public static void jdkChkBoxUnChecked() {
		getSerCheckBtn().setSelection(false);
		setEnableJDK(false);
		setEnableServer(false);
		setEnableDlGrp(false, false);
		setEnableDlGrpSrv(false, false);
	}

	/**
	 * Method is used when JDK directory text is modified.
	 * @param role
	 * @param label
	 */
	public static void modifyJdkText(
			WindowsAzureRole role, String label) {
		if (getAutoDlRdCldBtn().getSelection()) {
			setTxtUrl(cmbBoxListener(
					getCmbStrgAccJdk(),
					getTxtUrl(), "JDK"));
			updateJDKDlNote(label);
			updateJDKHome(role);
		}
	}

	/**
	 * Method is used when focus is lost
	 * from JDK directory text box.
	 * @param jdkPath
	 */
	public static void focusLostJdkText(String jdkPath) {
		// Update note below JDK URL text box
		File file = new File(jdkPath);
		if (getDlRdCldBtn().getSelection()
				&& !jdkPath.isEmpty()
				&& file.exists()) {
			String dirName = file.getName();
			getLblDlNoteUrl().
			setText(String.format(
					Messages.dlNtLblDir, dirName));
		} else {
			getLblDlNoteUrl().
			setText(Messages.dlgDlNtLblUrl);
		}
	}

	/**
	 * Method is used when JDK's deploy from download
	 * radio button is selected.
	 * @param role
	 */
	public static void jdkDeployBtnSelected(
			WindowsAzureRole role) {
		// deploy radio button selected
		setEnableDlGrp(true, false);
		updateJDKDlNote(Messages.dlNtLblDir);
		updateJDKHome(role);
	}

	/**
	 * Method is used when JDK's local
	 * radio button is selected.
	 */
	public static void jdkDeployOrAutoToLocalBtnSelected() {
		setEnableDlGrp(false, false);
		enableJdkRdButtons(getDlRdLocBtn());
		// Update note below URL text box
		getLblDlNoteUrl().
		setText(Messages.dlgDlNtLblUrl);
	}

	/**
	 * Method is used when JDK URL text is modified.
	 */
	public static void modifyJdkUrlText() {
		/*
		 * Extract storage account name
		 * and service endpoint from URL
		 * entered by user.
		 */
		String url = getTxtUrl().getText().trim();
		String nameInUrl =
				StorageRegistryUtilMethods.getAccNameFromUrl(
						url);
		setCmbStrgAccJdk(
				urlModifyListner(url, nameInUrl,
						getCmbStrgAccJdk()));
	}

	/**
	 * Method is used when accounts link on JDK tab is clicked.
	 */
	public static void jdkAccLinkClicked() {
		accountsLinkOfJdkClicked();
		updateJDKDlURL();
	}

	/**
	 * Method is used when Server check box is checked.
	 */
	public static void srvChkBoxChecked() {
		enableSrvRdButtons(getDlRdLocBtnSrv());
		setEnableServer(true);
		try {
			String[] servList =
					WindowsAzureProjectManager.
					getServerTemplateNames(cmpntFile);
			Arrays.sort(servList);
			getComboServer().setItems(servList);
		} catch (WindowsAzureInvalidProjectOperationException e) {
			Activator.getDefault().log(e.getMessage());
		}
	}

	/**
	 * Method is used when Server check box is unchecked.
	 */
	public static void srvChkBoxUnChecked() {
		setEnableServer(false);
		setEnableDlGrpSrv(false, false);
		getSerCheckBtn().setEnabled(true);
	}

	/**
	 * Method is used when Server directory text is modified.
	 * @param role
	 * @param label
	 */
	public static void modifySrvText(
			WindowsAzureRole role, String label) {
		if (isSrvAutoUploadChecked()) {
			setTxtUrlSrv(cmbBoxListener(
					getCmbStrgAccSrv(),
					getTxtUrlSrv(), "SERVER"));
			updateSrvDlNote(label);
			updateServerHome(role);
		}
	}

	/**
	 * Method is used when focus is lost
	 * from server directory text box.
	 * @param srvPath
	 * @param label
	 * @param labelNext
	 */
	public static void focusLostSrvText(String srvPath, String label,
			String labelNext) {
		File file = new File(srvPath);
		if (getDlRdCldBtnSrv().
				getSelection()
				&& !srvPath.isEmpty()
				&& file.exists()) {
			String dirName = file.getName();
			getLblDlNoteUrlSrv().
			setText(String.format(
					label, dirName));
		} else {
			getLblDlNoteUrlSrv().
			setText(labelNext);
		}
	}
	/**
	 * Method is used when server's deploy from download
	 * radio button is selected.
	 * @param role
	 * @param label
	 */
	public static void srvDeployBtnSelected(
			WindowsAzureRole role, String label) {
		// server deploy radio button selected
		JdkSrvConfig.setEnableDlGrpSrv(true, false);
		JdkSrvConfig.updateSrvDlNote(label);
		JdkSrvConfig.updateServerHome(role);
	}

	/**
	 * Method is used when server's local
	 * radio button is selected.
	 */
	public static void srvDeployOrAutoToLocalBtnSelected(String label) {
		setEnableDlGrpSrv(false, false);
		enableSrvRdButtons(getDlRdLocBtnSrv());
		// Update note below server URL text box
		getLblDlNoteUrlSrv().
		setText(label);
	}

	/**
	 * Method is used when server URL text is modified.
	 * @param role
	 * @param label
	 */
	public static void modifySrvUrlText() {
		/*
		 * Extract storage account name
		 * and service endpoint from URL
		 * entered by user.
		 */
		String url = getTxtUrlSrv().getText().trim();
		String nameInUrl =
				StorageRegistryUtilMethods.
				getAccNameFromUrl(
						url);
		setCmbStrgAccSrv(
				urlModifyListner(url, nameInUrl,
						getCmbStrgAccSrv()));
	}

	/**
	 * Method is used when accounts link on server tab is clicked.
	 */
	public static void srvAccLinkClicked() {
		accountsLinkOfSrvClicked();
		updateServerDlURL();
	}

	/**
	 * Method used when server auto upload radio
	 * button selected.
	 * @param role
	 * @param label
	 */
	public static void configureAutoUploadServerSettings(
			WindowsAzureRole role, String label) {
		setEnableDlGrpSrv(true, true);
		populateDefaultStrgAccForSrvAuto();
		updateServerDlURL();
		updateSrvDlNote(label);
		updateServerHome(role);
	}

	/**
	 * Method used when JDK auto upload radio
	 * button selected.
	 * @param role
	 * @param label
	 */
	public static void configureAutoUploadJDKSettings(
			WindowsAzureRole role, String label) {
		setEnableDlGrp(true, true);
		updateJDKDlURL();
		updateJDKDlNote(label);
		updateJDKHome(role);
	}
}
