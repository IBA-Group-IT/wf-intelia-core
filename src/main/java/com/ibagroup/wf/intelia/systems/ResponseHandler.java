package com.ibagroup.wf.intelia.systems;

public interface ResponseHandler<RESP, RESP_SRV> {

    RESP handleResponse(RESP_SRV response);

}
