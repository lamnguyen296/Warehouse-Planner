import { useMemo, useState } from 'react';
import toast from 'react-hot-toast';
import Button from '../../components/Button';
import Input from '../../components/Input';

const ReceiveGoodsForm = ({ request, warehouses, locations, onSubmit, onCancel }) => {
  const outstanding = request.details.filter((detail) => detail.receivedQuantity < detail.quantity);
  const [form, setForm] = useState({ purchaseDetailId: outstanding[0]?.id || '', receivedQty: 1, warehouseId: '', locationId: '' });
  const detail = outstanding.find((item) => item.id === Number(form.purchaseDetailId));
  const remaining = detail ? detail.quantity - detail.receivedQuantity : 0;
  const componentWarehouses = warehouses.filter((warehouse) => warehouse.type === 'COMPONENT_WAREHOUSE' && warehouse.status === 'ACTIVE');
  const availableLocations = useMemo(() => locations.filter((location) =>
    location.warehouseId === Number(form.warehouseId) && location.status !== 'MAINTENANCE'
  ), [locations, form.warehouseId]);
  const setField = (field, value) => setForm((current) => ({ ...current, [field]: value, ...(field === 'warehouseId' ? { locationId: '' } : {}) }));

  const submit = (event) => {
    event.preventDefault();
    if (Number(form.receivedQty) > remaining) { toast.error(`Maximum receivable quantity is ${remaining}`); return; }
    onSubmit({ purchaseDetailId: Number(form.purchaseDetailId), receivedQty: Number(form.receivedQty), warehouseId: Number(form.warehouseId), locationId: Number(form.locationId) });
  };

  return <form className="form-stack" onSubmit={submit}>
    <label className="input-group"><span className="input-label">Purchase line</span><select className="input-field" value={form.purchaseDetailId} onChange={(event) => setField('purchaseDetailId', event.target.value)} required>{outstanding.map((line) => <option key={line.id} value={line.id}>{line.item.code} - {line.item.name} ({line.receivedQuantity}/{line.quantity})</option>)}</select></label>
    <div className="form-grid">
      <label className="input-group"><span className="input-label">Component warehouse</span><select className="input-field" value={form.warehouseId} onChange={(event) => setField('warehouseId', event.target.value)} required><option value="">Select warehouse</option>{componentWarehouses.map((warehouse) => <option key={warehouse.id} value={warehouse.id}>{warehouse.code} - {warehouse.name}</option>)}</select></label>
      <label className="input-group"><span className="input-label">Location</span><select className="input-field" value={form.locationId} onChange={(event) => setField('locationId', event.target.value)} required><option value="">Select location</option>{availableLocations.map((location) => <option key={location.id} value={location.id}>{location.code}</option>)}</select></label>
    </div>
    <Input label={`Receive quantity (remaining ${remaining})`} type="number" min="1" max={remaining} value={form.receivedQty} onChange={(event) => setField('receivedQty', event.target.value)} required />
    <div className="form-actions"><Button type="button" variant="secondary" onClick={onCancel}>Cancel</Button><Button type="submit">Receive goods</Button></div>
  </form>;
};

export default ReceiveGoodsForm;
