import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

// Blocks access if logged in but wrong role (e.g. CUSTOMER trying to view /admin)
export default function RoleBasedRoute({ allowedRoles, children }) {
  const { user, isAuthenticated } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  if (!allowedRoles.includes(user.role)) {
    return <Navigate to="/" replace />;
  }
  return children;
}