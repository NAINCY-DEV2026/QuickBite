const steps = ['PLACED', 'CONFIRMED', 'PREPARING', 'READY_FOR_PICKUP', 'OUT_FOR_DELIVERY', 'DELIVERED'];
const labels = { PLACED: 'Placed', CONFIRMED: 'Confirmed', PREPARING: 'Preparing', READY_FOR_PICKUP: 'Ready', OUT_FOR_DELIVERY: 'On the way', DELIVERED: 'Delivered' };

export default function OrderStatusTracker({ status }) {
  if (status === 'CANCELLED' || status === 'REJECTED') {
    return (
      <div className="notice notice-error" style={{ fontWeight: 600 }}>
        Order {status === 'CANCELLED' ? 'cancelled' : 'rejected'}
      </div>
    );
  }

  const currentIndex = steps.indexOf(status);

  return (
    <div className="d-flex justify-content-between" style={{ position: 'relative' }}>
      {steps.map((step, i) => (
        <div key={step} className="d-flex flex-column align-items-center" style={{ flex: 1, zIndex: 1 }}>
          <div
            style={{
              width: '26px', height: '26px', borderRadius: '50%',
              background: i <= currentIndex ? 'var(--chili)' : '#fff',
              border: `2px solid ${i <= currentIndex ? 'var(--chili)' : 'var(--line)'}`,
            }}
          />
          <span style={{ fontSize: '0.72rem', marginTop: '0.4rem', textAlign: 'center', color: i <= currentIndex ? 'var(--ink)' : '#B0A89C', fontWeight: i === currentIndex ? 700 : 500 }}>
            {labels[step]}
          </span>
        </div>
      ))}
      <div style={{ position: 'absolute', top: '13px', left: '5%', right: '5%', height: '2px', background: 'var(--line)', zIndex: 0 }} />
    </div>
  );
}