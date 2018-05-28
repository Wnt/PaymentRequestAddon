package org.vaadin.jonni;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.Route;

@Route("")
public class DemoView extends Div {

    public DemoView() {
        PaymentRequest paymentRequest = new PaymentRequest();
		add(paymentRequest);
		paymentRequest.isSupported(isSupported -> Notification.show("Supported: " + isSupported));
    }
}
