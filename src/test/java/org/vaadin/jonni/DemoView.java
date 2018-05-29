package org.vaadin.jonni;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.router.Route;

import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.json.impl.JreJsonFactory;

@Route("")
public class DemoView extends Div {

    private Button button;

	public DemoView() {
		PaymentRequest.queryIsSupported(isSupported -> {
			if (isSupported) {
				addPaymentRequestHandlerToButton();
			}
			else {
				button.addClickListener( click -> Notification.show("Payment collection is not supported on your browser!", 3000, Position.MIDDLE));
			}
		});
		button = new Button("Pay");
		add(button );
    }

	private void addPaymentRequestHandlerToButton() {
		JreJsonFactory jsonFactory = new JreJsonFactory();
		JsonArray supportedPaymentMethods = jsonFactory.createArray();
		JsonObject basicCard = jsonFactory.createObject();
		basicCard.put("supportedMethods", "basic-card");
		supportedPaymentMethods.set(0, basicCard);
		
		JsonObject paymentDetails = jsonFactory.createObject();
		/**
		 * total: {
            label: 'Cart (10 items)',
            amount:{
              currency: 'EUR',
              value: 1337
            }
          }
		 */
		JsonObject total = jsonFactory.createObject();
		total.put("label", "Cart (10 items)");
		JsonObject totalAmount = jsonFactory.createObject();
		totalAmount.put("currency", "EUR");
		totalAmount.put("value", "1337");
		total.put("amount", totalAmount);
		paymentDetails.put("total", total);
		
		PaymentRequest paymentRequest = new PaymentRequest(supportedPaymentMethods, paymentDetails);
		paymentRequest.setPaymentResponseCallback((paymentResponse) -> {
			paymentResponse.complete();
			Notification.show("Done");
		});
		paymentRequest.install(button);
		
	}
}
