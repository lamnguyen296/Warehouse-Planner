import { useCallback, useEffect, useState } from 'react';
import { CheckCircle2, CirclePlay, Plus, XCircle } from 'lucide-react';
import toast from 'react-hot-toast';
import Button from '../../components/Button';
import Modal from '../../components/Modal';
import PageHeader from '../../components/PageHeader';
import StatusBadge from '../../components/StatusBadge';
import Table from '../../components/Table';
import assemblyService from '../../services/assemblyService';
import itemService from '../../services/itemService';
import locationService from '../../services/locationService';
import warehouseService from '../../services/warehouseService';
import AssemblyCompleteForm from './AssemblyCompleteForm';
import AssemblyForm from './AssemblyForm';
import { useAuth } from '../../auth/useAuth';
import { PERMISSIONS as P } from '../../auth/permissions';

const AssemblyList = () => {
  const { hasPermission } = useAuth();
  const canExecute = hasPermission(P.ASSEMBLY_EXECUTE);
  const [data, setData] = useState([]); const [items, setItems] = useState([]); const [warehouses, setWarehouses] = useState([]); const [locations, setLocations] = useState([]);
  const [createOpen, setCreateOpen] = useState(false); const [completing, setCompleting] = useState(null);
  const load = useCallback(async () => { const [orders, itemData, warehouseData, locationData] = await Promise.all([assemblyService.getAll(), itemService.getAll(), warehouseService.getAll(), locationService.getAll()]); setData(orders || []); setItems(itemData || []); setWarehouses(warehouseData || []); setLocations(locationData || []); }, []);
  useEffect(() => { load(); }, [load]);
  const perform = async (action, message) => { await action(); toast.success(message); await load(); };
  const create = async (payload) => { await assemblyService.create(payload); toast.success('Manual assembly order created'); setCreateOpen(false); await load(); };
  const complete = async (payload) => { await assemblyService.completeOrderPayload(completing.id, payload); toast.success('Assembly completed and SET stock reserved'); setCompleting(null); await load(); };
  const actions = (row) => <div className="row-actions">{canExecute && row.status === 'PENDING' && <Button size="sm" onClick={() => perform(() => assemblyService.startAssembly(row.id), 'Assembly started')}><CirclePlay size={14} /> Start</Button>}{canExecute && row.status === 'IN_PROGRESS' && <Button size="sm" onClick={() => setCompleting(row)}><CheckCircle2 size={14} /> Complete</Button>}{canExecute && ['PENDING', 'IN_PROGRESS'].includes(row.status) && <Button size="sm" variant="danger" onClick={() => perform(() => assemblyService.cancel(row.id), 'Assembly cancelled')}><XCircle size={14} /> Cancel</Button>}</div>;
  const columns = [
    { header: 'Order', accessor: 'assemblyNo', render: (row) => <div><strong>{row.assemblyNo}</strong><div className="subtle">{row.planningDetailId ? `Plan detail #${row.planningDetailId}` : 'Manual'}</div></div> },
    { header: 'SET', accessor: 'setItem', render: (row) => `${row.setItem?.code} - ${row.setItem?.name}` }, { header: 'Quantity', accessor: 'quantity' },
    { header: 'Component snapshot', accessor: 'components', render: (row) => row.components?.map((component) => `${component.itemCode} x ${component.requiredQuantity}`).join(', ') || 'N/A' },
    { header: 'Status', accessor: 'status', render: (row) => <StatusBadge value={row.status} /> }, { header: 'Next action', accessor: 'actions', render: actions },
  ];
  return <div><PageHeader title="Assembly Orders" subtitle="Consume reserved leaf components and produce SET inventory" actions={canExecute && <Button onClick={() => setCreateOpen(true)}><Plus size={16} /> Manual assembly</Button>} /><div className="surface"><Table columns={columns} data={data} /></div>
    <Modal isOpen={createOpen} onClose={() => setCreateOpen(false)} title="Manual assembly order"><AssemblyForm items={items} onSubmit={create} onCancel={() => setCreateOpen(false)} /></Modal>
    <Modal isOpen={Boolean(completing)} onClose={() => setCompleting(null)} title={`Complete ${completing?.assemblyNo || ''}`}>{completing && <AssemblyCompleteForm warehouses={warehouses} locations={locations} onSubmit={complete} onCancel={() => setCompleting(null)} />}</Modal>
  </div>;
};

export default AssemblyList;
