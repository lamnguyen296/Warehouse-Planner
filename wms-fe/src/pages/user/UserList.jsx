import { useCallback, useEffect, useMemo, useState } from 'react';
import { Loader, UserPlus, UsersRound } from 'lucide-react';
import toast from 'react-hot-toast';
import { useAuth } from '../../auth/useAuth';
import Button from '../../components/Button';
import Modal from '../../components/Modal';
import Table from '../../components/Table';
import userService from '../../services/userService';
import UserForm from './UserForm';
import './UserManagement.css';

const statusClass = {
  ACTIVE: 'success',
  INACTIVE: 'neutral',
  LOCKED: 'danger',
};

const UserList = () => {
  const { user: currentUser } = useAuth();
  const [users, setUsers] = useState([]);
  const [roles, setRoles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [editingUser, setEditingUser] = useState(null);
  const [modalOpen, setModalOpen] = useState(false);

  const loadData = useCallback(async () => {
    try {
      setLoading(true);
      const [userData, roleData] = await Promise.all([userService.getAll(), userService.getRoles()]);
      setUsers(userData || []);
      setRoles((roleData || []).sort((left, right) => left.name.localeCompare(right.name)));
    } catch {
      setUsers([]);
      setRoles([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const counts = useMemo(() => ({
    total: users.length,
    active: users.filter((user) => user.status === 'ACTIVE').length,
    restricted: users.filter((user) => user.status !== 'ACTIVE').length,
  }), [users]);

  const openCreate = () => {
    setEditingUser(null);
    setModalOpen(true);
  };

  const openEdit = (user) => {
    setEditingUser(user);
    setModalOpen(true);
  };

  const handleSubmit = async (payload) => {
    try {
      const savedUser = editingUser
        ? await userService.update(editingUser.id, payload)
        : await userService.create(payload);
      setUsers((current) => editingUser
        ? current.map((user) => user.id === savedUser.id ? savedUser : user)
        : [savedUser, ...current]);
      setModalOpen(false);
      toast.success(editingUser ? 'User updated successfully' : 'User created successfully');
    } catch {
      // The global API interceptor presents the backend error contract.
    }
  };

  const columns = [
    {
      header: 'User',
      accessor: 'username',
      render: (row) => (
        <div className="user-cell">
          <strong>{row.fullname}</strong>
          <span>@{row.username}{row.id === currentUser?.id ? ' (you)' : ''}</span>
        </div>
      ),
    },
    {
      header: 'Roles',
      accessor: 'roles',
      render: (row) => (
        <div className="role-badges">
          {(row.roles || []).map((role) => <span className="role-badge" key={role.id}>{role.name}</span>)}
        </div>
      ),
    },
    {
      header: 'Status',
      accessor: 'status',
      render: (row) => <span className={`status-badge ${statusClass[row.status] || 'neutral'}`}>{row.status}</span>,
    },
  ];

  return (
    <div className="animate-fade-in">
      <div className="page-header">
        <div>
          <h1>User Management</h1>
          <p>Provision internal accounts and assign job roles.</p>
        </div>
        <Button variant="primary" onClick={openCreate} disabled={loading || roles.length === 0}>
          <UserPlus size={18} />
          Create User
        </Button>
      </div>

      <div className="user-summary" aria-label="Account summary">
        <div><span>Total accounts</span><strong>{counts.total}</strong></div>
        <div><span>Active</span><strong className="success-text">{counts.active}</strong></div>
        <div><span>Inactive or locked</span><strong className="danger-text">{counts.restricted}</strong></div>
      </div>

      <section className="surface">
        {loading ? (
          <div className="user-loading">
            <Loader size={30} className="animate-spin" />
            <p>Loading users...</p>
          </div>
        ) : users.length === 0 ? (
          <div className="user-loading">
            <UsersRound size={36} />
            <p>No user accounts found.</p>
          </div>
        ) : (
          <Table columns={columns} data={users} onEdit={openEdit} />
        )}
      </section>

      <Modal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        title={editingUser ? `Edit ${editingUser.username}` : 'Create Internal User'}
        size="lg"
      >
        <UserForm
          initialData={editingUser}
          availableRoles={roles}
          currentUserId={currentUser?.id}
          onSubmit={handleSubmit}
          onCancel={() => setModalOpen(false)}
        />
      </Modal>
    </div>
  );
};

export default UserList;
