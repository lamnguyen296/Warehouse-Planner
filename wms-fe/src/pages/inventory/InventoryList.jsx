import { useCallback, useEffect, useMemo, useState } from 'react';
import { ArrowRightLeft, Boxes, Eye, LockKeyhole, Plus, RefreshCw } from 'lucide-react';
import toast from 'react-hot-toast';
import { useAuth } from '../../auth/useAuth';
import Button from '../../components/Button';
import Modal from '../../components/Modal';
import PageHeader from '../../components/PageHeader';
import StatusBadge from '../../components/StatusBadge';
import Table from '../../components/Table';
import '../../components/Table.css';
import inventoryService from '../../services/inventoryService';
import itemService from '../../services/itemService';
import locationService from '../../services/locationService';
import warehouseService from '../../services/warehouseService';
import InventoryForm from './InventoryForm';
import MovementAttachments from './MovementAttachments';
import { PERMISSIONS as P } from '../../auth/permissions';

const formatLabel = (value) => value?.replaceAll('_', ' ') || 'N/A';

const formatDateTime = (value) => {
  if (!value) return 'N/A';
  return new Intl.DateTimeFormat('en-GB', {
    year: 'numeric', month: 'short', day: '2-digit', hour: '2-digit', minute: '2-digit',
  }).format(new Date(value));
};

const movementDirection = (detail) => {
  if (detail.locationFromCode && detail.locationToCode) return 'MOVE';
  if (detail.locationFromCode) return 'DEBIT';
  if (detail.locationToCode) return 'CREDIT';
  return 'ADJUST';
};

