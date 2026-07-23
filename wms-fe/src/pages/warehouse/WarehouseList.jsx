import { useState, useEffect } from 'react';
import { Plus, Loader } from 'lucide-react';
import toast from 'react-hot-toast';
import Table from '../../components/Table';
import Button from '../../components/Button';
import Modal from '../../components/Modal';
import warehouseService from '../../services/warehouseService';
import WarehouseForm from './WarehouseForm';

const WarehouseList = () => {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingRow, setEditingRow] = useState(null);

  useEffect(() => {
    fetchWarehouses();
  }, []);

  const fetchWarehouses = async () => {
    try {
      setLoading(true);
      const res = await warehouseService.getAll();
      setData(res || []);
    } catch (error) {
      toast.error('Failed to load warehouses');
      setData([]);
    } finally {
      setLoading(false);
    }
  };

  const handleAdd = () => {
    setEditingRow(null);
    setIsModalOpen(true);
  };

  const handleEdit = (row) => {
    setEditingRow(row);
    setIsModalOpen(true);
  };

  const handleDelete = async (row) => {
    if (window.confirm(`Are you sure you want to delete ${row.name}?`)) {
      try {
        await warehouseService.delete(row.id);
        toast.success('Warehouse deleted successfully');
        setData(data.filter(item => item.id !== row.id));
      } catch (error) {
        // global interceptor handles toast
      }
    }
  };

  const handleFormSubmit = async (formData) => {
    try {
      if (editingRow) {
        const updated = await warehouseService.update(editingRow.id, formData);
        setData(data.map(item => item.id === editingRow.id ? updated : item));
        toast.success('Warehouse updated successfully');
      } else {
        const created = await warehouseService.create(formData);
        setData([created, ...data]);
        toast.success('Warehouse created successfully');
      }
      setIsModalOpen(false);
    } catch (error) {
      // global interceptor handles toast
    }
  };

  const columns = [
    { header: 'ID', accessor: 'id' },
    { header: 'Code', accessor: 'code', render: (row) => <strong>{row.code}</strong> },
    { header: 'Name', accessor: 'name' },
    { 
      header: 'Type', 
      accessor: 'type',
      render: (row) => (
        <span style={{ padding: '4px 8px', backgroundColor: 'var(--primary-light)', color: 'var(--primary)', borderRadius: '12px', fontSize: '12px', fontWeight: 600 }}>
          {row.type?.replace('_', ' ')}
        </span>
      )
    },
    { 
      header: 'Status', 
      accessor: 'status',
      render: (row) => (
        <span style={{ 
          padding: '4px 8px', 
          backgroundColor: row.status === 'ACTIVE' ? 'var(--success-bg)' : 'var(--danger-bg)', 
          color: row.status === 'ACTIVE' ? 'var(--success)' : 'var(--danger)', 
          borderRadius: '12px', 
          fontSize: '12px', 
          fontWeight: 600 
        }}>
          {row.status}
        </span>
      )
    }
  ];

  return (
    <div className="animate-fade-in">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
        <h1 style={{ fontSize: '24px', fontWeight: 600 }}>Warehouse Management</h1>
        <Button variant="primary" onClick={handleAdd}>
          <Plus size={18} />
          Add Warehouse
        </Button>
      </div>

      <div style={{ backgroundColor: 'var(--bg-paper)', padding: '20px', borderRadius: 'var(--radius-lg)', boxShadow: 'var(--shadow-sm)' }}>
        {loading ? (
          <div style={{ textAlign: 'center', padding: '40px', color: 'var(--text-muted)' }}>
            <Loader size={32} className="animate-spin" style={{ margin: '0 auto', marginBottom: '16px', color: 'var(--primary)' }}/>
            <p>Loading warehouses...</p>
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
        title={editingRow ? 'Edit Warehouse' : 'Add New Warehouse'}
      >
        <WarehouseForm 
          initialData={editingRow} 
          onSubmit={handleFormSubmit}
          onCancel={() => setIsModalOpen(false)}
        />
      </Modal>
    </div>
  );
};

export default WarehouseList;
