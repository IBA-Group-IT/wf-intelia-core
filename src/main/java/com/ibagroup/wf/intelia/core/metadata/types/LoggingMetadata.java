package com.ibagroup.wf.intelia.core.metadata.types;

import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;

public class LoggingMetadata extends Metadata {

    public LoggingMetadata(String input) {
        this(null, input);
    }

    public LoggingMetadata(String name, String input) {
        super(Type.TXT, IOUtils.toInputStream(input, StandardCharsets.UTF_8), name);
    }

}
