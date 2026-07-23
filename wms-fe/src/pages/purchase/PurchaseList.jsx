import { useCallback, useEffect, useState } from 'react';
import { CheckCircle2, PackageCheck, Plus, ShoppingCart, XCircle } from 'lucide-react';
import toast from 'react-hot-toast';
import Button from '../../components/Button';
import Modal from '../../components/Modal';
import PageHeader from '../../components/PageHeader';
import StatusBadge from '../../components/StatusBadge';
import Table from '../../components/Table';
import itemService from '../../services/itemService';
import locationService from '../../services/locationService';
import purchaseService from '../../services/purchaseService';
import warehouseService from '../../services/warehouseService';
import PurchaseForm from './PurchaseForm';
import ReceiveGoodsForm from './ReceiveGoodsForm';
import { useAuth } from '../../auth/useAuth';
import { PERMISSIONS as P } from '../../auth/permissions';

const PurchaseList = () => {
  const { hasPermission } = useAuth();
  const canExecute = hasPermission(P.PURCHASE_EXECUTE);
  const [data, setData] = useState([]);
  const [items, setItems] = useState([]);
  const [warehouses, setWarehouses] = useState([]);
  const [locations, setLocations] = useState([]);
  const [createOpen, setCreateOpen] = useState(false);
  const [receiving, setReceiving] = useState(null);

  const load = useCallback(async () => {
    const [orders, itemData, warehouseData, locationData] = await Promise.all([purchaseService.getAll(), itemService.getAll(), warehouseService.getAll(), locationService.getAll()]);
    setData(orders || []); setItems(itemData || []); setWarehouses(warehouseData || []); setLocations(locationData || []);
  }, []);
  useEffect(() => { load(); }, [load]);

  const perform = async (action, message) => { await action(); toast.success(message); await load(); };
  const create = async (payload) => { await purchaseService.create(payload); toast.success('Manual purchase request created'); setCreateOpen(false); await load(); };
  const receive = async (payload) => { await purchaseService.receiveGoods(receiving.id, payload); toast.success('Goods received'); setReceiving(null); await load(); };

  const actions = (row) => <div className="row-actions">
    {canExecute && row.status === 'PENDING' && <Button size="sm" onClick={() => perform(() => purchaseService.approve(row.id), 'Purchase approved')}><CheckCircle2 size={14} /> Approve</Button>}
    {canExecute && row.status === 'APPROVED' && <Button size="sm" onClick={() => perform(() => purchaseService.order(row.id), 'Purchase ordered')}><ShoppingCart size={14} /> Order</Button>}
    {canExecute && ['ORDERED', 'PARTIAL_RECEIVED'].includes(row.status) && <Button size="sm" onClick={() => setReceiving(row)}><PackageCheck size={14} /> Receive</Button>}
    {canExecute && ['PENDING', 'APPROVED'].includes(row.status) && <Button size="sm" variant="danger" onClick={() => perform(() => purchaseService.cancel(row.id), 'Purchase cancelled')}><XCircle size={14} /> Cancel</Button>}
  </div>;

  const columns = [
    { header: 'Request', accessor: 'requestNo', render: (row) => <div><strong>{row.requestNo}</strong><div className="subtle">{row.planningDetailId ? `Plan detail #${row.planningDetailId}` : 'Manual'}</div></div> },
    { header: 'Items', accessor: 'details', render: (row) => row.details.map((detail) => `${detail.item.code} x ${detail.quantity}`).join(', ') },
    { header: 'Received', accessor: 'received', render: (row) => `${row.details.reduce((sum, detail) => sum + detail.receivedQuantity, 0)} / ${row.details.reduce((sum, detail) => sum + detail.quantity, 0)}` },
    { header: 'Status', accessor: 'status', render: (row) => <StatusBadge value={row.status} /> },
    { header: 'Next action', accessor: 'actions', render: actions },
  ];

  return <div><PageHeader title="Purchase Requests" subtitle="Planned shortages and independent manual purchasing" actions={canExecute && <Button onClick={() => setCreateOpen(true)}><Plus size={16} /> Manual purchase</Button>} /><div className="surface"><Table columns={columns} data={data} /></div>
    <Modal isOpen={createOpen} onClose={() => setCreateOpen(false)} title="Manual purchase request"><PurchaseForm items={items} onSubmit={create} onCancel={() => setCreateOpen(false)} /></Modal>
    <Modal isOpen={Boolean(receiving)} onClose={() => setReceiving(null)} title={`Receive ${receiving?.requestNo || ''}`}>{receiving && <ReceiveGoodsForm request={receiving} warehouses={warehouses} locations={locations} onSubmit={receive} onCancel={() => setReceiving(null)} />}</Modal>
  </div>;
};

export default PurchaseList;