const InventoryList = () => {
  const { hasPermission } = useAuth();
  const canAdjust = hasPermission(P.INVENTORY_ADJUST);
  const [activeView, setActiveView] = useState('stock');
  const [stock, setStock] = useState([]);
  const [reservations, setReservations] = useState([]);
  const [movements, setMovements] = useState([]);
  const [warehouses, setWarehouses] = useState([]);
  const [locations, setLocations] = useState([]);
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [editing, setEditing] = useState(null);
  const [formOpen, setFormOpen] = useState(false);
  const [selectedMovement, setSelectedMovement] = useState(null);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const [inventoryData, reservationData, movementData, warehouseData, locationData, itemData] = await Promise.all([
        inventoryService.getAll(),
        inventoryService.getReservations(),
        inventoryService.getTransactions(),
        warehouseService.getAll(),
        locationService.getAll(),
        itemService.getAll(),
      ]);
      setStock(inventoryData || []);
      setReservations([...(reservationData || [])].sort((left, right) =>
        new Date(right.createdAt).getTime() - new Date(left.createdAt).getTime()));
      setMovements([...(movementData || [])].sort((left, right) =>
        new Date(right.createdAt).getTime() - new Date(left.createdAt).getTime()));
      setWarehouses(warehouseData || []);
      setLocations(locationData || []);
      setItems(itemData || []);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { load(); }, [load]);

  const itemMap = useMemo(() => new Map(items.map((item) => [item.id, item])), [items]);
  const warehouseMap = useMemo(() => new Map(warehouses.map((warehouse) => [warehouse.id, warehouse])), [warehouses]);
  const activeReservationCount = reservations.filter((reservation) => reservation.status === 'RESERVED').length;

  const openCreate = () => { setEditing(null); setFormOpen(true); };
  const openAdjust = (row) => { setEditing(row); setFormOpen(true); };

  const save = async (payload) => {
    if (editing) await inventoryService.update(editing.id, payload);
    else await inventoryService.create(payload);
    toast.success(editing ? 'Inventory adjusted' : 'Inventory created');
    setFormOpen(false);
    await load();
  };

  const stockColumns = [
    { header: 'Warehouse', accessor: 'warehouseCode', render: (row) => <strong>{warehouseMap.get(row.warehouseId)?.name || row.warehouseCode}</strong> },
    { header: 'Location', accessor: 'locationCode' },
    { header: 'Item', accessor: 'itemCode', render: (row) => <div><strong>{itemMap.get(row.itemId)?.name || row.itemCode}</strong><div className="subtle">{row.itemCode}</div></div> },
    { header: 'Total', accessor: 'totalQuantity' },
    { header: 'Reserved', accessor: 'reservedQuantity', render: (row) => <strong style={{ color: 'var(--warning)' }}>{row.reservedQuantity}</strong> },
    { header: 'Available', accessor: 'availableQuantity', render: (row) => <strong className={row.availableQuantity === 0 ? 'danger-text' : 'success-text'}>{row.availableQuantity}</strong> },
    ...(canAdjust ? [{ header: 'Action', accessor: 'action', render: (row) => <Button size="sm" variant="secondary" onClick={() => openAdjust(row)}>Adjust</Button> }] : []),
  ];

  const reservationColumns = [
    { header: 'Created', accessor: 'createdAt', render: (row) => formatDateTime(row.createdAt) },
    { header: 'Item', accessor: 'itemCode', render: (row) => <strong>{row.itemCode}</strong> },
    { header: 'Warehouse', accessor: 'warehouseCode' },
    { header: 'Quantity', accessor: 'quantity', render: (row) => <strong>{row.quantity}</strong> },
    { header: 'Status', accessor: 'status', render: (row) => <StatusBadge value={row.status} /> },
    { header: 'Business reference', accessor: 'planningDetailId', render: (row) => row.recycleOrderId
      ? `Recycle #${row.recycleOrderId}`
      : row.planningDetailId ? `Planning detail #${row.planningDetailId}` : 'N/A' },
    { header: 'Expires', accessor: 'expiredTime', render: (row) => formatDateTime(row.expiredTime) },
  ];

  const movementColumns = [
    { header: 'Transaction', accessor: 'transactionNo', render: (row) => <strong>{row.transactionNo}</strong> },
    { header: 'Type', accessor: 'transactionType', render: (row) => formatLabel(row.transactionType) },
    { header: 'Route', accessor: 'fromWarehouseCode', render: (row) => `${row.fromWarehouseCode || 'External'} -> ${row.toWarehouseCode || 'External'}` },
    { header: 'Reference', accessor: 'referenceType', render: (row) => row.referenceType ? `${row.referenceType} #${row.referenceId}` : 'N/A' },
    { header: 'Status', accessor: 'status', render: (row) => <StatusBadge value={row.status} /> },
    { header: 'Created', accessor: 'createdAt', render: (row) => formatDateTime(row.createdAt) },
    { header: 'Action', accessor: 'action', render: (row) => <Button size="sm" variant="secondary" onClick={() => setSelectedMovement(row)}><Eye size={14} /> Details</Button> },
  ];

  const views = [
    { id: 'stock', label: 'Stock', icon: Boxes, count: stock.length },
    { id: 'reservations', label: 'Reservations', icon: LockKeyhole, count: activeReservationCount },
    { id: 'movements', label: 'Movements', icon: ArrowRightLeft, count: movements.length },
  ];

  return (
    <div>
      <PageHeader
        title="Inventory"
        subtitle="Trace physical stock, planning reservations and every inventory movement"
        actions={<div className="page-actions">
          {activeView === 'stock' && canAdjust && <Button onClick={openCreate}><Plus size={16} /> Add opening stock</Button>}
          <Button variant="secondary" onClick={load} disabled={loading}><RefreshCw size={16} /> {loading ? 'Refreshing...' : 'Refresh'}</Button>
        </div>}
      />

      <div className="inventory-tabs" role="tablist" aria-label="Inventory views">
        {views.map((view) => {
          const Icon = view.icon;
          return <button
            key={view.id}
            type="button"
            role="tab"
            aria-selected={activeView === view.id}
            className={`inventory-tab ${activeView === view.id ? 'active' : ''}`}
            onClick={() => setActiveView(view.id)}
          >
            <Icon size={16} />
            <span>{view.label}</span>
            <span className="tab-count">{view.count}</span>
          </button>;
        })}
      </div>

      <div className="surface inventory-view" role="tabpanel">
        {activeView === 'stock' && <Table columns={stockColumns} data={stock} />}
        {activeView === 'reservations' && <>
          <div className="view-intro"><div><strong>Reservation history</strong><p>Active and historical allocations linked to planning details.</p></div><div className="view-stat"><span>Reserved quantity</span><strong>{reservations.filter((row) => row.status === 'RESERVED').reduce((sum, row) => sum + row.quantity, 0)}</strong></div></div>
          <Table columns={reservationColumns} data={reservations} />
        </>}
        {activeView === 'movements' && <>
          <div className="view-intro"><div><strong>Stock movement ledger</strong><p>Purchase, recycling, assembly, transfer and issue transactions.</p></div><div className="view-stat"><span>Completed</span><strong>{movements.filter((row) => row.status === 'COMPLETED').length}</strong></div></div>
          <Table columns={movementColumns} data={movements} />
        </>}
      </div>

      <Modal isOpen={formOpen} onClose={() => setFormOpen(false)} title={editing ? 'Adjust inventory' : 'Add opening stock'}>
        <InventoryForm initialData={editing} warehouses={warehouses} locations={locations} items={items} onSubmit={save} onCancel={() => setFormOpen(false)} />
      </Modal>

      <Modal size="lg" isOpen={Boolean(selectedMovement)} onClose={() => setSelectedMovement(null)} title={selectedMovement?.transactionNo || 'Movement details'}>
        {selectedMovement && <div className="form-stack">
          <div className="movement-meta">
            <div><span>Type</span><strong>{formatLabel(selectedMovement.transactionType)}</strong></div>
            <div><span>Status</span><StatusBadge value={selectedMovement.status} /></div>
            <div><span>Route</span><strong>{selectedMovement.fromWarehouseCode || 'External'} -&gt; {selectedMovement.toWarehouseCode || 'External'}</strong></div>
            <div><span>Reference</span><strong>{selectedMovement.referenceType ? `${selectedMovement.referenceType} #${selectedMovement.referenceId}` : 'N/A'}</strong></div>
            <div><span>Created</span><strong>{formatDateTime(selectedMovement.createdAt)}</strong></div>
            <div><span>Created by</span><strong>{selectedMovement.createdBy || 'System'}</strong></div>
          </div>
          <div className="movement-lines">
            <div className="section-label">Movement lines</div>
            <div className="table-container"><table className="wms-table"><thead><tr><th>Direction</th><th>Item</th><th>From location</th><th>To location</th><th>Quantity</th></tr></thead><tbody>
              {selectedMovement.details?.map((detail) => <tr key={detail.id}>
                <td><span className={`direction-badge ${movementDirection(detail).toLowerCase()}`}>{movementDirection(detail)}</span></td>
                <td><strong>{detail.itemCode}</strong></td>
                <td>{detail.locationFromCode || '-'}</td>
                <td>{detail.locationToCode || '-'}</td>
                <td><strong>{detail.quantity}</strong></td>
              </tr>)}
            </tbody></table></div>
          </div>
          <MovementAttachments transactionId={selectedMovement.id} canManage={canAdjust} />
          <div className="form-actions"><Button variant="secondary" onClick={() => setSelectedMovement(null)}>Close</Button></div>
        </div>}
      </Modal>
    </div>
  );
};

export default InventoryList;
