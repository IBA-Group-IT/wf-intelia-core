package za.co.standardbank.workfusion.rpa;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.mockito.Answers;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.webharvest.utils.SystemUtilities;
import com.freedomoss.crowdcontrol.webharvest.HitSubmissionDataItemDto;
import com.freedomoss.crowdcontrol.webharvest.HitSubmissionDataItemValueDto;
import com.freedomoss.crowdcontrol.webharvest.WebHarvestTaskItem;
import com.freedomoss.crowdcontrol.webharvest.plugin.datastore.service.DatabaseDataStoreServiceFactory;
import com.freedomoss.crowdcontrol.webharvest.plugin.datastore.service.RemoteDataStoreServiceFactory;
import com.ibagroup.wf.intelia.core.datastore.DataStoreAccess;
import com.workfusion.utils.security.Credentials;
import groovy.lang.Binding;

@SuppressStaticInitializationFor({"com.ibagroup.wf.intelia.core.datastore.DataStoreAccess",
        "com.freedomoss.crowdcontrol.webharvest.plugin.datastore.service.RemoteDataStoreServiceFactory"})
@PrepareForTest({DataStoreAccess.class, Method.class})
public class RpaBaseTest {

    @Mock(answer = Answers.RETURNS_MOCKS)
    private DatabaseDataStoreServiceFactory ddssFactory;
    @Mock(answer = Answers.RETURNS_MOCKS)
    private RemoteDataStoreServiceFactory rdssFactory;
    @Mock
    private SystemUtilities sys;
    @Mock(answer = Answers.RETURNS_MOCKS)
    private Credentials creds;
    @Mock(answer = Answers.RETURNS_MOCKS)
    private WebHarvestTaskItem item;

    private HitSubmissionDataItemDto hitSubmissionDataItemDto;

    private Binding binding;

    public RpaBaseTest() {
        super();
    }

    public Binding getBinding() {
        if (null == binding) {
            binding = new Binding(getInputValues());
            binding.setVariable("sys", sys);
            binding.setVariable("userInternalCredentials", creds);
            binding.setVariable("item", item);
            binding.setVariable("hit_submission_data_item", getHitSubmissionDataItemDto());
        }
        return binding;
    }

    public HitSubmissionDataItemDto getHitSubmissionDataItemDto() {
        if (null == hitSubmissionDataItemDto) {
            hitSubmissionDataItemDto = new HitSubmissionDataItemDto();
            hitSubmissionDataItemDto.setItemValueList(getInputValues().entrySet().stream().map((entry) -> {
                HitSubmissionDataItemValueDto item = new HitSubmissionDataItemValueDto();
                item.setName(entry.getKey());
                item.setValue(entry.getValue());
                return item;
            }).collect(Collectors.toList()));
        }
        return hitSubmissionDataItemDto;
    }

    public DatabaseDataStoreServiceFactory getDdssFactory() {
        return ddssFactory;
    }

    public RemoteDataStoreServiceFactory getRdssFactory() {
        return rdssFactory;
    }

    public Map<String, String> getInputValues() {
        HashMap<String, String> inputValues = new HashMap<>();
        return inputValues;
    }

}
