import { useState, useEffect } from 'react';
import { Plus, Loader } from 'lucide-react';
import toast from 'react-hot-toast';
import Table from '../../components/Table';
import Button from '../../components/Button';
import Modal from '../../components/Modal';
import locationService from '../../services/locationService';
import warehouseService from '../../services/warehouseService';
import LocationForm from './LocationForm';

const LocationList = () => {
  const [data, setData] = useState([]);
  const [warehouses, setWarehouses] = useState([]);
  const [loading, setLoading] = useState(false);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingRow, setEditingRow] = useState(null);

  useEffect(() => {
    fetchLocations();
    fetchWarehouses();
  }, []);

  const fetchWarehouses = async () => {
    try {
      const res = await warehouseService.getAll();
      setWarehouses(res || []);
    } catch (error) {
      console.error('Failed to load warehouses for location form');
    }
  };

  const fetchLocations = async () => {
    try {
      setLoading(true);
      const res = await locationService.getAll();
      setData(res || []);
    } catch (error) {
      toast.error('Failed to load locations');
      setData([]);
    } finally {
      setLoading(false);
    }
  };

  const handleAdd = () => {
    if (warehouses.length === 0) {
      toast.error('Please create a warehouse first');
      return;
    }
    setEditingRow(null);
    setIsModalOpen(true);
  };

  const handleEdit = (row) => {
    setEditingRow(row);
    setIsModalOpen(true);
  };

  const handleDelete = async (row) => {
    if (window.confirm(`Are you sure you want to delete ${row.code}?`)) {
      try {
        await locationService.delete(row.id);
        toast.success('Location deleted successfully');
        setData(data.filter(item => item.id !== row.id));
      } catch (error) {
        // global interceptor handles toast
      }
    }
  };

  const handleFormSubmit = async (formData) => {
    try {
      if (editingRow) {
        const updated = await locationService.update(editingRow.id, formData);
        setData(data.map(item => item.id === editingRow.id ? updated : item));
        toast.success('Location updated successfully');
      } else {
        const created = await locationService.create(formData);
        setData([created, ...data]);
        toast.success('Location created successfully');
      }
      setIsModalOpen(false);
    } catch (error) {
      // global interceptor handles toast
    }
  };

  const columns = [
    { header: 'ID', accessor: 'id' },
    { header: 'Warehouse', accessor: 'warehouseCode', render: (row) => <strong>{row.warehouseCode}</strong> },
    { header: 'Code', accessor: 'code', render: (row) => <span style={{ color: 'var(--primary)', fontWeight: 600 }}>{row.code}</span> },
    { header: 'Zone', accessor: 'zone' },
    { header: 'Rack', accessor: 'rack' },
    { header: 'Capacity', accessor: 'capacity' },
    { 
      header: 'Status', 
      accessor: 'status',
      render: (row) => {
        let bgColor, color;
        if (row.status === 'AVAILABLE') { bgColor = 'var(--success-bg)'; color = 'var(--success)'; }
        else if (row.status === 'OCCUPIED') { bgColor = 'var(--warning-bg)'; color = 'var(--warning)'; }
        else { bgColor = 'var(--danger-bg)'; color = 'var(--danger)'; }
        
        return (
          <span style={{ padding: '4px 8px', backgroundColor: bgColor, color: color, borderRadius: '12px', fontSize: '12px', fontWeight: 600 }}>
            {row.status}
          </span>
        );
      }
    }
  ];

  return (
    <div className="animate-fade-in">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
        <h1 style={{ fontSize: '24px', fontWeight: 600 }}>Location Management</h1>
        <Button variant="primary" onClick={handleAdd}>
          <Plus size={18} />
          Add Location
        </Button>
      </div>

      <div style={{ backgroundColor: 'var(--bg-paper)', padding: '20px', borderRadius: 'var(--radius-lg)', boxShadow: 'var(--shadow-sm)' }}>
        {loading ? (
          <div style={{ textAlign: 'center', padding: '40px', color: 'var(--text-muted)' }}>
            <Loader size={32} className="animate-spin" style={{ margin: '0 auto', marginBottom: '16px', color: 'var(--primary)' }}/>
            <p>Loading locations...</p>
          </div>
        ) : (
          <Table 
            columns={columns} 
            data={data} 
            onEdit={handleEdit}
            onDelete={handleDelete}
          />
        )}
      </div>

      <Modal 
        isOpen={isModalOpen} 
        onClose={() => setIsModalOpen(false)}
        title={editingRow ? 'Edit Location' : 'Add New Location'}
      >
        <LocationForm 
          initialData={editingRow} 
          warehouses={warehouses}
          onSubmit={handleFormSubmit}
          onCancel={() => setIsModalOpen(false)}
        />
      </Modal>
    </div>
  );
};

export default LocationList;
