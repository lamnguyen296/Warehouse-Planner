import { useCallback, useEffect, useMemo, useState } from 'react';
import { CheckCircle2, CirclePlay, Pencil, Plus, Trash2, XCircle } from 'lucide-react';
import toast from 'react-hot-toast';
import { useAuth } from '../../auth/useAuth';
import Button from '../../components/Button';
import Modal from '../../components/Modal';
import PageHeader from '../../components/PageHeader';
import StatusBadge from '../../components/StatusBadge';
import Table from '../../components/Table';
import itemService from '../../services/itemService';
import planningService from '../../services/planningService';
import workshopService from '../../services/workshopService';
import WorkshopForm from './WorkshopForm';
import { PERMISSIONS as P } from '../../auth/permissions';

const WorkshopList = () => {
  const { hasPermission, user } = useAuth();
  const [data, setData] = useState([]);
  const [items, setItems] = useState([]);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(null);

  const load = useCallback(async () => {
    const [requests, itemData] = await Promise.all([workshopService.getAll(), itemService.getAll()]);
    setData(requests || []);
    setItems(itemData || []);
  }, []);
  useEffect(() => { load(); }, [load]);

  const itemMap = useMemo(() => new Map(items.map((item) => [item.id, item])), [items]);
  const perform = async (action, successMessage) => { await action(); toast.success(successMessage); await load(); };

  const save = async (payload) => {
    if (editing) await workshopService.update(editing.id, payload);
    else await workshopService.create(payload);
    toast.success(editing ? 'Workshop request draft updated' : 'Workshop request draft created');
    setModalOpen(false);
    setEditing(null);
    await load();
  };

  const openCreate = () => { setEditing(null); setModalOpen(true); };
  const openEdit = (row) => { setEditing(row); setModalOpen(true); };
  const removeDraft = async (row) => {
    if (!window.confirm(`Delete draft ${row.requestNo}?`)) return;
    await workshopService.delete(row.id);
    toast.success('Workshop request draft deleted');
    await load();
  };

  const actionButtons = (row) => {
    const ownRequest = row.createdBy === user?.username;
    return <div className="row-actions">
    {row.status === 'DRAFT' && ownRequest && hasPermission(P.WORKSHOP_REQUEST_UPDATE_OWN) && <Button size="sm" variant="secondary" onClick={() => openEdit(row)}><Pencil size={14} /> Edit</Button>}
    {row.status === 'DRAFT' && ownRequest && hasPermission(P.WORKSHOP_REQUEST_UPDATE_OWN) && <Button size="sm" variant="danger" onClick={() => removeDraft(row)}><Trash2 size={14} /> Delete</Button>}
    {row.status === 'DRAFT' && ownRequest && hasPermission(P.WORKSHOP_REQUEST_SUBMIT_OWN) && <Button size="sm" onClick={() => perform(() => workshopService.submitRequest(row.id), 'Request submitted')}><CirclePlay size={14} /> Submit</Button>}
    {row.status === 'SUBMITTED' && hasPermission(P.WORKSHOP_REQUEST_APPROVE) && <Button size="sm" onClick={() => perform(() => workshopService.approveRequest(row.id), 'Request approved')}><CheckCircle2 size={14} /> Approve</Button>}
    {row.status === 'APPROVED' && hasPermission(P.PLANNING_RUN) && <Button size="sm" onClick={() => perform(() => planningService.generateFromWorkshop(row.id), 'Planning generated')}><CirclePlay size={14} /> Run MRP</Button>}
    {['DRAFT', 'SUBMITTED'].includes(row.status) && ownRequest && hasPermission(P.WORKSHOP_REQUEST_CANCEL_OWN) && <Button size="sm" variant="danger" onClick={() => perform(() => workshopService.cancelRequest(row.id), 'Request cancelled')}><XCircle size={14} /> Cancel</Button>}
  </div>;
  };

  const columns = [
    { header: 'Request', accessor: 'requestNo', render: (row) => <div><strong>{row.requestNo}</strong><div className="subtle">{row.createdBy}</div></div> },
    { header: 'Requested SETs', accessor: 'details', render: (row) => row.details?.map((detail) => `${itemMap.get(detail.itemId)?.code || detail.itemCode} x ${detail.quantity}`).join(', ') },
    { header: 'Priority', accessor: 'priority' },
    { header: 'Expected', accessor: 'expectedDate', render: (row) => row.expectedDate?.slice(0, 10) || 'N/A' },
    { header: 'Status', accessor: 'status', render: (row) => <StatusBadge value={row.status} /> },
    { header: 'Next action', accessor: 'actions', render: actionButtons },
  ];

  return <div>
    <PageHeader title="Workshop Requests" subtitle="Demand submitted by workshop and tracked through planning to issue" actions={hasPermission(P.WORKSHOP_REQUEST_CREATE) && <Button onClick={openCreate}><Plus size={16} /> New request</Button>} />
    <div className="surface"><Table columns={columns} data={data} /></div>
    <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title={editing ? `Edit ${editing.requestNo}` : 'New workshop request'}><WorkshopForm items={items} initialData={editing} onSubmit={save} onCancel={() => setModalOpen(false)} /></Modal>
  </div>;
};

export default WorkshopList;
