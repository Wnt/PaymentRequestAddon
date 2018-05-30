$0.addEventListener('click', () => {
  var supportedPaymentMethods = JSON.parse($1);
  var paymentDetails = JSON.parse($2);
  var paymentRequest = new PaymentRequest(
    supportedPaymentMethods,
    paymentDetails
  );
  paymentRequest.show()
    .then((paymentResponse) => {
      $0.lastPaymentResponse = paymentResponse;
      console.log(paymentResponse);
      //debugger;
      var event = new CustomEvent('paymentRequestSuccess', { detail: paymentResponse });
      $0.dispatchEvent(event);
    })
    .catch((err) => {
      var event = new CustomEvent('paymentRequestFailure', { detail: err });
      $0.dispatchEvent(event);
    });
});
