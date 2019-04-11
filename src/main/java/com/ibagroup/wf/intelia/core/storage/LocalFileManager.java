package com.ibagroup.wf.intelia.core.storage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.lang3.StringUtils;

public class LocalFileManager implements StorageManager {

    private String basePath;

    public LocalFileManager() {}

    public LocalFileManager(String basePath) {
        super();
        this.basePath = basePath;
    }

    @Override
    public boolean uploadFile(String path, InputStream input) {
        try {
            String fullPath = getFullPath(path);
            FileUtils.writeByteArrayToFile(new File(fullPath), IOUtils.toByteArray(input));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return true;
    }

    @Override
    public List<String> listFiles(String path, String filter) {
        final URI fullPath = new File(getFullPath(path)).toURI();
        Collection<File> listFiles = FileUtils.listFiles(new File(fullPath), new RegexFileFilter(filter), FileFilterUtils.trueFileFilter());
        return listFiles.stream().map((file) -> {
            String relateivePath = fullPath.relativize(file.toURI()).getPath();
            return relateivePath;
        }).collect(Collectors.toList());
    }

    @Override
    public InputStream getFile(String path) {
        try {
            String fullPath = getFullPath(path);
            return FileUtils.openInputStream(new File(fullPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getFullPath(String path) {
        return StringUtils.isNotBlank(basePath) ? FilenameUtils.concat(basePath, path) : path;
    }

    @Override
    public boolean deleteFile(String path) {
        File fullPath = new File(getFullPath(path));
        return FileUtils.deleteQuietly(fullPath);
    }

    @Override
    public boolean createDir(String path) {
        return false;
    }

}
