package org.vaadin.jonni;

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

		ui.getPage().executeJavaScript(""

				+ "var isSupported = false;\n"

				+ "if (window.PaymentRequest) {\n"

				+ "  isSupported = true;\n"

				+ "}\n"

				+ "var event = new CustomEvent('paymentRequestSupportReport', { detail: isSupported });\n"

				+ "$0.dispatchEvent(event)\n", ui);
	}

	private static final class PaymentRequestSupportReportListener implements DomEventListener {
		private final SupportedTestCallback callback;
		private DomListenerRegistration registration;

		private PaymentRequestSupportReportListener(SupportedTestCallback callback) {
			this.callback = callback;
		}

		@Override
		public void handleEvent(DomEvent event) {
			callback.isSupported(event.getEventData().asBoolean());
			registration.remove();
		}

		public void setRegistration(DomListenerRegistration registration) {
			this.registration = registration;
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
			JsonObject eventData = event.getEventData();
			PaymentResponse paymentResponse = new PaymentResponse(eventData, target);
			paymentResponseCallback.onPaymentResponse(paymentResponse);
		});
		ui.getPage().executeJavaScript(""

				+ "//debugger;\n"

				+ "$0.addEventListener('click', () => {\n"

				+ "var supportedPaymentMethods = JSON.parse($1);\n"

				+ "var paymentDetails = JSON.parse($2);\n"

				+ "var paymentRequest = new PaymentRequest(\n"

				+ "  supportedPaymentMethods,\n"

				+ "  paymentDetails\n"

				+ ");"

				+ "paymentRequest.show()\n"

				+ "  .then((paymentResponse) => {"

				// Store into target element so that we can call paymentResponse.complete()
				// later on
				+ "    $0.lastPaymentResponse = paymentResponse;"

				+ "    var event = new CustomEvent('paymentRequestSuccess', detail: paymentResponse);\n"

				+ "    $0.dispatchEvent(event);\n"

				+ "  })\n"

				+ "  .catch((err) => {\n"

				+ "    var event = new CustomEvent('paymentRequestFailure', detail: err);\n"

				+ "    $0.dispatchEvent(event);\n"

				+ "  });\n"

				+ "});\n"

				+ "", element, supportedPaymentMethods.toJson(), paymentDetails.toJson());
	}
}
