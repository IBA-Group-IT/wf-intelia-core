package com.ibagroup.wf.intelia.core.robots;

import com.ibagroup.wf.intelia.core.mis.TaskAction.Result;

public interface RobotProtocol {

    boolean storeCurrentMetadata();
    
    //void storeCurrentActionResult(Result result);
    
    void storeCurrentActionResult(Result result, String... description);
    //void storeCurrentActionDescription(Result result);

}
