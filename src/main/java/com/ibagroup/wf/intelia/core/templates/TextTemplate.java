package com.ibagroup.wf.intelia.core.templates;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import freemarker.template.TemplateException;

public abstract class TextTemplate {

    protected String templateName = "";
    protected HashMap<String, Object> scopes;

    public TextTemplate(String templateText) {
        this.scopes = new HashMap<>();
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }
    
    public TextTemplate put(Map<String, Object> data) {
        data.forEach ((k,v) -> put(k, v));
        return this;
    }

    @SuppressWarnings("unchecked")
    public TextTemplate put(String scope, Object data) {
        Object currentData = scopes.get(scope);
        if (currentData != null) {
            List<Object> list;
            if (currentData instanceof List) {
                list = (List<Object>) currentData;
            } else {
                list = new ArrayList<Object>();
                list.add(currentData);
                scopes.put(scope, list);
            }
            list.add(data);
        } else {
            scopes.put(scope, data);
        }
        return this;
    }

    public abstract String compile() throws IOException, TemplateException;
    public abstract void compileAndWrite(Writer writer) throws IOException, TemplateException;
}
