import { useState } from 'react';
import { Plus, Trash2 } from 'lucide-react';
import Button from '../../components/Button';
import Input from '../../components/Input';

const WorkshopForm = ({ items, initialData, onSubmit, onCancel }) => {
  const sets = items.filter((item) => item.itemType === 'SET' && item.status === 'ACTIVE');
  const [priority, setPriority] = useState(initialData?.priority || 'MEDIUM');
  const [expectedDate, setExpectedDate] = useState(initialData?.expectedDate?.slice(0, 10) || '');
  const [details, setDetails] = useState(initialData?.details?.map((detail) => ({
    itemId: detail.itemId,
    quantity: detail.quantity,
  })) || [{ itemId: '', quantity: 1 }]);

  const updateDetail = (index, field, value) => setDetails((current) =>
    current.map((detail, detailIndex) => detailIndex === index ? { ...detail, [field]: value } : detail)
  );

  const removeDetail = (index) => setDetails((current) => current.filter((_, detailIndex) => detailIndex !== index));

  const handleSubmit = (event) => {
    event.preventDefault();
    onSubmit({
      priority,
      expectedDate: expectedDate ? `${expectedDate}T23:59:59` : null,
      details: details.map((detail) => ({ itemId: Number(detail.itemId), quantity: Number(detail.quantity) })),
    });
  };

  return (
    <form className="form-stack" onSubmit={handleSubmit}>
      <div className="form-grid">
        <label className="input-group"><span className="input-label">Priority</span><select className="input-field" value={priority} onChange={(event) => setPriority(event.target.value)}>{['LOW', 'MEDIUM', 'HIGH', 'URGENT'].map((value) => <option key={value}>{value}</option>)}</select></label>
        <Input label="Expected date" type="date" value={expectedDate} onChange={(event) => setExpectedDate(event.target.value)} required />
      </div>
      <div className="row-actions" style={{ justifyContent: 'space-between' }}><strong>Requested SETs</strong><Button type="button" size="sm" variant="secondary" onClick={() => setDetails((current) => [...current, { itemId: '', quantity: 1 }])}><Plus size={14} /> Add line</Button></div>
      {details.map((detail, index) => (
        <div className="detail-row" key={index}>
          <label className="input-group"><span className="input-label">SET item</span><select className="input-field" value={detail.itemId} onChange={(event) => updateDetail(index, 'itemId', event.target.value)} required><option value="">Select SET</option>{sets.map((item) => <option key={item.id} value={item.id}>{item.code} - {item.name}</option>)}</select></label>
          <Input label="Quantity" type="number" min="1" value={detail.quantity} onChange={(event) => updateDetail(index, 'quantity', event.target.value)} required />
          <button type="button" className="icon-action" title="Remove line" aria-label="Remove line" disabled={details.length === 1} onClick={() => removeDetail(index)}><Trash2 size={16} /></button>
        </div>
      ))}
      <div className="form-actions"><Button type="button" variant="secondary" onClick={onCancel}>Cancel</Button><Button type="submit">{initialData ? 'Save draft' : 'Create draft'}</Button></div>
    </form>
  );
};

export default WorkshopForm;
