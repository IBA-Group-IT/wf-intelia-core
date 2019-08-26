package com.ibagroup.wf.intelia.core.storage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.io.IOUtils;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileOutputStream;

/**
 * Class is responsible for reading and uploading of files to shared folder
 */
public class SharedFolderManager implements StorageManager {

    public final static String SHARED_FOLDER_DOMAIN_PARAM_NAME = "sf_domain";
    public final static String SHARED_FOLDER_USER_PARAM_NAME = "sf_user";
    public final static String SHARED_FOLDER_PASSWORD_PARAM_NAME = "sf_password";

    private NtlmPasswordAuthentication auth;

    @Inject
    public SharedFolderManager(@Named(SHARED_FOLDER_DOMAIN_PARAM_NAME) String domain, @Named(SHARED_FOLDER_USER_PARAM_NAME) String user,
            @Named(SHARED_FOLDER_PASSWORD_PARAM_NAME) String password) {
        this.auth = new NtlmPasswordAuthentication(domain, user, password);
    }

    /**
     * Function uploads file on shared folder
     * 
     * @param bytes file content
     * @param path path on shared folder
     */
    @Override
    public boolean uploadFile(String path, InputStream input) {
        SmbFile sFile;
        try {
            sFile = new SmbFile(path, auth);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (SmbFileOutputStream sfos = new SmbFileOutputStream(sFile)) {
            sfos.write(IOUtils.toByteArray(input));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return true;
    }

    /**
     * Function returns Input Stream from a file on shared folder
     *
     * @param path full path to file on shared folder
     */
    @Override
    public InputStream getFile(String path) {
        try {
            return new SmbFile(path, auth).getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> listFiles(String path, String filter) {
        try {
            return Arrays.asList(new SmbFile(path, auth).list());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean deleteFile(String path) {
        try {
            new SmbFile(path, auth).delete();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean createDir(String path) {
        try {
            new SmbFile(path, auth).mkdir();
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
