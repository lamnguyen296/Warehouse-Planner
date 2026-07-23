import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from '../auth/useAuth';
import MainLayout from '../layouts/MainLayout';
import Login from '../pages/auth/Login';
import Dashboard from '../pages/dashboard/Dashboard';
import WarehouseList from '../pages/warehouse/WarehouseList';
import LocationList from '../pages/location/LocationList';
import ItemList from '../pages/item/ItemList';
import BomList from '../pages/bom/BomList';
import InventoryList from '../pages/inventory/InventoryList';
import WorkshopList from '../pages/workshop/WorkshopList';
import PlanningList from '../pages/planning/PlanningList';
import PurchaseList from '../pages/purchase/PurchaseList';
import RecycleList from '../pages/recycle/RecycleList';
import AssemblyList from '../pages/assembly/AssemblyList';
import TransferList from '../pages/transfer/TransferList';
import UserList from '../pages/user/UserList';
import { PERMISSIONS as P } from '../auth/permissions';

const ProtectedRoute = ({ children }) => {
  const { authenticated, ready } = useAuth();
  if (!ready) return <div className="app-loading">Loading session...</div>;
  if (!authenticated) return <Navigate to="/login" replace />;
  return children;
};

const PermissionRoute = ({ permissions, children }) => {
  const { hasPermission } = useAuth();
  return hasPermission(...permissions) ? children : <Navigate to="/dashboard" replace />;
};

const AppRoutes = () => {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      
      {/* Protected Routes */}
      <Route path="/" element={<ProtectedRoute><MainLayout /></ProtectedRoute>}>
        <Route index element={<Navigate to="/dashboard" replace />} />
        <Route path="dashboard" element={<Dashboard />} />
        <Route path="users" element={<PermissionRoute permissions={[P.SECURITY_MANAGE]}><UserList /></PermissionRoute>} />
        <Route path="warehouse" element={<PermissionRoute permissions={[P.MASTER_DATA_MANAGE]}><WarehouseList /></PermissionRoute>} />
        <Route path="location" element={<PermissionRoute permissions={[P.MASTER_DATA_MANAGE]}><LocationList /></PermissionRoute>} />
        <Route path="item" element={<PermissionRoute permissions={[P.MASTER_DATA_MANAGE]}><ItemList /></PermissionRoute>} />
        <Route path="bom" element={<PermissionRoute permissions={[P.MASTER_DATA_MANAGE]}><BomList /></PermissionRoute>} />
        <Route path="inventory" element={<PermissionRoute permissions={[P.INVENTORY_READ]}><InventoryList /></PermissionRoute>} />
        <Route path="workshop" element={<PermissionRoute permissions={[P.WORKSHOP_REQUEST_READ_OWN, P.WORKSHOP_REQUEST_READ_ALL]}><WorkshopList /></PermissionRoute>} />
        <Route path="planning" element={<PermissionRoute permissions={[P.PLANNING_READ]}><PlanningList /></PermissionRoute>} />
        <Route path="purchase" element={<PermissionRoute permissions={[P.PURCHASE_READ]}><PurchaseList /></PermissionRoute>} />
        <Route path="recycle" element={<PermissionRoute permissions={[P.RECYCLE_READ]}><RecycleList /></PermissionRoute>} />
        <Route path="assembly" element={<PermissionRoute permissions={[P.ASSEMBLY_READ]}><AssemblyList /></PermissionRoute>} />
        <Route path="transfer" element={<PermissionRoute permissions={[P.TRANSFER_READ]}><TransferList /></PermissionRoute>} />
      </Route>

      {/* Catch all */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
};

export default AppRoutes;
