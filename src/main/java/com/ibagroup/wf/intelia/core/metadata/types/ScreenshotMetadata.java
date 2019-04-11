package com.ibagroup.wf.intelia.core.metadata.types;

import java.io.ByteArrayInputStream;

public class ScreenshotMetadata extends Metadata {

    public ScreenshotMetadata(byte[] input) {
        this(null, input);
    }

    public ScreenshotMetadata(String name, byte[] input) {
        super(Type.PNG, new ByteArrayInputStream(input), name);
    }

}
