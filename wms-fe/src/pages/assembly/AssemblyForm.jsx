import { useState } from 'react';
import Button from '../../components/Button';
import Input from '../../components/Input';

const AssemblyForm = ({ items, onSubmit, onCancel }) => {
  const [form, setForm] = useState({ setItemId: '', quantity: 1 });
  const sets = items.filter((item) => item.itemType === 'SET' && item.status === 'ACTIVE');
  const submit = (event) => { event.preventDefault(); onSubmit({ setItemId: Number(form.setItemId), quantity: Number(form.quantity) }); };
  return <form className="form-stack" onSubmit={submit}>
    <label className="input-group"><span className="input-label">SET item</span><select className="input-field" value={form.setItemId} onChange={(event) => setForm((current) => ({ ...current, setItemId: event.target.value }))} required><option value="">Select SET</option>{sets.map((item) => <option key={item.id} value={item.id}>{item.code} - {item.name}</option>)}</select></label>
    <Input label="Assembly quantity" type="number" min="1" value={form.quantity} onChange={(event) => setForm((current) => ({ ...current, quantity: event.target.value }))} required />
    <div className="form-actions"><Button type="button" variant="secondary" onClick={onCancel}>Cancel</Button><Button type="submit">Create assembly order</Button></div>
  </form>;
};

export default AssemblyForm;
