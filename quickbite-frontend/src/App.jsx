import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { CartProvider } from './context/CartContext';
import ProtectedRoute from './components/ProtectedRoute';
import RoleBasedRoute from './components/RoleBasedRoute';
import MainLayout from './layouts/MainLayout';

import Home from './pages/Home';
import Login from './pages/Login';
import Register from './pages/Register';
import ForgotPassword from './pages/ForgotPassword';
import ResetPassword from './pages/ResetPassword';
import Restaurants from './pages/Restaurants';
import RestaurantDetails from './pages/RestaurantDetails';
import SearchDishes from './pages/SearchDishes';
import Cart from './pages/Cart';
import Addresses from './pages/Addresses';
import Checkout from './pages/Checkout';
import Orders from './pages/Orders';
import OrderDetails from './pages/OrderDetails';
import OwnerDashboard from './pages/OwnerDashboard';
import ManageMenu from './pages/ManageMenu';
import RestaurantOrders from './pages/RestaurantOrders';
import Profile from './pages/Profile';
import Notifications from './pages/Notifications';
import NotFound from './pages/NotFound';
import AdminDashboard from './pages/admin/AdminDashboard';
import AdminUsers from './pages/admin/AdminUsers';
import AdminRestaurants from './pages/admin/AdminRestaurants';
import AdminOrders from './pages/admin/AdminOrders';
import AdminCoupons from './pages/admin/AdminCoupons';
import AdminCategories from './pages/admin/AdminCategories';
import AdminReports from './pages/admin/AdminReports';

const customer = (el) => <RoleBasedRoute allowedRoles={['CUSTOMER']}>{el}</RoleBasedRoute>;
const owner = (el) => <RoleBasedRoute allowedRoles={['RESTAURANT_OWNER']}>{el}</RoleBasedRoute>;
const admin = (el) => <RoleBasedRoute allowedRoles={['ADMIN']}>{el}</RoleBasedRoute>;

function App() {
  return (
    <AuthProvider>
      <CartProvider>
        <BrowserRouter>
          <MainLayout>
            <Routes>
              <Route path="/" element={<Home />} />
              <Route path="/login" element={<Login />} />
              <Route path="/register" element={<Register />} />
              <Route path="/forgot-password" element={<ForgotPassword />} />
              <Route path="/reset-password" element={<ResetPassword />} />
              <Route path="/restaurants" element={<Restaurants />} />
              <Route path="/restaurants/:id" element={<RestaurantDetails />} />
              <Route path="/search" element={<SearchDishes />} />

              <Route path="/cart" element={customer(<Cart />)} />
              <Route path="/addresses" element={customer(<Addresses />)} />
              <Route path="/checkout" element={customer(<Checkout />)} />
              <Route path="/orders" element={customer(<Orders />)} />
              <Route path="/orders/:id" element={<ProtectedRoute><OrderDetails /></ProtectedRoute>} />
              <Route path="/profile" element={<ProtectedRoute><Profile /></ProtectedRoute>} />
              <Route path="/notifications" element={<ProtectedRoute><Notifications /></ProtectedRoute>} />

              <Route path="/owner" element={owner(<OwnerDashboard />)} />
              <Route path="/owner/restaurants/:id/menu" element={owner(<ManageMenu />)} />
              <Route path="/owner/restaurants/:id/orders" element={owner(<RestaurantOrders />)} />

              <Route path="/admin" element={admin(<AdminDashboard />)} />
              <Route path="/admin/orders" element={admin(<AdminOrders />)} />
              <Route path="/admin/restaurants" element={admin(<AdminRestaurants />)} />
              <Route path="/admin/users" element={admin(<AdminUsers />)} />
              <Route path="/admin/coupons" element={admin(<AdminCoupons />)} />
              <Route path="/admin/categories" element={admin(<AdminCategories />)} />
              <Route path="/admin/reports" element={admin(<AdminReports />)} />

              <Route path="*" element={<NotFound />} />
            </Routes>
          </MainLayout>
        </BrowserRouter>
      </CartProvider>
    </AuthProvider>
  );
}

export default App;