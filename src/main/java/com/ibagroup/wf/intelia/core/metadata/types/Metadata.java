package com.ibagroup.wf.intelia.core.metadata.types;

import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.temporal.Temporal;

import com.ibagroup.wf.intelia.core.CommonConstants;
import com.ibagroup.wf.intelia.core.robots.factory.RunnerContext;

public class Metadata {

    private final Type type;
    private final InputStream strm;
    private final String name;
    private final Temporal date;

    public Metadata(Type type, InputStream strm, String name, Temporal date) {
        this.type = type;
        this.strm = strm;
        if (!RunnerContext.getRecordUuid().equals(CommonConstants.DUMMY_UUID)) {
        	this.name = RunnerContext.getRecordUuid() + "/"+ name;
        } else {
        	this.name = name;
        }
        this.date = date;
    }

    public Metadata(Type type, InputStream strm, String name) {
        this(type, strm, name, OffsetDateTime.now());
    }

    public Metadata(Type type, InputStream strm) {
        this(type, strm, null);
    }

    public Type getType() {
        return type;
    }

    public InputStream getData() {
        return strm;
    }

    public String getName() {
        return name;
    }

    public Temporal getDate() {
        return date;
    }

    public enum Type {
        PNG(".png"), TXT(".txt");
        private String extension;

        Type(String extension) {
            this.extension = extension;
        }

        public String getExtension() {
            return extension;
        }
    }

}