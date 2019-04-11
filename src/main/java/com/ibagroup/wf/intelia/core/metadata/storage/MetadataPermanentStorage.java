package com.ibagroup.wf.intelia.core.metadata.storage;

import java.util.List;

public interface MetadataPermanentStorage {

    boolean storeAllMetadata();

    boolean storeAllMetadata(String uploadUid);
    
    List<String> getMetadataUrlList();
}
