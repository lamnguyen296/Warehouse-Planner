import { useMemo, useState } from 'react';
import Button from '../../components/Button';
import Input from '../../components/Input';

const TransferForm = ({ items, warehouses, plannings, transfers, onSubmit, onCancel }) => {
  const [form, setForm] = useState({ planningId: '', fromWarehouseId: '', toWarehouseId: '', itemId: '', quantity: 1 });
  const planned = Boolean(form.planningId);
  const activeWarehouses = warehouses.filter((warehouse) => warehouse.status === 'ACTIVE');
  const selectedPlanning = plannings.find((planning) => planning.id === Number(form.planningId));
  const plannedItems = useMemo(() => (selectedPlanning?.details || []).map((detail) => {
    const item = items.find((candidate) => candidate.id === detail.itemId);
    const committed = transfers.filter((transfer) => transfer.planningId === selectedPlanning.id
      && transfer.item?.id === detail.itemId && transfer.status !== 'CANCELLED')
      .reduce((sum, transfer) => sum + transfer.quantity, 0);
    return { ...item, remainingQuantity: Math.max(0, detail.requiredQuantity - committed) };
  }).filter((item) => item.id && item.itemType === 'SET' && item.status === 'ACTIVE'
    && item.remainingQuantity > 0), [items, selectedPlanning, transfers]);
  const availableItems = useMemo(() => planned
    ? plannedItems
    : items.filter((item) => item.status === 'ACTIVE'), [items, planned, plannedItems]);
  const selectedItem = availableItems.find((item) => item.id === Number(form.itemId));
  const setField = (field, value) => setForm((current) => {
    const next = { ...current, [field]: value };
    if (field === 'planningId' && value) {
      next.fromWarehouseId = activeWarehouses.find((warehouse) => warehouse.type === 'SET_WAREHOUSE')?.id || '';
      next.toWarehouseId = activeWarehouses.find((warehouse) => warehouse.type === 'WORKSHOP')?.id || '';
      next.itemId = '';
    }
    return next;
  });
  const submit = (event) => { event.preventDefault(); onSubmit({ planningId: form.planningId ? Number(form.planningId) : null, fromWarehouseId: Number(form.fromWarehouseId), toWarehouseId: Number(form.toWarehouseId), itemId: Number(form.itemId), quantity: Number(form.quantity) }); };

  return <form className="form-stack" onSubmit={submit}>
    <label className="input-group"><span className="input-label">Planning link</span><select className="input-field" value={form.planningId} onChange={(event) => setField('planningId', event.target.value)}><option value="">Manual transfer</option>{plannings.filter((planning) => planning.status === 'EXECUTING').map((planning) => <option key={planning.id} value={planning.id}>{planning.planningNo} - {planning.workshopRequestNo}</option>)}</select></label>
    <div className="form-grid"><label className="input-group"><span className="input-label">From warehouse</span><select className="input-field" value={form.fromWarehouseId} onChange={(event) => setField('fromWarehouseId', event.target.value)} disabled={planned} required><option value="">Select source</option>{activeWarehouses.map((warehouse) => <option key={warehouse.id} value={warehouse.id}>{warehouse.code} ({warehouse.type})</option>)}</select></label><label className="input-group"><span className="input-label">To warehouse</span><select className="input-field" value={form.toWarehouseId} onChange={(event) => setField('toWarehouseId', event.target.value)} disabled={planned} required><option value="">Select destination</option>{activeWarehouses.map((warehouse) => <option key={warehouse.id} value={warehouse.id}>{warehouse.code} ({warehouse.type})</option>)}</select></label></div>
    <label className="input-group"><span className="input-label">Item</span><select className="input-field" value={form.itemId} onChange={(event) => setField('itemId', event.target.value)} required><option value="">Select item</option>{availableItems.map((item) => <option key={item.id} value={item.id}>{item.code} - {item.name}{planned ? ` (remaining ${item.remainingQuantity})` : ''}</option>)}</select></label>
    <Input label={planned && selectedItem ? `Transfer quantity (remaining ${selectedItem.remainingQuantity})` : 'Transfer quantity'} type="number" min="1" max={planned ? selectedItem?.remainingQuantity : undefined} value={form.quantity} onChange={(event) => setField('quantity', event.target.value)} required />
    {planned && <p className="subtle">Planned issue must match the remaining reserved SET quantity for this planning.</p>}
    <div className="form-actions"><Button type="button" variant="secondary" onClick={onCancel}>Cancel</Button><Button type="submit">Create transfer</Button></div>
  </form>;
};

export default TransferForm;
