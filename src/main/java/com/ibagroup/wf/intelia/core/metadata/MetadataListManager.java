package com.ibagroup.wf.intelia.core.metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ibagroup.wf.intelia.core.metadata.types.Metadata;

public class MetadataListManager implements MetadataManager {

    static final Logger logger = LoggerFactory.getLogger(MetadataListManager.class);

    List<Metadata> metadataList = new ArrayList<>();

    @Override
    public void addMetadata(Metadata... metadata) {
        this.metadataList.addAll(java.util.Arrays.asList(metadata));
    }

    @Override
    public List<Metadata> getMetadataList() {
        return Collections.unmodifiableList(metadataList);
    }

    @Override
    public void clearMetadata() {
        metadataList.clear();
    }

}