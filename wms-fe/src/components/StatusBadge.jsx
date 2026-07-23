const STATUS_TONES = {
  ACTIVE: 'success', AVAILABLE: 'success', APPROVED: 'success', RECEIVED: 'success',
  COMPLETED: 'success', RESERVED: 'info', PLANNING: 'info', SUBMITTED: 'info',
  PENDING: 'neutral', DRAFT: 'neutral', IN_PROGRESS: 'warning', IN_TRANSIT: 'warning',
  EXECUTING: 'warning', PARTIAL_RECEIVED: 'warning', ORDERED: 'info',
  INACTIVE: 'danger', LOCKED: 'danger', FAILED: 'danger', CANCELLED: 'danger',
  RELEASED: 'neutral', EXPIRED: 'danger', CONSUMED: 'success', MAINTENANCE: 'danger',
};

const StatusBadge = ({ value }) => {
  if (!value) return <span className="status-badge neutral">N/A</span>;
  const tone = STATUS_TONES[value] || 'neutral';
  return <span className={`status-badge ${tone}`}>{value.replaceAll('_', ' ')}</span>;
};

export default StatusBadge;
