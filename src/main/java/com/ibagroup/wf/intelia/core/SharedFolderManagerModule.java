package com.ibagroup.wf.intelia.core;

import static com.ibagroup.wf.intelia.core.CoreModule.BOT_CONFIG_PARAMS_PARAM_NAME;
import static com.ibagroup.wf.intelia.core.storage.SharedFolderManager.SHARED_FOLDER_DOMAIN_PARAM_NAME;
import static com.ibagroup.wf.intelia.core.storage.SharedFolderManager.SHARED_FOLDER_PASSWORD_PARAM_NAME;
import static com.ibagroup.wf.intelia.core.storage.SharedFolderManager.SHARED_FOLDER_USER_PARAM_NAME;
import java.util.Map;
import java.util.Optional;
import javax.inject.Named;
import javax.inject.Singleton;
import org.codejargon.feather.Provides;
import com.ibagroup.wf.intelia.core.config.ConfigurationManager;
import com.ibagroup.wf.intelia.core.security.SecurityUtils;
import com.ibagroup.wf.intelia.core.storage.SharedFolderManager;
import com.ibagroup.wf.intelia.core.storage.StorageManager;
import com.workfusion.intake.core.Module;

/**
 * If included - provides default SharedFolderManager and SharedFolderStorageManager instances
 * resolved from bot params OR config:
 * <ul>
 * <li>sf_domain - name of the domain</li>
 * <li>sf_user - name of the user</li>
 * <li>sf_password - resolved from the Secrets Vault, for the user above</li>
 * </ul>
 * 
 * @author dmitriev
 *
 */
public class SharedFolderManagerModule implements Module {

    public final static String SHARED_FOLDER_STORAGE_MANAGER = "SharedFolderStorageManager";

    @Provides
    @Singleton
    @Named(SHARED_FOLDER_DOMAIN_PARAM_NAME)
    public String sharedFolderDomain(ConfigurationManager cfg, @Named(BOT_CONFIG_PARAMS_PARAM_NAME) Map<String, String> params) {
        return params.containsKey(SHARED_FOLDER_DOMAIN_PARAM_NAME) ? params.get(SHARED_FOLDER_DOMAIN_PARAM_NAME) : cfg.getConfigItem(SHARED_FOLDER_DOMAIN_PARAM_NAME);
    }

    @Provides
    @Singleton
    @Named(SHARED_FOLDER_USER_PARAM_NAME)
    public String sharedFolderUser(ConfigurationManager cfg, @Named(BOT_CONFIG_PARAMS_PARAM_NAME) Map<String, String> params) {
        return params.containsKey(SHARED_FOLDER_USER_PARAM_NAME) ? params.get(SHARED_FOLDER_USER_PARAM_NAME) : cfg.getConfigItem(SHARED_FOLDER_USER_PARAM_NAME);
    }

    @Provides
    @Singleton
    @Named(SHARED_FOLDER_PASSWORD_PARAM_NAME)
    public String sharedFolderPassword(SecurityUtils securityUtils, @Named(SHARED_FOLDER_USER_PARAM_NAME) String sfUser) {
        try {
            return securityUtils.getSecureEntry(sfUser).getValue();
        } catch (Throwable e) {
            throw new RuntimeException("No password resolved for Shared Folder user " + sfUser, e);
        }
    }

    @Provides
    @Singleton
    @Named(SHARED_FOLDER_STORAGE_MANAGER)
    public StorageManager storageManager(SharedFolderManager sfManager) {
        return sfManager;
    }

    @Provides
    @Singleton
    @Named(SHARED_FOLDER_STORAGE_MANAGER)
    public Optional<StorageManager> optionalStorageManager(SharedFolderManager sfManager) {
        return Optional.of(sfManager);
    }

}
