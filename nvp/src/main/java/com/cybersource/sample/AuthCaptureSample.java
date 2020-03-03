package com.cybersource.sample;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.Properties;

import com.cybersource.ws.client.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sample class that demonstrates how to call Credit Card Authorization.
 */
public class AuthCaptureSample {

	private static final Logger log = LoggerFactory.getLogger(AuthCaptureSample.class);
	private static Properties props = Utility.readProperties(new String[] { "cybs.properties" });

    public static void runAuthorizeAndCapture(String merchantReferenceCode) {

	   	// read in properties file.
		log.debug("Key file : {}", props.getProperty("keyFilename"));
		log.debug("merchantReferenceCode: {}", merchantReferenceCode);

	   	// run auth
   		String requestID = runAuth(merchantReferenceCode);
   		if (requestID != null) {
	   		// if auth was successful, run capture
   			runCapture(requestID, merchantReferenceCode);
   		}
	}

    public static void runAuthorizeAndReversal(String merchantReferenceCode) {

		log.debug("Key file : {}", props.getProperty("keyFilename"));
		log.debug("merchantReferenceCode: {}", merchantReferenceCode);

	   	// run auth
   		String requestID = runAuth(merchantReferenceCode);
   		if (requestID != null) {
	   		// if auth was successful, run capture
   			runReversal(requestID, merchantReferenceCode);
   		}
	}

	/**
	 * Runs Credit Card Authorization.
	 *
	 * @return the requestID.
	 */
    public static String runAuth(String merchantReferenceCode) {

	    String requestID = null;

	   	HashMap<String, String> request = new HashMap<String, String>();

		request.put( "ccAuthService_run", "true");
		request.put( "invoiceHeader_merchantDescriptor", "TMN Wallet Utility Bill");

		// We will let the Client get the merchantID from props and insert it
		// into the request Map.

		// this is your own tracking number.  CyberSource recommends that you
		// use a unique one for each order.

		request.put( "merchantReferenceCode", merchantReferenceCode);

		request.put( "billTo_firstName", "Krungsri" );
		request.put( "billTo_lastName", "Simple" );
		request.put( "billTo_street1", "1222 Rama 3 Road, Bang Phongphang" );
		request.put( "billTo_city", "Yannawa" );
		request.put( "billTo_state", "Bangkok" );
		request.put( "billTo_postalCode", "10210" );
		request.put( "billTo_country", "TH" );
		request.put( "billTo_email", "customer@mail.com" );
		request.put( "billTo_phoneNumber", "+6622962000" );
		request.put( "billTo_ipAddress", "10.7.7.7" );

		// request.put( "shipTo_firstName", "Jane" );
		// request.put( "shipTo_lastName", "Doe" );
		// request.put( "shipTo_street1", "100 Elm Street" );
		// request.put( "shipTo_city", "San Mateo" );
		// request.put( "shipTo_state", "CA" );
		// request.put( "shipTo_postalCode", "94401" );
		// request.put( "shipTo_country", "US" );

		// request.put( "card_accountNumber", "4012001037141112" );
		request.put( "card_accountNumber", "4111111111111111" );
		request.put( "card_expirationMonth", "12" );
		request.put( "card_expirationYear", "2021" );

		request.put( "purchaseTotals_currency", "AED" );

		// there are two items in this sample
		request.put( "item_0_productName", "KFLTFDIV" );
		request.put( "item_0_productSKU", "SKU00" );
		request.put( "item_0_quantity", "100" );
		request.put( "item_0_unitPrice", "10.00" );

		request.put( "item_1_productName", "KFLTFD70" );
		request.put( "item_1_productSKU", "SKU01" );
		request.put( "item_1_quantity", "100" );
		request.put( "item_1_unitPrice", "5.72" );

		// add more fields here per your business needs

		try {
			displayMap( "CREDIT CARD AUTHORIZATION REQUEST:", request );

			// run transaction now
			Map<String, String>  reply = Client.runTransaction( request, props );

			displayMap( "CREDIT CARD AUTHORIZATION REPLY:", reply );

			// if the authorization was successful, obtain the request id
			// for the follow-on capture later.
			String decision = (String) reply.get( "decision" );
			if ("ACCEPT".equalsIgnoreCase( decision ))
			{
				requestID = (String) reply.get( "requestID" );
			}
		}
		catch (ClientException e) {
			System.out.println( e.getMessage() );
			if (e.isCritical())
			{
				handleCriticalException( e, request );
			}
		}
		catch (FaultException e) {

			System.out.println( e.getMessage() );

			if (e.isCritical()) {
				handleCriticalException( e, request );
			}
		}

		return requestID;
    }

