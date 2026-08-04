import { useCallback, useEffect, useState } from 'react';
import { CirclePlay, Plus, Recycle, XCircle } from 'lucide-react';
import toast from 'react-hot-toast';
import Button from '../../components/Button';
import Input from '../../components/Input';
import Modal from '../../components/Modal';
import PageHeader from '../../components/PageHeader';
import StatusBadge from '../../components/StatusBadge';
import Table from '../../components/Table';
import itemService from '../../services/itemService';
import recycleService from '../../services/recycleService';
import bomService from '../../services/bomService';
import RecycleForm from './RecycleForm';
import { useAuth } from '../../auth/useAuth';
import { PERMISSIONS as P } from '../../auth/permissions';

const RecycleList = () => {
  const { hasPermission } = useAuth();
  const canExecute = hasPermission(P.RECYCLE_EXECUTE);
  const [data, setData] = useState([]);
  const [items, setItems] = useState([]);
  const [boms, setBoms] = useState([]);
  const [createOpen, setCreateOpen] = useState(false);
  const [completing, setCompleting] = useState(null);
  const [actualYield, setActualYield] = useState(0);
  const load = useCallback(async () => { const [orders, itemData, bomData] = await Promise.all([recycleService.getAll(), itemService.getAll(), bomService.getAll()]); setData(orders || []); setItems(itemData || []); setBoms(bomData || []); }, []);
  useEffect(() => { load(); }, [load]);
  const perform = async (action, message) => { await action(); toast.success(message); await load(); };
  const create = async (payload) => { await recycleService.create(payload); toast.success('Manual recycle order created'); setCreateOpen(false); await load(); };
  const openComplete = (row) => { setCompleting(row); setActualYield(row.expectedYield ?? row.quantity); };
  const complete = async (event) => { event.preventDefault(); await recycleService.completeOrder(completing.id, Number(actualYield)); toast.success('Recycle completed and stock updated'); setCompleting(null); await load(); };

  const actions = (row) => <div className="row-actions">
    {canExecute && row.status === 'PENDING' && <Button size="sm" onClick={() => perform(() => recycleService.startRecycle(row.id), 'Recycle started')}><CirclePlay size={14} /> Start</Button>}
    {canExecute && row.status === 'IN_PROGRESS' && <Button size="sm" onClick={() => openComplete(row)}><Recycle size={14} /> Complete</Button>}
    {canExecute && ['PENDING', 'IN_PROGRESS'].includes(row.status) && <Button size="sm" variant="danger" onClick={() => perform(() => recycleService.cancel(row.id), 'Recycle cancelled')}><XCircle size={14} /> Cancel</Button>}
  </div>;
  const columns = [
    { header: 'Order', accessor: 'orderNo', render: (row) => <div><strong>{row.orderNo}</strong><div className="subtle">{row.planningDetailId ? `Plan detail #${row.planningDetailId}` : 'Manual'}</div></div> },
    { header: 'Conversion', accessor: 'conversion', render: (row) => `${row.fromItem?.code} -> ${row.toItem?.code} (1:${row.conversionRatio || '?'})` },
    { header: 'Raw input', accessor: 'quantity' },
    { header: 'Yield', accessor: 'yield', render: (row) => `${row.actualYield ?? '-'} / ${row.expectedYield ?? '-'}` },
    { header: 'Status', accessor: 'status', render: (row) => <StatusBadge value={row.status} /> },
    { header: 'Next action', accessor: 'actions', render: actions },
  ];

  return <div><PageHeader title="Recycle Orders" subtitle="Convert raw components into finished components with actual-yield tracking" actions={canExecute && <Button onClick={() => setCreateOpen(true)}><Plus size={16} /> Manual recycle</Button>} /><div className="surface"><Table columns={columns} data={data} /></div>
    <Modal isOpen={createOpen} onClose={() => setCreateOpen(false)} title="Manual recycle order"><RecycleForm items={items} boms={boms} onSubmit={create} onCancel={() => setCreateOpen(false)} /></Modal>
    <Modal isOpen={Boolean(completing)} onClose={() => setCompleting(null)} title={`Complete ${completing?.orderNo || ''}`}><form className="form-stack" onSubmit={complete}><div className="subtle">Expected yield: <strong>{completing?.expectedYield ?? 'Not planned'}</strong></div><Input label="Actual finished-component yield" type="number" min="0" value={actualYield} onChange={(event) => setActualYield(event.target.value)} required /><div className="form-actions"><Button type="button" variant="secondary" onClick={() => setCompleting(null)}>Cancel</Button><Button type="submit">Confirm output</Button></div></form></Modal>
  </div>;
};

export default RecycleList;
