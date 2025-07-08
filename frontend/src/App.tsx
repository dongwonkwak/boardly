import { useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import './config/i18n';
import Navbar from '@/components/layout/Navbar';
import Dashboard from '@/pages/Dashboard';
import Footer from '@/components/layout/Footer';

function App() {
  useEffect(() => {
    // i18n is already initialized in the import
  }, []);

  return (
    <Router>
      <div className="min-h-screen flex flex-col">
        <Navbar />
        <Routes>
          <Route path="/" element={<Dashboard />} />
        </Routes>
        <Footer />
      </div>
    </Router>
  );
}

export default App;
