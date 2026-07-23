import { useMemo, useState } from 'react';
import Button from '../../components/Button';
import Input from '../../components/Input';

const BomForm = ({ initialData, items, onSubmit, onCancel }) => {
  const [form, setForm] = useState({
    parentItemId: initialData?.parentItemId || '',
    childItemId: initialData?.childItemId || '',
    quantity: initialData?.quantity || 1,
    priority: initialData?.priority ?? 0,
  });

  const activeItems = useMemo(() => items.filter((item) => item.status === 'ACTIVE'), [items]);
  const parent = activeItems.find((item) => item.id === Number(form.parentItemId));
  const children = activeItems.filter((item) => item.itemType === 'FINISHED_COMPONENT' && item.id !== parent?.id);

  const setField = (field, value) => setForm((current) => ({
    ...current,
    [field]: value,
    ...(field === 'parentItemId' ? { childItemId: '' } : {}),
  }));

  const handleSubmit = (event) => {
    event.preventDefault();
    onSubmit({
      parentItemId: Number(form.parentItemId),
      childItemId: Number(form.childItemId),
      quantity: Number(form.quantity),
      priority: Number(form.priority),
    });
  };

  return (
    <form className="form-stack" onSubmit={handleSubmit}>
      <label className="input-group">
        <span className="input-label">Parent item</span>
        <select className="input-field" value={form.parentItemId} onChange={(event) => setField('parentItemId', event.target.value)} required>
          <option value="">Select parent</option>
          {activeItems.map((item) => <option key={item.id} value={item.id}>{item.code} - {item.name} ({item.itemType})</option>)}
        </select>
      </label>
      <label className="input-group">
        <span className="input-label">Finished component output/child</span>
        <select className="input-field" value={form.childItemId} onChange={(event) => setField('childItemId', event.target.value)} required disabled={!parent}>
          <option value="">Select finished component</option>
          {children.map((item) => <option key={item.id} value={item.id}>{item.code} - {item.name}</option>)}
        </select>
      </label>
      {parent?.itemType === 'RAW_COMPONENT' && <p className="subtle">Quantity is the finished-component yield produced by one raw component.</p>}
      <div className="form-grid">
        <Input label="Quantity / conversion ratio" type="number" min="1" value={form.quantity} onChange={(event) => setField('quantity', event.target.value)} required />
        <Input label="Priority" type="number" min="0" value={form.priority} onChange={(event) => setField('priority', event.target.value)} required />
      </div>
      <div className="form-actions">
        <Button type="button" variant="secondary" onClick={onCancel}>Cancel</Button>
        <Button type="submit">Save BOM</Button>
      </div>
    </form>
  );
};

export default BomForm;
