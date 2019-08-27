package com.ibagroup.wf.intelia.core.system;

import org.apache.commons.io.FilenameUtils;
import org.openqa.selenium.WebElement;

import com.workfusion.rpa.helpers.Script;
import com.workfusion.rpa.helpers.utils.ApiUtils;

public class ChromePageObject extends WebPageObject {

	public String clickAndDownload(WebElement downloadLink) {
		String lastDownloadedFile = getLastDownloadedFileOnAgent();
		scrollToElement(downloadLink);
		downloadLink.click();
		String filePath = waitUntilFileDownloadedOnAgent(lastDownloadedFile);
		return filePath;
	}

	private String getLastDownloadedFileOnAgent() {
		StringBuilder script = new StringBuilder();
		script.append("def userHomeDir = System.getProperty('user.home');\n");
		script.append("def dir = new File(userHomeDir + '/Downloads');\n");
		script.append("def files = dir.listFiles();\n");
		script.append("def lastModifiedFile = files && files.size() > 0 ? files.sort { -it.lastModified() }.head() : null;\n");
		script.append("return lastModifiedFile?.getAbsolutePath();");

		String result = (String) Script.executeGroovyScript(script.toString());

		return result;
	}

	private String waitUntilFileDownloadedOnAgent(String lastDownloadedFile) {
		String lastDownloadedFileName = lastDownloadedFile != null ? ApiUtils.encodeFilePathForGroovyScript(FilenameUtils.getName(lastDownloadedFile)) : "";

		StringBuilder script = new StringBuilder();
		script.append("def getLastModifiedFileName = {\n");
		script.append("		def userHomeDir = System.getProperty('user.home');\n");
		script.append("		def dir = new File(userHomeDir + '/Downloads');\n");
		script.append("		def files = dir.listFiles();\n");
		script.append("		def lastModifiedFile = files && files.size() > 0 ? files.sort { -it.lastModified() }.head() : null;\n");
		script.append("		return lastModifiedFile?.getAbsolutePath();\n");
		script.append("}\n");
		script.append("\n");
		script.append("def downloadFilePath = getLastModifiedFileName();\n");
		script.append("def maxCounter = 100;\n");
		script.append("while ((downloadFilePath == null || org.apache.commons.io.FilenameUtils.getName(downloadFilePath).equals(" + lastDownloadedFileName + ")) && maxCounter-- > 0) {\n");
		script.append("		Thread.sleep(500);\n");
		script.append("		downloadFilePath = getLastModifiedFileName();\n");
		script.append("}\n");
		script.append("if (maxCounter > 0) {\n");
		script.append("		maxCounter = 2*60*10;\n");
		script.append("		downloadFilePath = getLastModifiedFileName();\n");
		script.append("		def fileExtension = org.apache.commons.io.FilenameUtils.getExtension(downloadFilePath);\n");
		script.append("		while (('crdownload'.equals(fileExtension) || 'tmp'.equals(fileExtension)) && maxCounter-- > 0) {\n");
		script.append("			Thread.sleep(500);\n");
		script.append("			downloadFilePath = getLastModifiedFileName();\n");
		script.append("			fileExtension = org.apache.commons.io.FilenameUtils.getExtension(downloadFilePath);\n");
		script.append("		}\n");
		script.append("		if(maxCounter == 0) return 'timeout';\n");
		script.append("		return downloadFilePath;\n");
		script.append("}\n");
		script.append("return 'not_started';\n");

		String res = (String) Script.executeGroovyScript(script.toString());

		if (res.equals("not_started")) {
			throw new RuntimeException("Download file error. Downloading of file hadn't started.");
		} else if (res.equals("timeout")) {
			throw new RuntimeException("Download file error. Downloading of file was processing too long.");
		}

		return res;
	}
}
