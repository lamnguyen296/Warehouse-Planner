import { useMemo, useState } from 'react';
import toast from 'react-hot-toast';
import Button from '../../components/Button';
import Input from '../../components/Input';

const InventoryForm = ({ initialData, warehouses, locations, items, onSubmit, onCancel }) => {
  const [form, setForm] = useState({
    warehouseId: initialData?.warehouseId || '',
    locationId: initialData?.locationId || '',
    itemId: initialData?.itemId || '',
    totalQuantity: initialData?.totalQuantity ?? 0,
  });

  const availableLocations = useMemo(() => locations.filter((location) =>
    (!form.warehouseId || location.warehouseId === Number(form.warehouseId))
      && location.status !== 'MAINTENANCE'
  ), [locations, form.warehouseId]);

  const setField = (field, value) => setForm((current) => ({
    ...current,
    [field]: value,
    ...(field === 'warehouseId' ? { locationId: '' } : {}),
  }));

  const handleSubmit = (event) => {
    event.preventDefault();
    const totalQuantity = Number(form.totalQuantity);
    const reservedQuantity = initialData?.reservedQuantity || 0;
    if (totalQuantity < reservedQuantity) {
      toast.error(`Total quantity cannot be lower than reserved quantity (${reservedQuantity})`);
      return;
    }
    onSubmit({
      warehouseId: Number(form.warehouseId),
      locationId: Number(form.locationId),
      itemId: Number(form.itemId),
      totalQuantity,
      reservedQuantity,
      availableQuantity: totalQuantity - reservedQuantity,
    });
  };

  return (
    <form className="form-stack" onSubmit={handleSubmit}>
      <div className="form-grid">
        <label className="input-group">
          <span className="input-label">Warehouse</span>
          <select className="input-field" value={form.warehouseId} disabled={Boolean(initialData)} onChange={(event) => setField('warehouseId', event.target.value)} required>
            <option value="">Select warehouse</option>
            {warehouses.filter((warehouse) => warehouse.status === 'ACTIVE').map((warehouse) => (
              <option key={warehouse.id} value={warehouse.id}>{warehouse.code} - {warehouse.name}</option>
            ))}
          </select>
        </label>
        <label className="input-group">
          <span className="input-label">Location</span>
          <select className="input-field" value={form.locationId} disabled={Boolean(initialData)} onChange={(event) => setField('locationId', event.target.value)} required>
            <option value="">Select location</option>
            {availableLocations.map((location) => <option key={location.id} value={location.id}>{location.code}</option>)}
          </select>
        </label>
      </div>
      <label className="input-group">
        <span className="input-label">Item</span>
        <select className="input-field" value={form.itemId} disabled={Boolean(initialData)} onChange={(event) => setField('itemId', event.target.value)} required>
          <option value="">Select item</option>
          {items.filter((item) => item.status === 'ACTIVE').map((item) => (
            <option key={item.id} value={item.id}>{item.code} - {item.name}</option>
          ))}
        </select>
      </label>
      <Input label="Total quantity" type="number" min={initialData?.reservedQuantity || 0} value={form.totalQuantity} onChange={(event) => setField('totalQuantity', event.target.value)} required />
      {initialData && <p className="subtle">Reserved quantity is system-managed: <strong>{initialData.reservedQuantity}</strong></p>}
      <div className="form-actions">
        <Button type="button" variant="secondary" onClick={onCancel}>Cancel</Button>
        <Button type="submit">{initialData ? 'Save adjustment' : 'Create inventory'}</Button>
      </div>
    </form>
  );
};

export default InventoryForm;
