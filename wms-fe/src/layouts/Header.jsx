import { Menu, User } from 'lucide-react';
import { useLocation } from 'react-router-dom';
import { useAuth } from '../auth/useAuth';
import NotificationBell from '../components/NotificationBell';
import './Header.css';

const Header = ({ onMenu }) => {
  const location = useLocation();
  const { user, roles } = useAuth();
  const pathName = location.pathname.split('/')[1] || 'dashboard';
  const title = pathName.charAt(0).toUpperCase() + pathName.slice(1);

  return (
    <header className="main-header">
      <div className="header-left">
        <button className="menu-btn" onClick={onMenu} aria-label="Open navigation"><Menu size={20} /></button>
        <h2 className="header-title">{title}</h2>
      </div>
      
      <div className="header-right">
        <NotificationBell />
        <div className="user-profile">
          <div className="avatar">
            <User size={18} />
          </div>
          <div className="user-copy">
            <span className="username">{user?.fullname || user?.username}</span>
            <span className="user-role">{roles.join(', ')}</span>
          </div>
        </div>
      </div>
    </header>
  );
};

export default Header;
