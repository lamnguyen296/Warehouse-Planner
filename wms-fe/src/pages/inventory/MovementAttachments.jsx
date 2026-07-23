import { useEffect, useRef, useState } from 'react';
import { ExternalLink, ImagePlus, LoaderCircle, Trash2, Upload } from 'lucide-react';
import toast from 'react-hot-toast';
import Button from '../../components/Button';
import inventoryService from '../../services/inventoryService';

const formatFileSize = (bytes) => {
  if (!bytes) return '0 KB';
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(0)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
};

const MovementAttachments = ({ transactionId, canManage }) => {
  const inputRef = useRef(null);
  const [attachments, setAttachments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);

  useEffect(() => {
    let active = true;
    setLoading(true);
    inventoryService.getTransactionAttachments(transactionId)
      .then((result) => { if (active) setAttachments(result || []); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [transactionId]);

  const upload = async (event) => {
    const file = event.target.files?.[0];
    event.target.value = '';
    if (!file) return;
    if (!['image/jpeg', 'image/png', 'image/webp'].includes(file.type)) {
      toast.error('Choose a JPEG, PNG or WebP image');
      return;
    }
    if (file.size > 5 * 1024 * 1024) {
      toast.error('Image must not exceed 5 MB');
      return;
    }

    setUploading(true);
    try {
      const created = await inventoryService.uploadTransactionAttachment(transactionId, file);
      setAttachments((current) => [created, ...current]);
      toast.success('Evidence uploaded');
    } finally {
      setUploading(false);
    }
  };

  const remove = async (attachment) => {
    if (!window.confirm(`Delete ${attachment.originalFilename}?`)) return;
    await inventoryService.deleteTransactionAttachment(transactionId, attachment.id);
    setAttachments((current) => current.filter((item) => item.id !== attachment.id));
    toast.success('Evidence deleted');
  };

  return <section className="movement-evidence">
    <div className="movement-evidence-header">
      <div>
        <div className="section-label">Photo evidence</div>
        <p>Delivery notes, received goods or transfer condition photos.</p>
      </div>
      {canManage && <>
        <input ref={inputRef} className="visually-hidden" type="file"
          accept="image/jpeg,image/png,image/webp" onChange={upload} />
        <Button type="button" variant="secondary" size="sm" disabled={uploading}
          onClick={() => inputRef.current?.click()}>
          {uploading ? <LoaderCircle className="animate-spin" size={15} /> : <Upload size={15} />}
          {uploading ? 'Uploading...' : 'Upload'}
        </Button>
      </>}
    </div>

    {loading ? <div className="movement-evidence-empty"><LoaderCircle className="animate-spin" size={20} /> Loading evidence...</div>
      : attachments.length === 0
        ? <div className="movement-evidence-empty"><ImagePlus size={20} /> No evidence attached</div>
        : <div className="movement-evidence-grid">{attachments.map((attachment) =>
          <article className="evidence-card" key={attachment.id}>
            <a href={attachment.secureUrl} target="_blank" rel="noreferrer" title="Open original image">
              <img src={attachment.secureUrl} alt={attachment.originalFilename} />
              <span><ExternalLink size={14} /></span>
            </a>
            <div className="evidence-card-meta">
              <strong title={attachment.originalFilename}>{attachment.originalFilename}</strong>
              <small>{formatFileSize(attachment.fileSize)} / {attachment.uploadedBy}</small>
            </div>
            {canManage && <button type="button" className="icon-action danger"
              onClick={() => remove(attachment)} title="Delete evidence" aria-label="Delete evidence">
              <Trash2 size={15} />
            </button>}
          </article>)}</div>}
  </section>;
};

export default MovementAttachments;
