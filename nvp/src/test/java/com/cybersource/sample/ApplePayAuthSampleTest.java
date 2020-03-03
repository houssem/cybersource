package com.cybersource.sample;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;


public class ApplePayAuthSampleTest
{
	private static final Logger log = LoggerFactory.getLogger(ApplePayAuthSampleTest.class);
	private static final SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");

	@Test
	public void shouldAcceptAuthorize() throws Exception {

		String merchantReferenceCode = df.format(new Date());
		ApplePayAuthSample.runAuth(merchantReferenceCode);
	}

}
