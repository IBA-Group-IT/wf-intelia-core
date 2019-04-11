package com.ibagroup.wf.intelia.core.metadata;

import java.util.List;

import com.ibagroup.wf.intelia.core.metadata.types.Metadata;

public interface MetadataManager {

    void addMetadata(Metadata... metadata);

    List<Metadata> getMetadataList();

    void clearMetadata();

}