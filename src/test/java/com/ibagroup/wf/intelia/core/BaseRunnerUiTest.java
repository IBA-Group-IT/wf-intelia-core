package com.ibagroup.wf.intelia.core;

import static org.powermock.api.mockito.PowerMockito.mockStatic;

import com.workfusion.rpa.helpers.utils.ApiUtils;
import org.junit.Before;
import org.powermock.core.classloader.annotations.PrepareForTest;

/**
 * @deprecated - Don't extend from BaseRunnerUiTest unless you really need heavy framework work under test.
 *
 */
@Deprecated
@PrepareForTest({ApiUtils.class})
public class BaseRunnerUiTest extends BaseRunnerTest {

  @Before
  public void initBaseRunner() throws Exception {
    mockStatic(ApiUtils.class);
  }
}
