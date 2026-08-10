import './App.css';
import HomePage from './pages/HomePage';
import FakePaymentPage from './pages/FakePaymentPage';

function App() {
  const fakePaymentPrefix = '/fake-payment/';

  if (window.location.pathname.startsWith(fakePaymentPrefix)) {
    const providerPaymentId =
        window.location.pathname.substring(
            fakePaymentPrefix.length,
        );

    return (
        <FakePaymentPage
            providerPaymentId={providerPaymentId}
        />
    );
  }

  return <HomePage />;
}

export default App;