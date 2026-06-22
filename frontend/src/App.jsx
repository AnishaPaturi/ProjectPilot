import React, { useState, useEffect } from 'react';
import Login from './components/Login';
import Register from './components/Register';
import PreferencesForm from './components/PreferencesForm';
import Dashboard from './components/Dashboard';
import { Compass, Settings, LogOut, BookOpen } from 'lucide-react';
import './App.css';

function App() {
  const [user, setUser] = useState(null);
  const [authView, setAuthView] = useState('login'); // 'login' | 'register'
  const [activeTab, setActiveTab] = useState('dashboard'); // 'dashboard' | 'preferences'
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const storedUser = localStorage.getItem('project_pilot_user');
    if (storedUser) {
      try {
        setUser(JSON.parse(storedUser));
      } catch (e) {
        console.error('Failed to parse user session:', e);
      }
    }
    setLoading(false);
  }, []);

  const handleLoginSuccess = (userData) => {
    setUser(userData);
    localStorage.setItem('project_pilot_user', JSON.stringify(userData));
    setActiveTab('dashboard');
  };

  const handleLogout = () => {
    setUser(null);
    localStorage.removeItem('project_pilot_user');
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-950">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-500"></div>
      </div>
    );
  }

  return (
    <div className="relative min-h-screen pb-16">
      {/* Decorative Blur Blobs */}
      <div className="absolute top-1/4 left-1/4 -translate-x-1/2 -translate-y-1/2 w-96 h-96 bg-blob-indigo opacity-30 pointer-events-none rounded-full blur-3xl"></div>
      <div className="absolute bottom-1/4 right-1/4 translate-x-1/2 translate-y-1/2 w-96 h-96 bg-blob-purple opacity-20 pointer-events-none rounded-full blur-3xl"></div>

      {/* Main Navigation Header */}
      <header className="glass-panel border-b border-white/5 sticky top-0 z-50 shadow-md">
        <div className="max-w-6xl mx-auto px-4 md:px-6 h-16 flex justify-between items-center">
          <div className="flex items-center gap-3">
            <div className="p-2 bg-gradient-to-tr from-indigo-600 to-purple-600 rounded-xl shadow-lg shadow-indigo-500/20 text-white">
              <Compass size={20} />
            </div>
            <div>
              <span className="font-extrabold text-lg tracking-tight bg-clip-text text-transparent bg-gradient-to-r from-white via-slate-100 to-slate-400">
                ProjectPilot <span className="text-indigo-400">AI</span>
              </span>
            </div>
          </div>

          {user && (
            <div className="flex items-center gap-2 md:gap-4">
              <button
                onClick={() => setActiveTab('dashboard')}
                className={`px-4 py-2 rounded-xl text-sm font-semibold flex items-center gap-1.5 transition-all cursor-pointer ${
                  activeTab === 'dashboard'
                    ? 'bg-indigo-600/10 border border-indigo-500/20 text-indigo-300'
                    : 'text-slate-400 hover:text-slate-200 border border-transparent'
                }`}
              >
                <BookOpen size={16} />
                <span className="hidden sm:inline">Recommendations</span>
              </button>

              <button
                onClick={() => setActiveTab('preferences')}
                className={`px-4 py-2 rounded-xl text-sm font-semibold flex items-center gap-1.5 transition-all cursor-pointer ${
                  activeTab === 'preferences'
                    ? 'bg-indigo-600/10 border border-indigo-500/20 text-indigo-300'
                    : 'text-slate-400 hover:text-slate-200 border border-transparent'
                }`}
              >
                <Settings size={16} />
                <span className="hidden sm:inline">Settings</span>
              </button>

              <div className="w-[1px] h-6 bg-slate-800"></div>

              <button
                onClick={handleLogout}
                className="p-2 bg-slate-900 hover:bg-slate-800 text-slate-400 hover:text-red-400 rounded-xl transition-all cursor-pointer flex items-center gap-1"
                title="Log Out"
              >
                <LogOut size={16} />
              </button>
            </div>
          )}
        </div>
      </header>

      {/* Main Body Content */}
      <main className="max-w-6xl mx-auto px-4 md:px-6 pt-10 relative z-10">
        {!user ? (
          <div className="py-12 flex flex-col items-center">
            {/* Landing Hero Title */}
            <div className="text-center max-w-2xl mx-auto mb-12 space-y-4">
              <h1 className="text-4xl md:text-5xl font-extrabold tracking-tight leading-tight text-transparent bg-clip-text bg-gradient-to-r from-white via-slate-100 to-slate-500">
                Plan Your Final-Year Major Project Instantly
              </h1>
              <p className="text-slate-400 text-base md:text-lg">
                Automatically fetch, verify, and rank IEEE journal papers based on your skills, constraints, and classmates' topics. Get a custom week-by-week implementation plan.
              </p>
            </div>

            {authView === 'login' ? (
              <Login 
                onLoginSuccess={handleLoginSuccess} 
                onToggleRegister={() => setAuthView('register')} 
              />
            ) : (
              <Register 
                onRegisterSuccess={handleLoginSuccess} 
                onToggleLogin={() => setAuthView('login')} 
              />
            )}
          </div>
        ) : (
          <div className="space-y-6">
            {/* Header banner greeting */}
            <div className="flex justify-between items-center flex-wrap gap-4 border-b border-slate-900 pb-4">
              <div>
                <h1 className="text-3xl font-extrabold text-slate-100 tracking-tight">
                  Hello, {user.name}
                </h1>
                <p className="text-slate-500 text-sm">
                  Research planner dashboard for {user.email}
                </p>
              </div>
            </div>

            {activeTab === 'dashboard' ? (
              <Dashboard userId={user.id} />
            ) : (
              <PreferencesForm 
                userId={user.id} 
                onSaveSuccess={() => setActiveTab('dashboard')} 
              />
            )}
          </div>
        )}
      </main>
    </div>
  );
}

export default App;
