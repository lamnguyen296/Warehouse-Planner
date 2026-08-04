import { useState, useEffect } from 'react';
import Input from '../../components/Input';
import Button from '../../components/Button';

const WarehouseForm = ({ initialData, onSubmit, onCancel }) => {
  const [formData, setFormData] = useState({
    code: '',
    name: '',
    type: 'COMPONENT_WAREHOUSE',
    status: 'ACTIVE'
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
      <Input
        label="Warehouse Code"
        name="code"
        value={formData.code}
        onChange={handleChange}
        required
        placeholder="e.g. WH-01"
      />
      <Input
        label="Warehouse Name"
        name="name"
        value={formData.name}
        onChange={handleChange}
        required
        placeholder="e.g. Raw Material Warehouse"
      />
      
      <div className="input-group">
        <label className="input-label">Type</label>
        <select 
          name="type" 
          value={formData.type} 
          onChange={handleChange}
          className="input-field"
          disabled={Boolean(initialData)}
        >
          <option value="WORKSHOP">Workshop</option>
          <option value="SET_WAREHOUSE">Set Warehouse</option>
          <option value="COMPONENT_WAREHOUSE">Component Warehouse</option>
        </select>
      </div>

      <div className="input-group">
        <label className="input-label">Status</label>
        <select 
          name="status" 
          value={formData.status} 
          onChange={handleChange}
          className="input-field"
        >
          <option value="ACTIVE">Active</option>
          <option value="INACTIVE">Inactive</option>
        </select>
      </div>

      <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px', marginTop: '16px' }}>
        <Button type="button" variant="secondary" onClick={onCancel}>Cancel</Button>
        <Button type="submit" variant="primary">Save</Button>
      </div>
    </form>
  );
};

export default WarehouseForm;
