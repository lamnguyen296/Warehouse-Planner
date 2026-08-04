import { NavLink } from 'react-router-dom';
import { LayoutDashboard, Warehouse, MapPin, Package, ListTree, Database, LogOut, ArrowRightLeft, PenTool, ClipboardList, Recycle, Users } from 'lucide-react';
import { useAuth } from '../auth/useAuth';
import './Sidebar.css';
import { PERMISSIONS as P } from '../auth/permissions';

const navItems = [
  { path: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { path: '/users', label: 'Users', icon: Users, permissions: [P.SECURITY_MANAGE] },
  { path: '/warehouse', label: 'Warehouses', icon: Warehouse, permissions: [P.MASTER_DATA_READ, P.MASTER_DATA_MANAGE] },
  { path: '/location', label: 'Locations', icon: MapPin, permissions: [P.MASTER_DATA_READ, P.MASTER_DATA_MANAGE] },
  { path: '/item', label: 'Items', icon: Package, permissions: [P.MASTER_DATA_READ, P.MASTER_DATA_MANAGE] },
  { path: '/bom', label: 'BOM', icon: ListTree, permissions: [P.MASTER_DATA_READ, P.MASTER_DATA_MANAGE] },
  { path: '/inventory', label: 'Inventory', icon: Database, permissions: [P.INVENTORY_READ] },
  { path: '/workshop', label: 'Workshop Requests', icon: PenTool, permissions: [P.WORKSHOP_REQUEST_READ_OWN, P.WORKSHOP_REQUEST_READ_ALL] },
  { path: '/planning', label: 'Planning', icon: ClipboardList, permissions: [P.PLANNING_READ] },
  { path: '/purchase', label: 'Purchase Requests', icon: ArrowRightLeft, permissions: [P.PURCHASE_READ] },
  { path: '/recycle', label: 'Recycle Orders', icon: Recycle, permissions: [P.RECYCLE_READ] },
  { path: '/assembly', label: 'Assembly Orders', icon: Package, permissions: [P.ASSEMBLY_READ] },
  { path: '/transfer', label: 'Transfer & Issue', icon: ArrowRightLeft, permissions: [P.TRANSFER_READ] },
];

const Sidebar = ({ open, onNavigate }) => {
  const { hasPermission, logout } = useAuth();
  const visibleItems = navItems.filter((item) => !item.permissions || hasPermission(...item.permissions));

  const handleLogout = async () => {
    await logout();
    window.location.assign('/login');
  };

  return (
    <aside className={`sidebar ${open ? 'open' : ''}`}>
      <div className="sidebar-header">
        <div className="logo-icon">W</div>
        <span className="logo-text">WMS Pro</span>
      </div>
      <nav className="sidebar-nav">
        {visibleItems.map((item) => {
          const Icon = item.icon;
          return (
            <NavLink
              key={item.path}
              to={item.path}
              className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
              onClick={onNavigate}
            >
              <Icon size={20} className="nav-icon" />
              <span>{item.label}</span>
            </NavLink>
          );
        })}
      </nav>
      <div className="sidebar-footer">
        <button className="nav-item logout-btn" onClick={handleLogout}>
          <LogOut size={20} className="nav-icon" />
          <span>Logout</span>
        </button>
      </div>
    </aside>
  );
};

export default Sidebar;
