import { useCallback, useEffect, useState } from 'react';
import { CheckCircle2, Eye, Play, RefreshCw, XCircle } from 'lucide-react';
import toast from 'react-hot-toast';
import { useAuth } from '../../auth/useAuth';
import Button from '../../components/Button';
import Modal from '../../components/Modal';
import PageHeader from '../../components/PageHeader';
import StatusBadge from '../../components/StatusBadge';
import Table from '../../components/Table';
import planningService from '../../services/planningService';
import { PERMISSIONS as P } from '../../auth/permissions';

const actionLabel = (action) => ({
  USE_AVAILABLE: 'Use stock', RECYCLE: 'Recycle', RECYCLE_AND_PURCHASE: 'Recycle + purchase',
  PURCHASE: 'Purchase', ASSEMBLE: 'Assemble',
}[action] || action);

const PlanningList = () => {
  const { hasPermission } = useAuth();
  const [data, setData] = useState([]);
  const [selected, setSelected] = useState(null);

  const load = useCallback(async () => setData(await planningService.getAll() || []), []);
  useEffect(() => { load(); }, [load]);

  const perform = async (action, message) => {
    await action();
    toast.success(message);
    await load();
    if (selected) setSelected(await planningService.getById(selected.id));
  };

  const view = async (row) => setSelected(await planningService.getById(row.id));
  const actions = (row) => <div className="row-actions">
    <Button size="sm" variant="secondary" onClick={() => view(row)}><Eye size={14} /> Details</Button>
    {row.status === 'PLANNING' && hasPermission(P.PLANNING_APPROVE) && <Button size="sm" onClick={() => perform(() => planningService.approvePlanning(row.id), 'Planning approved')}><CheckCircle2 size={14} /> Approve</Button>}
    {row.status === 'APPROVED' && hasPermission(P.PLANNING_EXECUTE) && <Button size="sm" onClick={() => perform(() => planningService.startExecution(row.id), 'Execution started and child orders created')}><Play size={14} /> Start execution</Button>}
    {row.status === 'EXECUTING' && hasPermission(P.PLANNING_FAIL) && <Button size="sm" variant="danger" onClick={() => perform(() => planningService.failPlanning(row.id), 'Planning failed and eligible reservations released')}><XCircle size={14} /> Fail</Button>}
  </div>;

  const columns = [
    { header: 'Planning', accessor: 'planningNo', render: (row) => <strong>{row.planningNo}</strong> },
    { header: 'Workshop request', accessor: 'workshopRequestNo' },
    { header: 'Lines', accessor: 'details', render: (row) => row.details?.length || 0 },
    { header: 'Status', accessor: 'status', render: (row) => <StatusBadge value={row.status} /> },
    { header: 'Created', accessor: 'createdAt', render: (row) => row.createdAt?.slice(0, 16).replace('T', ' ') },
    { header: 'Actions', accessor: 'actions', render: actions },
  ];

  return <div>
    <PageHeader title="Planning" subtitle="MRP explosion, allocation and execution control" actions={<Button variant="secondary" onClick={load}><RefreshCw size={16} /> Refresh</Button>} />
    <div className="surface"><Table columns={columns} data={data} /></div>
    <Modal isOpen={Boolean(selected)} onClose={() => setSelected(null)} title={selected?.planningNo || 'Planning details'}>
      {selected && <div className="form-stack">
        <div className="form-grid"><div><span className="subtle">Workshop request</span><div><strong>{selected.workshopRequestNo}</strong></div></div><div><span className="subtle">Status</span><div><StatusBadge value={selected.status} /></div></div></div>
        <div style={{ overflowX: 'auto' }}><table className="wms-table"><thead><tr><th>Item</th><th>Required</th><th>Available</th><th>Reserved</th><th>Recycle</th><th>Purchase</th><th>Action</th></tr></thead><tbody>{selected.details?.map((detail) => <tr key={detail.id}><td><strong>{detail.itemCode}</strong><div className="subtle">{detail.itemName}</div></td><td>{detail.requiredQuantity}</td><td>{detail.availableQuantity}</td><td>{detail.reservedQuantity}</td><td>{detail.recycleQuantity}</td><td>{detail.purchaseQuantity}</td><td>{actionLabel(detail.action)}</td></tr>)}</tbody></table></div>
        <div className="form-actions">{actions(selected)}<Button variant="secondary" onClick={() => setSelected(null)}>Close</Button></div>
      </div>}
    </Modal>
  </div>;
};

export default PlanningList;
