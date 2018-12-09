package com.faizal.flickrimagesearch;

import com.faizal.flickrimagesearch.common.Common;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

public class TestHomeActivity {

    @Test
    public void testImageURLMock() {
        String actual = Common.getImageURL(1, "123", "123", "123");

        // expected value
        String expected = "http://farm1.static.flickr.com/123/123_123.jpg";

        assertEquals("base url failed", expected, actual);
    }



}
