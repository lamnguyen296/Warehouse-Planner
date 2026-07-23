import { useState } from 'react';
import { Outlet } from 'react-router-dom';
import Sidebar from './Sidebar';
import Header from './Header';

const MainLayout = () => {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  return (
    <div className="app-shell">
      <Sidebar open={sidebarOpen} onNavigate={() => setSidebarOpen(false)} />
      {sidebarOpen && <button className="sidebar-backdrop" aria-label="Close navigation" onClick={() => setSidebarOpen(false)} />}
      <main className="app-main">
        <Header onMenu={() => setSidebarOpen(true)} />
        <div className="page-content">
          <Outlet />
        </div>
      </main>
    </div>
  );
};

export default MainLayout;