	/**
	 * Runs Credit Card Capture.
	 *
	 * @param props			Properties object.
	 * @param authRequestID	requestID returned by a previous authorization.
	 */
    public static void runCapture(String authRequestID, String merchantReferenceCode) {

	    String requestID = null;

	   	HashMap<String, String> request = new HashMap<String, String>();

		request.put( "ccCaptureService_run", "true" );
		request.put( "invoiceHeader_merchantDescriptor", "TMN Wallet Utility Bill");

		// We will let the Client get the merchantID from props and insert it
		// into the request Map.

		// so that you can efficiently track the order in the CyberSource
		// reports and transaction search screens, you should use the same
		// merchantReferenceCode for the auth and subsequent captures and
		// credits.
		request.put( "merchantReferenceCode", merchantReferenceCode);

		// reference the requestID returned by the previous auth.
		request.put( "ccCaptureService_authRequestID", authRequestID);

		request.put( "purchaseTotals_currency", "THB" );

		// partial settlement, this sample assumes only the first item has been shipped.
		//request.put( "item_0_productName", "KFLTFDIV" );
		//request.put( "item_0_productSKU", "SKU0" );
		//request.put( "item_0_quantity", "100" );
		//request.put( "item_0_unitPrice", "10.00" );

		// full settlement
		request.put( "purchaseTotals_grandTotalAmount", "1572.00" );

		// add more fields here per your business needs

		try
		{
			displayMap( "FOLLOW-ON CAPTURE REQUEST:", request );

			// run transaction now
			Map<String, String> reply = Client.runTransaction( request, props );

			displayMap( "FOLLOW-ON CAPTURE REPLY:", reply );
		}
		catch (ClientException e)
		{
			System.out.println( e.getMessage() );
			if (e.isCritical())
			{
				handleCriticalException( e, request );
			}
		}
		catch (FaultException e)
		{
			System.out.println( e.getMessage() );
			if (e.isCritical())
			{
				handleCriticalException( e, request );
			}
		}
    }

