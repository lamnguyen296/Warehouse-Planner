import { useState, useEffect } from 'react';
import { Plus, Loader } from 'lucide-react';
import toast from 'react-hot-toast';
import Table from '../../components/Table';
import Button from '../../components/Button';
import Modal from '../../components/Modal';
import bomService from '../../services/bomService';
import itemService from '../../services/itemService';
import BomForm from './BomForm';
import { useAuth } from '../../auth/useAuth';
import { PERMISSIONS as P } from '../../auth/permissions';

const BomList = () => {
  const { hasPermission } = useAuth();
  const canManage = hasPermission(P.MASTER_DATA_MANAGE);
  const [data, setData] = useState([]);
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(false);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingRow, setEditingRow] = useState(null);

  useEffect(() => {
    fetchBoms();
    fetchItems();
  }, []);

  const fetchItems = async () => {
    try {
      const res = await itemService.getAll();
      setItems(res || []);
    } catch (error) {
      console.error('Failed to load items for bom form');
    }
  };

  const fetchBoms = async () => {
    try {
      setLoading(true);
      const res = await bomService.getAll();
      setData(res || []);
    } catch (error) {
      toast.error('Failed to load boms');
      setData([]);
    } finally {
      setLoading(false);
    }
  };

  const handleAdd = () => {
    if (items.length === 0) {
      toast.error('Please create items first');
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
    if (window.confirm(`Are you sure you want to delete this BOM mapping?`)) {
      try {
        await bomService.delete(row.id);
        toast.success('BOM deleted successfully');
        setData(data.filter(item => item.id !== row.id));
      } catch (error) {
        // global interceptor handles toast
      }
    }
  };

  const handleFormSubmit = async (formData) => {
    try {
      const parent = items.find(i => i.id === Number(formData.parentItemId));
      const child = items.find(i => i.id === Number(formData.childItemId));
      const submitData = { 
        ...formData, 
        parentItemName: parent?.name,
        childItemName: child?.name 
      };

      if (editingRow) {
        const updated = await bomService.update(editingRow.id, submitData);
        updated.parentItemName = parent?.name;
        updated.childItemName = child?.name;
        setData(data.map(item => item.id === editingRow.id ? updated : item));
        toast.success('BOM updated successfully');
      } else {
        const created = await bomService.create(submitData);
        created.parentItemName = parent?.name;
        created.childItemName = child?.name;
        setData([created, ...data]);
        toast.success('BOM created successfully');
      }
      setIsModalOpen(false);
    } catch (error) {
      // global interceptor handles toast
    }
  };

  const columns = [
    { header: 'ID', accessor: 'id' },
    {
      header: 'Parent Item',
      accessor: 'parentItemCode',
      render: (row) => {
        const item = items.find(i => i.id === row.parentItemId);
        return <span style={{ fontWeight: 600, color: 'var(--primary)' }}>{item ? item.name : row.parentItemCode}</span>;
      }
    },
    {
      header: 'Finished Component',
      accessor: 'childItemCode',
      render: (row) => {
        const item = items.find(i => i.id === row.childItemId);
        return <span>{item ? item.name : row.childItemCode}</span>;
      }
    },
    { 
      header: 'Quantity', 
      accessor: 'quantity',
      render: (row) => <strong>{row.quantity}</strong>
    },
    { header: 'Priority', accessor: 'priority' }
  ];

  return (
    <div className="animate-fade-in">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
        <h1 style={{ fontSize: '24px', fontWeight: 600 }}>Bill of Materials (BOM)</h1>
        {canManage && <Button variant="primary" onClick={handleAdd}>
          <Plus size={18} />
          Add BOM Mapping
        </Button>}
      </div>

      <div style={{ backgroundColor: 'var(--bg-paper)', padding: '20px', borderRadius: 'var(--radius-lg)', boxShadow: 'var(--shadow-sm)' }}>
        {loading ? (
          <div style={{ textAlign: 'center', padding: '40px', color: 'var(--text-muted)' }}>
            <Loader size={32} className="animate-spin" style={{ margin: '0 auto', marginBottom: '16px', color: 'var(--primary)' }}/>
            <p>Loading BOMs...</p>
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
        isOpen={isModalOpen} 
        onClose={() => setIsModalOpen(false)}
        title={editingRow ? 'Edit BOM' : 'Add New BOM'}
      >
        <BomForm 
          initialData={editingRow} 
          items={items}
          onSubmit={handleFormSubmit}
          onCancel={() => setIsModalOpen(false)}
        />
      </Modal>
    </div>
  );
};

export default BomList;
