import { useCallback, useEffect, useState } from 'react';
import { Boxes, ClipboardList, PackageCheck, Warehouse } from 'lucide-react';
import { useAuth } from '../../auth/useAuth';
import PageHeader from '../../components/PageHeader';
import inventoryService from '../../services/inventoryService';
import planningService from '../../services/planningService';
import warehouseService from '../../services/warehouseService';
import workshopService from '../../services/workshopService';
import { PERMISSIONS as P } from '../../auth/permissions';

const Dashboard = () => {
  const { user, hasPermission } = useAuth();
  const [stats, setStats] = useState({ warehouses: 0, openRequests: 0, executingPlans: 0, unavailableStock: 0 });

  const load = useCallback(async () => {
    const canReadMasterData = hasPermission(P.MASTER_DATA_READ);
    const canReadWorkshop = hasPermission(P.WORKSHOP_REQUEST_READ_OWN, P.WORKSHOP_REQUEST_READ_ALL);
    const canReadInventory = hasPermission(P.INVENTORY_READ);
    const canReadPlanning = hasPermission(P.PLANNING_READ);
    const [warehouses, requests, plannings, inventory] = await Promise.all([
      canReadMasterData ? warehouseService.getAll() : Promise.resolve([]),
      canReadWorkshop ? workshopService.getAll() : Promise.resolve([]),
      canReadPlanning ? planningService.getAll() : Promise.resolve([]),
      canReadInventory ? inventoryService.getAll() : Promise.resolve([]),
    ]);
    setStats({
      warehouses: warehouses?.filter((warehouse) => warehouse.status === 'ACTIVE').length || 0,
      openRequests: requests?.filter((request) => !['COMPLETED', 'CANCELLED'].includes(request.status)).length || 0,
      executingPlans: plannings?.filter((planning) => planning.status === 'EXECUTING').length || 0,
      unavailableStock: inventory?.filter((stock) => stock.availableQuantity === 0).length || 0,
    });
  }, [hasPermission]);

  useEffect(() => { load(); }, [load]);

  const metrics = [
    ...(hasPermission(P.MASTER_DATA_READ) ? [{ label: 'Active warehouses', value: stats.warehouses, icon: Warehouse }] : []),
    ...(hasPermission(P.WORKSHOP_REQUEST_READ_OWN, P.WORKSHOP_REQUEST_READ_ALL) ? [{ label: 'Open workshop requests', value: stats.openRequests, icon: ClipboardList }] : []),
    ...(hasPermission(P.PLANNING_READ) ? [{ label: 'Executing plannings', value: stats.executingPlans, icon: PackageCheck }] : []),
    ...(hasPermission(P.INVENTORY_READ) ? [{ label: 'Zero available stock', value: stats.unavailableStock, icon: Boxes }] : []),
  ];

  return <div><PageHeader title={`Welcome, ${user?.fullname || user?.username}`} subtitle="Operational overview for the current warehouse workflow" /><div className="metric-grid">{metrics.map(({ label, value, icon: Icon }) => <div className="metric" key={label}><div className="row-actions" style={{ justifyContent: 'space-between' }}><span className="metric-label">{label}</span><Icon size={19} color="var(--primary)" /></div><div className="metric-value">{value}</div></div>)}</div></div>;
};

export default Dashboard;
