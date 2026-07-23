import { useCallback, useEffect, useMemo, useState } from 'react';
import { CheckCircle2, CirclePlay, Plus, XCircle } from 'lucide-react';
import toast from 'react-hot-toast';
import Button from '../../components/Button';
import Modal from '../../components/Modal';
import PageHeader from '../../components/PageHeader';
import StatusBadge from '../../components/StatusBadge';
import Table from '../../components/Table';
import itemService from '../../services/itemService';
import locationService from '../../services/locationService';
import planningService from '../../services/planningService';
import transferService from '../../services/transferService';
import warehouseService from '../../services/warehouseService';
import TransferForm from './TransferForm';
import { useAuth } from '../../auth/useAuth';
import { PERMISSIONS as P } from '../../auth/permissions';

const TransferList = () => {
  const { hasPermission } = useAuth();
  const canExecute = hasPermission(P.TRANSFER_EXECUTE);
  const [data, setData] = useState([]); const [items, setItems] = useState([]); const [warehouses, setWarehouses] = useState([]); const [locations, setLocations] = useState([]); const [plannings, setPlannings] = useState([]);
  const [createOpen, setCreateOpen] = useState(false); const [completing, setCompleting] = useState(null); const [locationId, setLocationId] = useState('');
  const load = useCallback(async () => { const [orders, itemData, warehouseData, locationData, planningData] = await Promise.all([transferService.getAll(), itemService.getAll(), warehouseService.getAll(), locationService.getAll(), planningService.getAll()]); setData(orders || []); setItems(itemData || []); setWarehouses(warehouseData || []); setLocations(locationData || []); setPlannings(planningData || []); }, []);
  useEffect(() => { load(); }, [load]);
  const perform = async (action, message) => { await action(); toast.success(message); await load(); };
  const create = async (payload) => { await transferService.create(payload); toast.success(payload.planningId ? 'Planned issue created' : 'Manual transfer created'); setCreateOpen(false); await load(); };
  const destinationLocations = useMemo(() => locations.filter((location) => location.warehouseId === completing?.toWarehouse?.id), [locations, completing]);
  const openComplete = (row) => { setCompleting(row); setLocationId(''); };
  const complete = async (event) => { event.preventDefault(); await transferService.complete(completing.id, Number(locationId)); toast.success(completing.planningId ? 'SET issued and planning completed when demand is fully delivered' : 'Transfer completed'); setCompleting(null); await load(); };
  const actions = (row) => <div className="row-actions">{canExecute && row.status === 'PENDING' && <Button size="sm" onClick={() => perform(() => transferService.execute(row.id), 'Transfer is in transit')}><CirclePlay size={14} /> Execute</Button>}{canExecute && row.status === 'IN_TRANSIT' && <Button size="sm" onClick={() => openComplete(row)}><CheckCircle2 size={14} /> Complete</Button>}{canExecute && ['PENDING', 'IN_TRANSIT'].includes(row.status) && <Button size="sm" variant="danger" onClick={() => perform(() => transferService.cancel(row.id), 'Transfer cancelled')}><XCircle size={14} /> Cancel</Button>}</div>;
  const columns = [
    { header: 'Transfer', accessor: 'transferNo', render: (row) => <div><strong>{row.transferNo}</strong><div className="subtle">{row.planningId ? `Planning #${row.planningId}` : 'Manual'}</div></div> },
    { header: 'Item', accessor: 'item', render: (row) => `${row.item?.code} - ${row.item?.name}` }, { header: 'Quantity', accessor: 'quantity' },
    { header: 'Route', accessor: 'route', render: (row) => `${row.fromWarehouse?.code} -> ${row.toWarehouse?.code}` },
    { header: 'Status', accessor: 'status', render: (row) => <StatusBadge value={row.status} /> }, { header: 'Next action', accessor: 'actions', render: actions },
  ];
  return <div><PageHeader title="Transfer & Issue" subtitle="Move stock between warehouses; SET warehouse to workshop is recorded as ISSUE" actions={canExecute && <Button onClick={() => setCreateOpen(true)}><Plus size={16} /> Create transfer</Button>} /><div className="surface"><Table columns={columns} data={data} /></div>
    <Modal isOpen={createOpen} onClose={() => setCreateOpen(false)} title="Create transfer"><TransferForm items={items} warehouses={warehouses} plannings={plannings} onSubmit={create} onCancel={() => setCreateOpen(false)} /></Modal>
    <Modal isOpen={Boolean(completing)} onClose={() => setCompleting(null)} title={`Complete ${completing?.transferNo || ''}`}><form className="form-stack" onSubmit={complete}><label className="input-group"><span className="input-label">Destination location</span><select className="input-field" value={locationId} onChange={(event) => setLocationId(event.target.value)} required><option value="">Select location</option>{destinationLocations.map((location) => <option key={location.id} value={location.id}>{location.code}</option>)}</select></label><div className="form-actions"><Button type="button" variant="secondary" onClick={() => setCompleting(null)}>Cancel</Button><Button type="submit">Confirm arrival</Button></div></form></Modal>
  </div>;
};

export default TransferList;
