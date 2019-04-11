package com.ibagroup.wf.intelia.systems;

public interface SameRequestToService<REQ> extends RequestPreparer<REQ, REQ> {

    default REQ prepareRequest(REQ response) {
        return response;
    }

}
