import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { Lock } from 'lucide-react';
import Input from '../../components/Input';
import Button from '../../components/Button';
import { useAuth } from '../../auth/useAuth';

const Login = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const { login } = useAuth();

  const handleLogin = async (e) => {
    e.preventDefault();
    if (!username || !password) {
      toast.error('Please enter username and password');
      return;
    }

    try {
      setLoading(true);
      await login(username, password);
      toast.success('Login successful');
      navigate('/dashboard', { replace: true });
    } catch (error) {
      toast.error(error.response?.data?.message || 'Invalid username or password');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', backgroundColor: 'var(--bg-main)', padding: '20px' }}>
      <div style={{ width: '100%', maxWidth: '400px', backgroundColor: 'var(--bg-paper)', borderRadius: 'var(--radius-lg)', boxShadow: 'var(--shadow-lg)', padding: '40px', textAlign: 'center' }} className="glass">
        <div style={{ display: 'inline-flex', alignItems: 'center', justifyContent: 'center', width: '64px', height: '64px', borderRadius: '50%', backgroundColor: 'var(--primary-light)', color: 'var(--primary)', marginBottom: '24px' }}>
          <Lock size={32} />
        </div>
        <h1 style={{ fontSize: '24px', fontWeight: 700, marginBottom: '8px', color: 'var(--text-primary)' }}>Welcome to WMS</h1>
        <p style={{ color: 'var(--text-muted)', marginBottom: '32px' }}>Sign in to continue</p>
        
        <form onSubmit={handleLogin} style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          <Input 
            label="Username"
            placeholder="Enter your username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
          />
          <Input 
            label="Password"
            type="password"
            placeholder="Enter your password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
          <Button type="submit" variant="primary" style={{ marginTop: '12px', padding: '12px' }} disabled={loading}>
            {loading ? 'Signing in...' : 'Sign In'}
          </Button>
        </form>
      </div>
    </div>
  );
};

export default Login;
