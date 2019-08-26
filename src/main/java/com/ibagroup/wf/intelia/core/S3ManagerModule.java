package com.ibagroup.wf.intelia.core;

import static com.ibagroup.wf.intelia.core.CoreModule.BOT_CONFIG_PARAMS_PARAM_NAME;
import static com.ibagroup.wf.intelia.core.storage.S3Manager.S3_BUCKET_PARAM_NAME;
import static com.ibagroup.wf.intelia.core.storage.S3Manager.S3_FOLDER_PARAM_NAME;
import java.util.Map;
import java.util.Optional;
import javax.inject.Named;
import javax.inject.Singleton;
import org.codejargon.feather.Provides;
import com.ibagroup.wf.intelia.core.config.ConfigurationManager;
import com.ibagroup.wf.intelia.core.storage.S3Manager;
import com.ibagroup.wf.intelia.core.storage.StorageManager;
import com.workfusion.intake.core.Module;

/**
 * If included - provides default S3Manager and S3StorageManager instances resolved from bot params
 * OR config:
 * <ul>
 * <li>s3_bucket - name of the bucket</li>
 * <li>s3_folder - name of the folder</li>
 * </ul>
 * 
 * @author dmitriev
 *
 */
public class S3ManagerModule implements Module {

    public final static String S3_STORAGE_MANAGER = "S3StorageManager";

    @Provides
    @Named(S3_BUCKET_PARAM_NAME)
    public String s3BucketName(ConfigurationManager cfg, @Named(BOT_CONFIG_PARAMS_PARAM_NAME) Map<String, String> params) {
        return params.containsKey(S3_BUCKET_PARAM_NAME) ? params.get(S3_BUCKET_PARAM_NAME) : cfg.getConfigItem(S3_BUCKET_PARAM_NAME);
    }

    @Provides
    @Named(S3_FOLDER_PARAM_NAME)
    public String s3FolderName(ConfigurationManager cfg, @Named(BOT_CONFIG_PARAMS_PARAM_NAME) Map<String, String> params) {
        return params.containsKey(S3_FOLDER_PARAM_NAME) ? params.get(S3_FOLDER_PARAM_NAME) : cfg.getConfigItem(S3_FOLDER_PARAM_NAME);
    }


    @Provides
    @Singleton
    @Named(S3_STORAGE_MANAGER)
    public StorageManager storageManager(S3Manager s3Manager) {
        return s3Manager;
    }

    @Provides
    @Singleton
    @Named(S3_STORAGE_MANAGER)
    public Optional<StorageManager> optionalStorageManager(S3Manager s3Manager) {
        return Optional.of(s3Manager);
    }

}
