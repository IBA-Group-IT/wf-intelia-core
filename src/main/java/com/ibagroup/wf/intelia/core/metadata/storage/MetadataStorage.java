package com.ibagroup.wf.intelia.core.metadata.storage;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.freedomoss.crowdcontrol.webharvest.CampaignDto;
import com.freedomoss.crowdcontrol.webharvest.RunDto;
import com.freedomoss.crowdcontrol.webharvest.WebHarvestTaskItem;
import com.ibagroup.wf.intelia.core.BindingUtils;
import com.ibagroup.wf.intelia.core.exceptions.ExceptionHandler;
import com.ibagroup.wf.intelia.core.metadata.MetadataManager;
import com.ibagroup.wf.intelia.core.metadata.types.Metadata;
import com.ibagroup.wf.intelia.core.storage.StorageManager;

import groovy.lang.Binding;

public class MetadataStorage implements MetadataPermanentStorage {

    private static final Logger logger = LoggerFactory.getLogger(MetadataStorage.class);

    public final Binding binding;
    public final StorageManager storageManager;
    public final MetadataManager metadataManager;
    public final Supplier<String> pathGenerator;
    public List<String> metadataUrlList;

    public ExceptionHandler exceptionHandler;

    public MetadataStorage(Binding binding, StorageManager storageManager, MetadataManager metadataManager, Supplier<String> pathGenerator) {
        super();
        this.metadataUrlList = new ArrayList<>();
        this.binding = binding;
        this.storageManager = storageManager;
        this.metadataManager = metadataManager;
        this.pathGenerator = pathGenerator;
    }

    public void setExceptionHandler(ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    public MetadataStorage(Binding binding, StorageManager storageManager, MetadataManager metadataManager) {
        this(binding, storageManager, metadataManager, () -> {
            WebHarvestTaskItem item = BindingUtils.getWebHarvestTaskItem(binding);
            RunDto runDto = item.getRun();
            CampaignDto campaignDto = item.getCampaignDto();
            String runname = runDto.getCampaignName();
            String taskName = campaignDto.getTitle().equals("WWI TODO: Config name should be here") ? "local-task" : campaignDto.getTitle();
            String taskPath = runDto.getUuid() + "/" + runname + "_" + new Date().toString().replaceAll("\\s", "_") + "/" + taskName;
            return taskPath;
        });
    }

    @Override
    public boolean storeAllMetadata() {
        return storeAllMetadata(null);
    }

    @Override
    public boolean storeAllMetadata(String uploadUid) {
        logger.info("Uploading " + metadataManager.getMetadataList().size() + " metadata elements");

        boolean allSuccess = true;

        String folderPath = pathGenerator.get();
        if (StringUtils.isNotBlank(uploadUid)) {
            folderPath += "/" + uploadUid;
        }

        for (int i = 0; i < metadataManager.getMetadataList().size(); i++) {
            Metadata metadata = metadataManager.getMetadataList().get(i);
            String recordFolder = null;
            String fileName = null;

            if (metadata.getName() != null && metadata.getName().contains("/")) {
                recordFolder = folderPath + "/" + metadata.getName().substring(0, metadata.getName().indexOf("/"));
                fileName = DateTimeFormatter.ISO_INSTANT.format(metadata.getDate()) + ("_" + metadata.getName().substring(metadata.getName().indexOf("/") + 1))
                        + metadata.getType().getExtension();
            } else {
                recordFolder = folderPath;
                fileName =
                        DateTimeFormatter.ISO_INSTANT.format(metadata.getDate()) + (metadata.getName() != null ? "_" + metadata.getName() : "") + metadata.getType().getExtension();
            }

            String filePath = recordFolder + "/" + fileName;
            try {
                allSuccess &= storageManager.uploadFile(filePath, metadata.getData());
                metadataUrlList.add(filePath);

            } catch (Exception exception) {
                if (null != exceptionHandler) {
                    exceptionHandler.logCurrentException(exception);
                }
                logger.error(exception.getMessage(), exception);
                allSuccess = false;
            }
        }

        metadataManager.clearMetadata();
        return allSuccess;
    }

    @Override
    public List<String> getMetadataUrlList() {
        return metadataUrlList;
    }
}
