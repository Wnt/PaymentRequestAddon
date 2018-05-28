package org.vaadin.jonni;

import com.vaadin.flow.component.UI;

public class PaymentRequest {

	static void isSupported(SupportedTestCallback callback) {

		UI ui = UI.getCurrent();
		ui.getElement().addEventListener("paymentRequestSupportChange", event -> {
			callback.isSupported(event.getEventData().asBoolean());
		});

		ui.getPage().executeJavaScript(""

				+ "var isSupported = false;\n"

				+ "if(window.PaymentRequest) {\n"

				+ "  isSupported = true;\n"

				+ "}\n"

				+ "var event = new CustomEvent('paymentRequestSupportChange', { detail: isSupported });\n"

				+ "$0.dispatchEvent(event)\n", ui);
	}

	interface SupportedTestCallback {
		void isSupported(boolean isSupported);
	}
}
