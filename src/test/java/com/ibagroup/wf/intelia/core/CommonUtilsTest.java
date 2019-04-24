package com.ibagroup.wf.intelia.core;

import java.text.ParseException;

import org.junit.Assert;
import org.junit.Test;

import com.ibagroup.wf.intelia.core.CommonUtils;

public class CommonUtilsTest {

    @Test
    public void testNormalizeDatesForIP() {
        try {
            String[] dates = CommonUtils.normalizeDatesForIP("21.01.2017", "05.03.2017", 3);
            String[] expecteds = {"2016-10-21", "2017-03-05"};
            Assert.assertArrayEquals(expecteds, dates);

            dates = CommonUtils.normalizeDatesForIP("21.02.2017", "05.03.2017", 3);
            String[] expecteds1 = {"2016-11-21", "2017-03-05"};
            Assert.assertArrayEquals(expecteds1, dates);

            dates = CommonUtils.normalizeDatesForIP("21.03.2017", "05.03.2017", 3);
            String[] expecteds2 = {"2016-12-21", "2017-03-05"};
            Assert.assertArrayEquals(expecteds2, dates);
        
            dates = CommonUtils.normalizeDatesForIP("21.11.2017", "05.03.2017", 3);
            String[] expecteds3 = {"2017-08-21", "2017-03-05"};
            Assert.assertArrayEquals(expecteds3, dates);

            dates = CommonUtils.normalizeDatesForIP("21.11.2017", "05.03.2017", 6);
            String[] expecteds4 = {"2017-05-21", "2017-03-05"};
            Assert.assertArrayEquals(expecteds4, dates);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testNormalizeDatesForATMX() {
        String[] dates = CommonUtils.normalizeDatesForATMX("21.02.2017", "21.02.2017", 2);
        String[] expecteds1 = {"JAN2017", "FEB2017"};
        Assert.assertArrayEquals(expecteds1, dates);

        dates = CommonUtils.normalizeDatesForATMX("21.02.2017", "02.03.2017", 3);
        String[] expecteds2 = {"DEC2016", "JAN2017", "FEB2017", "MAR2017"};
        Assert.assertArrayEquals(expecteds2, dates);

        dates = CommonUtils.normalizeDatesForATMX("21.02.2017", "02.03.2017", 7);
        String[] expecteds3 = {"AUG2016", "SEP2016", "OCT2016", "NOV2016", "DEC2016", "JAN2017", "FEB2017", "MAR2017"};
        Assert.assertArrayEquals(expecteds3, dates);

//        dates = CommonUtils.normalizeDatesForATMX("21.02.2017", "02.02.2017", 15);
//        String[] expecteds4 = {"DEC2015", "JAN2016", "FEB2016", "MAR2016", "APR2016", "MAY2016", "JUN2016", "JUL2016", "AUG2016", "SEP2016", "OCT2016", "NOV2016", "DEC2016", "JAN2017", "FEB2017"};
//        Assert.assertArrayEquals(expecteds4, dates);
    }
}
