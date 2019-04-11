package com.ibagroup.wf.intelia.systems;

public interface RequestPreparer<REQ, REQ_SRV> {

    REQ_SRV prepareRequest(REQ requestStr);

}
