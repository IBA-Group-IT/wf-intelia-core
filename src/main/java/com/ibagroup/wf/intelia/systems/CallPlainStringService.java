package com.ibagroup.wf.intelia.systems;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import org.webharvest.runtime.variables.NodeVariable;
import com.freedomoss.crowdcontrol.webharvest.plugin.httpextended.util.RequestEntry;
import com.freedomoss.crowdcontrol.webharvest.plugin.httpextended.util.web.HttpClientManagerExtended;
import com.freedomoss.crowdcontrol.webharvest.plugin.httpextended.util.web.HttpResponseWrapperExtended;

public class CallPlainStringService {

    private boolean sendXml = false;
    private boolean sendJson = false;
    private boolean applicationXml = false;

    public String callService(String endpoint, String requestStr) {
        HttpResponseWrapperExtended resp = null;
        try {
            HttpClientManagerExtended manager = new HttpClientManagerExtended();
            Set<RequestEntry<String>> headers = new HashSet<>();

            headers.add(new RequestEntry<String>("Content-Type", getContentType() + ";charset=UTF-8"));
            
            resp = manager.execute("post", Boolean.FALSE, getContentType(), endpoint, StandardCharsets.UTF_8.name(), "", "",
                    new NodeVariable(requestStr), new HashSet<>(), headers, 0, 0, 0.0);

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            os.write(resp.readBodyAsArray());
            String response = os.toString(StandardCharsets.UTF_8.name());
            resp.close();
            return response;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (null != resp) {
                resp.close();
            }
        }
    }

    public CallPlainStringService useSendXml() {
        sendXml = true;
        return this;
    }

    public CallPlainStringService useSendJson() {
        sendJson = true;
        return this;
    }
    
    public CallPlainStringService useApplicationXml() {
        applicationXml = true;
        return this;
    }


    private String getContentType() {
        if (sendXml) {
        	return "text/xml";		
        } else if (sendJson) {
            return "application/json";
        } else if (applicationXml) {
        	return "application/xml";
        }else {
            return "plain/txt";
        }
    }

}
