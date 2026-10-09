import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

// Blocks access if not logged in at all
export default function ProtectedRoute({ children }) {
  const { isAuthenticated } = useAuth();
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  return children;
}