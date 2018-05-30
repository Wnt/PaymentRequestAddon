package org.vaadin.jonni;

import java.io.IOException;
import java.nio.charset.Charset;

import com.google.common.io.Resources;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.dom.DomEventListener;
import com.vaadin.flow.dom.DomListenerRegistration;
import com.vaadin.flow.dom.Element;

import elemental.json.JsonArray;
import elemental.json.JsonObject;

public class PaymentRequest {

	private JsonArray supportedPaymentMethods;
	private JsonObject paymentDetails;
	private PaymentResponseCallback paymentResponseCallback;

	public PaymentRequest(JsonArray supportedPaymentMethods, JsonObject paymentDetails) {
		this.supportedPaymentMethods = supportedPaymentMethods;
		this.paymentDetails = paymentDetails;
	}

	static void queryIsSupported(SupportedTestCallback callback) {
		UI ui = UI.getCurrent();
		Element element = ui.getElement();
		PaymentRequestSupportReportListener listener = new PaymentRequestSupportReportListener(callback);
		final DomListenerRegistration registration = element.addEventListener("paymentRequestSupportReport", listener);
		listener.setRegistration(registration);
		String js = loadJavascript("support-test.js");

		ui.getPage().executeJavaScript(js, ui);
	}

	private static String loadJavascript(String jsFilename) {
		String js = null;
		try {
			js = Resources.toString(Resources.getResource("org/vaadin/jonni/" + jsFilename), Charset.forName("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Could not read " + jsFilename, e);
		}
		return js;
	}

	private static final class PaymentRequestSupportReportListener implements DomEventListener {
		private final SupportedTestCallback callback;
		private DomListenerRegistration registration;

		private PaymentRequestSupportReportListener(SupportedTestCallback callback) {
			this.callback = callback;
		}

		@Override
		public void handleEvent(DomEvent event) {
			JsonObject eventData = event.getEventData();
			boolean isSupported = eventData.getBoolean("event.detail");
			callback.isSupported(isSupported);
			registration.remove();
		}

		public void setRegistration(DomListenerRegistration registration) {
			this.registration = registration;
			registration.addEventData("event.detail");
		}
	}

	interface SupportedTestCallback {
		void isSupported(boolean isSupported);
	}

	interface PaymentResponseCallback {
		void onPaymentResponse(PaymentResponse paymentResponse);
	}

	public class PaymentResponse {

		private final JsonObject eventData;
		private final HasElement target;

		public PaymentResponse(JsonObject eventData, HasElement target) {
			this.eventData = eventData;
			this.target = target;
		}

		public void complete() {
			UI ui = UI.getCurrent();
			Element element = target.getElement();
			ui.getPage().executeJavaScript(""

					+ "$0.lastPaymentResponse.complete();\n"

					+ "", element);
		}

		public JsonObject getEventData() {
			return eventData;
		}

	}

	public void setPaymentResponseCallback(PaymentResponseCallback paymentResponseCallback) {
		this.paymentResponseCallback = paymentResponseCallback;

	}

	public void install(HasElement target) {
		UI ui = UI.getCurrent();
		Element element = target.getElement();
		final DomListenerRegistration registration = element.addEventListener("paymentRequestSuccess", event -> {
			JsonObject eventData = event.getEventData().getObject("event.detail");
			PaymentResponse paymentResponse = new PaymentResponse(eventData, target);
			paymentResponseCallback.onPaymentResponse(paymentResponse);
		});
		registration.addEventData("event.detail");

		String js = loadJavascript("install.js");

		ui.getPage().executeJavaScript(js, element, supportedPaymentMethods.toJson(), paymentDetails.toJson());
	}
}
