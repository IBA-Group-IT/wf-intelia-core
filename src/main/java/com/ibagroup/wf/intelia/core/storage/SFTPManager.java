package com.ibagroup.wf.intelia.core.storage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.vfs2.FileFilter;
import org.apache.commons.vfs2.FileFilterSelector;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.NameScope;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;

public class SFTPManager implements StorageManager {

    public final static String SFTP_HOST_PARAM_NAME = "sftp_host";
    public final static String SFTP_FOLDER_PARAM_NAME = "sftp_folder";
    public final static String SFTP_USER_PARAM_NAME = "sftp_user";
    public final static String SFTP_PASSWORD_PARAM_NAME = "sftp_password";

    private String host;
    private String folder;
    private String user;
    private String password;

    @Inject
    public SFTPManager(@Named(SFTP_HOST_PARAM_NAME) String host, @Named(SFTP_FOLDER_PARAM_NAME) String folder, @Named(SFTP_USER_PARAM_NAME) String user,
            @Named(SFTP_PASSWORD_PARAM_NAME) String password) {
        this.host = host;
        this.folder = folder;
        this.user = user;
        this.password = password;
    }

    @Override
    public boolean uploadFile(String path, InputStream input) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public List<String> listFiles(String path, String filter) {
        final Pattern pattern = Pattern.compile(filter);
        StandardFileSystemManager manager = null;
        try {
            manager = new StandardFileSystemManager();
            manager.init();

            FileObject root = getRoot(manager);

            final FileFilter fileFilter = new FileFilter() {
                @Override
                public boolean accept(FileSelectInfo fileInfo) {
                    return pattern.matcher(fileInfo.getFile().getName().getBaseName()).matches();
                }
            };

            FileSelector fileSelector = new FileFilterSelector(fileFilter);
            FileObject[] rootFiles = root.findFiles(fileSelector);

            if (rootFiles != null) {
                return Arrays.stream(rootFiles).map(f -> f.getName().getBaseName()).collect(Collectors.toList());
            }
        } catch (Throwable t) {
            throw new RuntimeException("Failed to list SFTP Folder " + path, t);
        } finally {
            if (manager != null) {
                manager.close();
            }
        }
        return Collections.emptyList();
    }

    @Override
    public InputStream getFile(String path) {
        StandardFileSystemManager manager = null;
        try {
            manager = new StandardFileSystemManager();
            manager.init();
            FileObject file = getFile(manager, path);
            if (file != null) {
                return file.getContent().getInputStream();
            }
        } catch (Throwable t) {
            throw new RuntimeException("Failed to get SFTP File " + path, t);
        } finally {
            if (manager != null) {
                manager.close(); // TODO verify if stream breaks upon closing manager
            }
        }

        return null;
    }

    @Override
    public boolean deleteFile(String path) {
        StandardFileSystemManager manager = null;
        try {
            manager = new StandardFileSystemManager();
            manager.init();
            FileObject file = getFile(manager, path);
            if (file != null) {
                return file.delete();
            }
        } catch (Throwable t) {
            throw new RuntimeException("Failed to delete SFTP File " + path, t);
        } finally {
            if (manager != null) {
                manager.close(); // TODO verify if stream breaks upon closing manager
            }
        }

        return false;
    }

    @Override
    public boolean createDir(String path) {
        StandardFileSystemManager manager = null;
        try {
            manager = new StandardFileSystemManager();
            manager.init();
            FileObject root = getRoot(manager);
            FileObject dir = root.resolveFile(path, NameScope.CHILD);

            if (!dir.exists()) {
                dir.createFolder();
            }
            if (!isDirectory(dir)) {
                throw new IOException(dir.getName().toString() + " is not a directory");
            }
            return true;
        } catch (Throwable t) {
            throw new RuntimeException("Failed to delete SFTP File " + path, t);
        } finally {
            if (manager != null) {
                manager.close();
            }
        }

    }

    private FileObject getRoot(StandardFileSystemManager manager) throws FileSystemException {

        // Setup our SFTP configuration
        FileSystemOptions opts = new FileSystemOptions();
        SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(opts, "no");
        SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, false);
        SftpFileSystemConfigBuilder.getInstance().setTimeout(opts, 10000);

        String sftpUri = "sftp://" + user + ":" + password + "@" + host + folder;

        return manager.resolveFile(sftpUri, opts);

    }


    private FileObject getFile(StandardFileSystemManager manager, String path) throws FileSystemException {
        return getRoot(manager).getChild(path);
    }

    private boolean isDirectory(FileObject fo) throws IOException {
        if (fo == null)
            return false;
        if (fo.getType() == FileType.FOLDER) {
            return true;
        } else {
            return false;
        }
    }
}
