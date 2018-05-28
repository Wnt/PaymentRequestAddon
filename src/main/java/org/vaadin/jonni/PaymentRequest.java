package org.vaadin.jonni;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;

@Tag("div")
public class PaymentRequest extends Component {

	private SupportedTestCallback isSupportedCallback;

	void isSupported(SupportedTestCallback callback) {
		this.isSupportedCallback = callback;

		UI.getCurrent().getPage().executeJavaScript(""

				+ "debugger;\n"

				+ "if(window.PaymentRequest) {\n"

				+ "  $0.$server.isSupportedCallback(true);\n"

				+ "} else {\n"

				+ "  $0.$server.isSupportedCallback(false);\n"

				+ "}", this);
	}

	@ClientCallable
	void isSupportedCallback(boolean isSupported) {
		this.isSupportedCallback.isSupported(isSupported);
	}
	
	interface SupportedTestCallback {
		void isSupported(boolean isSupported);
	}
}
