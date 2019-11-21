package com.ibagroup.wf.intelia.core.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import com.ibagroup.wf.intelia.core.adaptations.MachineVersionAdaptations;
import com.ibagroup.wf.intelia.core.storage.S3Manager;
import com.workfusion.rpa.helpers.RPA;
import com.workfusion.rpa.helpers.S3;
import com.workfusion.rpa.helpers.S3OverwriteStrategy;

import groovy.lang.Binding;

/**
 * Execute batch of scripts on spreadsheet excel file specified
 */
public class FileTransferUtils {

	public static final String DEFAULT_S3_BUCKET_FOR_TRANSFERING = "workfusion-resources";
	/**
	 * Default size of file part to upload|download
	 */
	public static final int DEFAULT_SPLIT_SIZE = 1024 * 1024;

	/**
	 * Upload file from InputStream to the file on Agent using Groovy Script. File will created in the 'java.io.tmpdir'
	 * property folder.
	 * 
	 * @param is
	 *            file to be uploaded
	 * @param fileName
	 *            name of the file
	 * @return file path
	 * 
	 * @param fileData
	 * @return
	 */
	public static String uploadByS3(InputStream inputStream, String filePath, Binding binding) {
		boolean upload = false;
		String fileLink = null;
		String tmpFileName = String.format("rpa_tmp_file_%s.%s", UUID.randomUUID().toString(), FilenameUtils.getExtension(filePath));

		S3Manager s3Manager = new S3Manager(binding, DEFAULT_S3_BUCKET_FOR_TRANSFERING, "rpa-temp");

		try {
			upload = s3Manager.uploadFile(tmpFileName, inputStream);
		} catch (Exception e) {
			throw new RuntimeException("Upload file error. An error occurred during uploading file to S3.", e);
		}

		if (upload) {
			fileLink = s3Manager.getS3FileWebUrl(tmpFileName);
		} else {
			throw new RuntimeException(String.format("Upload of '%s' file has failed.", tmpFileName));
		}

		try {
			StringBuilder script = new StringBuilder();
			script.append("import java.net.URL; \n");
			script.append("import org.apache.commons.io.FileUtils; \n");
			script.append("\n");
			script.append("URL fileUrl = new URL('" + fileLink + "');\n");

			if (filePath.contains("/")) {
				script.append("File file = new File('" + filePath + "'); \n");
			} else {
				script.append("def tmpDir = System.getProperty('java.io.tmpdir');\n");
				script.append("File file = new File(tmpDir+'/" + filePath + "'); \n");
			}

			script.append("FileUtils.copyURLToFile(fileUrl, file); \n");
			script.append("return file.getAbsolutePath();");

			String res = MachineVersionAdaptations.executeGroovyScript(script.toString());

			if (res == null) {
				throw new RuntimeException("A problem occurred during uploading of file. RPA Agent has returned NULL. See logs of RPA Agent.");
			}

			return res;
		} finally {
			try {
				s3Manager.deleteFile(tmpFileName);
			} catch (Exception e) {
				// do nothing
			}
		}
	}

	/**
	 * Download from agent using S3. Delete file on S3 after download from S3.
	 * 
	 * @param filePathOnAgent
	 * @param binding
	 * @return
	 * @throws IOException
	 */
	public static InputStream downloadByS3(String filePathOnAgent, Binding binding) throws IOException {
		String s3Url = uploadToS3ByAgent(filePathOnAgent, binding);
		String tmpFileName = FilenameUtils.getName(s3Url);
		S3Manager s3Manager = new S3Manager(binding, DEFAULT_S3_BUCKET_FOR_TRANSFERING, "rpa-temp");
		try {
			return s3Manager.getFile(FilenameUtils.getName(tmpFileName));
		} finally {
			try {
				s3Manager.deleteFile(tmpFileName);
			} catch (Exception e) {
				// do nothing
			}
		}
	}

	/**
	 * Split file to several temporary files with size specified, download each file separately and combine to one file.
	 * Remove temporary files on agent.
	 * 
	 * @param filePathOnAgent
	 * @return
	 */
	public static InputStream downloadBySplit(String filePathOnAgent) {
		return downloadBySplit(filePathOnAgent, DEFAULT_SPLIT_SIZE);
	}

	/**
	 * Split file to several temporary files with size specified, download each file separately and combine to one file.
	 * Remove temporary files on agent.
	 * 
	 * @param filePathOnAgent
	 * @param size
	 * @return
	 */
	public static InputStream downloadBySplit(String filePathOnAgent, int size) {
		// Split file on agent before download
		String paths = splitBeforeDownload(filePathOnAgent, size);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		for (String path : paths.split(";")) {
			// Download by parts and join file
			if (!path.isEmpty()) {
				byte[] tmpBytes = RPA.downloadFileFromAgent(path);
				RPA.deleteFileOnAgent(path);
				try {
					outputStream.write(tmpBytes);
				} catch (IOException e) {
					throw new RuntimeException("Can't read file: " + path, e);
				}
			}
		}
		return new ByteArrayInputStream(outputStream.toByteArray());
	}

	/**
	 * Split file (input stream) to parts with size specified and upload these parts to agent one by one. Join files on
	 * agent to one big file and return this file name with path.
	 * 
	 * @param is
	 * @param fileName
	 * @param size
	 * @return
	 * @throws IOException
	 */
	public static String uploadBySplit(InputStream is, String fileName) throws IOException {
		return uploadBySplit(is, fileName, DEFAULT_SPLIT_SIZE);
	}

