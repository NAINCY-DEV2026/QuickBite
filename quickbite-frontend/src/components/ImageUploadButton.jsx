import { useRef } from 'react';
import { validateImage } from '../utils/image';

export default function ImageUploadButton({ label = 'Change photo', onUpload, onError, disabled }) {
  const inputRef = useRef(null);

  const handleChange = async (e) => {
    const file = e.target.files?.[0];
    e.target.value = ''; // lets the same file be chosen again later
    if (!file) return;

    const problem = validateImage(file);
    if (problem) {
      onError?.(problem);
      return;
    }
    await onUpload(file);
  };

  return (
    <>
      <input
        ref={inputRef}
        type="file"
        accept="image/jpeg,image/png,image/webp,image/gif"
        hidden
        onChange={handleChange}
      />
      <button
        type="button"
        className="btn-ghost"
        disabled={disabled}
        style={{ padding: '0.4rem 0.9rem', fontSize: '0.82rem' }}
        onClick={() => inputRef.current?.click()}
      >
        {disabled ? 'Uploading...' : label}
      </button>
    </>
  );
}