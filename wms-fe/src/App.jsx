import { BrowserRouter } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import AppRoutes from './routes/AppRoutes';
import { AuthProvider } from './auth/AuthContext';
import { RealtimeProvider } from './realtime/RealtimeContext';
import './index.css';

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <RealtimeProvider>
          <Toaster position="top-right" toastOptions={{ duration: 3500 }} />
          <AppRoutes />
        </RealtimeProvider>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