	/**
	 * Split file (input stream) to parts with size specified and upload these parts to agent one by one. Join files on
	 * agent to one big file and return this file name with path.
	 * 
	 * @param is
	 * @param fileName
	 * @param size
	 * @return
	 * @throws IOException
	 */
	public static String uploadBySplit(InputStream is, String fileName, int splitSize) throws IOException {
		String name = fileName;
		String ext = "";
		List<String> paths = new ArrayList<String>();

		if (fileName.contains(".")) {
			name = FilenameUtils.getBaseName(fileName);
			ext = FilenameUtils.getExtension(fileName);
		}

		// Get byte array from input stream
		// TODO check if optimization is needed - it's can be better to use is.read(tmpBytes)
		// to read the part of stream and send it immediately
		byte[] allBytes = IOUtils.toByteArray(is);
		int length = Math.max(splitSize, allBytes.length);
		int partSize = Math.min(splitSize, allBytes.length);
		for (int i = 0; i < length; i = i + partSize) {
			byte[] part = Arrays.copyOfRange(allBytes, i, Math.min(i + partSize, allBytes.length));
			paths.add(RPA.sendToAgent(part, name + "_", "." + ext + "_tmp"));
			part = null;
		}

		return join(paths, fileName);
	}

	private static String uploadToS3ByAgent(String filePathOnAgent, Binding binding) {
		// upload to S3 using S3.uploadFileS3() method
		String s3EndpointUrl = BindingUtils.getPropertyValue(binding, "s3EndpointUrl");
		String signerType = "S3SignerType";
		String s3AccessKey = BindingUtils.getPropertyValue(binding, "s3AccessKey");
		String s3SecretKey = BindingUtils.getPropertyValue(binding, "s3SecretKey");
		String tmpFileName = String.format("rpa-temp/rpa_tmp_file_%s.%s", UUID.randomUUID().toString(), FilenameUtils.getExtension(filePathOnAgent));

		return S3.uploadFileS3(s3EndpointUrl, signerType, s3AccessKey, s3SecretKey, DEFAULT_S3_BUCKET_FOR_TRANSFERING, tmpFileName, filePathOnAgent,
				S3OverwriteStrategy.OVERWRITE);
	}

	/**
	 * Split file on agent. Return ;-separated list of files
	 * 
	 * @param fileName
	 * @param size
	 * @return
	 */
	private static String splitBeforeDownload(String fileName, int splitSize) {

		String resultFileNames = null;

		String name = FilenameUtils.getBaseName(fileName);
		String ext = FilenameUtils.getExtension(fileName);
		fileName = FilenameUtils.separatorsToUnix(fileName);

		StringBuilder script = new StringBuilder();
		script.append("import org.apache.commons.io.FileUtils; \n");
		script.append("String tmpDir = System.getProperty('java.io.tmpdir');\n");
		script.append("StringBuilder paths = new StringBuilder();\n");
		script.append("File srcFile = new File('" + fileName + "');\n");
		script.append("byte[] tmpBytes = FileUtils.readFileToByteArray(srcFile);\n");
		script.append("int length = Math.max(" + splitSize + ", tmpBytes.length);\n");
		script.append("int partSize = Math.min(" + splitSize + ", tmpBytes.length);\n");
		script.append("for (int i = 0; i < length; i = i + partSize) {\n");
		script.append("    byte[] part = Arrays.copyOfRange(tmpBytes, i,  Math.min(i + partSize, tmpBytes.length));\n");
		script.append("    File destFile = new File(tmpDir + '" + name + "." + ext + "_tmp' + i);\n");
		script.append("    FileUtils.writeByteArrayToFile(destFile, part, false);\n");
		script.append("    paths.append(destFile.getAbsolutePath()).append(';');\n");
		script.append("};\n");
		script.append("return paths.toString();\n");

		resultFileNames = (String) MachineVersionAdaptations.executeGroovyScript(script.toString());

		if (resultFileNames == null) {
			throw new RuntimeException("A problem occurred during join splited file. RPA Agent has returned NULL. See logs of RPA Agent.");
		}

		return resultFileNames;
	}

	/**
	 * Join files on agent to file specified and delete it after read. File will stored to 'java.io.tmpdir' directory.
	 * 
	 * @param files
	 *            the list of files to be combined
	 * @param destFileName
	 *            the destination file name wo path
	 * @return the path of final file
	 */
	private static String join(List<String> files, String destFileName) {
		// Run remote script to join files
		String returnFileName = null;

		try {

			for (String fileName : files) {
				StringBuilder script = new StringBuilder();
				fileName = FilenameUtils.separatorsToUnix(fileName);

				script.append("import org.apache.commons.io.FileUtils; \n");
				script.append("\n");
				script.append("def tmpDir = System.getProperty('java.io.tmpdir');\n");
				script.append("File destFile = new File(tmpDir +'/" + destFileName + "');\n");
				script.append("File tmpFile = new File('" + fileName + "');\n");
				script.append("byte[] tmpBytes = FileUtils.readFileToByteArray(tmpFile);\n");
				script.append("FileUtils.writeByteArrayToFile(destFile, tmpBytes, true);\n");
				script.append("FileUtils.deleteQuietly(tmpFile);\n");
				script.append("return destFile.getAbsolutePath();");

				Object res = MachineVersionAdaptations.executeGroovyScript(script.toString());

				if (res == null) {
					throw new RuntimeException("A problem occurred during join splited file. RPA Agent has returned NULL. See logs of RPA Agent.");
				}

				returnFileName = res.toString();
			}

		} finally {
		}

		return returnFileName;
	}
}
