package com.ibagroup.wf.intelia.core;

import java.util.Optional;
import javax.inject.Named;
import javax.inject.Singleton;
import org.apache.commons.lang3.StringUtils;
import org.codejargon.feather.Provides;
import com.ibagroup.wf.intelia.core.config.ConfigurationManager;
import com.ibagroup.wf.intelia.core.metadata.MetadataManager;
import com.ibagroup.wf.intelia.core.metadata.storage.MetadataPermanentStorage;
import com.ibagroup.wf.intelia.core.metadata.storage.MetadataStorage;
import com.ibagroup.wf.intelia.core.storage.S3Manager;
import com.ibagroup.wf.intelia.core.storage.StorageManager;
import com.workfusion.intake.core.Module;
import groovy.lang.Binding;

public class S3MetadataPermanentStorageModule implements Module {

    @Provides
    @Singleton
    public MetadataPermanentStorage metadataPermanentStorage(MetadataManager metadataManager, ConfigurationManager cfg, FlowContext flowContext, Binding context) {
        StorageManager storageMgr = new S3Manager(context, cfg.getConfigItem("screenshots_bucket"), cfg.getConfigItem("screenshots_folder"));
        return new MetadataStorage(context, storageMgr, metadataManager, () -> {
            String taskName = flowContext.getTaskName().equals("WWI TODO: Config name should be here") ? "local-task" : flowContext.getTaskName();

            String submissionId = flowContext.getSubmissionId();
            // String processGuid = BindingUtils.getPropertyValue(binding, PROCESS_UUID);
            String processGuid = flowContext.getProcessGuid();
            String taskPath = "";

            if (StringUtils.isNotBlank(processGuid)) {
                taskPath += processGuid + "/";
            }

            if (StringUtils.isNotBlank(submissionId)) {
                taskPath += submissionId + "/";
            }

            taskPath += taskName;
            return taskPath;
        });
    }

    @Provides
    @Singleton
    @Named("MetadataPermanentStorage")
    public Optional<MetadataPermanentStorage> optionalMetadataPermanentStorage(MetadataManager metadataManager, ConfigurationManager cfg, FlowContext flowContext, Binding context) {
        return Optional.of(metadataPermanentStorage(metadataManager, cfg, flowContext, context));
    }
}
