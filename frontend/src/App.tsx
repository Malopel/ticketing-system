import {Route, Routes} from 'react-router-dom';

import HomePage from './customer/pages/HomePage';
import ConcertPage from './customer/pages/ConcertPage';
import CheckoutPage from './customer/pages/CheckoutPage';
import FakePaymentPage from './customer/pages/FakePaymentPage';
import OrderStatusPage from './customer/pages/OrderStatusPage';
import ImpressumPage from './customer/pages/ImpressumPage';
import DatenschutzPage from './customer/pages/DatenschutzPage';
import PublicLayout from './customer/layout/PublicLayout';

import AdminApp from './admin/AdminApp';
import CountdownPage from "./customer/pages/CountdownPage.tsx";

function App() {
    return (
        <Routes>
            <Route element={<PublicLayout/>}>
                <Route path="/" element={<CountdownPage/>}/>

                <Route path="/homepage" element={<HomePage/>}/>

                <Route
                    path="/concerts/:concertId"
                    element={<ConcertPage/>}
                />

                <Route
                    path="/concerts/:concertId/checkout"
                    element={<CheckoutPage/>}
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
                    path="/impressum"
                    element={<ImpressumPage/>}
                />

                <Route
                    path="/datenschutz"
                    element={<DatenschutzPage/>}
                />
            </Route>

            <Route
                path="/admin/*"
                element={<AdminApp/>}
            />
        </Routes>
    );
}

export default App;
