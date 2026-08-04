import { useState, useEffect } from 'react';
import { ImagePlus, Trash2 } from 'lucide-react';
import toast from 'react-hot-toast';
import Input from '../../components/Input';
import Button from '../../components/Button';

const ItemForm = ({ initialData, onSubmit, onCancel }) => {
  const [formData, setFormData] = useState({
    code: '',
    name: '',
    itemType: 'RAW_COMPONENT',
    unit: 'CAI',
    description: '',
    status: 'ACTIVE'
  });
  const [imageFile, setImageFile] = useState(null);
  const [previewUrl, setPreviewUrl] = useState('');
  const [removeImage, setRemoveImage] = useState(false);

  useEffect(() => {
    if (initialData) {
      setFormData({
        code: initialData.code || '',
        name: initialData.name || '',
        itemType: initialData.itemType || 'RAW_COMPONENT',
        unit: initialData.unit || 'CAI',
        description: initialData.description || '',
        status: initialData.status || 'ACTIVE',
      });
      setPreviewUrl(initialData.imageUrl || '');
    } else {
      setPreviewUrl('');
    }
    setImageFile(null);
    setRemoveImage(false);
  }, [initialData]);

  useEffect(() => {
    if (!imageFile) return undefined;
    const objectUrl = URL.createObjectURL(imageFile);
    setPreviewUrl(objectUrl);
    return () => URL.revokeObjectURL(objectUrl);
  }, [imageFile]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    onSubmit({ ...formData, imageFile, removeImage });
  };

  const handleImageChange = (event) => {
    const file = event.target.files?.[0];
    if (!file) return;
    if (!['image/jpeg', 'image/png', 'image/webp'].includes(file.type)) {
      toast.error('Choose a JPEG, PNG or WebP image');
      event.target.value = '';
      return;
    }
    if (file.size > 5 * 1024 * 1024) {
      toast.error('Image must not exceed 5 MB');
      event.target.value = '';
      return;
    }
    setImageFile(file);
    setRemoveImage(false);
  };

  const clearImage = () => {
    setImageFile(null);
    setPreviewUrl('');
    setRemoveImage(Boolean(initialData?.imageUrl));
  };

  return (
    <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
      <Input
        label="Item Code"
        name="code"
        value={formData.code}
        onChange={handleChange}
        required
        placeholder="e.g. ITM-001"
      />
      <Input
        label="Item Name"
        name="name"
        value={formData.name}
        onChange={handleChange}
        required
        placeholder="e.g. Bánh Xe Nhựa"
      />
      
      <div style={{ display: 'flex', gap: '16px' }}>
        <div className="input-group" style={{ flex: 1 }}>
          <label className="input-label">Item Type</label>
          <select 
            name="itemType" 
            value={formData.itemType} 
            onChange={handleChange}
            className="input-field"
            disabled={Boolean(initialData)}
          >
            <option value="RAW_COMPONENT">Raw Component</option>
            <option value="FINISHED_COMPONENT">Finished Component</option>
            <option value="SET">Set</option>
          </select>
        </div>

        <div className="input-group" style={{ flex: 1 }}>
          <label className="input-label">Unit</label>
          <select 
            name="unit" 
            value={formData.unit} 
            onChange={handleChange}
            className="input-field"
          >
            <option value="CAI">Cái</option>
            <option value="KG">Kg</option>
            <option value="HOP">Hộp</option>
            <option value="BOH">Bộ</option>
          </select>
        </div>
      </div>

      <div className="input-group">
        <label className="input-label">Description</label>
        <textarea
          name="description"
          value={formData.description}
          onChange={handleChange}
          className="input-field"
          rows={3}
          placeholder="Enter item description..."
        />
      </div>

      <div className="input-group">
        <label className="input-label">Item image</label>
        <div className="item-image-editor">
          <div className="item-image-preview">
            {previewUrl
              ? <img src={previewUrl} alt={`${formData.name || 'Item'} preview`} />
              : <ImagePlus size={28} aria-hidden="true" />}
          </div>
          <div className="item-image-controls">
            <label className="btn btn-secondary btn-md item-image-picker">
              <ImagePlus size={16} />
              {previewUrl ? 'Replace image' : 'Choose image'}
              <input type="file" accept="image/jpeg,image/png,image/webp" onChange={handleImageChange} />
            </label>
            {previewUrl && <Button type="button" variant="secondary" onClick={clearImage}>
              <Trash2 size={16} /> Remove
            </Button>}
            <span className="field-hint">JPEG, PNG or WebP. Maximum 5 MB.</span>
          </div>
        </div>
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
          <option value="DISCONTINUED">Discontinued</option>
        </select>
      </div>

      <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px', marginTop: '16px' }}>
        <Button type="button" variant="secondary" onClick={onCancel}>Cancel</Button>
        <Button type="submit" variant="primary">Save</Button>
      </div>
    </form>
  );
};

export default ItemForm;
