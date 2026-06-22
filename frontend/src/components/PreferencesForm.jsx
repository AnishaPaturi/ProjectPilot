import React, { useState, useEffect } from 'react';
import { preferencesAPI } from '../services/api';
import { Settings, Save, Trash2, Plus, Users, Calendar, Brain, Code } from 'lucide-react';

export default function PreferencesForm({ userId, onSaveSuccess }) {
  const [domain, setDomain] = useState('');
  const [skills, setSkills] = useState('');
  const [teamSize, setTeamSize] = useState(4);
  const [duration, setDuration] = useState(4);
  const [avoidItem, setAvoidItem] = useState('');
  const [avoidList, setAvoidList] = useState([]);
  const [loading, setLoading] = useState(false);
  const [fetching, setFetching] = useState(true);
  const [message, setMessage] = useState('');

  useEffect(() => {
    const fetchPreferences = async () => {
      try {
        const response = await preferencesAPI.get(userId);
        if (response.data) {
          setDomain(response.data.domain || '');
          setSkills(response.data.skills || '');
          setTeamSize(response.data.teamSize || 4);
          setDuration(response.data.duration || 4);
          setAvoidList(response.data.avoidList || []);
        }
      } catch (err) {
        console.error('Failed to load preferences:', err);
      } finally {
        setFetching(false);
      }
    };
    fetchPreferences();
  }, [userId]);

  const handleAddAvoid = (e) => {
    e.preventDefault();
    if (avoidItem.trim() && !avoidList.includes(avoidItem.trim())) {
      setAvoidList([...avoidList, avoidItem.trim()]);
      setAvoidItem('');
    }
  };

  const handleRemoveAvoid = (indexToRemove) => {
    setAvoidList(avoidList.filter((_, idx) => idx !== indexToRemove));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(false);
    setMessage('');
    try {
      setLoading(true);
      await preferencesAPI.save(userId, {
        domain,
        skills,
        teamSize,
        duration,
        avoidList,
      });
      setMessage('Preferences saved successfully!');
      if (onSaveSuccess) onSaveSuccess();
    } catch (err) {
      setMessage('Error saving preferences.');
    } finally {
      setLoading(false);
    }
  };

  if (fetching) {
    return (
      <div className="flex items-center justify-center py-12">
        <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-indigo-500"></div>
      </div>
    );
  }

  return (
    <div className="w-full max-w-2xl mx-auto">
      <div className="glass-panel rounded-2xl p-8 shadow-2xl relative">
        <div className="flex items-center gap-3 mb-6">
          <div className="p-2 bg-indigo-500/10 rounded-lg text-indigo-400">
            <Settings size={22} />
          </div>
          <div>
            <h2 className="text-2xl font-bold text-slate-100">Project Preferences</h2>
            <p className="text-slate-400 text-sm">Configure domains, skills, and topics to avoid</p>
          </div>
        </div>

        {message && (
          <div className={`p-4 rounded-xl mb-6 text-sm border ${
            message.includes('success') 
              ? 'bg-emerald-500/10 border-emerald-500/20 text-emerald-400' 
              : 'bg-red-500/10 border-red-500/20 text-red-400'
          }`}>
            {message}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label className="block text-slate-300 text-xs font-semibold uppercase tracking-wider mb-2 flex items-center gap-1.5">
                <Brain size={14} className="text-slate-400" />
                Target Research Domain
              </label>
              <input
                type="text"
                required
                value={domain}
                onChange={(e) => setDomain(e.target.value)}
                placeholder="e.g. Cybersecurity + AI"
                className="w-full bg-slate-950/60 border border-slate-800 focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 rounded-xl py-3 px-4 text-slate-100 placeholder-slate-700 outline-none transition-all"
              />
            </div>

            <div>
              <label className="block text-slate-300 text-xs font-semibold uppercase tracking-wider mb-2 flex items-center gap-1.5">
                <Code size={14} className="text-slate-400" />
                Skills & Technologies
              </label>
              <input
                type="text"
                required
                value={skills}
                onChange={(e) => setSkills(e.target.value)}
                placeholder="e.g. Java, Python, PyTorch, React"
                className="w-full bg-slate-950/60 border border-slate-800 focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 rounded-xl py-3 px-4 text-slate-100 placeholder-slate-700 outline-none transition-all"
              />
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label className="block text-slate-300 text-xs font-semibold uppercase tracking-wider mb-2 flex items-center gap-1.5">
                <Users size={14} className="text-slate-400" />
                Team Size (Students)
              </label>
              <div className="relative">
                <select
                  value={teamSize}
                  onChange={(e) => setTeamSize(parseInt(e.target.value))}
                  className="w-full bg-slate-950/60 border border-slate-800 focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 rounded-xl py-3 px-4 text-slate-100 outline-none transition-all appearance-none cursor-pointer"
                >
                  <option value={1}>1 (Individual Project)</option>
                  <option value={2}>2 Members</option>
                  <option value={3}>3 Members</option>
                  <option value={4}>4 Members</option>
                </select>
                <div className="pointer-events-none absolute inset-y-0 right-0 flex items-center px-4 text-slate-500">
                  ▼
                </div>
              </div>
            </div>

            <div>
              <label className="block text-slate-300 text-xs font-semibold uppercase tracking-wider mb-2 flex items-center gap-1.5">
                <Calendar size={14} className="text-slate-400" />
                Project Duration (Months)
              </label>
              <div className="relative">
                <select
                  value={duration}
                  onChange={(e) => setDuration(parseInt(e.target.value))}
                  className="w-full bg-slate-950/60 border border-slate-800 focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 rounded-xl py-3 px-4 text-slate-100 outline-none transition-all appearance-none cursor-pointer"
                >
                  <option value={2}>2 Months</option>
                  <option value={3}>3 Months</option>
                  <option value={4}>4 Months</option>
                  <option value={6}>6 Months</option>
                </select>
                <div className="pointer-events-none absolute inset-y-0 right-0 flex items-center px-4 text-slate-500">
                  ▼
                </div>
              </div>
            </div>
          </div>

          {/* Avoid List Sub-module */}
          <div className="border-t border-slate-900 pt-6">
            <label className="block text-slate-300 text-xs font-semibold uppercase tracking-wider mb-2">
              Avoid List (Topics chosen by peers or generic topics)
            </label>
            <p className="text-slate-500 text-xs mb-3">
              The AI Similarity Agent will automatically filter out recommendations matching these projects.
            </p>

            <div className="flex gap-2">
              <input
                type="text"
                value={avoidItem}
                onChange={(e) => setAvoidItem(e.target.value)}
                placeholder="e.g. Fake News Detection"
                className="flex-1 bg-slate-950/60 border border-slate-800 focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 rounded-xl py-2.5 px-4 text-slate-100 placeholder-slate-700 outline-none transition-all"
              />
              <button
                type="button"
                onClick={handleAddAvoid}
                className="px-4 py-2.5 bg-slate-800 hover:bg-slate-700 text-slate-200 rounded-xl flex items-center gap-1.5 transition-all cursor-pointer text-sm font-semibold"
              >
                <Plus size={16} />
                <span>Add</span>
              </button>
            </div>

            {avoidList.length > 0 && (
              <div className="flex flex-wrap gap-2 mt-4 max-h-36 overflow-y-auto p-1.5 rounded-xl border border-slate-900 bg-slate-950/20">
                {avoidList.map((item, idx) => (
                  <div
                    key={idx}
                    className="flex items-center gap-1.5 bg-indigo-500/10 border border-indigo-500/20 text-indigo-300 text-xs font-medium py-1.5 px-3 rounded-full"
                  >
                    <span>{item}</span>
                    <button
                      type="button"
                      onClick={() => handleRemoveAvoid(idx)}
                      className="text-indigo-400 hover:text-indigo-200 focus:outline-none transition-colors cursor-pointer"
                    >
                      <Trash2 size={12} />
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>

          <div className="border-t border-slate-900 pt-6">
            <button
              type="submit"
              disabled={loading}
              className="w-full bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-500 hover:to-purple-500 text-white font-semibold py-3 rounded-xl shadow-lg hover:shadow-indigo-500/20 transition-all flex items-center justify-center gap-2 cursor-pointer disabled:opacity-50"
            >
              {loading ? 'Saving Preferences...' : (
                <>
                  <Save size={18} />
                  <span>Save & Apply Settings</span>
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
