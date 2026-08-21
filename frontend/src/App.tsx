import {Route, Routes} from 'react-router-dom';

import HomePage from './customer/pages/HomePage';
import ConcertPage from './customer/pages/ConcertPage';
import CheckoutPage from './customer/pages/CheckoutPage';
import FakePaymentPage from './customer/pages/FakePaymentPage';
import OrderStatusPage from './customer/pages/OrderStatusPage';

import AdminApp from './admin/AdminApp';

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

            <Route
                path="/admin/*"
                element={<AdminApp/>}
            />
        </Routes>
    );
}

export default App;