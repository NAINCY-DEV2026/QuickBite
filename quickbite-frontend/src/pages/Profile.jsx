import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  getMyProfile, updateProfile, changePassword, uploadProfileImage, deactivateAccount,
} from '../services/userService';
import { useAuth } from '../context/AuthContext';
import ImageUploadButton from '../components/ImageUploadButton';
import { imageUrl } from '../utils/image';
import { getErrorMessage } from '../utils/errors';
import { formatDate } from '../utils/format';
import { label } from '../utils/status';

const empty = { type: '', text: '' };

export default function Profile() {
  const [profile, setProfile] = useState(null);
  const [form, setForm] = useState({ name: '', phone: '' });
  const [pwForm, setPwForm] = useState({ currentPassword: '', newPassword: '' });
  const [infoMsg, setInfoMsg] = useState(empty);
  const [pwMsg, setPwMsg] = useState(empty);
  const [photoMsg, setPhotoMsg] = useState(empty);
  const [dangerMsg, setDangerMsg] = useState(empty);
  const [uploading, setUploading] = useState(false);
  const [confirmingDeactivate, setConfirmingDeactivate] = useState(false);
  const [deactivatePassword, setDeactivatePassword] = useState('');
  const { logout, updateUser } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    getMyProfile().then((res) => {
      setProfile(res.data);
      setForm({ name: res.data.name, phone: res.data.phone });
    });
  }, []);

  const handleProfileSubmit = async (e) => {
    e.preventDefault();
    try {
      const res = await updateProfile(form);
      setProfile(res.data);
      updateUser({ name: res.data.name });
      setInfoMsg({ type: 'success', text: 'Profile updated' });
    } catch (err) {
      setInfoMsg({ type: 'error', text: getErrorMessage(err, 'Could not update profile') });
    }
  };

  const handlePasswordSubmit = async (e) => {
    e.preventDefault();
    try {
      await changePassword(pwForm);
      setPwForm({ currentPassword: '', newPassword: '' });
      setPwMsg({ type: 'success', text: 'Password changed' });
    } catch (err) {
      setPwMsg({ type: 'error', text: getErrorMessage(err, 'Could not change password') });
    }
  };

  const handlePhoto = async (file) => {
    setUploading(true);
    try {
      const res = await uploadProfileImage(file);
      setProfile(res.data);
      setPhotoMsg({ type: 'success', text: 'Photo updated' });
    } catch (err) {
      setPhotoMsg({ type: 'error', text: getErrorMessage(err, 'Could not upload photo') });
    } finally {
      setUploading(false);
    }
  };

  const handleDeactivate = async (e) => {
    e.preventDefault();
    try {
      await deactivateAccount(deactivatePassword);
      logout();
      navigate('/');
    } catch (err) {
      setDangerMsg({ type: 'error', text: getErrorMessage(err, 'Could not deactivate account') });
    }
  };

  if (!profile) return <div className="container" style={{ paddingTop: '3rem' }}>Loading...</div>;

  const photo = imageUrl(profile.profileImage);

  return (
    <div className="container" style={{ maxWidth: '560px', paddingTop: '3rem', paddingBottom: '2rem' }}>
      <div className="d-flex gap-3 align-items-center mb-4">
        {photo ? (
          <img src={photo} alt={profile.name} className="avatar" />
        ) : (
          <div className="avatar">{profile.name.charAt(0).toUpperCase()}</div>
        )}
        <div style={{ minWidth: 0 }}>
          <h2 style={{ fontSize: '1.7rem', margin: 0 }}>{profile.name}</h2>
          <p style={{ color: '#8A8075', margin: 0, fontSize: '0.9rem' }}>
            {profile.email} · {label(profile.role)} · since {formatDate(profile.createdAt)}
          </p>
          <div className="mt-2">
            <ImageUploadButton
              label={photo ? 'Change photo' : 'Add photo'}
              disabled={uploading}
              onUpload={handlePhoto}
              onError={(text) => setPhotoMsg({ type: 'error', text })}
            />
          </div>
        </div>
      </div>
      {photoMsg.text && <div className={`notice notice-${photoMsg.type}`}>{photoMsg.text}</div>}

      <div className="ticket mb-4">
        <h4 style={{ fontSize: '1.05rem' }}>Basic info</h4>
        {infoMsg.text && <div className={`notice notice-${infoMsg.type}`} style={{ marginTop: '0.75rem' }}>{infoMsg.text}</div>}
        <form onSubmit={handleProfileSubmit}>
          <div className="field">
            <label className="field-label">Full name</label>
            <input className="field-input" value={form.name} maxLength={100}
                   onChange={(e) => setForm({ ...form, name: e.target.value })} required />
          </div>
          <div className="field">
            <label className="field-label">Phone</label>
            <input className="field-input" value={form.phone} inputMode="numeric" maxLength={10}
                   onChange={(e) => setForm({ ...form, phone: e.target.value.replace(/\D/g, '') })} required />
          </div>
          <button type="submit" className="btn-chili w-100">Save changes</button>
        </form>
      </div>

      <div className="ticket mb-4">
        <h4 style={{ fontSize: '1.05rem' }}>Change password</h4>
        {pwMsg.text && <div className={`notice notice-${pwMsg.type}`} style={{ marginTop: '0.75rem' }}>{pwMsg.text}</div>}
        <form onSubmit={handlePasswordSubmit}>
          <div className="field">
            <label className="field-label">Current password</label>
            <input type="password" className="field-input" value={pwForm.currentPassword}
                   onChange={(e) => setPwForm({ ...pwForm, currentPassword: e.target.value })} required />
          </div>
          <div className="field">
            <label className="field-label">New password</label>
            <input type="password" className="field-input" value={pwForm.newPassword} minLength={6} maxLength={72}
                   onChange={(e) => setPwForm({ ...pwForm, newPassword: e.target.value })} required />
          </div>
          <button type="submit" className="btn-ghost w-100">Update password</button>
        </form>
      </div>

      {profile.role !== 'ADMIN' && (
        <div className="danger-zone">
          <h4 style={{ fontSize: '1.05rem', color: 'var(--chili)' }}>Deactivate account</h4>
          <p style={{ color: '#5C5248', fontSize: '0.88rem' }}>
            You will be logged out and will not be able to sign in again.
            {profile.role === 'RESTAURANT_OWNER' && ' Your restaurants will be taken offline.'}
            {' '}Accounts with orders in progress cannot be deactivated.
          </p>
          {dangerMsg.text && <div className={`notice notice-${dangerMsg.type}`}>{dangerMsg.text}</div>}

          {!confirmingDeactivate ? (
            <button className="btn-ghost" style={{ borderColor: 'var(--chili)', color: 'var(--chili)' }}
                    onClick={() => setConfirmingDeactivate(true)}>
              Deactivate my account
            </button>
          ) : (
            <form onSubmit={handleDeactivate}>
              <div className="field">
                <label className="field-label">Enter your password to confirm</label>
                <input type="password" className="field-input" value={deactivatePassword}
                       onChange={(e) => setDeactivatePassword(e.target.value)} required />
              </div>
              <div className="d-flex gap-2">
                <button type="submit" className="btn-chili">Yes, deactivate</button>
                <button type="button" className="btn-ghost"
                        onClick={() => { setConfirmingDeactivate(false); setDeactivatePassword(''); setDangerMsg(empty); }}>
                  Cancel
                </button>
              </div>
            </form>
          )}
        </div>
      )}
    </div>
  );
}