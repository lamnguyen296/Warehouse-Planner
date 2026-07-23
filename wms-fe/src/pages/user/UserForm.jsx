import { useEffect, useMemo, useState } from 'react';
import { Check, LockKeyhole } from 'lucide-react';
import Button from '../../components/Button';
import Input from '../../components/Input';
import './UserManagement.css';

const initialForm = {
  username: '',
  password: '',
  fullname: '',
  status: 'ACTIVE',
  roles: [],
};

const UserForm = ({ initialData, availableRoles, currentUserId, onSubmit, onCancel }) => {
  const [formData, setFormData] = useState(initialForm);
  const [submitting, setSubmitting] = useState(false);
  const [formError, setFormError] = useState('');
  const editing = Boolean(initialData);
  const editingSelf = initialData?.id === currentUserId;

  useEffect(() => {
    if (!initialData) {
      setFormData(initialForm);
      return;
    }
    setFormData({
      username: initialData.username || '',
      password: '',
      fullname: initialData.fullname || '',
      status: initialData.status || 'ACTIVE',
      roles: initialData.roles?.map((role) => role.id) || [],
    });
  }, [initialData]);

  const selectedRoles = useMemo(() => new Set(formData.roles), [formData.roles]);

  const handleChange = (event) => {
    const { name, value } = event.target;
    setFormData((previous) => ({ ...previous, [name]: value }));
    setFormError('');
  };

  const toggleRole = (roleId) => {
    if (editingSelf) return;
    setFormData((previous) => ({
      ...previous,
      roles: previous.roles.includes(roleId)
        ? previous.roles.filter((id) => id !== roleId)
        : [...previous.roles, roleId],
    }));
    setFormError('');
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (formData.roles.length === 0) {
      setFormError('Select at least one role.');
      return;
    }

    const payload = {
      fullname: formData.fullname.trim(),
      status: formData.status,
      roles: formData.roles,
    };
    if (!editing) payload.username = formData.username.trim();
    if (formData.password) payload.password = formData.password;

    try {
      setSubmitting(true);
      await onSubmit(payload);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <form className="user-form" onSubmit={handleSubmit}>
      <div className="form-grid">
        <Input
          label="Username"
          name="username"
          value={formData.username}
          onChange={handleChange}
          minLength={4}
          required={!editing}
          disabled={editing}
          autoComplete="off"
          placeholder="e.g. operator01"
        />
        <Input
          label={editing ? 'New password (optional)' : 'Temporary password'}
          name="password"
          type="password"
          value={formData.password}
          onChange={handleChange}
          minLength={6}
          required={!editing}
          autoComplete="new-password"
          placeholder={editing ? 'Leave blank to keep current password' : 'At least 6 characters'}
        />
      </div>

      <Input
        label="Full name"
        name="fullname"
        value={formData.fullname}
        onChange={handleChange}
        required
        placeholder="Employee full name"
      />

      <div className="input-group">
        <label className="input-label" htmlFor="user-status">Account status</label>
        <select
          id="user-status"
          name="status"
          className="input-field"
          value={formData.status}
          onChange={handleChange}
          disabled={editingSelf}
        >
          <option value="ACTIVE">Active</option>
          <option value="INACTIVE">Inactive</option>
          <option value="LOCKED">Locked</option>
        </select>
      </div>

      <fieldset className="role-fieldset">
        <legend>Roles</legend>
        <div className="role-options">
          {availableRoles.map((role) => {
            const selected = selectedRoles.has(role.id);
            return (
              <label key={role.id} className={`role-option ${selected ? 'selected' : ''} ${editingSelf ? 'disabled' : ''}`}>
                <input
                  type="checkbox"
                  checked={selected}
                  onChange={() => toggleRole(role.id)}
                  disabled={editingSelf}
                />
                <span className="role-check" aria-hidden="true">{selected && <Check size={14} />}</span>
                <span>
                  <strong>{role.name}</strong>
                  <small>{role.description}</small>
                </span>
              </label>
            );
          })}
        </div>
      </fieldset>

      {editingSelf && (
        <div className="self-protection-note">
          <LockKeyhole size={16} />
          Your own status and roles are protected to prevent administrator lockout.
        </div>
      )}
      {formError && <p className="error-text">{formError}</p>}

      <div className="form-actions">
        <Button type="button" variant="secondary" onClick={onCancel} disabled={submitting}>Cancel</Button>
        <Button type="submit" variant="primary" disabled={submitting}>
          {submitting ? 'Saving...' : editing ? 'Save changes' : 'Create user'}
        </Button>
      </div>
    </form>
  );
};

export default UserForm;
