package com.cybersource.sample;

import com.cybersource.ws.client.Client;
import com.cybersource.ws.client.ClientException;
import com.cybersource.ws.client.FaultException;
import com.cybersource.ws.client.Utility;

import java.util.*;


public class ApplePayAuthSample
{

	private static Properties props = Utility.readProperties(new String[] { "cybs.properties" });

	/**
	 * Runs Credit Card Authorization.
	 *
	 * @return the requestID.
	 */
	public static String runAuth(String merchantReferenceCode) {

		String requestID = null;

		HashMap<String, String> request = new HashMap<String, String>();

		request.put( "ccAuthService_run", "true");
		// request.put( "invoiceHeader_merchantDescriptor", "TMN Wallet Utility Bill");

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
		request.put( "billTo_country", "AE" );
		request.put( "billTo_email", "houssem.kallel@mail.com" );
		request.put( "billTo_phoneNumber", "+6622962000" );
		request.put( "billTo_ipAddress", "10.7.7.7" );

		request.put( "purchaseTotals_currency", "AED" );
		request.put( "purchaseTotals_grandTotalAmount", "50.50" );

		request.put( "encryptedPayment_descriptor", "RklEPUNPTU1PTi5BUFBMRS5JTkFQUC5QQVlNRU5U" );
		request.put( "encryptedPayment_data", "ABCDEFabcdefABCDEFabcdef0987654321234567" );
		request.put( "encryptedPayment_encoding", "Base64" );

		request.put( "card_cardType", "001" );

		request.put( "paymentSolution", "001" );

		// add more fields here per your business needs
		try {
			displayMap( "CREDIT CARD AUTHORIZATION REQUEST:", request );

			// run transaction now
			Map<String, String> reply = Client.runTransaction( request, props);

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
