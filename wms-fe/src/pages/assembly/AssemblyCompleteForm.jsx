import { useMemo, useState } from 'react';
import Button from '../../components/Button';

const AssemblyCompleteForm = ({ warehouses, locations, onSubmit, onCancel }) => {
  const setWarehouses = warehouses.filter((warehouse) => warehouse.type === 'SET_WAREHOUSE' && warehouse.status === 'ACTIVE');
  const [form, setForm] = useState({ warehouseId: setWarehouses[0]?.id || '', locationId: '' });
  const availableLocations = useMemo(() => locations.filter((location) => location.warehouseId === Number(form.warehouseId)), [locations, form.warehouseId]);
  const submit = (event) => { event.preventDefault(); onSubmit({ warehouseId: Number(form.warehouseId), locationId: Number(form.locationId) }); };
  return <form className="form-stack" onSubmit={submit}>
    <label className="input-group"><span className="input-label">SET warehouse</span><select className="input-field" value={form.warehouseId} onChange={(event) => setForm({ warehouseId: event.target.value, locationId: '' })} required>{setWarehouses.map((warehouse) => <option key={warehouse.id} value={warehouse.id}>{warehouse.code} - {warehouse.name}</option>)}</select></label>
    <label className="input-group"><span className="input-label">Output location</span><select className="input-field" value={form.locationId} onChange={(event) => setForm((current) => ({ ...current, locationId: event.target.value }))} required><option value="">Select location</option>{availableLocations.map((location) => <option key={location.id} value={location.id}>{location.code}</option>)}</select></label>
    <div className="form-actions"><Button type="button" variant="secondary" onClick={onCancel}>Cancel</Button><Button type="submit">Complete assembly</Button></div>
  </form>;
};

export default AssemblyCompleteForm;