    /**
	 * Runs Credit Card Authorization And Capture.
	 *
	 * @param props	Properties object.
	 *
	 * @return the requestID.
	 */
    public static String runSale(String merchantReferenceCode) {

    	log.debug("merchantReferenceCode: {}", merchantReferenceCode);

	    String requestID = null;

	   	HashMap<String, String> request = new HashMap<String, String>();

		request.put( "ccAuthService_run", "true");
		request.put( "ccCaptureService_run", "true");
		request.put( "invoiceHeader_merchantDescriptor", "TMN Wallet Utility Bill");

		// We will let the Client get the merchantID from props and insert it
		// into the request Map.

		// this is your own tracking number.  CyberSource recommends that you
		// use a unique one for each order.

		request.put( "merchantReferenceCode", merchantReferenceCode);

		request.put( "billTo_firstName", "Krungsri" );
		request.put( "billTo_lastName", "Simple" );
		request.put( "billTo_street1", "1222 Rama 3 Road, Bang Phongphang" );
		request.put( "billTo_city", "Yannawa" );
		request.put( "billTo_state", "Bangkok" );
		request.put( "billTo_postalCode", "10210" );
		request.put( "billTo_country", "TH" );
		request.put( "billTo_email", "customer@mail.com" );
		request.put( "billTo_phoneNumber", "+6622962000" );
		request.put( "billTo_ipAddress", "10.7.7.7" );

		request.put( "card_accountNumber", "5200000000000007" );
		request.put( "card_expirationMonth", "12" );
		request.put( "card_expirationYear", "2021" );

		request.put( "purchaseTotals_currency", "THB" );

		// there are two items in this sample
		request.put( "item_0_productName", "KFLTFDIV" );
		request.put( "item_0_productSKU", "SKU00" );
		request.put( "item_0_quantity", "100" );
		request.put( "item_0_unitPrice", "10.00" );

		// add more fields here per your business needs

		try {

			displayMap( "CREDIT CARD SALE REQUEST:", request );

			// run transaction now
			Map<String, String>  reply = Client.runTransaction( request, props );

			displayMap( "CREDIT CARD SALE REPLY:", reply );

			// if the authorization was successful, obtain the request id
			// for the follow-on capture later.
			String decision = (String) reply.get( "decision" );
			if ("ACCEPT".equalsIgnoreCase( decision )) {
				requestID = (String) reply.get( "requestID" );
			}
		}
		catch (Exception e) {

			e.printStackTrace();
			log.error("ERROR: {}", e.getMessage());

		}

		return requestID;
    }

    /**
	 * Runs Full Authorization Reversal Request
	 *
	 * @param authRequestID	requestID returned by a previous authorization.
	 * @param merchantReferenceCode
	 */
    public static void runReversal(String authRequestID, String merchantReferenceCode) {

	    String requestID = null;

	   	HashMap<String, String> request = new HashMap<String, String>();

	   	// reference the requestID returned by the previous auth.
		request.put( "ccAuthReversalService_authRequestID", authRequestID);
		request.put( "ccAuthReversalService_run", "true" );
		request.put( "merchantReferenceCode", merchantReferenceCode);

		request.put( "purchaseTotals_currency", "THB" );
		// full amount
		request.put( "purchaseTotals_grandTotalAmount", "1572.00" );

		// add more fields here per your business needs

		try
		{
			displayMap( "FOLLOW-ON REVERSAL REQUEST:", request );

			// run transaction now
			Map<String, String> reply = Client.runTransaction( request, props );

			displayMap( "FOLLOW-ON REVERSAL REPLY:", reply );
		}
		catch (ClientException e)
		{
			System.out.println( e.getMessage() );
			if (e.isCritical())
			{
				handleCriticalException( e, request );
			}
		}
		catch (FaultException e)
		{
			System.out.println( e.getMessage() );
			if (e.isCritical())
			{
				handleCriticalException( e, request );
			}
		}
    }

    /**
    *
    * Reference: Payment_Tokenization_SO_API.pdf
    * Requesting an On-Demand Transaction
    *
    */
    public static void runPaymentWithToken(String merchantReferenceCode, String tokenId) {

		log.debug("merchantReferenceCode: {}", merchantReferenceCode);
		log.debug("tokenId: {}", tokenId);

	    String requestID = null;
	   	HashMap<String, String> request = new HashMap<String, String>();

		request.put("ccAuthService_run", "true");
		request.put("ccCaptureService_run", "true");
		//request.put("ccCreditService_run", "true");

		request.put("merchantReferenceCode", merchantReferenceCode);
		request.put("recurringSubscriptionInfo_subscriptionID", tokenId);

		request.put("purchaseTotals_currency", "THB");
		request.put("purchaseTotals_grandTotalAmount", "1234.5");

		request.put("merchantDefinedData_mddField_1", "MDD#1");
		request.put("merchantDefinedData_mddField_2", "MDD#2");
		request.put("merchantDefinedData_mddField_3", "MDD#3");
		request.put("merchantDefinedData_mddField_4", "MDD#4");

		try {

			displayMap( "CREDIT CARD PAYMENT TOKEN REQUEST:", request);

			// run transaction now
			Map<String, String> reply = Client.runTransaction(request, props);
			String decision = (String) reply.get("decision");

			if ("ACCEPT".equalsIgnoreCase(decision)) {
				requestID = (String) reply.get("requestID");
			}

			displayMap( "CREDIT CARD PAYMENT TOKEN REPLY:", reply);
		}
		catch (Exception e) {

			e.printStackTrace();
			log.error("ERROR: {}", e.getMessage());

		}
	}

