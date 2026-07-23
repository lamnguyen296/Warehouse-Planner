import { useState, useEffect } from 'react';
import Input from '../../components/Input';
import Button from '../../components/Button';

const LocationForm = ({ initialData, warehouses, onSubmit, onCancel }) => {
  const [formData, setFormData] = useState({
    warehouseId: warehouses[0]?.id || '',
    code: '',
    zone: '',
    rack: '',
    capacity: 100,
    status: 'AVAILABLE'
  });

  useEffect(() => {
    if (initialData) {
      setFormData(initialData);
    }
  }, [initialData]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    onSubmit(formData);
  };

  return (
    <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
      <div className="input-group">
        <label className="input-label">Warehouse</label>
        <select 
          name="warehouseId" 
          value={formData.warehouseId} 
          onChange={handleChange}
          className="input-field"
          required
        >
          {warehouses.map(w => (
            <option key={w.id} value={w.id}>{w.name} ({w.code})</option>
          ))}
        </select>
      </div>

      <Input
        label="Location Code"
        name="code"
        value={formData.code}
        onChange={handleChange}
        required
        placeholder="e.g. LOC-A1"
      />
      
      <div style={{ display: 'flex', gap: '16px' }}>
        <Input
          label="Zone"
          name="zone"
          value={formData.zone}
          onChange={handleChange}
          placeholder="e.g. Zone A"
        />
        <Input
          label="Rack"
          name="rack"
          value={formData.rack}
          onChange={handleChange}
          placeholder="e.g. Rack 1"
        />
      </div>

      <Input
        label="Capacity"
        name="capacity"
        type="number"
        value={formData.capacity}
        onChange={handleChange}
        required
        min={1}
      />

      <div className="input-group">
        <label className="input-label">Status</label>
        <select 
          name="status" 
          value={formData.status} 
          onChange={handleChange}
          className="input-field"
        >
          <option value="AVAILABLE">Available</option>
          <option value="OCCUPIED">Occupied</option>
          <option value="MAINTENANCE">Maintenance</option>
        </select>
      </div>

      <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px', marginTop: '16px' }}>
        <Button type="button" variant="secondary" onClick={onCancel}>Cancel</Button>
        <Button type="submit" variant="primary">Save</Button>
      </div>
    </form>
  );
};

export default LocationForm;
