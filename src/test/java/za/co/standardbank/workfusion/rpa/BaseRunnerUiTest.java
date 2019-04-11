package za.co.standardbank.workfusion.rpa;

import static org.powermock.api.mockito.PowerMockito.mockStatic;

import com.workfusion.rpa.helpers.utils.ApiUtils;
import org.junit.Before;
import org.powermock.core.classloader.annotations.PrepareForTest;

@PrepareForTest({ApiUtils.class})
public class BaseRunnerUiTest extends BaseRunnerTest {

  @Before
  public void initBaseRunner() throws Exception {
    mockStatic(ApiUtils.class);
  }
}
