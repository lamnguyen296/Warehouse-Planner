import { useState } from 'react';
import { Plus, Trash2 } from 'lucide-react';
import Button from '../../components/Button';
import Input from '../../components/Input';

const PurchaseForm = ({ items, onSubmit, onCancel }) => {
  const purchasableItems = items.filter((item) => item.status === 'ACTIVE' && item.itemType !== 'SET');
  const [details, setDetails] = useState([{ itemId: '', quantity: 1 }]);
  const update = (index, field, value) => setDetails((current) => current.map((detail, detailIndex) => detailIndex === index ? { ...detail, [field]: value } : detail));

  const submit = (event) => {
    event.preventDefault();
    onSubmit({ details: details.map((detail) => ({ itemId: Number(detail.itemId), quantity: Number(detail.quantity) })) });
  };

  return <form className="form-stack" onSubmit={submit}>
    <div className="row-actions" style={{ justifyContent: 'space-between' }}><div><strong>Purchase lines</strong><div className="subtle">Manual request, independent from planning</div></div><Button type="button" size="sm" variant="secondary" onClick={() => setDetails((current) => [...current, { itemId: '', quantity: 1 }])}><Plus size={14} /> Add line</Button></div>
    {details.map((detail, index) => <div className="detail-row" key={index}>
      <label className="input-group"><span className="input-label">Item</span><select className="input-field" value={detail.itemId} onChange={(event) => update(index, 'itemId', event.target.value)} required><option value="">Select item</option>{purchasableItems.map((item) => <option key={item.id} value={item.id}>{item.code} - {item.name}</option>)}</select></label>
      <Input label="Quantity" type="number" min="1" value={detail.quantity} onChange={(event) => update(index, 'quantity', event.target.value)} required />
      <button type="button" className="icon-action" title="Remove line" aria-label="Remove line" disabled={details.length === 1} onClick={() => setDetails((current) => current.filter((_, itemIndex) => itemIndex !== index))}><Trash2 size={16} /></button>
    </div>)}
    <div className="form-actions"><Button type="button" variant="secondary" onClick={onCancel}>Cancel</Button><Button type="submit">Create request</Button></div>
  </form>;
};

export default PurchaseForm;
