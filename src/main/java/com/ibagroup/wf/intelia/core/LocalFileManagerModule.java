package com.ibagroup.wf.intelia.core;

import static com.ibagroup.wf.intelia.core.CoreModule.BOT_CONFIG_PARAMS_PARAM_NAME;
import static com.ibagroup.wf.intelia.core.storage.LocalFileManager.LOCAL_FOLDER_BASE_PATH_PARAM_NAME;
import java.util.Map;
import java.util.Optional;
import javax.inject.Named;
import javax.inject.Singleton;
import org.codejargon.feather.Provides;
import com.ibagroup.wf.intelia.core.config.ConfigurationManager;
import com.ibagroup.wf.intelia.core.storage.LocalFileManager;
import com.ibagroup.wf.intelia.core.storage.StorageManager;
import com.workfusion.intake.core.Module;

/**
 * If included - provides default LocalFileManager and LocalFileStorageManager instances resolved
 * from bot params OR config:
 * <ul>
 * <li>lf_basepath - base path for all files</li>
 * </ul>
 * 
 * @author dmitriev
 *
 */
public class LocalFileManagerModule implements Module {

    public final static String LOCAL_FILE_STORAGE_MANAGER = "LocalFileStorageManager";

    @Provides
    @Named(LOCAL_FOLDER_BASE_PATH_PARAM_NAME)
    public String s3BucketName(ConfigurationManager cfg, @Named(BOT_CONFIG_PARAMS_PARAM_NAME) Map<String, String> params) {
        return params.containsKey(LOCAL_FOLDER_BASE_PATH_PARAM_NAME) ? params.get(LOCAL_FOLDER_BASE_PATH_PARAM_NAME) : cfg.getConfigItem(LOCAL_FOLDER_BASE_PATH_PARAM_NAME);
    }

    @Provides
    @Singleton
    @Named(LOCAL_FILE_STORAGE_MANAGER)
    public StorageManager storageManager(LocalFileManager lfManager) {
        return lfManager;
    }

    @Provides
    @Singleton
    @Named(LOCAL_FILE_STORAGE_MANAGER)
    public Optional<StorageManager> optionalStorageManager(LocalFileManager lfManager) {
        return Optional.of(lfManager);
    }

}
