package com.podbox.parsers;

import com.google.common.collect.ImmutableList;
import com.podbox.parsers.ThymeleafUrlParser.InvalidThymeleafUrl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

/**
 * @since 18/08/15.
 */
@RunWith(Parameterized.class)
public class ThymeleafUrlParserTest {

    private final String testValue;
    private final String expectedResult;
    private final boolean shouldFail;

    @Parameterized.Parameters(name = "{index}: {0} should {1}")
    public static Collection<String[]> data() {
        return new ImmutableList.Builder<String[]>()
                .add(new String[]{"@{http://www.thymeleaf/documentation.html}", "http://www.thymeleaf/documentation.html"})
                .add(new String[]{"@{/order/list}", "/order/list"})
                .add(new String[]{"@{~/billing-app/showDetails.htm}", "/billing-app/showDetails.htm"})
                .add(new String[]{"@{//scriptserver.example.net/myscript.js}", "/scriptserver.example.net/myscript.js"})
                .add(new String[]{"@{/order/details(id=3,action='show_all')}", "/order/details"})
                .add(new String[]{"@{/home#all_info(action='show')}", "/home"})
                .add(new String[]{"@{/sync/index/css/mapping.css(v=${buildNumber})}", "/sync/index/css/mapping.css"})
                .add(new String[]{"@{#{orders.details.localized_url}(id=${order.id})}", "FAIL"}) //This one should fail
                .build();
    }

    public ThymeleafUrlParserTest(String testValue, String expectedResult) {
        this.testValue = testValue;
        this.expectedResult = expectedResult;

        this.shouldFail = this.expectedResult.equals("FAIL");
    }

    @Test
    public void testParseCorrectly() throws Exception {
        try {
            String result = ThymeleafUrlParser.extractUrl(this.testValue);
            Assert.assertEquals("Extracted URL is not correct", this.expectedResult, result);
        } catch (InvalidThymeleafUrl e) {
            if (!this.shouldFail) {
                throw e;
            }
        }
    }
}
