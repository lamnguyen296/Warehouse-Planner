import { useState } from 'react';
import Button from '../../components/Button';
import Input from '../../components/Input';

const RecycleForm = ({ items, onSubmit, onCancel }) => {
  const [form, setForm] = useState({ fromItemId: '', toItemId: '', quantity: 1 });
  const rawItems = items.filter((item) => item.itemType === 'RAW_COMPONENT' && item.status === 'ACTIVE');
  const finishedItems = items.filter((item) => item.itemType === 'FINISHED_COMPONENT' && item.status === 'ACTIVE');
  const setField = (field, value) => setForm((current) => ({ ...current, [field]: value }));
  const submit = (event) => { event.preventDefault(); onSubmit({ fromItemId: Number(form.fromItemId), toItemId: Number(form.toItemId), quantity: Number(form.quantity) }); };

  return <form className="form-stack" onSubmit={submit}>
    <label className="input-group"><span className="input-label">Raw component input</span><select className="input-field" value={form.fromItemId} onChange={(event) => setField('fromItemId', event.target.value)} required><option value="">Select raw item</option>{rawItems.map((item) => <option key={item.id} value={item.id}>{item.code} - {item.name}</option>)}</select></label>
    <label className="input-group"><span className="input-label">Finished component output</span><select className="input-field" value={form.toItemId} onChange={(event) => setField('toItemId', event.target.value)} required><option value="">Select output item</option>{finishedItems.map((item) => <option key={item.id} value={item.id}>{item.code} - {item.name}</option>)}</select></label>
    <Input label="Raw input quantity" type="number" min="1" value={form.quantity} onChange={(event) => setField('quantity', event.target.value)} required />
    <div className="form-actions"><Button type="button" variant="secondary" onClick={onCancel}>Cancel</Button><Button type="submit">Create recycle order</Button></div>
  </form>;
};

export default RecycleForm;
