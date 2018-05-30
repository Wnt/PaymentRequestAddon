var isSupported = false;
if (window.PaymentRequest) {
  isSupported = true;
}
var event = new CustomEvent('paymentRequestSupportReport', { detail: isSupported });
$0.dispatchEvent(event);