	public static String runConvertTransactionToCustomerProfile(String paymentRequestID, String merchantReferenceCode) {

		log.debug("paymentRequestID: {}", paymentRequestID);
		log.debug("merchantReferenceCode: {}", merchantReferenceCode);

		String subscriptionID = null;
	   	HashMap<String, String> request = new HashMap<String, String>();

	   	request.put("paySubscriptionCreateService_run", "true");
		request.put("recurringSubscriptionInfo_frequency", "on-demand");

		request.put("paySubscriptionCreateService_paymentRequestID", paymentRequestID);
		request.put("merchantReferenceCode", merchantReferenceCode);

		try {

			displayMap("FOLLOW-ON TXN TO PROFILE REQUEST:", request);

			// run transaction now
			Map<String, String> reply = Client.runTransaction(request, props);
			String decision = (String) reply.get("decision");

			if ("ACCEPT".equalsIgnoreCase(decision)) {
				subscriptionID = (String) reply.get("paySubscriptionCreateReply_subscriptionID");
			}

			log.debug("subscriptionID: {}", subscriptionID);

			displayMap("FOLLOW-ON TXN TO PROFILE REPLY:", reply);
		}
		catch (Exception e) {

			e.printStackTrace();
			log.error("ERROR: {}", e.getMessage());

		}

		return subscriptionID;
	}


	/**
	 * Displays the content of the Map object.
	 *
	 * @param header	Header text.
	 * @param map		Map object to display.
	 */
    private static void displayMap( String header, Map mapraw ) {

	    System.out.println( header );

	    TreeMap<String, String> map = new TreeMap<>(mapraw);
		StringBuffer dest = new StringBuffer();

		if (map != null && !map.isEmpty()) {
			Iterator iter = map.keySet().iterator();
			String key, val;

			while (iter.hasNext()) {
				key = (String) iter.next();
				val = (String) map.get( key );
				dest.append( key + "=" + val + "\n" );
			}
		}

		System.out.println( dest.toString() );
    }


	/**
	 * An exception is considered critical if some type of disconnect occurs
	 * between the client and server and the client can't determine whether the
	 * transaction was successful. If this happens, you might have a
	 * transaction in the CyberSource system that your order system is not
	 * aware of. Because the transaction may have been processed by
	 * CyberSource, you should not resend the transaction, but instead send the
	 * exception information and the order information (customer name, order
	 * number, etc.) to the appropriate personnel at your company to resolve
	 * the problem. They should use the information as search criteria within
	 * the CyberSource Transaction Search Screens to find the transaction and
	 * determine if it was successfully processed. If it was, you should update
	 * your order system with the transaction information. Note that this is
	 * only a recommendation; it may not apply to your business model.
	 *
	 * @param e			Critical ClientException object.
	 * @param request	Request that was sent.
	 */
	private static void handleCriticalException(ClientException e, Map request) {
		// send the exception and order information to the appropriate
		// personnel at your company using any suitable method, e.g. e-mail,
		// multicast log, etc.
	}

	/**
	 * See header comment in the other version of handleCriticalException
	 * above.
	 *
	 * @param e			Critical ClientException object.
	 * @param request	Request that was sent.
	 */
	private static void handleCriticalException(FaultException e, Map request) {
		// send the exception and order information to the appropriate
		// personnel at your company using any suitable method, e.g. e-mail,
		// multicast log, etc.
	}
}
