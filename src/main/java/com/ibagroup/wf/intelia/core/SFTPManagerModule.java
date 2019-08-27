package com.ibagroup.wf.intelia.core;

import static com.ibagroup.wf.intelia.core.CoreModule.BOT_CONFIG_PARAMS_PARAM_NAME;
import static com.ibagroup.wf.intelia.core.storage.SFTPManager.SFTP_FOLDER_PARAM_NAME;
import static com.ibagroup.wf.intelia.core.storage.SFTPManager.SFTP_HOST_PARAM_NAME;
import static com.ibagroup.wf.intelia.core.storage.SFTPManager.SFTP_PASSWORD_PARAM_NAME;
import static com.ibagroup.wf.intelia.core.storage.SFTPManager.SFTP_USER_PARAM_NAME;
import java.util.Map;
import java.util.Optional;
import javax.inject.Named;
import javax.inject.Singleton;
import org.codejargon.feather.Provides;
import com.ibagroup.wf.intelia.core.config.ConfigurationManager;
import com.ibagroup.wf.intelia.core.security.SecurityUtils;
import com.ibagroup.wf.intelia.core.storage.SFTPManager;
import com.ibagroup.wf.intelia.core.storage.StorageManager;
import com.workfusion.intake.core.Module;

/**
 * If included - provides default SFTPManager and SFTPStorageManager instances resolved from bot
 * params OR config:
 * <ul>
 * <li>sftp_host - name of the host</li>
 * <li>sftp_folder - name of the folder</li>
 * <li>sftp_user - name of the user</li>
 * <li>sftp_password - resolved from the Secrets Vault, for the user above</li>
 * </ul>
 * 
 * @author dmitriev
 *
 */
public class SFTPManagerModule implements Module {

    public final static String SFTP_STORAGE_MANAGER = "SFTPStorageManager";

    @Provides
    @Named(SFTP_HOST_PARAM_NAME)
    public String s3BucketName(ConfigurationManager cfg, @Named(BOT_CONFIG_PARAMS_PARAM_NAME) Map<String, String> params) {
        return params.containsKey(SFTP_HOST_PARAM_NAME) ? params.get(SFTP_HOST_PARAM_NAME) : cfg.getConfigItem(SFTP_HOST_PARAM_NAME);
    }

    @Provides
    @Named(SFTP_FOLDER_PARAM_NAME)
    public String s3FolderName(ConfigurationManager cfg, @Named(BOT_CONFIG_PARAMS_PARAM_NAME) Map<String, String> params) {
        return params.containsKey(SFTP_FOLDER_PARAM_NAME) ? params.get(SFTP_FOLDER_PARAM_NAME) : cfg.getConfigItem(SFTP_FOLDER_PARAM_NAME);
    }

    @Provides
    @Singleton
    @Named(SFTP_USER_PARAM_NAME)
    public String sharedFolderUser(ConfigurationManager cfg, @Named(BOT_CONFIG_PARAMS_PARAM_NAME) Map<String, String> params) {
        return params.containsKey(SFTP_USER_PARAM_NAME) ? params.get(SFTP_USER_PARAM_NAME) : cfg.getConfigItem(SFTP_USER_PARAM_NAME);
    }

    @Provides
    @Singleton
    @Named(SFTP_PASSWORD_PARAM_NAME)
    public String sharedFolderPassword(SecurityUtils securityUtils, @Named(SFTP_USER_PARAM_NAME) String sfUser) {
        try {
            return securityUtils.getSecureEntry(sfUser).getValue();
        } catch (Throwable e) {
            throw new RuntimeException("No password resolved for SFTP user " + sfUser, e);
        }
    }

    @Provides
    @Singleton
    @Named(SFTP_STORAGE_MANAGER)
    public StorageManager storageManager(SFTPManager s3Manager) {
        return s3Manager;
    }

    @Provides
    @Singleton
    @Named(SFTP_STORAGE_MANAGER)
    public Optional<StorageManager> optionalStorageManager(SFTPManager s3Manager) {
        return Optional.of(s3Manager);
    }

}
