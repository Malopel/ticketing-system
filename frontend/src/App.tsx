import {Route, Routes} from 'react-router-dom';

import HomePage from './pages/HomePage';
import ConcertPage from './pages/ConcertPage';
import CheckoutPage from './pages/CheckoutPage';
import FakePaymentPage from './pages/FakePaymentPage';
import OrderStatusPage from './pages/OrderStatusPage';

function App() {
    return (
        <Routes>
            <Route path="/" element={<HomePage/>}/>

            <Route
                path="/concerts/:concertId"
                element={<ConcertPage/>}
            />

            <Route
                path="/concerts/:concertId/checkout"
                element={<CheckoutPage />}
            />

            <Route
                path="/orders/:orderId"
                element={<OrderStatusPage/>}
            />

            <Route
                path="/fake-payment/:providerPaymentId"
                element={<FakePaymentPage/>}
            />
        </Routes>
    );
}

export default App;