package za.co.standardbank.workfusion.rpa;

import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.whenNew;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.reflect.Whitebox;

import com.ibagroup.wf.intelia.core.MachineTask;
import com.ibagroup.wf.intelia.core.config.ConfigurationManager;
import com.ibagroup.wf.intelia.core.config.MapConfiguration;
import com.ibagroup.wf.intelia.core.datastore.DataStoreAccess;
import com.ibagroup.wf.intelia.core.exceptions.ExceptionHandler;
import com.ibagroup.wf.intelia.core.metadata.MetadataManager;
import com.ibagroup.wf.intelia.core.mis.IRobotLogger;
import com.ibagroup.wf.intelia.core.robots.RobotProtocol;
import com.ibagroup.wf.intelia.core.robots.factory.MachineTaskRobotFactory;
import com.ibagroup.wf.intelia.core.robots.factory.RobotsFactory;
import com.ibagroup.wf.intelia.core.robots.factory.RobotsFactoryBuilder;

@PrepareForTest({MachineTask.class, MachineTaskRobotFactory.class})
public class BaseRunnerTest extends RpaBaseTest {

    public BaseRunnerTest() {
        super();
    }

    @Mock
    private MachineTask machineTask;

    private ConfigurationManager cfg;
    private ExceptionHandler exHandler;
    private MetadataManager activityMgr;
    private RobotsFactory robotsFactory;
    private IRobotLogger robotLogger;

    @Before
    public void initBase() throws Exception {
        Whitebox.setInternalState(DataStoreAccess.class, getDdssFactory());
        Whitebox.setInternalState(DataStoreAccess.class, getRdssFactory());

        whenNew(MachineTask.class).withAnyArguments().thenReturn(machineTask);
    }

    protected <T extends RobotProtocol> T wrapRunner(T runner) {
        T _runner = spy(runner);
        doReturn(true).when(_runner).storeCurrentMetadata();
        return _runner;
    }

    public ConfigurationManager getCfg() {
        if (null == cfg) {
            cfg = new MapConfiguration(getConfigValues());
        }
        return cfg;
    }

    public Map<String, String> getConfigValues() {
        Map<String, String> inputValues = new HashMap<>();
        return inputValues;
    }

    public ExceptionHandler getExLogger() {
        if (null == exHandler) {
            exHandler = Mockito.mock(ExceptionHandler.class);
        }
        return exHandler;
    }

    public MetadataManager getActivityMgr() {
        if (null == activityMgr) {
            activityMgr = Mockito.mock(MetadataManager.class);
        }
        return activityMgr;
    }


    public RobotsFactory getRunnersFactory() {
        if (null == robotsFactory) {
            robotsFactory = Mockito.spy(new RobotsFactoryBuilder().setActivityMgr(getActivityMgr()).setExHandler(getExLogger()).setCfg(getCfg()).setRobotLogger(getRobotLogger())
                    .setBinding(getBinding()).build());
        }
        return robotsFactory;
    }

    public IRobotLogger getRobotLogger() {
        if (null == robotLogger) {
            robotLogger = Mockito.mock(IRobotLogger.class);
        }
        return robotLogger;
    }
}
