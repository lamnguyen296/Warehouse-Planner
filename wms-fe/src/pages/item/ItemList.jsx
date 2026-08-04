import { useState, useEffect } from 'react';
import { Image as ImageIcon, Plus, Loader } from 'lucide-react';
import toast from 'react-hot-toast';
import Table from '../../components/Table';
import Button from '../../components/Button';
import Modal from '../../components/Modal';
import itemService from '../../services/itemService';
import ItemForm from './ItemForm';
import { useAuth } from '../../auth/useAuth';
import { PERMISSIONS as P } from '../../auth/permissions';

const ItemList = () => {
  const { hasPermission } = useAuth();
  const canManage = hasPermission(P.MASTER_DATA_MANAGE);
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingRow, setEditingRow] = useState(null);

  useEffect(() => {
    fetchItems();
  }, []);

  const fetchItems = async () => {
    try {
      setLoading(true);
      const res = await itemService.getAll();
      setData(res || []);
    } catch (error) {
      toast.error('Failed to load items');
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
        await itemService.delete(row.id);
        toast.success('Item deleted successfully');
        setData(data.filter(item => item.id !== row.id));
      } catch (error) {
        // global interceptor handles toast
      }
    }
  };

  const handleFormSubmit = async ({ imageFile, removeImage, ...formData }) => {
    let saved = null;
    try {
      if (editingRow) {
        saved = await itemService.update(editingRow.id, formData);
      } else {
        saved = await itemService.create(formData);
      }

      if (imageFile) saved = await itemService.uploadImage(saved.id, imageFile);
      else if (removeImage && saved.imageUrl) saved = await itemService.deleteImage(saved.id);

      setData((current) => editingRow
        ? current.map((item) => item.id === saved.id ? saved : item)
        : [saved, ...current]);
      toast.success(editingRow ? 'Item updated successfully' : 'Item created successfully');
      setIsModalOpen(false);
    } catch (error) {
      if (saved) {
        setEditingRow(saved);
        await fetchItems();
      }
    }
  };

  const columns = [
    { header: 'Image', accessor: 'imageUrl', render: (row) => row.imageUrl
      ? <img className="item-table-image" src={row.imageUrl} alt={row.name} />
      : <span className="item-table-image empty"><ImageIcon size={18} /></span> },
    { header: 'Code', accessor: 'code', render: (row) => <strong>{row.code}</strong> },
    { header: 'Name', accessor: 'name' },
    { 
      header: 'Type', 
      accessor: 'itemType',
      render: (row) => {
        let bgColor = 'var(--bg-main)', color = 'var(--text-secondary)';
        if (row.itemType === 'SET') { bgColor = '#fdf4ff'; color = '#c026d3'; } // fuchsia
        else if (row.itemType === 'FINISHED_COMPONENT') { bgColor = 'var(--primary-light)'; color = 'var(--primary)'; }
        else if (row.itemType === 'RAW_COMPONENT') { bgColor = 'var(--warning-bg)'; color = '#d97706'; } // orange
        
        return (
          <span style={{ padding: '4px 8px', backgroundColor: bgColor, color: color, borderRadius: '12px', fontSize: '12px', fontWeight: 600 }}>
            {row.itemType?.replace('_', ' ')}
          </span>
        );
      }
    },
    { 
      header: 'Unit', 
      accessor: 'unit',
      render: (row) => {
        const unitMap = { 'CAI': 'Cái', 'KG': 'Kg', 'HOP': 'Hộp', 'BOH': 'Bộ' };
        return unitMap[row.unit] || row.unit || 'Cái';
      }
    },
    { 
      header: 'Status', 
      accessor: 'status',
      render: (row) => {
        let bgColor, color;
        if (row.status === 'ACTIVE') { bgColor = 'var(--success-bg)'; color = 'var(--success)'; }
        else if (row.status === 'DISCONTINUED') { bgColor = 'var(--danger-bg)'; color = 'var(--danger)'; }
        else { bgColor = 'var(--warning-bg)'; color = 'var(--warning)'; }
        
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
        <h1 style={{ fontSize: '24px', fontWeight: 600 }}>Item Management</h1>
        {canManage && <Button variant="primary" onClick={handleAdd}>
          <Plus size={18} />
          Add Item
        </Button>}
      </div>

      <div style={{ backgroundColor: 'var(--bg-paper)', padding: '20px', borderRadius: 'var(--radius-lg)', boxShadow: 'var(--shadow-sm)' }}>
        {loading ? (
          <div style={{ textAlign: 'center', padding: '40px', color: 'var(--text-muted)' }}>
            <Loader size={32} className="animate-spin" style={{ margin: '0 auto', marginBottom: '16px', color: 'var(--primary)' }}/>
            <p>Loading items...</p>
          </div>
        ) : (
          <Table 
            columns={columns} 
            data={data} 
            onEdit={canManage ? handleEdit : undefined}
            onDelete={canManage ? handleDelete : undefined}
          />
        )}
      </div>

      <Modal 
        size="lg"
        isOpen={isModalOpen} 
        onClose={() => setIsModalOpen(false)}
        title={editingRow ? 'Edit Item' : 'Add New Item'}
      >
        <ItemForm 
          initialData={editingRow} 
          onSubmit={handleFormSubmit}
          onCancel={() => setIsModalOpen(false)}
        />
      </Modal>
    </div>
  );
};

export default ItemList;
